package dcad.process.preprocess;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import Jama.Matrix;
import dcad.Prefs;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.Stroke;
import dcad.util.GConstants;
import dcad.util.Maths;

/**
 * Class contains some functions etc to do segmentation on the raw data
 * @author vishalk
 *
 */
public class SegmentorNew
{
	private SpeedBasedDetection m_speedSeg = null;
	private TimeBasedDetection m_timeSeg = null;
	private CurvatureBasedDetection m_curvatureSeg = null;
	private static final int COMMON_SEG_WINDOW = 4;
	private static final double COMMON_SEG_DISTANCE = Prefs.getAnchorPtSize()*2+1;

	public SegmentorNew()
	{
	}
	
	/**
	 * Not used anywhere? Take 5 pixels to the left and right and take the cumulative-average.
	 * @param strk
	 */
	public void smoothen(Stroke strk)
	{
		Vector m_ptList = strk.getM_ptList();
		//1/6/2008 Added this for smoothening the stroke
		int stkLen = m_ptList.size();
/*		for(int i=0;i<stkLen;i++)
			///System.out.println((PixelInfo)m_ptList.get(i));*/
		double leftx, lefty, rightx, righty;
		for(int i=5;i<stkLen-5;i++)
		{
			PixelInfo p;
			double x=0,y=0;
			for(int j=i;j>i-5;j--)
			{
				p=(PixelInfo) m_ptList.elementAt(j);
				x+=p.getX();
				y+=p.getY();
			}
			leftx = x ;
			lefty = y ;
			
			x=y=0;
			for(int j=i;j<i+5;j++)
			{
				p=(PixelInfo) m_ptList.elementAt(j);
				x+=p.getX();
				y+=p.getY();
			}
			rightx = x;
			righty = y;
			
/*			double angle1 = Maths.newAngleInDegrees(new Point2D.Double(leftx,lefty),new Point2D.Double());
			double angle2 = Maths.newAngleInDegrees(*/
			
			x=(leftx + rightx) / 10;
			y=(lefty + righty) / 10;
			p  =(PixelInfo) m_ptList.elementAt(i);
			p.x = (int)x;
			p.y = (int) y;
		}
/*		for(int i=0;i<stkLen;i++)
			///System.out.println((PixelInfo)m_ptList.get(i));*/
	}

	
	
