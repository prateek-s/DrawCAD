package dcad.model.constraint.connect;

import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.Prefs;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class lineCircularCurveTangencyConstraint extends TangentConstraint
{
	public lineCircularCurveTangencyConstraint(SegLine seg1, SegCircleCurve seg2, AnchorPoint ap, int category,boolean promoted)
	{
//		super(seg1,seg2,contactPt1,contactPt2,category,promoted);
		super(seg1,seg2,ap,category,promoted);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg2.getM_start());
		addPoint(seg2.getM_end());
		addPoint(seg2.getM_center());
		calcMarkers();
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getPerpendicularSegmentsEQ(pointStrings[1], pointStrings[2], pointStrings[5],pointStrings[0]));
		returnVec.addAll(constraintEquations.getPointOnLineEQ(pointStrings[0],pointStrings[1],pointStrings[2]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[5],pointStrings[3],pointStrings[4],pointStrings[0]));
		return returnVec;
	}
	
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		Vector returnVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		returnVec.add(constraintEquations.getPerpendicularSegmentsPD(pt, xory, anchorPoints[1], anchorPoints[2], anchorPoints[5],anchorPoints[0], pointStrings[1], pointStrings[2], pointStrings[5],pointStrings[0]));
		returnVec.addAll(constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[5],anchorPoints[3],anchorPoints[4],anchorPoints[0],pointStrings[5],pointStrings[3],pointStrings[4],pointStrings[0]));
		return returnVec;
	}

	private void calcMarkers()
	{
		SegLine lineSeg = (SegLine)m_seg1;
		SegCircleCurve curveSeg = (SegCircleCurve)m_seg2;
			
		marker_line_center1.setLine(curveSeg.getM_center().getM_point(), m_contactPt1);
		marker_line_center2.setLine(curveSeg.getM_center().getM_point(), m_contactPt1);
		
		//Calculate marker line contact
		
		Point2D oppositePoint = null;
		if(m_contactPt1.distance(lineSeg.getM_start().getM_point()) < MARKER_LINE_LENGTH/2)
			oppositePoint = lineSeg.getM_end().getM_point();
		else if(m_contactPt1.distance(lineSeg.getM_end().getM_point()) < Prefs.getAnchorPtSize())
			oppositePoint = lineSeg.getM_start().getM_point();

		Point2D p1,p2; 
		if(oppositePoint == null)
		{
			p1 = GMethods.interpolate(m_contactPt1,lineSeg.getM_start().getM_point(),MARKER_LINE_LENGTH);
			p2 = GMethods.interpolate(m_contactPt1,lineSeg.getM_end().getM_point(),MARKER_LINE_LENGTH);
		}
		else
		{
			p1 = GMethods.interpolate(m_contactPt1,oppositePoint,MARKER_LINE_LENGTH);
			p2 = GMethods.extrapolate(oppositePoint,m_contactPt1,MARKER_LINE_LENGTH);
		}
		marker_line_contact.setLine(p1,p2);
		
		
		if(lineSeg.getM_start().distance(m_contactPt1) >= lineSeg.getM_end().distance(m_contactPt1))
		{
			setAngleMarkers(lineSeg.getM_start().getM_point(), curveSeg.getM_center().getM_point(), m_contactPt1, anglemarker1);
			setAngleMarkers(lineSeg.getM_start().getM_point(), curveSeg.getM_center().getM_point(), m_contactPt1, anglemarker2);
		}
		else
		{
			setAngleMarkers(lineSeg.getSegEnd().getM_point(), curveSeg.getM_center().getM_point(), m_contactPt1, anglemarker1);
			setAngleMarkers(lineSeg.getSegEnd().getM_point(), curveSeg.getM_center().getM_point(), m_contactPt1, anglemarker2);
		}
	}
	
	public void update()
	{
		calcMarkers();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		if ( 
				constraintsHelper.areLinesPerpendicular((AnchorPoint)points.get(1),(AnchorPoint)points.get(2),(AnchorPoint)points.get(5),(AnchorPoint)points.get(0),-1,true)
				&& constraintsHelper.onLineOrNot((AnchorPoint)points.get(0),(AnchorPoint)points.get(1),(AnchorPoint)points.get(2),true)
				&& constraintsHelper.onCircularCurveOrNot((AnchorPoint)points.get(3),(AnchorPoint)points.get(4),(AnchorPoint)points.get(5),(AnchorPoint)points.get(0),true)
			)
			return true;
		///System.out.println("line curve tangency failed...");
		return false;
	}
	
}