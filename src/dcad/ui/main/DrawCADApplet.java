package dcad.ui.main;

import java.awt.Toolkit;
import java.awt.peer.LightweightPeer;

import javax.swing.JApplet;

import dcad.ui.drawing.DrawingView;
import dcad.util.GMethods;

public class DrawCADApplet extends JApplet
{
	public boolean isStarted = false;
	
	public void init()
	{	
		if(!isStarted)
		{
			GMethods.applet = this;
			
//			System.out.println("Applet code base : " + this.getCodeBase() + "\n");
//			System.out.println("Applet document base : " + this.getDocumentBase() + "\n" );
			GMethods.codeBase = this.getCodeBase().toString();
			GMethods.init("DrawCAD.properties");

			//this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

			this.setContentPane(MainWindow.getApplicationContentPane());
			this.setJMenuBar(MainWindow.getApplicationMenuBar());
			MainWindow.startApplication();
			System.out.println("\n\n***************    Started   *****************");

			isStarted=true;
		}

	}
}
