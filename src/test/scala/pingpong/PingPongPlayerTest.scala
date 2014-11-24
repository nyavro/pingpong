package pingpong

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._

/**
 * Created by eny on 24.11.14.
 */
class PingPongPlayerTest(_system: ActorSystem) extends TestKit(_system) with FunSuite with BeforeAndAfterAll with ImplicitSender {

  def this() = this(ActorSystem("PostponeSpec"))

  test("starts pinging by GO! command") {
    val playerA = system.actorOf(Props[PingPongPlayer])
    playerA ! ("GO!", self)
    expectMsg("ping")
    expectMsg("ping")
    expectMsg("ping")
  }

  test("after M pings player pongs") {
    val ponging = system.actorOf(Props[PingPongPlayer])
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    expectMsg("pong")
  }

  test("after K pongs players switch") {
    val player = system.actorOf(Props[PingPongPlayer])
    player ! ("GO!", self)  //player is pinger now
    player ! "pong"
    player ! "pong"
    player ! "pong"
    player ! "pong"
    player ! "pong"
    expectMsg("start")
  }

  test("after G cycles game finished") {
    val player = system.actorOf(Props[PingPongPlayer])
  }

}
