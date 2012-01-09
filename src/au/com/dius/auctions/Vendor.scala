package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class Vendor(auctionHouse:ActorRef) extends Actor {
  var auction:Option[ActorRef] = None
  
  def receive = {
    case c:Create => auctionHouse ! c
    case a:ActorRef => auction = Some(a)
    case Close => if(auction.isEmpty) {
      println("haven't got an auction yet")
      self ! Close
    } else {
      println("closing")
      auction.get ! Close
    }
  }
}