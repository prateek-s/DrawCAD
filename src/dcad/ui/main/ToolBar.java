package dcad.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.peer.DesktopPeer;


import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer; 

import dcad.util.GMethods;
import dcad.ui.drawing.DrawingView;
import dcad.model.constraint.Constraint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.segment.ConvertSegment;
import dcad.process.recognition.segment.SegmentRecognitionScheme;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.ConvertStrokeType;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.drawing.*;

public class ToolBar extends JPanel implements ActionListener
{
	private static final String REPLAY = "Replay";
	private static final String CLEAR = "Clear";
	private static final String UNDO = "Undo";
	private static final String REDO = "Redo";
	private static final String ERASE = "Erase";
	private static final String LOGS = "Logs";
	private static final String LEGEND = "Legend";
	private static final String INITIALIZE = "Initialize";
	private static final String HELP = "Help";
	private ScalingFactorsWindow scalingFW = null;
	private TestCases testCases = null;
	private DrawingView dv = null;
	// 8-09-09
	private static final String SCALING_FACTOR = "Set Scaling Factors";
	private static final String TESTCASES = "Run Testcases";
	private static final String GRID = "Add Grid";
	private static final String CONVERT = "Convert";
	
	JButton jbConvertElement = null;
	JButton jbTestCase = null;
	
	private static ToolBar toolBar;
	
	private WindowActions winAct = null;
	private DrawingData dData = null;
	
	private static int LINE_SEG = 0 ;
	private static int CIRCULAR_ARC = 1;
	private static boolean isConvertBitSet = false;
	private static int segIs = -1;
	private GeometryElement ge;
	private SegmentRecognizer segRecog = null;
	private Stroke parentstroke = null;
	
	private final int NORMAL_STROKE = 0;
	private final int MARKER = 1;
	
	public static ToolBar getInstance()
	{
		if (toolBar == null) toolBar = new ToolBar();
		return toolBar;
	}
	
	private ToolBar()
	{
		winAct = WindowActions.getInstance();
		
		init();
	}

	private void init()
	{
		GridLayout gl = new GridLayout(1, 1);
		setLayout(gl);
		setPreferredSize(new Dimension(10, 28));
		setBackground(Color.GRAY);

		//Create and add the edit toolbar
		JToolBar EditTB = new JToolBar("Edit Toolbar");
		addEditButtons(EditTB);
		EditTB.setMargin(new Insets(3, 2, -2, 2));
		add(EditTB, BorderLayout.WEST);

		//Create and add the drawing toolbar
		JToolBar drawTB = new JToolBar("Drawing Toolbar");
		addDrawButtons(drawTB);
		drawTB.setMargin(new Insets(3, 2, -2, 2));
		add(drawTB, BorderLayout.EAST);
	}
	
	protected void addEditButtons(JToolBar toolBar)
	{
		JButton button = null;

	
		
		button = makeNavigationButton("clear", CLEAR, "Clear the contents of the drawing view", CLEAR);
		toolBar.add(button);

		button = makeNavigationButton("Erase-icon", ERASE, "Erase/remove the selected components", ERASE);
		toolBar.add(button);

		button = makeNavigationButton("Undo-icon", UNDO, "Undo the last action", UNDO);
		toolBar.add(button);

		button = makeNavigationButton("Redo-icon", REDO, "Redo the last Undo operation", REDO);
		toolBar.add(button);

		button = makeNavigationButton("Replay-icon", REPLAY, "Replays the current drawing", REPLAY);
		toolBar.add(button);

		//button = makeNavigationButton("initialize", INITIALIZE, "Initialize the properties", INITIALIZE);
		//toolBar.add(button);
	
	
		
		// 28-09-09
		button = makeNavigationButton("grid-icon", GRID, "Adds grid to the drawing window", GRID);
		toolBar.add(button);
		
		button = makeNavigationButton("help", HELP, "Open the tutorial", HELP);
		toolBar.add(button);
		
		
	}

