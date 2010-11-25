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
	
	public int findType(Stroke stroke, int user_given)
	{
		reset();
		RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
		m_markerRecog = recogMan.getMarkerRecognitionMan().getMarkerRecognizer();
		m_stroke = stroke;

		if(stroke.isStrokeConverted()) {
			if(stroke.getStrokeConvertedTo() == NORMAL_STROKE) {
				return Stroke.TYPE_NORMAL;
			}
			else{
				int type = m_markerRecog.checkForMarker(stroke);
				if(type == Marker.TYPE_NONE) { 
					 JOptionPane.showMessageDialog(MainWindow.getDv(),"Given stroke does not match any marker");
					return Stroke.TYPE_NORMAL;
				}
				else {
					///System.out.println("Converted to marker");
					return Stroke.TYPE_MARKER;
				}
			}
		}
		else{
			if(stroke.isSmallSize() && (stroke.getLength() >= SegmentRecognitionScheme.PT_WINDOW))
			{
				int type ;
				if( dcad.Prefs.getSegScheme() == GConstants.SEG_SCHEME_SIMPLE  )
						type = m_markerRecog.simple_checkForMarker(stroke);
				else type = m_markerRecog.checkForMarker(stroke) ;
				
				if(type == Marker.TYPE_NONE){ 
					return Stroke.TYPE_NORMAL;
				}
				else{ 
					return Stroke.TYPE_MARKER;
				}
			}
		return Stroke.TYPE_NORMAL;
		}
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
