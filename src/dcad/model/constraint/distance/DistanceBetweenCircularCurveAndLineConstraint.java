package dcad.model.constraint.distance;

import java.awt.BasicStroke;
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
import dcad.model.constraint.connect.ConnectConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GConstants;

public class DistanceBetweenCircularCurveAndLineConstraint extends ConnectConstraint
{
	private double m_distance=0;
	private double distanceToShow = 0;
	protected Line2D markerLine = new Line2D.Double(-1, -1, -1, -1);
	
	public DistanceBetweenCircularCurveAndLineConstraint(SegLine seg1, SegCircleCurve seg2,double distance, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		
		// Find the nearest point on line from the center of an arc
		Point2D p=seg1.getNearestPointOnSeg(seg2.getM_center().getM_point());
		AnchorPoint ap1 = new AnchorPoint(p.getX(),p.getY());
		
		//Find the nearest point on arc from the point found above
		p = seg2.getNearestPointOnSeg(p);
		AnchorPoint ap2 = new AnchorPoint(p.getX(),p.getY());

		setM_contactPt1(ap1.getM_point());
		setM_contactPt2(ap2.getM_point());
		
		addPoint(ap1);
		addPoint(ap2);
		
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		addPoint(seg2.getM_center());

		distanceToShow = distance;
		if(GConstants.drawingRatio == -1)
			setDrawingRatio(ap1.distance(ap2),distance);
		m_distance=distance * GConstants.drawingRatio;
	}
	
	public String toString()
	{
		return addPrefix()+" Distance between "+m_seg1.getM_label()+" and "+m_seg2.getM_label()+" : " + distanceToShow;
	}

	public void draw(Graphics g)
	{
		if(m_seg1.isHighlighted() || m_seg2.isHighlighted())
		{
			AnchorPoint ap1=(AnchorPoint)points.get(0);
			AnchorPoint ap2=(AnchorPoint)points.get(1);
			markerLine.setLine(ap1.getX(),ap1.getY(),ap2.getX(),ap2.getY());
			
			Graphics2D g2d = (Graphics2D)g;
			Color prevColor = g.getColor();
			g2d.setColor(getColor());
			
			// create a dashed line for radii lines
			BasicStroke prevStroke = (BasicStroke)g2d.getStroke();
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(), prevStroke.getLineJoin(), prevStroke.getMiterLimit(), new float[]{4, 4, 8, 4}, prevStroke.getDashPhase()));
			g2d.draw(markerLine);
			g2d.setStroke(prevStroke);
			
			Point2D midPoint = new Point2D.Double( ( markerLine.getX1() + markerLine.getX2() ) / 2  ,  ( markerLine.getY1() + markerLine.getY2() ) / 2 );
			g2d.drawString( String.valueOf(distanceToShow) , (int) midPoint.getX() - 10 , (int) midPoint.getY() - 10 );
			
			// reset the graphics color back
			g2d.setColor(prevColor);
			super.draw(g);
		}
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		
		returnVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[1],pointStrings[0],pointStrings[6]));
		returnVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		returnVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[0],pointStrings[2],pointStrings[3]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[6],pointStrings[4],pointStrings[5],pointStrings[1]));
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[0],pointStrings[1],m_distance));

		return returnVec;
	}

	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector retVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		
		retVec.addAll(constraintEquations.getCollinearPointsPD(pt,xory,anchorPoints[1],anchorPoints[0],anchorPoints[6],pointStrings[1],pointStrings[0],pointStrings[6]));
		retVec.add(constraintEquations.getPerpendicularSegmentsPD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]));
		retVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[0],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[2],pointStrings[3]));
		retVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[6],anchorPoints[4],anchorPoints[5],anchorPoints[1],pointStrings[6],pointStrings[4],pointStrings[5],pointStrings[1]));
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));

		return retVec;
	}

	public boolean isConstraintSolved()
	{
		AnchorPoint[] ap=constraintsHelper.getAnchorPoints(points);

		if (
				constraintsHelper.areSlopesEqual(ap[0],ap[6],ap[1],ap[6],true) 
				&& constraintsHelper.areLinesPerpendicular(ap[0],ap[1],ap[2],ap[3],-1,true) 
				&& constraintsHelper.onLineOrNot(ap[0],ap[2],ap[3],true) 
				&& constraintsHelper.onCircularCurveOrNot(ap[4],ap[5],ap[6],ap[1],true) 
				&& constraintsHelper.independentLengthSatisfied(ap[0],ap[1],m_distance,true)
			)
			return true;
		
		System.out.println("Distance between curve and line failed...");
		return false;
	}

}