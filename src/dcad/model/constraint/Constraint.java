package dcad.model.constraint;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.ImpPoint;
import dcad.process.beautification.MathEvaluator;
import dcad.ui.drawing.DrawingView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;

public abstract class Constraint implements Serializable
{
	public static final int SOFT = 0;
	public static final int HARD = 1;
	public static final int HIDDEN = 2;
	private boolean promoted=false;
	private boolean deleted = false;
	
	/**
	 * defines what percentage of 
	 */
	public final static double STROKE_PERCENT_GAP = 5.0;
	
	/**
	 * The minmum value of allowed gap between two points to be called as touching
	 * why 2 not 0? ? ?
	 */
	public final static int MIN_ALLOWED_CONNECT_GAP = 1;
	
	/**
	 * The maximum value of allowed gap between two points to be called as touching
	 */
	//public final static int MAX_ALLOWED_CONNECT_GAP = Prefs.getAnchorPtSize();
	public final static int MAX_ALLOWED_CONNECT_GAP = Prefs.getAnchorPtSize() * 3;
	
	
	/**
	 * The maximum value of allowed gap between two points to be called as collinear
	 */
	public final static int MAX_ALLOWED_COLLINEAR_GAP = Prefs.getAnchorPtSize();
	
	// member variables
	protected Vector points = new Vector();
	/**
	 * indicates the catagory of constraint (hard/soft)
	 */
	protected int m_category = SOFT;
	
	protected Color m_color = null;
	
	public abstract void draw(Graphics g);

	public int getM_category()
	{
		return m_category;
	}

	public void setM_category(int m_category)
	{
		this.m_category = m_category;
	}

	public Vector getPoints()
	{
		return points;
	}

	public void clearPoints()
	{
		while(points.size()!=0)
		{
			((AnchorPoint)points.get(0)).removeConstraint(this);
			points.remove(0);
		}
		points.clear();
	}
	
	public Color getDefColor()
	{
		if(m_category == SOFT) return GVariables.SOFT_CONSTRAINT_COLOR;
		else if(m_category == HARD) return GVariables.HARD_CONSTRAINT_COLOR;
		else return Color.BLACK;
	}
	
	/**
	 * this constraint ceases to exist. This method basically removes is from all the segments etc in 
	 * which this constraint had been added
	 */
	protected abstract void delete();
	
	/**
	 * Returns the Error Equation of the constraint in the string format 
	 * @param fixedPoints Vector containing the fixed points
	 * @return String
	 */
	public abstract Vector getEquation(Vector fixedPoints);

	/**
	 * Returns the Partial differential string of the error string wrt the given variable
	 * @param var variable wrt which the Partial derivative is to be found
	 * @param fixedPoints Vector containing the fixed points
	 * @return String
	 */
	public abstract Vector getPartialDifferentialString(String var, Vector fixedPoints);
	
	public void update() {}

	public void remove()
	{
		// remove the constraints from the segment
		delete();
		
		// remove this constraints from all the anchor points
		clearPoints();
		
		// remove the constraints from the Drawing data
		DrawingView dv = GMethods.getCurrentView();
		dv.getM_drawData().removeConstraint(this);
		
		// remove the constraints from the constraint solver
	}
	
	public String addPrefix()
	{
		if(isPromoted())
			return "( SOFT ) ";
		if(m_category == HARD)
			return "( HARD ) ";
		return "( UNKNOWN ) ";
	}
	
	public String toString()
	{
		return "";
	}

	public Color getColor()
	{
		if(m_color == null)
			return getDefColor();
		return m_color;
	}
	
	protected void addPoint(AnchorPoint ap)
	{
		points.add(ap);
		if(m_category==Constraint.HARD)
			ap.addConstraint(this);
	}
	
	private boolean constraintChanged=true;
	private Vector errorEquationsNodes = new Vector();
	private HashMap hashErrorPDNodes = new HashMap();

	public Vector getNodesErr()
	{
		if(constraintChanged) //Two points were snapped and the points in this constraints changed. Fetch the equations
		{
			errorEquationsNodes = new Vector();
			MathEvaluator me = new MathEvaluator();
			Vector errorEquationsStrings=getEquation(new Vector());

			Iterator iter=errorEquationsStrings.iterator();
	        while (iter.hasNext())
			{
	        	String str = (String)iter.next();
	        	if(str!="")
	        		errorEquationsNodes.add(me.getNode(str));
			}
			hashErrorPDNodes.clear();
			constraintChanged=false;
		}
		return errorEquationsNodes;
	}
	
	public Vector getPDNodes(String id)
	{
		if(hashErrorPDNodes.containsKey(id))
			return (Vector)hashErrorPDNodes.get(id);

		MathEvaluator me = new MathEvaluator();
		Vector result=new Vector();

		Vector errorPDEquationsStrings=getPartialDifferentialString(id,new Vector());
		Iterator iter=errorPDEquationsStrings.iterator();
		while(iter.hasNext())
		{
			String str = (String)iter.next();
			if(str!="")
				result.add(me.getNode(str));
		}
		hashErrorPDNodes.put(id,result);
		return result;
	}

	public void changePoint(ImpPoint ip1,ImpPoint ip2)
	{
		int count=points.size();
		for(int l=0;l<count;l++)
			if(points.elementAt(l)==ip1)
			{
				points.remove(ip1);
				points.add(l,ip2);
				constraintChanged=true;
			}
	}
	
	public boolean isConstraintSolved()
	{
		return false;
	}

	public boolean isPromoted()
	{
		return promoted;
	}

	public void setPromoted(boolean promoted)
	{
		//this.promoted=false;
		this.promoted = promoted;
	}

	protected void setDrawingRatio(double value1,double value2)
	{
		
		// GConstants.drawingRatio = value1 / value2;
		// 3-10-09
		GConstants.drawingRatio = GConstants.cmScaleDrawingRatio;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public void resetDrawingRatio(double v1,double v2)
	{
	}
	
}