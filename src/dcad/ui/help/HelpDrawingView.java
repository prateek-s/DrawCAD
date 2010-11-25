package dcad.ui.help;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import dcad.Prefs;
import dcad.model.BoundingBox;
import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.circleArc.circularArcConstraint;
import dcad.model.constraint.collinearity.CollinearLinesConstraint;
import dcad.model.constraint.collinearity.CollinearPointsConstraint;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.constraint.length.EqualRelLengthConstraint;
import dcad.model.constraint.pointOnSegment.pointOnCircularCurveConstraint;
import dcad.model.constraint.pointOnSegment.pointOnLineConstraint;
import dcad.model.constraint.pointOnSegment.pointOnPointConstraint;
import dcad.model.constraint.points.NoMergeConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.io.Command;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.constraint.pointOnSegmentRecognizer;
import dcad.process.recognition.marker.MarkerRecogManager;
import dcad.process.recognition.marker.MarkerToConstraintConverter;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.help.HelpRecognizeView.SelectionListener;
import dcad.ui.main.ColorRenderer;
import dcad.ui.main.MainWindow;
//import dcad.ui.help.addJFrame;
//import dcad.ui.main.MyTableModel;
import dcad.ui.main.StatusBar;
import dcad.ui.main.WindowActions;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.Prefs;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**Class to help the user while drawing
 * It will highlight various possible actions user can do (in the help window)
 * @author Sunil Kumar
 */
public class HelpDrawingView extends JPanel  //implements ActionListener
{
	private JPanel m_panel = null; 
	JPanel panel = new JPanel(null);
    JScrollPane scrollpane = null;
    private static int rowNumber;   // it will keep track of which array index was previously accessed to
    								
    private final int DRAW_TABLE_NUMBER = 1;
    private static JTextArea textArea= null;
    private static int COLUMN_DIVIDER ;
    private static int TABLE_ROW_HEIGHT;
    private int tableHeight;
    private double winHeight;
    private final String tableDrawViewRowInfo[] = new String[]  // help information
                         {"To draw a stroke: \n\n 1. Keep left mouse button pressed in the \n Drawing View \n 2. And drag the mouse",
                          "To fix an element/anchor point: \n\n 1. Press Mouse middle button on it",
                          "To move an element/anchor point: \n\n 1. Press right mouse button on an element/ \n anchor point \n 2. And drag the mouse",
                          "To remove an anchor point:\n\n 1. press Shift key and left click on an anchor \n point",
                          "To add an anchor point to the stroke: \n\n 1. Keep Shift key pressed and mouse left \n click on the original stroke where the \n point is to be added.",
                          "To separate anchor points: \n\n 1. Keep Shift key pressed and  mouse right \n click on the anchor point.",
                          "To select an element: \n\n 1. Press Control key and click on  an element. \n 2. It will also show its related constraints in \n Constraint view",
                          "To remove an element:\n\n 1. press Ctrl key and click on an element to \n select it. \n 2. Now press Delete key to remove it",
                          "A character typed will be shown near \n mouse pointer",
                          "To align points horizontally: \n\n 1. Select both the points. \n\n 2. Press 'h' and ENTER key",
                          "To align points vertically: \n\n 1. Select both the points. \n\n 2. Press 'v' and ENTER key",
                          "To set distance between two elements: \n\n 1. Select both the elements (except between \ncircular arcs). \n 2. Type the distance and press ENTER key",
                          "To set the properties of an element:\n\n 1. Mouse left click on element  \n 2. It will open new window.  \n 3. Set properties.",
                          "To set equal length/radius of line/arc: \n\n 1. Draw line markers on both the elements.",
                          "To align two lines parallel: \n\n 1. Draw arrow markers on both the elements.",
                          "To align a line and an arc tangential: \n\n 1. Draw arrow markers on both the elements.",
                          "To set angle between two lines: \n\n 1. Draw angle marker between two lines. \n\n 2. Type the number and press ENTER key.",
                          "To set equal angle constraint for two \n pairs of lines: \n\n 1. Draw angle markers on both angles.",
                          "To align two lines perpendicular to each other: \n\n 1. Draw perpendicularity marker between two lines",
                          "To see various elements related to a constraint:\n\n 1. Highlight the constraint. \n\n 2. It will show its related elements in Drawing View",
                          "To delete a constraint from Constraint view: \n\n 1. Highlight the constraint. \n\n 2. Press DELETE key.",
                          "To convert line to arc and vice-versa: \n\n 1. Select the element. \n\n 2. Press Convert button in the toolbar.",
                          "To convert marker to normal stroke and \n vice-versa: \n\n 1. Press convert button in the toolbar.\n It will convert the last drawn stroke. ",
                         };
    
