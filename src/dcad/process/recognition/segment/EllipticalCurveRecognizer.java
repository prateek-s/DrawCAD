package dcad.process.recognition.segment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import ij.measure.CurveFitter;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class EllipticalCurveRecognizer extends CurveRecognizer
{

	public EllipticalCurveRecognizer(double[][] points)
	{
		super(points);
		// TODO Auto-generated constructor stub
	}

	public double approximate()
	{
		if(m_points.length <= 1)
		{
			// return the default minimum confidance
			return getConfidance();
		}

		double xPts [] = GMethods.getCoords(m_points, 0);
		double yPts [] = GMethods.getCoords(m_points, 1);
			
		CurveFitter cf = new CurveFitter(xPts, yPts);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		error = GMethods.findSumIgnoreSign(cf.getResiduals());
		confidance = calcConfidance();

		Vector mod_pts = new Vector();
		mod_pts.add(new Point2D.Double(m_points[0][0], m_points[0][1]));
		mod_pts.add(new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]));
		m_modifiedPoints = mod_pts;
		
		// return the confidance value
		return getConfidance();
	}

	public Segment getSegment()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
