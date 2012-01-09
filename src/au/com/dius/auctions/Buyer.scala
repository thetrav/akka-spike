package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class Buyer(name:String, auctionHouse:ActorRef) extends Actor {
  var myAuctions:Collection[ActorRef] = List()
  
  def receive = {
    case auctions:Collection[ActorRef] => myAuctions = auctions
    case bidAmount:Int => {
      if(myAuctions.isEmpty) {
        auctionHouse ! ListAuctions
        self ! bidAmount
      } else {
        myAuctions.head ! Bid(bidAmount, self)
      }
    }
  }
}