    private final int DRAW_STROKE = 0;
    private final int FIX_ELEMENT = 1;
    private final int MOVE_ELEMENT = 2;
    private final int REMOVE_AP = 3;
    private final int ADD_AP = 4;
    private final int SEPARATE_APS = 5;
    private final int SELECT_ELE = 6;
    private final int DELETE_ELE = 7;
    private final int TYPE_CHAR = 8;
    private final int ALIGN_HORIZONTALLY = 9;
    private final int ALIGN_VERTICALLY = 10;
    private final int DISTANCE_BETWEEN_ELES = 11;
   
    private final int SET_ELE_PROP = 12;

    private final int EQUAL_LEN_RAD_MARKER = 13;
    private final int ALIGN_LINES_PARALLEL = 14;
    private final int ALIGN_LINE_ARC_TANGENTIAL = 15;
    private final int ANGLE_BETWEEN_LINES = 16;
    private final int EQUAL_ANGLES = 17;
    private final int PERP_MARKER  = 18;
    private final int HIGHLIGHT_CONSTRAINT_REL_ELE = 19;
    private final int DELETE_CONSTRAINT = 20;
    private final int CONVERT_ELEMENT = 21;
    private final int CONVERT_STROKE = 22;
    
    // these arrays store on a particular action which table rows should be highlighted
    private final int LEFT_MOUSE_CLICK[] = {DRAW_STROKE};
    private final int MIDDLE_MOUSE_CLICK[] = {FIX_ELEMENT};
    private final int RIGHT_MOUSE_CLICK[] = {MOVE_ELEMENT};
    private final int HIGHLIGHT_ELEMENTS[] = {SET_ELE_PROP,FIX_ELEMENT,
    											MOVE_ELEMENT,SELECT_ELE, DRAW_STROKE };
    private final int SELECT_ELEMENT[] = {SELECT_ELE};
    private final int REMOVE_ANCHOR_POINT[] = {REMOVE_AP,ADD_AP,SEPARATE_APS};
    private final int TYPE_LETTERS[] = {TYPE_CHAR};
    private final int DELETE_ELEMENTS[] = {DELETE_ELE};
    private final int SELECT_POINTS[] = {ALIGN_HORIZONTALLY,ALIGN_VERTICALLY,DISTANCE_BETWEEN_ELES,
    										DELETE_ELE};
    private final int SELECT_POINT_LINE[] = {DISTANCE_BETWEEN_ELES,DELETE_ELE};
    private final int SELECT_POINT_ARC[] = {DISTANCE_BETWEEN_ELES,DELETE_ELE};
    private final int SELECT_LINE_ARC[] = {DISTANCE_BETWEEN_ELES,DELETE_ELE};
    private final int SELECT_POINT[] = {ALIGN_HORIZONTALLY,ALIGN_VERTICALLY,DISTANCE_BETWEEN_ELES,
    									DELETE_ELE};
    private final int SELECT_LINE[] = {DISTANCE_BETWEEN_ELES,
    									DELETE_ELE, CONVERT_ELEMENT};
    private final int SELECT_ARC[] = {DISTANCE_BETWEEN_ELES,
    									DELETE_ELE, CONVERT_ELEMENT};
    private final int CONSTRAINT_VIEW[] = {HIGHLIGHT_CONSTRAINT_REL_ELE, DELETE_CONSTRAINT};
    
    private final int HIGHLIGHT_POINT[] = {FIX_ELEMENT,MOVE_ELEMENT,REMOVE_AP,DRAW_STROKE,SELECT_ELE,SEPARATE_APS};
   
    private final int MOVE_DELETE[] = {MOVE_ELEMENT,DELETE_ELE,DISTANCE_BETWEEN_ELES};
    
    private final int CONVERT_STROKE_TYPE[] = {CONVERT_STROKE,DRAW_STROKE};
    
    private final int MARKER_EQUALITY[] = {EQUAL_LEN_RAD_MARKER,DRAW_STROKE};
    
    private final int MARKER_PARALLEL_ON_LINE[] = {ALIGN_LINES_PARALLEL, ALIGN_LINE_ARC_TANGENTIAL, DRAW_STROKE};
    
    private final int MARKER_PARALLEL_ON_ARC[] ={ALIGN_LINE_ARC_TANGENTIAL, DRAW_STROKE};
    
    private final int MARKER_PERP[] = {PERP_MARKER,DRAW_STROKE};
    
    private final int MARKER_ANGLE[] = {ANGLE_BETWEEN_LINES, EQUAL_ANGLES, DRAW_STROKE};
    
    private final int ADD_ANCHOR_PT[] = {ADD_AP};
    
  
    
