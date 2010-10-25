package dcad.model.constraint;

import java.util.Vector;
import dcad.model.geometry.AnchorPoint;

public class constraintEquations
{
	
	public static String getYDifferenceEQ(String[] point1,String[] point2)
	{
		return "("+point1[1]+" - "+point2[1]+")";
	}
	
	public static String getYDifferenceSquaredEQ(String[] point1,String[] point2)
	{
		return "sqr"+getYDifferenceEQ(point1,point2); 
	}
	
	public static String getYDifferencePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2,String[] point1,String[] point2)
	{
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("y"))
			return "(1)";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("y"))
			return "(-1)";
		return "0.0";
	}
	
	public static String getYDifferenceSquaredPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2,String[] point1,String[] point2)
	{
		String str = "0.0";
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("y"))
			str = str + "+" + "(" + "2*("+point1[1]+" - "+point2[1]+")" + ")";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("y"))
			str = str + "+" + "(" + "2*("+point2[1]+" - "+point1[1]+")" + ")";
		return str;
	}

	public static String getXDifferenceEQ(String[] point1,String[] point2)
	{
		return "("+point1[0]+" - "+point2[0]+")";
	}
	
	public static String getXDifferenceSquaredEQ(String[] point1,String[] point2)
	{
		return "sqr"+getXDifferenceEQ(point1,point2); 
	}
	
	public static String getXDifferencePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2,String[] point1,String[] point2)
	{
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("x"))
			return "(1)";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("x"))
			return "(-1)";
		return "0.0";
	}

	public static String getXDifferenceSquaredPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2,String[] point1,String[] point2)
	{
		String str = "0.0";
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("x"))
			str = str + "+" + "(" + "2*("+point1[0]+" - "+point2[0]+")" +")";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("x"))
			str = str + "+" + "(" + "2*("+point2[0]+" - "+point1[0]+")" + ")";
		return str;
	}
	
	
	
	/************************************* Horizontal / Vertical Segment *************************************/
	
	public static String getHorizontalLineSegmentEQ(String[] point1, String[] point2)
	{
		return constraintsHelper.horizontalConstraintScaleFactor + "*("+getYDifferenceSquaredEQ(point1,point2)+")";
	}
	
	public static String getHorizontalLineSegmentPD(String pt, String xory,AnchorPoint p1, AnchorPoint p2,String[] point1, String[] point2)
	{
		return constraintsHelper.horizontalConstraintScaleFactor + "*("+getYDifferenceSquaredPD(pt,xory,p1,p2,point1,point2)+")";
	}

	public static String getVerticalLineSegmentEQ(String[] point1, String[] point2)
	{
		return constraintsHelper.verticalConstraintScaleFactor + "*("+getXDifferenceSquaredEQ(point1,point2)+")";
	}

	public static String getVerticalLineSegmentPD(String pt, String xory,AnchorPoint p1, AnchorPoint p2,String[] point1, String[] point2)
	{
		return constraintsHelper.verticalConstraintScaleFactor + "*("+getXDifferenceSquaredPD(pt,xory,p1,p2,point1,point2)+")";
	}
	
	
	
	/************************************* Distance / Line length *************************************/

	public static String getXDistanceEQ(String[] point1,String[] point2, double distance)
	{
		return getXDifferenceSquaredEQ(point1,point2) + " - (" + Double.toString(distance) + "*" + Double.toString(distance) + ")";
	}
	
	public static String getXDistancePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, String[] point1, String[] point2)
	{
		return getXDifferenceSquaredPD(pt,xory,p1,p2,point1,point2);
	}
	
	public static String getYDistanceEQ(String[] point1,String[] point2, double distance)
	{
		return getYDifferenceSquaredEQ(point1,point2) + " - (" + Double.toString(distance) + "*" + Double.toString(distance) + ")";
	}
	
	public static String getYDistancePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, String[] point1, String[] point2)
	{
		return getYDifferenceSquaredPD(pt,xory,p1,p2,point1,point2);
	}

	public static String getDistanceEQ(String[] point1, String[] point2)
	{
		return "(" + getXDifferenceSquaredEQ(point1,point2) + " + " + getYDifferenceSquaredEQ(point1,point2) + ")";
	}

	public static String getDistancePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, String[] point1, String[] point2)
	{
		return "(" + getXDifferenceSquaredPD(pt,xory,p1,p2,point1,point2) + " + " + getYDifferenceSquaredPD(pt,xory,p1,p2,point1,point2) + ")";
	}
	
	public static String getOverlapEQ(String[] point1, String[] point2)
	{
		return constraintsHelper.pointOverlapConstraintScaleFactor + "*(" + getDistanceEQ(point1,point2) + ")";
	}
	
	public static String getOverlapPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, String[] point1, String[] point2)
	{
		return constraintsHelper.pointOverlapConstraintScaleFactor + "*(" + getDistancePD(pt,xory,p1,p2,point1,point2)   + ")";
	}
	
	public static String getEqualRelativeLengthEQ(String[] point1, String[] point2, String[] point3,String[] point4)
	{
		return "(" + getDistanceEQ(point1,point2) + "-" + getDistanceEQ(point3,point4) + ")";
	}
	
	public static String getEqualRelativeLengthPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, AnchorPoint p4,String[] point1, String[] point2, String[] point3, String[] point4)
	{
		return "(" + getDistancePD(pt,xory,p1,p2,point1,point2) + "-" + getDistancePD(pt,xory,p3,p4,point3,point4) + ")";
	}
	
	public static String getLineLengthEQ(String[] point1, String[] point2, double distance)
	{
		return getDistanceEQ(point1,point2) + " - (" + Double.toString(distance) + "*" + Double.toString(distance) + ")";
	}
	
	public static String getLineLengthPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, String[] point1, String[] point2)
	{
		return getDistancePD(pt,xory,p1,p2,point1,point2);
	}

	public static String getMidPointXEQ(String[] point1,String[] point2, String[] point3)
	{
		//return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + "sqr(" + point1[0] + "+" + point2[0] + "- 2*" + point3[0] + ")" + ")";
		return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + point1[0] + "+" + point2[0] + "- 2*" + point3[0] + ")";
	}
	
	public static String getMidPointXPD(String pt, String xory, AnchorPoint p1,AnchorPoint p2,AnchorPoint p3,String[] point1,String[] point2, String[] point3)
	{
		String str = "0.0";
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("x"))
			//str = str + "+" + "(" + "2*(" + point1[0] + "+" + point2[0] + "- 2*" + point3[0] + ")" + ")";
			str = str + "+" + "(1)";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("x"))
			//str = str + "+" + "(" + "2*(" + point1[0] + "+" + point2[0] + "- 2*" + point3[0] + ")" + ")";
			str = str + "+" + "(1)";
		if(pt.equals(p3.getM_strId()) && xory.equalsIgnoreCase("x"))
			//str = str + "+" + "(" + "-4*(" + point1[0] + "+" + point2[0] + "- 2*" + point3[0] + ")" + ")";
			str = str + "+" + "(-2)";
		return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + str + ")";
	}
	
	public static String getMidPointYEQ(String[] point1,String[] point2, String[] point3)
	{
		//return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + "sqr(" + point1[1] + "+" + point2[1] + "- 2*" + point3[1] + ")" + ")";
		return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + point1[1] + "+" + point2[1] + "- 2*" + point3[1] + ")";
	}
	
	public static String getMidPointYPD(String pt, String xory, AnchorPoint p1,AnchorPoint p2,AnchorPoint p3,String[] point1,String[] point2, String[] point3)
	{
		String str = "0.0";
		if(pt.equals(p1.getM_strId()) && xory.equalsIgnoreCase("y"))
			//str = str + "+" + "(" + "2*(" + point1[1] + "+" + point2[1] + "- 2*" + point3[1] + ")" + ")";
			str = str + "+" + "(1)";
		if(pt.equals(p2.getM_strId()) && xory.equalsIgnoreCase("y"))
			//str = str + "+" + "(" + "2*(" + point1[1] + "+" + point2[1] + "- 2*" + point3[1] + ")" + ")";
			str = str + "+" + "(1)";
		if(pt.equals(p3.getM_strId()) && xory.equalsIgnoreCase("y"))
			//str = str + "+" + "(" + "-4*(" + point1[1] + "+" + point2[1] + "- 2*" + point3[1] + ")" + ")";
			str = str + "+" + "(-2)";
		return constraintsHelper.lineMidPointConstraintScaleFactor + "*(" + str + ")";
	}
	
	
	
	/************************************* Perpendicular lines *************************************/
	
	public static String getPerpendicularSegmentsEQ(String[] point1, String[] point2, String[] point3, String[] point4)
	{
		String str;
		str = constraintsHelper.perpendicularLinesScaleFactor + "*" + "((" + point1[1] + "-" + point2[1] + ") * (" + point3[1] + "-" + point4[1] + ")+(" + point1[0] + "-" + point2[0] + ") *  (" + point3[0] + "-" + point4[0] + "))";
		return str;
	}
	
	public static String getConnectedPerpendicularSegmentsEQ(String[] point1, String[] point2, String[] point3)
	{
		return getPerpendicularSegmentsEQ(point1,point2,point3,point2);
	}
	
	public static String getPerpendicularSegmentsPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, AnchorPoint p4, String[] point1, String[] point2, String[] point3, String[] point4)
	{
		String str = "0.0";
		int index = 0;
		if(xory.equals("y"))
			index = 1;
		if(pt.equals(p1.getM_strId()))
			str = str + "+" + "(" + point3[index] + "-" + point4[index] + ")";
		if(pt.equals(p2.getM_strId()))
			str = str + "+" + "(" + point4[index] + "-" + point3[index] + ")";
		if(pt.equals(p3.getM_strId()))
			str = str + "+" + "(" + point1[index] + "-" + point2[index] + ")";
		if(pt.equals(p4.getM_strId()))
			str = str + "+" + "(" + point2[index] + "-" + point1[index] + ")";

		return constraintsHelper.perpendicularLinesScaleFactor + "*(" + str + ")";
	}

	public static String getConnectedPerpendicularSegmentsPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3)
	{
		return getPerpendicularSegmentsPD(pt,xory,p1,p2,p3,p2,point1,point2,point3,point2);
	}

	
	
	/************************************* Parallel lines *************************************/
	
	public static String getParallelSegmentConstraintEQ(String[] point1, String[] point2,String[] point3,String[] point4)
	{
		return 	 constraintsHelper.parallelLinesScaleFactor + "*(" + "(" + point1[1] + "-" + point2[1] + ")*(" + point3[0] + "-" + point4[0] + ")-(" + point1[0] + "-" + point2[0] + ")*(" + point3[1] + "-" + point4[1] + "))";
	}
	
	public static String getParallelSegmentConstraintPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, AnchorPoint p4, String[] point1, String[] point2, String[] point3, String[] point4)
	{
		String str="0.0";
		
		if(xory.equals("x"))
		{
			if (pt.equals(p1.getM_strId()))
				str = str + "+" + "(" + point4[1] + "-" + point3[1] + ")";
			if (pt.equals(p2.getM_strId()))
				str = str + "+" + "(" + point3[1] + "-" + point4[1] + ")";
			if (pt.equals(p3.getM_strId()))
				str = str + "+" + "(" + point1[1] + "-" + point2[1] + ")";
			if (pt.equals(p4.getM_strId()))
				str = str + "+" + "(" + point2[1] + "-" + point1[1] + ")";
		}
		else
		{
			if (pt.equals(p1.getM_strId()))
				str = str + "+" + "(" + point3[0] + "-" + point4[0] + ")";
			if (pt.equals(p2.getM_strId()))
				str = str + "+" + "(" + point4[0] + "-" + point3[0] + ")";
			if (pt.equals(p3.getM_strId()))
				str = str + "+" + "(" + point2[0] + "-" + point1[0] + ")";
			if (pt.equals(p4.getM_strId()))
				str = str + "+" + "(" + point1[0] + "-" + point2[0] + ")";
		}
		
		return constraintsHelper.parallelLinesScaleFactor + "*(" + str + ")";
	}
	
	
	
	/************************************* Collinear points *************************************/

	public static Vector getCollinearPointsEQ(String[] point1, String[] point2, String[] point3)
	{
		Vector v=new Vector();
		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintEQ(point1,point2,point1,point3) +")");
