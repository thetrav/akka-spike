package au.com.dius.auctions
import akka.actor.ActorRef

case class Bid(amount:Int, name:String, buyer:ActorRef)
case class Sold(key:String, amount:Int, name:Option[String])
case class Open
case class Close(key:String)
case class RegisterAuction(minimum:Int, description:String)
case class RegisterVendor(name:String)
case class RegisterBuyer(name:String)
case class ListAuctions
case class ListOldAuctions
case class Status
