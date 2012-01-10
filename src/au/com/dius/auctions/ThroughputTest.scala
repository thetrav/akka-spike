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
    
    vendorDriver ! AddTestVendor("First")
    vendorDriver ! AddTestVendor("Second")
    vendorDriver ! AddTestVendor("Third")
    
    val buyerDriver = Actor.actorOf[BuyerDriver].start()
    
    buyerDriver ! AddTestBuyer("Harry")
    buyerDriver ! AddTestBuyer("Larry")
    buyerDriver ! AddTestBuyer("Garry")
    
    frame.setVisible(true)
  }
}

case class AddTestBuyer(name: String)
case class BidOnSomething(buyer:ActorRef)

class BuyerDriver extends Actor {
  val creationServers = (Actor.remote.actorFor("creation", "localhost", 2666), Actor.remote.actorFor("creation", "localhost", 2666))
  def receive = {
    case AddTestBuyer(name) => {
      val f1 = creationServers._1 ? RegisterBuyer(name)
      f1.onResult {
        case a:ActorAddress => {
          println("buyer registered:"+a.key)
          scheduleBid(a.actorRef)
        }
      }
    }
    case BidOnSomething(buyer:ActorRef) => {
      buyer ! "RandomBid"
      scheduleBid(buyer)
    }
  }
  
  def scheduleBid(buyer:ActorRef) {
    buyer ! "getAuctions"
    val bidAgain: Runnable = new Runnable() {
    def run() {
        self ! BidOnSomething(buyer)
      }
    }
  	Scheduler.scheduleOnce(bidAgain, 1L, TimeUnit.SECONDS)
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
          println("vendor registered:" + a.key)
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
      println("closing all auctions")
      vendor ! "CloseAll"
      self ! OpenAuction(vendor)
    }
  }
}
