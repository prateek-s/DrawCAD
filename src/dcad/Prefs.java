package dcad;

import dcad.model.geometry.AnchorPoint;
import dcad.process.preprocess.CurvatureBasedDetection;
import dcad.process.preprocess.SegmentPtDetectionScheme;
import dcad.process.preprocess.SpeedBasedDetection;
import dcad.process.preprocess.TimeBasedDetection;
import dcad.process.recognition.constraint.IndAngleRecognizer;
import dcad.process.recognition.constraint.RelAngleRecognizer;
import dcad.process.recognition.constraint.RelLengthRecognizer;
import dcad.util.GConstants;

public class Prefs
{
	private static int segScheme=5;
	private static int acceptableVotes;
	private static int anchorPtType;
	private static int anchorPtSize;
	private static int movePtType;
	private static int movePtSize;
	private static int maxSegDist; 
	private static int showConstraints;
	private static int calcHardConstraints; 
	private static int calcSoftConstraints; 
	private static int showAnchorPoints; 
	private static int markerSize; 
	private static int closestLocIterations; 
	private static int maxJacobianIterations; 
	private static int minJacobianIterations; 
	private static double minAcceptableNorm;
	private static double minAcceptableInvertableSVDValue;
	private static double minTimeScalingFactor;
	private static double maxTimeScalingFactor;
	private static double speedScalingFactor;
	private static double curvatureScalingFactor;
	private static double indAngleLimit;
	private static double relAngleLimit;
	private static double relLengthPercentage;
	private static boolean promoteConstraints;
	public static String helpURL;

	public static int getSegScheme()
	{
		return segScheme;
	}
	
	public static void setSegScheme(int scheme)
	{
		if((scheme == GConstants.SEG_SCHEME_ALL)
			|| (scheme == GConstants.SEG_SCHEME_CURVATURE)
			|| (scheme == GConstants.SEG_SCHEME_SPEED)
			|| (scheme == GConstants.SEG_SCHEME_TIME)
			|| (scheme == GConstants.SEG_SCHEME_SPEED_CURVATURE) 
			|| (scheme == GConstants.SEG_SCHEME_SIMPLE))
			
				segScheme = scheme;
		
		else segScheme = GConstants.SEG_SCHEME_SIMPLE;
//		///System.out.println("scheme: "+scheme);
	}

	public static int getAnchorPtType()
	{
		return anchorPtType;
	}

	public static void setAnchorPtType(int apType)
	{
		if((apType == AnchorPoint.TYPE_CIRCLE)
			|| (apType == AnchorPoint.TYPE_SQUARE)
			|| (apType == AnchorPoint.TYPE_TRIANGLE)) anchorPtType = apType;
		else anchorPtType = AnchorPoint.TYPE_CIRCLE;
//		///System.out.println("anchorPtType: "+anchorPtType);
	}

	public static int getAnchorPtSize()
	{
		return anchorPtSize;
	}

	public static void setAnchorPtSize(int apSize)
	{
		if((apSize >= 1) && (apSize <= 5)) anchorPtSize = apSize;
		else anchorPtSize = GConstants.DEF_AP_SIZE;
		//		///System.out.println("anchorPtSize: "+anchorPtSize);
	}

	public static int getMovePtSize()
	{
		return movePtSize;
	}

	public static void setMovePtSize(int mvPtSize)
	{
		if((mvPtSize >= 1) && (mvPtSize <= 5)) movePtSize = mvPtSize;
		else movePtSize = GConstants.DEF_AP_SIZE;
		//		///System.out.println("movePtSize: "+movePtSize);
	}

	public static int getMovePtType()
	{
		return movePtType;
	}

	public static void setMovePtType(int mvPtType)
	{
		if((mvPtType == AnchorPoint.TYPE_CIRCLE)
				|| (mvPtType == AnchorPoint.TYPE_SQUARE)
				|| (mvPtType == AnchorPoint.TYPE_TRIANGLE)) movePtType = mvPtType;
		else movePtType = AnchorPoint.TYPE_CIRCLE;
		//		///System.out.println("movePtType: "+movePtType);
	}

	public static int getCalcHardConstraints()
	{
		return calcHardConstraints;
	}

	public static void setCalcHardConstraints(int calcCons)
	{
		if((calcCons == GConstants.CALC_HARD_CONSTRAINTS_ON_MOUSE_RELEASE)
				|| (calcCons == GConstants.CALC_HARD_CONSTRAINTS_ON_FLY)) calcHardConstraints = calcCons;
		else calcHardConstraints = GConstants.CALC_HARD_CONSTRAINTS_ON_MOUSE_RELEASE;
		//		///System.out.println("calcHardConstraints: "+calcHardConstraints);
	}

	public static int getCalcSoftConstraints()
	{
		return calcSoftConstraints;
	}

