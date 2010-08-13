package dcad.ui.main;

import java.applet.AppletContext;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.PixelInfo;
import dcad.process.io.CommandQueue;
import dcad.process.io.IOManager;
import dcad.process.io.InputHandler;
import dcad.process.io.OutputHandler;
import dcad.ui.drawing.CommandView;
import dcad.ui.drawing.CommandWindow;
import dcad.ui.drawing.DrawingData;
import dcad.ui.drawing.DrawingView;
import dcad.ui.help.HelpView;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.model.geometry.Stroke;
import dcad.model.marker.Marker;
import dcad.model.constraint.Constraint;
import dcad.model.geometry.Text;

public class WindowActions
{
	private static WindowActions winAct = null;
	private CommandQueue cq = null;
	private LegendWindow lw = null;
	private Vector undoVector = null;
	DrawingView dv = null;
	// 06-10-09
	//indexes in a particular state needed for assigning values to original vectors 
	private static int STROKE = 0;
	private static int MARKER = 1;
	private static int TEXT_ELEMENT = 2;
	private static int CONSTRAINT = 3;
	private static int undoBit = 0;
	
	// it is useful to check whether previous operation was undo to clear undo markers
	public static int getUndoBit() {
		return undoBit;
	}


	public static void setUndoBit(int undoBit) {
		WindowActions.undoBit = undoBit;
	}


	public Vector getUndoVector() {
		return undoVector;
	}


	public void resetUndoVector() {
		if(undoVector != null )
		this.undoVector.clear();
	}


	private static int undoIndex = -1;

	
	public static int getUndoIndex() {
		return undoIndex;
	}


	public static void setUndoIndex(int undoIndex) {
		WindowActions.undoIndex = undoIndex;
	}


	public void addElementToUndoVector(){
		DrawingView dv = GMethods.getCurrentView();
		DrawingData m_drawData = dv.getDrawData();
		
		Vector strokeListVector = new Vector();
		strokeListVector = m_drawData.getStrokeList();
		strokeListVector = copyStrokeList(strokeListVector);
		
		Vector markerVector = new Vector();
		markerVector = m_drawData.getM_markers();
		markerVector = copyMarkers(markerVector);
		
		Vector textElementVector  = new Vector();
		textElementVector = m_drawData.getM_textElements();
		textElementVector = copyTextElements(textElementVector);
		
		Vector constraintsVector  = new Vector();
		constraintsVector = m_drawData.getM_constraints();
		constraintsVector = copyConstraints(constraintsVector);
		
		Vector currState = new Vector();
		currState.add(strokeListVector);
		currState.add(markerVector);
		currState.add(textElementVector);
		currState.add(constraintsVector);
		
		if(undoVector == null){
			undoVector = new Vector();
		}
		undoVector.add(currState);
		undoIndex++;
	}
	
	public Vector copyStrokeList(Vector strokeList){
		Vector vect = new Vector();
		int index = 0;
		for (index = 0; index <= (strokeList.size()-1); index++){
			vect.add((Stroke)strokeList.get(index));
		}
		return vect;
	}
	
	public Vector copyMarkers(Vector markerList){
		Vector vect = new Vector();
		int index = 0;
		for (index = 0; index <= (markerList.size()-1); index++){
			vect.add((Marker)markerList.get(index));
		}
		return vect;
	}
	
	public Vector copyTextElements(Vector textElementsList){
		Vector vect = new Vector();
		int index = 0;
		for (index = 0; index <= (textElementsList.size()-1); index++){
			vect.add((Text)textElementsList.get(index));
		}
		return vect;
	}
	
	public Vector copyConstraints(Vector constraintsList){
		Vector vect = new Vector();
		int index = 0;
		for (index = 0; index <= (constraintsList.size()-1); index++){
			vect.add((Constraint)constraintsList.get(index));
		}
		return vect;
	}
	//
	
	public void removeUndoVectorElements(){
		int index;
		for (index = undoIndex+1; index < undoVector.size(); index++ ){
			undoVector.removeElementAt(index);
		}
	}
	
	public static WindowActions getInstance()
	{
		if(winAct == null) winAct = new WindowActions();
		return winAct;
	}
	
	private WindowActions()
	{
		
		lw = new LegendWindow();
	}
	
	WindowActions(String File){
		
	}
	
	public void clearDrawingData()
	{
		DrawingView dv = GMethods.getCurrentView();
		DrawingData m_drawData = dv.getDrawData();
		m_drawData.clearStrokeList();
		m_drawData.clearMarkers();
		m_drawData.clearConstraints();
		m_drawData.clearTextElements();
		resetUndoVector();
		setUndoIndex(-1);
		
		dv.clearView();
		RecognizedView rv = MainWindow.getRecognizedView();
		rv.clear();
	}
	
