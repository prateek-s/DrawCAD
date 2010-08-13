package dcad.process.io;

import java.util.Vector;

public class CommandQueue
{
	private int m_currentCommIndex = -1;
	private Vector m_commands = new Vector();

	public int getM_currentCommIndex()
	{
		return m_currentCommIndex;
	}

	public void setM_currentCommIndex(int commIndex)
	{
		m_currentCommIndex = commIndex;
	}
	
	public void execUpto(int index)
	{
		execFromTo(0, index);
	}

	public void execAll()
	{
		execFromTo(0, m_commands.size()-1);
	}

	public int execWithPause()
	{
		for (int i=m_currentCommIndex+1; i<m_commands.size(); i++)
		{
			Command comm = (Command)m_commands.elementAt(i);
			System.out.println(comm);
			// we want to move one step even if its a Pause statement	
			m_currentCommIndex = i; 
			if(!comm.execute())
			{
				break;
			}
		}
		return m_currentCommIndex;
	}
	
	public void execFromTo(int start ,int end)
	{
		end = (end<m_commands.size())? end:m_commands.size()-1;
		for (int i=start; i<=end; i++)
		{
			Command comm = (Command)m_commands.elementAt(i);
/*ISHWAR This was printing command
			if (comm.getM_command().indexOf("mouseReleased")!=-1)
				System.out.println("mouse released" + comm.getM_command());
			else if(comm.getM_command().indexOf("mouseDragged")!=-1)
				System.out.println("mouse dragged" + comm.getM_command());*/
			comm.execute();
		}
		m_currentCommIndex = end; 
	}

	public int prevPauseIndex()
	{
		return m_commands.lastIndexOf(new Command(Command.PAUSE), m_currentCommIndex-1);
	}

	public Vector getM_commands()
	{
		return m_commands;
	}

	public void setM_commands(Vector m_commands)
	{
		this.m_commands = m_commands;
	}
	
	public void add(Command comm)
	{
		m_commands.add(comm);
		m_currentCommIndex ++;
	}
	
	public void clear()
	{
		m_commands.clear();
		m_currentCommIndex = -1;
	}
}
