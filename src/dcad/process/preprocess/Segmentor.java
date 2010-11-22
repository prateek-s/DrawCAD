package dcad.process.preprocess;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
//import com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import dcad.model.geometry.*;

import Jama.Matrix;
import dcad.Prefs;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.Segment;
import dcad.util.GConstants;
import dcad.util.Maths;
import dcad.process.ProcessManager;
import dcad.process.preprocess.*;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.segment.SegmentRecognitionScheme;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.util.MagicConstant;
/**
 * Class contains functions to do segmentation on the raw data. Finds
 * segment points by various algorithms.
 * @author vishalk
 *
 */
public class Segmentor
{
	private SpeedBasedDetection m_speedSeg = null;
	private TimeBasedDetection m_timeSeg = null;
	private CurvatureBasedDetection m_curvatureSeg = null;
	
	private static final int COMMON_SEG_WINDOW = 3;
	private static final double COMMON_SEG_DISTANCE = Prefs.getAnchorPtSize()*2+1;
	//19-09-09
	private static final double TolerantDistance = MagicConstant.SegmentorTolerantDistance ; // distance to be ignored while selecting common segment   
														//points from both the algorithms( Speed + Curvature)
	private static final int PixelIndexWindow = MagicConstant.SegmentorPixelIndexWindow ; 
	
	private static final double  DEFAULT = 999999.0;
	private static double errorTolerance = MagicConstant.SegmentorErrorTolerance ;
	
	Vector CommonSegmentPts;    //vector to store segments from both Speed + Curvature Schemes
	Vector CurvVector;
	Vector SpeedVector;
	Vector IterWiseSegPts;
	private ProcessManager m_processManager;
	double CurvPts[][];
	int CurvPtCount;
	
	public Segmentor()
	{
	}
	
	/**
	 * Fills in all the details about the stroke. Speeds,curvature etc.
	 * Shouldn't this be  really in the Stroke class?
	 * @param theStroke
	 */
	private void setPixelInfo(Stroke theStroke)
	{
		// get the data from the stroke. (x,y,t)
		Vector ptList = theStroke.getM_ptList();
		//double [][] strokeMat = theStroke.getPointsAs2DMatrix_Double();
		int stkLen = ptList.size();
		// init local variables
		PixelInfo prevPixel = null;
		PixelInfo currPixel = null;
		
		int winSize_speed = 0;
		int winSize_slope = 0;
		/**
		 * Cumulative distance for speed calculations
		 */
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
			PixelInfo a = (PixelInfo) ptList.get(index - winSize_speed); //index - 2
			double cummTime_speed = currPixel.getTime() - a.getTime();
			currPixel.setSpeed(cummDist_speed/cummTime_speed);
			
			/* Speed is just d(i,i-winSize)/(t(i(-t(i-winSize)) */
			
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
				if(slopeChange == 0.0 && thisDist == 0.0){
					currPixel.setCurvature(prevPixel.getCurvature());
				}
				else {
				currPixel.setCurvature(slopeChange/thisDist);
				}
			}
			else
			{
				// NOTE: TODO this should not happen
			}
			///System.out.println("i = "+index+": "+currPixel);
			
