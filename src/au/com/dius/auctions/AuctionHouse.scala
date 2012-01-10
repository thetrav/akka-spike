package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class AuctionHouse extends Actor {
  var auctions = Map[String, ActorRef]()
  var oldAuctions = List[(String, Int, Option[String])]()
  var sequence = 1
  
  def nextId = {
    sequence += 1
    sequence
  }
  
  def receive = {
    case RegisterVendor(name:String) => {
      val key = nextId + " - " + name
      val vendor = Actor.actorOf(new Vendor(key, name, self)).start()
      self.reply((key, vendor))
    }
    case RegisterBuyer(name:String) => {
      val key = nextId + " - " + name
      val buyer = Actor.actorOf(new Buyer(key, name, self)).start()
      self.reply((key, buyer))
    }
    case RegisterAuction(minimum, description) => {
      val key = nextId +" - "+ description
      val auction = Actor.actorOf(
          new Auction(key, minimum, description, self))
          .start()
      auctions += key -> auction
      auction ! Open
      self.reply((key, auction))
    }
    case Sold(key, amount, buyer) => {
      val auction = auctions(key)
      oldAuctions = (key, amount, buyer) :: oldAuctions
      auctions -= key
    }
    case ListAuctions => {
      self.reply(auctions)
    }
    case ListOldAuctions => {
      self.reply(oldAuctions)
    }
  }
}