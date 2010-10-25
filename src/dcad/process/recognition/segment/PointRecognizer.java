package dcad.process.recognition.segment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;


public class PointRecognizer extends SegmentRecognitionScheme
{
	public PointRecognizer(double[][] points)
	{
		super(points);
		// TODO Auto-generated constructor stub
	}

	public double approximate()
	{
		// the segment is a point if all the points are ard the centriod of the segment
		// get the centriod of the segment
		double[] centriod = GMethods.getCetriod(m_points);
//ISHWAR		///System.out.println("centriod: "+centriod[0]+", "+centriod[1]);
		
		
		double lsError = 0.0;
		// check if all points are within PT_WINDOW distance with centriod.
		for (int i = 0; i < m_points.length; i++)
		{
			// check if the centriod is out of range
			if((Math.abs(m_points[i][0]-centriod[0]) > PT_WINDOW)||((Math.abs(m_points[i][1]-centriod[1]) > PT_WINDOW)))
			{
				// This may not be a point, add extra distance to the error 
				double extraDist = Point.distance(centriod[0], centriod[1], m_points[i][0], m_points[i][1]) - PT_WINDOW;
				lsError += (extraDist > 0)? extraDist:0;
			}
			else
			{
				// do nothing as point is within the window.. so continue
			}
		}
		error = lsError;
		
		// TODO: for now
		confidance = calcConfidance();
		
		// set the modified points, these would be returned in case point confidance is the highest.
		Vector mod_pts = new Vector();
		
/*		// set the first point as the modified recognized point
		mod_pts[0][0] = m_points[0][0];
		mod_pts[0][1] = m_points[0][1];
*/
		// set the centriod as the modified recognized point. 
		mod_pts.add(new Point2D.Double(centriod[0], centriod[1]));
		
		m_modifiedPoints = mod_pts;
		
		// return the confidance value
		return getConfidance();
	}

	public Segment getSegment()
	{
		return new SegPoint(m_modifiedPoints);
	}
}
