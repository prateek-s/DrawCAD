package dcad.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import dcad.Prefs;
import dcad.model.geometry.AnchorPoint;
import dcad.process.io.IOManager;
import dcad.process.preprocess.CurvatureBasedDetection;
import dcad.process.preprocess.SegmentPtDetectionScheme;
import dcad.process.preprocess.SpeedBasedDetection;
import dcad.process.preprocess.TimeBasedDetection;
import dcad.process.recognition.constraint.IndAngleRecognizer;
import dcad.process.recognition.constraint.RelAngleRecognizer;
import dcad.process.recognition.constraint.RelLengthRecognizer;
import dcad.ui.help.HelpView;


public class PropertyFileHandler
{
	private static PropertyFileHandler m_propHandler = null;
	private Properties m_prop;
	public static PropertyFileHandler getInstance()
	{
		if(m_propHandler == null) m_propHandler = new PropertyFileHandler();
		return m_propHandler;
	}
	
	private PropertyFileHandler()
	{
		m_prop = new Properties();
	}
	
	public void loadData(String theFileName)
	{
		try 
		{
//			System.out.println(theFileName);
			if(GMethods.getInputStream(theFileName)==null)
				System.out.println("!!! The property file couldn't be opened... !!! This is loadData() of PropertyFileHandler class. \n\n\n\n\n");
			m_prop.load(GMethods.getInputStream(theFileName));
	    } 
		catch (IOException e) 
	    {
	    	// let the value of variables be the default values itself
	    	return;
	    }
	    
	    String string = "";
	    // read all the properties one by one.
	    
	    // read the type of segmentation scheme to be used.
	    string = m_prop.getProperty("segScheme");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int type = Integer.parseInt(string.trim());
	    		Prefs.setSegScheme(type);
			} catch (NumberFormatException e)
			{
	    		Prefs.setSegScheme(GConstants.SEG_SCHEME_SPEED_CURVATURE);
			}
	    }
	    else
	    {
    		Prefs.setSegScheme(GConstants.SEG_SCHEME_SPEED_CURVATURE);
	    }
	    	

	    // read the type of acceptable number of votes for a segment point.
	    // This denotes minimum number of votes required for a point to be considered as segment point
	    string = m_prop.getProperty("acceptableVotes");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int acceptableVotes = Integer.parseInt(string.trim());
	    		Prefs.setAcceptableVotes(acceptableVotes);
			} catch (NumberFormatException e)
			{
	    		Prefs.setAcceptableVotes(SegmentPtDetectionScheme.DEF_ACCEPTABLE_VALUE);
			}
	    }
	    else
	    {
    		Prefs.setAcceptableVotes(SegmentPtDetectionScheme.DEF_ACCEPTABLE_VALUE);
	    }

	    // read the type of Anchor Point to be used.
	    string = m_prop.getProperty("apType");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int type = Integer.parseInt(string.trim());
	    		Prefs.setAnchorPtType(type);
			} catch (NumberFormatException e)
			{
	    		Prefs.setAnchorPtType(AnchorPoint.TYPE_CIRCLE);
			}
	    }
	    else
	    {
    		Prefs.setAnchorPtType(AnchorPoint.TYPE_CIRCLE);
	    }
	    
	    // read the size of anchor point to be used.
	    string = m_prop.getProperty("apSize");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int size = Integer.parseInt(string.trim());
	    		Prefs.setAnchorPtSize(size);
			} catch (NumberFormatException e)
			{
	    		Prefs.setAnchorPtSize(GConstants.DEF_AP_SIZE);
			}
	    }
	    else
	    {
    		Prefs.setAnchorPtSize(GConstants.DEF_AP_SIZE);
	    }

	    // read the type of Anchor Point to be used.
	    string = m_prop.getProperty("mpType");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int type = Integer.parseInt(string.trim());
	    		Prefs.setMovePtType(type);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMovePtType(GConstants.DEF_MP_TYPE);
			}
	    }
	    else
	    {
    		Prefs.setMovePtType(GConstants.DEF_MP_TYPE);
	    }

	    // read the size of move point to be used.
	    string = m_prop.getProperty("mpSize");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int size = Integer.parseInt(string.trim());
	    		Prefs.setMovePtSize(size);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMovePtSize(GConstants.DEF_MP_SIZE);
			}
	    }
	    else
	    {
    		Prefs.setMovePtSize(GConstants.DEF_MP_SIZE);
	    }

	    // read the size of max segment distance
	    string = m_prop.getProperty("maxSegDist");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int size = Integer.parseInt(string.trim());
	    		Prefs.setMaxSegDist(size);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMaxSegDist(GConstants.DEF_MAX_SEG_DIST);
			}
	    }
	    else
	    {
    		Prefs.setMaxSegDist(GConstants.DEF_MAX_SEG_DIST);
	    }

	    // read the size of show what type of constraints on what action
	    string = m_prop.getProperty("showConstraints");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int showConstraints = Integer.parseInt(string.trim());
	    		Prefs.setShowConstraints(showConstraints);
			} catch (NumberFormatException e)
			{
	    		Prefs.setShowConstraints(GConstants.SHOW_ALL_CONSTRAINTS);
			}
	    }
	    else
	    {
    		Prefs.setShowConstraints(GConstants.SHOW_ALL_CONSTRAINTS);
	    }
	    
	    // read the size of when to recalculate constraints
	    string = m_prop.getProperty("calcHardConstraints");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int calcHardConstraints = Integer.parseInt(string.trim());
	    		Prefs.setCalcHardConstraints(calcHardConstraints);
			} catch (NumberFormatException e)
			{
	    		Prefs.setCalcHardConstraints(GConstants.CALC_HARD_CONSTRAINTS_ON_MOUSE_RELEASE);
			}
	    }
	    else
	    {
    		Prefs.setCalcHardConstraints(GConstants.CALC_HARD_CONSTRAINTS_ON_MOUSE_RELEASE);
	    }
	    
	    // read the size of when to recalculate constraints
	    string = m_prop.getProperty("calcSoftConstraints");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int calcSoftConstraints = Integer.parseInt(string.trim());
	    		Prefs.setCalcSoftConstraints(calcSoftConstraints);
			} catch (NumberFormatException e)
			{
	    		Prefs.setCalcSoftConstraints(GConstants.CALC_SOFT_CONSTRAINTS_ON_MOUSE_RELEASE);
			}
	    }
	    else
	    {
    		Prefs.setCalcSoftConstraints(GConstants.CALC_SOFT_CONSTRAINTS_ON_MOUSE_RELEASE);
	    }
	    
	    // read the size of when to show the anchor points
	    string = m_prop.getProperty("showAnchorPoints");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int showAnchorPoints = Integer.parseInt(string.trim());
	    		Prefs.setShowAnchorPoints(showAnchorPoints);
			} catch (NumberFormatException e)
			{
	    		Prefs.setShowAnchorPoints(GConstants.SHOW_ANCHORPOINT_ALWAYS);
			}
	    }
	    else
	    {
    		Prefs.setShowAnchorPoints(GConstants.SHOW_ANCHORPOINT_ALWAYS);
	    }

	    // read the size of stroke to be considered as a marker
	    string = m_prop.getProperty("markerSize");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int markerSize = Integer.parseInt(string.trim());
	    		Prefs.setMarkerSize(markerSize);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMarkerSize(GConstants.DEF_MARKER_SIZE);
			}
	    }
	    else
	    {
    		Prefs.setMarkerSize(GConstants.DEF_MARKER_SIZE);
	    }
		
	    // in case the moved point location is infeasible, the points is 
		// to be moved to location as close as possible to the new location
	    string = m_prop.getProperty("closestLocIterations");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int closestLocIter = Integer.parseInt(string.trim());
	    		Prefs.setClosestLocIterations(closestLocIter);
			} catch (NumberFormatException e)
			{
	    		Prefs.setClosestLocIterations(GConstants.DEF_CLOSEST_LOCATION_ITERATIONS);
			}
	    }
	    else
	    {
    		Prefs.setClosestLocIterations(GConstants.DEF_CLOSEST_LOCATION_ITERATIONS);
	    }

	    // Max Limit on the number of Jacobian iteration while solving 
	    string = m_prop.getProperty("maxJacobianIterations");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int maxJacobIter = Integer.parseInt(string.trim());
	    		Prefs.setMaxJacobianIterations(maxJacobIter);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMaxJacobianIterations(GConstants.DEF_MAX_JACOBIAN_ITERATIONS);
			}
	    }
	    else
	    {
    		Prefs.setMaxJacobianIterations(GConstants.DEF_MAX_JACOBIAN_ITERATIONS);
	    }

	    // Min Limit on the number of Jacobian iteration while solving 
	    string = m_prop.getProperty("minJacobianIterations");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		int minJacobIter = Integer.parseInt(string.trim());
	    		Prefs.setMinJacobianIterations(minJacobIter);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMinJacobianIterations(GConstants.DEF_MIN_JACOBIAN_ITERATIONS);
			}
	    }
	    else
	    {
    		Prefs.setMinJacobianIterations(GConstants.DEF_MIN_JACOBIAN_ITERATIONS);
	    }

	    // The acceptable value of norm of the constraints vector
	    string = m_prop.getProperty("acceptableNorm");
	    if(string != null)
	    {
	    	// convert the value to double
	    	try
			{
	    		double acceptNorm = Double.parseDouble(string.trim());
	    		Prefs.setMinAcceptableNorm(acceptNorm);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMinAcceptableNorm(GConstants.DEF_ACCEPTABLE_NORM);
			}
	    }
	    else
	    {
    		Prefs.setMinAcceptableNorm(GConstants.DEF_ACCEPTABLE_NORM);
	    }
	    
	    // range near which the angle is considered to be important (in the case of 0/90/180/270)
	    string = m_prop.getProperty("indAngleLimit");
	    if(string != null)
	    {
	    	// convert the value to double
	    	try
			{
	    		double indAngleLimit = Double.parseDouble(string.trim());
	    		Prefs.setIndAngleLimit(indAngleLimit);
			} catch (NumberFormatException e)
			{
	    		Prefs.setIndAngleLimit(IndAngleRecognizer.DEF_MAX_ANGLE_TOL);
			}
	    }
	    else
	    {
    		Prefs.setIndAngleLimit(IndAngleRecognizer.DEF_MAX_ANGLE_TOL);
	    }

	    //range near which the angle is considered to be important (in the case of 0/90/180/270)
	    string = m_prop.getProperty("relAngleLimit");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double relAngleLimit = Double.parseDouble(string.trim());
	    		Prefs.setRelAngleLimit(relAngleLimit);
			} catch (NumberFormatException e)
			{
	    		Prefs.setRelAngleLimit(RelAngleRecognizer.DEF_MAX_ANGLE_TOL);
			}
	    }
	    else
	    {
    		Prefs.setRelAngleLimit(RelAngleRecognizer.DEF_MAX_ANGLE_TOL);
	    }

	    // If two line segmentents are equal by this much percent then they are considered equal
	    string = m_prop.getProperty("relLengthPercentage");
	    if(string != null)
	    {
	    	// convert the value to double
	    	try
			{
	    		double relLengthPercentage = Double.parseDouble(string.trim());
	    		Prefs.setRelLengthPercentage(relLengthPercentage);
			} catch (NumberFormatException e)
			{
	    		Prefs.setRelLengthPercentage(RelLengthRecognizer.DEF_MAX_EQUAL_LENGTH_TOL_PERCENT);
			}
	    }
	    else
	    {
    		Prefs.setRelLengthPercentage(RelLengthRecognizer.DEF_MAX_EQUAL_LENGTH_TOL_PERCENT);
	    }

	    // min acceptable value in the SVD diagonal which needs to be considered for inversion
	    string = m_prop.getProperty("acceptableInvertableSVDValue");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double acceptableSVDValue = Double.parseDouble(string.trim());
	    		Prefs.setMinAcceptableInvertableSVDValue(acceptableSVDValue);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMinAcceptableInvertableSVDValue(GConstants.DEF_MIN_ACCEPTABLE_SVD_VALUE);
			}
	    }
	    else
	    {
    		Prefs.setMinAcceptableInvertableSVDValue(GConstants.DEF_MIN_ACCEPTABLE_SVD_VALUE);
	    }
	    
	    // breakpoints are to be detected only between the min and max scaled values of the Average of time
	    string = m_prop.getProperty("minTimeScalingFactor");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double minTimeFactor = Double.parseDouble(string.trim());
	    		Prefs.setMinTimeScalingFactor(minTimeFactor);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMinTimeScalingFactor(TimeBasedDetection.DEF_MIN_SCALING_FACTOR);
			}
	    }
	    else
	    {
    		Prefs.setMinTimeScalingFactor(TimeBasedDetection.DEF_MIN_SCALING_FACTOR);
	    }
	    
	    string = m_prop.getProperty("maxTimeScalingFactor");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double maxTimeFactor = Double.parseDouble(string.trim());
	    		Prefs.setMaxTimeScalingFactor(maxTimeFactor);
			} catch (NumberFormatException e)
			{
	    		Prefs.setMaxTimeScalingFactor(TimeBasedDetection.DEF_MAX_SCALING_FACTOR);
			}
	    }
	    else
	    {
    		Prefs.setMaxTimeScalingFactor(TimeBasedDetection.DEF_MAX_SCALING_FACTOR);
	    }
	    
	    // breakpoints are to be detected only below the scaled values of the Average of speed;
	    string = m_prop.getProperty("speedScalingFactor");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double speedFactor = Double.parseDouble(string.trim());
	    		Prefs.setSpeedScalingFactor(speedFactor);
			} catch (NumberFormatException e)
			{
	    		Prefs.setSpeedScalingFactor(SpeedBasedDetection.DEF_SCALING_FACTOR);
			}
	    }
	    else
	    {
    		Prefs.setSpeedScalingFactor(SpeedBasedDetection.DEF_SCALING_FACTOR);
	    }
	    
	    // breakpoints are to be detected only above the scaled values of the Average of curvature;
	    string = m_prop.getProperty("curvatureScalingFactor");
	    if(string != null)
	    {
	    	// convert the value to integer
	    	try
			{
	    		double curvatureFactor = Double.parseDouble(string.trim());
	    		Prefs.setCurvatureScalingFactor(curvatureFactor);
			} catch (NumberFormatException e)
			{
	    		Prefs.setCurvatureScalingFactor(CurvatureBasedDetection.DEF_SCALING_FACTOR);
			}
	    }
	    else
	    {
    		Prefs.setCurvatureScalingFactor(CurvatureBasedDetection.DEF_SCALING_FACTOR);
	    }
	    
	    

	    string = m_prop.getProperty("promoteConstraints");
	    if(string != null)
	    {
    		if(string.charAt(0)=='1')
    			Prefs.setPromotionPreference(true);
    		else
    			Prefs.setPromotionPreference(false);
	    }
	    else
    		Prefs.setPromotionPreference(false);


	    string = m_prop.getProperty("lastDirectory");
	    
	    //Doing such things may give an error with applet.
//	    if(string != null)
//	    	IOManager.setLast_dir(string);

	    
	    string = m_prop.getProperty("helpURL");
	    if(string != null)
   			HelpView.baseURLForHelp = string;

	}

	public void storeData(String theFileName)
	{
		// Write properties file.
		try
		{
			FileReader fr = new FileReader(theFileName);
			try
			{
				fr.close();
			}catch (IOException e) {}
		} catch (FileNotFoundException e2)
		{	
	    	try
			{
	    		// create the file
				FileWriter fw = new FileWriter(theFileName);
				fw.close();
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	    try 
	    {
	        m_prop.store(new FileOutputStream(theFileName), null);
	    } catch (IOException e) 
	    {
	    }
	    
	    // set all the properties one by one through either default values 
	    // or through values of the global variables.
	    m_prop.setProperty("segScheme", "new value");
	}
}
