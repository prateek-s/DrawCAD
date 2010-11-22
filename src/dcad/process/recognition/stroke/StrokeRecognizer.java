package dcad.process.recognition.stroke;

import javax.swing.JOptionPane;

import dcad.model.geometry.Stroke;
import dcad.model.marker.Marker;
import dcad.process.ProcessManager;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.marker.MarkerRecognizer;
import dcad.process.recognition.segment.SegmentRecognitionScheme;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;

public class StrokeRecognizer
{
	private Stroke m_stroke = null;
	private MarkerRecognizer m_markerRecog = null;
	private final int NORMAL_STROKE = 0;
	private final int MARKER = 1;
	public StrokeRecognizer()
	{
		reset();
	}
	
	public void reset()
	{
		m_stroke = null;
		m_markerRecog = null;
	}
	
	

	/**
	 * Check if the given stroke can be a marker. Also takes user input,
	 * for the case when stroke<->marker input is given.
	 * @param stroke
	 * @param user_specified : 0:neutral,-1:favour stroke, +1:favour marker. Also kind of marker.
	 * @return
	 */
	public int findType(Stroke stroke, int user_specified)
	{
		reset();
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		m_markerRecog = recogMan.getMarkerRecognitionMan().getMarkerRecognizer();
		m_stroke = stroke;

		int type ; 
	//	if(stroke.getStrokeConvertedTo() == NORMAL_STROKE){
		if(user_specified == -1) { //It's a stroke!
			return Stroke.TYPE_NORMAL;
		}

		if(stroke.isSmallSize() && user_specified > 0 &&
				(stroke.getLength() >= SegmentRecognitionScheme.PT_WINDOW))
		{
			type = user_specified ;
		//FILL HERE	
			return type ;
		}

		if(stroke.isSmallSize() && (stroke.getLength() >= SegmentRecognitionScheme.PT_WINDOW))
		{
			if( dcad.Prefs.getSegScheme() == GConstants.SEG_SCHEME_SIMPLE )
				type = m_markerRecog.simple_checkForMarker(stroke);
			else 
				type = m_markerRecog.checkForMarker(stroke) ;

			if(type == Marker.TYPE_NONE){ 
				return Stroke.TYPE_NORMAL;
			}
			else{ 
				return Stroke.TYPE_MARKER;
			}
		}
		return Stroke.TYPE_NORMAL;

		//return Stroke.TYPE_NORMAL;

	}
	
	
	public Stroke getM_stroke()
	{
		return m_stroke;
	}

	public Marker getMarker()
	{
		if(m_stroke != null)
		{
			return m_markerRecog.getM_marker();
		}
		return null;
	}
}