	private void setPixelInfo(Stroke theStroke)
	{
//		smoothen(theStroke);
		
		// get the data from the stroke in the required format
		Vector ptList = theStroke.getM_ptList();
		int stkLen = ptList.size();
		double[] slopes = new double[stkLen];
		
		for(int i=1;i<stkLen;i++)
		{
			PixelInfo oldPixel = (PixelInfo)ptList.get(i-1);
			PixelInfo currentPixel = (PixelInfo)ptList.get(i);
			Point2D p1 = new Point2D.Double(oldPixel.getX(),oldPixel.getY());
			Point2D p2 = new Point2D.Double(currentPixel.getX(),currentPixel.getY());
			
			slopes[i] = Maths.newAngleInDegrees(p1,p2);
			slopes[i]*=-1;
			if(slopes[i] < 0)
				slopes[i]= 180 - (slopes[i]*-1);
//			///System.out.println("Points : " + p1.toString() + "  " + p2.toString() + "  " + slopes[i]);
		}
		slopes[0] = slopes[1];
		
		double[] newSlopes = new double[stkLen];
		int leftBound,rightBound ;
		int windowSize = 10;
		int noOfPoints;
		double sumOfSlopes;
		for(int i=0;i<stkLen;i++)
		{
			noOfPoints = 1;
			sumOfSlopes = slopes[i];
			leftBound = i > windowSize ? (i-windowSize) : 0;
			rightBound = i >= stkLen - windowSize ? stkLen : i+windowSize;

			for(int j=leftBound;j<i;j++)
			{
				sumOfSlopes += slopes[j];
				noOfPoints++;
			}
			for(int j=i+1;j<rightBound;j++)
			{
				sumOfSlopes += slopes[j];
				noOfPoints++;
			}
			newSlopes[i] = sumOfSlopes / noOfPoints;
		}

		for(int i=windowSize;i<stkLen-windowSize;i++)
		{
			PixelInfo oldPixel = (PixelInfo)ptList.get(i-1);
			PixelInfo currentPixel = (PixelInfo)ptList.get(i);
			Point2D p1 = new Point2D.Double(oldPixel.getX(),oldPixel.getY());
			Point2D p2 = new Point2D.Double(currentPixel.getX(),currentPixel.getY());
			///System.out.println("Points : " + p1.toString() + "  " + p2.toString() + "  " + slopes[i] + "  " + newSlopes[i]);
		}
		
		
		
		// init local variables
		PixelInfo prevPixel = null;
		PixelInfo currPixel = null;
		int winSize_speed = 0;
		int winSize_slope = 0;
		double cummDist_speed = 0.0;
		
		Iterator iter = ptList.iterator();
		// set the pixel properties for the first pixel of the stroke.
		if(iter.hasNext())
		{
			// init the curvature and curvature of first pixel, set them to 0
			prevPixel = (PixelInfo)iter.next();
			prevPixel.setCurvature(0);
			prevPixel.setSpeed(0);
			prevPixel.setSlope(0);
			// ///System.out.println("i = 0: "+prevPixel);
		}

		// set the pixel property values for the remaining pixels
		int index=0;
		while(iter.hasNext())
		{
			index++;

			// get the second pixel
			currPixel = (PixelInfo)iter.next();

			// increment the counter for speed
			if(winSize_speed < SpeedBasedDetection.DEF_WIN_SIZE_SPEED) 
			{
				winSize_speed++;
			}
			else
			{
				// remove the distance of the last pixel in the window
				Point a = ((PixelInfo) ptList.get(index - winSize_speed -1));
				Point b = ((PixelInfo)ptList.get(index - winSize_speed));
				// ///System.out.println("prev dist: "+a.distance(b));
				cummDist_speed -= a.distance(b); 
			}
			// ///System.out.println("Speed: "+winSize_speed);
			
			// add the distance of current pixel in the window
			double thisDist = prevPixel.distance(currPixel);
			/////System.out.println("this dist: "+thisDist);
			cummDist_speed += thisDist;
			/////System.out.println("summ distance: "+cummDist_speed);
			
			// do speed calculations for this pixel
			PixelInfo a = (PixelInfo) ptList.get(index - winSize_speed);
			double cummTime_speed = currPixel.getTime() - a.getTime();
			currPixel.setSpeed(cummDist_speed/cummTime_speed);
			
			// set slope for the current pixel
			if(winSize_slope < CurvatureBasedDetection.DEF_WIN_SIZE_SLOPE)
			{
				winSize_slope++;
			}

			// calculate the actual window size
			int start = index - winSize_slope;
			int end = index + winSize_slope;
			
			// incase the window is running out of the stroke length, adjust the window size to fit the stroke
			if(end > (stkLen-1))
			{
				end = stkLen-1;
				start = index - (end-index);
			}
			
			// TODO: check for this code
			/////System.out.println("Slope :start: "+start+" end: "+end);
			double[][] winElem = theStroke.getWindowElemAs2DMatrix_Double(start, end);
			if(winElem!=null)
			{
				double[] odr = Maths.performODR(winElem);
				currPixel.setSlope(odr[0]);
				System.out.print(" Angle : " + Math.toDegrees(Math.atan(odr[0])));
				
				//ISHWAR
				/////System.out.println(Maths.angle(currPixel.getSlope(), 1) + "  " + Maths.angle(prevPixel.getSlope(), 1) + "\n");
				// calculate the curvature information
				double slopeChange = Maths.angle(currPixel.getSlope(), 1) - Maths.angle(prevPixel.getSlope(), 1);
				
				//ISHWAR
/*				if(  (Maths.angle(currPixel.getSlope(), 1) < 0 && Maths.angle(prevPixel.getSlope(), 1) >0)  || ( Maths.angle(currPixel.getSlope(), 1) > 0 && Maths.angle(prevPixel.getSlope(), 1) <0) )
				{
					///System.out.println("Slope Changed");
					slopeChange=0.0001;
				}*/
				
				
				// ///System.out.println("slopeChange "+slopeChange);
				currPixel.setCurvature(slopeChange/thisDist);
			}
			else
			{
				// NOTE: TODO this should not happen
			}
			///System.out.println("i = "+index+": "+currPixel);
			
			prevPixel = currPixel;
			currPixel = null;
		}		
/*		
		if(iter.hasNext())
		{
			// get the second point
			currPixel = (PixelInfo)iter.next();
			
			prevCurrDist = prevPixel.distance(currPixel);
			prevCurrAngle = GlobalMethods.angle(prevPixel, currPixel);
			// ///System.out.println(prevCurrAngle);
			// no need to check for divide by zero error points are not sampled until the mouse moves 
			// and when the mouse moves time will be different due to the sampling rate.
			double speed = prevCurrDist / (currPixel.getTime() - prevPixel.getTime());
			/////System.out.println("speed: "+ speed);
			currPixel.setSpeed(speed);
		}

		while (iter.hasNext())
		{
			PixelInfo nextPixel = (PixelInfo) iter.next();
			
			// calculate curvature at this pixel
			double currNextDist = currPixel.distance(nextPixel);
			double currNextAngle = GlobalMethods.angle(currPixel, nextPixel);
			// ///System.out.println(currNextAngle);
			
			// no need to check for divide by zero error points are not sampled until the mouse moves 
			// and when the mouse moves time will be different due to the sampling rate.
			double speed = currNextDist / (nextPixel.getTime() - currPixel.getTime());
			/////System.out.println("speed: "+ speed);
			nextPixel.setSpeed(speed);
			
			// curvature is the change of slope divided by change of distance 
			// double curvature = (currNextAngle-prevCurrAngle)/(currNextDist+prevCurrDist);
			double curvature = (currNextAngle-prevCurrAngle)/prevCurrDist;
	    	
	    	// set the value of curvature for this pixel
	    	currPixel.setCurvature(Math.abs(curvature));
	    	
	    	// transfer values
	    	prevPixel = currPixel;
	    	currPixel = nextPixel;
	    	prevCurrDist = currNextDist;
	    	prevCurrAngle = currNextAngle;
		}
*/		
		// set the curvature of the last pixel to 0, the last pixel is stored in currPixel
		if(currPixel != null) currPixel.setCurvature(0);
		
		for(int i=0;i<ptList.size();i++)
		{
			PixelInfo pi = (PixelInfo)ptList.get(i);
//ISHWAR			///System.out.println(i + ". (" + pi.x + "," + pi.y + ") " +pi.getCurvature() + " " + pi.getTime() + " " + pi.getSpeed());
		}
	}
	
