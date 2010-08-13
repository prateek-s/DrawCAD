package dcad.process.preprocess;

import java.util.Vector;

public abstract class SegmentPtDetectionScheme
{
	public static int DEF_ACCEPTABLE_VALUE = -1;
	protected double[] m_data = null;

	protected double[] getM_data()
	{
		return m_data;
	}

	protected void setM_data(double[] data)
	{
		m_data = data;
	}

	protected abstract void reset();

	protected abstract double getThreshold();

	protected abstract double getMean();

	protected abstract void normalize(double threshold);

	protected abstract Vector detectSegmentPoints();

	protected abstract double getScalingFactor();
}