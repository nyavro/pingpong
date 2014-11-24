package pingpong

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration._

/**
 * Created by eny on 24.11.14.
 */

class Breaking(delayBetweenBreaks:Duration, playerA:ActorRef, playerB:ActorRef) extends Actor {
  override def receive: Receive = {
    case "start" =>
      try {
        Thread.sleep(delayBetweenBreaks.toMillis)
      } catch {
        case e:InterruptedException => Thread.interrupted
      }
      playerA ! "failure"
      playerB ! "failure"
      self ! "start"
    case "stop" =>
      println("stopping Breaking actor")
      context.stop(self)
  }
}

object PingPongApp extends App {
  val SetsCount = 2
  val system = ActorSystem("pingpong")
  val DelayBetweenBreaks = 300.millis
  val DelayBetweenPings = 10.millis
  val PingsToPong = 3
  val PongsToSwitch = 2
  val playerA = system.actorOf(
    PingPongPersistent.props(
      DelayBetweenPings,
      PingsToPong,
      PongsToSwitch,
      id = System.currentTimeMillis.toString
    ),
    "playerA"
  )
  val playerB = system.actorOf(
    PingPongPersistent.props(
      DelayBetweenPings,
      PingsToPong,
      PongsToSwitch,
      id = (System.currentTimeMillis + 1).toString
    ),
    "playerB"
  )
  val break = system.actorOf(Props(classOf[Breaking], DelayBetweenBreaks, playerA, playerB))
  playerA ! Go(playerB, SetsCount)
  break ! "start"
  Thread.sleep(10000)
  break ! "stop"
  system.shutdown()
  println("finished")
}
