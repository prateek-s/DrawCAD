package dcad.model.constraint.angle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.marker.MarkerAngle;
import dcad.model.marker.MarkerPerpendicular;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class PerpendicularSegConstraint extends RelAngleConstraint
{
	private Line2D m_markerLine1 = new Line2D.Double(-1,-1,-1,-1);
	private Line2D m_markerLine2 = new Line2D.Double(-1,-1,-1,-1);

	public PerpendicularSegConstraint(SegLine seg1, SegLine seg2,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
		
		Vector uniquePoints=new Vector();
		uniquePoints= constraintsHelper.getUniquePointsForConnectedLines(seg1,seg2);
		if(uniquePoints.size()>0)
		{
			addPoint((AnchorPoint)uniquePoints.elementAt(0));
			addPoint((AnchorPoint)uniquePoints.elementAt(1));
			addPoint((AnchorPoint)uniquePoints.elementAt(2));
		}
		calcMarker();
		setM_angleDiff(90.0);
	}
	
	public String toString()
	{
		return addPrefix()+" Perpendicular lines : "+m_seg1.getM_label()+" , "+m_seg2.getM_label();
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getConnectedPerpendicularSegmentsEQ(pointStrings[0],pointStrings[2],pointStrings[1]));
		return retVec;
	}
	
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.add(constraintEquations.getConnectedPerpendicularSegmentsPD(pt, xory, anchorPoints[0],anchorPoints[2],anchorPoints[1],pointStrings[0],pointStrings[2],pointStrings[1]));
		return retVec;
	}


	public void draw(Graphics g)
	{
		
		// draw only if both the segments are enabled and both are instances of line.
		if(m_seg1.isEnabled() && m_seg2.isEnabled())
		{
			if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL)&&(m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
			{
				Graphics2D g2d = (Graphics2D)g;
				// set the color of the graphics to the color of the segment
				Color prevColor = g.getColor();
				g2d.setColor(getColor());
				
				if((m_markerLine1 != null) && (m_markerLine2 != null))
				{
					g2d.draw(m_markerLine1);
					g2d.draw(m_markerLine2);
				}
				// reset the graphics color back
				g2d.setColor(prevColor);
			}
		}
	}

	protected  void calcMarker()
	{
		if(points.size()>0)
		{
			AnchorPoint A=(AnchorPoint)points.get(0);
			AnchorPoint C=(AnchorPoint)points.get(1);
			AnchorPoint E=(AnchorPoint)points.get(2);
		// 	find point on the LARGER part of line1
			Point2D p1 = GMethods.interpolate(E.getM_point(), A.getM_point(), SIZE);
			
		// 	find point on the LARGER part of line2
			Point2D p2 = GMethods.interpolate(E.getM_point(), C.getM_point(), SIZE);
			
		// 	at this point we have two points on the LARGER points
			Vector centers = GMethods.CircleCircleIntersections(p1.getX(), p1.getY(), SIZE, p2.getX(), p2.getY(), SIZE);
			if(centers != null)
			{
				Point2D p3 = null;
				if(E.distance((Point2D)centers.get(0)) >= E.distance((Point2D)centers.get(1)))
				{
					p3 = (Point2D)centers.get(0);
				}
				else
				{
					p3 = (Point2D)centers.get(1);
				}
				m_markerLine1.setLine(p1, p3);
				m_markerLine2.setLine(p2, p3);
			}
		}
	}
	
	public void update()
	{
		calcMarker();
		super.update();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		SegLine seg1=(SegLine)m_seg1;
		SegLine seg2=(SegLine)m_seg2;
		if (constraintsHelper.areLinesPerpendicular(seg1.getM_start(),seg1.getM_end(),seg2.getM_start(),seg2.getM_end(),-1,true))
			return true;
		System.out.println("Perpendicular segments constraint failed...");
		return false;
	}
}