	public Vector performSegmentation(Stroke theStroke)
	{
		
		// get information about speed and direction (and hence curvature data) for the stroke.
		setPixelInfo(theStroke);
		// this vector stores vectors of segment points detected through various methods
		Vector segVectorList = new Vector();
		
		switch (Prefs.getSegScheme())
		{
		case GConstants.SEG_SCHEME_ALL:
			segVectorList.add(getSpeedSeg(theStroke));
			segVectorList.add(getCurvatureSeg(theStroke));
			segVectorList.add(getTimeSeg(theStroke));
			break;
		
		case GConstants.SEG_SCHEME_SPEED:
			segVectorList.add(getSpeedSeg(theStroke));
			break;
		
		case GConstants.SEG_SCHEME_CURVATURE:
			segVectorList.add(getCurvatureSeg(theStroke));
			break;

		case GConstants.SEG_SCHEME_TIME:
			segVectorList.add(getTimeSeg(theStroke));
			break;

		case GConstants.SEG_SCHEME_SPEED_CURVATURE:
			segVectorList.add(getSpeedSeg(theStroke));
			segVectorList.add(getCurvatureSeg(theStroke));
			break;
		
		default:
			segVectorList.add(getSpeedSeg(theStroke));
			segVectorList.add(getCurvatureSeg(theStroke));
			break;
		}

		// get the common segment points
		///System.out.println("common segment points");
		Vector selectedSegPos = detectCommonSegPt(segVectorList, theStroke);

		// TODO: remove this code, This is just to print the common segments points
		Vector ptList = theStroke.getM_ptList();
		Iterator iter = selectedSegPos.iterator();
		while (iter.hasNext())
		{
			 Integer index = (Integer) iter.next();
			 PixelInfo pixel = (PixelInfo)ptList.get(index.intValue());
			 ///System.out.println("Index: "+index+", "+pixel);
		}
		
		///System.out.println("\n\n");
		return selectedSegPos;
	}

