package dcad.util;

import java.awt.geom.Point2D;

import Jama.Matrix;

public class Maths
{
	/**
	 * Performs ODR and returns the slope of the line
	 * @param ptMat the 2D matrix (X column and Y column) of the points on which ODR is to be performed 
	 * @return double[] the slope of the regression line and its intersection with the y-axix
	 */
	public static double[] performODR(double[][] ptMat)
	{
		double[] odr = new double[2];
		odr[0] = odr[1] = 0.0;

		int len = ptMat.length;
		//System.out.println("windowLength "+len);
		if(len <= 1) return odr;
		
		double[][] A_mat = new double[len][2];
		double[][] Y_mat = new double[len][2];
		for(int i=0; i<len; i++)
		{
			// set the x coordinate as the first column of A
			A_mat[i][0] = ptMat[i][0];
			
			// set 1 as the first column of A
			A_mat[i][1] = 1;
			
			// set the y coordinate as Y
			Y_mat[i][0] = ptMat[i][1];
			Y_mat[i][1] = 0;
		}
		
		// TODO remove this code
/*		A_mat = new double[7][2];
		Y_mat = new double[7][2];

		A_mat[0][0] = 10;
		A_mat[0][1] = 1;
		Y_mat[0][0] = 20;
		Y_mat[0][1] = 0;

		A_mat[1][0] = 10;
		A_mat[1][1] = 1;
		Y_mat[1][0] = 30;
		Y_mat[1][1] = 0;
		
		A_mat[2][0] = 10;
		A_mat[2][1] = 1;
		Y_mat[2][0] = 40;
		Y_mat[2][1] = 0;

		A_mat[3][0] = 10;
		A_mat[3][1] = 1;
		Y_mat[3][0] = 50;
		Y_mat[3][1] = 0;

		A_mat[4][0] = 10;
		A_mat[4][1] = 1;
		Y_mat[4][0] = 60;
		Y_mat[4][1] = 0;
		
		// add 2 extra points
		A_mat[5][0] = 9;
		A_mat[5][1] = 1;
		Y_mat[5][0] = 40;
		Y_mat[5][1] = 0;

		A_mat[6][0] = 10;
		A_mat[6][1] = 1;
		Y_mat[6][0] = 70;
		Y_mat[6][1] = 0;
		
		
		
		GlobalMethods.printMat(A_mat);
		GlobalMethods.printMat(Y_mat);
		
*/
		// calculate inverse of (A'A)
		Matrix A = new Matrix(A_mat);
		Matrix At = A.transpose();
		Matrix At_A = At.times(A);
		Matrix At_A_inverse;
		try
		{
			At_A_inverse = At_A.inverse();	
			// calculate A'Y
			Matrix Y = new Matrix(Y_mat);
			Matrix At_Y = At.times(Y);
			
			// X0 = (inv(A'A))A'Y
			Matrix sol = At_A_inverse.times(At_Y);
//			System.out.println("slope = "+sol.get(0,0));
//			System.out.println("intersection = "+sol.get(1,0));
			odr[0] = sol.get(0,0);
			odr[1] = sol.get(1,0);
//			System.out.println("----------------------");
//			System.out.println("slope = "+sol.get(0,1));
//			System.out.println("intersection = "+sol.get(1,1));
		} catch (Exception e)
		{
			// A'A is a singular matrix so inverse not possible.
			// TODO: handle exception
			if(ptMat[ptMat.length-1][1] > ptMat[0][1]) odr[0] = odr[1] = Double.POSITIVE_INFINITY;
			else if (ptMat[ptMat.length-1][1] < ptMat[0][1]) odr[0] = odr[1] = Double.NEGATIVE_INFINITY;
			else odr[0] = odr[1] = 0.0;
			// e.printStackTrace();
		}

//		Matrix covMat = getCovarianceMat(ptMat);
//		covMat.print(2,5);
//		GlobalMethods.printArr(covMat.eig().getRealEigenvalues());
//		covMat.eig().getV().print(2,5);
		//System.out.println(slope);
		return odr;
	}
	
