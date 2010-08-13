package dcad.model.geometry;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Vector;

import javax.activation.MailcapCommandMap;

import dcad.model.geometry.segment.SegLine;
import dcad.model.geometry.segment.Segment;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GConstants;
import dcad.util.GVariables;



// make is as good as basepoint class of shikav 
/**
 * 
 */
public abstract class ImpPoint extends GeometryElement
{
	protected Point2D m_point = null;
	protected int m_size;
	protected int m_type;
	public static final int TYPE_CIRCLE = 1;
	public static final int TYPE_SQUARE = 2;
	public static final int TYPE_TRIANGLE = 3;
	protected Vector vecParent = new Vector();
	

	public ImpPoint(Point2D point, Vector parent)
	{
		if(point == null)
			m_point = new Point2D.Double(-1, -1);
		else
			m_point = point;
		if(!(parent==null))
			vecParent=parent;
		else
			;
		setM_color(GVariables.DEF_IP_COLOR);
	}

	protected void drawPoint(Graphics2D g2d, int size, int type)
	{	
		int x = (int)(getX()+0.5);
		int y = (int)(getY()+0.5);
		switch(type)
		{
			case ImpPoint.TYPE_SQUARE:
			g2d.fillRect(x-size+1, y-size+1, size*2, size*2);
			break;
			
			case ImpPoint.TYPE_CIRCLE:
			g2d.fillArc(x-size+1, y-size+1, size*2, size*2, 0, 360);
			break;

			case ImpPoint.TYPE_TRIANGLE:
			int ydisp = (int)((size/3.464)+0.5);
			g2d.fillPolygon(new int[]{x, x+(size/2), x-(size/2)}, new int[]{y-2*ydisp, y+ydisp, y+ydisp}, 3);	
			break;
				
			default: 
			g2d.drawRect((int)getM_point().getX()-size+1, (int)getM_point().getY()-size+1, size*2, size*2);
		}
		
		// reset the color back
//		if(m_type == AP_TYPE_SQUARE) g.drawRect((int)m_AnchorPoint.getM_point().getX() - m_size+1, (int)m_AnchorPoint.getM_point().getY() - m_size+1, m_size*2, m_size*2);
//		else if(m_type == AP_TYPE_CIRCLE) g.drawArc((int)m_AnchorPoint.getM_point().getX() - m_size+1, (int)m_AnchorPoint.getM_point().getY() - m_size+1, m_size*2, m_size*2, 0, 360);

	}
	
	public Point2D getM_point()
	{
		if(m_point==null)
			System.out.println("m_point is null.. This is getM_point function of ImpPoint.java");
		return m_point;
	}

	public void setM_point(Point2D point)
	{
		this.m_point.setLocation(point);
	}
	
	public void setM_point(double x, double y)
	{
		this.m_point.setLocation(x, y);
	}
	
	public double getX()
	{
		return m_point.getX();
	}

	public double getY()
	{
		return m_point.getY();
	}

	/**
	 * get the distance pt from anchor point in case its within limits., -1 otherwise
	 * @param pt
	 * @return distance
	 */
	public double distance(Point2D pt)
	{
		return getM_point().distance(pt);
	}

	// Returns the straight line distance between this Point and the new Point
	public double distance(ImpPoint q)
	{
		return distance(q.getM_point());
	}

	public double distance(double x, double y)
	{
		return getM_point().distance(x, y);
	}

	public void move(double xnew, double ynew)
	{
		if (!isFixed())
		{
			// maintain the order of the statements
			Point2D oldPT = (Point2D)m_point.clone();
			m_point.setLocation(xnew, ynew);
			
			for(int l=0;l<vecParent.size();l++)
			{
				Segment seg = (Segment)vecParent.elementAt(l);
				if(seg != null) {
					seg.movePt(this, oldPT);
					// added on 18-05-10
					// to remove constraints from a line segment when its both points are merged 
			/*		if(seg instanceof SegLine){
						DrawingView dv = MainWindow.getDv();
						Point2D pt1 = new Point2D.Double(seg.getSegStart().getX(), seg.getSegStart().getY());
						Point2D pt2 = new Point2D.Double(seg.getSegEnd().getX(), seg.getSegEnd().getY());
						double dist1 = pt1.distance(m_point);
						double dist2 = pt2.distance(m_point);
						
						
						if(((dist1 <= (m_size+seg.getSegStart().m_size)) && (dist2 <= (m_size+seg.getSegEnd().m_size))) ){
							dv.setMovedPointSegment(seg);
							System.out.println("Both points are equal");
						}
						else{
							dv.setMovedPointSegment(null);
							System.out.println("Both points are not equal");
						}
					/*	if(pt1.equals(m_point) && pt2.equals(m_point)){
							//seg.removeAllConstraints();
							
							dv.setMovedPointSegment(seg);
							System.out.println("Both points are equal");
						}
						else{
							dv.setMovedPointSegment(null);
							System.out.println("Both points are not equal");
						}*/
						/*if(pt1.equals(m_point)){
							System.out.println("Start point of segment is equal");
						}
						if(pt2.equals(m_point)){
							System.out.println("End point of segment is equal");
						}
					}*/
				}
			}
		}
	}
	
