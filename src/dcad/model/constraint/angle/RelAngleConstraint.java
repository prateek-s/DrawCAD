package dcad.model.constraint.angle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.StringTokenizer;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.RelativeConstraint;
import dcad.model.constraint.constraintEquations;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.MarkerAngle;
import dcad.util.GConstants;
import dcad.util.GMethods;

public class RelAngleConstraint extends RelativeConstraint
{
	public static final double SIZE = 14;
	public static final double ANGLUAR_GAP = SIZE/2;
	public static final double RATIO = 0.25;
	protected double m_angleDiff = 0.0;
	private Arc2D m_arc = null; 
	
	public RelAngleConstraint(Segment seg1, Segment seg2,int category,boolean promoted)
	{
		super(seg1, seg2,category,promoted);
	}
	
	public RelAngleConstraint(Segment seg1, Segment seg2, double diff,int category,boolean promoted)
	{
		this(seg1, seg2,category,promoted);
		setM_angleDiff(diff);
		System.out.println("Angle between lines " + diff);
		Vector uniquePoints=new Vector();
		uniquePoints= constraintsHelper.getUniquePointsForConnectedLines((SegLine)seg1,(SegLine)seg2);
		if(uniquePoints.size()>0)
		{
			addPoint((AnchorPoint)uniquePoints.elementAt(0));
			addPoint((AnchorPoint)uniquePoints.elementAt(1));
			addPoint((AnchorPoint)uniquePoints.elementAt(2));
		}
		calcMarker();
	}

	public double getM_angleDiff()
	{
		if(m_angleDiff==-1)
			m_angleDiff=m_arc.getAngleExtent();
		return m_angleDiff;
	}

	public void setM_angleDiff(double diff)
	{
//		System.out.println("Angle given is  : "+ diff);
		m_angleDiff = diff;
		
		// set the angle within the range.
		if(m_angleDiff > 360) m_angleDiff = diff - (((int)(diff/360))*360);
		else if(m_angleDiff < -360) m_angleDiff = diff - (((int)(diff/360)+1)*360);
	}
	
	public String toString()
	{
		return addPrefix()+" Angle between lines "+m_seg1.getM_label()+" and "+m_seg2.getM_label()+" : "+GMethods.formatNum(m_angleDiff);
	}

	public Vector getEquation(Vector fixedPoints)
	{
		Vector retVec = new Vector();
		if(points.size()>0)
		{
			AnchorPoint[] anchorPoints=constraintsHelper.getAnchorPoints(points);
			String[][] pointStrings=constraintsHelper.getpointStrings(anchorPoints);
			retVec.add(constraintEquations.getConstantAngleEQ(pointStrings[0],pointStrings[1],pointStrings[2],getM_angleDiff()));
		}
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
		retVec.add(constraintEquations.getConstantAnglePD(pt, xory, anchorPoints[0], anchorPoints[1], anchorPoints[2], pointStrings[0], pointStrings[1], pointStrings[2]));
		return retVec;
	}

	public  void draw(Graphics g)
	{
		// draw only if both the segments are enabled and both are instances of line.
		if(m_seg1.isEnabled() && m_seg2.isEnabled() && (m_seg1 instanceof SegLine) && (m_seg2 instanceof SegLine))
		{
			if((m_seg1.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL)&&(m_seg2.getM_parentStk().getM_type() == Stroke.TYPE_NORMAL))
			{
				Graphics2D g2d = (Graphics2D)g;
				// set the color of the graphics to the color of the segment
				Color prevColor = g.getColor();
				g2d.setColor(getColor());
				
				// for hard constraint the indicator should be drawn where marker was origially drawn by the user AFAP 
				if(m_arc != null) g2d.draw(m_arc);
				
				// reset the graphics color back
				g2d.setColor(prevColor);
			}
		}
	}

	protected void calcMarker()
	{
		//This function is called form the constructor of the class.
		//The constructor may have been called from other constraints such as perpendicularSegCons or parallelSegCons etc.
		//In that case, those constraints will add points after calling the super constructor
		//At that time, the points will have no element. So, this check is must. 
		if(points.size()>0)
		{
			AnchorPoint A=(AnchorPoint)points.get(0);
			AnchorPoint B=(AnchorPoint)points.get(1);
			AnchorPoint C=(AnchorPoint)points.get(2);
			m_arc = new Arc2D.Double((C.getX()-SIZE), (C.getY()-SIZE), (2*SIZE), (2*SIZE), 0, 0, Arc2D.OPEN);
			//m_arc = new Arc2D.Double();//(Arc2D.OPEN);
			m_arc.setAngles(A.getM_point(), B.getM_point());
			if(m_arc.getAngleExtent() > 180) m_arc.setAngleExtent(m_arc.getAngleExtent()-360);
			
			//changed on 30-05-10
			// to show relative angle 
			//m_arc.setAngleExtent(m_arc.getAngleExtent()-360);
			//m_arc.setAngleExtent(m_arc.getAngleExtent() - 360);
		}
	}

	public void update()
	{
		calcMarker();
		super.update();
	}
	
	public void changeAngle()
	{
		setM_angleDiff(m_arc.getAngleExtent());
	}
	
	public boolean isConstraintSolved()
	{
		this.update();
		System.out.println("The marker arc angle is : " + m_arc.getAngleExtent());
		
/*		double angle1=((SegLine)m_seg1).getM_angle();
		double angle2=((SegLine)m_seg2).getM_angle();
		
		if(angle1<0)
			angle1+=180;
		if(angle2<0)
			angle2+=180;
		
		System.out.println("angles : " + angle1 + "  " + angle2  +"  "+ this.getM_angleDiff());*/
		System.out.println(this.getM_angleDiff() + " This was the difference");
		if(Math.abs( Math.abs(m_arc.getAngleExtent()) - getM_angleDiff() )<=constraintsHelper.relativeAngleErrorThreshold)
			return true;
		
		System.out.println("Relative angle constraint failed...");
		return false;
	}

}
