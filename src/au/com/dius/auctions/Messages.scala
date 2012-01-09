package au.com.dius.auctions
import akka.actor.ActorRef

case class Bid(amount:Int, buyer:ActorRef)
case class Sold(key:String, amount:Int, buyer:Option[String])
case class Open
case class Close
case class Create(minimum:Int, description:String)
case class ListAuctions
