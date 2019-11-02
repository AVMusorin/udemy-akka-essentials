package part2Actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

object ActorsIntro extends App {

  // part 1 ActorSystem
  val actorSystem = ActorSystem("firstActorSystem")

  // part 2 creating actors
  // word count Actor
  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0
    //behaviour
    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word Counter] Unknown ${msg.toString}")
    }
  }

  // part 3 instantiate our actor
  val wordCountActor = actorSystem.actorOf(Props[WordCountActor], "wordCounter")

  // part 4 communicate
  wordCountActor ! "Hello world!"

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi! My name is $name")
      case _ =>
    }
  }

  object Person {
    def props(name: String): Props = {
      Props(new Person(name))
    }
  }

  val person = actorSystem.actorOf(Props(new Person("Bob")))
  person ! "hi"

  // best practice use companion object
  val person2 = actorSystem.actorOf(Person.props("Bob2"))
  person2 ! "hi"
}
