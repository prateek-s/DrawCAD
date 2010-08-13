package dcad.model.geometry.segment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;

public class SegPoint extends Segment
{
	private final static int DEF_POINT_SIZE = 1;
	private AnchorPoint m_pt = null;

	public SegPoint(Vector points)
	{
		super();
		m_type = Segment.POINT;
		if(points.size() >= 1)
		{
			// first Point is the point.
			m_pt = addAnchorPoint((Point2D)points.get(0));
			setM_shape(new Line2D.Double(m_pt.getX(), m_pt.getY(), m_pt.getX(), m_pt.getY()));
			
			setM_length(1);
			// calculate anchor points for this segment
		}
	}
	
	public void draw(Graphics g)
	{
/*		if(getM_impPoints().size() == 0)
		{
			Point2D pt = (Point2D)m_points.get(0);
			addAnchorPoint(pt);
		}
*/		if(!isEnabled()) return;
		super.draw(g);

		Graphics2D g2d = (Graphics2D)g;
		// set the color of the graphics to the color of the segment
		Color prevColor = g2d.getColor();
		g2d.setColor(getM_color());
		// There should be only on point in the vector. If more than one points are there then return

		if(m_shape != null)
		{
			// to draw a point, draw a line starting and ending at the same position.
			g2d.draw(m_shape);
			g2d.drawArc((int)((Line2D)m_shape).getX1() - DEF_POINT_SIZE, (int)((Line2D)m_shape).getY1() - DEF_POINT_SIZE, DEF_POINT_SIZE*2, DEF_POINT_SIZE*2, 0, 360);
		}

		// reset the graphics color back
		g2d.setColor(prevColor);
	}
	
	public Vector intersects(Segment seg)
	{
		Vector intersects = new Vector();
		Line2D thisSegPt = (Line2D)this.getM_shape();
		if(seg instanceof SegPoint)
		{
			Line2D otherSegPt = (Line2D)seg.getM_shape();

			// check if both points point to same location 
			if(containsPt(otherSegPt.getP1()))
			{
				intersects.add(thisSegPt.getP1());
			}
		}
		else
		{
			// for all other types of segments, similar method would be defined in those segments. So use those methods. 
			intersects.addAll(seg.intersects(this));
		}
		
		return intersects;
	}
	
	public Vector touches(Segment seg)
	{
		Vector touches = new Vector();
		Line2D thisSegPt = (Line2D)this.getM_shape();
		if(seg instanceof SegPoint)
		{
			Line2D otherSegPt = (Line2D)seg.getM_shape();
			
			// check if both points are near each other 
			if(thisSegPt.getP1().distance(otherSegPt.getP1()) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
			{
				touches.add(thisSegPt.getP1());
				touches.add(otherSegPt.getP1());
			}
		}
		else
		{
			// for all other types of segments, similar method would be defined in those segments. So use those methods. 
			touches.addAll(reverseRelOrder(seg.touches(this)));
		}
		
		return touches;
	}

	public Point2D getNearestPointOnSeg(Point2D pt)
	{
		Line2D thisSegPt = (Line2D)this.getM_shape();
		return thisSegPt.getP1();
		
/*		Line2D thisSegPt = (Line2D)this.getM_shape();
		double dist = thisSegPt.ptSegDist(pt);
		if((dist >= Constraint.MIN_ALLOWED_CONNECT_GAP) && (dist <= Constraint.MAX_ALLOWED_CONNECT_GAP)) return thisSegPt.getP1();
		return null;
*/	}

	public void movePt(ImpPoint ap, Point2D pt)
	{
		((Line2D)m_shape).setLine(pt, pt);	
//		updateConstarints();
	}
	public void movePt4Constraints()
	{
//		Point2D pt=(Point2D)getM_impPoints().get(0);
//		((Line2D)m_shape).setLine(pt,pt);	
	}


	public void move(int x1, int y1, int x2, int y2)
	{
		boolean canMove = true;
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			if(ip.isFixed())
			{
				canMove = false;
				break;
			}
		}
		// change the line
		if(canMove)
		{
			m_pt.setM_point(x2, y2);
			((Line2D)m_shape).setLine(x2, y2, x2, y2);
		}
		updateConstarints();
	}

	public boolean containsPt(double x, double y)
	{
		Line2D thisSegPt = (Line2D)this.getM_shape();
		if(thisSegPt.ptSegDist(x, y) < Constraint.MIN_ALLOWED_CONNECT_GAP) return true;
		return false;
	}

	public AnchorPoint getM_pt()
	{
		return m_pt;
	}

	public void setM_pt(AnchorPoint m_pt)
	{
		this.m_pt = m_pt;
	}

	public int getSegmentPt(AnchorPoint ip)
	{
		if(ip.equals(getM_pt()))
		{
			return getM_rawStartIdx();
		}
		return -1;
	}

	public AnchorPoint getSegEnd()
	{
		return getM_pt();
	}

	public AnchorPoint getSegStart()
	{
		return getM_pt();
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void changePoint(ImpPoint ip1, ImpPoint ip2)
	{
		super.changePoint(ip1, ip2);
		changePoint4Segment(ip1,ip2);
	}
	
	public void changePoint4Segment(ImpPoint ip1,ImpPoint ip2)
	{
		m_pt=(AnchorPoint)ip2;
	}


	
}
