package dcad.util;

import java.awt.Toolkit;
import java.util.Vector;

import dcad.process.beautification.ConstraintSolver;



/**
 * Class for holding Global constants related to the Application 
 * @author vishalk
 *
 */
public class GConstants 
{
	public static final String APP_NAME = "DrawCAD";
	
	public static final String IMAGE_SRC = "src/images/icons/";
	
	// constants for decoration of basepoints
	public static final int DEF_AP_SIZE = 5;
	public static final int DEF_MP_SIZE = 3;
	public static final int DEF_MP_TYPE = 3;
	
	// default segmentation scheme value
	public static final int SEG_SCHEME_ALL=0;
	public static final int SEG_SCHEME_CURVATURE=1;
	public static final int SEG_SCHEME_TIME=2;
	public static final int SEG_SCHEME_SPEED=3;
	public static final int SEG_SCHEME_SPEED_CURVATURE=4;
	public static final int  SEG_SCHEME_SIMPLE = 5 ;
	
	public static final int DRAW_MODE = 0;
	public static final int EDIT_MODE = 1;

	public static final int SHOW_ALL_CONSTRAINTS = 0;
	public static final int SHOW_SOFT_CONSTRAINTS = 1;
	public static final int SHOW_HARD_CONSTRAINTS = 2;
	public static final int SHOW_ALL_CONSTRAINTS_ON_HL = 3;
	public static final int SHOW_SOFT_CONSTRAINTS_ON_HL = 4;
	public static final int SHOW_HARD_CONSTRAINTS_ON_HL = 5;
	public static final int SHOW_SOFT_ON_HL_AND_HARD_CONSTRAINTS = 6;
	public static final int SHOW_NO_CONSTRAINTS = 7;
	
	public static final int DEF_MARKER_SIZE = 70;

	public static final int CALC_SOFT_CONSTRAINTS_ON_MOUSE_RELEASE = 0;
	public static final int CALC_SOFT_CONSTRAINTS_ON_FLY = 1;
	
	public static final int CALC_HARD_CONSTRAINTS_ON_MOUSE_RELEASE = 0;
	public static final int CALC_HARD_CONSTRAINTS_ON_FLY = 1;
	
	public static final int SHOW_ANCHORPOINT_ALWAYS = 0;
	public static final int SHOW_ANCHORPOINT_ON_HL = 1;
	
	public static final String BREAK = "BREAK;";
	public static final int DEF_MAX_SEG_DIST = 10;
	
	/**
	 * min acceptable value in the SVD diagonal which needs to be considered for inversion
	 */
	public static final double DEF_MIN_ACCEPTABLE_SVD_VALUE = 0.001;

	/**
	 * The acceptable value of norm of the constraints vector
	 */
	//public static final double DEF_ACCEPTABLE_NORM = 0.0001;
	public static final double DEF_ACCEPTABLE_NORM = 5;

	/**
	 * in case the moved point location is infeasible, the points is 
	 * to be moved to location as close as possible to the new location
	 */
	public static final int DEF_CLOSEST_LOCATION_ITERATIONS = 10;
	
	/**
	 * Max Limit on the number of Jacobian iteration while solving 
	 */
	public static final int DEF_MAX_JACOBIAN_ITERATIONS = 100;
	/**
	 * Max Limit on the number of Jacobian iteration while solving 
	 */
	public static final int DEF_MIN_JACOBIAN_ITERATIONS = 30;
	
	public static final String codeBase="http://www.cse.iitb.ac.in/~chintan/DrawCAD/";
	
	public static double drawingRatio = -1;
	// 3-10-09
	public static final int DEPTH_PER_INCHES =Toolkit.getDefaultToolkit().getScreenResolution();
	public static final double cmScaleDrawingRatio = DEPTH_PER_INCHES/2.54;
	public static final double mmScaleDrawingRatio = cmScaleDrawingRatio/10;
	public static final int LEFT_CLICK = 0;
	public static final int MIDDLE_CLICK = 1;
	public static final int RIGHT_CLICK = 2;
	public static final int HIGHLIGHT_ELEMENTS = 3;
	public static final int SELECT_ELEMENT = 4;
	public static final int REMOVE_ANCHOR_POINT = 5;
	public static final int TYPE_LETTERS = 6;
	public static final int DELETE_ELEMENTS = 7;
	public static final int SELECT_POINTS = 8;
	public static final int SELECT_POINT_LINE = 9;
	public static final int SELECT_POINT_ARC = 10;
	public static final int SELECT_LINE_ARC = 11;
	public static final int SELECT_POINT = 12;
	public static final int SELECT_LINE = 13;
	public static final int SELECT_ARC = 14;
	public static final int CONSTRAINT_VIEW = 15;
	public static final int HIGHLIGHT_POINT = 16;
	public static final int MOVE_DELETE_ELEMENTS = 17;
	public static final int CONVERT_STROKE_TYPE = 18;
	public static final int MARKER_EQUALITY  = 19;
	public static final int MARKER_PARALLEL_ON_LINE  = 20;
	public static final int MARKER_PARALLEL_ON_ARC = 21;
	public static final int MARKER_PERP  = 22;
	public static final int MARKER_ANGLE  = 23;
	public static final int ADD_AP = 24;

}