	public void undo()
	{
		
		DrawingView dv = GMethods.getCurrentView();
//		Cursor prevCursorType = dv.getCursor();
//		dv.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//		dv.repaint();
//		dv.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		GVariables.undoing=true;
		// added 06-10-09
	/*	if(undoIndex > 0){
			undoBit = 1;
			System.out.println("undo" + undoIndex);
		 		resetVectors(--undoIndex);
		 	}	
		*/
		
		if(cq == null)
		{
			cq = dv.getM_drawData().getM_commands();
		}
		CommandQueue tempCQ = cq;
		// do the repeat the processing till before this breakpoint
		int prevPauseIndex = cq.prevPauseIndex();
		//System.out.println(prevPauseIndex+"###############");
		if(prevPauseIndex >= 0)
		{
			// prev pause statement found, execute from start to prevPauseIndex
			cq.execUpto(prevPauseIndex);
		}
		cq = tempCQ;
		
		//unHighlightElements(dv.getM_highlightedElements());
		
		
		
		GVariables.undoing=false;
//		dv.setCursor(prevCursorType);
		dv.repaint();
//		JOptionPane.showMessageDialog(dv,"DONE!");
	}
	
	private void unHighlightElements(Vector highlightedElements)
	{
		Iterator iter = highlightedElements.iterator();
		while (iter.hasNext())
		{
			GeometryElement ele = (GeometryElement) iter.next();
			ele.setHighlighted(false);
			iter.remove();
		}
	}
	
	public void replay()
	{
		undo();
		redo();
	}
	
	public void redo()
	{
		
		if(cq != null)
		{
			CommandQueue tempCQ = cq;
			// execute till the next pause statement
			cq.execWithPause();
			cq = tempCQ;
		}
		
	/*	
		// added 06-10-09
		if(undoVector != null){
			
			if(undoIndex < (undoVector.size() - 1)){
				undoBit = 0;
				System.out.println("redo" + undoIndex);
			resetVectors(++undoIndex);
			}
		}*/
		unHighlightElements(GMethods.getCurrentView().getM_highlightedElements());
		GMethods.getCurrentView().repaint();
	}
	
	void resetVectors(int index){
		DrawingView dv = GMethods.getCurrentView();
		DrawingData m_drawData = dv.getDrawData();
		Vector state = (Vector)undoVector.get(index);
		
		
		Vector strokeListVector= new Vector();
		strokeListVector = (Vector)state.get(STROKE);
		
		Vector markerVector = new Vector(); 
		markerVector= (Vector)state.get(MARKER);
		
		Vector textElementVector = new Vector();
		textElementVector = (Vector)state.get(TEXT_ELEMENT);
		
		Vector constraintsVector = new Vector();
		constraintsVector = (Vector)state.get(CONSTRAINT);
		/*
		m_drawData.setStrokeList(strokeListVector);
		m_drawData.setMarkers(markerVector);
		m_drawData.setTextElements(textElementVector);
		m_drawData.setM_constraints(constraintsVector); 
		*/
		m_drawData.clearStrokeList();
		
		m_drawData.clearMarkers();
		m_drawData.clearTextElements();
		m_drawData.clearConstraints();
		
		//dv.repaint();
		
		Iterator iter = strokeListVector.iterator();
		while (iter.hasNext())
		{
			 Stroke stk = (Stroke) iter.next();
			 m_drawData.addStroke(stk);
		}
		
		iter = markerVector.iterator();
		while (iter.hasNext())
		{
			 Marker marker = (Marker) iter.next();
			 m_drawData.addMarker(marker);
		}
		
		iter = textElementVector.iterator();
		while (iter.hasNext())
		{
			 Text text = (Text) iter.next();
			 m_drawData.addTextElement(text);
		}
		
		iter = constraintsVector.iterator();
		while (iter.hasNext())
		{
			 Constraint cons = (Constraint) iter.next();
			 m_drawData.addConstraint(cons);
		}
		
		RecognizedView recView = MainWindow.getRecognizedView();
		recView.reset(m_drawData.getM_constraints());
		//first clear the vectors and copy elements one by one 
	}
	
	public void deleteSelection()
	{
		DrawingView dv = GMethods.getCurrentView();
		dv.deleteKeyPressed();
	}

