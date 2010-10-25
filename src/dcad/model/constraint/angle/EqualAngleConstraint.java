package dcad.model.constraint.angle;

import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.segment.SegLine;
import dcad.model.marker.MarkerAngle;

public class EqualAngleConstraint extends Constraint
{
	protected RelAngleConstraint m_constraint1 = null;
	protected RelAngleConstraint m_constraint2 = null;

	public EqualAngleConstraint(MarkerAngle marker1, MarkerAngle marker2,int category,boolean promoted)
	{
		super();
		setM_category(category);
		setPromoted(promoted);
		m_constraint1=new RelAngleConstraint(marker1.getM_seg1(), marker1.getM_seg2(), marker1.getM_angle(),Constraint.HIDDEN,false); 
		m_constraint2=new RelAngleConstraint(marker2.getM_seg1(), marker2.getM_seg2(), marker2.getM_angle(),Constraint.HIDDEN,false);
		
		Vector uniquePoints=new Vector();
		uniquePoints= constraintsHelper.getUniquePointsForConnectedLines((SegLine)marker1.getM_seg1(),(SegLine)marker1.getM_seg2());
		//if(uniquePoints.size()>0)
		{
			addPoint((AnchorPoint)uniquePoints.elementAt(0));
			addPoint((AnchorPoint)uniquePoints.elementAt(1));
			addPoint((AnchorPoint)uniquePoints.elementAt(2));
		}
		uniquePoints= constraintsHelper.getUniquePointsForConnectedLines((SegLine)marker2.getM_seg1(),(SegLine)marker2.getM_seg2());
		//if(uniquePoints.size()>0)
		{
			addPoint((AnchorPoint)uniquePoints.elementAt(0));
			addPoint((AnchorPoint)uniquePoints.elementAt(1));
			addPoint((AnchorPoint)uniquePoints.elementAt(2));
		}
	}

	public void draw(Graphics g)
	{
		if(m_constraint1!=null && m_constraint2!=null)
		{
			m_constraint1.draw(g);
			m_constraint2.draw(g);
		}
	}
	
	public Vector getEquation(Vector fixedPoints)
	{
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		Vector retVec = new Vector();
		retVec.add(constraintEquations.getEqualAngleEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3],pointStrings[4],pointStrings[5]));
		return retVec;
	}
	
	public Vector getPartialDifferentialString(String var, Vector fixedPoints)
	{
		StringTokenizer st = new StringTokenizer(var, ".");
		String pt = st.nextToken();
		String xory = st.nextToken();
		AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
		String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
		Vector retVec = new Vector();
		retVec.add(constraintEquations.getEqualAnglePD(pt, xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2],anchorPoints[3],anchorPoints[4],anchorPoints[5],pointStrings[3],pointStrings[4],pointStrings[5]));
		return retVec;
	}
	
	public String toString()
	{
		return super.toString()+" Equal Angles : ("+m_constraint1.getM_seg1().getM_label() + ", "+m_constraint1.getM_seg2().getM_label()+") and ("+m_constraint2.getM_seg1().getM_label()+", "+m_constraint2.getM_seg2().getM_label()+")";
	}

	public void delete()
	{
		m_constraint1.getM_seg1().removeConstraint(this);
		m_constraint1.getM_seg2().removeConstraint(this);
		m_constraint2.getM_seg1().removeConstraint(this);
		m_constraint2.getM_seg2().removeConstraint(this);
		if(m_constraint1!=null)
			m_constraint1.remove();
		if(m_constraint2!=null)
			m_constraint2.remove();
	}
	
	
	//Added on 13-5-2008
	public void changePoint(ImpPoint ip1,ImpPoint ip2)
	{
		super.changePoint(ip1,ip2);
		m_constraint1.changePoint(ip1,ip2);
		m_constraint2.changePoint(ip1,ip2);
	}

	
	public void update()
	{
		m_constraint1.update();
		m_constraint1.changeAngle();

		m_constraint2.update();
		m_constraint2.changeAngle();
		super.update();
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
	///	///System.out.println("Equal angle constraint : " + m_constraint1.getM_angleDiff() + "  " + m_constraint2.getM_angleDiff());
 		if(constraintsHelper.areAnglesEqual(m_constraint1.getM_angleDiff(),m_constraint2.getM_angleDiff(),true))
 			return true;
 	///	///System.out.println("Equal angle constraint failed...");
 		return false;
 		
	}

}