package pingpong

import java.util.Date

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import scala.concurrent.duration._

/**
 * Created by eny on 23.11.14.
 */

class PingPongPlayer() extends Actor {

  val N = 1.second
  val B = 10.seconds
  val M = 4
  val K = 5
  val G = 3

  val delayBetweenPings = N
  val delayBetweenBreaks = B
  val pingsToPong = M
  val pongsToSwitch = K
  val setsCount = G

  override def receive: Receive = ponging(0)

  def pinging(round:Int, count:Int): Receive = {
    case ("start", partner:ActorRef) =>
      for(i<-1 to pingsToPong) {
        try {
          Thread.sleep(delayBetweenPings.toMillis)
          partner ! "ping"
        } catch {
          case e:InterruptedException => Thread.interrupted()
        }
      }
    case "pong" =>
      val pongsReceived = count + 1
      if(pongsReceived >= pongsToSwitch) {
        context become ponging(0)
        sender ! "start"
      } else {
        context become pinging(round, pongsReceived)
      }
  }
  
  def ponging(count:Int): Receive = {
    case "ping" =>
      val pingsReceived = count + 1
      println(s"ping $pingsReceived received at: " + new Date)
      if (pingsReceived >= pingsToPong) {
        sender ! "pong"
        context become ponging(0)
      } else {
        context become ponging(pingsReceived)
      }
    case ("GO!", partner:ActorRef) =>
      context become pinging(0, 0)
      self ! ("start", partner)
    case "start" =>
      context become pinging(0, 0)
      self ! ("start", sender())
  }
}
