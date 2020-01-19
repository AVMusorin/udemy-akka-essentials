package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}

object StartingStoppingActors extends App {
  val system = ActorSystem("StartingStoppingActors")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info("Starting child")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child $name")
        val childOpt = children.get(name)
        childOpt.foreach(context.stop)
      case Stop =>
        log.info("Stop myself")
        context.stop(self)
      case message =>
        log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  import Parent._

//  val parent = system.actorOf(Props[Parent], "parent")
//  parent ! StartChild("child1")
//
//  val child = system.actorSelection("/user/parent/child1")
//  child ! "Hi!"
//
//  parent ! StopChild("child1")

  // stopping is async
//  for (i <- 1 to 50) child ! s"Check $i"

//  parent ! StartChild("child2")
//  val child2 = system.actorSelection("/user/parent/child2")
//
//  parent ! Stop
//
//  for (i <- 1 to 50) child2 ! s"[$i] check"

  /**
   *
   * Second method to stop actors
   */

//  val loseActor = system.actorOf(Props[Child])
//  loseActor ! "Hello!"
//  loseActor ! PoisonPill
//  loseActor ! "Hey!"

  /**
   * Killing actor
   */

//  val loseActor2 = system.actorOf(Props[Child])
//  loseActor2 ! "Hello!"
//  loseActor2 ! Kill
//  loseActor2 ! "Hey!"

  class Watcher extends Actor with ActorLogging {
    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started $name")
        context.watch(child)
      case Terminated(actorRef) =>
        log.info(s"Child was terminated ${actorRef.path}")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")

  val childW = system.actorSelection("/user/watcher/watchedChild")
  childW ! PoisonPill
}
