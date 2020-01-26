package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

object Dispatchers extends App {
  class Counter extends Actor with ActorLogging {
    var count = 0
    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")
    }
  }

//   ConfigFactory.load().getConfig("dispatchersDemo")
//  method 1
  val system = ActorSystem("DispatcherDemo")
//  val simpleActors = for (i <- 1 to 10)
//    yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//
//  val r = new Random()
//
//  for (i <- 1 to 1000) {
//    simpleActors(r.nextInt(10)) ! i
//  }

  class DBActor extends Actor with ActorLogging {
//    implicit val executionContext: ExecutionContextExecutor = context.dispatcher
    // solution #1
    implicit val executionContext: ExecutionContextExecutor = context.system.dispatchers.lookup("my-dispatcher")
    // solution #2

    override def receive: Receive = {
      case message => Future {
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }

  val dBActor = system.actorOf(Props[DBActor], "db_actor")
  val nonBlockingActor = system.actorOf(Props[Counter], "nonblocking_actor")

  for (i <- 1 to 1000) {
    val message = s"[$i] Important message"
    dBActor ! message
    nonBlockingActor ! message
  }
}
