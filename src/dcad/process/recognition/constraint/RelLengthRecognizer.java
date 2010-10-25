package dcad.process.recognition.constraint;

import java.util.Vector;

import javax.swing.text.html.MinimalHTMLWriter;

import dcad.Prefs;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.EqualRadiusConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;
import dcad.util.Maths;

public class RelLengthRecognizer extends RelConstraintRecognitionScheme
{
	public static final double DEF_MAX_EQUAL_LENGTH_TOL_PERCENT = 0.05;
	public static final double NINETY_DEGREES = 90.0;
	public static final double ZERO_DEGREES = 0.0;
	public static final double ANGLE_TOLERANCE = 0.01;
	public RelLengthRecognizer(Segment seg1, Segment seg2)
	{
		super(seg1, seg2);
	}

	public Vector recognize()
	{
		Constraint cons = null;
		double theta1,theta2;
		
		/*if((m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX())!=0){
			theta1 = (Math.atan((m_seg1.getSegEnd().getY()- m_seg1.getSegStart().getY())/
					(m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX()))) *  180 / Math.PI;
			}
		if((m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX())!=0){
			theta2 = (Math.atan((m_seg2.getSegEnd().getY()- m_seg2.getSegStart().getY())/
					(m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX()))) *  180 / Math.PI;
			}*/
		//added on 05-04-10
		if(m_seg1 instanceof SegLine && m_seg2 instanceof SegLine)
		{
			SegLine l1=(SegLine)m_seg1;
			SegLine l2=(SegLine)m_seg2;
		if(constraintsHelper.areLengthsEqual(l1.getM_start(),l1.getM_end(),l2.getM_start(),l2.getM_end(),false)){
			//1 Check if the segments are adjacent
			if(m_seg1.getSegEnd().equals(m_seg2.getSegStart()) || m_seg2.getSegEnd().equals(m_seg1.getSegStart())){
				cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
			}
		
		    //2 Check whether both are parallel to each other
			else if(constraintsHelper.areSlopesEqual(m_seg1.getSegStart(),m_seg1.getSegEnd(),m_seg2.getSegStart(),m_seg2.getSegEnd(),false)){
				cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
			}
			
			//3 Check if one line makes an angle of theta with X-axis then is other making (180-theta) or (360-theta) 
			
			else if(true){
				if((m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX())!=0){
				theta1 = (Math.atan((m_seg1.getSegEnd().getY()- m_seg1.getSegStart().getY())/
						(m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX()))) *  180 / Math.PI;
				}
				else {
					theta1 = 0;
				}
				if((m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX())!=0){
				theta2 = (Math.atan((m_seg2.getSegEnd().getY()- m_seg2.getSegStart().getY())/
						(m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX()))) *  180 / Math.PI;
				}
				else{
					theta2 = 0;
				}
				// check for theta and 180-theta  or theta and 360-theta 2
				if((theta1 == (180-theta2)) || (theta1 == (360-theta2))){
					cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
				}
				else if((theta1 == 0 && theta2 == 90) || (theta1 == 90 && theta2 == 0) ){
					cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
				}
			}
		}
		
	}
		/*
		if(m_seg1 instanceof SegLine && m_seg2 instanceof SegLine)
		{
			SegLine l1=(SegLine)m_seg1;
			SegLine l2=(SegLine)m_seg2;
			
			//added on 05-04-10
			if(constraintsHelper.areLengthsEqual(l1.getM_start(),l1.getM_end(),l2.getM_start(),l2.getM_end(),false)){
				cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
				//1 Check if the segments are adjacent
	//			if(m_seg1.getSegEnd().equals(m_seg2.getSegStart()) || m_seg2.getSegEnd().equals(m_seg1.getSegStart())){
		//			cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
		//		}
			
/*			    //2 Check whether both are parallel to each other
				else if(constraintsHelper.areSlopesEqual(m_seg1.getSegStart(),m_seg1.getSegEnd(),m_seg2.getSegStart(),m_seg2.getSegEnd(),false)){
					cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
				}
				
				//3 Check if one line makes an angle of theta with X-axis then is other making (180-theta) or (360-theta) 
				else if(true){
				/*	if((m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX())!=0){
					theta1 = (Math.atan((m_seg1.getSegEnd().getY()- m_seg1.getSegStart().getY())/
							(m_seg1.getSegEnd().getX()- m_seg1.getSegStart().getX()))) *  180 / Math.PI;
					}
					else {
						theta1 = 0;
					}
					
						if((m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX())!=0){
					theta2 = (Math.atan((m_seg2.getSegEnd().getY()- m_seg2.getSegStart().getY())/
							(m_seg2.getSegEnd().getX()- m_seg2.getSegStart().getX()))) *  180 / Math.PI;
					}
					else{
						theta2 = 0;
					}
					*/
				/*	
					theta1 = Maths.AngleInDegrees(m_seg1.getSegStart().getX(), m_seg1.getSegStart().getY(),
								m_seg1.getSegEnd().getX(),m_seg1.getSegEnd().getY());
					
					theta2 = Maths.AngleInDegrees(m_seg2.getSegStart().getX(), m_seg2.getSegStart().getY(),
							m_seg2.getSegEnd().getX(),m_seg2.getSegEnd().getY());
					
					
					
					///System.out.println("Theta 1 = " + theta1 + "Theta 2 = " + theta2);
					
					
					
					// from 0 to PI
					if(theta1 > 0.0 && theta2 > 0.0){
						if(theta1 < 90.0){
							theta2 = 180 - theta2;
						}
						else{
							theta1 = 180 - theta1;
						}
					}
					// 1st and 4th quad.
					else if(theta1 > 0.0 && theta2 < 0.0){
						theta2 = -(theta2);
					}
					// 4th and 1st quad
					else if(theta1 < 0.0 && theta2 > 0.0){
						theta1 = -(theta1);
					}
					//3rd and 4th quad.
					else if(theta1 < 0.0 && theta2 < 0.0){
						theta1 = -(theta1);
						theta2 = -(theta2);
					}
					
				
					
					if((theta1 - theta2) < ANGLE_TOLERANCE){
						cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
					}
					else if(((Double.compare(theta1,ZERO_DEGREES) == 0) && (Double.compare(theta2,NINETY_DEGREES) == 0)) 
							|| ((Double.compare(theta2,ZERO_DEGREES) == 0) && (Double.compare(theta1,NINETY_DEGREES) == 0))){
						cons=getEqualLengthConstraint(l1,l2,Constraint.HARD,true);
					}
				}*/
			//}
		//}
		else if(m_seg1 instanceof SegCircleCurve && m_seg2 instanceof SegCircleCurve)
		{
			SegCircleCurve c1=(SegCircleCurve) m_seg1;
			SegCircleCurve c2=(SegCircleCurve) m_seg2;
			if(constraintsHelper.areLengthsEqual(c1.getM_start(),c1.getM_center(),c2.getM_start(),c2.getM_center(),false))
				cons=getEqualRadiusConstraint(c1,c2,Constraint.HARD,true);
		}
		
		if(cons!=null)
			addConstraint(cons,new Segment[]{m_seg1,m_seg2});
		return m_constraints;
	}
	
	public static Constraint getEqualLengthConstraint(SegLine l1,SegLine l2,int category, boolean promoted)
	{
		Constraint c=constraintsHelper.getConstraintBetween2Segments(l1,l2,EqualRelLengthConstraint.class);
		if(c==null)
			return new EqualRelLengthConstraint(l1, l2, category,promoted);
		if(!promoted)
			c.setPromoted(promoted);
		return null;
	}
	
	public static Constraint getEqualRadiusConstraint(SegCircleCurve c1,SegCircleCurve c2, int category, boolean promoted)
	{
		Constraint c=constraintsHelper.getConstraintBetween2Segments(c1,c2,EqualRadiusConstraint.class);
		if(c==null)
			return new EqualRadiusConstraint(c1, c2, category,promoted);
		if(!promoted)
			c.setPromoted(promoted);
		return null;
	}
}