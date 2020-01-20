package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {

  object StartChild
  class LifecycleActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I'm starting")

    override def postStop(): Unit = log.info("I've stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }
  }

  val system = ActorSystem("Lifecycle")
  val parent = system.actorOf(Props[LifecycleActor], "parent")

  parent ! StartChild
  parent ! PoisonPill

  /**
   * Restart
   */

  object Fail
  object FailChild
  class Parent extends Actor with ActorLogging {
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
    }
  }

  class Child extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("Supervised child started")
    override def postRestart(reason: Throwable): Unit = log.info("Supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"Supervised actor restarting because of ${reason.getMessage}")
    }

    override def aroundPostRestart(reason: Throwable): Unit = {
      log.info("Supervised child restarted")
    }

    override def receive: Receive = {
      case Fail =>
        log.warning("I'm failing")
        throw new RuntimeException("I failed")
    }
  }

  val supervised = system.actorOf(Props[Parent], "supervisor")
  supervised ! FailChild
}
