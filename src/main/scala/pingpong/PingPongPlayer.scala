package pingpong

import java.util.Date

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive
import scala.concurrent.duration._

/**
 * Created by eny on 23.11.14.
 */

object PingPongPlayer {
  def props(
    delayBetweenPings:Duration,
    delayBetweenBreaks:Duration,
    pingsToPong:Int,
    pongsToSwitch:Int,
    setsCount:Int) = Props(classOf[PingPongPlayer],
      delayBetweenPings, delayBetweenBreaks, pingsToPong, pongsToSwitch
  )
}

class PingPongPlayer(
  val delayBetweenPings:Duration,
  val delayBetweenBreaks:Duration,
  val pingsToPong:Int,
  val pongsToSwitch:Int,
  val setsCount:Int) extends Actor {

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
