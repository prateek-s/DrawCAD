package dcad.process.recognition.constraint;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.segment.SegCircleCurve;
import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.SegPoint;
import dcad.model.geometry.segment.Segment;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;
import dcad.util.GVariables;

/**
 * Given 2 segments/group, tries to recognizes all the constraints between them
 * @author vishalk
 *
 */
public class RelConstraintRecognizer
{
	private RelLengthRecognizer m_relLengthRecog = null;
	private ConnectionRecognizer m_connectRecog = null;
	private RelAngleRecognizer m_relAngleRecog = null;
	private RelSideRecognizer m_relSideRecog = null;
	private CoCentricityRecognizer m_coCentricityRecog = null;
	private CollinearLinesRecognizer m_coLlinearLinesRecog = null;
	private CollinearPointsRecognizer m_collinearityPtRecog = null;
	private pointOnSegmentRecognizer m_pointOnSegmentRecog = null;
	private tangencyRecognizer m_tangencyRecog = null;
	private Vector m_constraints = null;
	private Segment m_seg1 = null;
	private Segment m_seg2 = null;

	public RelConstraintRecognizer()
	{
		m_constraints = new Vector();
	}
	
	public void init(Segment seg1, Segment seg2)
	{
		m_constraints = new Vector();
		m_seg1=seg1;
		m_seg2=seg2;
	}
	
	public Vector recognizeConnectConstraints(Segment seg1, Segment seg2)
	{
		init(seg1, seg2);
		if((seg1!=null) && (seg2!=null))
		{
			Vector connectCons = recogConnectConstraints();
			if(connectCons != null) m_constraints.addAll(connectCons);
		}
		return m_constraints;
	}

	private Vector recogConnectConstraints()
	{
		if(m_connectRecog == null) m_connectRecog = new ConnectionRecognizer(m_seg1, m_seg2);
		else m_connectRecog.init(m_seg1, m_seg2);
		return m_connectRecog.recognize();
	}
	
	/**
	 * recognize constraints between the 2 segments.. 
	 * TODO There will be another method for recognizing constraints between two groups (groups will be formed from related segments) 
	 * @param seg1 the first segment
	 * @param seg2 the second segment
	 * @return Vector of all the constraints recognized.
	 */
	public Vector recognizeConstraints(Segment seg1, Segment seg2)
	{
		init(seg1, seg2);
		Vector tangentCons = null;
		Vector lengthCons = null;
		Vector collinearLinesCons = null;
		Vector angleCons = null;
		Vector cocentricityCons = null;
		//Vector pointOnSegmentCons = null;
	 // changed on 5-04-10	
		if((seg1!=null) && (seg2!=null))
		{
			if(seg1 instanceof SegLine && seg2 instanceof SegLine){			
				angleCons = recogAngleConstraints();
				lengthCons = recogLengthConstraints();				
			}
			else if((seg1 instanceof SegLine && seg2 instanceof SegCircleCurve) || (seg1 instanceof SegCircleCurve && seg2 instanceof SegLine)){
				tangentCons = recogTangencyConstraints();
			}
			else if(seg1 instanceof SegCircleCurve && seg2 instanceof SegCircleCurve){
				tangentCons = recogTangencyConstraints();
				lengthCons = recogLengthConstraints();		
			}

		}
		
		if(GVariables.getDRAWING_MODE() == GConstants.EDIT_MODE){
			DrawingView dv = MainWindow.getDv();
			Vector m_highlightedElements = dv.getM_highlightedElements();
			if(dv.isM_elementDragged() && (dv.getM_highlightedElements().size() > 0)){
				Iterator iter = m_highlightedElements.iterator();
				while (iter.hasNext()){
					GeometryElement seg = (GeometryElement)iter.next();
					if(seg instanceof SegCircleCurve){
						SegCircleCurve segCC = (SegCircleCurve)seg;
						if(seg1 instanceof SegCircleCurve && seg2 instanceof SegCircleCurve){
							if(segCC.equals(seg1) || segCC.equals(seg2)){
								cocentricityCons = recogCoCentricityConstraints();
								break;
							}
						}
					}	
					
					else if(seg instanceof SegLine){
						/////System.out.println("Seg Line");
						SegLine segL = (SegLine) seg; 
						if(seg1 instanceof SegLine && seg2 instanceof SegLine){
							if(segL.equals(seg1) || segL.equals(seg2)){
								collinearLinesCons = recogCollinearLinesConstraints();
								break;
							}
						}
					}
					else if(seg instanceof ImpPoint){
						ImpPoint segPt = (ImpPoint) seg;
						
          				Point2D point = segPt.getM_point();
          				if(seg1 instanceof SegCircleCurve && seg2 instanceof SegCircleCurve ){
          					if(point.equals(((SegCircleCurve)seg1).getM_center().getM_point()) ||
          							point.equals(((SegCircleCurve)seg2).getM_center().getM_point())){
          						cocentricityCons = recogCoCentricityConstraints();
          						break;
          					}
          				}
					}
				}
				
			}
		}
		//Vector sideCons = recogSideConstraints();
		
		Vector pointOnSegmentCons = recogPointOnSegmentConstraints();
		
		// add all the constraints found above, some processing can be done at this stage
		if(tangentCons!=null) m_constraints.addAll(tangentCons);
		if(collinearLinesCons!=null) m_constraints.addAll(collinearLinesCons);
//		if(collinearityPtCons != null) m_constraints.addAll(collinearityPtCons);
		if(angleCons != null) m_constraints.addAll(angleCons);
		if(lengthCons != null) m_constraints.addAll(lengthCons);
		//if(sideCons != null) m_constraints.addAll(sideCons);
		if(cocentricityCons != null) m_constraints.addAll(cocentricityCons);
		if(pointOnSegmentCons!=null) m_constraints.addAll(pointOnSegmentCons);
		
		return m_constraints;
	}
	
	
	private Vector recogLengthConstraints()
	{
		if(m_relLengthRecog == null) m_relLengthRecog = new RelLengthRecognizer(m_seg1, m_seg2);
		else m_relLengthRecog.init(m_seg1, m_seg2);
		//if(m_seg1 instanceof SegLine && m_seg2 instanceof SegLine)
			return m_relLengthRecog.recognize();
		//return null;
	}	
	
	
	public Vector recogAngleConstraints()
	{
		if(m_relAngleRecog == null) m_relAngleRecog = new RelAngleRecognizer(m_seg1, m_seg2);
		else m_relAngleRecog.init(m_seg1, m_seg2);
		//if(m_seg1 instanceof SegLine && m_seg2 instanceof SegLine)
			return m_relAngleRecog.recognize();
		//return null;
	}
	
