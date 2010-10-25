package dcad.process.recognition.segment;

import java.awt.geom.Point2D;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import dcad.Prefs;

public class DrawCADCircleFitter1
{
	
	double centerX=0,centerY=0;
	double radius;
	Matrix points;
	Matrix errors;
	Matrix Jacobian;

	int noOfPoints;
	int noOfErrorEquations;
	int noOfVariables = 3;
	
	double prevNorm = java.lang.Double.MAX_VALUE;
	double norm;

	public void doFit(double[][] stroke) throws Exception
	{
		int i;
		
		noOfPoints = stroke.length;
		noOfErrorEquations = 1;
		errors=new Matrix(noOfErrorEquations , 1);//new double[points.length-1];
		
		//Make an initial guess for the center
		for(i=0;i<noOfPoints;i++)
		{
			centerX+=stroke[i][0];
			centerY+=stroke[i][1];
		}
		errors.set(0,0,0);
		centerX /= (noOfPoints);
		centerY /= (noOfPoints);
		radius =Math.sqrt(  Math.pow( centerX - stroke[noOfPoints/2][0] , 2 ) + Math.pow( centerY - stroke[noOfPoints/2][1] , 2 )  );
		
		Jacobian = new Matrix(noOfErrorEquations,noOfVariables);
		for(i=0;i<noOfErrorEquations;i++)
			for(int j=0;j<noOfVariables;j++)
				Jacobian.set(i,j,0);
		
		points=new Matrix(noOfPoints,2);
		for(i=0;i<noOfPoints;i++)
		{
			points.set(i , 0 , stroke[i][0]);
			points.set(i , 1 , stroke[i][1]);
		}

		newton();
			
	}
	
	public void newton()
	{
		int i,k;
		int iterationsLimit=10;
		double x1,y1;
		
		for (k = 0; k<iterationsLimit ; k++)
		{
			double error = 0;
			double jacobianX, jacobianY, jacobianRadius;
			jacobianX = jacobianY = jacobianRadius = 0;
			for(i=0;i<noOfPoints;i++)
			{
				x1=points.get(i,0);
				y1=points.get(i,1);
				error +=  ( (centerX-x1)*(centerX-x1) + (centerY-y1)*(centerY-y1) - radius*radius );
				jacobianX += ( 2*(centerX-x1) );
				jacobianY += ( 2*(centerY-y1) );
				jacobianRadius += ( 2*radius );
			}
			errors.set(0,0,-1 * error);
			Jacobian.set( 0 , 0 , jacobianX  );
			Jacobian.set( 0 , 1 , jacobianY  );
			Jacobian.set( 0 , 2 , -jacobianRadius  );
		
			norm = errors.normF();
			///System.out.println("Norm : " + norm);

			if(norm==java.lang.Double.NaN)
				break;
			if (norm < 1 )
			{
				///System.out.println("-----------------------------Solution found d < min norm " + Prefs.getMinAcceptableNorm() + "-----------------------------");
				break;
			}
			
			if((norm >=(prevNorm - 1) && norm<=(prevNorm + 1)))
				break;

			Matrix dx;
			Matrix ainv;

			SingularValueDecomposition svd ;
			
			svd = new SingularValueDecomposition(Jacobian.transpose());
			
			Matrix u = svd.getU();
			Matrix s = svd.getS();
			Matrix v = svd.getV();
			
			// create a new matrix s1 with dimension same as s, and initilize it to 0
			Matrix s1 = new Matrix(s.getRowDimension(),s.getColumnDimension());
			for (int l = 0; l < s.getRowDimension(); l++)
				for (int n = 0; n < s.getColumnDimension(); n++)
					s1.set(l, n, 0.0);

			for(int l=0;l<s.getRowDimension();l++)
				if(s.get(l,l) >= 0.0001) // If value is greater than 0.001 then only take its reciprocal else don't take it.
					s1.set(l,l,1.0/s.get(l,l));
			
//			ainv=(v.times(s1)).times(u.transpose());
			ainv=(u.times(s1)).times(v.transpose());

			dx = ainv.times(errors);
			
			centerX += dx.get(0,0);
			centerY += dx.get(1,0);
			radius += dx.get(2,0);
			prevNorm = norm;
		}
	}

	public Point2D.Double getCenter()
	{
		return new Point2D.Double(centerX,centerY);
	}

	public double getRadius()
	{
		return radius;
	}
}