    private final int HIGHLIGHT_ROWS[][]	=	{LEFT_MOUSE_CLICK,
    											MIDDLE_MOUSE_CLICK,
    											RIGHT_MOUSE_CLICK,
    											HIGHLIGHT_ELEMENTS,
    											SELECT_ELEMENT,
    											REMOVE_ANCHOR_POINT,
    											TYPE_LETTERS,
    											DELETE_ELEMENTS,
    											SELECT_POINTS,
    											SELECT_POINT_LINE,
    											SELECT_POINT_ARC,
    											SELECT_LINE_ARC,
    											SELECT_POINT,
    											SELECT_LINE,
    											SELECT_ARC,
    											CONSTRAINT_VIEW,
    											HIGHLIGHT_POINT,
    											MOVE_DELETE,
    											CONVERT_STROKE_TYPE,
    											MARKER_EQUALITY,
    											MARKER_PARALLEL_ON_LINE,
    											MARKER_PARALLEL_ON_ARC,
    											MARKER_PERP,
    											MARKER_ANGLE,
    											ADD_ANCHOR_PT
    											};
    
    private final String toolTipText[] = new String[]
                         {"Keep left mouse button pressed and drag will draw a stroke in Drawing View",
    					  "Mouse middle button click on an element/anchor point will fix it",
    					  "Press right mouse button on an element/anchor point and drag will move it in Drawing View",
    					  "Press Control key and click on an element/anchor point will select it and shows related constraints in constraint view",
    					  "Press Ctrl key, click on an element and press Delete key will remove it",
    					  "Press Shift key and left click on an anchor point will remove it",
    					  "A character typed will be shown near mouse pointer",
    					  "Highlight the arc/line and type number followed by 'a' will set the arc/line angle to specified degrees",
    					  "Highlight the arc, type length and press ENTER key will set the arc radius to specified length",
                         };
    
  
    private CustomTableCellRenderer custTabCellRendered = null;
    
    public CustomTableCellRenderer getCustTabCellRendered() {
		return custTabCellRendered;
	}
	public void setCustTabCellRendered(CustomTableCellRenderer custTabCellRendered) {
		this.custTabCellRendered = custTabCellRendered;
	}



	JTable table = null;

    public JTable getTable() {
		return table;
	}
	public void setTable(JTable table) {
		this.table = table;
	}

	public HelpDrawingView() {
		// TODO Auto-generated constructor stub
		
		COLUMN_DIVIDER = (2*getWidth())/5;
	}
	
	public JComponent DrawingWindow(){
		m_panel = createPanel();
		//scrollpane = new JScrollPane(m_panel);
		//addJFrame addFrame = new addJFrame();
		//addFrame.createAndShowGUI(scrollpane);
		tableHeight = TABLE_ROW_HEIGHT * table.getRowCount();
		//winHeight = m_panel.getHeight();
		return m_panel; 
	}
	
	public void setText(int row){
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double winHeight = dim.getHeight();
		//int tableHeight = table.getHeight();
		
		double changeInHeight = winHeight - tableHeight;
		changeInHeight = changeInHeight/GConstants.cmScaleDrawingRatio;
		/////System.out.println("Window Height = " + winHeight + "Table Height = " + tableHeight );
		/////System.out.println("ChangeInHeight = " + changeInHeight);
		double index;
		String appendString = "";
		
	/*	for(index=0.0; index<changeInHeight; index+=1.5){
			appendString+="\n";
		}
		*/ 
		textArea.setText(tableDrawViewRowInfo[row]+ appendString);
		}
	
	public void selectRows(int rowNum){
		rowNumber = rowNum;
		int col;
			for(col = 0; col < HIGHLIGHT_ROWS[rowNum].length; col++){
				if(col == 0){
					selectRow(HIGHLIGHT_ROWS[rowNum][col]);
				}
				else{
					addSelectedRow(HIGHLIGHT_ROWS[rowNum][col]);
				}
			}
	}
	
	public void unselectRows(){
		int col;
		for(col = 0; col < HIGHLIGHT_ROWS[rowNumber].length; col++){
			unselectRow(HIGHLIGHT_ROWS[rowNumber][col]);
		}
	}
	
	public void unselectRow(int rowNum){
		table.removeRowSelectionInterval(rowNum, rowNum);
	}
	
	
	public void selectRow(int rowNum){
		table.setRowSelectionInterval(rowNum, rowNum);
	}
	
	public void addSelectedRow(int rowNum){
		table.addRowSelectionInterval(rowNum, rowNum);
	}
	
