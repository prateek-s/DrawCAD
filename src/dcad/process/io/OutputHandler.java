package dcad.process.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.JOptionPane;
import dcad.ui.drawing.DrawingView;
import dcad.util.GMethods;

public class OutputHandler
{
	FileWriter m_fileW = null;
	
	public OutputHandler()
	{
	}
	
	private int writeToFile(FileWriter fw, String str) throws IOException
	{
		fw.write(str);
		return 0;		
	}
	
	public int write(String str) throws IOException
	{
		if(m_fileW != null)
		{
			return writeToFile(m_fileW, str);
		}
		return -1;
	}

	public FileWriter getM_fileW()
	{
		return m_fileW;
	}

	public void setM_fileW(FileWriter fw)
	{
		m_fileW = fw;
	}

/*	public void saveToUnknownFile(String dirName, String fName)
	{
		///System.out.println("OutputHandler.saveToUnknownFile()");
		saveToKnownFile(dirName, fName);
		IOManager.setFilename(fName);
		IOManager.setLast_dir(dirName);
	}
*/	
	public void saveToUnKnownFile(String dirName, String fName) throws IOException 
	{
		/////System.out.println("OutputHandler.saveToKnownFile()");
		File f = new File(dirName, fName);
		int option = JOptionPane.OK_OPTION;
		if (f.exists())
		{
			option = JOptionPane.showConfirmDialog(null, "The file \""+fName+"\" already exists!\n Do u want to overwrite!");
		}
		if(option == JOptionPane.OK_OPTION)
		{
			saveToFile(dirName, fName);
			IOManager.setFilename(fName);
			IOManager.setLast_dir(dirName);
		}
	}
	
	public void saveToFile(String dir, String file) throws IOException
	{
	//	///System.out.println("OutputHandler.saveToFile()");
		DrawingView dv = GMethods.getCurrentView();;
		CommandQueue cq = dv.getM_drawData().getM_commands();
		m_fileW = new FileWriter(dir+file);
		
		Iterator iter = cq.getM_commands().iterator();
		while (iter.hasNext())
		{
			Command comm = (Command) iter.next();
			writeToFile(m_fileW, comm.toString()+"\n");
		}
		m_fileW.close();
	}
}
