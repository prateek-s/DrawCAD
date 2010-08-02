package dcad.process.recognition.segment;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.Segment;
import dcad.util.GMethods;

public class CircularCurveRecognizer extends CurveRecognizer
{
	public static final double MIN_DIST_BETWEEN_TWO_POINTS = 5.0;
	public CircularCurveRecognizer(double[][] points)
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

		// make the starting and end point of the circular curve as the start and end points of the raw segment
		Point2D start = new Point2D.Double(m_points[0][0], m_points[0][1]);
		Point2D end = new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]);

		//CircleFitter cf = new CircleFitter();
		//DrawCADNewCircleFitter cf=new DrawCADNewCircleFitter();
		DrawCADCircleFitter cf=new DrawCADCircleFitter();
		//DrawCADCircleFitter1 cf=new DrawCADCircleFitter1();
		try
		{
			// check if the points are almost equidistant to each other,
			// otherwise the results MAY not be as desired.
/*			double[][] modPoints = padAdditionalPoints();
			
			cf.doFit(modPoints);
*/	
			cf.doFit(m_points);
			Point2D.Double center = cf.getCenter();
			double radius = cf.getRadius();

			double lsError = 0.0;
			for (int i = 0; i < m_points.length; i++)
			{
				lsError += Math.abs(Point2D.Double.distance(center.x, center.y, m_points[i][0], m_points[i][1]) - radius);
			}
			error = lsError;
			// setError(cf.getError());
		} catch (Exception e)
		{
			error = Double.MAX_VALUE;
		}
		confidance = calcConfidance();

/*		Point2D newStart = new Point2D.Double(m_points[0][0], m_points[0][1]);
		Point2D newEnd = new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]);
*/		Point2D center = new Point2D.Double(cf.getCenter().x, cf.getCenter().y);
		Point2D newCenter = new Point2D.Double(cf.getCenter().x, cf.getCenter().y);
		Point2D radius = new Point2D.Double(cf.getRadius(), cf.getRadius());

		// do processing such that the start and end points of the original stroke are the start and end points circular arc
		Line2D.Double seLine = new Line2D.Double(start, end);
		double seDist = start.distance(end);
		double centerDist = Math.sqrt(((radius.getX()*radius.getX()) - ((seDist*seDist)/4)));

		if(centerDist >= 0)
		{
			Vector intersections = GMethods.CircleCircleIntersections(start.getX(), start.getY(), radius.getX(), end.getX(), end.getY(), radius.getX());
			if(intersections != null)
			{
				Point2D center1 = (Point2D)intersections.get(0);
				Point2D center2 = (Point2D)intersections.get(1);
				if(seLine.relativeCCW(center1) == seLine.relativeCCW(center))
				{
					newCenter = center1;
				}
				else if(seLine.relativeCCW(center2) == seLine.relativeCCW(center))
				{
					newCenter = center2;
				}
				else
				{
					newCenter = center1;
/*					newStart = new Point2D.Double(m_points[0][0], m_points[0][1]);
					newEnd = new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]);
*/				}
			}
			else
			{
				System.err.println("This should not happen");
			}
		}else
		{
			// mid point is the center
			newCenter = new Point2D.Double((start.getX()+end.getX())/2, (start.getY()+end.getY())/2);
		}
		
		if(newCenter.distance(center) > 2*Constraint.MAX_ALLOWED_CONNECT_GAP)
		{
			// change the start and the end values to the recognizes circle's end points.
			start = new Point2D.Double(m_points[0][0], m_points[0][1]);
			end = new Point2D.Double(m_points[m_points.length-1][0], m_points[m_points.length-1][1]);
		}
		else
		{
			// change the center to adjusted center
			center = newCenter;
		}
		
		Vector mod_pts = new Vector();

	
		mod_pts.add(start);
		mod_pts.add(end);
		mod_pts.add(center);
		mod_pts.add(radius);
		m_modifiedPoints = mod_pts;
		
		// return the confidance value
		return getConfidance();
	}

	private double[][] padAdditionalPoints()
	{
		Vector newPoints = new Vector();
		double[] prevPoint = new double[2]; 
		prevPoint[0] = m_points[0][0];
		prevPoint[1] = m_points[0][1];
		newPoints.add(prevPoint);
		for(int i=1; i<m_points.length; i++)
		{
			double[] nextPoint = new double[2]; 
			nextPoint[0] = m_points[i][0];
			nextPoint[1] = m_points[i][1];
			newPoints.addAll(getIntermediatePoints(prevPoint, nextPoint));
			prevPoint = nextPoint;
		}
		
		double[][] returnPoints = new double[newPoints.size()][2];
		for(int i=0; i<newPoints.size(); i++)
		{
			double[] element = (double[]) newPoints.get(i);
			returnPoints[i][0] = element[0];
			returnPoints[i][1] = element[1];
		}
		return returnPoints;
	}
	
	private Vector getIntermediatePoints(double[] prevPt, double[] nextPt)
	{
		Vector returnPoints = new Vector();
		double distance = Math.sqrt(((prevPt[0] - nextPt[0])*(prevPt[0] - nextPt[0]) + (prevPt[1] - nextPt[1])*(prevPt[1] - nextPt[1]))); 
		
		if(distance > MIN_DIST_BETWEEN_TWO_POINTS)
		{
			double[] midPt = new double[2];
			midPt[0] = (prevPt[0]+nextPt[0])/2;
			midPt[1] = (prevPt[1]+nextPt[1])/2;
			returnPoints.addAll(getIntermediatePoints(prevPt, midPt));
			returnPoints.addAll(getIntermediatePoints(midPt, nextPt));
//			interPt[0] = (prevPt[0]*(distance-1)+nextPt[0])/distance;
//			interPt[1] = (prevPt[1]*(distance-1)+nextPt[1])/distance;;
		}
		else
		{
			returnPoints.add(nextPt);
		}
		return returnPoints;
	}
	
	public double getRadius()
	{
		Vector modPts = m_modifiedPoints;
		if(modPts != null)
		{
			Point2D point = (Point2D)modPts.get(3);
			return point.getX();
		}
		return -1;
	}
	
	public Point2D getCenter()
	{
		Vector modPts = m_modifiedPoints;
		if(modPts != null)
		{
			Point2D point = (Point2D)modPts.get(2);
			return point;
		}
		return null;
	}
	
	public Segment getSegment()
	{
		SegCircleCurve seg=new SegCircleCurve(m_modifiedPoints);
//ISHWAR		seg.beautifyForAngle();
		
		return seg;//new SegCircleCurve(getM_modifiedPoints());
	}

}
