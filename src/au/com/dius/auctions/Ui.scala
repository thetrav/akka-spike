package au.com.dius.auctions
import javax.swing.JFrame
import javax.swing.JMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.WindowConstants
import akka.actor.Actor
import javax.swing.JTextArea
import javax.swing.JScrollPane
import javax.swing.AbstractButton
import java.awt.Container
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JTextField
import akka.actor.ActorRef

object Ui {
    val auctionHouse = Actor.actorOf[AuctionHouse].start()

    def repaint(frame:JFrame) {
      frame.invalidate()
      frame.validate()
      frame.repaint()
    }
    
    def action(b:AbstractButton, a:ActionEvent => Any) {
      b.addActionListener(new ActionListener{ 
        def actionPerformed(e:ActionEvent){
          a(e)
        }
      })
    }
    
    def menuItem(c:Container, name:String, a:ActionEvent => Any) = {
      val item = new JMenuItem(name)
      c.add(item)
      action(item, a)
    }
  
	def main(args:Array[String]) {
	  val frame = new JFrame("auction")
	  frame.setSize(500,100)
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	  
	  val text = new JTextArea()
	  frame.getContentPane().add(new JScrollPane(text))
	  
	  val menu = new JMenuBar()
	  frame.setJMenuBar(menu)
	  
	  menuItem(menu, "Refresh", e => {
	    val future = auctionHouse ? ListAuctions
	    future.onResult {
	      case m:Map[String, ActorRef] => {
	        var s = ""
	        m.keys.foreach( key => {
	          s += "\t" + key + "\n"
	        })
	        text.setText(s)
	        repaint(frame)
	      } 
	    }
	  })
	  menuItem(menu, "Vendor", e => createVendor)
	  menuItem(menu, "Buyer", e => createBuyer)
	  //made a change
	  frame.setVisible(true)
	}
	
	
	def newFrame(name:String) = {
	  val frame = new JFrame(name)
	  frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
	  frame.setSize(400,300)
	  frame
	}
	
	def createBuyer() {
	  val name = JOptionPane.showInputDialog("Enter Buyer Name")
	  val buyer = Actor.actorOf(new Buyer(name, auctionHouse)).start()
	  
	  new BuyerUi(buyer).init()
	}
	
	def createVendor() {
	  val vendor = Actor.actorOf(new Vendor(auctionHouse)).start()

	  new VendorUi(vendor).init()
	}
}