package dcad.process.recognition.segment;

import java.awt.geom.Point2D;
import java.util.Arrays;

import dcad.model.geometry.segment.Segment;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.ui.main.ToolBar;
public class SegmentRecognizer
{
	
	private static final int MAX_CONF_FOR_POINT = -100;
	private static final int MAX_LENGTH_FOR_POINT = 20;
	private static final double LINE_CIRCLE_THRESHOLD = 1.0;
	private static final int MIN_PTS_FOR_CIRCLE = 20;
	private double[][] m_points = null;
	private double m_confidance = -Double.MAX_VALUE;
	private int m_segType = Segment.NONE; 
	private double m_segLen = 0.0;
	private Segment m_recogSeg = null;
	public LineRecognizer m_lineRecog = null;
	private SegmentRecognitionScheme m_pointRecog = null;
	private EllipticalCurveRecognizer m_ellipCurveRecog = null;
	public CircularCurveRecognizer m_circleCurveRecog = null;
	
	//02-06-10
	private DrawingView dv = null; 
	private final int CIRCULAR_ARC = 1;
	private final int LINE = 0;
	
	
	public SegmentRecognizer()
	{
		reset();
	}
	

	
	private void reset()
	{
		m_points = null;
		m_recogSeg = null;
		m_segLen = 0.0;
		//m_segType = Segment.NONE; 
		m_confidance = -Double.MAX_VALUE;
	}
	
	private void init(double[][] points)
	{
		reset();
		setM_points(points);
	}
	
	public SegmentRecognitionScheme recognizeSegment(double[][] ptMat)
	{
		// initilize the members elements
		init(ptMat);
//		System.out.println("SIZE: "+ptMat.length);
		SegmentRecognitionScheme brs = null;
		// System.out.println("inside recognize");
		
		if(m_points!=null)
		{
			// we have points, get the index of the best fit.
			brs = getBestMatch(); 
		
			m_confidance = brs.getConfidance();
				
			/*
			if(tBar.isConvertActiveBitSet()){
				tBar.setConvertActiveBit(INIT_VALUE);
				m_confidance = 200.0;
			}
			else{
			m_confidance = brs.getConfidance();
			}*/
		}
		
		return brs;
	}

	/**
	 * finds the best match for this segment from all the possibilities.
	 */
	private SegmentRecognitionScheme getBestMatch()
	{			
		
		// fix some order of evaluation to find out the type of the segment
		
			double [] confidance = new double[3];
			double pointConf = confidance[0] = isPoint();
			double lineConf = confidance[1] = isLine();
			double circleConf = confidance[2] = isCircle();
//		double ellipseConf = confidance[3] = isEllipse();
		// at this point all the recognition schemes are set, we need to choose the best one
			if(dv == null){
				dv = MainWindow.getDv();
			}
			
			if(dv.isSegmentConverted()){
				if(dv.isSegmentConvertedTo() == LINE){
					m_segType = Segment.LINE;
				}
				else if(dv.isSegmentConvertedTo() == CIRCULAR_ARC){
					m_segType = Segment.CIRCLE;
				}
			}
			else{
				int lastIdx = confidance.length-1;
			
				Arrays.sort(confidance);
			
				if(confidance[lastIdx] == pointConf){
					// this could be a line as well as the next best approximation is line
					if (confidance[lastIdx-1] == lineConf){
						if((pointConf < MAX_CONF_FOR_POINT) && (getSegLength() > MAX_LENGTH_FOR_POINT)) m_segType = Segment.LINE;
						else m_segType = Segment.POINT;
					}
				
					else{
						m_segType = Segment.POINT;
					}
				}
				else if(confidance[lastIdx] == lineConf) m_segType = Segment.LINE;
			
				else if(confidance[lastIdx] == circleConf){
					double segkLen = getSegLength();
					// this could be a line as well as the next best approximation is line
					if (confidance[lastIdx-1] == lineConf){
						double radius = m_circleCurveRecog.getRadius();
						double ratio = segkLen/radius;
						//				System.out.println("ratio "+ratio);
						if(ratio > LINE_CIRCLE_THRESHOLD) m_segType = Segment.CIRCLE;
						else m_segType = Segment.LINE;
					}
					else{
						m_segType =  Segment.CIRCLE;
					}
				}
			}
		
/*		int bestIdx = GMethods.findMax(confidance);
		switch (bestIdx)
		{
		case 0:
			m_segType = Segment.POINT;
			break;

		case 1:
			m_segType = Segment.LINE;
			break;
		
		case 2:
			m_segType = Segment.CIRCLE;
			break;
		
		case 3:
			m_segType = Segment.ELLIPSE;
			break;

		default:
			m_segType = Segment.GENERAL; 
			break;
		}
*/		
		// at this point m_segType should be set.
		return getRecogScheme();
	}
	
	
	
