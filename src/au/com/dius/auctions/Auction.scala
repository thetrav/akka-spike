package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class Auction(id:String,
    minimum:Int, 
    description:String, 
    auctionHouse:ActorRef) extends Actor {
  //first bid can be minimum
  var currentAmount:Int = minimum-1 
  var currentBuyer:Option[ActorRef] = None
  var currentName:Option[String] = None
  
  def receive = {
    case Open => None
    case Bid(newAmount:Int, newName:String, newBuyer:ActorRef) => {
      if(newAmount > currentAmount) {
        currentAmount = newAmount
        currentBuyer = Some(newBuyer)
        currentName = Some(newName)
      }
    }
    case Close => {
      auctionHouse ! Sold(id, currentAmount, currentName)
      self.stop()
    }
  }
}