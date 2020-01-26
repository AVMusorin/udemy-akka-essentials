package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {
  /**
   * Resource actor
   * - open => it can receive read/write requests to the resource
   * - otherwise it will postpone all read/write requests until the state is open
   *
   * Resource actor is closed
   * - Open => switch to the open state
   *
   * Resource actor is opened
   * - Read, Write are handled
   * - Close switch to the close state
   */

  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = "Hello"

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing message $message")
        stash()
    }

    def open: Receive = {
      case Read =>
        log.info(s"I've read $innerData")
      case Write(data) =>
        log.info(s"Writing $data")
        innerData = data
      case Close =>
        log.info("Closing resource")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing message $message")
        stash()
    }
  }

  val system = ActorSystem("StashDemo")

  val resourceActor = system.actorOf(Props[ResourceActor])

  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Open
  resourceActor ! Write("I love stash")
  resourceActor ! Close
  resourceActor ! Read
}
