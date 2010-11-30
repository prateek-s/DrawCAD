 package dcad.ui.main;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

//import org.GNOME.Accessibility.Application;

import dcad.process.io.IOManager;
import dcad.ui.drawing.DrawingView;
import dcad.ui.drawing.HorizontalScale;
import dcad.ui.drawing.VerticalScale;
import dcad.ui.help.HelpDrawingView;
import dcad.ui.help.HelpRecognizeView;
import dcad.ui.help.HelpView;
import dcad.ui.main.EditView; 
import dcad.ui.main.TempClass;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;


public class MainWindow extends ScrollPane implements ActionListener, AdjustmentListener
{
	private static StatusBar m_statusBar;
	private static ToolBar m_toolBar;
	private static Cursor m_defCursor = null;
	private static Logger m_logger = Logger.getLogger("global");
	private static MainWindow application = null;
	private static HelpDrawingView  helpDrawingView= null;
	private static HelpRecognizeView helpRecognizeView = null;
	
	private static EditView ev = null ;

	private static HorizontalScale hs=null;
	private static VerticalScale vs=null;
	private static DrawingView dv = null;
	private static TempClass tc = null;
	
	private static JSplitPane m_splitpane = null;
	private static JSplitPane m_upperPane = null;
	private static JSplitPane m_LowerPane = null;
	private static JSplitPane m_LowerlowerPane = null ;
	private static JSplitPane m_hzScaleDrawView = null;
	private static JSplitPane m_vtScaleDraw = null;
	private static JSplitPane m_LeftSplitPane = null;
	
	
	private static JComponent drawingTab;
	private static JComponent helpTab;
	private static JComponent recognizedTab;
	private static JComponent helpSubTab;
	private static JComponent helpRecognizeTab;
	private static JComponent horizontalScale;
	private static JComponent verticalScale;
	private static JComponent tempclass;
	private static JComponent EditTab ;
	 
	private static int winHeight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private static int winWidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private static int TOP_HORIZONTAL_DIVIDER = winHeight/25;
	private static int LEFT_VERTICAL_DIVIDER = winWidth/35;
	private static int RIGHT_VERTICAL_DIVIDER = (3*winWidth)/4;
	private static int EDIT_DIVIDER = (2*winHeight)/3;
	private static int BOTTOM_DIVIDER = (3*winHeight)/4;
	
	
	public static HelpDrawingView getHelpDrawingView() {
		return helpDrawingView;
	}

	public static void setHelpDrawingView(HelpDrawingView helpDrawingView) {
		MainWindow.helpDrawingView = helpDrawingView;
	}

	
	public static HelpRecognizeView getHelpRecognizeView() {
		return helpRecognizeView;
	}

	public static void setHelpRecognizeView(HelpRecognizeView helpRecognizeView) {
		MainWindow.helpRecognizeView = helpRecognizeView;
	}
	
	public static EditView getEv() 
	{
		return ev;
	}
	
	public static void setEv(EditView e)
	{
		MainWindow.ev = e ;
	}
	
	public static DrawingView getDv() {
		return dv;
	}

	public static void setDv(DrawingView dv) {
		MainWindow.dv = dv;
	}

	public static VerticalScale getVs() {
		return vs;
	}

	public static void setVs(VerticalScale vs) {
		MainWindow.vs = vs;
	}

	public static HorizontalScale getHs() {
		return hs;
	}

	public static void setHs(HorizontalScale hs) {
		MainWindow.hs = hs;
	}

	
	public static JComponent getHorizontalScale() {
		return horizontalScale;
	}

	public static void setHorizontalScale(JComponent horizontalScale) {
		MainWindow.horizontalScale = horizontalScale;
	}

	public static JComponent getVerticalScale() {
		return verticalScale;
	}

	public static void setVerticalScale(JComponent verticalScale) {
		MainWindow.verticalScale = verticalScale;
	}
	
	public static DrawingView getDrawingView()
	{
		return (DrawingView)((JScrollPane)drawingTab).getViewport().getView();
		
	}

	public static HelpView getHelpView()
	{
		//return (HelpView)((JScrollPane)helpTab).getViewport().getView();
		return (HelpView)helpTab;
	}

