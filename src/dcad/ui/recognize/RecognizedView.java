package dcad.ui.recognize;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.FontMetrics;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.IndependentConstraint;
import dcad.model.constraint.IndependentPointConstraints;
import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.recognition.constraint.ConstraintRecogManager;
import dcad.process.recognition.constraint.RelAngleRecognizer;
import dcad.process.recognition.constraint.RelConstraintRecognizer;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.ui.drawing.DrawingData;
import dcad.ui.drawing.DrawingView;
import dcad.ui.help.HelpDrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.process.recognition.*;

/**
 * List of all constraints are displayed. <location is at the bottom>. List is editable.
 * @author prateek
 *
 */
public class RecognizedView extends JPanel implements MouseListener,MouseMotionListener, KeyListener,
KeyEventDispatcher
{
    private JList list;
    private DefaultListModel listModel;
    private String str4DeletedCons = " X ";
    
    private Vector highlightedElements= new Vector();
    private static HelpDrawingView helpDrawView = null;
    //This has all constraints of the current drawing 
    Vector allConstraints=new Vector();
    static int count = 0;
    //This has the list of constraints to be shown
    //This list can be different from the list of all constraints based upon the last user operation
    //Selection of an element changes this list. Movement / Drawing operation by user changes this list.
    Vector listConstraints=new Vector();

	public RecognizedView(){
		super(new BorderLayout());

		setAutoscrolls(true);
		this.setBackground(GVariables.BACKGROUND_COLOR);

        listModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(list, BorderLayout.CENTER);
        list.addMouseListener(this);
        list.addMouseMotionListener(this);
	}
	
	private String getStr4DeletedCons(Constraint c){
		if(! c.isDeleted())
			return c.toString();
		return str4DeletedCons + c.toString();
	}

	public void addConstraint(Constraint c)
	{
		if(listModel.contains(c)) return ;
		listModel.addElement(getStr4DeletedCons(c));
		listConstraints.addElement(c);
		allConstraints.addElement(c);
	}
	
	public void clear(){
		listModel.removeAllElements();
		allConstraints.clear();
		listConstraints.clear();
		updateUI();
	}
	
	/**
	 * Is the method called by other parts of the program, displays the argument vector
	 * (constraints), updates UI etc.
	 * @param cons
	 */
	public void reset(Vector cons){
		clear();
		Iterator iterator = cons.iterator();
		while (iterator.hasNext())
			addConstraint((Constraint) iterator.next());
		updateUI();
	}
	
	private void show(Vector cons){
		Vector v = cons;
		if(v==null)
			v = allConstraints;
		listModel.removeAllElements();
		listConstraints.clear();
		for (int i = 0; i < v.size(); i++){
			Constraint c = (Constraint) v.elementAt(i);
			listModel.addElement(getStr4DeletedCons(c));
			listConstraints.addElement(c);
		}
	}
	
	public void updateSelection(Vector selectedElements){
		list.clearSelection();

		Vector cons = new Vector();
		
		if(selectedElements.size()==1){
			GeometryElement element = (GeometryElement) selectedElements.get(0);
			if(element instanceof Segment)
				cons.addAll(((Segment)element).getM_constraints());
			else if(element instanceof AnchorPoint){
				cons.addAll(constraintsHelper.getAllIndependentPointConstraints((AnchorPoint)element));
				cons.addAll(constraintsHelper.getPointSegmentConstraintsOfPoints((AnchorPoint)element));
			}
		}
		else{
			Vector aps = new Vector(), segments = new Vector();
			int i,j;
			for(i=0;i<selectedElements.size();i++){
				if(selectedElements.get(i) instanceof AnchorPoint)
					aps.add((AnchorPoint)selectedElements.get(i));
				else if(selectedElements.get(i) instanceof Segment)
					segments.add((Segment)selectedElements.get(i));
			}
			int apSize = aps.size(), segSize = segments.size();
			Vector v;
			
			for(i=0;i<apSize;i++)
				for(j=i+1;j<aps.size();j++){
					v = constraintsHelper.getIndependentPointConstraints((AnchorPoint)aps.get(i),(AnchorPoint)aps.get(j));
					cons.removeAll(v);
					cons.addAll(v);
				}
			for(i=0;i<apSize;i++)
				for(j=0;j<segSize;j++){
					
				}
			for(i=0;i<segSize;i++)
				for(j=i+1;j<segSize;j++){
					v = constraintsHelper.getRelativeConstraintsBetween2Segments((Segment)segments.get(i),(Segment)segments.get(j));
					cons.removeAll(v);
					cons.addAll(v);
				}
			
			
/*			Iterator iter = gEles.iterator();
			while (iter.hasNext())
			{
				GeometryElement element = (GeometryElement) iter.next();
			if(element instanceof Segment)
			{
				Segment seg = (Segment)element;
				cons.removeAll(seg.getM_constraints());
				cons.addAll(seg.getM_constraints());
				}
			}*/
		}
		if(selectedElements.size()!=0)
			show(cons);
		else
			show(null);
		updateUI();
	}
	/**Function to check is this collinear lines constraint
	 * @author Sunil Kumar
	 */
	//added on 22-05-10
	public boolean isThisCollinearLinesCons(Constraint cons){
		String constraintString = cons.toString();
		String parsedCons[];
		parsedCons = constraintString.split("[ ]+");

		if((parsedCons[3].compareToIgnoreCase("collinear") == 0) && (parsedCons[4].compareToIgnoreCase("lines") == 0)){
					return true;
			}
		else 
			return false;
	}
	
	//added on 22-05-10
	/**Function to add parallel lines constraint between two lines
	 * @author Sunil Kumar
	 */
	public void addParallelLinesCons(SegLine seg1, SegLine seg2){
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		ConstraintRecogManager consRecogMan = recogMan.getConstraintRecogManager();
		RelConstraintRecognizer relConsRecog = consRecogMan.getRelConsRecog();
		
		//get the parallel lines constraint between lines
		Vector angleCons = relConsRecog.recogAngleConstraints();
		RelAngleRecognizer relAngleRecog =  new RelAngleRecognizer(seg1, seg2);
		Constraint cons = relAngleRecog.getParallelSegmentsConstraint(seg1, seg2, Constraint.HARD,true);
		
		if(cons != null){
			///System.out.println("constraint returned");
		}
	
		
		DrawingView dv = MainWindow.getDv();
		
		DrawingData m_drawData  = dv.getDrawData();
		if (cons != null){
			//add this constraint in the list of drawData constraints
			m_drawData.addConstraint(cons);
			
			// also in the list of constraint window or recognize view
			//addConstraint(cons);
			///System.out.println("Constraint added");	
		}
		/*
			Vector constraints = new Vector();
			constraints.add(cons);
			
			if ((constraints != null) && (constraints.size() > 0))
			{
				///System.out.println("Constraint is not null");
				if (ConstraintSolver.addConstraintsAfterDrawing(constraints) != null){
					dv.setJustAddedConstraints(constraints);
					///System.out.println("Constraint added after drawing");
				}
			}*/
			//dv.snapIPsAndRecalculateConstraints();
			//GMethods.getHelpView().initialize(HelpView.afterDrawing);
		//}
			
		//dv.repaint();
	}
	

	public void deleteConstraint(int index){
		GMethods.getCurrentView().logEvent("RecognizedView|deleteConstraint({int}" + index + ");");
		GMethods.getCurrentView().logEvent(Command.PAUSE);
		
		Constraint c=(Constraint)listConstraints.get(index);
		
		//added on 22-05-10
		// if constraint is collinear lines then need to add parallel lines 
		// constraint between the two
		if(isThisCollinearLinesCons(c)){
			///System.out.println("This is collinear lines constraint");
			// get both the line segments
			RelativeConstraint rc=(RelativeConstraint)c;
			
			//add parallel lines constraint between both the segments
			//DrawingView dv = MainWindow.getDv();
			//dv.snapIPsAndRecalculateConstraints();
			
			addParallelLinesCons((SegLine)rc.getM_seg1(), (SegLine)rc.getM_seg2());
			
		}
		
		//If it is HARD (not promoted) OR it is already deleted
		if(c.isDeleted() || !c.isPromoted()){
			c.remove();
			allConstraints.remove(c);
			listModel.remove(index);
			listConstraints.remove(index);
		}
		else{
			c.setDeleted(true);
			listModel.remove(index);
			listModel.add(index, getStr4DeletedCons(c));
		}
		GMethods.getCurrentView().repaint();
		updateUI();
	}
	
	public void mouseReleased(MouseEvent e){

		if(e.getButton() == MouseEvent.BUTTON2){
/*			if(list.getCellBounds(0,0)!=null) // There is at least one constraint
			{
				int index = (int)(e.getPoint().getY()/list.getCellBounds(0, 0).getHeight());
				deleteConstraint(index);
			}*/
		}
		if(e.getButton() == MouseEvent.BUTTON1){
/*			int index = (int)(e.getPoint().getY()/list.getCellBounds(0, 0).getHeight());
			Constraint cons=(Constraint)listConstraints.get(index);
			if(!(cons.getM_category()==Constraint.SOFT))
				cons.setPromoted(false);
			//GMethods.getCurrentView().repaint(); */
			
			
		}
	}
	
	private void clearHighlighting(){
		while(highlightedElements.size()!=0){
			((GeometryElement) highlightedElements.get(0)).setHighlighted(false);
			highlightedElements.remove(0);
		}
	}
	
	public void mouseMoved(MouseEvent e){
		int index = -1;
		if(list.getCellBounds(0,0) !=null )
			index = (int)(e.getPoint().getY()/list.getCellBounds(0, 0).getHeight());
		clearHighlighting();
		if(index>=0 && index<listConstraints.size()){
			Constraint c=(Constraint)listConstraints.get(index);
			if(c instanceof RelativeConstraint){
				RelativeConstraint rc=(RelativeConstraint)c;
				rc.getM_seg1().setHighlighted(true);
				rc.getM_seg2().setHighlighted(true);
				highlightedElements.add(rc.getM_seg1());
				highlightedElements.add(rc.getM_seg2());
			}
			else if(c instanceof IndependentConstraint){
				IndependentConstraint ic=(IndependentConstraint)c;
				ic.getM_seg().setHighlighted(true);
				highlightedElements.add(ic.getM_seg());
			}
			else if(c instanceof IndependentPointConstraints){
				IndependentPointConstraints ip=(IndependentPointConstraints)c;
				Vector v=ip.getPoints();
				AnchorPoint p;
				for(int i=0;i<v.size();i++){
					p=(AnchorPoint)v.get(i);
					p.setHighlighted(true);
					highlightedElements.add(p);
				}
			}
			else if(c instanceof PointSegmentConstraint){
				PointSegmentConstraint p=(PointSegmentConstraint)c;
				p.getM_seg().setHighlighted(true);
				p.getM_ap().setHighlighted(true);
				highlightedElements.add(p.getM_seg());
				highlightedElements.add(p.getM_ap());
			}
			
			list.setSelectedIndex(index);
		}
			
		GMethods.getCurrentView().repaint();
	}
	
	public void mouseExited(MouseEvent e) {
		clearHighlighting();
		GMethods.getCurrentView().repaint();
		removeKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		MainWindow.getM_statusBar().setCoordLabelText("");
		helpDrawView.unselectRows();
	}
	
	public void mouseDragged(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}
	
	public void mouseEntered(MouseEvent e) {
		requestFocusInWindow();
		removeKeyListener(this);
		addKeyListener(this);
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		if(helpDrawView == null){
			helpDrawView = MainWindow.getHelpDrawingView();
		}
		helpDrawView.selectRows(GConstants.CONSTRAINT_VIEW);
	}

	public void keyTyped(KeyEvent e){
		char c = e.getKeyChar();
		switch (c){
		case KeyEvent.VK_DELETE:
			// Delete the selected elements
			deleteKeyPressed();
			break;
		default:
			break;
		}
	}

	private void deleteKeyPressed(){
		deleteConstraint(list.getSelectedIndex());
	}
	
	public void keyPressed(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {}
	
	public boolean dispatchKeyEvent(KeyEvent e){
		processKeyEvent(e);
		return true;
	}
	
	//Code added
	public Vector return_all_constraints(){
		return allConstraints;
	}
	public Vector getListConstraints(){
		return listConstraints;
	}
}