package dcad.ui.main;

import java.awt.Point;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.segment.Segment;


/**
 * Not  an interface as the name suggests.
 * This is the class containing all the actions that are supported. For example, segment_delete, is an action.
 * Mouse clicked is not. However, segment_select is an action. 
 * Should these be handled in Segment and associated classes? No. These are 'global' methods, since in the program any action can potentially 
 * change the entire drawing completely. As a result, seg_property_change not only changes property of the segment, but also of other segments. 
 * @author prateek
 *
 */

/**
 * Design notes. 
 * This class essentially contains the list of all the actions that any UI must support. For example, UI can either be java-awt, log-driven, web-based, etc. 
 * Thus, the class serves as the interface for all the UI actions. The methods below have been carefully chosen and are hopefully an exhaustive and orthogonal 
 * collection of actions supported by the program. If any capability needs to be added, it should be done *HERE* first, tested, and then later modify the UI to call 
 * the added action.
 * Most methods do not need to inform the UI of anything. UI can assume that once the method is called and returns, the intended action has been either successfully 
 * performed or if there is an error, the program raises the appropriate exception. However, to facilitate a tighter integration with the UI, integer return codes
 * have been provided. These allow the UI to keep some track of the internal state of the program. Ideally, UI should have no business with the state of the program. 
 * 
 */

public class ActionInterface 
{
	/*
	 * The local variables of this class together form a subset of the state of the entire program. 
	 * The state is divided into these categories:
	 * UI State: handled exclusively by *View. Mainly drawing view.
	 * ActionItem state: This class
	 * Drawing State: Handled bydrawing data, and invariably, by the segments and strokes which the drawing is composed of. .
	 */


	
	public int A_draw_Stroke(Vector points) {
		return 0 ;
	}

	public int A_delete_Segment(Segment seg) {
		
	}
	
	public int A_move_Segment(Segment seg, Point to) {
		
	}
	
	public int A_change_seg_property(Segment seg, String property_type, String value) {
	
	}
	
	public int A_undo(int count) {
		
	}
	
	public int A_redo(int count) {
		
	}
	
	public int A_change_to_marker(Stroke strk) {
		
	}
	
	public int A_change_marker_to_segment(Stroke strk) {
		
	}
	
	public int A_add_anchor_point(Point pt) {
		
	}
	
	public int A_delete_anchor_point(Point pt) {
		
	}
	
	public int A_add_text(String text, Point location) {
		
	}
	
	public int A_delete_text(Point location) {
		
	}
	
	public int A_change_Segment_to(Segment seg, int seg_type) {
			
	}
	
	public int A_clear() {
		
	}
	
	public int A_load() {
		
	}
	
	public int A_save() {
		
	}
	
	public int A_add_constraint(Constraint constraint) {
		
	}
	
	public int A_delete_constraint(Constraint constraint) {
		
		
	}
	
		
	public Segment A_seg_selected(Point pt) {
		
		
	}
	
	public GeometryElement A_element_selected(Point pt) {
		
		
	}
	
	/**********************************************************************************************************************/
	
	public ActionInterface() {
		// TODO Auto-generated constructor stub
	}
	
	

/********************* END OF CLASS ***********************************/
}
