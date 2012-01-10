package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

object CreationServer {
	def main(args:Array[String]) {
	  println("starting creation server")
	  Actor.remote.start(args(0), Integer.parseInt(args(1)))
	  Actor.actorOf(new CreationServer(args(0), Integer.parseInt(args(1)), args(2), Integer.parseInt(args(3)))).start()
	}
}

case class ActorAddress(key:String, host:String, port:Int) {
  def actorRef = Actor.remote.actorFor(key, host, port)
}

class CreationServer(host:String, port:Int, auctionHouseHost:String, auctionHousePort:Int) extends Actor {
  val auctionHouse = Actor.remote.actorFor("auctionHouse", auctionHouseHost, auctionHousePort)
  
  var sequence = 0
  def nextId():Int = {
    sequence += 1
    sequence
  } 
  
  def receive = {
    case RegisterVendor(name:String) => {
//      println("new vendor registered: "+name)
      val key = nextId() + " - " + name
      val vendorLocal = Actor.actorOf(new Vendor(key, name, self)).start()
      Actor.remote.register(key, vendorLocal)
      self.reply(ActorAddress(key, host, port))
    }
    case RegisterBuyer(name:String) => {
//      println("new buyer registered: "+name)
      val key = nextId() + " - " + name
      val buyer = Actor.actorOf(new Buyer(key, name, auctionHouse)).start()
      Actor.remote.register(key, buyer)
      self.reply(ActorAddress(key, host, port))
    }
    case RegisterAuction(minimum, description) => {
      val key = nextId() +" - "+ description
      val auction = Actor.actorOf(
          new Auction(key, minimum, description, auctionHouse))
          .start()
      Actor.remote.register(key, auction)
      auctionHouse ! ActorAddress(key, host, port)
      self.reply(ActorAddress(key, host, port))
    }
    case m => println("message received: "+m)
  }
  
  override def preStart() = {
    Actor.remote.register("creation", self)
  }
}