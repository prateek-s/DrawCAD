package dcad.process.preprocess;

import java.util.Iterator;
import java.util.Vector;

import dcad.model.geometry.PixelInfo;
import dcad.model.geometry.SegmentPoint;
import dcad.model.geometry.Stroke;

public class PreProcessor
{
	private Segmentor m_segmentor;
	private SegmentorNew m_segmentorNew;

	public PreProcessor()
	{
		init();
	}

	/**
	 * Method to initialize various member variables
	 *
	 */
	private void init()
	{
		m_segmentor = new Segmentor();
		m_segmentorNew = new SegmentorNew();
	}
	
	/**
	 * This method is the main method called to preprocess the data. Internally
	 * this method would call other routines for segmentation, basic beautification etc.
	 * @return Vector, Decorated segment points as a Vector
	 */
	public Vector preProcess(Stroke theStroke)
	{
		// perform segmentation of the stroke to get the segment points/ breakpoints
		Vector segmentPos = m_segmentor.performSegmentation(theStroke);
		//Vector segmentPos = m_segmentorNew.performSegmentation(theStroke);
		
		
		// find the pixelInfo objects related to the segment point position
		Vector segPts = new Vector();
		Iterator iter = segmentPos.iterator();
		while (iter.hasNext())
		{
			Integer anInt = (Integer) iter.next();
			PixelInfo pixel = (PixelInfo) theStroke.getM_ptList().get(anInt.intValue());
			
			//CHINTAN
			Vector tempV = new Vector();
			tempV.add(theStroke);
			segPts.add(new SegmentPoint(pixel, tempV));
		}
		
		return segPts;
//		return segmentPos;
	}

}
