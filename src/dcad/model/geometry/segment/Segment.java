package dcad.model.geometry.segment;

import ij.io.ImportDialog;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.connect.ConnectConstraint;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.constraint.connect.lineCircularCurveTangencyConstraint;
import dcad.model.constraint.connect.twoCircularCurveTangencyConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.Stroke;
import dcad.process.ProcessManager;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.ConstraintRecogManager;
import dcad.process.recognition.constraint.IndConstraintRecognizer;
import dcad.process.recognition.constraint.RelConstraintRecognizer;
import dcad.util.GConstants;
import dcad.util.GVariables;



public abstract class Segment extends GeometryElement
{
	/* Types of segments supported.
	 */
	public static final int NONE = -1;
	public static final int GENERAL = 0;
	public static final int POINT = 1;
	public static final int LINE = 2;
	public static final int CIRCLE = 3;
	public static final int ELLIPSE = 4;

	/**
	* every segment is a part of some stroke.
	*/
	protected Stroke m_parentStk = null;
	
	/**
	* to store the start of the pixel position from where the segment start in the raw stroke.
	*/
	protected int m_rawStartIdx = -1;

	/**
	* to store the start of the pixel position from where the segment end in the raw stroke.
	*/
	protected int m_rawEndIdx = -1;
	
	/**
	 * storing the group to which the segment belongs to
	 */
	protected GeometryElement m_group;
	
	/**
	 * what is the category of this segment as detected by the recognition algorithm
	 */ 
	protected int m_type = Segment.NONE;
	
	/**
	 * the length of the raw segment
	 */
	protected double m_rawLength = 0;
	
	/**
	 * the length of the segment
	 */
	protected double m_length = 0;
	
	/**
	 * To store all the anchor points for this segment.
	 */
	protected Vector m_impPoints = null;
	
	/**
	 * To store the constraints related to this segment
	 */
	protected Vector m_constraints = null;
	
	/**
	 * Store the shape object
	 */
	protected Shape m_shape = null;
	
	public Segment()
	{
		//setM_color(GVariables.RECOGNIZED_COLOR);
//		setM_points(points);
		m_impPoints = new Vector();
		m_constraints = new Vector();
	}
	
	public Segment( Stroke stk,int rawStartIndex, int rawEndIndex )
	{
		m_impPoints = new Vector();
		m_constraints = new Vector();

		//Set the start and end index first and then set the stroke. The order is important.
		//Order important because length computed in setM_parentStk using the class variables
		//for start and end index
		
		m_rawStartIdx = rawStartIndex;
		m_rawEndIdx = rawEndIndex;
		setM_parentStk(stk);
	}
	
	public Vector getRawPoints()
	{
		Vector points = new Vector();
		if(m_parentStk !=null)
		{
			points.addAll(m_parentStk.getM_ptList().subList(getM_rawStartIdx(), getM_rawEndIdx()+1));
		}
		return points;
	}
	
	public Stroke getM_parentStk()
	{
		return m_parentStk;
	}

	/**
	 * Since every segment has a parent, set the parent of this segment to the parameter stroke.
	 * Also calculate the set the raw length.
	 * @param stk
	 */
	public void setM_parentStk(Stroke stk)
	{
		setM_parent(stk);
		m_parentStk = stk;
		if((m_rawEndIdx >=0 ) && (m_rawStartIdx >=0 ))
		{
			m_rawLength = m_parentStk.getLength(m_rawStartIdx, m_rawEndIdx);
		}
	}

	public void setM_type(int m_type)
	{
		this.m_type = m_type;
	}

	public int getM_type () 
	{	
		return this.m_type ;
	}
	public GeometryElement getM_group()
	{
		return m_group;
	}

	public void setM_group(GeometryElement m_group)
	{
		this.m_group = m_group;
	}

	public int getM_rawEndIdx()
	{
		return m_rawEndIdx;
	}

	public void setM_rawEndIdx(int endIdx)
	{
		m_rawEndIdx = endIdx;
		if((m_rawEndIdx >=0 ) && (m_rawStartIdx >=0 ) && (m_parentStk!=null))
		{
			m_rawLength = m_parentStk.getLength(m_rawStartIdx, m_rawEndIdx);
		}
	}

	public int getM_rawStartIdx()
	{
		return m_rawStartIdx;
	}

	public void setM_rawStartIdx(int startIdx)
	{
		m_rawStartIdx = startIdx;
		if((m_rawEndIdx >=0 ) && (m_rawStartIdx >=0 ) && (m_parentStk!=null))
		{
			m_rawLength = m_parentStk.getLength(m_rawStartIdx, m_rawEndIdx);
		}
	}

