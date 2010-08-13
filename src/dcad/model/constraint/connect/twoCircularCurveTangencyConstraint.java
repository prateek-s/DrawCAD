package dcad.model.constraint.connect;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.util.GMethods;

public class twoCircularCurveTangencyConstraint extends TangentConstraint
{
	public twoCircularCurveTangencyConstraint(SegCircleCurve seg1, SegCircleCurve seg2,AnchorPoint ap, int category,boolean promoted)
	{
//		super(seg1,seg2,contactPt1,contactPt2,category,promoted);
		super(seg1,seg2,ap,category,promoted);
		addPoint(seg1.getM_start());
		addPoint(seg1.getM_end());
		addPoint(seg1.getM_center());
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
		returnVec.addAll(constraintEquations.getCollinearPointsEQ(pointStrings[0],pointStrings[3],pointStrings[6]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[3],pointStrings[1],pointStrings[2],pointStrings[0]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurveEQ(pointStrings[6],pointStrings[4],pointStrings[5],pointStrings[0]));
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
		returnVec.addAll(constraintEquations.getCollinearPointsPD(pt, xory,anchorPoints[0],anchorPoints[3],anchorPoints[6],pointStrings[0],pointStrings[3],pointStrings[6]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[3],anchorPoints[1],anchorPoints[2],anchorPoints[0],pointStrings[3],pointStrings[1],pointStrings[2],pointStrings[0]));
		returnVec.addAll(constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[6],anchorPoints[4],anchorPoints[5],anchorPoints[0],pointStrings[6],pointStrings[4],pointStrings[5],pointStrings[0]));
		return returnVec;
	}

	private void calcMarkers()
	{
			SegCircleCurve curveSeg1 = (SegCircleCurve)m_seg1;
			SegCircleCurve curveSeg2 = (SegCircleCurve)m_seg2;
			
			marker_line_center1.setLine(curveSeg1.getM_center().getM_point(), m_contactPt1);
			marker_line_center2.setLine(curveSeg2.getM_center().getM_point(), m_contactPt2);
			
			// calculate the display markers
			Line2D line = null;
			if(curveSeg1.getM_radius() >= curveSeg2.getM_radius())
			{
				Point2D p1 = GMethods.interpolate(m_contactPt1, curveSeg1.getM_center().getM_point(), curveSeg2.getM_radius());
				line = new Line2D.Double(p1, curveSeg2.getM_center().getM_point());
			}
			else
			{
				Point2D p1 = GMethods.interpolate(m_contactPt2, curveSeg2.getM_center().getM_point(), curveSeg1.getM_radius());
				line = new Line2D.Double(p1, curveSeg1.getM_center().getM_point());
			}
			double distance = line.getP1().distance(line.getP2());
			double rad = Math.sqrt((distance/2)*(distance/2) + (MARKER_LINE_LENGTH/2)*(MARKER_LINE_LENGTH/2));
			Vector centers = GMethods.CircleCircleIntersections(line.getX1(), line.getY1(), rad, line.getX2(), line.getY2(), rad);
			if(marker_line_contact != null) marker_line_contact.setLine((Point2D)centers.get(0), (Point2D)centers.get(1)); 
			
			setAngleMarkers(marker_line_contact.getP1(), curveSeg1.getM_center().getM_point(), m_contactPt1, anglemarker1);
			setAngleMarkers(marker_line_contact.getP1(), curveSeg2.getM_center().getM_point(), m_contactPt2, anglemarker2);
	}

	public void update()
	{
		calcMarkers();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		if( 
				constraintsHelper.areSlopesEqual((AnchorPoint)points.get(0),(AnchorPoint)points.get(3),(AnchorPoint)points.get(6),(AnchorPoint)points.get(0),true)
				&& constraintsHelper.onCircularCurveOrNot((AnchorPoint)points.get(1),(AnchorPoint)points.get(2),(AnchorPoint)points.get(3),(AnchorPoint)points.get(0),true)
				&& constraintsHelper.onCircularCurveOrNot((AnchorPoint)points.get(4),(AnchorPoint)points.get(5),(AnchorPoint)points.get(6),(AnchorPoint)points.get(0),true)
			)
			return true;
		System.out.println("Two curves tangency failed...");
		return false;
	}
}