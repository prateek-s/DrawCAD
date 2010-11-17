package dcad.process.recognition.marker;

import ij.Prefs;

import java.awt.geom.Point2D;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.constraint.connect.IntersectionConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;
import dcad.model.marker.MarkerAngle;
import dcad.model.marker.MarkerEquality;
import dcad.model.marker.MarkerParallel;
import dcad.model.marker.MarkerPerpendicular;
import dcad.process.ProcessManager;
import dcad.process.preprocess.PreProcessingManager;
import dcad.process.preprocess.PreProcessor;
import dcad.process.preprocess.Segmentor;
import dcad.process.recognition.RecognitionManager;
import dcad.process.recognition.segment.SegmentRecognizer;
import dcad.util.GConstants;

public class MarkerRecognizer
{
	private static MarkerRecognizer m_markerRecog = null;
	private Marker m_marker = null; 
	
	public static MarkerRecognizer getInstance()
	{
		if(m_markerRecog == null) m_markerRecog = new MarkerRecognizer();
		return m_markerRecog;
	}
	/**
	 * Check if the stroke is actually a marker
	 * @param stroke
	 * @return
	 */
	public int checkForMarker(Stroke stroke)
	{
		m_marker = null;
		int markerType = Marker.TYPE_NONE; 
		// size is small now check which type of marker is it
		/**
		 * The marker recognition depends on the segmentation previously performed.
		 * For instance, the number of segments determines what marker it could be.
		 * 
		 */
		
		Vector Segments = stroke.getM_segList(); 
		
		if(dcad.Prefs.getSegScheme() == GConstants.SEG_SCHEME_SIMPLE) 
		{
		//	Segmentor m_segmentor = new Segmentor() ;
		//	Vector segpts = m_segmentor.performSegmentation(stroke,GConstants.SEG_SCHEME_ALL);
			PreProcessingManager preProcessMan = ProcessManager.getInstance().getPreProManager();
			PreProcessor preProcessor = preProcessMan.getPreProcessor();
			Vector segpts = preProcessor.temp_preProcess(stroke);
			
			RecognitionManager recogMan = ProcessManager.getInstance().getRecogManager();
			SegmentRecognizer segmentRecog = recogMan.getSegmentRecogMan().getSegmentRecognizer();
			try {
				Segments = stroke.temp_recognizeSegments(segmentRecog , segpts);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		int segCount = Segments.size();
		
		switch (segCount)
		{
			case 1: // This could be an Angle or Equal Length Marker
				// get the first segment
				Segment seg = (Segment)stroke.getM_segList().get(0);
				Vector constraints = seg.getConstraintByType(IntersectionConstraint.class);
				if(constraints != null)
				{
					if(constraints.size() == 1)
					{
						IntersectionConstraint iCons = (IntersectionConstraint)constraints.get(0);
						Segment consSeg = iCons.getM_seg1().equals(seg)? iCons.getM_seg2():iCons.getM_seg1();
						// it can only be a equality marker, check is the other segment is a line
						Point2D contactPt = iCons.getM_seg1().equals(seg)? iCons.getM_contactPt2():iCons.getM_contactPt1();
						if(consSeg instanceof SegLine)
						{
							SegLine segLine = (SegLine)consSeg;
							if((segLine.getM_start().distance(contactPt) >= Constraint.MAX_ALLOWED_CONNECT_GAP)&&(segLine.getM_end().distance(contactPt) >= Constraint.MAX_ALLOWED_CONNECT_GAP))
							{
								m_marker = new MarkerEquality(stroke, consSeg);	
								markerType = m_marker.getM_type();
							}
						}
						else if(consSeg instanceof SegCircleCurve)
						{
							SegCircleCurve c = (SegCircleCurve)consSeg;
							if((c.getM_start().distance(contactPt) >= Constraint.MAX_ALLOWED_CONNECT_GAP)&&(c.getM_end().distance(contactPt) >= Constraint.MAX_ALLOWED_CONNECT_GAP))
							{
								m_marker = new MarkerEquality(stroke, consSeg);	
								markerType = m_marker.getM_type();
							}
						}
					}
					else if(constraints.size() == 2)
					{
						// it can only be an angle constraint, but check if the segments are also intersecting and are line segments 
						IntersectionConstraint iCons1 = (IntersectionConstraint)constraints.get(0);
						Segment consSeg1 = iCons1.getM_seg1().equals(seg)? iCons1.getM_seg2():iCons1.getM_seg1();
						
						IntersectionConstraint iCons2 = (IntersectionConstraint)constraints.get(1);
						Segment consSeg2 = iCons2.getM_seg1().equals(seg)? iCons2.getM_seg2():iCons2.getM_seg1();
						if((consSeg1 instanceof SegLine) && (consSeg2 instanceof SegLine))
						{
							// check if there is a connection between the two segments
							Vector uniquePoints=constraintsHelper.getUniquePointsForConnectedLines((SegLine)consSeg1,(SegLine)consSeg2);
							if(uniquePoints.size()>0)
							{
								if(seg.getDistance( ((AnchorPoint)uniquePoints.elementAt(2)).getM_point()) <= Marker.MARKER_SIZE)
								{
									m_marker = new MarkerAngle(stroke, consSeg1, consSeg2, null);	
									markerType = m_marker.getM_type();
								}
							}
						}
					}
				}
				break;
				
			case 2: // This could be a Parallel Arrow or Right angle Marker
				// get the segments of the marker
				Segment markerSegment1 = (Segment)Segments.get(0);
				Segment markerSegment2 = (Segment)Segments.get(1);
				if(markerSegment1 instanceof SegLine && markerSegment2 instanceof SegLine)
				{
					Vector cons1 = (markerSegment1).getConstraintByType(IntersectionConstraint.class);
					Vector cons2 = (markerSegment2).getConstraintByType(IntersectionConstraint.class);
					if(cons1.size()==1 && cons2.size()==1)
					{
						IntersectionConstraint ic1=(IntersectionConstraint)cons1.get(0);
						IntersectionConstraint ic2=(IntersectionConstraint)cons2.get(0);
						
						Segment segment1,segment2;
						if(ic1.getM_seg1()==markerSegment1)
							segment1=ic1.getM_seg2();
						else 
							segment1=ic1.getM_seg1();
						if(ic2.getM_seg1()==markerSegment2)
							segment2=ic2.getM_seg2();
						else 
							segment2=ic2.getM_seg1();

						//Parallel marker - It may be for a line or a curve
						if(segment1==segment2)
						{
							m_marker = new MarkerParallel(stroke, segment1);	
							markerType = m_marker.getM_type();
						}
						else if(segment1 instanceof SegLine && segment2 instanceof SegLine)
						{
							// check if there is a connection between the two segments
							Vector uniquePoints=constraintsHelper.getUniquePointsForConnectedLines((SegLine)segment1,(SegLine)segment2);
							if(uniquePoints.size()>0)
							{
								if(markerSegment1.getM_length() <= Marker.MARKER_SIZE && markerSegment2.getM_length()<=Marker.MARKER_SIZE)
								{
									m_marker = new MarkerPerpendicular(stroke, (SegLine)segment1,(SegLine)segment2);	
									markerType = m_marker.getM_type();
								}
							}
						}
						
						
					}
				}
				break;

			default:
				markerType = Marker.TYPE_NONE;
				break;
		}
		return markerType;
	}

	public Marker getM_marker()
	{
		return m_marker;
	}
	
}