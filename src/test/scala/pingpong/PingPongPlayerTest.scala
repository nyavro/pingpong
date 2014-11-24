package pingpong

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._
import scala.concurrent.duration._

/**
 * Created by eny on 24.11.14.
 */
class PingPongPlayerTest(_system: ActorSystem) extends TestKit(_system) with FunSuite with BeforeAndAfterAll with ImplicitSender {

  def this() = this(ActorSystem("PostponeSpec"))

  val N = 1.second
  val B = 10.seconds
  val M = 4
  val K = 5
  val G = 3

  test("starts pinging by GO! command") {
    val playerA = system.actorOf(PingPongPlayer.props(N, B, M, K, G))
    playerA ! ("GO!", self)
    expectMsg("ping")
    expectMsg("ping")
    expectMsg("ping")
  }

  test("after M pings player pongs") {
    val ponging = system.actorOf(PingPongPlayer.props(N, B, M, K, G))
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    ponging ! "ping"
    expectMsg("pong")
  }

  test("after K pongs players switch") {
    val player = system.actorOf(PingPongPlayer.props(N, B, M, K, G))
    player ! ("GO!", self)  //player is pinger now
    player ! "pong"
    player ! "pong"
    player ! "pong"
    player ! "pong"
    player ! "pong"
    expectMsg("start")
  }

  test("after G cycles game finished") {
    val player = system.actorOf(PingPongPlayer.props(N, B, M, K, G))
  }

}
