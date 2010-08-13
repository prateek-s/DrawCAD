package dcad.ui.help;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.plaf.FontUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import dcad.ui.help.HelpDrawingView.CustomTableCellRenderer;
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
import java.awt.event.MouseEvent;
import javax.swing.table.TableModel;
/**Class to help the user while drawing
 * It will highlight various possible actions user can do (in the help window)
 * @author Sunil Kumar
 */
public class HelpRecognizeView extends JPanel 
{
	private JPanel m_panel = null; 
	private static JPanel panel = new JPanel(null);
    JScrollPane scrollpane = null;
    private static JTable table;
    private JTextArea textArea= null;
    private final int RECOZNIZE_TABLE_NUMBER = 2;
    private static int COLUMN_DIVIDER ;
    private static int TABLE_ROW_HEIGHT;
    private CustomTableCellRenderer custTabCellRendered = null;
    private HelpDrawingView helpDrawView = null;
    
    private final String tableRecogViewRowInfo[]=new String[]
             {"Mouse hover on a constraint in the Recognize \n View : \n\n It will show the elements related to this \n constraint in the drawing view\n\n\n",
              "Mouse hover on a constraint in the Recognize \n View  \n\n Now Press DELETE key will delete the selected \n constraint from Drawing View\n\n\n",
    		};
    private final String toolTipText[] = new String[]
             { "Mouse hover on the constraint will highlight its related elements in Drawing View",
    			"Mouse hover on the constraint and press Delete key will delete it",                                     
             };
   
    public HelpRecognizeView() {
		// TODO Auto-generated constructor stub
    		COLUMN_DIVIDER = (2*getWidth())/5;
	}
    	
	public JComponent constraintWindow(){
		m_panel = createPanel();
		scrollpane = new JScrollPane(m_panel);
		//addJFrame addFrame = new addJFrame();
		//addFrame.createAndShowGUI(scrollpane);
		return scrollpane; 
	}
	
	public void setText(int row){
		textArea.setText(tableRecogViewRowInfo[row]);
	}
	
	
	public JPanel createPanel(){
	    panel = new JPanel(new BorderLayout());
	    textArea = new JTextArea();
	    textArea.setEditable(false);

	    table = new JTable(new RecognizerTableModel()){    
	    	    //Implement table cell tool tips.
	    	    public String getToolTipText(MouseEvent e) {
	    	        String tip = null;
	    	        java.awt.Point p = e.getPoint();
	    	        int rowIndex = rowAtPoint(p);
	    	        //int colIndex = columnAtPoint(p);
	    	        int realRowIndex = convertRowIndexToModel(rowIndex);

	    	        if((realRowIndex >= 0) && (realRowIndex < toolTipText.length)){
	    	            tip = toolTipText[realRowIndex];
	    	        }
	    	        return tip;
	    	    }

	    };
	    
	    TABLE_ROW_HEIGHT = (3*table.getRowHeight())/2;
	    //table.setColumnSelectionAllowed(false);
	    //table.setCellSelectionEnabled(false);
	    table.setRowSelectionAllowed(true);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    helpDrawView = MainWindow.getHelpDrawingView();
	    custTabCellRendered = helpDrawView.new CustomTableCellRenderer();//add cell rendered to table
	    TableColumn tcol;
	    tcol = table.getColumnModel().getColumn(0);
	    tcol.setCellRenderer(custTabCellRendered);
	    tcol = table.getColumnModel().getColumn(1);
	    tcol.setCellRenderer(custTabCellRendered);
	    SelectionListener listener = new SelectionListener(table);
	    table.getSelectionModel().addListSelectionListener(listener);
       
        
	    table.setDefaultRenderer(Color.class, new ColorRenderer(true));
	    
	    this.setBackground(GVariables.BACKGROUND_COLOR);
	   
	    table.getColumnModel().getColumn(1).setPreferredWidth(COLUMN_DIVIDER);
	        table.setRowHeight(TABLE_ROW_HEIGHT);
	    
	    panel.add(table.getTableHeader(), BorderLayout.PAGE_START);
	    panel.add(table);
	    panel.add(textArea,BorderLayout.PAGE_END);
	    return panel;
	}
	
	// it deals with selsections related to table
	
	class SelectionListener implements ListSelectionListener {
		  JTable table;

		  SelectionListener(JTable table) {
		    this.table = table;
		  }
		  public void valueChanged(ListSelectionEvent e) {
		   
		    if (e.getValueIsAdjusting()) {
		     // System.out.println("The mouse button has not yet been released");
		    }
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
               // System.out.println("No rows are selected.");
            } else {
                int selectedRow = lsm.getMinSelectionIndex();
                //System.out.println("Row " + selectedRow + " is now selected.");
                
                setText(selectedRow);
            }
		  }
		}
}

class RecognizerTableModel extends AbstractTableModel 
{
	public RecognizerTableModel() {
		// TODO Auto-generated constructor stub
		JTable table = new JTable(data, columnNames);
	}
    private String[] columnNames = {"Operation","Effect"};
    private Object[][] data = 
    {
     		{"Mouse Hover" , "Highlight related elements"},
    		{"Mouse Hover + Delete", "Delete constraint"},
    	
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
