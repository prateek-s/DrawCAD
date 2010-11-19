package dcad.model.constraint;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;
import dcad.util.Maths;

public class constraintsHelper
{
	
	public static final String horizontalConstraintScaleFactor="100"; 
	public static final String verticalConstraintScaleFactor="100"; 
	public static final String lineMidPointConstraintScaleFactor="100"; 
	public static final String equalAngleConstraintScaleFactor="10000";
	public static final String constantAngleConstraintScaleFactor="10000";
	public static final String pointOverlapConstraintScaleFactor="100";
//	public static final String pointOnCircleConstraintScaleFactor="100";
	public static final String independentAngleConstraintScaleFactor="10";
	public static final String collinearPointsScaleFactor="1";
	public static final String perpendicularLinesScaleFactor="1"; 
	public static final String parallelLinesScaleFactor="1"; 
	
	
	//Per xyz pixels of line, we can afford 1 pixel of error
	//public static final double lineLengthErrorThreshold=40;
	public static final double lineLengthErrorThreshold=100;
	public static final double equalAnglesErrorThreshold=1;
	public static final double onLineOrNotAngleThreshold=1;
	public static final double independentAngleErrorThreshold=1;
	public static final double horizontalVerticalLineErrorThreshold=1;
	public static final double parallelSegmentsAnglesErrorThreshold=1;
	public static final double perpendicularSegmentsAnglesErrorThreshold=1;
	public static final double relativeAngleErrorThreshold=1;
	public static final double pointOverlapErrorThreshold=1;
	public static final double equalRadiusErrorThreshold=100;
	private final static double angleThreshold = 180 - equalAnglesErrorThreshold;
	
	
	public static void addCons2SegsAndRecogview(Constraint c, Segment[] segments)
	{
		if(c!=null)
		{
			for(int i=0;i<segments.length;i++)
				segments[i].addConstraint(c);
			GMethods.addConstraintToRecogView(c);
		}
	}
	
	
	public static AnchorPoint[] getAnchorPoints(Vector points)
	{
		AnchorPoint[] anchorPoints=new AnchorPoint[points.size()];
		for(int i=0;i<points.size();i++)
			anchorPoints[i]=(AnchorPoint)points.elementAt(i);
		return anchorPoints;
	}

	public static String[][] getpointStrings(AnchorPoint[] anchorPoints)
	{
		String[][] pointStrings=new String[anchorPoints.length][2];
		for(int i=0;i<anchorPoints.length;i++)
			pointStrings[i]=setVariable(anchorPoints[i]);
		return pointStrings;
		
	}
	
	protected static String[] setVariable(AnchorPoint pt)
	{
		String[] point = new String[2];
		point[0] = pt.getM_strId() + ".x";
		point[1] = pt.getM_strId() + ".y";
		return point;
	}

	public static Vector getUniquePointsForConnectedLines(SegLine seg1, SegLine seg2)
	{
		Vector v= new Vector();
		if (seg1.getM_start().equals(seg2.getM_start()) && !seg1.getM_end().equals(seg2.getM_end()))
		{
			v.add(seg1.getM_end());
			v.add(seg2.getM_end());
			v.add(seg1.getM_start());
		} 
		else if (seg1.getM_start().equals(seg2.getM_end()) && !seg1.getM_end().equals(seg2.getM_start()))
		{
			v.add(seg1.getM_end());
			v.add(seg2.getM_start());
			v.add(seg1.getM_start());
		}
		else if (seg1.getM_end().equals(seg2.getM_start()) && !seg1.getM_start().equals(seg2.getM_end()))
		{
			v.add(seg1.getM_start());
			v.add(seg2.getM_end());
			v.add(seg1.getM_end());
		}
		else if (seg1.getM_end().equals(seg2.getM_end()) && !seg1.getM_start().equals(seg2.getM_start()))
		{
			v.add(seg1.getM_start());
			v.add(seg2.getM_start());
			v.add(seg1.getM_end());
		}
		else
			;
		return v;
	}
	