	private Vector recogSideConstraints()
	{
		if(m_relSideRecog == null) m_relSideRecog = new RelSideRecognizer(m_seg1, m_seg2);
		else m_relSideRecog.init(m_seg1, m_seg2);
		return m_relSideRecog.recognize();
	}
	
	private Vector recogCoCentricityConstraints()
	{
		if(m_coCentricityRecog == null) m_coCentricityRecog = new CoCentricityRecognizer(m_seg1, m_seg2);
		else m_coCentricityRecog.init(m_seg1, m_seg2);
		//if((m_seg1 instanceof SegCircleCurve) && (m_seg2 instanceof SegCircleCurve))
			return m_coCentricityRecog.recognize();
		//return null;
	}
	
	private Vector recogPtCollinearityConstraints()
	{
		if(m_collinearityPtRecog == null) m_collinearityPtRecog = new CollinearPointsRecognizer(m_seg1, m_seg2);
		else m_collinearityPtRecog.init(m_seg1, m_seg2);
		return m_collinearityPtRecog.recognize();
	}
	
	private Vector recogPointOnSegmentConstraints()
	{
		if(m_pointOnSegmentRecog == null) m_pointOnSegmentRecog = new pointOnSegmentRecognizer(m_seg1, m_seg2);
		else m_pointOnSegmentRecog.init(m_seg1, m_seg2);
		return m_pointOnSegmentRecog.recognize();
	}
	
	private Vector recogTangencyConstraints()
	{
		if(m_tangencyRecog == null) m_tangencyRecog = new tangencyRecognizer(m_seg1, m_seg2);
		else m_tangencyRecog.init(m_seg1, m_seg2);
		return m_tangencyRecog.recognize();
	}
	
	private Vector recogCollinearLinesConstraints()
	{
		if(m_coLlinearLinesRecog == null) m_coLlinearLinesRecog  = new CollinearLinesRecognizer(m_seg1, m_seg2);
		else m_coLlinearLinesRecog.init(m_seg1, m_seg2);
		//if(m_seg1 instanceof SegLine && m_seg2 instanceof SegLine)
			return m_coLlinearLinesRecog.recognize();
		//return null;
	}
}