	public static RecognizedView getRecognizedView()
	{
		return (RecognizedView)((JScrollPane)recognizedTab).getViewport().getView();
	}

	
	private static JSplitPane createSplitPane()
	{
		//Help tab is used by drawing view. Initialize it first
		//helpTab = new JScrollPane(new HelpView());
			
		//helpTab = new HelpView();
		
		recognizedTab = new JScrollPane(new RecognizedView());
		JTabbedPane tabbedPane = new JTabbedPane() ;

		
		// 25-09-09
		helpDrawingView = new HelpDrawingView();
		helpSubTab = helpDrawingView.DrawingWindow();
		
		helpRecognizeView = new HelpRecognizeView();
		helpRecognizeTab = helpRecognizeView.constraintWindow();
		
		dv = new DrawingView();
		drawingTab = new JScrollPane(dv);
		drawingTab.setFocusable(true);
		ev = new EditView();
		EditTab = new JScrollPane (ev);
	//	tabbedPane.add("EDIT",EditTab) ;
	//	tabbedPane.add("CONSTRAINTS", recognizedTab) ;
		// add Scrollbar adjustment listener to Drawing Window - horizontal and vertical
		
		final JScrollBar drawHrBar = ((JScrollPane)drawingTab).getHorizontalScrollBar();
		drawHrBar.setMaximum(100);
		AdjustmentListener hListener = new AdjustmentListener() {
		      public void adjustmentValueChanged(AdjustmentEvent e) {
		        hs.setXMoved(((Adjustable)e.getSource()).getValue());
		      }
		    };
		
		drawHrBar.addAdjustmentListener(hListener);  
		
		final JScrollBar drawVerBar = ((JScrollPane)drawingTab).getVerticalScrollBar();
		AdjustmentListener vListener = new AdjustmentListener() {
		      public void adjustmentValueChanged(AdjustmentEvent e) {
		        vs.setYMoved(((Adjustable)e.getSource()).getValue());
		      }
		    };
		
		 drawVerBar.addAdjustmentListener(vListener);   
		    
		 
		
		vs = new VerticalScale(); 
		verticalScale = new JScrollPane(vs);
		verticalScale.setFocusable(true);
		
		tc = new TempClass();              // just a filler above Vertical Scale
		tempclass = new JScrollPane(tc);
		tempclass.setFocusable(true);
		
		hs = new HorizontalScale(); // horizontal divider
		horizontalScale = new JScrollPane(hs);
		horizontalScale.setFocusable(true);
		
		
		m_LeftSplitPane = createSplitPane(JSplitPane.VERTICAL_SPLIT, tempclass ,verticalScale,TOP_HORIZONTAL_DIVIDER);
	
		m_hzScaleDrawView = createSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalScale , drawingTab, TOP_HORIZONTAL_DIVIDER);
	
		m_vtScaleDraw = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_LeftSplitPane , m_hzScaleDrawView, LEFT_VERTICAL_DIVIDER);
					
		//m_upperPane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_vtScaleDraw,helpSubTab, RIGHT_VERTICAL_DIVIDER);

		m_LowerPane = createSplitPane(JSplitPane.VERTICAL_SPLIT, m_vtScaleDraw , EditTab , EDIT_DIVIDER);	
		m_LowerlowerPane = createSplitPane(JSplitPane.VERTICAL_SPLIT,m_LowerPane,recognizedTab , BOTTOM_DIVIDER) ;
		
		m_splitpane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_LowerlowerPane, helpSubTab,RIGHT_VERTICAL_DIVIDER);
		//m_splitpane.setResizeWeight(0.7);
		
		//tabbedPane.add(m_LowerPane);
		
		
		return m_splitpane;
	}
	
	
	public static Container getApplicationContentPane()
	{
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);

		contentPane.add(createSplitPane());
		
		m_statusBar = StatusBar.getInstance();
        contentPane.add(m_statusBar, BorderLayout.SOUTH);
        
        m_toolBar = ToolBar.getInstance();
        contentPane.add(m_toolBar, BorderLayout.NORTH);
        
		return contentPane;
	}
	
	public static JMenuBar getApplicationMenuBar()
	{
		return MenuBar.getInstance();
	}

	public static String setAppName(String title)
	{
		if((title == null)||(title.equals(""))) title = IOManager.DEF_FILENAME;
		return (GConstants.APP_NAME+" - "+title);
	}
	
	public void actionPerformed(ActionEvent e)
	{
	}

	public static StatusBar getM_statusBar()
	{
		return m_statusBar;
	}

	public static void setM_statusBar(StatusBar bar)
	{
		m_statusBar = bar;
	}

	public static ToolBar getM_toolBar()
	{
		return m_toolBar;
	}

	public static void setM_toolBar(ToolBar bar)
	{
		m_toolBar = bar;
	}

	public static MainWindow getApplication()
	{
		return application;
	}

	public static Cursor getM_defCursor()
	{
		return m_defCursor;
	}

	public static void startApplication()
	{
		// Initilize the status bar
		m_statusBar.setCoordLabelText(" Coordinates ");
	}


	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		// TODO Auto-generated method stub
		hs.repaint();
		vs.repaint();
	}
	
	public static JSplitPane createSplitPane(int splitOrientation, Component comp1, Component comp2, int divider){
		JSplitPane js = new JSplitPane(splitOrientation, comp1 , comp2);
		js.setOneTouchExpandable(true);
		js.setDividerLocation(divider);
		return js;
	}
	
}