	protected void addDrawButtons(JToolBar toolBar)
	{
		JButton button = null;

		button = makeNavigationButton("log", LOGS, "Show commands generated as a part of textual representation of the drawing", LOGS);
		toolBar.add(button);

		button = makeNavigationButton("legend", LEGEND, "Show color codes for the drawing", LEGEND);
		toolBar.add(button);
	// 08-09-09	
		button = makeNavigationButton("", SCALING_FACTOR, "Facilitates setting values of scaling factors", SCALING_FACTOR);
		toolBar.add(button);
		
		jbTestCase = makeNavigationButton("", TESTCASES, "It will run test cases", TESTCASES);
		toolBar.add(jbTestCase);
		
		jbConvertElement = makeNavigationButton("", CONVERT, "It will convert marker to normal stroke and vice-versa or the selected elements recognition scheme", CONVERT);
		toolBar.add(jbConvertElement);
	
		
}

	protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText)
	{
		//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setIcon(GMethods.getImageIcon(imageName+".gif",altText));
		button.setText(altText);
		return button;
	}

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		if(CLEAR.equals(cmd))
		{ 
			if(JOptionPane.showConfirmDialog(this, "The Drawing View will be cleared. \n  This action cannot be undone!\n               Continue?") ==  JOptionPane.OK_OPTION)
				winAct.clearDrawingData();
		}
		else if(UNDO.equals(cmd))
			winAct.undo();
		else if(REDO.equals(cmd))
			winAct.redo();
		else if(ERASE.equals(cmd))
			winAct.deleteSelection();
		else if(LOGS.equals(cmd))
			winAct.showCommandsWindow();			
		else if(LEGEND.equals(cmd))
			winAct.showLegend();			
		else if(REPLAY.equals(cmd))
			winAct.replay();			
		else if(INITIALIZE.equals(cmd))
			GMethods.initializeProperties();
			//GMethods.getCurrentView().getM_drawData().resetDrawingRatio();
		else if(HELP.equals(cmd)){
			URI uri = null;
			try {
				uri = new URI("http://www.cse.iitb.ac.in/~skumar/project/DrawCAD/tutorial/index.html");
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
          //  throws IOException
		}
		else if(SCALING_FACTOR.equals(cmd)){
			if(scalingFW == null){
				scalingFW = new ScalingFactorsWindow();
				scalingFW.createAndShowGUI();	
				
			}
			else{
				scalingFW.setModal(true);
				scalingFW.setCursorPosition();
				scalingFW.setVisible(true);
			}
			
		}
		else if(TESTCASES.equals(cmd)){
			 int index = 0;
			 winAct.clearDrawingData();
			if(testCases == null){
				index = 0;
			testCases = new TestCases();
			testCases.ExtractFile(index);
			testCases.setFlag(1);
			setText("Run Next Testcase");
			testCases.SetIndex(++index);
			}
			else{
				index = testCases.GetIndex();
				testCases.ExtractFile(index);
				testCases.setFlag(1);
				if((testCases.getEleCount() -1) == (index)){
					testCases = null;
					setText("Run Testcases");
				      JOptionPane.showMessageDialog(jbTestCase,"Testcases finished");
				}
				else{
					testCases.SetIndex(++index);
				}
			}
		}
		else if(GRID.equals(cmd)){
			
			if(dv == null){
				    dv = MainWindow.getDv();
			}
			boolean status = dv.isM_gridActive();
			dv.setM_gridActive(!status);	
			dv.repaint();
		}
	// 22-01-10	
		else if(CONVERT.equals(cmd)){
			if(dv == null){
			    dv = MainWindow.getDv();
			}
			// To convert marker to normal stroke and vice-versa
			if(dv.getSelectedElements() != null){
				if(dv.getSelectedElements().size() == 0){
					ConvertStrokeType convStrokeType = new ConvertStrokeType();
					convStrokeType.ConvertLastDrawnStroke();
				}
				else if(dv.getSelectedElements().size() == 1){
					ConvertSegment convSegment = new ConvertSegment();
					convSegment.ConvertSelectedSegment();
					///System.out.println("Convert element");
				}
			}
			/*if(dv == null){
			    dv = MainWindow.getDv();
			}
			
			
			Vector stkList = dv.getM_drawData().getStrokeList();
			Stroke stk = null;
			if(stkList != null){
				
				// get the last stroke
				stk = (Stroke)stkList.get(stkList.size()-1);
				//stk = dv.getM_drawData().getLastStroke(true);
				// create a new stroke
				//dv.getCurrentStroke();
				
				int x = 0;
				int y = 0;
				long time = 0; 
				int count = 0;
				int buttonType = 1;
				// get the list of raw points
				Vector rawptList = stk.getM_ptList();
				
				// find the stroke type of current stroke
			
				int strokeType = stk.getM_type();
				
				//delete the earlier stroke
				dv.setStrokeConverted(true);
				boolean converted = true;
				GMethods.getCurrentView().logEvent("setStrokeConverted({boolean}" + converted + ");");
				if(strokeType == MARKER){
					// change to normal stroke
					Vector markerList = dv.getM_drawData().getM_markers();
					Marker marker = (Marker)markerList.get(markerList.size()-1);
					
					// add to transcript
					GMethods.getCurrentView().logEvent("DrawingData|removeMarker({Marker}" + marker + ");");
					//GMethods.getCurrentView().logEvent(Command.PAUSE);
					
					dv.getM_drawData().removeMarker(marker);
					dv.setStrokeConvertedTo(NORMAL_STROKE);
					GMethods.getCurrentView().logEvent("setStrokeConvertedTo({int}" + NORMAL_STROKE + ");");
				}
				else if(strokeType == NORMAL_STROKE){
					dv.setStrokeConvertedTo(MARKER);
					GMethods.getCurrentView().logEvent("setStrokeConvertedTo({int}" + MARKER + ");");
					// set bit to marker
				}
				
				// add to transcript
				GMethods.getCurrentView().logEvent("Stroke|delete();");
				//GMethods.getCurrentView().logEvent(Command.PAUSE);
				
				stk.delete();
				
				Iterator iter = rawptList.iterator();
				while(iter.hasNext()){
					PixelInfo pi = (PixelInfo)iter.next();
					x = (int)pi.getX();
					y = (int)pi.getY();
					time = pi.getTime();
					// add them to current stroke
					//dv.addPointToStroke(x, y, time);
					
					// add to transcript
					if(count == 0){
						dv.mouseButton1Pressed(x, y, time);
						//dv.logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
						//dv.logEvent("mouseButton1Pressed({int}" + x + ", {int}" + y + ", {long}" + time + ");");
					}
					else{
						dv.mouseDragged(x, y, time);
						//dv.logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
					}
					count ++;
				}
				//dv.logEvent("mouseReleased({int}" + x + ", {int}" + y + ", {int}" + buttonType + ");");
			//	dv.getCurrentStroke().setM_ptList(rawptList);
				//dv.getM_drawData().removeUnusedMarker();
				dv.mouseReleased(x, y, buttonType);
			}
			*/
		}
		
	}
			
	/*	if(dv == null){
    dv = MainWindow.getDv();
}
Vector selElements = dv.getSelectedElements();


if(selElements != null){
	if(selElements.size() == 1){
		ge = (GeometryElement)selElements.get(0);
		Vector rawPoints = new Vector();
		Stroke stroke = dv.getCurrentStroke();
// if selected element is a line
// 1. get the pixels of the raw segment.	
//  2. Delete the selected element		


	if(ge instanceof SegLine){
			///System.out.println("Line");
			rawPoints = ((SegLine) ge).getRawPoints();
			ge.delete();
	
			segIs = LINE_SEG;
			// 23-02-10
			stroke.setIsConvertedTo(CIRCULAR_ARC);
			stroke.setConverted(true);
			isConvertBitSet = true;
		}

		else if(ge instanceof SegCircleCurve){
			///System.out.println("Circular arc");
			rawPoints = ((SegCircleCurve) ge).getRawPoints();
			ge.delete();
			
			stroke.setIsConvertedTo(LINE_SEG);
			stroke.setConverted(true);
			segIs = CIRCULAR_ARC;
			isConvertBitSet = true;
		}
		
		
	// 3. Form another stroke containing these pixels.	
		Iterator iter;
		iter = rawPoints.iterator();
		while (iter.hasNext())
		{	
			PixelInfo pi =  (PixelInfo)iter.next();
			stroke.addPoint((int)pi.getX(),(int)pi.getY() ,pi.getTime());
		}
		
		Vector constraints = dv.addStroke(stroke);
		
	// 4. If earlier it was a line, fit the circular arc on pixels.			
		if (stroke.getM_type() == Stroke.TYPE_NORMAL)
		{
			if ((constraints != null) && (constraints.size() > 0))
			{
				if (ConstraintSolver.addConstraintsAfterDrawing(constraints) != null)
					dv.setJustAddedConstraints(constraints);
			}
			dv.snapIPsAndRecalculateConstraints();
			//GMethods.getHelpView().initialize(HelpView.afterDrawing);
		}
		
************	//method 2	comment		
		Vector constraints = ((SegLine) ge).getM_constraints();
		if(ge instanceof SegLine){	
			dData = dv.getDrawData();
	
			// to remove constraints from constraint vector
			Iterator iter;
			iter = constraints.iterator();
			while (iter.hasNext()){
			///System.out.println("remove constraint");
			Constraint cons =  (Constraint)iter.next();
			dData.removeConstraint(cons);
			}
		
	   
			//to remove the related constraints from other segment's constraints
			// like parallel line, equal line and also from recognized view 	
			Vector stkList = dData.getStrokeList(); 	
			Iterator iter1;
			iter1 = stkList.iterator();
			while (iter1.hasNext()){
					Stroke stk = (Stroke)iter1.next();
		
					Vector segList = new Vector();
					segList = stk.getM_segList();
		
					Iterator iter2;
					iter2 = segList.iterator();
		
						while(iter2.hasNext()){
							Segment seg = (Segment)iter2.next();
							if(seg instanceof SegLine){
								if(((SegLine)seg).equals((SegLine)ge)){
									continue;
								}
							}
							iter = constraints.iterator();
							while (iter.hasNext()){
								///System.out.println("remove constraint");
								Constraint cons =  (Constraint)iter.next();
								seg.removeConstraint(cons);
							}			
						}
			// change the segment type from line to circular arc
			
			//SegmentRecognitionScheme brs = segRecog.recognizeSegment((Segment)ge);
			SegLine l = (SegLine)ge;
			parentstroke = l.getM_parentStk();
			
			Vector consts = dv.recognizeSegmentsAndConstraints(parentstroke);
			if (stroke.getM_type() == Stroke.TYPE_NORMAL)
			{
				if ((consts != null) && (consts.size() > 0))
				{
					if (ConstraintSolver.addConstraintsAfterDrawing(consts) != null)
						dv.setJustAddedConstraints(consts);
				}
				dv.snapIPsAndRecalculateConstraints();	
			}
	
		((SegLine) ge).removeAllConstraints();
********			
		dv.setLastStrokeBit(true);
		dv.repaint();
		DrawingData m_drawData = dv.getDrawData();
		MainWindow.getRecognizedView().reset(m_drawData.getM_constraints());
	}
}*/

			
		/*		
				
				
			*/	
				
	
	
	void setText(String text){
		jbTestCase.setText(text);
	}
	void setTestCaseNull(){
		testCases = null;
		setText("Run Testcases");
	}
	
	//22-01-10
	
	public boolean isConvertActiveBitSet(){
		return isConvertBitSet;
	}
	
	public int segRecog(){
		return segIs;
	}
	
	public void setConvertActiveBit(boolean value){
		isConvertBitSet = value;
	}
	
	public void setSegRecog(int value){
		segIs = value;
	}
	public GeometryElement getGeomElement(){
		return ge;
	}
	
	public Stroke getParentStroke(){
		return parentstroke;
	}
}
