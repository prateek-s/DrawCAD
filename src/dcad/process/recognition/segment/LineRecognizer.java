package dcad.process.recognition.segment;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.util.Maths;

public class LineRecognizer extends SegmentRecognitionScheme
{
	public LineRecognizer(double[][] points)
	{
		super(points);
		// TODO Auto-generated constructor stub
		
		/************ MODIFIED ACHTUNG */
		
	}

	public double approximate()
	{
		if(m_points.length <= 1)
		{
			// return the default minimum confidance
			return getConfidance();
		}
		
		// perform recognition
		// Write the line recognition also
		double[] regression = performRegression();
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;
//		System.out.println("+++++++++++++++");
//		for (int i = 0; i < regression.length; i++)
//		{
//			System.out.println(regression[i]);
//		}
//		System.out.println("+++++++++++++++");
		if((regression[0] == Double.NEGATIVE_INFINITY)||(regression[0] == Double.POSITIVE_INFINITY)||(regression[0] == Double.NaN))
		{
			// The line is most probably parallel to the y axis. 
			// send the end point of the segment as the end points of the line formed.
			x1 = m_points[0][0];
			y1 = m_points[0][1];
			x2 = m_points[m_points.length-1][0];
			y2 = m_points[m_points.length-1][1];
		}
		else
		{
			// ODR went well. Calculate the end points of the line.
			x1 = 0.0;
			y1 = regression[1];
			x2 = ACCURACY;
			y2 = regression[0]*ACCURACY + regression[1]; // y= mx+c
		}
		
		Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
		
		// calculate error and send a confidance value? Least Squared Error
		double lsError = 0.0;
		for (int i = 0; i < m_points.length; i++)
		{
			lsError += line.ptLineDist(m_points[i][0], m_points[i][1]);
		}
		
		error = lsError;
		
		// TODO: for now
		confidance = calcConfidance();
		Vector mod_pts = new Vector();
		
/*		// set the modified points, these would be returned in case point confidance is the highest.
		// the endpoints of the line would be the only 2 points describing the line 
		// Project the endpoints of the oraginal segment to the line
		double p1Dist = line.ptLineDist(x1, y1);
		double p2Dist = line.ptLineDist(x2, y2);
		
		// find the perpendicular spl
		double perp_m = -ACCURACY/(line.y2 - line.y1);
		 
		 
		
		// c = y-mx
		double c1 = m_points[0][1] - perp_m*m_points[0][0];
		double c2 = m_points[m_points.length][1] - perp_m*m_points[m_points.length][0];
		
		Line2D.Double line_p1 = new Line2D.Double(0, c1, ACCURACY, perp_m*ACCURACY + c1);
		Line2D.Double line_p2 = new Line2D.Double(0, c2, ACCURACY, perp_m*ACCURACY + c2);
		
		// TODO: find the new points
		
*/		
		mod_pts.add(new Point2D.Double(m_points[0][0], m_points[0][1]));
		mod_pts.add(new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]));
		m_modifiedPoints = mod_pts;
		
		// return the confidance value
		return getConfidance();
	}
	
	private double[] performRegression()
	{
		// this is the regression performed on these set of points. It can be changed to any other type
		return Maths.performODR(m_points);
	}
	
	public Segment getSegment()
	{
		return new SegLine(m_modifiedPoints);
	}
}
