package dcad.process.io;

public class CommandParser
{
	
	public void reset()
	{
	}
	
	public Command Parse(String str)
	{
		Command comm = new Command(str);
		return comm;
	}
}