	public static AnchorPoint getCommonPointBetweenLineAndCircularCurve(SegLine l,SegCircleCurve c)
	{
		if(l.getM_start() == c.getM_start() || l.getM_start() == c.getM_end() || l.getM_start() == c.getM_center())
			return l.getM_start();
		if(l.getM_end() == c.getM_start() || l.getM_end() == c.getM_end() || l.getM_end() == c.getM_center())
			return l.getM_end();
		return null;
	}
	
	public static Vector getAllIndependentConstraintsOfSegment(Segment seg)
	{
		//It must be cloned
		//Bug found on 9-5-2008 
		//I hadn't cloned it. So, when I was removing some element from the vector in the next stages
		//It removed the constraint from that actual segment
		Vector v = (Vector)seg.getM_constraints().clone();
		for(int i=0;i<v.size();)
			if(! ( v.elementAt(i) instanceof IndependentConstraint) )
				v.remove(i);
			else
				i++;
		return v;
	}

	public static Vector getAllIndependentPointConstraints(AnchorPoint ap)
	{
		Vector v = (Vector)ap.getConstraints().clone();
		for(int i=0;i<v.size();)
			if(! ( v.elementAt(i) instanceof IndependentPointConstraints) )
				v.remove(i);
			else
				i++;
		return v;
	}
	
	public static Vector getIndependentPointConstraints(AnchorPoint ap1,AnchorPoint ap2)
	{
		Vector v=(Vector)ap1.getConstraints().clone();
		IndependentPointConstraints ic;
		Vector result = new Vector();
		
		for(int i=0;i<v.size();i++)
		{
			if(v.elementAt(i) instanceof IndependentPointConstraints)
			{
				ic=(IndependentPointConstraints)v.elementAt(i);
				if(ic.getPoints().contains(ap2))
					result.add(ic);
			}
		}
		return result;
	}
	
	public static Vector getRelativeConstraintsBetween2Segments(Segment seg1,Segment seg2)
	{
		//We must clone it
		Vector v=(Vector)seg1.getM_constraints().clone();
		RelativeConstraint rc;
		Vector result = new Vector();
		
		for(int i=0;i<v.size();i++)
		{
			if(v.elementAt(i) instanceof RelativeConstraint)
			{
				rc=(RelativeConstraint)v.elementAt(i);
				if(rc.getM_seg1()==seg2 || rc.getM_seg2()==seg2)
					result.add(rc);
			}
		}
		return result;
	}
	
	public static Vector getPointSegmentConstraintsOfPoints(AnchorPoint ap)
	{
		Vector v=(Vector)ap.getConstraints().clone(); //Not sure if cloning is really required
		Vector result = new Vector();
		for(int i=0;i<v.size();i++)
		{
			if(v.get(i) instanceof PointSegmentConstraint)
			{
				PointSegmentConstraint p=(PointSegmentConstraint)v.get(i);
				if(p.getM_ap()==ap)
					result.add(p);
			}
		}
		return result;
	}
	
	public static boolean hasConstraint(Segment seg,Class className)
	{
		int i=seg.getM_constraints().size()-1;
		Constraint c;
		while(i>=0)
		{
			c=(Constraint)seg.getM_constraints().elementAt(i);
			if(c.getClass()==className)
				return true;
			i--;
		}
		return false;
	}
	
	public static RelativeConstraint getConstraintBetween2Segments(Segment seg1, Segment seg2, Class className)
	{
		Vector v=constraintsHelper.getConstraintsByType(seg1.getM_constraints(),className);
		RelativeConstraint rc;
		
		for(int i=0;i<v.size();i++)
		{
			if(v.elementAt(i) instanceof RelativeConstraint)
			{
				rc=(RelativeConstraint)v.elementAt(i);
				if(rc.getM_seg1()==seg2 || rc.getM_seg2()==seg2)
					return rc;
			}
		}
		return null;
	}
	
	public static Vector getConstraintsByType(Vector constraints,Class classname)
	{
		Vector matches = new Vector();
		Iterator iterator = constraints.iterator();
		while (iterator.hasNext())
		{
			Constraint cons = (Constraint) iterator.next();
			if((classname.equals(cons.getClass())))
				matches.add(cons);
		}
		return matches;
	}

