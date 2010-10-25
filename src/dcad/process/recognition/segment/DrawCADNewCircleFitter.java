package dcad.process.recognition.segment;

import java.awt.geom.Point2D;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import dcad.Prefs;

public class DrawCADNewCircleFitter
{
	
	double centerX=0,centerY=0;
	double radius;
	Matrix points;
	Matrix errors;
	Matrix Jacobian;

	int noOfPoints;
	int noOfErrorEquations;
	
	double prevNorm = java.lang.Double.MAX_VALUE;
	double norm;

	public void doFit(double[][] stroke) throws Exception
	{
		int i;
		
		noOfPoints = stroke.length;
		
		errors=new Matrix(stroke.length-1 , 1);//new double[points.length-1];
		noOfErrorEquations = errors.getRowDimension();
		
		//Make an initial guess for the center
		for(i=0;i<noOfErrorEquations;i++)
		{
			centerX+=stroke[i][0];
			centerY+=stroke[i][1];
			errors.set(i,0,0);
		}
		centerX+=stroke[i][0];
		centerY+=stroke[i][1];
		centerX/=(noOfPoints);
		centerY/=(noOfPoints);
		
		Jacobian = new Matrix(noOfErrorEquations,2);
		for(i=0;i<noOfErrorEquations;i++)
			for(int j=0;j<2;j++)
				Jacobian.set(i,j,0);
		
		points=new Matrix(noOfPoints*2,1);
		for(i=0;i<noOfPoints;i++)
		{
			points.set(i*2 , 0 , stroke[i][0]);
			points.set(i*2+1 , 0 , stroke[i][1]);
		}

		newton();
			
	}
	
	public void newton()
	{
		int i,k;
		int iterationsLimit=20;
		double x1,y1,x2,y2;
		
		for (k = 0; k<iterationsLimit ; k++)
		{
			for(i=0;i<noOfErrorEquations;i++)
			{
				x1=points.get(i*2,0);
				y1=points.get(i*2+1,0);
				x2=points.get((i+1)*2,0);
				y2=points.get((i+1)*2+1,0);
				errors.set(i,0, -1 * ( (x1-centerX)*(x1-centerX) + (y1-centerY)*(y1-centerY) - (x2-centerX)*(x2-centerX) - (y2-centerY)*(y2-centerY) ) );
				Jacobian.set( i , 0 , 2*(centerX-x1) + 2*(x2-centerX) );
				Jacobian.set( i , 1 , 2*(centerY-y1) + 2*(y2-centerY) );
			}
		
			norm = errors.normF();
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
			
			svd = new SingularValueDecomposition(Jacobian);
			
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
			
			ainv=(v.times(s1)).times(u.transpose());
			dx = ainv.times(errors);
			
			centerX += dx.get(0,0);
			centerY += dx.get(1,0);
			prevNorm = norm;
		}
		
		double min=Double.MAX_VALUE;
		int minIndex=-1;
		for(i=0;i<noOfErrorEquations;i++)
			if(errors.get(i,0)<min)
			{
				min=errors.get(i,0);
				minIndex=i;
			}
			
		x1=points.get(minIndex*2,0);
		y1=points.get(minIndex*2 + 1,0);
		radius =  Math.sqrt((x1-centerX)*(x1-centerX) + (y1-centerY)*(y1-centerY)); 
		
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