package dcad.process.recognition.segment;

import java.util.Iterator;
import java.util.Vector;

import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GMethods;

/** Class to convert from one recognition type to another. Defunct, doesnt work .
 * @author sunil
 */
public class ConvertSegment {
	
	private DrawingView dv = null;
	private final int CIRCULAR_ARC = 1;
	private final int LINE = 0;
	private boolean converted = true;
	
	public ConvertSegment() {
		if(dv == null){
			dv = MainWindow.getDv();
		}
	}
	
	public void ConvertSelectedSegment(){
		// Get the selected geometric element
		GeometryElement ge = (GeometryElement)dv.getSelectedElements().get(0);
		
		Vector rawptList = null;
		int x = 0;
		int y = 0;
		long time = 0; 
		int count = 0;
		int buttonType = 1;
		
		// get the raw points in rawPtList Vector and setSegment Converted and To to respective values
		if(ge instanceof SegLine){
			rawptList = ((SegLine)ge).getRawPoints();
			dv.setSegmentConverted(true);
			dv.setSegmentConvertedTo(CIRCULAR_ARC);
			GMethods.getCurrentView().logEvent("setSegmentConverted({boolean}" + converted + ");");
			GMethods.getCurrentView().logEvent("setSegmentConvertedTo({int}" + CIRCULAR_ARC + ");");
		}
		else if(ge instanceof SegCircleCurve){
			rawptList = ((SegCircleCurve)ge).getRawPoints();
			dv.setSegmentConverted(true);
			dv.setSegmentConvertedTo(LINE);
			GMethods.getCurrentView().logEvent("setSegmentConverted({boolean}" + converted + ");");
			GMethods.getCurrentView().logEvent("setSegmentConvertedTo({int}" + LINE + ");");
		}
		
		// remove the selected element
		ge.delete();
		dv.logEvent("deleteKeyPressed()");
		
		Iterator iter = rawptList.iterator();
		while(iter.hasNext()){
			PixelInfo pi = (PixelInfo)iter.next();
			x = (int)pi.getX();
			y = (int)pi.getY();
			time = pi.getTime();
			// add them to current stroke
			//dv.addPointToStroke(x, y, time);
			
			// add to transcript
			if(count == 0){
				dv.mouseButton1Pressed(x, y, time);
				//dv.logEvent("mouseMoved({int}" + x + ", {int}" + y + ");");
				//dv.logEvent("mouseButton1Pressed({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			}
			else{
				dv.mouseDragged(x, y, time);
				//dv.logEvent("mouseDragged({int}" + x + ", {int}" + y + ", {long}" + time + ");");
			}
			count ++;
		}
		//dv.logEvent("mouseReleased({int}" + x + ", {int}" + y + ", {int}" + buttonType + ");");
	//	dv.getCurrentStroke().setM_ptList(rawptList);
		//dv.getM_drawData().removeUnusedMarker();
		dv.mouseReleased(x, y, buttonType);
		}
}
