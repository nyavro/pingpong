package pingpong

import akka.actor.{Actor, ActorRef, Props}
import akka.persistence.PersistentActor

import scala.concurrent.duration.Duration

/**
 * Created by eny on 24.11.2014.
 */
object PingPongPersistent {
  def props(
    delayBetweenPings:Duration,
    delayBetweenBreaks:Duration,
    pingsToPong:Int,
    pongsToSwitch:Int,
    setsCount:Int,
    id:String) = Props(
      classOf[PingPongPersistent],
      delayBetweenPings, delayBetweenBreaks, pingsToPong, pongsToSwitch, setsCount, id
    )
}

case class State(pings:Int, pongs:Int, rounds:Int)

class PingPongPersistent(
  val delayBetweenPings:Duration,
  val delayBetweenBreaks:Duration,
  val pingsToPong:Int,
  val pongsToSwitch:Int,
  val setsCount:Int,
  val id:String) extends PersistentActor {
  override def persistenceId = id

  var pingsCount = 0
  var pongsCount = 0
  var roundsCount = 0

  def receiveCommand: Receive = {
    case "failure"  => throw new Exception("failure")
    case msg:Any =>
      persist(msg) {
        case "ping" => pingsCount += 1
          if(pingsCount>=pingsToPong) {
            pingsCount = 0
            sender ! "pong"
          }
        case "pong" =>
          pongsCount += 1
          if(pongsCount>=pongsToSwitch) {
            pongsCount = 0
            sender ! ("GO!", self)
          }
          else self !("start", sender())
        case ("GO!", partner:ActorRef) =>
          roundsCount += 1
          if (roundsCount > setsCount) context.stop(self)
          else self !("start", partner)
        case ("start", partner:ActorRef) =>
          for(i<-1 to pingsToPong) {
            try {
              Thread.sleep(delayBetweenPings.toMillis)
              partner ! "ping"
            } catch {
              case e:InterruptedException => Thread.interrupted
            }
          }
      }
  }

  def receiveRecover: Actor.Receive = {
    case "ping" => pingsCount += 1
  }
}