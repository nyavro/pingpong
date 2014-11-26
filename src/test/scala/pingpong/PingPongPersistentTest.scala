package pingpong

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._

import scala.concurrent.duration._

/**
 * Created by eny on 24.11.14.
 */
class PingPongPersistentTest(_system: ActorSystem) extends TestKit(_system) with FunSuite with BeforeAndAfterAll with ImplicitSender {

  def this() = this(ActorSystem("PostponeSpec"))

  override def afterAll() = system.shutdown()

  test("starts pinging by GO! command") {
    val playerA = system.actorOf(PingPongPersistent.props(8.millis, 4, 5, System.currentTimeMillis.toString))
    playerA ! Go(self, State(1, self))
    expectMsg(Ping(State(1, self)))
    expectMsg(Ping(State(1, self)))
    expectMsg(Ping(State(1, self)))
    expectMsg(Ping(State(1, self)))
    expectNoMsg()
  }

  test("after M pings player pongs") {
    val ponging = system.actorOf(PingPongPersistent.props(6.millis, 4, 5, System.currentTimeMillis.toString))
    ponging ! Ping(State(1, self))
    ponging ! Ping(State(1, self))
    ponging ! Ping(State(1, self))
    ponging ! Ping(State(1, self))
    expectMsg(Pong(State(1, self)))
    expectNoMsg()
  }

  test("after K pongs players switch") {
    def expect(msg:Any, count:Int) = for(i<-1 to count) {expectMsg(msg)}
    val player = system.actorOf(PingPongPersistent.props(5.millis, 4, 3, System.currentTimeMillis.toString))
    player ! Go(self, State(1, self))  //player is pinger now
    expect(Ping(State(1, self)),4)
    player ! Pong(State(1, self))
    expect(Ping(State(1, self)),4)
    player ! Pong(State(1, self))
    expect(Ping(State(1, self)),4)
    player ! Pong(State(1, self))
    expectMsg(Go(player, State(0, self)))
    expectNoMsg()
  }

  test("runs full cycle") {
    val player = system.actorOf(PingPongPersistent.props(10.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Go(self, State(2, self))
    expectMsg(Ping(State(2, self)))
    expectMsg(Ping(State(2, self)))
    player ! Pong(State(2, self))
    expectMsg(Ping(State(2, self)))
    expectMsg(Ping(State(2, self)))
    player ! Pong(State(2, self))
    expectMsg(Go(player, State(1, self)))
    player ! Ping(State(1, self))
    player ! Ping(State(1, self))
    expectMsg(Pong(State(1, self)))
    player ! Ping(State(1, self))
    player ! Ping(State(1, self))
    expectMsg(Pong(State(1, self)))
    expectNoMsg()
  }

  test("handles problem during ponging") {
    val player = system.actorOf(PingPongPersistent.props(10.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Ping(State(1, self))
    player ! "failure"
    expectMsg("failure emulated")
    player ! Ping(State(1, self))
    expectMsg(Pong(State(1, self)))
    expectNoMsg()
  }

  test("ignores failures during pinging") {
    val player = system.actorOf(PingPongPersistent.props(9.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Go(self, State(1, self))
    player ! "failure"
    expectMsg(Ping(State(1, self)))
    expectMsg(Ping(State(1, self)))
    expectNoMsg()
  }

  test("Sends Finished marker after all sets done") {
    val player = system.actorOf(PingPongPersistent.props(9.millis, 1, 1, System.currentTimeMillis.toString))
    player ! Go(self, State(0, self))
    expectMsg("Finished")
    expectNoMsg()
  }
}
