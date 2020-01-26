package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {
  val system = ActorSystem("MailboxesDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * Custom priority queue
   */

  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[P0]") => 0
        case message: String if message.startsWith("[P1]") => 1
        case message: String if message.startsWith("[P2]") => 2
        case message: String if message.startsWith("[P3]") => 3
        case _ => 4
    })

  val supportTickerActor = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
//  supportTickerActor ! PoisonPill
//  supportTickerActor ! "[P3] Hi"
//  supportTickerActor ! "[P2] Hi"
//  supportTickerActor ! "[P1] Hi"
//  supportTickerActor ! "[P0] Hi"

  /**
   * control-aware mailbox
   * we'll use UnboundedControlAwareMailbox
   */

  // step 1 mark import messages as control messages
  case object ManagementTicket extends ControlMessage

  /**
   * step 2 - configure who gets the mailbox
   * - make the actor attach to the mailbox
   */
  val controlAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
  controlAwareActor ! "[P3] Hi"
  controlAwareActor ! "[P2] Hi"
  controlAwareActor ! ManagementTicket
}