    public static double slope(Point2D p1, Point2D p2)
    {
    	return slope(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
	
    public static double slope(double x1, double y1, double x2, double y2)
    {
    	double slope;
    	if(Double.compare(x1,x2) == 0)
    	{
    		if(Double.compare(y1, y2) == 0) slope = Double.NaN;
    		else if(Double.compare(y1, y2) < 0) slope = Double.NEGATIVE_INFINITY;
    		else slope = Double.POSITIVE_INFINITY;
    	}
    	else 
    	    slope = ((double)(y1-y2)/(double)(x1-x2));
    	return slope;
    }
    
    public static double angle(double deltaY, double deltaX)
    {
    	return Math.atan2(deltaY, deltaX);
    }
    
    public static double angleInDegrees(Point2D p1, Point2D p2)
    {
    	return Math.toDegrees(angle(p1.getX(), p1.getY(), p2.getX(), p2.getY()));
//    	return anglenew((Point)p1, (Point)p2);
    }
	
    public static double newAngleInDegrees(Point2D p1,Point2D p2)
    {
    	double x1,x2,y1,y2;
    	x1 = p1.getX();
    	x2 = p2.getX();
    	y1 = p1.getY();
    	y2 = p2.getY();
    	if(x1==x2)
    		return 90;
    	if(y1==y2)
    		return 0;
/*    	if( x1 <= (x2+1) && x1 >= (x2-1) )
    		return 90;
    	if(y1 <= (y2+1) && y1 >= (y2-1) )
    		return 0;*/
    	return Math.toDegrees( Math.atan2( (p1.getY() - p2.getY()) , (p1.getX() - p2.getX() )) );
    	
    }
	/**Function to find angle in degrees
	 * @author Sunil Kumar
	 */
    //added on 11-05-10
    public static double AngleInDegrees(double x1, double y1, double x2, double y2)
    {
    	
    	if(x1==x2)
    		return 90;
    	if(y1==y2)
    		return 0;

    	return Math.toDegrees( Math.atan2( (y2 - y1) , (x2 - x1 )) );
    	
    }
    public static double angle(double x1, double y1, double x2, double y2)
    {
//    	double angle;
//    	if(x1 > x2)
//    	    angle = (double)((Math.atan(slope(x1, y1, x2, y2))) + Math.toRadians(180));
//    	else if(x1 < x2 && y1 > y2)
//    	    angle = (double)((Math.atan(slope(x1, y1, x2, y2))) + Math.toRadians(360));
//    	else if(x1 == x2 && y1 > y2)
//    	    angle = (double)(Math.toRadians(270));
//    	else
//    	    angle = (double)((Math.atan(slope(x1, y1, x2, y2))));
//    	return Math.toDegrees(angle);
    	
    	double dx = x2-x1;
    	double dy = y2-y1;
    	return Math.atan2(dy, dx);
//    	double angle = Math.atan2(dy, dx);
//    	if(angle < 0 ) return Math.PI*2 + angle;
//    	else return angle;
   }
    
//    public static double anglenew(java.awt.Point a, java.awt.Point b) {
//    	 
//        double dx = b.getX() - a.getX();
//        double dy = b.getY() - a.getY();
//        double angle = 0.0d;
// 
//        if (dx == 0.0) {
//            if(dy == 0.0)     angle = 0.0;
//            else if(dy > 0.0) angle = Math.PI / 2.0;
//            else              angle = (Math.PI * 3.0) / 2.0;
//        }
//        else if(dy == 0.0) {
//            if(dx > 0.0)      angle = 0.0;
//            else              angle = Math.PI;
//        }
//        else {
//            if(dx < 0.0)      angle = Math.atan(dy/dx) + Math.PI;
//            else if(dy < 0.0) angle = Math.atan(dy/dx) + (2*Math.PI);
//            else              angle = Math.atan(dy/dx);
//        }
////        return (angle * 180) / Math.PI;
//    	if(angle > 0 ) return Math.toDegrees(angle);
//    	else return Math.toDegrees(Math.PI*2 + angle);
//    }
    

}
