package dcad.process.preprocess;


import java.util.Vector;

import dcad.Prefs;
import dcad.util.GMethods;


/**
 * This class, given time data, tries to find segment point using time data and average based filtering.
 * @author vishalk
 *
 */
public class TimeBasedDetection extends SegmentPtDetectionScheme
{
	public static final int DEF_WIN_SIZE_TIME = 2;
	public static final double DEF_MIN_SCALING_FACTOR = 1.5;
	public static final double DEF_MAX_SCALING_FACTOR = 2.5;
	private static final int DEF_THRESHOLD = 25;
	
	// Every point in the stroke should have all the time information set.
	public TimeBasedDetection(double[] timeData)
	{
		// this stroke should have all the information set
		m_data = timeData;
	}
	
	public void reset()
	{
		setM_data(null);
	}

	public double getMean()
	{
		double mean = 0.0;
		for (int i = DEF_WIN_SIZE_TIME; i < m_data.length; i++)
		{
			mean += m_data[i];
		}
		mean /= m_data.length;
		//System.out.println("\ntime Mean :"+mean);
		return mean;
	}
	
	public void normalize(double threshold)
	{
		for (int i = 0; i < m_data.length; i++)
		{
			m_data[i] -= threshold;
//			System.out.println(m_data[i]);
			// reduce the value to 0 is more than 0
			// if(m_timeData[i] > 0) m_timeData[i] = 0;
		}
	}
	
	// this would return the index of points it detects as the segments points
	public Vector detectSegmentPoints()
	{
		Vector segPts = new Vector();
		
		// NOTE: first and the last points are automatically detected as segment points
		// the first point is always a segment point
		segPts.add(new Integer(0));

		double threshold = getThreshold();
		System.out.println("\nThreshold: "+threshold);
		// normalize to perform avg based filtering
		//normalize(mean);
		
		// find segment points, maximas of time
		int i = 1;
		double maxima = threshold;
		int maxIndex = 0;
		boolean regionStart = false;
		
		while(i < m_data.length)
		{
			if((m_data[i]) > threshold)
			{
				regionStart = true;
				if((m_data[i]) > maxima)
				{
					maxima = m_data[i];
					maxIndex = i;
				}
			}
			else
			{
				if(regionStart)
				{
					if((maxIndex >= DEF_WIN_SIZE_TIME)&&(maxIndex <= (m_data.length-DEF_WIN_SIZE_TIME)))
					{
						// the pixel just before this is the segment point
						segPts.add(new Integer(maxIndex-1));
						System.out.println("TIME Segment Point detected: "+(maxIndex)+",  "+GMethods.formatNum(m_data[maxIndex]));
					}
					maxima = threshold;
					maxIndex = 0;
				}
				regionStart = false;
			}
			i++;
		}
		
		// the last point is always a segment point
		segPts.add(new Integer(m_data.length-1));

		return segPts;
	}
	
	public double getScalingFactor()
	{
		// find standard deviation
		double sd = 0.0;
		double mean = getMean();
		for (int i = 0; i < m_data.length; i++)
		{
			sd += Math.pow((mean - m_data[i]), 2);
		}
		sd /= m_data.length;
		sd = Math.sqrt(sd);
		//System.out.println("\ntime SD :"+sd);
		return (sd+mean)/mean;
	}
	
	public double getThreshold()
	{
		// TODO change this fixed threshold
		double mean = getMean();
		double th = mean*getScalingFactor();
		double t1 = mean*Prefs.getMinTimeScalingFactor();
		double t2 = mean*Prefs.getMaxTimeScalingFactor();
//		System.out.println("Time: LOW water mark: "+ t1);
//		System.out.println("Time: HIGH water mark: "+ t2);
//		System.out.println("Time: Threshold: "+ th);
		
		// check if threshold is within range.
		double returnTh = th;
		if(returnTh<t1) returnTh = t1;
		else if(returnTh>t2) returnTh = t2;
		//System.out.println("Final threshold: "+returnTh);
		return returnTh;
	}
}
