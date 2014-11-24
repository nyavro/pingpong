package pingpong

import akka.actor.ActorSystem
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
    playerA ! Go(self, 1)
    expectMsg(Ping(1))
    expectMsg(Ping(1))
    expectMsg(Ping(1))
    expectMsg(Ping(1))
    expectNoMsg()
  }

  test("after M pings player pongs") {
    val ponging = system.actorOf(PingPongPersistent.props(6.millis, 4, 5, System.currentTimeMillis.toString))
    ponging ! Ping(1)
    ponging ! Ping(1)
    ponging ! Ping(1)
    ponging ! Ping(1)
    expectMsg(Pong(1))
    expectNoMsg()
  }

  test("after K pongs players switch") {
    def expect(msg:Any, count:Int) = for(i<-1 to count) {expectMsg(msg)}
    val player = system.actorOf(PingPongPersistent.props(5.millis, 4, 3, System.currentTimeMillis.toString))
    player ! Go(self, 1)  //player is pinger now
    expect(Ping(1),4)
    player ! Pong(1)
    expect(Ping(1),4)
    player ! Pong(1)
    expect(Ping(1),4)
    player ! Pong(1)
    expectMsg(Go(player, 0))
    expectNoMsg()
  }

  test("runs full cycle") {
    val player = system.actorOf(PingPongPersistent.props(10.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Go(self, 2)
    expectMsg(Ping(2))
    expectMsg(Ping(2))
    player ! Pong(2)
    expectMsg(Ping(2))
    expectMsg(Ping(2))
    player ! Pong(2)
    expectMsg(Go(player, 1))
    player ! Ping(1)
    player ! Ping(1)
    expectMsg(Pong(1))
    player ! Ping(1)
    player ! Ping(1)
    expectMsg(Pong(1))
    expectNoMsg()
  }

  test("handles problem during ponging") {
    val player = system.actorOf(PingPongPersistent.props(10.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Ping(1)
    player ! "failure"
    expectMsg("failure emulated")
    player ! Ping(1)
    expectMsg(Pong(1))
    expectNoMsg()
  }

  test("ignores failures during pinging") {
    val player = system.actorOf(PingPongPersistent.props(9.millis, 2, 2, System.currentTimeMillis.toString))
    player ! Go(self, 1)
    player ! "failure"
    expectMsg(Ping(1))
    expectMsg(Ping(1))
    expectNoMsg()
  }
}
