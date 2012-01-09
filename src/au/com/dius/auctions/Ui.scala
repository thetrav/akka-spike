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

object Ui {
    val auctionHouse = Actor.actorOf(new AuctionHouse()).start()
  
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
	  
	  val menu = new JMenuBar()
	  frame.setJMenuBar(menu)
	  menuItem(menu, "AuctionHouse", e => showAuctionHouse)
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
	  
	  val frame = newFrame("Vendor")
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
	  
	  val pane = new JPanel()
	  frame.getContentPane().add(pane)
	  val auctionCombo = new JComboBox()
	  pane.add(auctionCombo)
	  
	  val amount = new JTextField("100")
	  pane.add(amount)
	  
	  val b = new JButton("Bid")
	  pane.add(b)
	  action(b, e=>{
	    buyer ! Integer.parseInt(amount.getText())
	  })
	  
	  menuItem(menuBar, "update", e => {
	    
	  })
	  frame.setVisible(true)
	}
	
	def createVendor() {
	  val vendor = Actor.actorOf(new Vendor(auctionHouse)).start()

	  val frame = newFrame("Vendor")
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
	  val panel = new JPanel()
	  frame.getContentPane().add(new JScrollPane(panel))
	  menuItem(menuBar, "new Sale", e => {
	    val description = JOptionPane.showInputDialog("enter item description")
	    val minimum = Integer.parseInt(JOptionPane.showInputDialog("enter minimum bid"))
	    vendor !! Create(minimum, description)
	    val row = new JPanel()
	    row.add(new JLabel(description))
	    val b = new JButton("end")
	    row.add(b)
	    panel.add(row)
	    frame.invalidate()
	    frame.validate()
	    frame.repaint()
	    action(b, e => { vendor !! Close })
	  })
	  
	  frame.setVisible(true)
	}
	
	def showAuctionHouse() {
	  val frame = newFrame("AuctionHouse")
	  val text = new JTextArea()
	  
	  frame.getContentPane().add(new JScrollPane(text))
	  
	  val menuBar = new JMenuBar()
	  frame.setJMenuBar(menuBar)
	  menuItem(menuBar, "refresh", e => {
	    val result = (auctionHouse !! ListAuctions).toString() 
	    text.setText(result)
	    frame.invalidate()
	    frame.repaint()
	  })
	  
	  frame.setVisible(true)
	}
}