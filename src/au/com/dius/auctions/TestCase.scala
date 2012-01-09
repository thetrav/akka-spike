package au.com.dius.auctions
import akka.actor.Actor

object TestCase {
  def main(args: Array[String]) {
	  val auctionHouse = Actor.actorOf[AuctionHouse].start()
	  val vendor = Actor.actorOf(new Vendor(auctionHouse)).start()
	  vendor ! Create(10, "penny farthing bicycle")
	  val buyer = Actor.actorOf(new Buyer("trav", auctionHouse)).start()
	  buyer ! 100
	  (1 to 5).foreach(arg => {
		  Thread.sleep(5)
		  //Yielder.hangout()		  
	  })
	  vendor ! Close
  }
}