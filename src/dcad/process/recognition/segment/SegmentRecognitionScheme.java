package dcad.process.recognition.segment;

import java.util.Vector;

import dcad.model.geometry.segment.Segment;

public abstract class SegmentRecognitionScheme
{
	// public members
	public static final int ACCURACY = 1000;

	// A line of length <= 2*(PT_WINDOW)+1 pixels will be considered as point
	public static final int PT_WINDOW = 2;
	
	// protected members
	protected double confidance;
	protected double error;
	protected double[][] m_points;
	protected Vector m_modifiedPoints;

	// constructor
	public SegmentRecognitionScheme(double[][] points)
	{
		super();
		init(points);
		
	}
	
	// abstract methods
	public abstract double approximate();
	public abstract Segment getSegment();

	// implemented methods, some with default implementations.
	protected void init(double[][] points)
	{
		confidance = -Double.MAX_VALUE;
		error = Double.MAX_VALUE;
		m_points = null;
		m_modifiedPoints = new Vector();
		m_points = points;
	}

	public double getConfidance()
	{
		return confidance;
	}

	public double calcConfidance()
	{
		// default confidance is the negative of error. Therefore more the error less the confidance.
		return -error;
	}
}