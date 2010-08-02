package dcad.model.geometry.segment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.length.LineMidPointConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.Stroke;
import dcad.model.marker.Marker;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.util.Maths;
import dcad.ui.main.*;
import dcad.ui.drawing.*;
public class SegLine extends Segment
{
	/**
	 * Stores the angle which the segment makes in the plane.
	 */
	private double m_angle = 0.0;
	private AnchorPoint m_start = null;
	private AnchorPoint m_end = null;

	private AnchorPoint m_middle = null;
	public AnchorPoint getM_middle()
	{
		return m_middle;
	}
	
	public SegLine(Vector points)
	{
		super();
		m_type = Segment.LINE;
		if (points.size() >= 2)
		{
			// first 2 are the end points of the line.
			m_start = addAnchorPoint((Point2D) points.get(0));
			m_end = addAnchorPoint((Point2D) points.get(1));

			//Do not do "addAnchorPoint" here. It will increase no. of anchorpoints in the system
			m_middle=new AnchorPoint(null,null);
			
			m_shape=new Line2D.Double(m_start.getM_point(), m_end.getM_point());
			setDetails();
			
			//TODO middle point
			//addConstraint(new LineMidPointConstraint(this,Constraint.HARD,false));
		}
	}
	
	public SegLine(AnchorPoint start, AnchorPoint end, Stroke stk,int rawStartIndex, int rawEndIndex)
	{
		super(stk,rawStartIndex,rawEndIndex);
		m_type = Segment.LINE;
		
		start.addParent(this);
		end.addParent(this);

		m_start = start;
		m_end = end;
		m_middle=new AnchorPoint(null,null);

		m_impPoints.add(m_start);
		m_impPoints.add(m_end);
		
		m_shape = new Line2D.Double(m_start.getM_point(),m_end.getM_point());
		setDetails();
	}

	private void setDetails()
	{
		//TODO middle point
		m_middle.getM_point().setLocation(  (m_start.getX()+m_end.getX())/2  ,  (m_start.getY()+m_end.getY())/2  );
		setM_length(m_start.distance(m_end));
		m_angle=Maths.angleInDegrees(m_start.getM_point(), m_end.getM_point());
		
		
		//Added on 13-4-2008
		//This was added to remove those strange lines shown at Sir's machine.
		//Those were due to the perpendicularity markers. See day diary of 13th April
		if(m_length<10)
		{
/*			Vector v = this.getM_constraints();
			for(;v.size()!=0;)
				((Constraint)v.get(0)).remove();*/
		}
		
	}

	public void draw(Graphics g)
	{
		if (!isEnabled())
			return;
		if(m_length<10)
			return;
		super.draw(g);

		Graphics2D g2d = (Graphics2D) g;
		// set the color of the graphics to the color of the segment
		Color prevColor = g2d.getColor();
		
		
		if(m_parentStk.getM_type()==Stroke.TYPE_NORMAL)
		{
			int size = m_middle.getM_size();
			Point2D midPoint = m_middle.getM_point();
			//g2d.setColor(m_middle.getM_color());
			g2d.setColor(Color.ORANGE);
			g2d.fillArc((int)midPoint.getX()-size+1, (int)midPoint.getY()-size+1, size*2, size*2, 0, 360);
		}
		
		//added on 28-05-10
		if(m_parentStk.getM_type()==Stroke.TYPE_MARKER){
			g2d.setColor(GVariables.MARKER_COLOR);
			DrawingView dv = MainWindow.getDv();
			Point pt = dv.getM_mousePos();
			Marker marker = null;
			Vector segList = m_parentStk.getM_segList();
			Iterator iter = segList.iterator();
			if ((marker = dv.isPtOnAnyMarker(pt)) != null){
				while(iter.hasNext()){
				SegLine seg = (SegLine)iter.next();	
				if(seg.isSelected()){
					g2d.setColor(GVariables.SELECTED_COLOR);
				}
				else if(seg.containsPt(pt)){
						g2d.setColor(getM_color());
					}
				}	
			}
			else{
				while(iter.hasNext()){
					SegLine seg = (SegLine)iter.next();	
					if(seg.isSelected()){
						g2d.setColor(GVariables.SELECTED_COLOR);
					}
				}
			}
		}
		else{
		g2d.setColor(getM_color());
		}
		g2d.draw(m_shape);

		boolean anythingHighlighted = isHighlighted();
		if (!anythingHighlighted)
			for (int i = 0; i < m_impPoints.size(); i++)
				if (((ImpPoint) m_impPoints.get(i)).isHighlighted())
					anythingHighlighted = true;
		if (anythingHighlighted)
		{
			BasicStroke prevStroke = (BasicStroke) g2d.getStroke();
			g2d.setColor(GVariables.DRAWING_ASSIST_COLOR);
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(),
					prevStroke.getLineJoin(), prevStroke.getMiterLimit(),
					new float[] { 4, 4, 8, 4 }, prevStroke.getDashPhase()));

