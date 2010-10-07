package dcad.ui.main;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.Text;
import dcad.model.geometry.segment.Segment;
import dcad.model.marker.Marker;

public class ActionHelper 
{

	/** Add, remove elements from the draw data */
	public void addGeoElement(GeometryElement gEle) 
	{
		if (gEle instanceof Stroke)
			m_drawData.addStroke((Stroke) gEle);
		else if (gEle instanceof Text)
			m_drawData.addTextElement((Text) gEle);
		else if (gEle instanceof Marker)
			m_drawData.addMarker((Marker) gEle);
	}

	public void removeGeoElement(GeometryElement gEle) 
	{
		if (gEle instanceof Stroke)
			m_drawData.removeStroke((Stroke) gEle);
		else if (gEle instanceof Text)
			m_drawData.removeTextElement((Text) gEle);
		else if (gEle instanceof Marker)
			m_drawData.removeMarker((Marker) gEle);
	}
	/************************************************************/

	public Vector isPtOnAnyAnchorPoint(Point pt) 
	{
		Vector selectedAps = new Vector();
		Vector apList = m_drawData.getAllAnchorPoints();
		for (int i = 0; i < apList.size(); i++) {
			AnchorPoint ap = (AnchorPoint) apList.get(i);
			if (ap.containsPt(pt))
				selectedAps.add(ap);
		}
		return selectedAps;
	}

	public Segment isPtOnAnySegment(Point2D pt)
	{
		// find the closest segment
		Vector segList = m_drawData.getAllSegments();
		Iterator itr = segList.iterator();
		while (itr.hasNext()) {
			Segment seg = (Segment) itr.next();
			if (seg.containsPt(pt))
				return seg;
		}

		return null;
	}

	public GeometryElement isPtOnAnyText(Point2D pt) 
	{
		Iterator itr = m_drawData.getM_textElements().iterator();
		while (itr.hasNext()) {
			Text txt = (Text) itr.next();
			if (txt.containsPt(pt))
				return txt;
		}
		return null;
	}

	public Marker isPtOnAnyMarker(Point2D pt) 
	{
		Vector markerList = m_drawData.getM_markers();
		Iterator itr = markerList.iterator();
		while (itr.hasNext()) {
			Marker marker = (Marker) itr.next();
			if (marker.containsPt(pt))
				return marker;
		}
		return null;
	}

	public Stroke isPtOnAnyStroke(Point2D pt) 
	{
		for (int i = m_drawData.getStrokeList().size() - 1; i >= 0; i--) {
			Stroke stroke = (Stroke) m_drawData.getStrokeList().get(i);
			if (stroke.containsPt(pt))
				return stroke;
		}
		return null;
	}

	public Vector isPtOnGeometryElement(Point2D pt) 
	{
		Vector gEles = new Vector();
		Vector aps = isPtOnAnyAnchorPoint(m_mousePos);
		if ((aps != null) && (aps.size() > 0)) {
			gEles.addAll(aps);
			return gEles;
		}

		// check if the mouse is close to any other geometry element
		GeometryElement gEle = isPtOnAnySegment(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled())) {
			gEles.add(gEle);
			return gEles;
		}

		// check if point is on any Text element
		gEle = isPtOnAnyText(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled())) {
			gEles.add(gEle);
			return gEles;
		}

		// check if point is on any marker
		gEle = isPtOnAnyMarker(m_mousePos);
		if ((gEle != null) && (gEle.isEnabled())) {
			gEles.add(gEle);
			return gEles;
		}

		return gEles;
	}

	
	
	
}
