package dcad.process.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class InputHandler
{
	CommandQueue m_commandQ = null;
	CommandParser m_commParser = null; 
	
	public InputHandler()
	{
		m_commandQ = new CommandQueue();
		m_commParser = new CommandParser();
	}
	
	/**
	 * the method loads the content of a 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public CommandQueue loadFile(String dir, String filename) throws IOException
	{
//		System.out.println("InputHandler.loadFile()");
		if(filename == null) return null;
		m_commandQ.clear();
		m_commParser.reset();
		
		String line = "";
		InputStream ins = new FileInputStream(dir+filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		
		// read one line at a time;
		int line_num = -1;
		boolean isPause = false;
		while ((line = in.readLine()) != null)
		{
			line_num++;
			// trim to remove the trailing and leading white spaces.
			line = line.trim();
//			System.out.println(line);
			if(!line.equals(Command.PAUSE))
			{
				//System.out.println("NOT PAUSE");
				isPause = false;
				// this is a normal command
				Command comm = m_commParser.Parse(line);
				if(comm == null) throw new IOException("Parsing Error @line number: "+line_num);
				m_commandQ.add(comm);
			}
			else if(!isPause)
			{
//				System.out.println("PAUSE");
				// its a blank line .. so just insert a blank line, only one blank line is to be inserted for all the blank lines. 
				isPause = true;
				// this is a normal command
				Command comm = m_commParser.Parse(line);
				if(comm == null) throw new IOException("Parsing Error @line number: "+line_num);
				m_commandQ.add(comm);
			}
		}
		return m_commandQ;
	}

	public CommandQueue getM_commandQ()
	{
		return m_commandQ;
	}

	public void setM_commandQ(CommandQueue m_commandq)
	{
		m_commandQ = m_commandq;
	}
}