	/**
	 * raw length of the stroke
	 * @return raw stroke length 
	 */
	public double getM_rawLength()
	{
		return m_rawLength;
	}

	protected void setM_rawLength(double m_length)
	{
		this.m_rawLength = m_length;
	}

	public Vector getM_impPoints()
	{
		return m_impPoints;
	}

	public void setM_impPoints(Vector points)
	{
		m_impPoints = points;
	}
	
	public void drawAnchorPts(Graphics gc)
	{
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			GeometryElement ap = (GeometryElement) iter.next();
			ap.draw(gc);
		}
	}
	
	public void drawConstraints(Graphics gc)
	{
		Iterator iter = getM_constraints().iterator();
		while (iter.hasNext())
		{
			Constraint cons = (Constraint) iter.next();
			
			if(cons.isDeleted())
				continue;
			if(this instanceof SegLine && ((SegLine)this).getM_length() < 10)
				continue;
			if(this instanceof SegCircleCurve && ((SegCircleCurve)this).getM_radius() < 10)
				continue;
			
			switch(Prefs.getShowConstraints())
			{
			case GConstants.SHOW_ALL_CONSTRAINTS:
				cons.draw(gc);
				break;
			case GConstants.SHOW_SOFT_CONSTRAINTS:
				if(cons.getM_category()==Constraint.SOFT) cons.draw(gc);
				break;
			case GConstants.SHOW_HARD_CONSTRAINTS:
				if(cons.getM_category()==Constraint.HARD) cons.draw(gc);
				break;
			case GConstants.SHOW_ALL_CONSTRAINTS_ON_HL:
				if(isHighlighted()) cons.draw(gc);
				break;
			case GConstants.SHOW_SOFT_CONSTRAINTS_ON_HL:
				if(isHighlighted() && (cons.getM_category()==Constraint.SOFT)) cons.draw(gc);
				break;
			case GConstants.SHOW_HARD_CONSTRAINTS_ON_HL:
				if(isHighlighted() && (cons.getM_category()==Constraint.HARD)) cons.draw(gc);
				break;
			case GConstants.SHOW_SOFT_ON_HL_AND_HARD_CONSTRAINTS:
				if((isHighlighted() && (cons.getM_category()==Constraint.SOFT))||(cons.getM_category()==Constraint.HARD)) cons.draw(gc);
				break;
			case GConstants.SHOW_NO_CONSTRAINTS:
				break;
			default:
				cons.draw(gc);
				break;
			}
		}
	}
	
	/**
	 * Add a given 2d point to as the anchor point of this segment. Creates a new Anchorpt (Imppt) object and stores that
	 * @param pt
	 * @return
	 */
	protected AnchorPoint addAnchorPoint(Point2D pt)
	{
		// calculate anchor points for this segment
		Vector tempV = new Vector();
		tempV.add(this);
		AnchorPoint ap = new AnchorPoint(pt, tempV);
		m_impPoints.add(ap);
		return ap;
	}
	
	/**
	 * Replace first AP by the second
	 * @param ap1
	 * @param ap2
	 */
	public void changeAnchorPoint(AnchorPoint ap1, AnchorPoint ap2)
	{
		for(int len=0;len<m_impPoints.size();len++) {
			if(m_impPoints.elementAt(len)==ap1)
			{
				m_impPoints.remove(ap1);
				m_impPoints.add(len,ap2);
			}
		}
	}
	
	public double getM_length()
	{
		return m_length;
	}

	public void setM_length(double m_length)
	{
		this.m_length = m_length;
	}

	public Vector getM_constraints()
	{
		return m_constraints;
	}

	public void setM_constraints(Vector m_constraints)
	{
		this.m_constraints = m_constraints;
	}
	
	public void addConstraint(Constraint cons)
	{
		m_constraints.add(cons);
	}

	public boolean removeConstraint(Constraint cons)
	{
		return m_constraints.remove(cons);
	
	}
	
	// 22-01-10
	public void removeAllConstraints(){
		m_constraints.removeAllElements();
		if(m_constraints.size() == 0);
			///System.out.println("Vector is null");
	}
	

	public Shape getM_shape()
	{
		return m_shape;
	}

	protected void setM_shape(Shape m_shape)
	{
		this.m_shape = m_shape;
	}

	public String toString()
	{
		String s = this.m_label ;
		return s;
		//return ("*********************m_points were removed from the segment... This msg. is being displayed from tostring function of segment************************");
		//return m_points.get(0).toString()+"----"+m_points.get(m_points.size()-1).toString();
	}
	
	public void clearConstraints(int catagory)
	{
//		///System.out.println("Segment.clearConstraints()"+getM_strId());
		// remove all the constarints
		//ISHWAR Changed "SOFT" to catagory
		Vector constraints = getConstraintsByCatagory(catagory);
		Iterator iter = constraints.iterator();
		while (iter.hasNext())
		{
			Constraint cons = (Constraint) iter.next();
//			///System.out.println(cons);
			
			// remove only constraints of the given catagory
			if(cons.getM_category() == catagory)
			{
				cons.remove();
//				///System.out.println("Constraint Removed: "+cons);
			}
			iter.remove();
		}
	}
	
	public Vector getConstraintsByCatagory(int catagory)
	{
		Vector cons = new Vector();
		Iterator iter = m_constraints.iterator();
		while (iter.hasNext())
		{
			Constraint constraint = (Constraint) iter.next();
			if(constraint.getM_category() == catagory)
			{
				cons.add(constraint);
			}
		}
		return cons;
	}
	
	/**
	 * returns the intersection points of this segment with this other segment
	 * @param seg the other segment with which the intersections of this segments will be found
	 * @return Vector containing the intersection points.
	 */
	public abstract Vector intersects(Segment seg);
	
	public abstract Vector touches(Segment seg);
	
	public abstract Point2D getNearestPointOnSeg(Point2D pt);
	
	public abstract int getSegmentPt(AnchorPoint ip);
	
	public abstract AnchorPoint getSegStart();
	
	public abstract AnchorPoint getSegEnd();
	
	/**
	 * 
	 * @param ap moved IP
	 * @param pt Old point, before moving
	 */
	public abstract void movePt(ImpPoint ap, Point2D pt);
	public abstract void movePt4Constraints();