	public static void setCalcSoftConstraints(int calcCons)
	{
		if((calcCons == GConstants.CALC_SOFT_CONSTRAINTS_ON_MOUSE_RELEASE)
				|| (calcCons == GConstants.CALC_SOFT_CONSTRAINTS_ON_FLY)) calcSoftConstraints = calcCons;
		else calcSoftConstraints = GConstants.CALC_SOFT_CONSTRAINTS_ON_MOUSE_RELEASE;
		//		///System.out.println("calcSoftConstraints: "+calcSoftConstraints);
	}

	public static int getMaxSegDist()
	{
		return maxSegDist;
	}

	public static void setMaxSegDist(int mSegDist)
	{
		if((mSegDist >= 0) && (mSegDist <= GConstants.DEF_MAX_SEG_DIST)) maxSegDist = mSegDist;
		else maxSegDist = GConstants.DEF_MAX_SEG_DIST;
		//		///System.out.println("maxSegDist: "+maxSegDist);
	}

	public static int getShowAnchorPoints()
	{
		return showAnchorPoints;
	}

	public static void setShowAnchorPoints(int showAP)
	{
		if((showAP == GConstants.SHOW_ANCHORPOINT_ALWAYS) 
				|| (showAP == GConstants.SHOW_ANCHORPOINT_ON_HL)) showAnchorPoints = showAP;
		else showAnchorPoints = showAP;
		//		///System.out.println("showAnchorPoints: "+showAnchorPoints);
	}

	public static int getShowConstraints()
	{
		return showConstraints;
	}

	public static void setShowConstraints(int showCons)
	{
		if((showCons == GConstants.SHOW_ALL_CONSTRAINTS)
				|| (showCons == GConstants.SHOW_SOFT_CONSTRAINTS)
				|| (showCons == GConstants.SHOW_HARD_CONSTRAINTS)
				|| (showCons == GConstants.SHOW_ALL_CONSTRAINTS_ON_HL)
				|| (showCons == GConstants.SHOW_SOFT_CONSTRAINTS_ON_HL)
				|| (showCons == GConstants.SHOW_HARD_CONSTRAINTS_ON_HL)
				|| (showCons == GConstants.SHOW_SOFT_ON_HL_AND_HARD_CONSTRAINTS)
				|| (showCons == GConstants.SHOW_NO_CONSTRAINTS)) showConstraints = showCons;
		else showConstraints = GConstants.SHOW_SOFT_ON_HL_AND_HARD_CONSTRAINTS;
		//		///System.out.println("showConstraints: "+showConstraints);
	}

	public static int getMarkerSize()
	{
		return markerSize;
	}

	public static void setMarkerSize(int markSize)
	{
		if((markSize >= 10) && (markSize <= 100)) markerSize = markSize;
		else markerSize = GConstants.DEF_MARKER_SIZE;
		//		///System.out.println("markerSize: "+markerSize);
	}

	public static double getMinAcceptableNorm()
	{
		return minAcceptableNorm;
	}

	public static void setMinAcceptableNorm(double acceptNorm)
	{
//		if((acceptNorm >= 0.00001) && (acceptNorm <= 1)) minAcceptableNorm = acceptNorm;
//		else minAcceptableNorm = GConstants.DEF_ACCEPTABLE_NORM;
		if((acceptNorm >= 0.001) && (acceptNorm <= 25)) minAcceptableNorm = acceptNorm;
		else minAcceptableNorm = GConstants.DEF_ACCEPTABLE_NORM;
		//		///System.out.println("acceptableNorm: "+minAcceptableNorm);
	}

	public static double getMinAcceptableInvertableSVDValue()
	{
		return minAcceptableInvertableSVDValue;
	}

	public static void setMinAcceptableInvertableSVDValue(double acceptableSVDValue)
	{
		if((acceptableSVDValue >= 0.00001) && (acceptableSVDValue <= 1)) minAcceptableInvertableSVDValue = acceptableSVDValue;
		else minAcceptableInvertableSVDValue = GConstants.DEF_MIN_ACCEPTABLE_SVD_VALUE;
		//		///System.out.println("acceptableInvertableSVDValue: "+minAcceptableInvertableSVDValue);
	}

	public static int getClosestLocIterations()
	{
		return closestLocIterations;
	}

	public static void setClosestLocIterations(int closestLocIter)
	{
		if((closestLocIter >= 2) && (closestLocIter <= 100)) closestLocIterations = closestLocIter;
		else closestLocIterations = GConstants.DEF_CLOSEST_LOCATION_ITERATIONS;
		//		///System.out.println("closestLocIterations: "+closestLocIterations);
	}

	public static int getMaxJacobianIterations()
	{
		return maxJacobianIterations;
	}

	public static void setMaxJacobianIterations(int maxJacobIter)
	{
		if((maxJacobIter >= 100) && (maxJacobIter <= 200)) maxJacobianIterations = maxJacobIter;
		else maxJacobianIterations = GConstants.DEF_MAX_JACOBIAN_ITERATIONS;
		//		///System.out.println("maxJacobianIterations: "+maxJacobianIterations);
	}

