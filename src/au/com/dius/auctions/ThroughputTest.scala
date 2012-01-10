package au.com.dius.auctions
import javax.swing.JFrame
import javax.swing.JTextArea
import javax.swing.JScrollPane
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Scheduler
import java.util.concurrent.TimeUnit

object ThroughputTest {

  def main(args: Array[String]) {
    val frame = new JFrame("test")
    frame.setSize(800, 600)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val text = new JTextArea()
    frame.getContentPane().add(new JScrollPane(text))

    val vendorDriver = Actor.actorOf[VendorDriver].start()

    (1 to 5).foreach(i => {
      vendorDriver ! AddTestVendor("Vendor" + i)
    })

    val buyerDriver = Actor.actorOf[BuyerDriver].start()

    val firstNames = List("Travis", "John", "Alan", "Tony", "Martin", "Simon", "Luke", "John", "Tim", "Michael")
    val lastNames = List("Dixon", "Fowler", "Jones", "Alberts", "Kroft", "Peyton Jones", "Moris", "Eisenhower", "Kennedy", "Obama")
    (0 to 9).foreach(i => {
      (0 to 9).foreach(j => {
        buyerDriver ! AddTestBuyer(firstNames(i) + " " + lastNames(j))
      })
    })
    val refresher = Actor.actorOf(new ScreenRefresher(frame, text)).start()
    val refreshAgain: Runnable = new Runnable() {
      def run() {
        refresher ! "REFRESH"
      }
    }
    Scheduler.schedule(refreshAgain, 1L, 200L,TimeUnit.MILLISECONDS)

    frame.setVisible(true)
  }
}

class ScreenRefresher(frame: JFrame, text: JTextArea) extends Actor {
  val auctionHouse = Actor.remote.actorFor("auctionHouse", "localhost", 2555)

  def receive = {
    case _ => {
      println("refreshing")
      val f1 = auctionHouse ? ListAuctions
      f1.onResult {
        case l: List[ActorAddress] => {
          val s = new StringBuffer()
          l.foreach(address => {
            s.append((address.actorRef !! Status).toString() + "\n")
          })
          text.setText(s.toString())
          frame.invalidate()
          frame.validate()
          frame.repaint()
        }
      }
    }
  }
}

case class AddTestBuyer(name: String)
case class BidOnSomething(buyer: ActorRef)

class BuyerDriver extends Actor {
  val creationServers = (Actor.remote.actorFor("creation", "localhost", 2666), Actor.remote.actorFor("creation", "localhost", 2666))
  def receive = {
    case AddTestBuyer(name) => {
      val f1 = creationServers._1 ? RegisterBuyer(name)
      f1.onResult {
        case a: ActorAddress => {
          scheduleBid(a.actorRef)
        }
      }
    }
    case BidOnSomething(buyer: ActorRef) => {
      buyer ! "RandomBid"
      scheduleBid(buyer)
    }
  }

  def scheduleBid(buyer: ActorRef) {
    buyer ! "getAuctions"
    val bidAgain: Runnable = new Runnable() {
      def run() {
        self ! BidOnSomething(buyer)
      }
    }
    val time = (math.random * Integer.MAX_VALUE % 1000).toLong
    Scheduler.scheduleOnce(bidAgain, time, TimeUnit.MILLISECONDS)
  }
}

case class AddTestVendor(name: String)
case class OpenAuction(vendor: ActorRef)
case class CloseAuction(vendor: ActorRef)

class VendorDriver extends Actor {
  val creationServers = (Actor.remote.actorFor("creation", "localhost", 2666), Actor.remote.actorFor("creation", "localhost", 2666))
  val names = List("toys", "lego", "train", "truck", "glue", "paint", "paper", "scissors", "tennis ball", "pogo stick")
  def receive = {
    case AddTestVendor(name) => {
      val f1 = creationServers._1 ? RegisterVendor(name)
      f1.onResult {
        case a: ActorAddress => {
          val vendor = a.actorRef
          self ! OpenAuction(vendor)
        }
      }
    }
    case OpenAuction(vendor) => {
      val registerFuture = vendor ? RegisterAuction((math.random * 100).toInt, names((math.random * 10).toInt))
      val closeAuction: Runnable = new Runnable() {
        def run() {
          self ! CloseAuction(vendor)
        }
      }
      Scheduler.scheduleOnce(closeAuction, 3L, TimeUnit.SECONDS)
    }
    case CloseAuction(vendor) => {
      vendor ! "CloseAll"
      self ! OpenAuction(vendor)
    }
  }
}
