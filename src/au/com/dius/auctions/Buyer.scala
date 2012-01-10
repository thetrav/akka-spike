package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef
import javax.swing.JFrame
import Ui.menuItem
import Ui.repaint
import Ui.action
import Ui.newFrame
import javax.swing.JMenuBar
import javax.swing.WindowConstants
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JButton
import scala.collection.immutable.MapLike

case class BidOn(amount: Int, auction: String)

class Buyer(key: String, name: String, auctionHouse: ActorRef) extends Actor {
  var myAuctions: Map[String, ActorAddress] = Map()

  def receive = {
    case "getName" => self.reply(name)
    case "getAuctions" => {
      auctionHouse ! ListAuctions
    }
    case "RandomBid" => {
      if(!myAuctions.isEmpty) {
	      val ind = math.random * Integer.MAX_VALUE % myAuctions.size
	      val key = myAuctions.keys.toList(ind.toInt)
	      val bid = math.random * Integer.MAX_VALUE % 1000000
	      self ! BidOn(bid.toInt, key)
      }
    }
    case auctions: List[ActorAddress] => {
      myAuctions = auctions.map((i:ActorAddress) => (i.key, i)).toMap
    }
    case BidOn(amount: Int, auction: String) => {
      myAuctions(auction).actorRef ! Bid(amount, name, self)
    }
  }
}

class BuyerUi(buyer: ActorRef) {
  def init() {
    val name: String = (buyer !! "getName").toString()
    val frame = newFrame(name)
    val menu = new JMenuBar()
    frame.setJMenuBar(menu)

    val pane = new JPanel()
    frame.getContentPane().add(pane)

    val auctions = new JComboBox()
    pane.add(auctions)

    val updateAuctions = () => {
      val future = buyer ? "getAuctions"
      future.onResult {
        case data: Iterable[String] => {
          auctions.removeAllItems()
          data.foreach { item => auctions.addItem(item) }
          repaint(frame)
        }
      }
    }

    updateAuctions()

    menuItem(menu, "updateAuctions", e => { updateAuctions() })

    val amount = new JTextField("100")
    pane.add(amount)

    val bid = new JButton("bid")
    pane.add(bid)

    action(bid, e => {
      val item = auctions.getSelectedItem().asInstanceOf[String]
      buyer !! BidOn(Integer.parseInt(amount.getText()), item)
    })

    frame.setVisible(true)
  }
}