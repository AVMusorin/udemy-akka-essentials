package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello, there!"
      case message: String => println(s"[${context.self.path}] I've received message $message")
      case number: Int => println(s"[Simple Actor] I've received number: '$number'")
      case SpecialMessage(message) => println(s"[Simple Actor] I've received special message '$message'")
      case SendMessageToMyself(message) => self ! message
      case SendHiTo(ref) => ref ! "Hi!" // alice is being passed as a sender as implicit
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // keep the original sender
    }
  }

  val system = ActorSystem("ActorSystemCapabilities")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor!"

  // messages can be of any type
  // IMPORTANT!
  // 1. messages must be immutable
  // 2. messages must be serializable (it is Java interface)

  // in practice use case classes and case objects
  simpleActor ! 5

  case class SpecialMessage(message: String)

  simpleActor ! SpecialMessage("special message")

  // actors have information about their context and about themselves
  // context.self === 'this' in OOP
  case class SendMessageToMyself(message: String)

  simpleActor ! SendMessageToMyself("I'm actor")

  val alice: ActorRef = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SendHiTo(ref: ActorRef)
  alice ! SendHiTo(bob)

  // dead letters
  alice ! "Hi!"

  // forwarding messages
  // D -> A -> B
  // forwarding = sending a message with the original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi!", bob)


  /**
   * Exercises
   *
   * 1. Counter
   *  - Increment
   *  - Decrement
   *  - Print
   *
   *  2. a Bank account as Actor
   *  receives
   *   - Deposit an amount
   *   - Withdraw an amount
   *   - Statement
   *   replies with
   *   - Success
   *   - Failure
   */

  class CounterActor extends Actor {
    import CounterActor._
    var amount = 0
    override def receive: Receive = {
      case Increment => amount += 1
      case Decrement => amount -= 1
      case Print => println(s"[$self] amount is $amount")
    }
  }

  object CounterActor {
    sealed trait CounterAction

    case object Increment extends CounterAction
    case object Decrement extends CounterAction
    case object Print extends CounterAction
  }

  val counter = system.actorOf(Props[CounterActor], "Counter")

  counter ! CounterActor.Increment
  counter ! CounterActor.Decrement
  counter ! CounterActor.Increment
  counter ! CounterActor.Print
}
