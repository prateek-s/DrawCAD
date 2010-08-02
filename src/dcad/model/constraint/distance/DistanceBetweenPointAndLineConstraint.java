package dcad.model.constraint.distance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GConstants;
import dcad.util.GMethods;


public class DistanceBetweenPointAndLineConstraint extends PointSegmentConstraint
{
	private double m_distance=0;
	private double distanceToShow = 0;

	public DistanceBetweenPointAndLineConstraint(SegLine l, AnchorPoint ap,double distance, int category,boolean promoted)
	{
		super(l,ap,category,promoted);
		Point2D p = l.getNearestPointOnSeg(ap.getM_point());
		distanceToShow = distance;
		double distanceBetweenPoints = ap.distance(p);
		if(GConstants.drawingRatio == -1)
		{
			if(distanceBetweenPoints < distance)
				setDrawingRatio(1,1);
			else
				setDrawingRatio(distanceBetweenPoints,distance);
		}
		m_distance=distance * GConstants.drawingRatio;
		
		//We had to add this code so as to choose the initial position enough far so as to make nonzero distance.
		//This occurred in the benchmark image.
		
		// Find the nearest point on line from the center of an arc
/*		if(distanceBetweenPoints == 0 || distanceBetweenPoints<m_distance)
		{
			double difference = m_distance - distanceBetweenPoints;
			Point2D nearestPoint = null;
			if(l.getM_start().distance(p) > difference)
				nearestPoint = l.getM_start().getM_point();
			else if(l.getM_end().distance(p) > difference)
				nearestPoint = l.getM_end().getM_point();
			if(nearestPoint != null)
				//p = GMethods.interpolate(p,nearestPoint,difference);
				p = new Point2D.Double(p.getX() + m_distance,p.getY() + m_distance);
			else
				//p = GMethods.extrapolate(p,l.getM_start().getM_point(),difference - l.getM_start().getM_point().distance(p));
				p = new Point2D.Double(p.getX() + m_distance,p.getY() + m_distance);
		}*/
		
		AnchorPoint ap1 = new AnchorPoint(p.getX(),p.getY());
		addPoint(ap1);
		addPoint(l.getM_start());
		addPoint(l.getM_end());
		addPoint(ap);
		
	}
	public String toString()
	{
		return addPrefix()+" Distance between " + m_seg.getM_label() + " and " + m_ap.getM_label() + " : " + distanceToShow;
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		retVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[0],pointStrings[1],pointStrings[2]));
		retVec.add(constraintEquations.getLineLengthEQ(pointStrings[0],pointStrings[3],m_distance));
		retVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[0],pointStrings[3],pointStrings[1],pointStrings[2]));
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
		retVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]));
		retVec.add(constraintEquations.getLineLengthPD(pt, xory, anchorPoints[0],anchorPoints[3],pointStrings[0],pointStrings[3]));
		retVec.add(constraintEquations.getPerpendicularSegmentsPD(pt,xory,anchorPoints[0],anchorPoints[3],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[3],pointStrings[1],pointStrings[2]));
		return retVec;
	}

	public boolean isConstraintSolved()
	{
		this.update();
		if(
				constraintsHelper.independentLengthSatisfied((AnchorPoint)points.elementAt(0),(AnchorPoint)points.elementAt(3),m_distance,true) 
				&& constraintsHelper.onLineOrNot((AnchorPoint)points.get(0),(AnchorPoint)points.get(1),(AnchorPoint)points.get(2),true)
		)
			return true;
		System.out.println("Point - Line Distance constraint failed...");
		return false;
	}
	
	public void draw (Graphics g)
	{
		if(m_seg.isHighlighted() || m_ap.isHighlighted())
		{
			AnchorPoint ap1 = (AnchorPoint)points.get(0);
			AnchorPoint ap2 = (AnchorPoint)points.get(3);
			Graphics2D g2d = (Graphics2D)g;
			Color prevColor = g.getColor();
			g2d.setColor(getColor());

			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));
			Line2D l = new Line2D.Double(ap1.getM_point(),ap2.getM_point());
			g2d.draw(l);
			g2d.setStroke(prevStroke);
			
			Point2D midPoint = new Point2D.Double( ( ap1.getX() + ap2.getX() ) / 2  ,  ( ap1.getY() + ap2.getY() ) / 2 );
			g2d.drawString( String.valueOf(distanceToShow) , (int) midPoint.getX() - 10 , (int) midPoint.getY() - 10 );
				
			g2d.setColor(prevColor);
		}
	}

	
}