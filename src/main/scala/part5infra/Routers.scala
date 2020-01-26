package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, FromConfig, RoundRobinPool, RoundRobinRoutingLogic, Router}

import scala.collection.immutable.IndexedSeq

object Routers extends App {

  /**
   * Manual router
   */
  class Master extends Actor {
    private val slaves: IndexedSeq[ActorRefRoutee] = for (_ <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave])
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      case Terminated(ref) =>
        router = router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router = router.addRoutee(newSlave)
      case message =>
        router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo")
  val master = system.actorOf(Props[Master])

//  for (i <- 1 to 10) {
//    master ! s"[$i] message"
//  }

  /**
   * Method 2
   */
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")

//    for (i <- 1 to 10) {
//      poolMaster ! s"[$i] message"
//    }

  /**
   * Method 3
   * config
   */

  val poolMaster2 = system.actorOf(FromConfig.props(Props[Master]), "poolMaster2")

  for (i <- 1 to 10) {
    poolMaster2 ! s"[$i] message"
  }
}