//		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintEQ(point2,point1,point2,point3) +")");
//		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintEQ(point3,point1,point3,point2) +")");
		return v;
	}

	public static Vector getCollinearPointsPD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3)
	{
		Vector v=new Vector();
		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintPD(pt,xory,p1,p2,p1,p3,point1,point2,point1,point3) +")");
//		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintPD(pt,xory,p2,p1,p2,p3,point2,point1,point2,point3) +")");
//		v.add(constraintsHelper.collinearPointsScaleFactor + "*(" + getParallelSegmentConstraintPD(pt,xory,p3,p1,p3,p2,point3,point1,point3,point2) +")");
		return v;
	}
	
	
	
	/************************************* Point on segment *************************************/
	
	//Auxiliary point, Start of line, End of line
	public static Vector getPointOnLineEQ(String[] point1, String[] point2,String[] point3)
	{
		Vector v=new Vector();
		v.addAll(getCollinearPointsEQ(point1,point2,point3));
		
		//We are not worrying about this second condition...
/*		//The point should be on the line and NOT outside the line (still satisfying the slope equation)
		String str1="sqrt(" + getXDifferenceSquaredEQ(point2,point3) + ")";
		String str2="sqrt(" + getXDifferenceSquaredEQ(point1,point2) + ")";
		String str3="sqrt(" + getXDifferenceSquaredEQ(point1,point3) + ")";
		str1=str1 + "-" + str2 + "-" + str3;
//		v.add(str1);
		v.add("sqr("+str1+")");*/
		return v;		
	}
	
	public static Vector getPointOnLinePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3)
	{
		Vector v=new Vector();
		v.addAll(getCollinearPointsPD(pt,xory,p1,p2,p3,point1,point2,point3));
		
		//We are not worrying about this second condition...
/*		if(xory.equalsIgnoreCase("x"))
		{
			String str1="0.0",str2="0.0",str3="0.0",s1,s2;
			//Auxiliary point
			if(pt.equals(p1.getM_strId()))
			{
				str1=getXDifferenceEQ(point2,point1)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point1,point2) + ")";
				str2=getXDifferenceEQ(point3,point1)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point1,point3) + ")";
			}
			else if(pt.equals(p2.getM_strId())) // Start point
			{
				str1=getXDifferenceEQ(point2,point3)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point2,point3) + ")";
				str2=getXDifferenceEQ(point1,point2)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point1,point2) + ")";
			}
			else if(pt.equals(p3.getM_strId())) // End point
			{
				str1=getXDifferenceEQ(point3,point2)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point2,point3) + ")";
				str2=getXDifferenceEQ(point1,point3)+"/"+"sqrt(" + getXDifferenceSquaredEQ(point1,point3) + ")";
			}
			else
				///System.out.println("ERROR : constraintEquations !!!");
			s1="("+str1+")+("+str2+")";
//			v.add(s1);
			
			str1="sqrt(" + getXDifferenceSquaredEQ(point2,point3) + ")";
			str2="sqrt(" + getXDifferenceSquaredEQ(point1,point2) + ")";
			str3="sqrt(" + getXDifferenceSquaredEQ(point1,point3) + ")";
			s2="2*("+str1 + "-" + str2 + "-" + str3+")";
			v.add(s1+"*"+s2);
			return v;		
		}
		else
			v.add("0.0");*/
		return v;
	}
	
	//center, start, end, auxiliary point
	public static Vector getPointOnCircularCurveEQ(String[] point1, String[] point2,String[] point3,String[] point4)
	{
		Vector v=new Vector();
		String str1=constraintEquations.getEqualRelativeLengthEQ(point2,point1,point4,point1);
		v.add(str1);
//		String str2=constraintEquations.getEqualRelativeLengthEQ(point3,point1,point4,point1);
//		v.add(str2);
		
		// there has to be another constraint that the new point is still between the two points
		// /_213 = /_214 + /_413
		//String str3 = "("+constraintEquations.getAngleEQWithoutScaleFactor(point2, point1, point4)+") + ("+constraintEquations.getAngleEQWithoutScaleFactor(point4, point1, point3)+") - ("+constraintEquations.getAngleEQWithoutScaleFactor(point2, point1, point3)+")";
		//str3=constraintsHelper.pointOnCircleConstraintScaleFactor + "*("+str3+")";
//		///System.out.println("\n" + str3);
		//v.add(str3);
		return v;		
	}
	
	public static Vector getPointOnCircularCurvePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, AnchorPoint p4, String[] point1, String[] point2, String[] point3, String[] point4)
	{
		Vector v=new Vector();
		String str1 = constraintEquations.getEqualRelativeLengthPD(pt, xory, p2,p1,p4,p1,point2,point1,point4,point1);
		v.add(str1);
//		String str2 = constraintEquations.getEqualRelativeLengthPD(pt, xory, p3,p1,p4,p1,point3,point1,point4,point1);
//		v.add(str2);
		
		// there has to be another constraint that the new point is still between the two points
		// /_213 = /_214 + /_413
		//String str3 = "("+constraintEquations.getAnglePDWithoutScaleFactor(pt, xory, p2, p1, p4, point2, point1, point4)+") + ("+constraintEquations.getAnglePDWithoutScaleFactor(pt, xory, p4, p1, p3, point4, point1, point3)+") - ("+constraintEquations.getAnglePDWithoutScaleFactor(pt, xory, p2, p1, p3, point2, point1, point3)+")";//getAngleCCPD(pt, xory, p1, p2, p3, p4, point1, point2, point3, point4);
		//str3=constraintsHelper.pointOnCircleConstraintScaleFactor + "*("+str3+")";
//		///System.out.println("\nPartial - " + var + ": " + str3);
		//v.add(str3);
		
		return v;
	}

	
	
	/************************************* Angles *************************************/
	
	public static String getAngleEQWithoutScaleFactor(String[] point1, String[] point2, String[] point3)
	{
		String u = "(((" + point1[0] + " - " + point2[0] + ") * (" + point3[0] + "- " + point2[0] + ")) + ((" + point1[1] + " - " + point2[1] + ") * (" + point3[1] + "- " + point2[1] + "))) ";
		String w = "((sqr(" + point1[0] + " - " + point2[0] + ") + sqr(" + point1[1] + "- " + point2[1] + ")) * (sqr(" + point3[0]+ " -  " + point2[0]+ ") + sqr(" + point3[1] + " - " + point2[1] + ")))";
		return ( u + " / sqrt(" + w + ")" );
	}

	public static String getAnglePDWithoutScaleFactor(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3)
	{
		String str = "";
		String u = null, w = null;
		String u1 = null, w1 = null;
		if (pt.equals(p1.getM_strId()) || pt.equals(p2.getM_strId()) || pt.equals(p3.getM_strId()))
		{
			u = "(((" + point1[0] + " - " + point2[0] + ") * (" + point3[0] + "- " + point2[0] + ")) + ((" + point1[1] + " - " + point2[1] + ") * (" + point3[1] + "- " + point2[1] + "))) ";
			w = "((sqr(" + point1[0] + " - " + point2[0] + ") + sqr(" + point1[1] + "- " + point2[1] + ")) * (sqr(" + point3[0]+ " -  " + point2[0]+ ") + sqr(" + point3[1] + " - " + point2[1] + ")))";

			// For point 1
			if (pt.equals(p1.getM_strId()) && xory.equals("x"))
			{
				u1 = "(" + point3[0] + "  - " + point2[0] + " )";
				w1 = "(2 * (" + point1[0] + "  - " + point2[0] + ") * (sqr(" + point3[0] + " -  " + point2[0] + ") + sqr(" + point3[1] + " - " + point2[1] + ")))";
			}
			else if (pt.equals(p1.getM_strId()) && xory.equals("y"))
			{
				u1 = "(" + point3[1] + "  - " + point2[1] + " )";
				w1 = "(2 * (" + point1[1] + "  - " + point2[1] + ") * (sqr(" + point3[0] + " -  " + point2[0] + ") + sqr(" + point3[1] + " - " + point2[1] + ")))";
			}

			// For point 2
			else if (pt.equals(p2.getM_strId()) && xory.equals("x"))
			{
				u1 = "((" + point2[0] + "  - " + point3[0] + " )+(" + point2[0] + "  - " + point1[0] + " ))";
				w1 = "((2 * (" + point2[0] + "  - " + point1[0] + ") * (sqr(" + point3[0] + " -  " + point2[0] + ") + sqr(" + point3[1] + " - " + point2[1] + ")))+(2 * (" + point2[0] + "  - " + point3[0] + ") * (sqr(" + point1[0] + " -  " + point2[0] + ") + sqr(" + point1[1] + " - " + point2[1] + "))))";
			}
			else if (pt.equals(p2.getM_strId()) && xory.equals("y"))
			{
				u1 = "((" + point2[1] + "  - " + point3[1] + " )+(" + point2[1] + "  - " + point1[1] + " ))";
				w1 = "((2 * (" + point2[1] + "  - " + point1[1] + ") * (sqr(" + point3[0] + " -  " + point2[0] + ") + sqr(" + point3[1] + " - " + point2[1] + ")))+(2 * (" + point2[1] + "  - " + point3[1] + ") * (sqr(" + point1[0] + " -  " + point2[0] + ") + sqr(" + point1[1] + " - " + point2[1] + "))))";
			}

			// For point 3
			else if (pt.equals(p3.getM_strId()) && xory.equals("x"))
			{
				u1 = "(" + point1[0] + "  - " + point2[0] + " )";
				w1 = "(2 * (" + point3[0] + "  - " + point2[0] + ") * (sqr(" + point1[0] + " -  " + point2[0] + ") + sqr(" + point1[1] + " - " + point2[1] + ")))";
			}
			else if (pt.equals(p3.getM_strId()) && xory.equals("y"))
			{
				u1 = "(" + point1[1] + "  - " + point2[1] + " )";
				w1 = "(2 * (" + point3[1] + "  - " + point2[1] + ") * (sqr(" + point1[0] + " -  " + point2[0] + ") + sqr(" + point1[1] + " - " + point2[1] + ")))";
			}

			str = "((sqrt(" + w + ")*" + u1 + "- (" + u + "/(2*sqrt(" + w + ")))*" + w1 + ")/" + w +")";
		}
		if(str.length()==0)
			str = "0.0";

		return str;
		
	}

	public static String getEqualAngleEQ(String[] point1, String[] point2, String[] point3,String[] point4, String[] point5, String[] point6)
	{
		String str;
		str=getAngleEQWithoutScaleFactor(point1,point3,point2) + " - " + getAngleEQWithoutScaleFactor(point4,point6,point5);
		str=constraintsHelper.equalAngleConstraintScaleFactor + "*("+str+")";
		return str;
	}
	
	public static String getEqualAnglePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3, AnchorPoint p4, AnchorPoint p5, AnchorPoint p6, String[] point4, String[] point5, String[] point6)
	{
		String str;
		str=getAnglePDWithoutScaleFactor(pt, xory,p1,p3,p2,point1,point3,point2) + " - " +getAnglePDWithoutScaleFactor(pt, xory,p4,p6,p5,point4,point6,point5);
		str=constraintsHelper.equalAngleConstraintScaleFactor + "*("+str+")";
		return str;
	}

	public static String getConstantAngleEQ(String[] point1, String[] point2, String[] point3,double angle)
	{
		String str;
		str = getAngleEQWithoutScaleFactor(point1,point3,point2) + "- cos(" + String.valueOf(Math.toRadians(angle)) + ")";
		str=constraintsHelper.constantAngleConstraintScaleFactor + "*("+str+")";
		return str;
	}
	
	public static String getConstantAnglePD(String pt, String xory, AnchorPoint p1, AnchorPoint p2, AnchorPoint p3, String[] point1, String[] point2, String[] point3)
	{
		return 	constraintsHelper.constantAngleConstraintScaleFactor + "*("+getAnglePDWithoutScaleFactor(pt,xory,p1,p3,p2,point1,point3,point2)+")";
	}
	
	public static String getIndependentAngleEQ(String[] point1, String[] point2,double angle)
	{
		String str;
		//dy - tan(theta)*dx
		//str= "sqr(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
		str= "(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
		str=constraintsHelper.independentAngleConstraintScaleFactor + "*("+str+")";
		///System.out.println("Angle is : " + angle);
		///System.out.println(str);
		return str;
	}
	
	public static String getIndependentAnglePD(String pt, String xory, AnchorPoint p1,AnchorPoint p2,String[] point1, String[] point2, double angle)
	{
		String str = "";
		if (pt.equals(p2.getM_strId())  && xory.equals("y") )
		{
			//str= "-2*(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
			str= "-2";
		}
		else if (pt.equals(p1.getM_strId())  && xory.equals("y") )
		{
			//str= "2*(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
			str= "2";
		}
		else if (pt.equals(p2.getM_strId())  && xory.equals("x") )
		{
			//str= "-2*(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
			//str=str + "* tan(" + String.valueOf(Math.toRadians(angle)) + ")";
			str="-1 * tan(" + String.valueOf(Math.toRadians(angle)) + ")";
		}
		else if (pt.equals(p1.getM_strId())  && xory.equals("x") )
		{
			//str= "2*(" + getYDifferenceEQ(point1,point2) + " - (" + getXDifferenceEQ(point2,point1) + " *  tan(" + String.valueOf(Math.toRadians(angle)) +") ) )";
			//str=str + "* tan(" + String.valueOf(Math.toRadians(angle)) + ")";
			str="tan(" + String.valueOf(Math.toRadians(angle)) + ")";
		}
		else
			str="0.0";
		str=constraintsHelper.independentAngleConstraintScaleFactor + "*("+str+")";
		///System.out.println(str);
		return str;
	}

}