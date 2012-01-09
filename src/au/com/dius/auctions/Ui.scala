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

object Ui {
	val auctionHouse = new AuctionHouse()
    val auctionHouseActor = Actor.actorOf(auctionHouse).start()
  
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
	  frame.setSize(800,600)
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	  
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
	  val menu = new JMenu("create")
	  menuBar.add(menu)
	  menuItem(menu, "AuctionHouse", e => createAuctionHouse)
	  menuItem(menu, "Vendor", e => createVendor)
	  
	  frame.setVisible(true)
	}
	
	def newFrame(name:String) = {
	  val frame = new JFrame(name)
	  frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
	  frame.setSize(400,300)
	  frame
	}
	
	def createVendor() {
//	  val name = JOptionPanel.showInputDialog("enter name")
	  val frame = newFrame("Vendor")
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
//	  JLabel name = 
	  
	}
	
	def createAuctionHouse() {
	  val frame = newFrame("AuctionHouse")
	  
	  
	  
	  val text = new JTextArea()
	  
	  frame.getContentPane().add(new JScrollPane(text))
	  
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
	  menuItem(menuBar, "refresh", e => {
	    var s = ""
	    auctionHouse.auctions.foreach( t => s += t._1 + "\t" + t._2+" \n")
	    text.setText(s)
	    frame.invalidate()
	    frame.repaint()
	  })
	  
	  frame.setVisible(true)
	}
}