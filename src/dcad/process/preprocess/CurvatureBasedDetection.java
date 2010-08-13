package dcad.process.preprocess;

import java.sql.Struct;
import java.util.Vector;

import dcad.Prefs;
import dcad.util.GMethods;
import dcad.process.preprocess.*;

/**
 * This class, given curvature data, tries to find segment point using curvature data and average based filtering.
 * @author vishalk
 *
 */
public class CurvatureBasedDetection extends SegmentPtDetectionScheme
{
//	private static final double DEF_SCLAING_FACTOR = 2;
	public static final double MIN_THRESHOLD_VALUE = 0.1;
	public static final double DEF_SCALING_FACTOR = 3;
	public static final int DEF_WIN_SIZE_SLOPE = 3;
	private static int CurvPtCount = 0;
	// 17-09-09
	private static double [][] CurvatureData = new double [1000][2];  // to store curvature data[1] and relative index[0] info of local maxima.
	// Every point in the stroke should have all the curvature information set.
	public CurvatureBasedDetection(){
		
	}
	public CurvatureBasedDetection(double[] curvatureData)
	{
		// this stroke should have all the information set
		m_data = curvatureData;
	}
	
	public void reset()
	{
		setM_data(null);
	}

	public double getThreshold()
	{
		double th = getMean()*getScalingFactor();
		//return (th >= MIN_THRESHOLD_VALUE)? th:MIN_THRESHOLD_VALUE;
		return th;
	}

	public double getMean()
	{
		double mean = 0.0;

		// ignore some values while calculating the mean, equal to the Window size used for calculating the curvature.
		int len = m_data.length - DEF_WIN_SIZE_SLOPE;
		for (int i = DEF_WIN_SIZE_SLOPE; i < len; i++)
		{
			if((m_data[i] != Double.NaN) && (m_data[i] != Double.POSITIVE_INFINITY) && (m_data[i] != Double.NEGATIVE_INFINITY))
			{
				mean += m_data[i];
			}
		}
		mean /= m_data.length;
		System.out.println("\ncurvature Mean :"+mean);
		return mean;
	}
	
	public void normalize(double threshold)
	{
		for (int i = 0; i < m_data.length; i++)
		{
			m_data[i] -= threshold;
			//System.out.println(m_data[i]);
			// reduce the value to 0 is more than 0
			// if(m_curvatureData[i] > 0) m_curvatureData[i] = 0;
		}
	}
	
	// this would return the index of points it detects as the segments points
	public Vector detectSegmentPoints()
	{
		Vector segPts = new Vector();
		int limit = m_data.length;
		double threshold = getThreshold();
		// 18-09-09
		int index = 0;
//ISHWAR		System.out.println("Threshold: "+threshold);
		// normalize to perform avg based filtering
		//normalize(mean);
		
		// find segment points, maximas of curvature
		// NOTE: first and the last points are automatically detected as segment points
		// the first point is always a segment point
		segPts.add(new Integer(0));
		CurvatureData[index][0] = 0;
		CurvatureData[index++][1] = m_data[0];
		int i = 1;
		// ignore the first maxima, this is because we have already added the first points as a segment point
		// therefore ignore all the points till all the initial cliff is done.
//		while((i<limit)&&(m_data[i] > threshold)) i++;
		
		double maxima = threshold;
		int maxIndex = 0;
		boolean regionStart = false;

		while(i < limit)
		{
//			System.out.println(m_data[i]);
			//System.out.println(i+": "+m_data[i]+", "+regionStart);
			if(m_data[i] > threshold)
			{
				regionStart = true;
				if(m_data[i] > maxima)
				{
					maxima = m_data[i];
					maxIndex = i;
				}
			}
			else
			{
				// add this point only if the pixel is not towards the extremes of the stroke
				if(regionStart)
				{
					if((maxIndex >= DEF_WIN_SIZE_SLOPE)&&(maxIndex <= (limit-DEF_WIN_SIZE_SLOPE)))
					{
						segPts.add(new Integer(maxIndex));
						CurvatureData[index][0] = maxIndex;
						CurvatureData[index++][1] = m_data[maxIndex];
						System.out.println("CURVATURE Segment Point detected: "+maxIndex+",  "+GMethods.formatNum(maxima));
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
		CurvatureData[index][0] = m_data.length-1;
		CurvatureData[index++][1] = m_data[m_data.length-1];
		Sort sort = new Sort();
		sort.bubbleSort(CurvatureData,index);
		setIndex(index);
		for (index=0; index< getIndex(); index++){
			System.out.println("index = " + CurvatureData[index][0] + "value =" + CurvatureData[index][1] + "");
		}
		return segPts;
	}
	
	public double getScalingFactor()
	{
		return Prefs.getCurvatureScalingFactor();
	}
	
	public double[][] getCurvatureData(){
		return CurvatureData;
	}
	public void setIndex(int index){
		CurvPtCount = index;
	}
	public int getIndex(){
		return CurvPtCount;
	}
}
