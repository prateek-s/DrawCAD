package dcad.model.constraint.connect;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.PointSegmentConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class IntersectionConstraint extends ConnectConstraint
{
	
	public IntersectionConstraint(Segment seg1, Segment seg2, Point2D intersectionPt1, Point2D intersectionPt2, int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
		setM_contactPt1(intersectionPt1);
		setM_contactPt2(intersectionPt1);
		points.add(new AnchorPoint(intersectionPt1, null));
	}

	public String toString()
	{
		return super.toString()+"Intersection Constraints: Segments: "+m_seg1.getM_label()+", "+m_seg2.getM_label()+" ("+GMethods.formatNum(m_contactPt1.getX())+", "+GMethods.formatNum(m_contactPt1.getY())+")"+" ("+GMethods.formatNum(m_contactPt2.getX())+", "+GMethods.formatNum(m_contactPt2.getY())+")";
	}

	public Vector getEquation(Vector fixedPoints)
	{
/*		Vector retVec = new Vector();
		retVec.addAll(constraintsHelper.getPointOnSegmentEQs(m_seg1,(AnchorPoint)points.get(0)));
		retVec.addAll(constraintsHelper.getPointOnSegmentEQs(m_seg2,(AnchorPoint)points.get(0)));
		return retVec;*/
		return null;
	}
	
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
/*		Vector returnVec = new Vector();
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		returnVec.addAll(constraintsHelper.getPointOnSegmentPDs(pt,xory,m_seg1,(AnchorPoint)points.get(0)));
		returnVec.addAll(constraintsHelper.getPointOnSegmentPDs(pt,xory,m_seg2,(AnchorPoint)points.get(0)));
		return returnVec;*/
		return null;
	}

	public void draw(Graphics g)
	{
		if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL) && (m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
		{
		}
		super.draw(g);
	}
}