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
    case Create(minimum, description) => {
      val key = nextId + description
      val auction = Actor.actorOf(
          new Auction(key, minimum, description, self))
          .start()
      auctions += key -> auction
      auction ! Open
      self.reply(auction)
    }
    case Sold(key, amount, buyer) => {
      val auction = auctions(key)
      oldAuctions = (key, amount, buyer) :: oldAuctions
      auctions -= key
    }
    case ListAuctions => {
      self.reply(auctions)
    }
  }
}