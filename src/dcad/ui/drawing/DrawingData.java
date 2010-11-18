package dcad.ui.drawing;

import java.util.Iterator;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.length.IndLengthConstraint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.io.CommandQueue;
import dcad.util.GConstants;
import dcad.util.GVariables;


/**
 * Strokes, constraints, markets, anchors, text etc added by recognizeSegmentsAndConstraints (primarily)
 * DrawingData object contains all the displayable objects in the current drawing
 */
public class DrawingData
{
	
	private static Vector m_constraints;
	/**
	 * Stroke List. Accessed for..
	 */
	private static Vector<Stroke> m_stkList;
	//private Vector m_geoElements;
	private static Vector m_textElements;
	private static Vector m_markers;
	private static CommandQueue m_commands; 
	/**
	 * this is just a temporary storage for the input file.
	 */
	private CommandQueue m_inputFileCommands;

	public DrawingData()
	{
		m_stkList = new Vector();
		m_constraints = new Vector();
		m_markers = new Vector();
		m_textElements = new Vector();
//		m_geoElements = new Vector();
		m_commands = new CommandQueue();
		m_inputFileCommands = null;
	}
	
	public String toString()
	{
		String drawing = "BEGIN: \n" ;
		for (Stroke strk: this.m_stkList) {
			for (Segment seg: strk.getM_segList()) {
				drawing= drawing + "\n <SEGMENT>" + seg.toString() + "</SEGMENT>" ;
			}
		}
		return drawing ;
	}
	
	
	public Vector getStrokeList()
	{
		return m_stkList;
	}

	public void setStrokeList(Vector sList)
	{
		m_stkList.clear();
		m_stkList.addAll(sList);
	}
	
	public void addStroke(Stroke stroke)
	{
		// Set the ID for the stroke
//		///System.out.println("**************************************************"+m_stkCounter);
//		stroke.setM_strId(Integer.toString(m_stkCounter++));
		/////System.out.println(m_stkList.size()+"#######################");
		m_stkList.add(stroke);
//		Command comm = new DataCommand(stroke.toXMLString());
//		m_commands.addCommand(comm);
	}
	
//	public void addGeoElement(GeometryElement gEle)
//	{
//		m_geoElements.add(gEle);
//	}
	public void addMarker(Marker marker)
	{
		m_markers.add(marker);
	}
	public void addTextElement(Text text)
	{
		m_textElements.add(text);
	}
	
	public boolean removeStroke(Stroke stroke)
	{
		return m_stkList.remove(stroke);
	}
	
	public boolean removeMarker(Marker marker)
	{
		return m_markers.remove(marker);
	}
	
	public void clearMarkers(){
		m_markers.clear();
	}
	
	public void clearTextElements(){
		m_textElements.clear();
	}
	
	public void clearStrokeList(){
		m_stkList.clear();
	}
	
	public void clearConstraints(){
		m_constraints.clear();
	}
	
	public boolean removeTextElement(Text text)
	{
		return m_textElements.remove(text);
	}
	
	public boolean clear()
	{
		m_stkList.clear();
		m_constraints.clear();
		m_commands.clear();
		return true;
	}

	public void setStrokeEnabled(Stroke stroke, boolean bool)
	{
		stroke.setEnabled(bool);
	}
	
	public void setStrokeEnabled(int index, boolean bool)
	{
		Stroke stroke = (Stroke)m_stkList.get(index);
		if(stroke != null) setStrokeEnabled(stroke, bool);
	}
	
	public void setAllStrokeEnabled(boolean bool)
	{
		Iterator iter = m_stkList.iterator();
		while (iter.hasNext())
		{
			Stroke stroke = (Stroke) iter.next();
			setStrokeEnabled(stroke, bool);
		}
	}

//	public void setAllGeoElementEnabled(boolean bool)
//	{
//		Iterator iter = m_geoElements.iterator();
//		while (iter.hasNext())
//		{
//			GeometryElement ele = (GeometryElement) iter.next();
//			ele.setEnabled(bool);
//		}
//	}

	/**
	 * Method to return the last enabled stroke
	 * @param enabled: boolean
	 * @return Stroke
	 */
	public Stroke getLastStroke(boolean enabled)
	{
		Stroke stroke = null;
		for(int i=m_stkList.size()-1; i>=0  ; i--)
		{
			stroke = (Stroke)m_stkList.get(i);
			if(stroke.isEnabled()==enabled) break;
			stroke = null;
		}
		return stroke;
	}

	/**
	 * Method to return the first enabled stroke
	 * @param enabled: boolean
	 * @return Stroke
	 */
	public Stroke getFirstStroke(boolean enabled)
	{
		Stroke stroke = null;
		Iterator iter = m_stkList.iterator();
		while (iter.hasNext())
		{
			stroke = (Stroke) iter.next();
			if(stroke.isEnabled()==enabled)	break;
			stroke = null;
		}
		return stroke;
	}