			prevPixel = currPixel;
			currPixel = null;
		}		

		// set the curvature of the last pixel to 0, the last pixel is stored in currPixel
		if(currPixel != null) currPixel.setCurvature(0);
		
		for(int i=0;i<ptList.size();i++)
		{
			PixelInfo pi = (PixelInfo)ptList.get(i);
//ISHWAR			///System.out.println(i + ". (" + pi.x + "," + pi.y + ") " +pi.getCurvature() + " " + pi.getTime() + " " + pi.getSpeed());
		}
	}
	
	public Vector performSegmentation(Stroke theStroke,int segScheme)
	{
		
		// get information about speed and direction (and hence curvature data) for the stroke.
		setPixelInfo(theStroke);
		/* this vector stores vectors of segment points detected through various methods */
		Vector segVectorList = new Vector();

		switch (segScheme)
		{
		//PRATEEK: Is this the place where ALL the segmentation decisions are made??
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
		
		case GConstants.SEG_SCHEME_SIMPLE : 
			segVectorList.add(getSimpleSeg(theStroke)) ;
			break ;
			
			
		default:
			segVectorList.add(getSpeedSeg(theStroke));
			segVectorList.add(getCurvatureSeg(theStroke));
			break;
		}

		Vector selectedSegPos;
	
		selectedSegPos = detectCommonSegPt(segVectorList, theStroke);
			
		return selectedSegPos;
	}

	
	/**
	 * Just return the first and last points of the stroke as segment points.
	 * Simple semantics
	 * @param theStroke
	 * @return Vector 
	 */
	private Vector getSimpleSeg(Stroke theStroke)
	{
		Vector seg_pt_list = new Vector() ;
		Vector stroke_pt_list = theStroke.getM_ptList() ;
		Integer START = new Integer(0) ; //START
		Integer END = new Integer(stroke_pt_list.size()-1) ;
		//no segment recognized when start and end co-incide!
		if (START == END)
		{
			END = new Integer(END.intValue()-1) ;
		}
		
		seg_pt_list.add(START) ;
		seg_pt_list.add(END) ;
		return seg_pt_list ;
		
	}
	
	
	public Vector getSpeedSeg(Stroke theStroke)
	{
		// detect segment points based of speed data
		double[] speedData = theStroke.getSpeedData();
		m_speedSeg = new SpeedBasedDetection(speedData);
		Vector segPt_speed = m_speedSeg.detectSegmentPoints();
		return segPt_speed;
	}

	
	public Vector getTimeSeg(Stroke theStroke)
	{
		// detect segment points based of time data
		double[] timeData = theStroke.getTimeData();
		m_timeSeg = new TimeBasedDetection(timeData);
		Vector segPt_time = m_timeSeg.detectSegmentPoints();
		return segPt_time;
	}

	
	public Vector getCurvatureSeg(Stroke theStroke)
	{
		// detect segment points based of curvature data
		double[] curvatureData = theStroke.getCurvatureData();
		m_curvatureSeg = new CurvatureBasedDetection(curvatureData);
		Vector segPt_curvature = m_curvatureSeg.detectSegmentPoints();
		return segPt_curvature;
	}
	/**
	 * Returns the list of segment/anchor points found by ALL the methods used.  
	 * @param segVL : List of all segment points detected by the various methods (vector of vectors)
	 * @param theStroke
	 * @return
	 */
	public Vector detectCommonSegPt(Vector segVL, Stroke theStroke)
	{
		
		int len = theStroke.getM_ptList().size();
		int[] stk_Arr = new int[len];
		int methodCnt = segVL.size(); //number of methods used to detect seg-points. 

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
		/* commonDistance size of anchor_point, which are end-points of segments.
		 * We want to filter-out seg-points too close together and create 'large'
		 *  seg-points to replace them.
		 */
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
				Integer obj = (Integer) iterator.next(); //get the seg point index
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
	
	// 29-08-09

	/**Function to return vector in a relevant form as desired by the 
	 * calling function  
	 * @author Sunil Kumar
	 */
	private Vector returnRelevantVector(Vector segVectorList)
	{
		Vector segPts = new Vector();
		Iterator iter = segVectorList.iterator();
		Vector element = (Vector) iter.next();
		Iterator iterator = element.iterator();
		while(iterator.hasNext()){
			Integer obj = (Integer) iterator.next();
			int index = obj.intValue();
			segPts.add(new Integer(index));
		}
	
		return segPts;
	}
	
	/**Function to find segment points by using modified Hybrid fits approach 
	 * Not used anywhere?
	 * @author Sunil Kumar
	 */
	private Vector combinationAlgorithm(Stroke theStroke)
	{
		CurvatureBasedDetection cbd = new CurvatureBasedDetection();
		SpeedBasedDetection sbd = new SpeedBasedDetection();
		CommonSegmentPts = new Vector();
		CurvPts = cbd.getCurvatureData();
		double SpeedPts[][] = sbd.getSpeedData();
		CurvPtCount = cbd.getIndex();
		int SpeedPtCount = sbd.getIndex();
		int index ;
		CurvVector = new Vector();
		//copy the speed and curvature data in two vectors
		for (index = 0; index < CurvPtCount ; index++){
			Double obj = CurvPts[index][0];
			CurvVector.add(new Integer(obj.intValue()));
		}
		
		///System.out.println("Curvature Indices");
		printValues(CurvVector);
		
		SpeedVector = new Vector();
		for (index = 0; index < SpeedPtCount ; index++){
			Double obj = SpeedPts[index][0];
			SpeedVector.add(new Integer(obj.intValue()));
		}
		///System.out.println("Speed Indices");
		printValues(SpeedVector);
		Sort sort = new Sort();
		int ElementIndex;
		// find common points (intersection of speed and curvature) and put it in CommonSegmentPts vector
		
		boolean flag = false;
		for(index = 0; index < SpeedVector.size(); index++ ){
			
			ElementIndex = sort.linearSearch(CurvVector, (Integer)SpeedVector.elementAt(index));
			
			if(ElementIndex != -1){
				CommonSegmentPts.add((Integer)(CurvVector.elementAt(ElementIndex)));
				CurvVector.remove(ElementIndex);
				SpeedVector.remove(index --);  // point is removed from vectors
				
			}		
		}
		///System.out.println("Common Points");
		printValues(CommonSegmentPts);
		int ElemAtIndex;
		// Find the curvature based segment points in the Tolerance Distance of speed based segment points 
		for(index = 0; index < SpeedVector.size(); index++ ){
			ElementIndex = FindNearbyPts(CurvVector, (Integer)SpeedVector.elementAt(index), theStroke);
			if(ElementIndex != -1){
				ElemAtIndex = ((Integer)CurvVector.elementAt(ElementIndex) + (Integer)SpeedVector.elementAt(index))/2;
				CommonSegmentPts.add(ElemAtIndex);
				CurvVector.remove(ElementIndex);
				SpeedVector.remove(index--);
			}
		}
		//printValues(CommonSegmentPts);
		removeNearbyPixels(theStroke);
		//setNewScalingFactor(theStroke);
		Collections.sort(CommonSegmentPts);
		printValues(CommonSegmentPts);
		double PrevIterError;
		double NewIterError;
		Vector IterationPts = new Vector(); 
		/////System.out.println("Error " + (PrevIterError = calculateError(CommonSegmentPts, theStroke)));
		// create a vector that stores segment points of each iteration
		IterWiseSegPts = new Vector();
		// for hybrid
		IterationPts = copyVector(IterationPts, CommonSegmentPts);
		double PrevError = calculateError(IterationPts, theStroke);
		if(PrevError > 10000.0){	// if stroke consists of curves
			errorTolerance = 90.0;
		}
		IterationPts.add((double ) PrevError);
		IterWiseSegPts = appendVector(IterWiseSegPts,IterationPts);
		double NewError = -1;

		int curvIndex = -1, speedIndex = -1;
		double curvError,speedError;
		Vector curvPts = new Vector();
		Vector speedPts = new Vector();
		Vector changeInError = new Vector();
		while((CurvVector.size() != 0) || (SpeedVector.size() != 0) ){
			curvError = DEFAULT;
			speedError = DEFAULT;
			
			if(CurvVector.size() != 0){
				clearVector(curvPts);
				curvPts = copyVector(curvPts, CommonSegmentPts);
				curvIndex = (Integer)CurvVector.get(0);
				curvPts.add((Integer)curvIndex);
				CurvVector.remove(0);
				Collections.sort(curvPts);
				curvError = calculateError(curvPts, theStroke);
				curvPts.add((Double)curvError);
			}
			
			if(SpeedVector.size() != 0){
				clearVector(speedPts);
				speedPts = copyVector(speedPts, CommonSegmentPts);
				speedIndex = (Integer)SpeedVector.get(0);
				speedPts.add((Integer)speedIndex);
				SpeedVector.remove(0);
				Collections.sort(speedPts);
				speedError = calculateError(speedPts, theStroke);
				speedPts.add((Double)speedError);
			}
			
			if((speedError <= curvError) || (curvError == DEFAULT)){
				NewError = speedError;
				//changeInError.add((Double)(NewError - PrevError));
				if((PrevError - NewError) > errorTolerance){
				IterWiseSegPts = appendVector(IterWiseSegPts, speedPts);
				CommonSegmentPts.add((Integer)speedIndex);
				changeInError.add((Double)(PrevError - NewError));
				PrevError = NewError;
				}
			}
			else if((curvError < speedError) || (speedError == DEFAULT)){
				NewError = curvError;
				if((PrevError - NewError) > errorTolerance){
				IterWiseSegPts = appendVector(IterWiseSegPts, curvPts);
				CommonSegmentPts.add((Integer)curvIndex);
				changeInError.add((Double)(PrevError - NewError));
				PrevError = NewError;
				}
			}
		
			
		}
			
		CommonSegmentPts = clearVector(CommonSegmentPts);
		Vector TempVect = new Vector();
		CommonSegmentPts = copyVectorIndex(IterWiseSegPts, TempVect, IterWiseSegPts.size() - 1);
		//TempVect = copyVectorIndex(IterWiseSegPts, TempVect, IterWiseSegPts.size() -1);
		/*index = 0;
		while(index < (TempVect.size()-1)){
			CommonSegmentPts.add((Integer)TempVect.get(index++));
		}*/
		//CommonSegmentPts = selectBestSegPtsSet(IterWiseSegPts);
		
		return CommonSegmentPts;
	}
	/**Function to print all the elements in a Vector
	 * @author Sunil Kumar
	 */
	
	void printValues(Vector Vect){
		int index;
		for(index=0;index< Vect.size(); index++){	
			///System.out.println("Element " + Vect.elementAt(index));
		}
		
	}
	
	/**Function to find points which are nearby the points obtained by 
	 * both speed and curvature based techniques
	 * @author Sunil Kumar
	 */
	// find segment points which are detected by both Curvature and speed at a distance of TolerantDistance
	public int FindNearbyPts(Vector CurvData, int Eleindex, Stroke theStroke){
		int index;
		Vector WinElements = new Vector();
		int CurvEleIndex;
		Double minDistance = 999.0;  // initialized to some larger value say 999.0
		Double Distance;
		int ReturnIndex = -1;
		int min = Eleindex - PixelIndexWindow;
		int max = Eleindex + PixelIndexWindow;
		for(index=0;index<CurvData.size(); index++){
			CurvEleIndex = (Integer)CurvData.elementAt(index);
			if(CurvEleIndex >= min && CurvEleIndex <= max){
					Vector ptList = theStroke.getM_ptList();
					Point CurvPt = ((PixelInfo) ptList.get(CurvEleIndex));
					Point SpeedPt = ((PixelInfo)ptList.get(Eleindex));
					if((Distance = SpeedPt.distance(CurvPt)) < TolerantDistance){
						if(minDistance > Distance){
							minDistance = Distance;
							ReturnIndex = index;
						}
					}
			}
		}
		
		return ReturnIndex;
	}
	
	/**Function To calculate commulative error for a given set of segment points
	 * @author Sunil Kumar
	 */
	// To calculate commulative error for a given set of segment points
	double calculateError(Vector SegPtVect, Stroke theStroke){
		m_processManager= ProcessManager.getInstance();
		RecognitionManager recogMan = m_processManager.getRecogManager();
			SegmentRecognizer segmentRecog = recogMan.getSegmentRecogMan().getSegmentRecognizer();
		Vector ptList = theStroke.getM_ptList();
		double CummError = 0.0;
		int index = 0,End;
		int Start = (Integer)(SegPtVect.get(index));
		for(index=1; index < SegPtVect.size(); index++){
			End = (Integer)(SegPtVect.get(index));
			CummError += ErrorBetweenSegPts(Start,End,segmentRecog, theStroke);
			Start = End;	
		}
		return CummError;
	}
	
	/**Function To Calculate the error between two segment points
	 * @author Sunil Kumar
	 */
	// Calculate the error between two segment points
	double ErrorBetweenSegPts(int Start, int End ,SegmentRecognizer segRecog, Stroke theStroke){
		double Error = 0.0;
		double[][] seg = theStroke.getWindowElemAs2DMatrix_Double(Start, End);
		if(seg != null)
		{
			SegmentRecognitionScheme brs = segRecog.recognizeSegment(seg);
			if(brs == null){ 
				///System.out.println(" brs is null");
			}
			else
			{
				Error = -brs.getConfidance();
			}
		}		
			return Error;
	}
	
	// Remove segment points from Curvature and Speed vectors which are at Tolerance distance from segment points of 
	// CommonSegmentPts Vector(Speed + Curvature)
	/**Function To Remove segment points from Curvature and Speed vectors which are at Tolerance distance from segment points of 
	CommonSegmentPts Vector(Speed + Curvature)
	 * @author Sunil Kumar
	 */
	void removeNearbyPixels(Stroke theStroke){
		int index;
		int CurvIndex;
		int SpeedIndex;
		Vector ptList = theStroke.getM_ptList();
		for(index=0; index < CommonSegmentPts.size(); index++){
			Point CommonPt = (PixelInfo)ptList.get((Integer)CommonSegmentPts.get(index));
			for(CurvIndex = 0; CurvIndex < CurvVector.size(); CurvIndex++){
				Point CurvPt = (PixelInfo)ptList.get((Integer)CurvVector.get(CurvIndex));
				if(CommonPt.distance(CurvPt) <= TolerantDistance){
					CurvVector.remove(CurvIndex--);
				}
			}
			for(SpeedIndex = 0; SpeedIndex < SpeedVector.size(); SpeedIndex++){
				Point SpeedPt = (PixelInfo)ptList.get((Integer)SpeedVector.get(SpeedIndex));
				if(CommonPt.distance(SpeedPt) <= TolerantDistance){
					SpeedVector.remove(SpeedIndex--);
				}
			}
		}
	}
	
	Vector copyVector(Vector IterWiseSegPts,Vector CommonSegmentPts){
		int index;
		for(index=0;index<CommonSegmentPts.size(); index++){
			IterWiseSegPts.add((Integer)CommonSegmentPts.get(index));
		}
		return IterWiseSegPts;
	}
	
	Vector clearVector(Vector Vect){
		int index = 0;
		while(Vect.size()!=0){
			Vect.remove(index);
		}
		return Vect;
	}
	
	// to append vector at the end of iteration wise segment points vector 
	Vector appendVector(Vector IterWiseSegPts,Vector CommonSegmentPts){
		int CommPtIndex=0;
		Vector SegPts = new Vector();
		double error;
		while(CommPtIndex < CommonSegmentPts.size()){
			if(CommPtIndex == (CommonSegmentPts.size() - 1)){
				error = (Double)CommonSegmentPts.get(CommPtIndex++);
				SegPts.add((Double)error);
			}
			else{
			SegPts.add((Integer)CommonSegmentPts.get(CommPtIndex++));
			}
		}
		IterWiseSegPts.add(SegPts);
		return IterWiseSegPts;
	}
	
	/**Function To select best fit from the set of Hybrid fits obtained
	 * @author Sunil Kumar
	 */
	Vector selectBestSegPtsSet(Vector IterWiseSegPts){
		Vector Vect = new Vector();			// to return final set of segment pts
		Vector TempVect = new Vector();   // to store temporary iteration results
		int index = -1;
		// if it consists of only common pts
		if(IterWiseSegPts.size() == 1) {
			index = 0;
		}
		
		//if it consists of only two segment pt sets 
		else if(IterWiseSegPts.size() == 2){
			TempVect = copyVectorIndex(IterWiseSegPts, TempVect, 0);
			double PrevError = (Double)TempVect.get(TempVect.size()-1);
			TempVect = clearVector(TempVect);
			TempVect = copyVectorIndex(IterWiseSegPts, TempVect, 1);
			double NewError = (Double)TempVect.get(TempVect.size()-1);
			TempVect = clearVector(TempVect);
			
			if((PrevError - NewError) > 100.0){        //selecting the second set if error reduction is > 100.0
				index = 1;
			}
			else{
				index = 0;
			}
		}
		else if(IterWiseSegPts.size() > 2){
			TempVect = copyVectorIndex(IterWiseSegPts, TempVect, 0);
			double PrevError = (Double)TempVect.get(TempVect.size()-1);
			TempVect = clearVector(TempVect);
			
			TempVect = copyVectorIndex(IterWiseSegPts, TempVect, 1);
			double NewError = (Double)TempVect.get(TempVect.size()-1);
			TempVect = clearVector(TempVect);
			double ChangeInPrevError = PrevError - NewError;
			double ChangeInNewError;
			PrevError = NewError;
			if(ChangeInPrevError < 15.0){        //selecting the first set if change in error  is < 30.0
				index = 0;
			}
			else{
				int Iterator = 2;
				int Iterations = IterWiseSegPts.size();
				while(Iterator < Iterations){
					TempVect = copyVectorIndex(IterWiseSegPts, TempVect, Iterator);
					NewError = (Double)TempVect.get(TempVect.size()-1);
					TempVect = clearVector(TempVect);
					ChangeInNewError = PrevError - NewError;
					// if new iteration's error is less (or more) than prev itertion within 30.0 
					// or if previous change in segment pts Error > 3 times new change in segment pts error
					if( (ChangeInNewError < 30.0) && (NewError < 1000.0)){// (ChangeInPrevError > (3 * ChangeInNewError))
						index = Iterator - 1;
						break;
					}
					ChangeInPrevError = ChangeInNewError;
					PrevError = NewError;
					Iterator++;
				}
				
			}
			
		}
	
		//}
		TempVect = clearVector(TempVect);
		TempVect = copyVectorIndex(IterWiseSegPts, TempVect, index);
		//TempVect = copyVectorIndex(IterWiseSegPts, TempVect, IterWiseSegPts.size() -1);
		index = 0;
		while(index < (TempVect.size()-1)){
			Vect.add((Integer)TempVect.get(index++));
		}
		return Vect;
	}
	
	Vector copyVectorIndex(Vector IterWiseSegPts, Vector TempVect, int index){
		
		
		int LocalIndex = -1;
		Vector element = new Vector();
	//	Iterator iter = IterWiseSegPts.iterator();
		element = (Vector)IterWiseSegPts.get(index);
		// to iterate upto the vector index 
/*		while (LocalIndex < index){
			element = (Vector) iter.next();
			LocalIndex++;
		}
		
		// copy the relevant elements to TempVect
			LocalIndex = 0;
			Iterator iterator = element.iterator();
			while (LocalIndex < element.size()){
				if(LocalIndex == (element.size()-1)){
					TempVect.add((Double)element.get(LocalIndex++));
				}
				else{
				TempVect.add((Integer)element.get(LocalIndex++));
				}
			}*/
			for(LocalIndex = 0; LocalIndex < element.size() - 1; LocalIndex++){
				TempVect.add((Integer)element.get(LocalIndex));
			}
		return TempVect;
	}
	
	int findMinimumErrorIndex(Vector CurvVector, Vector CommonSegPts, Stroke theStroke){
		double minError = 9999.0;
		Collections.sort(CommonSegPts);
		double PrevError = calculateError(CommonSegPts, theStroke);
		double NewError;
		Vector Pts = new Vector();
		int CurvIndex = 0;
		int Index = -1;
		while(CurvIndex < CurvVector.size()){
			Pts = clearVector(Pts);
			Pts = copyVector(Pts, CommonSegmentPts);
			Pts.add((Integer) CurvVector.get(CurvIndex));
			Collections.sort(Pts);
			NewError = calculateError(Pts, theStroke);
			if((minError > NewError)&& ((PrevError - NewError) > 100.0) ){
				minError = NewError;
				Index = CurvIndex;
			}
			CurvIndex++;
		}
		
		return Index;
		
	}
}