	public void showCommandsWindow()
	{
		DrawingView dv = GMethods.getCurrentView();
		CommandView cp = new CommandView(dv.getM_drawData().getM_commands());
		CommandWindow cw = new CommandWindow(cp);
		cw.createAndShowGUI();
//		System.out.println("WindowActions.showCommandsWindow()");
	}
	
	public void openExistingFileAction()
	{
		// save the contents of the screen first
		DrawingView dv = GMethods.getCurrentView();
		if(!dv.isM_saved()) saveAsMIAction();
		
		// load the file through a dialog box
		FileDialog fd = new FileDialog(new Frame(), "Load Existing File", FileDialog.LOAD);
		fd.setDirectory(IOManager.getLast_dir());
		fd.setFile(IOManager.DEF_FILENAME);
		fd.show();
		String file = fd.getFile();
		if (file != null)
		{
			String directory = fd.getDirectory();
			if(directory != null)
			{
				openFile(directory, file);
			}
		}
		dv.setM_saved(true);
		dv.setM_newFile(false);
	}

	public void reloadFileAction()
	{
		openFile(IOManager.getLast_dir(), IOManager.getFilename());
		DrawingView dv = GMethods.getCurrentView();
		dv.setM_saved(true);
	}
	
	public void newFileAction()
	{
		// save the contents of the screen first
		saveMIAction();

		DrawingView dv = GMethods.getCurrentView();
		dv.init();
		IOManager.setFilename(IOManager.DEF_FILENAME);
	}
	
	public void saveAsMIAction()
	{
		FileDialog fd = new FileDialog(new Frame(), "Save Drawing As..", FileDialog.SAVE);
		fd.setDirectory(IOManager.getLast_dir());
		fd.setFile(IOManager.getFilename());
		fd.show();
		String file = fd.getFile();
		if (file != null)
		{
			String directory = fd.getDirectory();
			if(directory != null)
			{
				try
				{
					OutputHandler OH = IOManager.getInstance().getOutputH();
					if(OH != null) OH.saveToUnKnownFile(directory, file);

//					// open the newly saved file
//					openFile(directory, file);
					// change the file name and the last visited directory
					IOManager.setLast_dir(directory);
					IOManager.setFilename(file);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		}
		DrawingView dv = GMethods.getCurrentView();
		dv.setM_saved(true);
		dv.setM_newFile(false);
	}

	public void saveMIAction()
	{
		//Code for storin the whole object.
		//GMethods.getCurrentView()
		try
		{
//			  DataOutputStream dos;
//			  dos = new DataOutputStream(new FileOutputStream("serialized.txt"));
//			  ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream("serialized.txt"));
//			  objOut.writeObject(GMethods.getCurrentView());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());
		}

	
		
		
		
//		System.out.println("MenuBar.saveMIAction()");
		DrawingView dv = GMethods.getCurrentView();
		
		// check if the drawing is saved, already
		if((!dv.isM_newFile()) && (!dv.isM_saved()))
		{
			// This is not a new file
			try
			{
				OutputHandler OH = IOManager.getInstance().getOutputH();
				if(OH != null) OH.saveToFile(IOManager.getLast_dir(), IOManager.getFilename());
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
		else if(dv.isM_newFile() && (!dv.isM_saved()))
		{
			// file has never been saved
			saveAsMIAction();
		}
	}

	public boolean openFile(String dir, String filename)
	{
//		System.out.println("MenuBar.openFile()");
		InputHandler IH = IOManager.getInstance().getInputH();
		if(IH != null)
		try
		{
			CommandQueue cq = IH.loadFile(dir, filename);
			if(cq != null)
			{
				// change the file name and the last visited directory
				IOManager.setLast_dir(dir);
				IOManager.setFilename(filename);
				cq.execAll();
			}
			else
			{
				// file loading failed, reload the pervious file again
				JOptionPane.showMessageDialog(null, "Opening file "+filename+" failed");
				reloadFileAction();
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return true;
	}

	public void setCq(CommandQueue cq)
	{
		this.cq = cq;
	}
	
	public void showLegend()
	{
		lw.createAndShowGUI();
	}

	
	public void showHelp()
	{
		HelpView.showHelp = !HelpView.showHelp;
/*	    if(GMethods.applet!=null)
	    {
	    	try
	    	{
	    		AppletContext a = GMethods.applet.getAppletContext();
	    		URL u = new URL("http://www.cse.iitb.ac.in/~chintan/MTP/");
	    		a.showDocument(u,"_self");
	    	}
	    	catch (Exception e)
	    	{
	    		;
	    	}
	    }
	    */
	}
	
}