	private SegmentRecognitionScheme getRecogScheme()
	{
		switch (m_segType)
		{
		case Segment.POINT:
			return m_pointRecog;

		case Segment.LINE:
			return m_lineRecog;
		
		case Segment.CIRCLE:
			return m_circleCurveRecog;
		
		case Segment.ELLIPSE:
			return m_ellipCurveRecog;

		case Segment.GENERAL:
			return null;

		default:
			return null;
		}
	}
	
	private double isPoint()
	{
		m_pointRecog = new PointRecognizer(m_points);
		double conf = m_pointRecog.approximate();
		//System.out.println("Point conf: "+conf);
		return conf;
	}
		
	public double isLine()
	{
		m_lineRecog = new LineRecognizer(m_points);
		double conf = m_lineRecog.approximate();
		//System.out.println("line conf: "+conf);
		return conf;
	}
	
	public double isCircle()
	{
		// get the total length of all the points
		if(m_points.length<MIN_PTS_FOR_CIRCLE)
		{
			// This segment has got too few points to be recognized as circle, check if the total distance of the segment is > threshold
			//If it is, the stroke was drawn very fast and enough points have not been tracked. So, don't consider it to be an arc. It's a line.
			//Changed the condition on 7-7-2008
			double totDist = 0;
			for (int i = 1; i < m_points.length; i++)
			{
				totDist += Math.pow(m_points[i][0]-m_points[i-1][0], 2) + Math.pow(m_points[i][1]-m_points[i-1][1], 2);
			}
			//It is a very big stroke possibly of a line.
			if(totDist > MIN_PTS_FOR_CIRCLE) return -Double.MAX_VALUE;
		}
		m_circleCurveRecog = new CircularCurveRecognizer(m_points);
		double conf = m_circleCurveRecog.approximate();
		//System.out.println("Circle conf: "+conf);
		return conf;
	}
	
	public double isEllipse()
	{
		m_ellipCurveRecog = new EllipticalCurveRecognizer(m_points);
		double conf = m_ellipCurveRecog.approximate();
		System.out.println("Ellipse: "+conf);
		return conf;
	}

	public double[][] getM_points()
	{
		return m_points;
	}

	public void setM_points(double[][] m_points)
	{
		this.m_points = m_points;
	}

/*	public Segment getSegmentApprox()
	{
		Segment approxSeg = null;
		// return proper segment based on the type recieved (This could be a general segment)
		switch (m_segType)
		{
		case Segment.POINT:
			approxSeg =  m_pointApp.getSegment();
			break;

		case Segment.LINE:
			approxSeg = m_lineApp.getSegment();
			break;

		case Segment.ARC:
			approxSeg = m_lineApp.getSegment();
			break;

		case Segment.CIRCLE:
			approxSeg = m_circleCurveApp.getSegment();
			break;

		case Segment.ELLIPSE:
			approxSeg = m_lineApp.getSegment();
			break;

		default:
			break;
		}
		setM_recogSeg(approxSeg);
		return approxSeg;
	}
*/
	public double getM_confidance()
	{
		return m_confidance;
	}

	public void setM_confidance(double m_confidance)
	{
		this.m_confidance = m_confidance;
	}

	public Segment getM_recogSeg()
	{
		return m_recogSeg;
	}

	public void setM_recogSeg(Segment seg)
	{
		m_recogSeg = seg;
	}

	public int getM_segType()
	{
		return m_segType;
	}

	public void setM_segType(int type)
	{
		m_segType = type;
	}
	
	private double getSegLength()
	{
		if(m_segLen == 0.0)
		{
			for (int i = 1; i < m_points.length; i++)
			{
				m_segLen += Point2D.distance(m_points[i-1][0], m_points[i-1][1], m_points[i][0], m_points[i][1]);
			}
		}
		return m_segLen;
	}
}
