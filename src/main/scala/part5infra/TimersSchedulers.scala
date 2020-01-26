package part5infra

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}
import java.time._

//import part5infra.TimersSchedulers.SelfClosingActor.Heartbeat

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  //  val system = ActorSystem("TimersSchedulerDemo")
  //  val simpleActor = system.actorOf(Props[SimpleActor])
  //  system.log.info("Scheduling")
  //
  //  import system.dispatcher
  //  system.scheduler.scheduleOnce(1 second){
  //    simpleActor ! "reminder"
  //  }
  //
  //  val routine: Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
  //    simpleActor ! "heartbeat"
  //  }
  //
  //  system.scheduler.scheduleOnce(5 seconds) {
  //    routine.cancel()
  //  }

  /**
   * Exercise: implement a self-closing actor
   *
   * - if the actor receives a message (anything), you have 1 second to send it another message
   * - if the time window expires, the actor will stop itself
   * - if you send another message, the time window is reset
   */

  /*
  Not good realization
   */

  //  object SelfClosingActor {
  //    case object Heartbeat
  //  }
  //
  //  class SelfClosingActor extends Actor with ActorLogging {
  //    override def receive: Receive = processMessages(Some(LocalDateTime.now()))
  //
  //    def processMessages(lastMessageTime: Option[LocalDateTime]): Receive = {
  //      case Heartbeat => lastMessageTime.foreach { time =>
  //        val now: LocalDateTime = LocalDateTime.now()
  //        val diff = java.time.Duration.between(time, now).toMillis
  //        log.info(s"[Heartbeat] diff $diff")
  //        if (diff > 1000) {
  //          log.info("Stopping actor")
  //          context.stop(self)
  //        } else {
  //          context.become(processMessages(Some(now)))
  //        }
  //      }
  //      case message => log.info(message.toString)
  //    }
  //  }
  //
  //  import system.dispatcher
  //
  //  val system = ActorSystem("TimersSchedulerDemo")
  //  val selfClosingActor = system.actorOf(Props[SelfClosingActor])
  //
  //  selfClosingActor ! "Hi!"
  //
  //  system.scheduler.schedule(0 seconds, 1 second) {
  //    selfClosingActor ! Heartbeat
  //  }


  /**
   * Another realization
   */
  val system = ActorSystem("SelfClosing")
  import system.dispatcher

  class SelfClosingActor extends Actor with ActorLogging {
    var schedule = createTimeoutWindow()
    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Closing...")
        context.stop(self)
      case message =>
        log.info(s"Message: ${message.toString}")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }
  }

//  val closingActor = system.actorOf(Props[SelfClosingActor])
//  closingActor ! "Hello"
//  Thread.sleep(2000)
//  closingActor ! "check"

  /**
   * Timer
   */

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 5 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrap")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I'm alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }
  val timerBasedHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor])
  system.scheduler.scheduleOnce(5 second) {
    timerBasedHeartbeatActor ! Stop
  }
}