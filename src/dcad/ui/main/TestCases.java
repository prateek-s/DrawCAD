/**
 * Class for running test cases one by one through GUI 
 * @author Sunil
 *
 */

package dcad.ui.main;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import dcad.ui.drawing.DrawingView;
import dcad.util.GMethods;
import dcad.ui.main.*;
/**Class to run Testcases one by one through GUI
 * @author Sunil Kumar
 */
class TestCases{
	ToolBar tb = null;
	private static int index;
	private int flag;
	//private WindowActions winAct = null;

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	File directory = null;
	File[] files = null;
	public TestCases() {
		//winAct = WindowActions.getInstance();
		String absPath = System.getProperty("user.dir");
		 directory = new File(absPath + "/testcases/");  
		 files = directory.listFiles(new Filter());  
		 Arrays.sort(files);
	}
	
	public void ExtractFile(int index){
			setFlag(0);
		    //Print out the name of files in the directory  
			 String file = files[index].toString().trim();
			 String arr[] = file.split("\\/");
			 file = arr[arr.length -1];
		    ///System.out.println(file);  
		    openFile(file);	
	}
	
	void openFile(String file){

		WindowActions wActions = null;
		wActions = WindowActions.getInstance();
		String absPath = System.getProperty("user.dir");
		DrawingView dv = GMethods.getCurrentView();
	//	if(!dv.isM_saved()) wActions.saveAsMIAction();
		if (file != null)
		{
			String directory = absPath + "/testcases/";
			if(directory != null)
			{
				wActions.openFile(directory, file);
			}
		}
		dv.setM_saved(true);
		dv.setM_newFile(false);
	}
	
	void SetIndex(int index){
		TestCases.index = index;
	}
	int GetIndex(){
		return index;
	}
	int getEleCount(){
		return files.length;
	}
}

class Filter implements FileFilter  
{  
   public boolean accept(File file)  
   {  
      if(file.getName().endsWith("~") || file.getName().endsWith(".exe")){
   	return false;   
      }
      else{
   	return true;   
      }
   }  
} 