	public JPanel createPanel()
	{
	    panel = new JPanel(new BorderLayout());
	    textArea = new JTextArea();
	    textArea.setEditable(false);
	    table = new JTable(new HelpTableModel()){    
    	    public String getToolTipText(MouseEvent e) {
    	        String tip = null;
    	        java.awt.Point p = e.getPoint();
    	        int rowIndex = rowAtPoint(p);
    	        int realRowIndex = convertRowIndexToModel(rowIndex);
    	        
    	/*        if((realRowIndex >= 0) && (realRowIndex < toolTipText.length)){
    	            tip = toolTipText[realRowIndex];
    	        }
    	  */
    	        if((realRowIndex >= 0) && (realRowIndex < tableDrawViewRowInfo.length)){
    	          //  tip =tableDrawViewRowInfo[realRowIndex];
    	            tip= "";
    	            setText(realRowIndex);
    	        }
    	        return tip;
    	    }
	    };

	    TABLE_ROW_HEIGHT = (3*table.getRowHeight())/2;
	    table.setRowSelectionAllowed(true);
	 
	    
	    custTabCellRendered = new CustomTableCellRenderer();	//add cell rendered to table
	    TableColumn tcol;
	    tcol = table.getColumnModel().getColumn(0);
	    tcol.setCellRenderer(custTabCellRendered);
	    tcol = table.getColumnModel().getColumn(1);
	    tcol.setCellRenderer(custTabCellRendered);
     
	    
	    SelectionListener listener = new SelectionListener(table);   // add selection listener to the table
	    table.getSelectionModel().addListSelectionListener(listener);
	    table.setDefaultRenderer(Color.class, new ColorRenderer(true));
	    this.setBackground(GVariables.BACKGROUND_COLOR);
	    table.getColumnModel().getColumn(1).setPreferredWidth(COLUMN_DIVIDER);
	    table.setRowHeight(TABLE_ROW_HEIGHT);
	    
	   
	    panel.add(table.getTableHeader(),BorderLayout.PAGE_START);
	    panel.add(table);
	    panel.add(textArea,BorderLayout.PAGE_END);
	    return panel;
	}

	
	class SelectionListener implements ListSelectionListener {
		  JTable table;

		  SelectionListener(JTable table) {
		    this.table = table;
		  }
		  public void valueChanged(ListSelectionEvent e) {
		   
		    if (e.getValueIsAdjusting()) {
		      /////System.out.println("The mouse button has not yet been released");
		    }
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
               // ///System.out.println("No rows are selected.");
            } else {
                int selectedRow = lsm.getMinSelectionIndex();
                /////System.out.println("Row " + selectedRow + " is now selected.");
                
                setText(selectedRow);
            }
		  }
		}
	
	
	
	public class CustomTableCellRenderer extends DefaultTableCellRenderer{
		
		public CustomTableCellRenderer(){
			
		}
	    public Component getTableCellRendererComponent (JTable table, 
	Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
	      Component cell = super.getTableCellRendererComponent(
	                         table, obj, isSelected, hasFocus, row, column);
	      if (isSelected) {
	        cell.setBackground(GVariables.FIXED_COLOR);
	      } 
	
	      else {
	        cell.setBackground(GVariables.BACKGROUND_COLOR);
	      }
	    
	      return cell;
	    }
	  }

}

class HelpTableModel extends AbstractTableModel
{
	public HelpTableModel() {
		// TODO Auto-generated constructor stub
		JTable table = new JTable(data, columnNames);
	}
    private String[] columnNames = {"Operation","Effect"};
    private Object[][] data = 
    {
    		// color codes for drawing
    		{"Left Button Pressed +\n Drag" , "Draw a Stroke"},
    		{"Middle Click", "Fix the element"},
    		{"Right Button Pressed +\n Drag" , "Move the element/s"},
    		{"Shift + Left click", "Remove the anchor point"},
    		{"Shift + Left Click ","Add an anchor point"},
    		{"Shift + Right Click","Separate anchor points"},
    		{"Ctrl + Left click", "Select the element/s"},
    		{"Select,  delete", "Delete the element"},
    		{"Type a Character", "Show near mouse pointer"},
    		{"Select points, h", "Align Horizontally"},
    		{"Select points, v", "Align vertically"},
    		{"Select elements, distance","Set distance in cm"},
    		{"Left click on it, enter values","Set properties "},
    		{"Line marker on lines/arcs","Set equal length/radius"},
    		{"Arrow Marker on lines","Align them parallel"},
    		{"Arrow Marker on line and arc", "Align them tangentially"},
    		{"Angle Marker between Lines, enter angle","Set angle in degrees"},
    		{"Angle Marker","Equal Angles"},
    		{"Perpendicularity Marker","Align lines perpendicular"},
    		{"Highlight Constraint","Show related elements"},
    		{"Highlight constraint + delete", "Delete the constraint"}, 
    		{"Select element, press Convert button", "Convert line to arc and vice-versa"},
    		{"Press Convert button", "Convert marker to normal stroke and vice-versa"},
    };

    public int getColumnCount() 
    {
        return columnNames.length;
    }

    public int getRowCount() 
    {
        return data.length;
    }

    public String getColumnName(int col) 
    {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) 
    {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) 
    {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) 
    {
            return false;
    }
    
   
}