	private Vector getSpeedSeg(Stroke theStroke)
	{
		// detect segment points based of speed data
		double[] speedData = theStroke.getSpeedData();
		m_speedSeg = new SpeedBasedDetection(speedData);
		Vector segPt_speed = m_speedSeg.detectSegmentPoints();
		return segPt_speed;
	}

	private Vector getTimeSeg(Stroke theStroke)
	{
		// detect segment points based of time data
		double[] timeData = theStroke.getTimeData();
		m_timeSeg = new TimeBasedDetection(timeData);
		Vector segPt_time = m_timeSeg.detectSegmentPoints();
		return segPt_time;
	}

	private Vector getCurvatureSeg(Stroke theStroke)
	{
		// detect segment points based of curvature data
		double[] curvatureData = theStroke.getCurvatureData();
		m_curvatureSeg = new CurvatureBasedDetection(curvatureData);
		Vector segPt_curvature = m_curvatureSeg.detectSegmentPoints();
		return segPt_curvature;
	}
	
	private Vector detectCommonSegPt(Vector segVL, Stroke theStroke)
	{
		
		int len = theStroke.getM_ptList().size();
		int[] stk_Arr = new int[len];
		int methodCnt = segVL.size();

		// check if accepable value is set to a reasonable vaule
		if((Prefs.getAcceptableVotes() >= 1) && (Prefs.getAcceptableVotes() <= methodCnt))
		{
			// set the method count to acceptable votes.
			methodCnt = Prefs.getAcceptableVotes();
		}
		
		// TODO: is there a better way to do it? 
		// init the array to all 0
		for(int i=0; i<len; i++) stk_Arr[i]=0;
		
		int commonWindow = COMMON_SEG_WINDOW;
		double commonDistance = COMMON_SEG_DISTANCE+2;
		Vector commonSegPts = new Vector();
		Iterator iter = segVL.iterator();
		
		// repeat for each segmentation algorithm
		while (iter.hasNext())
		{
			Vector element = (Vector) iter.next();
			Iterator iterator = element.iterator();
			int lastUsedPos=-1;
			while (iterator.hasNext())
			{
				Integer obj = (Integer) iterator.next();
				int index = obj.intValue();
				PixelInfo indexPt = (PixelInfo)theStroke.getM_ptList().get(index);
				
				int start = index - commonWindow;
				int end = index + commonWindow;
				// check if i is within the array bounds, if yes, increment the array,
				// make sure that one segmentation scheme increments one point only once
				start = (start>lastUsedPos)? start:lastUsedPos+1;
				end = (end<len)? end:len-1;
				for(int i=start; i<=end; i++)
				{
					PixelInfo pt = (PixelInfo)theStroke.getM_ptList().get(i);
					if(indexPt.distance(pt) <= commonDistance)
					{
						stk_Arr[i]++;
						lastUsedPos = i;
					}
				}
			}
		}
		
		// find the points with more than methodCnt value
		int i = 0;
		while(i<len)
		{
			// store the index as the segment point, if condition is met
			if(stk_Arr[i] >= methodCnt)
			{
				// find out the streak of continuous pixels
				int beginIdx = i;
				int endIdx = i;
				i++;
				while(i<len)
				{
					if(stk_Arr[i] >= methodCnt)
					{
						i++;
					}
					else break;
				}
				endIdx = i-1;
				
				// find one point from that streak
				int segPt = 0;
				// check if the segment is very short
				if((beginIdx==0)&&(endIdx==(len-1)))
				{
					if(beginIdx==endIdx) commonSegPts.add(new Integer(beginIdx));
					else
					{	// segment is not just one point
						commonSegPts.add(new Integer(beginIdx));
						commonSegPts.add(new Integer(endIdx));
					}
					return commonSegPts;
				}
				else if(beginIdx==0) segPt = 0;
				else if(endIdx==(len-1)) segPt = endIdx;
				else segPt = (int)Math.floor((beginIdx+endIdx)/2);
				
				// add segPt to the list of detected segments.
				commonSegPts.add(new Integer(segPt));
			}
			i++;
		}
		
		return commonSegPts;
	}
}
