package pingpong

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor

import scala.concurrent.duration.Duration

/**
 * Created by eny on 24.11.2014.
 */
object PingPongPersistent {
  def props(
    delayBetweenPings:Duration,
    pingsToPong:Int,
    pongsToSwitch:Int,
    id:String) = Props(
      classOf[PingPongPersistent],
      delayBetweenPings, pingsToPong, pongsToSwitch, id
    )
}

sealed trait Command
case class Ping(setsLeft:Int) extends Command
case class Pong(setsLeft:Int) extends Command
case class Go(partner:ActorRef, setsLeft:Int) extends Command
case class Start(pingsLeft:Int, partner:ActorRef, setsLeft:Int) extends Command

class PingPongPersistent(
  val delayBetweenPings:Duration,
  val pingsToPong:Int,
  val pongsToSwitch:Int,
  val id:String) extends PersistentActor {
  override def persistenceId = id

  var pingsCount = 0
  var pongsCount = 0
  var ponging = false

  def receiveCommand: Receive = {
    case "failure"  =>
      if(ponging) {
        sender ! "failure emulated"
        throw new Exception("failure")
      }
    case msg:Any =>
      persist(msg) {
        case Ping(setsLeft:Int) =>
          ponging = true
          pingsCount += 1
          println(s"${self.path.name}: Ping($pingsCount)")
          if(pingsCount>=pingsToPong) {
            pingsCount = 0
            ponging = false
            sender ! Pong(setsLeft)
          }
        case Pong(setsLeft:Int) =>
          pongsCount += 1
          println(s"${self.path.name}: Pong($pongsCount)")
          if(pongsCount>=pongsToSwitch) {
            pongsCount = 0
            sender ! Go(self, setsLeft-1)
          }
          else self ! Start(pingsToPong, sender(), setsLeft)
        case Go(partner:ActorRef, setsLeft:Int) =>
          if(setsLeft>0) {
            println(s"${self.path.name}: It is mine turn to ping!")
            self ! Start(pingsToPong, partner, setsLeft)
          } else {
            println(s"PingPong finished")
            context.stop(self)
            context.stop(sender())
          }
        case Start(pingsLeft:Int, partner:ActorRef, setsLeft:Int) =>
          if(pingsLeft>0) {
            try {
              Thread.sleep(delayBetweenPings.toMillis)
              partner ! Ping(setsLeft)
              self ! Start(pingsLeft-1, partner, setsLeft)
            } catch {
              case e:InterruptedException => Thread.interrupted
            }
          }
      }
  }

  def receiveRecover: Receive = {
    case Ping(s:Int) => pingsCount = (pingsCount + 1) % pingsToPong
    case Pong(s:Int) => pongsCount = (pongsCount + 1) % pongsToSwitch
  }
}