package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef

class AuctionHouse extends Actor {
  var auctions = Map[String, ActorAddress]()
  var oldAuctions = List[(String, Int, Option[String])]()
  var sequence = 1

  def nextId = {
    sequence += 1
    sequence
  }

  def receive = {
    case a:ActorAddress => {
      println("new lot accepted:"+a.key)
      auctions += a.key -> a
      a.actorRef ! Open
    }
    case Sold(key, amount, buyer) => {
      println("lot: "+key + " sold to "+buyer+" for "+amount)
      oldAuctions = (key, amount, buyer) :: oldAuctions
      auctions -= key
    }
    case ListAuctions => {
      self.reply(auctions.values.toList)
    }
  }
  
  override def preStart() = {
    Actor.remote.register("auctionHouse", self)
  }
}

object AuctionHouse {
  def main(args: Array[String]) {
    println("starting auction house server")
    Actor.remote.start(args(0), Integer.parseInt(args(1)))
    Actor.actorOf[AuctionHouse].start()
  }
}