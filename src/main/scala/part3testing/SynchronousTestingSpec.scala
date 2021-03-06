package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import scala.concurrent._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.Duration

class SynchronousTestingSpec extends WordSpecLike with BeforeAndAfterAll {
  implicit val system = ActorSystem("SyncTesting")

  override def afterAll(): Unit = {
    system.terminate()
  }

  import SynchronousTestingSpec._

  "A counter" should {
    "sync increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc

      assert(counter.underlyingActor.count == 1)
    }

    "sync increase its counter at the call of the receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      assert(counter.underlyingActor.count == 1)
    }

    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val prob = TestProbe()

      prob.send(counter, Read)
      prob.expectMsg(Duration.Zero, 0)
    }
  }
}

object SynchronousTestingSpec {
  case object Inc
  case object Read
  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }
  }
}
