package part1recap

object MultiThreadingRecap extends App {
  // creating a Thread

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Running in parallel a")
  })

  val bThread = new Thread(() => println("Running in parallel b"))


  //  aThread.start()
  //  bThread.start()

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withDraw(money: Int) = amount -= money

    def safeWithDraw(money: Int) = this.synchronized {
      amount -= money
    }
  }

  val acc = new BankAccount(10000)

  val baThread = new Thread(() => acc.withDraw(100))

  val bbThread = new Thread(() => acc.withDraw(500))

//  baThread.start()
//  bbThread.start()
//
//  println(acc)

  // inter thread communication on the JVM
  // wait - notify mechanism

  // scala Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import scala.util.Success
  import scala.util.Failure

  val future = Future {
    42
  }

  // callbacks
//  future.onComplete {
//    case Success(v) => println(v)
//    case Failure(_) => println("Exception")
//  }
//
//  val aProcessFuture = future.map(_ + 1)
//
//  val aFlatMapFuture = future.flatMap { v =>
//    Future {
//      v + 2
//    }
//  }

  // 1M numbers in between 10 threads
  val features = (0 to 9).map { i =>
    100000 * i until 100000 * (i + 1)
  }.map { r =>
    Future {
      if (r.contains(2342342)) {
        throw new RuntimeException("invalid")
      }
      r.sum
    }
  }

  val sumFuture = Future.reduceLeft(features)(_ + _)
  sumFuture.onComplete(println)
}