	//ISHWAR Added this function
	public void move4Constraints(double xnew,double ynew)
	{
		if(!isFixed())
		{
			m_point.setLocation(xnew, ynew);
			for(int l=0;l<vecParent.size();l++)
			{
				Segment seg = (Segment)vecParent.elementAt(l);
				if(seg != null) seg.movePt4Constraints();
			}
		}
	}
	

	public void move(int x1, int y1, int x2, int y2)
	{
		move(x2, y2);
	}
	
	public boolean containsPt(double x1, double y1)
	{
		boolean returnVal = false;
		double dist = getM_point().distance(x1, y1);
		int x = (int)(getX()+0.5);
		int y = (int)(getY()+0.5);
		switch (m_type)
		{
			case ImpPoint.TYPE_SQUARE:
				Rectangle2D rect = new Rectangle((int)getM_point().getX()-m_size+1, (int)getM_point().getY()-m_size+1, m_size*2, m_size*2);
				if(rect.contains(x, y)) returnVal = true;
				break;
			
			case ImpPoint.TYPE_CIRCLE:
				if(dist <= m_size*3) returnVal = true;
				break;
	
			case ImpPoint.TYPE_TRIANGLE:
				int ydisp = (int)((m_size/3.464)+0.5);
				Polygon poly = new Polygon(new int[]{x, x+(m_size/2), x-(m_size/2)}, new int[]{y-2*ydisp, y+ydisp, y+ydisp}, 3);
				if(poly.contains(x, y)) returnVal = true;
				break;
	
			default: returnVal = false;
			break;
		}
		
		return returnVal;
	}
	
	public void setFixed(boolean fixed)
	{
		// If the call is to fix the point, just do it
		if(fixed==true)
			super.setFixed(true);
		else // If the call is to free the point, check if all parents are free
		{
			int l;
			for(l=0;l<vecParent.size();l++)
				if( ((GeometryElement)vecParent.elementAt(l)).isFixed())
					break;
			if(l==vecParent.size())
				super.setFixed(fixed);
		}
	}
	
	public Vector findNeighboringPts(Vector allPts)
	{
		Vector closePts = new Vector();
		Iterator iter = allPts.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			double dist = distance(ip);
			if((dist <= (m_size+ip.m_size)) && (!ip.equals(this)))
			{
				closePts.add(ip);
			}
		}
		
		return closePts;
	}
	
	public ImpPoint findClosestIP(Vector closePts)
	{
		ImpPoint closePT = null;
		double closeDist = Double.MAX_VALUE;
		if(closePts!=null)
		{
			Iterator iter = closePts.iterator();
			while (iter.hasNext())
			{
				ImpPoint ip = (ImpPoint) iter.next();
				double dist = ip.distance(this);
				if((dist <= (m_size+ip.m_size)) && (!ip.equals(this))&&(dist < closeDist))
				{
					closeDist = dist;
					closePT = ip; 
				}
			}
		}
		if(closePT != null)
			return closePT;
		return null;
	}

	public Point2D findClosestSeg(Vector closeSegments)
	{
		Point2D closePt = null;
		double closeDist = Double.MAX_VALUE;
		if(closeSegments!=null)
		{
			int z;
			for( z=0;z<vecParent.size();z++)
				if(vecParent.elementAt(z) instanceof Segment)
					break;

			int l=0;
			Vector v=getAllParents();
			for(l=0;l<getAllParents().size();l++)
				closeSegments.remove(v.get(l));
			
			Iterator iter = closeSegments.iterator();
			while (iter.hasNext())
			{
				Segment seg = (Segment) iter.next();
				Point2D nearPt = seg.getNearestPointOnSeg(m_point);
				if(nearPt != null)
				{
					double dist = nearPt.distance(m_point);
					if((dist < closeDist) && (containsPt(nearPt)))
					{
						closeDist = dist;
						closePt = nearPt; 
					}
				}
			}
		}
//		System.out.println(closePt);
		return closePt;
	}
	
	public String toString()
	{
//		System.out.println("ImpPoint.toString()");
		return getM_label()+" "+getM_point() + ": "+m_parent ;
	}
	
	
	public void deleteSelf()
	{
		vecParent=new Vector();
		this.m_point=null;
		//this.setM_point(5,5);
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof ImpPoint)
		{
			ImpPoint ip = (ImpPoint)obj;
			if(ip.getM_strId().equals(getM_strId())) return true;
//			// check if  any of the parents are null
//			if((getM_parent() == null) || (ip.getM_parent() == null))
//			{
//				return false;
//			}
//			else if((ip.getM_point().equals(m_point)) && (ip.getM_parent().equals(m_parent)))
//			{
//				return true;
//			}
		}
		return false;
	}

	public boolean overlap(ImpPoint point)
	{
		return overlap(point.getM_point());
	}

	public boolean overlap(Point2D point)
	{
		return containsPt(point);
		//      // in case the points are very close, they are overlapping
//		if((m_point.equals(point)) || (distance(point) <= 0.1))
//		{
//			return true;
//		}
//		return false;
	}
	
	public void addParent(Object o)
	{
		if(!(vecParent.contains(o)))
			vecParent.add(o);
	}
	
	public void removeParent(Object o)
	{
//		if(!(vecParent.contains(o)))
			vecParent.remove(o);
	}

	
	public Vector getAllParents()
	{
			return vecParent;
	}

	public void setAllParents(Vector parents)
	{
			vecParent = parents;
	}
	
	

}
