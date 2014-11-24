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

  val N = 10.millisecond
  val B = 10.seconds
  val M = 4
  val K = 5
  val G = 3

  test("starts pinging by GO! command") {
    val playerA = system.actorOf(PingPongPersistent.props(N, B, M, K, G, System.currentTimeMillis.toString))
    playerA ! ("GO!", self)
    expectMsg("ping")
    expectMsg("ping")
    expectMsg("ping")
    expectMsg("ping")
    expectNoMsg()
  }

  test("after M pings player pongs") {
    val ponging = system.actorOf(PingPongPersistent.props(N, B, M, K, G, System.currentTimeMillis.toString))
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    expectMsg("pong")
    expectNoMsg()
  }

  test("after K pongs players switch") {
    def expect(msg:Any, count:Int) = for(i<-1 to count) {expectMsg(msg)}
    val player = system.actorOf(PingPongPersistent.props(N, B, M, 3, G, System.currentTimeMillis.toString))
    player ! ("GO!", self)  //player is pinger now
    expect("ping",4)
    player ! "pong"
    expect("ping",4)
    player ! "pong"
    expect("ping",4)
    player ! "pong"
    expectMsg(("GO!", player))
    expectNoMsg()
  }

  test("runs full cycle") {
    val player = system.actorOf(PingPongPersistent.props(10.milliseconds, B, 2, 2, 1, System.currentTimeMillis.toString))
    player ! ("GO!", self)
    expectMsg("ping")
    expectMsg("ping")
    player ! "pong"
    expectMsg("ping")
    expectMsg("ping")
    player ! "pong"
    expectMsg(("GO!", player))
    player ! "ping"
    player ! "ping"
    expectMsg("pong")
    player ! "ping"
    player ! "ping"
    expectMsg("pong")
    expectNoMsg()
  }

  test("handles problem during ponging") {
    val player = system.actorOf(PingPongPersistent.props(10.milliseconds, B, 2, 2, 1, System.currentTimeMillis.toString))
    player ! "ping"
    player ! "failure"
    player ! "ping"
    expectMsg("pong")
    expectNoMsg()
  }
}