	public Vector getM_constraints()
	{
		return m_constraints;
	}

	public void setM_constraints(Vector m_constraints)
	{
		this.m_constraints = m_constraints;
	}

	public Vector getConstraintsForStroke(Stroke stroke)
	{
		Vector constraints = new Vector();
		Iterator iter = stroke.getM_segList().iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			constraints.addAll(seg.getM_constraints());
		}
		return constraints;
	}
	
	public void addConstraints(Vector constraints)
	{
		m_constraints.addAll(constraints);
	}

	public void addConstraint(Constraint cons)
	{
		m_constraints.add(cons);
	}

	public void removeConstraint(Constraint cons)
	{
		m_constraints.remove(cons);
	}

	public Vector getAllAnchorPoints()
	{
		/////System.out.println("DrawingData.getAllAnchorPoints()");
		Vector apList = new Vector();
		Iterator iter = getStrokeList().iterator();
		while (iter.hasNext())
		{
			Stroke stroke = (Stroke) iter.next();
			if(stroke.getM_type() == Stroke.TYPE_NORMAL)
			{
				Iterator iterator = stroke.getM_segList().iterator();
				while (iterator.hasNext())
				{
					Segment seg = (Segment) iterator.next();
					apList.removeAll(seg.getM_impPoints());
					apList.addAll(seg.getM_impPoints());
				}
			}
		}
		return apList;
	}
	
	//While editing, we want to move all connected segments to move when highlighted segment(s) are moved.
	public Vector getAllAnchorPointsWhileEditing(Vector highlightedElements)
	{
		/////System.out.println("DrawingData.getAllAnchorPoints()");
		Vector apList = new Vector();
		Iterator iter = getStrokeList().iterator();
		while (iter.hasNext())
		{
			Stroke stroke = (Stroke) iter.next();
			if(stroke.getM_type() == Stroke.TYPE_NORMAL)
			{
				Iterator iterator = stroke.getM_segList().iterator();
				while (iterator.hasNext())
				{
					Segment seg = (Segment) iterator.next();
					if (!(highlightedElements.contains(seg)))
						apList.addAll(seg.getM_impPoints());
				}
			}
		}
		return apList;
	}
	

	public Vector getAllSegments()
	{
		Vector segList = new Vector();
		Iterator iter = getStrokeList().iterator();
		while (iter.hasNext())
		{
			Stroke stroke = (Stroke) iter.next();
			if(stroke.getM_type() == Stroke.TYPE_NORMAL) segList.addAll(stroke.getM_segList());
		}
		return segList;
	}

	public CommandQueue getM_commands()
	{
		return m_commands;
	}

	public void setM_commands(CommandQueue m_commands)
	{
		this.m_commands = m_commands;
	}

//	public Vector getM_geoElements()
//	{
//		return m_geoElements;
//	}
//
//	public void setM_geoElements(Vector elements)
//	{
//		m_geoElements = elements;
//	}

	public Vector getM_markers()
	{
		return m_markers;
	}

	public boolean isUnusedMarker()
	{
		Iterator iter = m_markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(!marker.isM_used()) return true;
		}
		return false;
	}
	
	public boolean removeUnusedMarker(){

		Iterator iter = m_markers.iterator();
		while (iter.hasNext()){
			Marker marker = (Marker) iter.next();
			if(!marker.isM_used()){
				removeMarker(marker);
				return true;
			}
		}
		return false;
	}
	public Vector getUnusedMarkers()
	{
		Vector unusedMarkers = new Vector();
		Iterator iter = m_markers.iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(!marker.isM_used()) unusedMarkers.add(marker);
		}
		return unusedMarkers;
	}
	
	public boolean isUnusedText()
	{
		Iterator iter = m_textElements.iterator();
		while (iter.hasNext())
		{
			Text text = (Text) iter.next();
			if(!text.isM_used()) return true;
		}
		return false;
	}
	
	public Vector getUnusedText()
	{
		Vector unusedText = new Vector();
		Iterator iter = m_textElements.iterator();
		while (iter.hasNext())
		{
			Text text = (Text) iter.next();
			if(!text.isM_used()) unusedText.add(text);
		}
		return unusedText;
	}
	
	public Vector getMarkers(Class classname)
	{
		Vector vec = new Vector();
		Iterator iter = getM_markers().iterator();
		while (iter.hasNext())
		{
			Marker marker = (Marker) iter.next();
			if(marker.getClass().equals(classname)) vec.add(marker);
		}
		return vec;
	}

	public Vector getM_textElements()
	{
		return m_textElements;
	}

	public CommandQueue getM_inputFileCommands()
	{
		return m_inputFileCommands;
	}

	public void resetDrawingRatio()
	{
		Vector v = getM_constraints();
		for(int i=0;i<v.size();i++)
		{
			Constraint c = (Constraint)v.get(i);
			c.resetDrawingRatio(GConstants.drawingRatio,1);
		}
	}
}
