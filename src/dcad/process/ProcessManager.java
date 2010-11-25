package dcad.process;

import dcad.process.beautification.ConstraintSolver;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.recognition.RecognitionManager;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * This class is the main class for processing the raw data obtained from user or through
 * a lesson file. This class gives pointers to other manager for various stanges of processing.
 * This is a singleton class, but the other managers it calls/uses many not be singleton. 
 * @author vishalk
 *
 */
public class ProcessManager
{
	private PreProcessingManager m_preProManager;
	private RecognitionManager m_recogManager;
	private ConstraintSolver m_constraintSolver;
	private static ProcessManager processMan;

	public static ProcessManager getInstance()
	{
		if (processMan == null) processMan = new ProcessManager();
		return processMan;
	}

	private ProcessManager()
	{
		init();
	}
	
	private void init()
	{
		m_preProManager = PreProcessingManager.getInstance();
		m_recogManager = RecognitionManager.getInstance();
//		m_constraintSolver = ConstraintSolver.getInstance();
	}

	public PreProcessingManager getPreProManager()
	{
		return m_preProManager;
	}

	public RecognitionManager getRecogManager()
	{
		return m_recogManager;
	}

	public ConstraintSolver getM_constraintSolver()
	{
		return m_constraintSolver;
	}

	//returns all fields of the given object in a string
	 public static String dump(Object o, int callCount, ArrayList excludeList)
	 {
	  //add this object to the exclude list to avoid circual references in the future
	  if (excludeList == null) excludeList = new ArrayList();
	  excludeList.add(o);

	  callCount++;
	  StringBuffer tabs = new StringBuffer();
	  for (int k = 0; k < callCount; k++)
	  {
	   tabs.append("\t");
	  }
	  StringBuffer buffer = new StringBuffer();
	  Class oClass = o.getClass();
	  if (oClass.isArray()) {   
	   buffer.append("\n");
	   buffer.append(tabs.toString());
	   buffer.append("[");
	   for (int i = 0; i < Array.getLength(o); i++)
	   {
	    if (i < 0) buffer.append(",");
	    Object value = Array.get(o, i);

	    if (value != null)
	    {
	     if (excludeList.contains(value))
	     {
	      buffer.append("circular reference");
	     }
	     else if (value.getClass().isPrimitive() || value.getClass() == java.lang.Long.class || value.getClass() == java.lang.String.class || value.getClass() == java.lang.Integer.class || value.getClass() == java.lang.Boolean.class)
	     {
	      buffer.append(value);
	     }
	     else
	     {
	    //  buffer.append(dump(value, callCount, excludeList));
	     }
	    }
	   }
	   buffer.append(tabs.toString());
	   buffer.append("]\n");
	  }
	  else
	  {   
	   buffer.append("\n");
	   buffer.append(tabs.toString());
	   buffer.append("{\n");
	   while (oClass != null)
	   {    
	    Field[] fields = oClass.getDeclaredFields();
	    for (int i = 0; i < fields.length; i++)
	    {
	     if (fields[i] == null) continue;

	     buffer.append(tabs.toString());
	     fields[i].setAccessible(true);
	     buffer.append(fields[i].getName());
	     buffer.append("=");
	     try
	     {
	      Object value = fields[i].get(o);
	      if (value != null)
	      {
	       if (excludeList.contains(value))
	       {
	        buffer.append("circular reference");
	       }
	       else if ((value.getClass().isPrimitive()) || (value.getClass() == java.lang.Long.class) || (value.getClass() == java.lang.String.class) || (value.getClass() == java.lang.Integer.class) || (value.getClass() == java.lang.Boolean.class))
	       {
	        buffer.append(value);
	       }
	       else
	       {
	        buffer.append(dump(value, callCount, excludeList));
	       }
	      }
	     }
	     catch (IllegalAccessException e)
	     {
	      System.out.println("IllegalAccessException: " + e.getMessage());
	     }
	     buffer.append("\n");
	    }
	    oClass = oClass.getSuperclass();
	   }
	   buffer.append(tabs.toString());
	   buffer.append("}\n");
	  }
	  return buffer.toString();
	 }
}
