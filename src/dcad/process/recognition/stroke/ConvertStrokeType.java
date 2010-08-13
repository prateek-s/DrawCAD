package dcad.process.recognition.stroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.peer.DesktopPeer;


import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer; 

import dcad.util.GMethods;
import dcad.ui.drawing.DrawingView;
import dcad.model.constraint.Constraint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.beautification.ConstraintSolver;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.segment.SegmentRecognitionScheme;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.process.recognition.stroke.StrokeRecognizer;
import dcad.ui.drawing.*;
import dcad.ui.main.MainWindow;



/**
 * Class to convert normal stroke to marker and vice-versa
 * @author Sunil 
 */
public class ConvertStrokeType {
	private final int NORMAL_STROKE = 0;
	private final int MARKER = 1;
	private DrawingView dv = null;
	
	public ConvertStrokeType() {
		// TODO Auto-generated constructor stub
		if(dv == null){
			dv = MainWindow.getDv();
		}
	}
	
	public void ConvertLastDrawnStroke(){
		Vector stkList = dv.getM_drawData().getStrokeList();
		Stroke stk = null;
		if(stkList != null){
			
			// get the last stroke
			stk = (Stroke)stkList.get(stkList.size()-1);
			//stk = dv.getM_drawData().getLastStroke(true);
			// create a new stroke
			//dv.getCurrentStroke();
			
			int x = 0;
			int y = 0;
			long time = 0; 
			int count = 0;
			int buttonType = 1;
			// get the list of raw points
			Vector rawptList = stk.getM_ptList();
			
			// find the stroke type of current stroke
		
			int strokeType = stk.getM_type();
			
			//delete the earlier stroke
			dv.setStrokeConverted(true);
			boolean converted = true;
			GMethods.getCurrentView().logEvent("setStrokeConverted({boolean}" + converted + ");");
			if(strokeType == MARKER){
				// change to normal stroke
				Vector markerList = dv.getM_drawData().getM_markers();
				Marker marker = (Marker)markerList.get(markerList.size()-1);
				
				// add to transcript
				GMethods.getCurrentView().logEvent("DrawingData|removeMarker({Marker}" + marker + ");");
				//GMethods.getCurrentView().logEvent(Command.PAUSE);
				
				dv.getM_drawData().removeMarker(marker);
				dv.setStrokeConvertedTo(NORMAL_STROKE);
				GMethods.getCurrentView().logEvent("setStrokeConvertedTo({int}" + NORMAL_STROKE + ");");
			}
			else if(strokeType == NORMAL_STROKE){
				dv.setStrokeConvertedTo(MARKER);
				GMethods.getCurrentView().logEvent("setStrokeConvertedTo({int}" + MARKER + ");");
				// set bit to marker
			}
			
			// add to transcript
			GMethods.getCurrentView().logEvent("Stroke|delete();");
			//GMethods.getCurrentView().logEvent(Command.PAUSE);
			
			stk.delete();
			
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
}
