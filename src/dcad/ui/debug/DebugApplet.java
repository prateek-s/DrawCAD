package dcad.ui.debug;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.Graphics;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.event.ActionListener;
import java.awt.event.*;


public class DebugApplet extends JApplet implements ActionListener{
	
  JButton nextIteration;
  JButton previousIteration;
  JButton clear;
  
  double values[][][]; 
  int iterations,points;
  int iterationIndex=-1,pointIndex;
  Color[] colors = new Color[]{Color.BLUE,Color.GREEN, Color.RED, Color.CYAN,Color.ORANGE,Color.MAGENTA, Color.YELLOW,Color.BLACK, Color.PINK};
  
  
  public void init()
  {

    Container cp = getContentPane();
    cp.setLayout(new FlowLayout());
    cp.setBackground(Color.white);

    nextIteration = new JButton("Next");
    nextIteration.setLocation(100,400);
    cp.add(nextIteration);
    nextIteration.addActionListener(this);
    
    previousIteration = new JButton("Back");
    previousIteration.setLocation(100,400);
    cp.add(previousIteration);
    previousIteration.addActionListener(this);
    
    clear = new JButton("Initialize");
    clear .setLocation(100,400);
    cp.add(clear );
    clear.addActionListener(this);
    
    initialize();
    
  }
  
  public void initialize()
  {
	  iterationIndex = -1;
		try
		{
			FileReader fstream = new FileReader("debugOutput.txt");
			BufferedReader in = new BufferedReader(fstream);
			String str;
			iterations=Integer.parseInt(in.readLine());
			points = Integer.parseInt(in.readLine());
			values = new double[iterations][points][4];
			while(true)
			{
				str = in.readLine();
				if(str==null)
					break;
				///System.out.println(str);
				if(str.split("#").length!=1)
				{
					iterationIndex++;
					pointIndex = 0;
				}
				else
				{
					String [] tempStrArr = str.split("=");
					for(int z=0;z<tempStrArr.length;z++)
						values[iterationIndex][pointIndex][z]= Double.parseDouble(tempStrArr[z]);
					pointIndex++;
				}
			}
			in.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			 e.printStackTrace();
		}
		
		iterationIndex = 1;

  }
  
  public void drawPoint(Graphics2D g2,double x, double y, double id, double isFixed,Color c)
  {
	  g2.setPaint(c);
	  g2.fill(new Arc2D.Double(x, y, 5, 5, 90, 360, Arc2D.OPEN));
	  g2.setPaint(Color.black);
//	  g2.drawString("Filled Arc2D", (int)x, 250);
  }
  
  int R=0,G=0,B=250;
 public void paint(Graphics g)
 {
	 g.clearRect(0,0,1000,1000);
 	super.paint(g);

 	Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
 	
 	for(int i=0;i<iterationIndex;i++)
 	{
 		for(int j=0;j<points;j++)
 			drawPoint(g2,values[i][j][1],values[i][j][2],values[i][j][0],values[i][j][3],colors[i]);
 	}
	  
/*	    g2.setPaint(Color.gray);
	    int x = 50;
	    int y = 70;
//	    g2.setStroke(wideStroke);
	    g2.draw(new Arc2D.Double(x, y, 200, 200, 90, 270,Arc2D.OPEN));
	    g2.drawString("Arc2D", x, 250);
	    
	    g2.draw(new Line2D.Double(x, y, 200, 200));
	    g2.drawString("Line2D", x, 250);*/
  }
 
 public void actionPerformed(ActionEvent evt) 
 {
	 if (evt.getSource() == nextIteration)
	 {
		 if(iterationIndex < iterations)
			 iterationIndex++;
	 }
	 else if (evt.getSource() == previousIteration)
	 {
		 if(iterationIndex > 1)
			 iterationIndex--;
	 }
	 if (evt.getSource() == clear)
	 {
		 initialize();
	 }
	 repaint();
 }  
 

  public static void main(String[] args) {
    run(new DebugApplet(), 200, 500);
  }

  public static void run(JApplet applet, int width, int height) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(applet);
    frame.setSize(width, height);
    applet.init();
    applet.start();
    frame.setVisible(true);
  }
}