	public static Constraint doesConstraintAlreadyExist(AnchorPoint ap1, Class className, AnchorPoint[] allPoints)
	{
		Vector allConstraints=getConstraintsByType(ap1.getConstraints(),className);
		for(int i=0;i<allConstraints.size();i++)
		{
			Constraint c=(Constraint)allConstraints.elementAt(i);
			if(checkPoints(c,allPoints))
				return c;
		}
		return null;
	}
	
	public static boolean checkPoints(Constraint c,AnchorPoint[] allPoints)
	{
		Vector pointsOfConstraint=c.getPoints();
		if(pointsOfConstraint.size()!=allPoints.length)
			return false;
		for(int i=0;i<allPoints.length;i++)
			if(!pointsOfConstraint.contains(allPoints[i]))
				return false;
		return true;
	}
	
	public static boolean withinRange(double value, double base, double range)
	{
		if(base>=0)
		{
			if(value<=base+range && value >=base-range)
				return true;
		}
/*		else
		{
			if(value>=base+range && value <=base-range)
				return true;
		}*/
		return false;
	}
	
	public static boolean areSlopesEqual(AnchorPoint p1,AnchorPoint p2,AnchorPoint p3,AnchorPoint p4,boolean doPrint)
	{
		double angle1=Maths.newAngleInDegrees(p1.getM_point(),p2.getM_point());
		double angle2=Maths.newAngleInDegrees(p3.getM_point(),p4.getM_point());
	//	///System.out.println(" \n\n\n Angles : " + angle1 + "  " + angle2);
		/*if(angle1<0)
			angle1 += 180;
		if(angle2<0)
			angle2 += 180;
		///System.out.println(" \n\n\n Angles : " + angle1 + "  " + angle2);
		if(Math.abs(angle1-angle2)<equalAnglesErrorThreshold || Math.abs(angle1-angle2) > 180 - equalAnglesErrorThreshold)
			return true;
		printConstraintSolvingFailure("constraintsHelper.areSlopesEqual" , "Angles : " + angle1 + "  " + angle2  +"  ", doPrint);
		return false;
		*/
		// added on 09-05-10
		// check conditions to change in angle symmetric across quadrants
		double resultant = 0.0;
		double tempAngle1 = 0.0;
		double tempAngle2 = 0.0;
		
		//added on 10-05-10
		//1. when angle1 == 0 
	//////////////////////////////////////////////////////////////////////////	
		if(Double.compare(angle1, 0.0) == 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = 0.0;
			}
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				resultant = angle2 - angle1;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			
			else if(Double.compare(angle2, 0.0) < 0){
				resultant = angle1 - angle2;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////////////////////		
		// if angle1 is positive
		else if(Double.compare(angle1, 0.0) > 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = angle1 - angle2;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				resultant = Math.abs(angle2 - angle1);
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			// TO - DO
			else if(Double.compare(angle2, 0.0) < 0){
				if((Double.compare(angle1, angleThreshold) == 0) || (Double.compare(angle1, angleThreshold) > 0)){
					tempAngle1 = 180 - angle1; 
				}
				else{
					tempAngle1 = angle1;
				}
				if((Double.compare(Math.abs(angle2), angleThreshold) == 0) || (Double.compare(Math.abs(angle2), angleThreshold) > 0)){
					tempAngle2 = 180 - Math.abs(angle2);
				}
				else{
					tempAngle2 = Math.abs(angle2);
				}
				resultant = tempAngle1 + tempAngle2;
				// to check in case of theta  and (theta - 180)
				if((Double.compare(Math.abs(resultant), angleThreshold) == 0) || (Double.compare(Math.abs(resultant), angleThreshold) > 0)){
					resultant = Math.abs(180 - resultant);
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////////////		
		// if angle1 is negative
		else if(Double.compare(angle1, 0.0) < 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = angle2 - angle1;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			// TO - DO
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				if((Double.compare(angle2, angleThreshold) == 0) || (Double.compare(angle2, angleThreshold) > 0)){
					tempAngle2 = 180 - angle2; 
				}
				else{
					tempAngle2 = angle2;
				}
				if((Double.compare(Math.abs(angle1), angleThreshold) == 0) || (Double.compare(Math.abs(angle1), angleThreshold) > 0)){
					tempAngle1 = 180 - Math.abs(angle1);
				}
				else{
					tempAngle1 = Math.abs(angle1);
				}
				resultant = tempAngle1 + tempAngle2;
				// to check in case of theta  and (theta - 180)
				if((Double.compare(Math.abs(resultant), angleThreshold) == 0) || (Double.compare(Math.abs(resultant), angleThreshold) > 0)){
					resultant = Math.abs(180 - resultant);
				}
			}
			
			else if(Double.compare(angle2, 0.0) < 0){
				resultant = Math.abs(angle1 - angle2);
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
		}
		
	//	///System.out.println("Resultant : " + resultant);
	//	///System.out.println("Absolute Resultant : " + Math.abs(resultant) + "Threshold" + equalAnglesErrorThreshold);
		if(Math.abs(resultant)<equalAnglesErrorThreshold){
			///System.out.println("true");
			return true;
		}
			
		printConstraintSolvingFailure("constraintsHelper.areSlopesEqual" , "Angles : " + angle1 + "  " + angle2  +"  ", doPrint);
		return false;
		
		
	}
	
	/**Function to check whether the slopes of two lines are equal
	 * @author Sunil Kumar
	 */
	
	// it uses atan2 to find angle with X-axis
	// which gives angle in the range of -180 to +180
	public static boolean areSlopesEqual(double x1,double y1,double x2,double y2,
										double x3,double y3,double x4,double y4, boolean doPrint){
		double angle1=Maths.AngleInDegrees(x1, y1, x2, y2);
		double angle2=Maths.AngleInDegrees(x3, y3, x4, y4);
		///System.out.println(" \n\n\n Angles : " + angle1 + "  " + angle2);
		/*if(angle1<0)
			angle1 += 180;
		if(angle2<0)
			angle2 += 180;
		///System.out.println(" \n\n\n Angles : " + angle1 + "  " + angle2);
		if(Math.abs(angle1-angle2)<equalAnglesErrorThreshold || Math.abs(angle1-angle2) > 180 - equalAnglesErrorThreshold)
			return true;
		printConstraintSolvingFailure("constraintsHelper.areSlopesEqual" , "Angles : " + angle1 + "  " + angle2  +"  ", doPrint);
		return false;
		*/
		// added on 09-05-10
		// check conditions to change in angle symmetric across quadrants
		double resultant = 0.0;
		double tempAngle1 = 0.0;
		double tempAngle2 = 0.0;
		
		//added on 10-05-10
		//1. when angle1 == 0 
	//////////////////////////////////////////////////////////////////////////	
		if(Double.compare(angle1, 0.0) == 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = 0.0;
			}
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				resultant = angle2 - angle1;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			
			else if(Double.compare(angle2, 0.0) < 0){
				resultant = angle1 - angle2;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////////////////////		
		// if angle1 is positive
		else if(Double.compare(angle1, 0.0) > 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = angle1 - angle2;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				resultant = Math.abs(angle2 - angle1);
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			// TO - DO
			else if(Double.compare(angle2, 0.0) < 0){
				if((Double.compare(angle1, angleThreshold) == 0) || (Double.compare(angle1, angleThreshold) > 0)){
					tempAngle1 = 180 - angle1; 
				}
				else{
					tempAngle1 = angle1;
				}
				if((Double.compare(Math.abs(angle2), angleThreshold) == 0) || (Double.compare(Math.abs(angle2), angleThreshold) > 0)){
					tempAngle2 = 180 - Math.abs(angle2);
				}
				else{
					tempAngle2 = Math.abs(angle2);
				}
				resultant = tempAngle1 + tempAngle2;
				// to check in case of theta  and (theta - 180)
				if((Double.compare(Math.abs(resultant), angleThreshold) == 0) || (Double.compare(Math.abs(resultant), angleThreshold) > 0)){
					resultant = Math.abs(180 - resultant);
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////////////		
		// if angle1 is negative
		else if(Double.compare(angle1, 0.0) < 0){
			if(Double.compare(angle2, 0.0) == 0){
				resultant  = angle2 - angle1;
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
			
			// if angle2 is greater than 0 find resultant
			// if resultant is greater than 179 because(180 - 179) is the error threshold
			else if(Double.compare(angle2, 0.0) > 0){
				if((Double.compare(angle2, angleThreshold) == 0) || (Double.compare(angle2, angleThreshold) > 0)){
					tempAngle2 = 180 - angle2; 
				}
				else{
					tempAngle2 = angle2;
				}
				if((Double.compare(Math.abs(angle1), angleThreshold) == 0) || (Double.compare(Math.abs(angle1), angleThreshold) > 0)){
					tempAngle1 = 180 - Math.abs(angle1);
				}
				else{
					tempAngle1 = Math.abs(angle1);
				}
				resultant = tempAngle1 + tempAngle2;
				// to check in case of theta  and (theta - 180)
				if((Double.compare(Math.abs(resultant), angleThreshold) == 0) || (Double.compare(Math.abs(resultant), angleThreshold) > 0)){
					resultant = Math.abs(180 - resultant);
				}
			}
			
			else if(Double.compare(angle2, 0.0) < 0){
				resultant = Math.abs(angle1 - angle2);
				if((Double.compare(resultant, angleThreshold) == 0) || (Double.compare(resultant, angleThreshold) > 0)){
					resultant = 180 - resultant;
				}
			}
		}
		
		///System.out.println("Resultant : " + resultant);
		///System.out.println("Absolute Resultant : " + Math.abs(resultant) + "Threshold" + equalAnglesErrorThreshold);
		if(Math.abs(resultant)<equalAnglesErrorThreshold){
			///System.out.println("true");
			return true;
		}
			
		printConstraintSolvingFailure("constraintsHelper.areSlopesEqual" , "Angles : " + angle1 + "  " + angle2  +"  ", doPrint);
		return false;
		
		
	}
	
	public static boolean areLinesPerpendicular(Point2D p1,Point2D p2,Point2D p3,Point2D p4,double errorThreshold,boolean doPrint)
	{
		double angle1=Maths.angleInDegrees(p1,p2);
		double angle2=Maths.angleInDegrees(p3,p4);
		if(angle1<0)
			angle1+=180;
		if(angle2<0)
			angle2+=180;
		if(errorThreshold == -1)
			errorThreshold = perpendicularSegmentsAnglesErrorThreshold;
		
//		///System.out.println("constraintsHelper.areLinesPerpendicular. Angles : " + angle1 + "  " + angle2  +"  ");
		if(constraintsHelper.withinRange(Math.abs(angle1-angle2),90,errorThreshold))//<=GConstants.parallelSegmentsAnglesErrorThreshold)
			return true;
		printConstraintSolvingFailure("constraintsHelper.areLinesPerpendicular","Angles : " + angle1 + "  " + angle2  +"  ",doPrint);
		return false;
	}
	
	public static boolean areLinesPerpendicular(AnchorPoint p1,AnchorPoint p2,AnchorPoint p3,AnchorPoint p4,double errorThreshold,boolean doPrint)
	{
		return areLinesPerpendicular(p1.getM_point(),p2.getM_point(),p3.getM_point(),p4.getM_point(),errorThreshold,doPrint);
	}

	public static boolean onLineOrNot(AnchorPoint p,AnchorPoint start,AnchorPoint end,boolean doPrint)
	{
		//The new point is overlapping with any of the end points of the line
		//In that case, don't look for the slopes, just assume that the point is on the line
		if(p.distance(start)<constraintsHelper.pointOverlapErrorThreshold || p.distance(end)<constraintsHelper.pointOverlapErrorThreshold)
			return true;

		return areSlopesEqual(start,p,end,p,false);
/*		double angle1=Maths.angle( p.getM_point() , start.getM_point() );
		double angle2=Maths.angle( p.getM_point() , end.getM_point() );
		
		if(Math.abs(angle1-angle2)<constraintsHelper.onLineOrNotAngleThreshold)
			return true;
		printConstraintSolvingFailure("constraintsHelper.onLineOrNot"," One angle : " + angle1 + " Two angle : " + angle2,doPrint);
		return false;*/
	}

	public static boolean onCircularCurveOrNot(AnchorPoint start,AnchorPoint end,AnchorPoint center,AnchorPoint p,boolean doPrint)
	{
		double distanceNew=center.getM_point().distance(p.getM_point());
		double distanceStart=center.getM_point().distance(start.getM_point());
		double distanceEnd=center.getM_point().distance(end.getM_point());
		double errorThreshold=distanceStart/constraintsHelper.lineLengthErrorThreshold;
		if(constraintsHelper.withinRange(distanceNew,distanceStart,errorThreshold) && constraintsHelper.withinRange(distanceNew,distanceEnd,errorThreshold))
			return true;
		printConstraintSolvingFailure("constraintsHelper.onCircularCurveOrNot"," New distance: "+ distanceNew + " Start: " + distanceStart + " End: " + distanceEnd,doPrint);
		return false;

	}

	public static boolean areAnglesEqual(double angle1,double angle2,boolean doPrint)
	{
		if(angle1<0)
			angle1=-angle1;
		if(angle2<0)
			angle2=-angle2;
		if(Math.abs(angle1-angle2)<constraintsHelper.equalAnglesErrorThreshold)
			return true;
		printConstraintSolvingFailure("constraintsHelper.areAnglesEqual"," One angle : " + angle1 + " , Other angle : " + angle2,doPrint);
		return false;
	}
	
	public static boolean areRadiusEqual(double r1, double r2, boolean doPrint)
	{
		double errorThreshold = r1 / equalRadiusErrorThreshold;
		if(constraintsHelper.withinRange(r2,r1,errorThreshold))
			return true;
		//if(Math.abs(r1-r2) < equalRadiusErrorThreshold)
		//	return true;
		printConstraintSolvingFailure("constraintsHelper.areRadiusEqual"," One radius : " + r1 + " , Other radius : " + r2,doPrint);
		return false;
	}
	
	public static boolean areLengthsEqual(AnchorPoint p1,AnchorPoint p2,AnchorPoint p3,AnchorPoint p4,boolean doPrint)
	{
		double distance1=p1.getM_point().distance( p2.getM_point());
		double distance2=p3.getM_point().distance( p4.getM_point());
		double errorThreshold=distance1/constraintsHelper.lineLengthErrorThreshold;
		if(constraintsHelper.withinRange(distance2,distance1,errorThreshold))
			return true;
		printConstraintSolvingFailure("constraintsHelper.areLengthsEqual"," One length : " + distance1 + " , Other length : " + distance2,doPrint);
		return false;
	}
	
	public static boolean independentLengthSatisfied(AnchorPoint p1,AnchorPoint p2, double length,boolean doPrint)
	{
		double distance1=p1.getM_point().distance( p2.getM_point());
		double errorThreshold=distance1/constraintsHelper.lineLengthErrorThreshold;
		if(constraintsHelper.withinRange(distance1,length,errorThreshold))
			return true;
		printConstraintSolvingFailure("constraintsHelper.independentLengthSatisfied"," Desired : " + length + " , Actual : " + distance1,doPrint);
		return false;
	}
	
	public static boolean pointsOverlap(AnchorPoint p1,AnchorPoint p2)
	{
		if(p1.getM_point().distance(p2.getM_point())<constraintsHelper.pointOverlapErrorThreshold)
			return true;
		return false;
	}
	
	public static boolean arePointsUnique(Constraint c)
	{
		Vector points=c.getPoints();
		for(int i=0;i<points.size();i++)
			for(int j=i+1;j<points.size();j++)
				if(points.get(i)==points.get(j))
					return false;
		return true;
	}
	
	public static boolean arePointsUnique(AnchorPoint[] a)
	{
		for(int i=0;i<a.length;i++)
			for(int j=i+1;j<a.length;j++)
				if(a[i]==a[j])
					return false;
		return true;
		
	}
	public static void printConstraintSolvingFailure(String functionName, String str, boolean doPrint)
	{
		if(doPrint)
		{
			///System.out.println("CONSTRAINT SOLVING FAILURE!!! in <" + functionName + "> : " + str);
			///System.out.println("");
		}
	}


	public static Constraint getNoMergeConstraintBetweenPoints(AnchorPoint ap1,AnchorPoint ap2)
	{
		Vector v = getConstraintsByType(ap1.getConstraints(),NoMergeConstraint.class);
		NoMergeConstraint c;
		for(int i=0;i<v.size();i++)
		{
			c = (NoMergeConstraint)v.get(i);
			if(c.points.contains(ap2))
				return c;
		}
		return null;
	}
	
	//Removes the constraints from the system
	public static void removeConstraints(Vector constraints)
	{
		for(int i=0;i<constraints.size();i++)
			((Constraint)constraints.get(i)).remove();
	}


	/**
	 * Returns the list of constraint associated with these points
	 * @param ptsVector the points for whom all the associated constraints are to be detected
	 * @return list of constraints 
	 */
	public static Vector getListOfConstraints(Vector ptsVector)
	{
		// this vector will contain all the contraints to be returned
		Vector allConstraints = new Vector();
	
		// first add all the constraints directly associated to these points
		Iterator iter = ptsVector.iterator();
		while (iter.hasNext())
			constraintsHelper.addAllIgnoreDuplicates(  allConstraints  ,  ((AnchorPoint)iter.next()).getConstraints()  );
		
		// starting with these initial set of constraints recursively find all the constraints 
		// for all the affected points: NOTE size of allConstraints will keep on increasing
		for (int i = 0; i < allConstraints.size(); i++)
		{
			Constraint c = (Constraint) allConstraints.elementAt(i);
			iter = c.getPoints().iterator();
			while (iter.hasNext())
			{
				AnchorPoint ap = (AnchorPoint) iter.next();
				//If this point has already been considered, continue
				if(ptsVector.contains(ap))
					continue;
				constraintsHelper.addAllIgnoreDuplicates(  allConstraints  ,  ap.getConstraints()  );
			}
		}
		
		//Do not return the deleted constraints. We do not want to add the error equations of them.
		for(int i=0;i<allConstraints.size();)
		{
			Constraint c = (Constraint)allConstraints.get(i);
			if(c.isDeleted())
				allConstraints.remove(i);
			else
				i++;
		}
		
		return allConstraints;
	}


	public static void addAllIgnoreDuplicates(Vector source, Vector destination)
	{
		int destinationSize = destination.size();
		for(int i=0;i<destinationSize;i++)
		{
			Object o = destination.get(i);
			if(! (source.contains(o)) )
				source.add(o);
		}
	}


	public static Vector getAllAnchorPointsOfConstraints(Vector constraints)
	{
		Vector result = new Vector();
		
		for (Object o : constraints) {
			Constraint co = (Constraint)o ;
			Vector points = co.getPoints() ;
			addAllIgnoreDuplicates(  result  , points );
		}
		return result;
	}
	

	public static Vector minus(Vector a,Vector b)
	{
		Vector temp = (Vector)a.clone();
		temp.removeAll(b);
		return temp;
	}
	
	public static Vector minusInverse(Vector a,Vector b)
	{
		Vector temp = minus(a,b);
		return minus(a,temp);
	}
	
	public static boolean haveCommonParent(AnchorPoint a, AnchorPoint b)
	{
		Vector v1 = a.getAllParents();
		Vector v2 = b.getAllParents();
		for(int i=0;i<v1.size();i++)
			if(v2.contains(v1.get(i)))
				return true;
		return false;
	}
	
/*(	
	public static Vector getPointOnSegmentEQs(Segment seg,AnchorPoint p)
	{
		Vector v=new Vector();
		if(seg instanceof SegLine)
		{
			SegLine l=(SegLine)seg;
			v.add(p);
			v.add(l.getM_start());
			v.add(l.getM_end());
			String[][] pointStrings=getpointStrings(getAnchorPoints(v));
			return constraintEquations.getPointOnLineEQ(pointStrings[0],pointStrings[1],pointStrings[2]);
		}
		else if(seg instanceof SegCircleCurve)
		{
			SegCircleCurve c=(SegCircleCurve)seg;
			v.add(c.getM_center());
			v.add(c.getM_start());
			v.add(c.getM_end());
			v.add(p);
			String[][] pointStrings=getpointStrings(getAnchorPoints(v));
			return constraintEquations.getPointOnCircularCurveEQ(pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]);
		}
		else if(seg instanceof SegPoint)
		{
			SegPoint s=(SegPoint)seg;
			v.add(s.getM_pt());
			v.add(p);
			String[][] pointStrings=getpointStrings(getAnchorPoints(v));
			return constraintEquations.getPointOnPointEQ(pointStrings[0],pointStrings[1]);
		}
		else
			return null;
	}
	
	public static Vector getPointOnSegmentPDs(String pt, String xory,Segment seg,AnchorPoint p)
	{
		Vector v=new Vector();
		if(seg instanceof SegLine)
		{
			SegLine l=(SegLine)seg;
			v.add(p);
			v.add(l.getM_start());
			v.add(l.getM_end());
			AnchorPoint[] anchorPoints=getAnchorPoints(v);
			String[][] pointStrings=getpointStrings(anchorPoints);
			return constraintEquations.getPointOnLinePD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],pointStrings[0],pointStrings[1],pointStrings[2]);
		}
		else if(seg instanceof SegCircleCurve)
		{
			SegCircleCurve c=(SegCircleCurve)seg;
			v.add(c.getM_center());
			v.add(c.getM_start());
			v.add(c.getM_end());
			v.add(p);
			AnchorPoint[] anchorPoints=getAnchorPoints(v);
			String[][] pointStrings=getpointStrings(getAnchorPoints(v));
			return constraintEquations.getPointOnCircularCurvePD(pt,xory,anchorPoints[0],anchorPoints[1],anchorPoints[2],anchorPoints[3],pointStrings[0],pointStrings[1],pointStrings[2],pointStrings[3]);
		}
		else if(seg instanceof SegPoint)
		{
			SegPoint s=(SegPoint)seg;
			v.add(s.getM_pt());
			v.add(p);
			AnchorPoint[] anchorPoints=getAnchorPoints(v);
			String[][] pointStrings=getpointStrings(getAnchorPoints(v));
			return constraintEquations.getPointOnPointPD(pt,xory,anchorPoints[0],anchorPoints[1],pointStrings[0],pointStrings[1]);
		}
		else
			return null;
	}
	*/
	/*	public static pointOnSegmentConstraint getPointOnSegmentConstraint(Segment seg,AnchorPoint ap,int category)
	{
		if(seg instanceof SegPoint)
		{
			pointOnPointConstraint pc=new pointOnPointConstraint(seg,ap,category);
			return pc;
		}
		else if(seg instanceof SegLine)
		{
			pointOnLineConstraint lc=new pointOnLineConstraint(seg,ap,category);
			return lc;
		}
		else if(seg instanceof SegCircleCurve)
		{
			pointOnCircularCurveConstraint cc=new pointOnCircularCurveConstraint(seg,ap,category);
			return cc;
		}
		else
		{
			///System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Segment is corrupted !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return null;
		}
	}
*/
	
/*	public static TangentConstraint getTangentConstraint(Segment seg1, Segment seg2, Point2D pt1, Point2D pt2, int category)
	{
		if(seg1 instanceof SegLine && seg2 instanceof SegCircleCurve)
		{
			lineCircularCurveTangencyConstraint lc=new lineCircularCurveTangencyConstraint((SegLine)seg1,(SegCircleCurve)seg2,pt1,pt2,category);// pc=new pointOnPointConstraint(seg,ap);
			return lc;
		}
		else if(seg1 instanceof SegCircleCurve && seg2 instanceof SegLine)
		{
			lineCircularCurveTangencyConstraint lc=new lineCircularCurveTangencyConstraint((SegLine)seg2,(SegCircleCurve)seg1,pt1,pt2,category);// pc=new pointOnPointConstraint(seg,ap);
			return lc;
		}
		else if(seg1 instanceof SegCircleCurve && seg2 instanceof SegCircleCurve)
		{
			twoCircularCurveTangencyConstraint cc=new twoCircularCurveTangencyConstraint((SegCircleCurve)seg1,(SegCircleCurve)seg2,pt1,pt2,category);
			return cc;
		}
		else
		{
			///System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Segment is corrupted !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return null;
		}
	}
*/

	
}