//	public abstract void movePt();
	
	public double getDistance(Point2D pt)
	{
		Point2D point = getNearestPointOnSeg(pt);
		if(point != null) return point.distance(pt);
		else return Double.MAX_VALUE;
	}
	
	protected Vector reverseRelOrder(Vector vec)
	{
		Vector newVec = new Vector();
		int len = vec.size();
		if((len%2) == 0)
		{
			for (int i = 0; i < len; i=i+2)
			{
				newVec.add(vec.get(i+1));
				newVec.add(vec.get(i));
			}
		}
		return newVec;
	}
	
	/**
	 * Get all the relative constraints of the type specified which contain this segment
	 * @param seg
	 * @param classname
	 * @return
	 */
	public Vector getConstraintWithSeg(Class classname)
	{
		Segment seg = this ;
		Vector matches = new Vector();
		Iterator iterator = m_constraints.iterator();
		while (iterator.hasNext())
		{
			Constraint cons = (Constraint) iterator.next();
			if(cons instanceof RelativeConstraint)
			{
				RelativeConstraint relCons = (RelativeConstraint)cons;
				if(((relCons.getM_seg1().equals(seg))||(relCons.getM_seg2().equals(seg))) && (classname.equals(relCons.getClass())))
				{
					matches.add(cons);
				}
			}
		}
		return matches;
	}

	public Vector getConstraintByType(Class classname)
	{
		return constraintsHelper.getConstraintsByType(m_constraints,classname);
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof Segment)
		return  getM_strId().equals(((Segment)obj).getM_strId());
		return false;
	}
	
	public void setSelected(boolean selected)
	{
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			ip.setSelected(selected);
		}
		super.setSelected(selected);
	}
	
	public void setHighlighted(boolean highlighted)
	{
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			ip.setHighlighted(highlighted);
		}
		super.setHighlighted(highlighted);
	}
	
	/**
	 * Does no actual drawing or anything?
	 */
	public void draw(Graphics g)
	{
		super.draw(g); //does nothing?
		
		Stroke stk = (Stroke)m_parent;
		if((stk != null) && (stk.getM_type() == Stroke.TYPE_NORMAL))
		{
			// draw the anchor points
			drawAnchorPts(g);
			
			// draw the constraints
			drawConstraints(g);
		}
	}
	
	public void setFixed(boolean fixed)
	{
		super.setFixed(fixed);
		Iterator iter = getM_impPoints().iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			ip.setFixed(fixed);
		}
	}
	
	public Vector findConstraints(Vector segments)
	{
		Vector cons = new Vector();

		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		ConstraintRecogManager consRecogMan = recogMan.getConstraintRecogManager();
		IndConstraintRecognizer indConsRecog = consRecogMan.getIndConsRecog();
		RelConstraintRecognizer relConsRecog = consRecogMan.getRelConsRecog();
		
		// find and all the independent constraints for this segment
		Vector indCons = indConsRecog.recognizeConstraints(this);
		if(indCons != null) cons.addAll(indCons);
		
		Iterator iter = segments.iterator();
		while (iter.hasNext())
		{
			Segment seg = (Segment) iter.next();
			cons.addAll(findConstraints(seg, relConsRecog));
		}
		return cons;
	}
	
	public Vector findConstraints(Segment seg, RelConstraintRecognizer relConsRecog)
	{
//		///System.out.println("Segment.findConstraints()");
		Vector cons = new Vector();
		if(!seg.equals(this))
		{
			Vector relCons = relConsRecog.recognizeConstraints(this, seg);
			if(relCons != null) cons.addAll(relCons);
		}		
		return cons;
	}
	
	public Vector findClosestSeg(Vector closeSegments)
	{
		Vector closePtDiff = new Vector();
		double closeDist = Double.MAX_VALUE;
		if(closeSegments!=null)
		{
			Iterator iter = closeSegments.iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				if(!seg.equals(this))
				{
					// the new segment is not the same as this one
					Vector constraints = seg.getConstraintWithSeg( IntersectionConstraint.class);
					constraints.addAll(seg.getConstraintWithSeg( lineCircularCurveTangencyConstraint.class));
					constraints.addAll(seg.getConstraintWithSeg( twoCircularCurveTangencyConstraint.class));
					Iterator iter2 = constraints.iterator();
					while (iter2.hasNext())
					{
						ConnectConstraint cons = (ConnectConstraint) iter2.next();
						Point2D nearPt1 = cons.getM_contactPt1();
						Point2D nearPt2 = cons.getM_contactPt2();
						
						//if in this constraint, this segment is the second segment then invert
						if(cons.getM_seg2().equals(this) || cons.getM_seg1().equals(this))
						{
							nearPt1 = cons.getM_contactPt2();
							nearPt2 = cons.getM_contactPt1();
						}
						double dist = nearPt1.distance(nearPt2);
						if((dist <= (Constraint.MAX_ALLOWED_CONNECT_GAP))&&(dist < closeDist))
						{
							closeDist = dist;
							closePtDiff.add(nearPt1); 
							closePtDiff.add(nearPt2); 
						}
					}
				}
			}
		}
		return closePtDiff;
	}

	
	/**
	 * clear ALL the constraints of this segment (SOFT AND HARD) and all the imp points
	 */
	
	public void delete()
	{
		
		clearConstraints(Constraint.SOFT);
		clearConstraints(Constraint.HARD);
		for(int l=0;l<m_impPoints.size();l++)
		{
			ImpPoint ip =(ImpPoint)m_impPoints.elementAt(l); 
			ip.unSelect();//.setHighlighted(false);
			ip.removeParent(this);

			//Added on 9-5-2008 This removes the point constraints that are not added to the segemnts.
			if(ip.getAllParents().size()==0)
				((AnchorPoint)ip).deleteConstraints();
		}
		
		m_impPoints.clear();
		m_parentStk.removeSegment(this);
		super.delete();
	}
	
	/**
	 * Updates the constraints related to the Segment based on the new values of the segments
	 * Currently it updates only the hard constraints
	 */
	protected void updateConstarints()
	{
		Iterator iter = m_constraints.iterator();
		while (iter.hasNext())
		{
			Constraint cons = (Constraint) iter.next();
			if(cons.getM_category() == Constraint.HARD)
			{
				cons.update();
			}
		}
	}
	
	
	public void changePoint(ImpPoint ip1,ImpPoint ip2)
	{
		ip2.addParent(this);
		ip1.removeParent(this);
		for(;((AnchorPoint)ip1).getConstraints().size()!=0;)
		{
			Constraint cons=(Constraint)(((AnchorPoint)ip1).getConstraints().elementAt(0));
			cons.changePoint(ip1,ip2);
			((AnchorPoint)ip2).addConstraint(cons);
			((AnchorPoint)ip1).removeConstraint(cons);
		}

		changeAnchorPoint((AnchorPoint)ip1,(AnchorPoint)ip2);

	}

	public abstract void changePoint4Segment(ImpPoint ip1, ImpPoint ip2);
}