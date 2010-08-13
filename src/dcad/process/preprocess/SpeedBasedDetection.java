package dcad.process.preprocess;

import java.util.Vector;

import dcad.Prefs;
import dcad.util.GMethods;


/**
 * This class, given speed data, tries to find segment point using speed data and average based filtering.
 * @author vishalk
 *
 */
public class SpeedBasedDetection extends SegmentPtDetectionScheme
{
	public static final int DEF_WIN_SIZE_SPEED = 2;
	public static final double DEF_SCALING_FACTOR = 0.4;
	private static int SpeedPtCount = 0;
//	private static final double DEF_SCLAING_FACTOR = 0.5;
	// 17-09-09
	private static double [][] SpeedData = new double [1000][2];  // to store speed data[1] and relative index[0] info of local maxima.
	
	public SpeedBasedDetection(){
		
	}
	// Every point in the stroke should have all the speed information set.
	public SpeedBasedDetection(double[] speedData)
	{
		// this stroke should have all the information set
		m_data = speedData;
	}
	
	/* (non-Javadoc)
	 * @see process.preprocess.IDetectionScheme#reset()
	 */
	public void reset()
	{
		setM_data(null);
	}

	/* (non-Javadoc)
	 * @see process.preprocess.IDetectionScheme#getThreshold()
	 */
	public double getThreshold()
	{
		return getMean() * getScalingFactor();
	}
	
	/* (non-Javadoc)
	 * @see process.preprocess.IDetectionScheme#getMean()
	 */
	public double getMean()
	{
		double mean = 0.0;

		// ignore some values while calculating the mean, equal to the Window size used for calculating the speed.
		for (int i = DEF_WIN_SIZE_SPEED; i < m_data.length; i++)
		{
			mean += m_data[i];
		}
		mean /= m_data.length;
		System.out.println("\nSpeed Mean :"+mean);
		return mean;
	}
	
	/* (non-Javadoc)
	 * @see process.preprocess.IDetectionScheme#normalize(double)
	 */
	public void normalize(double threshold)
	{
		for (int i = 0; i < m_data.length; i++)
		{
			m_data[i] -= threshold;
			System.out.println(m_data[i]);
			// reduce the value to 0 is more than 0
			// if(m_speedData[i] > 0) m_speedData[i] = 0;
		}
	}
	
	/** This returns the index of points it detects as the segments points
	 * @see process.preprocess.IDetectionScheme#detectSegmentPoints()
	 */
	public Vector detectSegmentPoints()
	{
		Vector segPts = new Vector();
		int limit = m_data.length;
		int index = 0;
		double threshold = getThreshold();
		System.out.println("Threshold: "+threshold);

		// normalize to perform avg based filtering
		//normalize(mean);
		
		// find segment points, minimas of speed
		// NOTE: first and the last points are automatically detected as segment points
		// the first point is always a segment point
		segPts.add(new Integer(0));
		SpeedData[index][0] = 0;
		// added on 25-02-10
		if(m_data != null){
		SpeedData[index++][1] = m_data[0];
		}
		
		int i = 1;
		
		// ignore the first minima, this is because we have already added the first points as a segment point
		// therefore ignore all the points till all the initial depression is done.
//		while((i<limit)&&(m_data[i] < threshold)) i++;
		
		double minima = threshold;
		int minIndex = 0;
		boolean regionStart = false;
		
		while(i < limit)
		{
			if(m_data[i] < threshold)
			{
				regionStart = true;
				if(m_data[i] < minima)
				{
					minima = m_data[i];
					minIndex = i;
				}
			}
			else
			{
				if(regionStart)
				{
					if((minIndex >= DEF_WIN_SIZE_SPEED)&&(minIndex <= (limit-DEF_WIN_SIZE_SPEED)))
					{
						segPts.add(new Integer(minIndex));
						SpeedData[index][0] = minIndex;
						SpeedData[index++][1] = m_data[minIndex];
						System.out.println("SPEED Segment Point detected: "+minIndex+",  "+GMethods.formatNum(minima));
					}
					minima = threshold;
					minIndex = 0;
				}
				regionStart = false;
			}
			i++;
		}
		
		// the last point is always a segment point
		segPts.add(new Integer(m_data.length-1));
		SpeedData[index][0] = m_data.length-1;
		SpeedData[index++][1] = m_data[m_data.length-1];
		Sort sort = new Sort();
		sort.bubbleSort(SpeedData,index);
		setIndex(index);
		for (index=0; index< getIndex(); index++){
			System.out.println("index = " + SpeedData[index][0] + "value =" + SpeedData[index][1] + "");
		}
		return segPts;
	}
	
	/* (non-Javadoc)
	 * @see process.preprocess.IDetectionScheme#getScalingFactor()
	 */
	public double getScalingFactor()
	{
		return Prefs.getSpeedScalingFactor();
	}
	public double[][] getSpeedData(){
		return SpeedData;
	}
	public void setIndex(int index){
		SpeedPtCount = index;
	}
	public int getIndex(){
		return SpeedPtCount;
	}
}
