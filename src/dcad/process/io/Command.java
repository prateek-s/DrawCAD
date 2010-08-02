package dcad.process.io;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import dcad.model.marker.Marker;
import dcad.util.GMethods;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.*;

public class Command
{
	public static final String PAUSE = "";
	private String m_command = PAUSE;
	private Class m_methodParams[] = null;
	private Object m_params[] = null;
	private Object m_class = null;
	private String m_methodName = PAUSE;
	private Method m_method = null;
	private Constructor m_constructor = null;
	DrawingView dv = MainWindow.getDv();
	
	public Command(String comm)
	{
		m_command = comm;
	}

	public String getM_command()
	{
		return m_command;
	}

	public void setM_command(String m_command)
	{
		this.m_command = m_command;
	}

	public String toString()
	{
		return getM_command();
	}
	
	/*public boolean execute()
	{
		// check out if this is a Pause statement
		if(m_command.equals(PAUSE)) return false;
		
		String str = m_command;
		int indexOfPipe = str.indexOf("|");
		String temp = "" ;
		if(indexOfPipe!=-1)
			temp = str.substring(0,str.indexOf("|"));
		
		if(temp=="")
			m_class = GMethods.getCurrentView();
		else if(temp.equals("RecognizedView"))
		{
			m_class = GMethods.getRecognizedView();
			str = str.substring(indexOfPipe+1);
		}
		
		m_methodName = str.substring(0, str.indexOf("("));
		
		//added on 11-05-10
		if(m_methodName.equalsIgnoreCase("setParamsLine")){
			dv.getLineWindow().performSubmitActionLineParam();
			//m_class = LineParameterWindow.class;
			//m_methodName = "performSubmitActionLineParam";
			
		}
		else if(m_methodName.equalsIgnoreCase("closeParamsLine")){
			//m_class = LineParameterWindow.class;
			//m_methodName = "performCancelActionLineParam";
			dv.getLineWindow().performCancelActionLineParam();
		}
		else if(m_methodName.equalsIgnoreCase("setParamsCircularArc")){
			dv.getCircArcWindow().performSubmitActionCirArcParam();
		}
		else if(m_methodName.equalsIgnoreCase("closeParamsCircularArc")){
			dv.getCircArcWindow().performCancelActionCirArcParam();
		}
		else{
		//System.out.println("*******************"+m_methodName);
		str = str.substring(str.indexOf("(")+1, str.lastIndexOf(")"));
		//System.out.println("*******************"+str);

		m_methodParams = null;
		m_params = null;
		if(str.length() > 0)
		{
			String strArr[] = str.split(",");
			m_methodParams = new Class[strArr.length];
			m_params = new Object[strArr.length];
			//System.out.println("+++++++"+strArr.length);
			for (int i = 0; i < strArr.length; i++)
			{
				//System.out.println("**** "+strArr[i]);
				String type = strArr[i].substring(strArr[i].indexOf("{")+1, strArr[i].lastIndexOf("}")).trim();
				String value = strArr[i].substring(strArr[i].lastIndexOf("}")+1).trim();
				//System.out.println("**** "+strArr[i]+"**** "+type+"**** "+value);
				try
				{
					if(type.equals("int"))
					{
						m_methodParams[i] = int.class;
						m_params[i] = new Integer(value);
					}
					else if(type.equals("long"))
					{
						m_methodParams[i] = long.class;
						m_params[i] = new Long(value);
					}
					else if(type.equals("char"))
					{
						m_methodParams[i] = char.class;
						m_params[i] = new Character(value.charAt(0));
					}
					else
					{
						m_methodParams[i] = Class.forName(type);
						m_params[i] = value;
					}
 
					//System.out.println("---"+m_methodParams[i].getName());
				} 
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		
		}
		try
		{
//			for(int i=0; i<m_class.getClass().getMethods().length; i++)
//			{
//				System.out.println("METHODS"+m_class.getClass().getMethods()[i]);
//			}
			m_method = m_class.getClass().getMethod(m_methodName, m_methodParams);
//			System.out.println("****************** "+m_method);
			m_method.invoke(m_class, m_params);
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		}
		return true;
	}*/
	
	public boolean execute()
	{
		// check out if this is a Pause statement
		if(m_command.equals(PAUSE)) return false;
		
		String str = m_command;
		int indexOfPipe = str.indexOf("|");
		String temp = "" ;
		if(indexOfPipe!=-1)
			temp = str.substring(0,str.indexOf("|"));
		
		if(temp=="")
			m_class = GMethods.getCurrentView();
		else if(temp.equals("RecognizedView")){
			m_class = GMethods.getRecognizedView();
			str = str.substring(indexOfPipe+1);
		}
		// added on 30-05-10
		else if(temp.equals("DrawingData")){
			m_class = GMethods.getDrawingData();
			str = str.substring(indexOfPipe+1);
		}
		else if(temp.equals("Stroke")){
			m_class = GMethods.getLastStroke();
			str = str.substring(indexOfPipe+1);
		}
		
		//RecognizedView|deleteConstraint({int}0);
		m_methodName = str.substring(0, str.indexOf("("));
		//System.out.println("*******************"+m_methodName);
		str = str.substring(str.indexOf("(")+1, str.lastIndexOf(")"));
		//System.out.println("*******************"+str);

		m_methodParams = null;
		m_params = null;
		if(str.length() > 0)
		{
			String strArr[] = str.split(",");
			m_methodParams = new Class[strArr.length];
			m_params = new Object[strArr.length];
			//System.out.println("+++++++"+strArr.length);
			for (int i = 0; i < strArr.length; i++)
			{
				//System.out.println("**** "+strArr[i]);
				String type = strArr[i].substring(strArr[i].indexOf("{")+1, strArr[i].lastIndexOf("}")).trim();
				String value = strArr[i].substring(strArr[i].lastIndexOf("}")+1).trim();
				//System.out.println("**** "+strArr[i]+"**** "+type+"**** "+value);
				try
				{
					if(type.equals("int"))
					{
						m_methodParams[i] = int.class;
						m_params[i] = new Integer(value);
					}
					else if(type.equals("long"))
					{
						m_methodParams[i] = long.class;
						m_params[i] = new Long(value);
					}
					else if(type.equals("char"))
					{
						m_methodParams[i] = char.class;
						m_params[i] = new Character(value.charAt(0));
					}
					else if(type.equals("boolean")){
						m_methodParams[i] = boolean.class;
						m_params[i] = new Boolean(value);
					}
					else if(type.equals("Marker")){
						m_methodParams[i] = Marker.class;
						m_params[i] = value;
					}
					else
					{
						m_methodParams[i] = Class.forName(type);
						m_params[i] = value;
					}
 
					//System.out.println("---"+m_methodParams[i].getName());
				} 
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		try
		{
//			for(int i=0; i<m_class.getClass().getMethods().length; i++)
//			{
//				System.out.println("METHODS"+m_class.getClass().getMethods()[i]);
//			}
			m_method = m_class.getClass().getMethod(m_methodName, m_methodParams);
//			System.out.println("****************** "+m_method);
			m_method.invoke(m_class, m_params);
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Command)
		{
			Command comm = (Command)obj;
			return m_command.equals(comm.m_command);
		}
		return false;
	}
}
