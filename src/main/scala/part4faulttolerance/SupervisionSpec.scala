package part4faulttolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SupervisionSpec extends TestKit(ActorSystem("SupervisionSpec"))
 with WordSpecLike with ImplicitSender with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import SupervisionSpec._
}

object SupervisionSpec {
  class Supervisor extends Actor {
    override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }
    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  case object Report
  class FussyWordCounter extends Actor {
    var wordCount = 0
    override def receive: Receive = {
      case Report => sender() ! wordCount
      case "" => throw new NullPointerException
      case sentence: String =>
        if (sentence.length > 10) {
          throw new IllegalArgumentException
        } else if (sentence.head.isLower) {
          throw new RuntimeException
        } else {
          wordCount += sentence.split(" ").length
        }
      case _ => throw new Exception
    }
  }
}
