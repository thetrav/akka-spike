package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class AuctionHouse extends Actor {
  var auctions = Map[String, ActorRef]()
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
      println("sold "+auctions(key)+" for " + amount + " to " + buyer)
    }
    case ListAuctions => {
      self.reply(auctions.values)
    }
  }
  
}