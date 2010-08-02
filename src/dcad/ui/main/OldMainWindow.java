 package dcad.ui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import dcad.process.io.IOManager;
import dcad.ui.drawing.DrawingView;
import dcad.ui.recognize.RecognizedView;
import dcad.util.GConstants;

public class OldMainWindow implements ActionListener 
{
	private static StatusBar m_statusBar;
	private static ToolBar m_toolBar;
	private static OldMainWindow application = null;
	private static JSplitPane m_splitpane = null;
	private static Cursor m_defCursor = null;
	private static Logger m_logger = Logger.getLogger("global");
	
	private static JComponent drawingTab;
	private static JComponent recognizedTab;
	
	public static DrawingView getDrawingView()
	{
		return (DrawingView)((JScrollPane)drawingTab).getViewport().getView();
		
	}

	public static RecognizedView getRecognizedView()
	{
		return (RecognizedView)((JScrollPane)recognizedTab).getViewport().getView();
	}

	private static JSplitPane createSplitPane()
	{
		DrawingView dv = new DrawingView();
		drawingTab = new JScrollPane(dv);
		drawingTab.setFocusable(true);
		recognizedTab = new JScrollPane(new RecognizedView());

		//m_splitpane.setFocusable(true);
		m_splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawingTab, recognizedTab);
		m_splitpane.setOneTouchExpandable(true);
		m_splitpane.setResizeWeight(0.7);
		
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

	public static OldMainWindow getApplication()
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
	
}


/*
private JScrollPane createCommandPanel()
{
	JScrollPane jp = new JScrollPane(new CommandView(m_tabbedPane.getDrawingView().getM_drawData().getM_commands()));
	return jp;
}

	private Cursor createCustomCursor()
	{
        
		//Get the default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        //Load an image for the cursor
        Image cursorImage = GMethods.getImageIcon("pencil.gif","pencil").getImage();// new ImageIcon(GConstants.IMAGE_SRC+"pencil.gif", "pencil").getImage();
        //Create the hotspot for the cursor
        Point cursorHotSpot = new Point(0,0);

        //Create the custom cursor
        Cursor customCursor = toolkit.createCustomCursor(cursorImage, cursorHotSpot, "Pencil");

        return customCursor;
	}


*/