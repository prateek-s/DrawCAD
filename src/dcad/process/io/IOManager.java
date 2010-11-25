package dcad.process.io;

import dcad.ui.main.MainWindow;

public class IOManager
{
	public static final String DEF_FILENAME = "Untitled";
	private static IOManager m_ioManager;
	private InputHandler inputH;
	private OutputHandler outputH;
	private static String last_dir = System.getProperty("user.dir") + "/testcases/";
	private static String filename = DEF_FILENAME;
	

	public static IOManager getInstance()
	{
		if (m_ioManager == null) m_ioManager = new IOManager();
		return m_ioManager;
	}

	private IOManager()
	{
		init();
	}

	/**
	 * Method to initialize various member variables
	 *
	 */
	private void init()
	{
		outputH = new OutputHandler();
		inputH = new InputHandler();
	}

	public InputHandler getInputH()
	{
		return inputH;
	}

	public OutputHandler getOutputH()
	{
		return outputH;
	}

	public static String getLast_dir()
	{
		///System.out.println(last_dir);
		return last_dir;
	}

	public static void setLast_dir(String last_dir)
	{
		IOManager.last_dir = last_dir;
	}

	public static String getFilename()
	{
		return filename;
	}

	public static void setFilename(String filename)
	{
		IOManager.filename = filename;
		MainWindow.setAppName(filename);
	}
}
