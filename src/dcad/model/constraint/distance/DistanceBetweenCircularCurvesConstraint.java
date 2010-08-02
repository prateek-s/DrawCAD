
package dcad.model.constraint.distance;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.connect.ConnectConstraint;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.length.RelLengthConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GConstants;

public class DistanceBetweenCircularCurvesConstraint extends ConnectConstraint
{
	private double m_distance=0;
	private double distanceToShow = 0;
	public DistanceBetweenCircularCurvesConstraint(SegCircleCurve seg1, SegCircleCurve seg2,double distance, int catagory,boolean promoted)
	{
		super(seg1, seg2,Constraint.HARD,promoted);
		
		// Find the nearest point on circular arc 1 from the center of an arc 2
		Point2D p=seg1.getNearestPointOnSeg(seg2.getM_center().getM_point());
		AnchorPoint ap1 = new AnchorPoint(p.getX(),p.getY());
		
		// Find the nearest point on circular arc 2 from point p
		p = seg2.getNearestPointOnSeg(p);
		AnchorPoint ap2 = new AnchorPoint(p.getX(),p.getY());

		setM_contactPt1(ap1.getM_point());
		setM_contactPt2(ap2.getM_point());
		
		addPoint(ap1);
		addPoint(ap2);
		
		
		
		//calcMarkers(seg1, seg2);
		distanceToShow = distance;
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg1.getM_center());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		addPoint(seg2.getM_center());
		
		//Add two auxiliary points
		/*AnchorPoint p1=new AnchorPoint(seg1.getM_start().getX(),seg1.getM_start().getY());
		AnchorPoint p2=new AnchorPoint(seg2.getM_start().getX(),seg2.getM_start().getY());
		addPoint(p1);
		addPoint(p2);*/
		m_distance = distance * GConstants.cmScaleDrawingRatio;
	}
	
	public String toString()
	{
		return addPrefix()+"Distance Between Circular Arcs Constraint: Segments: "+m_seg1.getM_label()+", "+m_seg2.getM_label()+", Distance:" + distanceToShow;
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[7],pointStrings[5],pointStrings[6],pointStrings[1]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[4],pointStrings[3],pointStrings[2],pointStrings[0]));
		returnVec.add(constraintEquations.getLineLengthEQ(pointStrings[0],pointStrings[1],m_distance));

		returnVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[1],pointStrings[0],pointStrings[4]));
		returnVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[1],pointStrings[0],pointStrings[7]));
		returnVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[7],pointStrings[0],pointStrings[4]));
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
	
		retVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[7],anchorPoints[5],anchorPoints[6],anchorPoints[1],pointStrings[7],pointStrings[5],pointStrings[6],pointStrings[1]));
		retVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[4],anchorPoints[3],anchorPoints[2],anchorPoints[0],pointStrings[4],pointStrings[3],pointStrings[2],pointStrings[0]));
		retVec.add(constraintEquations.getLineLengthPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]));

		retVec.addAll(constraintEquations.getCollinearPointsPD(pt,xory,anchorPoints[1],anchorPoints[0],anchorPoints[4],pointStrings[1],pointStrings[0],pointStrings[4]));
		retVec.addAll(constraintEquations.getCollinearPointsPD(pt,xory,anchorPoints[1],anchorPoints[0],anchorPoints[7],pointStrings[1],pointStrings[0],pointStrings[7]));
		retVec.addAll(constraintEquations.getCollinearPointsPD(pt,xory,anchorPoints[7],anchorPoints[0],anchorPoints[4],pointStrings[7],pointStrings[0],pointStrings[4]));
		return retVec;
	}

	public void draw(Graphics g)
	{
	}

	protected void calcMarkers(SegCircleCurve seg1, SegCircleCurve seg2)
	{
	}

	public void update()
	{
		//calcMarkers((SegCircleCurve)m_seg1, (SegLine)m_seg2);
		super.update();
	}
	public boolean isConstraintSolved()
	{
		this.update();
		AnchorPoint[] ap=constraintsHelper.getAnchorPoints(points);
		
		if(
				constraintsHelper.onCircularCurveOrNot(ap[5],ap[6],ap[7],ap[1],true) 
				&& constraintsHelper.onCircularCurveOrNot(ap[2],ap[3],ap[4],ap[0],true) 
				&& constraintsHelper.independentLengthSatisfied(ap[0],ap[1],m_distance,true)
				&& 	constraintsHelper.areSlopesEqual(ap[0],ap[4],ap[1],ap[4],true) 
				&& 	constraintsHelper.areSlopesEqual(ap[0],ap[7],ap[1],ap[7],true) 
				&& 	constraintsHelper.areSlopesEqual(ap[0],ap[4],ap[7],ap[4],true) 
			)
			return true;
		System.out.println("Distance between circular curves constraint failed... ");
		return false;
	}

	public void setM_distance(double m_distance)
	{
		this.m_distance = m_distance;
	}
}
