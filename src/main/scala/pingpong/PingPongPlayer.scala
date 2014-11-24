package pingpong

import java.util.Date

import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.duration._

/**
 * Created by eny on 23.11.14.
 */

sealed trait Event
class Round extends Event
class Ping extends Event
class Pong extends Event

object PingPongPlayer {
  def props(
    delayBetweenPings:Duration,
    delayBetweenBreaks:Duration,
    pingsToPong:Int,
    pongsToSwitch:Int,
    setsCount:Int) = Props(
      classOf[PingPongPlayer],
      delayBetweenPings, delayBetweenBreaks, pingsToPong, pongsToSwitch, setsCount
    )
}

class PingPongPlayer(
  val delayBetweenPings:Duration,
  val delayBetweenBreaks:Duration,
  val pingsToPong:Int,
  val pongsToSwitch:Int,
  val setsCount:Int) extends Actor {

  override def receive: Receive = ponging(0, 0)

  def pinging(round:Int, count:Int): Receive = {
    case ("start", partner:ActorRef) =>
      for(i<-1 to pingsToPong) {
        try {
          Thread.sleep(delayBetweenPings.toMillis)
          partner ! "ping"
        } catch {
          case e:InterruptedException => Thread.interrupted
        }
      }
    case "pong" =>
      val pongsReceived = count + 1
      if(pongsReceived >= pongsToSwitch) {
        context become ponging(round, 0)
        sender ! ("GO!", self)
      } else {
        context become pinging(round, pongsReceived)
        self !("start", sender())
      }
  }
  
  def ponging(round:Int, count:Int): Receive = {
    case "start" =>
      context become pinging(round, 0)
      self ! ("start", sender())
    case "ping" =>
      val pingsReceived = count + 1
      println(s"ping $pingsReceived received at: " + new Date)
      if (pingsReceived >= pingsToPong) {
        sender ! "pong"
        context become ponging(round, 0)
      } else {
        context become ponging(round, pingsReceived)
      }
    case ("GO!", partner:ActorRef) =>
      val roundsCount = round + 1
      if(roundsCount>setsCount) context.stop(self)
      else {
        context become pinging(roundsCount, 0)
        self ! ("start", partner)
      }
    case "failure" =>
      throw new RuntimeException("Failure during message receiving occured")
  }
}