	public static int getMinJacobianIterations()
	{
		return minJacobianIterations;
	}

	public static void setMinJacobianIterations(int minJacobIter)
	{
		if((minJacobIter >= 2) && (minJacobIter <= 99)) minJacobianIterations = minJacobIter;
		else minJacobianIterations = GConstants.DEF_MIN_JACOBIAN_ITERATIONS;
		//		///System.out.println("minJacobianIterations: "+minJacobianIterations);
	}

	public static double getCurvatureScalingFactor()
	{
		return curvatureScalingFactor;
	}

	public static void setCurvatureScalingFactor(double curvatureFactor)
	{
		if((curvatureFactor >= 1) && (curvatureFactor <= 10)) curvatureScalingFactor = curvatureFactor;
		else curvatureScalingFactor = CurvatureBasedDetection.DEF_SCALING_FACTOR;
		//		///System.out.println("curvatureScalingFactor: "+curvatureScalingFactor);
	}

	public static double getMaxTimeScalingFactor()
	{
		return maxTimeScalingFactor;
	}

	public static void setMaxTimeScalingFactor(double maxTimeFactor)
	{
		if((maxTimeFactor >= 1) && (maxTimeFactor <= 5) && (maxTimeFactor > minTimeScalingFactor)) maxTimeScalingFactor = maxTimeFactor;
		else maxTimeScalingFactor = TimeBasedDetection.DEF_MAX_SCALING_FACTOR;
		//		///System.out.println("maxTimeScalingFactor: "+maxTimeScalingFactor);
	}

	public static double getMinTimeScalingFactor()
	{
		return minTimeScalingFactor;
	}

	public static void setMinTimeScalingFactor(double minTimeFactor)
	{
		if((minTimeFactor >= 1) && (minTimeFactor <= 5) && (minTimeFactor < maxTimeScalingFactor)) minTimeScalingFactor = minTimeFactor;
		else minTimeScalingFactor = TimeBasedDetection.DEF_MIN_SCALING_FACTOR;
		//		///System.out.println("minTimeScalingFactor: "+minTimeScalingFactor);
		Prefs.minTimeScalingFactor = minTimeScalingFactor;
	}

	public static double getSpeedScalingFactor()
	{
		return speedScalingFactor;
	}

	public static void setSpeedScalingFactor(double speedFactor)
	{
		//if((speedFactor >= 0.1) && (speedFactor <= 1)) 
		speedScalingFactor = speedFactor;
	//	else speedScalingFactor = SpeedBasedDetection.DEF_SCALING_FACTOR;
		//		///System.out.println("speedScalingFactor: "+speedScalingFactor);
		//Prefs.speedScalingFactor = speedScalingFactor;
	}

	public static int getAcceptableVotes()
	{
		return acceptableVotes;
	}

	public static void setAcceptableVotes(int acceptableVotes)
	{
		if(acceptableVotes >= 1) Prefs.acceptableVotes = acceptableVotes;
		else Prefs.acceptableVotes = SegmentPtDetectionScheme.DEF_ACCEPTABLE_VALUE;
		//		///System.out.println("acceptableVotes: "+acceptableVotes);
	}

	public static double getIndAngleLimit()
	{
		return indAngleLimit;
	}

	public static void setIndAngleLimit(double indAngleLimit)
	{
		if((indAngleLimit >=0.1) && (indAngleLimit <=10.0)) Prefs.indAngleLimit = indAngleLimit;
		else Prefs.indAngleLimit = IndAngleRecognizer.DEF_MAX_ANGLE_TOL;
		//		///System.out.println("indAngleLimit: "+indAngleLimit);
	}

	public static double getRelAngleLimit()
	{
		return relAngleLimit;
	}

	public static void setRelAngleLimit(double relAngleLimit)
	{
		if((relAngleLimit >=0.1) && (relAngleLimit <=10.0)) Prefs.relAngleLimit = relAngleLimit;
		else Prefs.relAngleLimit = RelAngleRecognizer.DEF_MAX_ANGLE_TOL;
		//		///System.out.println("relAngleLimit: "+relAngleLimit);
	}

	public static double getRelLengthPercentage()
	{
		return relLengthPercentage;
	}

	public static void setRelLengthPercentage(double relLengthPercentage)
	{
		if((relLengthPercentage >=0.1) && (relLengthPercentage <=10.0)) Prefs.relLengthPercentage = relLengthPercentage;
		else Prefs.relLengthPercentage = RelLengthRecognizer.DEF_MAX_EQUAL_LENGTH_TOL_PERCENT;
		//		///System.out.println("relLengthPercentage: "+relLengthPercentage);
	}
	public static boolean getPromotionPreference()
	{
		return promoteConstraints;
	}
	public static void setPromotionPreference(boolean promotionPreference)
	{
		promoteConstraints=promotionPreference;
	}
}