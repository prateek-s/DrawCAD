package dcad.ui.help;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import dcad.Prefs;
import dcad.model.BoundingBox;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.circularArcConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.collinearity.CollinearPointsConstraint;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.pointOnSegment.pointOnPointConstraint;
import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.main.MainWindow;
import dcad.ui.main.StatusBar;
import dcad.ui.main.WindowActions;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;

public class HelpView extends JPanel implements ActionListener
{
	public static boolean showHelp = true;
	public static String afterDrawing = "afterDrawing.html"; 
	public static String afterMovement = "afterMovement.html";
	public static String movementFailed = "movementFailed.html";
	public static String constraintAddingFailed = "constraintAddingFailed.html";
	public static String afterClear = "index.html"; 
	public static String baseURLForHelp = "http://www.cse.iitb.ac.in/~chintan/DrawCAD/help/";
	//public static String baseURLForHelp = "http://10.129.30.109/help/";
	
	private JEditorPane contents;
	private String currentURL; 
	
	Stack history = new Stack();
	JButton backButton = new JButton();
	JButton topButton = new JButton();
	JButton showHelpButton = new JButton();
	
	public HelpView()
	{
		currentURL = baseURLForHelp;
		this.setBackground(GVariables.BACKGROUND_COLOR);
		this.setLayout(new BorderLayout());
		
		contents = new JEditorPane();
		contents.setEditable( false );
		contents.setFont((new Font("Dialog", 20, Font.PLAIN)));

		contents.addHyperlinkListener(
				new HyperlinkListener() {
					public void hyperlinkUpdate( HyperlinkEvent e )
					{
						JEditorPane pane = (JEditorPane) e.getSource();
						if (e instanceof HTMLFrameHyperlinkEvent) 
						{
							HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
							HTMLDocument doc = (HTMLDocument)pane.getDocument();
							doc.processHTMLFrameHyperlinkEvent(evt);
						} 
						if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
							getThePage( e.getURL().toString() );
					}
					
				}
		);
		
		
		showHelpButton.setText("Turn off help");
		showHelpButton.addActionListener(this);
		
		//backButton.setPreferredSize(new Dimension(70,40));
		backButton.setText("Back");
		backButton.addActionListener(this);
		
		//topButton.setPreferredSize(new Dimension(70,40));
		topButton.setText("Top");
		topButton.addActionListener(this);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBackground(Color.WHITE);
		panel.add(showHelpButton);
		panel.add(backButton);
		panel.add(topButton);
		
		this.add(panel,BorderLayout.NORTH);
		
		this.add(new JScrollPane(contents));
		//this.add(contents);
		
		getThePage(currentURL);
		
	}
	
	private void getThePage( String location )
	{
		Font fnt = new Font("Courier New", Font.PLAIN, 25);
		UIManager.put("EditorPane.font", fnt);
		contents.setFont((new Font("Dialog", 20, Font.PLAIN)));
		if(currentURL != null)
			history.push(currentURL);
		currentURL = location;
		setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
		try 
		{
			//There is some bug in the JEditorPane class.
			//Because of that, we have to call this more than 2 times.
			if(showHelp)
			{
				contents.setPage( location );
				contents.setPage( location );
				contents.setPage( location );
			}
		}
		catch ( Exception io ) 
		{
			JOptionPane.showMessageDialog( this, "Help is not currently available.", "Bad URL", JOptionPane.ERROR_MESSAGE );
		}
		setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
	}
	
	public void initialize(String location)
	{
		//history.clear();
		//currentURL = null;
		getThePage(baseURLForHelp + location);
	}
	
	public void goBackOnePage()
	{
		if(history.size() > 0)
		{
			getThePage((String)history.pop());
			//The function above adds back the POPed URL to the stack. Remove it. 
			history.pop();
		}
	}
	
	public void handleHelpFunctions()
	{
		if(showHelp)
		{
			showHelp = false;
			showHelpButton.setText("Turn on help");
		}
		else
		{
			showHelp = true;
			showHelpButton.setText("Turn off help");
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == backButton)
			goBackOnePage();
		else if(e.getSource() == topButton)
			getThePage(baseURLForHelp);
		else if(e.getSource() == showHelpButton)
			handleHelpFunctions();
				
	}
	
	public void refresh()
	{
		getThePage(currentURL);
	}
	   
}

/*	      contents.setContentType("text/html");
contents.setText("<HTML><b>chintan</b><img src=\"http://www.cse.iitb.ac.in/~chintan/DrawCAD/help/images/underSegmentation1.gif\"></HTML>");*/

