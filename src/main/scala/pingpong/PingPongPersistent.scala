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

case class State(setsLeft:Int, supervisor:ActorRef)

sealed trait Command

case class Ping(state:State) extends Command
case class Pong(state:State) extends Command
case class Go(partner:ActorRef, state:State) extends Command
case class Start(pingsLeft:Int, partner:ActorRef, state:State) extends Command

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
        case Ping(setsLeft:State) =>
          ponging = true
          pingsCount += 1
          println(s"${self.path.name}: Ping($pingsCount)")
          if(pingsCount>=pingsToPong) {
            pingsCount = 0
            ponging = false
            sender ! Pong(setsLeft)
          }
        case Pong(state:State) =>
          pongsCount += 1
          println(s"${self.path.name}: Pong($pongsCount)")
          if(pongsCount>=pongsToSwitch) {
            pongsCount = 0
            sender ! Go(self, State(state.setsLeft-1, state.supervisor))
          }
          else self ! Start(pingsToPong, sender(), state)
        case Go(partner:ActorRef, state:State) =>
          if(state.setsLeft>0) {
            println(s"${self.path.name}: It is mine turn to ping!")
            self ! Start(pingsToPong, partner, state)
          } else {
            println(s"PingPong finished")
            state.supervisor ! "Finished"
          }
        case Start(pingsLeft:Int, partner:ActorRef, setsLeft:State) =>
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
    case Ping(s:State) => pingsCount = (pingsCount + 1) % pingsToPong
    case Pong(s:State) => pongsCount = (pongsCount + 1) % pongsToSwitch
  }
}