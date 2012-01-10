package au.com.dius.auctions
import akka.actor.Actor
import akka.actor.ActorRef
import Ui._
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JMenuBar
import javax.swing.JLabel
import javax.swing.JButton
import javax.swing.BoxLayout

class Vendor(id:String, name:String, auctionHouse:ActorRef) extends Actor {
  var auctions = Map[String, ActorRef]()
  
  def receive = {
    case "getName" => self.reply(name)
    case "getAuctions" => self.reply(auctions.keys)
    case c:RegisterAuction => auctionHouse ! c
    case a:ActorAddress => auctions += a.key -> a.actorRef
    case Close(key) => auctions(key) ! Close
    case "CloseAll" => auctions.values.foreach(a => a ! Close); auctions = Map()
  }
}

class VendorUi(vendor:ActorRef) {
  def init() {
    val name: String = (vendor !! "getName").toString()
    val frame = newFrame(name)
	val menuBar = new JMenuBar()
    frame.setJMenuBar(menuBar)
    val panel = new JPanel()
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
    frame.getContentPane().add(new JScrollPane(panel))
    
    val refresh = () => {
      val future = vendor ? "getAuctions"
      future.onResult {
        case auctions:Iterable[String] => {
          println("got keys:"+auctions)
          panel.removeAll()
          auctions.foreach( auction => {
            val row = new JPanel()
            row.add(new JLabel(auction))
            val b = new JButton("end")
            row.add(b)
            action(b, e => { vendor ? Close(auction) })
            panel.add(row)
          })
          repaint(frame)
        } 
      }
    }
    
    menuItem(menuBar, "new Auction", e => {
      val description = JOptionPane.showInputDialog("enter item description")
      val minimum = Integer.parseInt(JOptionPane.showInputDialog("enter minimum bid"))
      vendor ? RegisterAuction(minimum, description)
      refresh()
    })
    
    menuItem(menuBar, "refresh", e => { refresh() })
    
    frame.setVisible(true)
  }
}