			// ISHWAR - for showing the line length while dragging
			// g2d.drawString(String.valueOf(getM_length()),
			// (float)m_start.getX(),(float)m_start.getY());
			g2d.setStroke(prevStroke);
		}
		// reset the graphics color back
		g2d.setColor(prevColor);
	}

	public double getM_angle()
	{
		return m_angle;
	}

	public Vector intersects(Segment seg)
	{
		// System.out.println("SegLine.intersects()");
		Vector intersects = new Vector();
		Line2D line1 = (Line2D) this.getM_shape();
		if (seg instanceof SegPoint)
		{
			Line2D segPt = (Line2D) seg.getM_shape();
			// check if the point intersects the line
			if (line1.intersectsLine(segPt))
			{
				Point2D interPt = GMethods.intersection(line1, segPt);
				if (interPt != null)
				{
					intersects.add(segPt.getP1());
					// System.out.println("Point at line intersect at: "+segPt);
				}
			}
		} else if (seg instanceof SegLine)
		{
			// other segment is a line .. find the intersection
			Line2D line2 = (Line2D) seg.getM_shape();
			if (line1.intersectsLine(line2))
			{
				// line segments intersect, find the intersection point.
				Point2D interPt = GMethods.intersection(line1, line2);
				if (interPt != null)
				{
					intersects.add(interPt);
					// System.out.println("Lines Intersect at: "+interPt);
				}
			}
		} else
		{
			// for all other types of segments, similar method would be defined
			// in those segments. So use those methods.
			intersects.addAll(seg.intersects(this));
		}
		return intersects;
	}

	public Vector touches(Segment seg)
	{
		// System.out.println("SegLine.touches()");
		Vector touches = new Vector();
		Line2D line1 = (Line2D) this.getM_shape();
		if (seg instanceof SegPoint)
		{
			Line2D segPt = (Line2D) seg.getM_shape();

			// find the nearest distance of p1 of segPt with the end points of
			// line 1
			Point2D touchPt = this.getNearestPointOnSeg(segPt.getP1());
			if ((touchPt != null)
					&& (touchPt.distance(segPt.getP1()) <= Constraint.MAX_ALLOWED_CONNECT_GAP))
			{
				// System.out.println("Line Touches point at: "+touchPt);
				touches.add(touchPt);
				touches.add(segPt.getP1());
			}
		}

		else if (seg instanceof SegLine)
		{
			Line2D line2 = (Line2D) seg.getM_shape();

			// create two new lines with line1 and line2 extended from both
			// sides by predefined number of pixels (MAX_ALLOWED_CONNECT_GAP)
			// extrapolate the two lines
			Point2D pt2 = GMethods.extrapolate(line1.getP1(), line1.getP2(),
					Constraint.MAX_ALLOWED_CONNECT_GAP);
			Point2D pt1 = GMethods.extrapolate(line1.getP2(), line1.getP1(),
					Constraint.MAX_ALLOWED_CONNECT_GAP);
			Line2D newLine1 = new Line2D.Double(pt1, pt2);

			pt2 = GMethods.extrapolate(line2.getP1(), line2.getP2(),
					Constraint.MAX_ALLOWED_CONNECT_GAP);
			pt1 = GMethods.extrapolate(line2.getP2(), line2.getP1(),
					Constraint.MAX_ALLOWED_CONNECT_GAP);
			Line2D newLine2 = new Line2D.Double(pt1, pt2);
			if (newLine1.intersectsLine(newLine2))
			{
				Point2D interPt = GMethods.intersection(newLine1, newLine2);
				if (interPt != null)
				{
					double d1 = this.getNearestPointOnSeg(interPt).distance(interPt);
					double d2 = seg.getNearestPointOnSeg(interPt).distance(interPt);
					if (d1 < 1)
					{
						touches.add(interPt);
						// line1 contains the intersection point, from the end
						// points of line2 find the closer point to the
						// intersection point
						if (line2.getP1().distance(interPt) <= line2.getP2().distance(interPt))
						{
							touches.add(line2.getP1());
						} else
						{
							touches.add(line2.getP2());
						}
					} else if (d2 < 1)
					{
						// line1 contains the intersection point, from the end
						// points of line2 find the closer point to the
						// intersection point
						if (line1.getP1().distance(interPt) <= line1.getP2().distance(interPt))
						{
							touches.add(line1.getP1());
						} else
						{
							touches.add(line1.getP2());
						}
						touches.add(interPt);
					} else
					{
						// line1 contains the intersection point, from the end
						// points of line2 find the closer point to the
						// intersection point
						if (line1.getP1().distance(interPt) <= line1.getP2().distance(interPt))
						{
							touches.add(line1.getP1());
						} else
						{
							touches.add(line1.getP2());
						}

						// line1 constains the intersection point, from the end
						// points of line2 find the closer point to the
						// intersection point
						if (line2.getP1().distance(interPt) <= line2.getP2().distance(interPt))
						{
							touches.add(line2.getP1());
						} else
						{
							touches.add(line2.getP2());
						}
					}
				}
			}

			/*
			 * boolean l1p1done = false; boolean l1p2done = false;
			 *  // check if the endpoints of line 2 are near line1 (NOTE: end
			 * points of line 2 can only be the nearest point to line 2) // this
			 * is because the lines may be JUST intersecting. // find the
			 * nearest distance of p1 of line 1 with the end points of line 2
			 * Point2D touchPt = this.getNearestPointOnSeg(line2.getP1());
			 * if((touchPt != null) && (touchPt.distance(line2.getP1()) <=
			 * Constraint.MAX_ALLOWED_CONNECT_GAP)) { System.out.println("Line
			 * Touches point at: "+touchPt); l1p1done =
			 * touchPt.equals(line1.getP1()); l1p2done =
			 * touchPt.equals(line1.getP2()); touches.add(touchPt);
			 * touches.add(line2.getP1()); } touchPt =
			 * this.getNearestPointOnSeg(line2.getP2()); if((touchPt != null) &&
			 * (touchPt.distance(line2.getP2()) <=
			 * Constraint.MAX_ALLOWED_CONNECT_GAP)) { System.out.println("Line
			 * Touches point at: "+touchPt); l1p1done =
			 * touchPt.equals(line1.getP1()); l1p2done =
			 * touchPt.equals(line1.getP2()); touches.add(touchPt);
			 * touches.add(line2.getP2()); }
			 * 
			 * if(!l1p1done) { touchPt =
			 * seg.getNearestPointOnSeg(line1.getP1()); if((touchPt != null) &&
			 * (touchPt.distance(line1.getP1()) <=
			 * Constraint.MAX_ALLOWED_CONNECT_GAP)) { System.out.println("Line
			 * Touches point at: "+touchPt); touches.add(line1.getP1());
			 * touches.add(touchPt); } } if(!l1p2done) { touchPt =
			 * seg.getNearestPointOnSeg(line1.getP2()); if((touchPt != null) &&
			 * (touchPt.distance(line1.getP2()) <=
			 * Constraint.MAX_ALLOWED_CONNECT_GAP)) { System.out.println("Line
			 * Touches point at: "+touchPt); touches.add(line1.getP2());
			 * touches.add(touchPt); } }
			 */} else
		{
			// circular arc already has a method for intersection with a SegLine
			touches.addAll(reverseRelOrder(seg.touches(this)));
		}
		return touches;
	}

	public boolean containsPt(double x, double y)
	{
		Line2D shape = (Line2D) this.getM_shape();
		if (shape.ptSegDist(x, y) < Constraint.MIN_ALLOWED_CONNECT_GAP)
			return true;
		return false;
	}

	public Point2D getNearestPointOnSeg(Point2D p)
	{
		// System.out.println("SegLine.getNearestPointOnSeg()");
		Line2D line = (Line2D) m_shape;

		double p1Dist = line.getP1().distance(p);
		double p2Dist = line.getP2().distance(p);

		// both end points of line 1 are near p1 of line2, find whichone is
		// closer
		if (p1Dist <= p2Dist)
		{
			// P1 point of line 1 touches P1 of line 2
			if (p1Dist < Constraint.MAX_ALLOWED_CONNECT_GAP)
				return line.getP1();
		} else
		{
			// P2 point of line 1 touches P1 of line 2
			if (p2Dist < Constraint.MAX_ALLOWED_CONNECT_GAP)
				return line.getP2();
		}
		// none of the end points of line1 are near P2 of Line 2. Just find the
		// nearest point.
		// But this case is not possible in case of lines as one of the end
		// points has to be CLOSER
		Point2D nearPt = GMethods.nearestPointOnLineFromPoint(line, p);
		return nearPt;
	}

	public void movePt(ImpPoint ap, Point2D pt)
	{
		//Check if the mid-point of the line was moved. If so, call the move function of the line. Else just update the line.
		//TODO middle point
		/*if(m_middle==ap)
		{
			int oldMidPointX=(int)(m_start.getX()+m_end.getX())/2;
			int oldMidPointY=(int)(m_start.getY()+m_end.getY())/2;
			move(oldMidPointX,oldMidPointY,ap.getX(),ap.getY());
		}
		else*/
		{
			((Line2D) m_shape).setLine(m_start.getM_point(), m_end.getM_point());
			setDetails();
		}
	}

	public void movePt4Constraints()
	{
		((Line2D) m_shape).setLine(m_start.getM_point(), m_end.getM_point());
		setDetails();
	}
	
	public void move(int x1, int y1, int x2, int y2)
	{
		boolean canMove = true;
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			if (ip.isFixed())
			{
				canMove = false;
				break;
			}
		}

		if (canMove)
			moveFree(x1, y1, x2, y2);
		else
		{
			if ((m_start.isFixed()) && (!m_end.isFixed()))
				moveWithOnePtFixed(m_start, m_end, x2, y2);
			else if ((!m_start.isFixed()) && (m_end.isFixed()))
				moveWithOnePtFixed(m_end, m_start, x2, y2);
		}
		updateConstarints();
	}

	private void moveWithOnePtFixed(AnchorPoint start, AnchorPoint end, int x2, int y2)
	{
		double dist = start.distance(x2, y2);
		Point2D pt = null;
		if (dist >= m_length)
			pt = GMethods.interpolate(start.getM_point(), new Point2D.Double(x2, y2), m_length);
		else
			pt = GMethods.extrapolate(start.getM_point(), new Point2D.Double(x2, y2), m_length	- dist);
		end.setM_point(pt);
		((Line2D) m_shape).setLine(start.getM_point(), end.getM_point());
		setDetails();
	}

	private void moveFree(int x1, int y1, int x2, int y2)
	{
		AffineTransform tx = new AffineTransform();
		tx.setToTranslation(x2 - x1, y2 - y1);
		tx.transform(m_start.getM_point(), m_start.getM_point());
		tx.transform(m_end.getM_point(), m_end.getM_point());

		//TODO middle point
		//tx.transform(m_middle.getM_point(), m_middle.getM_point());
		
		((Line2D) m_shape).setLine(m_start.getM_point(), m_end.getM_point());
		setDetails();
	}

	public AnchorPoint getM_end()
	{
		return m_end;
	}

	public AnchorPoint getM_start()
	{
		return m_start;
	}

	public int getSegmentPt(AnchorPoint ip)
	{
		if (ip.equals(getM_start()))
			return getM_rawStartIdx();
		else if (ip.equals(getM_end()))
			return getM_rawEndIdx();
		return -1;
	}

	public AnchorPoint getSegEnd()
	{
		return getM_end();
	}

	public AnchorPoint getSegStart()
	{
		return getM_start();
	}

	public GeometryElement copy()
	{
		return null;
	}

	public void changePoint(ImpPoint ip1, ImpPoint ip2)
	{
		super.changePoint(ip1, ip2);
		changePoint4Segment(ip1,ip2);
	}
	
	public void changePoint4Segment(ImpPoint ip1,ImpPoint ip2)
	{
		if (m_start == ip1)
			m_start = (AnchorPoint) ip2;
		if(m_end==ip1)
			m_end = (AnchorPoint) ip2;
	}

}
