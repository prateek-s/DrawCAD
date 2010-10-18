package dcad.model.geometry.segment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.circleArc.circularArcConstraint;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;
import dcad.model.geometry.Stroke;
import dcad.model.marker.Marker;
import dcad.ui.drawing.DrawingView;
import dcad.ui.main.MainWindow;
import dcad.util.GMethods;
import dcad.util.GVariables;
import dcad.util.Maths;

public class SegCircleCurve extends SegCurve
{
	private AnchorPoint m_center;
	private AnchorPoint m_start;
	private AnchorPoint m_end;

	private double m_radius;
	private double m_startAngle;
	private double m_arcAngle;
	private double dummyArcAngle;
	private Vector m_points;

	
	public SegCircleCurve(Vector points)
	{
		super();
		m_points=points;
		m_type = Segment.CIRCLE;
	}
	
	//Called immediately after the curve is recognized. This cannot be included in constructor because there some of the variables are not initialized
	public void updateDetails()
	{
			// the member variables are not set, calculate and set them
			// find values for member variables, once and for all
			Point2D startPt = (Point2D) m_points.get(0);
			Point2D endPt = (Point2D) m_points.get(1);
			Point2D centerPt = (Point2D) m_points.get(2);

			// System.out.println("start: "+startPt+", end: "+endPt+", center: "+centerPt);
			double radius = ((Point2D) m_points.get(3)).getX();
			// System.out.println("Radius: "+radius);

			Vector rawPts = getRawPoints();
			// set the translator
			AffineTransform tx = new AffineTransform();
			// translate these points by placing center at origin
			tx.setToTranslation(-centerPt.getX(), -centerPt.getY());
			Point2D newStartPt = new Point2D.Double();
			Point2D newEndPt = new Point2D.Double();
			tx.transform(startPt, newStartPt);
			tx.transform(endPt, newEndPt);
			double startAngle = Math.toDegrees(Maths.angle(-newStartPt.getY(), newStartPt.getX()));
			double endAngle = Math.toDegrees(Maths.angle(-newEndPt.getY(), newEndPt.getX()));
			if (startAngle < 0)
				startAngle += 360;
			if (endAngle < 0)
				endAngle += 360;
			// System.out.println("-----------: "+newStartPt+" , Angle: "+startAngle);
			// System.out.println("-----------: "+newEndPt+" , Angle: "+endAngle);

			double arcAngle = 0.0;
			Iterator iter = rawPts.iterator();

			// find the net angle
			Point2D thisPt = (Point2D) iter.next();
			Point2D newThisPt = new Point2D.Double();
			tx.transform(thisPt, newThisPt);
			double thisAngle = Math.toDegrees(Maths.angle(-newThisPt.getY(), newThisPt.getX()));
			if (thisAngle < 0)
				thisAngle += 360;

			Point2D nextPt = null;
			Point2D newNextPt = new Point2D.Double();
			while (iter.hasNext())
			{
				nextPt = (Point2D) iter.next();
				tx.transform(nextPt, newNextPt);
				double nextAngle = Math.toDegrees(Maths.angle(-newNextPt.getY(), newNextPt.getX()));
				if (nextAngle < 0)
					nextAngle += 360;

				arcAngle += getCurveAngle(thisAngle, nextAngle);
				thisAngle = nextAngle;
				// System.out.println("++++++++++"+arcAngle+", "+newNextPt);
			}

			
			m_radius=radius;
			m_startAngle=startAngle;
			if (Math.abs(arcAngle) <= 360)
			{
				setM_arcAngle(arcAngle);
			} else
			{
				// arc angle is greater that the whole circle
//				m_end.getM_point().setLocation(m_start.getM_point());
				endPt.setLocation(startPt.getX(),startPt.getY());
				if (arcAngle > 360)
					setM_arcAngle(360);
				else if (arcAngle < -360)
					setM_arcAngle(-360);
			}

			
			
			// here we have the total angle
//ISHWAR			System.out.println("Angles are: " + (startAngle) + " , " + (endAngle) + " , "+ (arcAngle));

			// set the member variables
/*ISHWAR			System.out.println("START:  "
					+ newStartPt
					+ ", "
					+ GMethods.getPointOnCircle(Math.atan2(startPt.getY() - centerPt.getY(),
							startPt.getX() - centerPt.getX()), centerPt, radius));
			System.out.println("END:  "
					+ newEndPt
					+ ", "
					+ GMethods.getPointOnCircle(Math.atan2(endPt.getY() - centerPt.getY(), endPt
							.getX()
							- centerPt.getX()), centerPt, radius));*/

			// add start anchor point
			m_start = addAnchorPoint(GMethods.getPointOnCircle(Math.atan2(startPt.getY()
					- centerPt.getY(), startPt.getX() - centerPt.getX()), centerPt, radius));

			// add mid anchor point
			// addAnchorPoint(GMethods.getPointOnCircle(Math.atan2((startPt.getY()+endPt.getY())/2
			// -centerPt.getY(),
			// (startPt.getX()+endPt.getX())/2-centerPt.getX()), centerPt,
			// radius), Color.GREEN);

			// add end anchor point
			m_end = addAnchorPoint(GMethods.getPointOnCircle(Math.atan2(endPt.getY()
					- centerPt.getY(), endPt.getX() - centerPt.getX()), centerPt, radius));

			// add center anchor point
			m_center = addAnchorPoint(centerPt);
			// set the color of this AP to backgroud color so that tis visible
			// only when selected.
			m_center.setM_color(GVariables.BACKGROUND_COLOR);
			// m_center.setM_size(3);

			setM_shape(new Arc2D.Double((m_center.getX() - m_radius), (m_center.getY() - m_radius),
				(2 * m_radius), (2 * m_radius), m_startAngle, m_arcAngle, Arc2D.OPEN));
					
		

			//Add a constraint saying that the center should be at equal distance from the start and the end points
			new circularArcConstraint(this,Constraint.HARD,false);
			//addConstraint();
	}
	
	public void draw(Graphics g)
	{
		if (!isEnabled())
			return;
		super.draw(g);

		Graphics2D g2d = (Graphics2D) g;
		Color prevColor = g2d.getColor();
		//added on 28-05-10
		if(m_parentStk.getM_type()==Stroke.TYPE_MARKER){
			g2d.setColor(GVariables.MARKER_COLOR);
			DrawingView dv = MainWindow.getDv();
			Point pt = dv.getM_mousePos();
			Marker marker = null;
			Vector segList = m_parentStk.getM_segList();
			Iterator iter = segList.iterator();
			if ((marker = dv.A.isPtOnAnyMarker(pt)) != null){
				while(iter.hasNext()){
				SegCircleCurve seg = (SegCircleCurve)iter.next();
				if(seg.isSelected()){
					g2d.setColor(GVariables.SELECTED_COLOR);
				}
				else if(seg.containsPt(pt)){
						g2d.setColor(getM_color());
					}
				}	
			}
			else{
				while(iter.hasNext()){
					SegCircleCurve seg = (SegCircleCurve)iter.next();	
					if(seg.isSelected()){
						g2d.setColor(GVariables.SELECTED_COLOR);
					}
				}
			}
		}
		else{
		g2d.setColor(getM_color());
		}
		
		// draw the circular arc
		g2d.draw(m_shape);

		// check if any anchor point is selected
		boolean anythingHighlighted = isHighlighted();
		if (!anythingHighlighted)
			for (int i = 0; i < m_impPoints.size(); i++)
				if (((ImpPoint) m_impPoints.get(i)).isHighlighted())
					anythingHighlighted = true;
		if (anythingHighlighted)
		{
			// create a dashed line for radii lines
			BasicStroke prevStroke = (BasicStroke) g2d.getStroke();
			g2d.setColor(GVariables.DRAWING_ASSIST_COLOR);
			g2d.setStroke(new BasicStroke(prevStroke.getLineWidth(), prevStroke.getEndCap(),
					prevStroke.getLineJoin(), prevStroke.getMiterLimit(),
					new float[] { 4, 4, 8, 4 }, prevStroke.getDashPhase()));
			g2d.drawLine((int) m_center.getX(), (int) m_center.getY(), (int) m_start.getX(),
					(int) m_start.getY());
			g2d.drawLine((int) m_center.getX(), (int) m_center.getY(), (int) m_end.getX(),
					(int) m_end.getY());
			g2d.setStroke(prevStroke);
			
			//ISHWAR - For showing arc angle and radius
			/*g2d.drawString(String.valueOf(getM_arcAngle()), (float)m_center.getX(),(float)m_center.getY());
			g2d.drawArc(  (int)(m_center.getX()-20)  , (int)(m_center.getY()-20)  ,40, 40, (int)m_startAngle,(int)m_arcAngle);
			g2d.drawString(String.valueOf(m_radius), (float)m_start.getX(),(float)m_end.getY());*/
			
		} 
		// reset the graphics color back
		g2d.setColor(prevColor);
		
	}

	public void movePt4Constraints()
	{
		
		
		Point2D newStartPt = m_start.getM_point();//new Point2D.Double();
		Point2D newEndPt = m_end.getM_point();//new Point2D.Double();
		Point2D newPt = new Point2D.Double();
		Point2D newCenter = m_center.getM_point();//new Point2D.Double();
		double startAngle = m_startAngle;
		double endAngle = dummyArcAngle + m_startAngle;
		double newStartAngle = 0;
		double newEndAngle = 0;
		double newRad = m_radius;

		// starting point moved/ adjust the arc
		double distCE = m_center.distance(m_start);

		// calculate the avg radius
		newRad = (distCE + m_radius) / 2;
		newRad=m_center.distance(m_start);

		newStartAngle=Maths.angleInDegrees(m_start.getM_point(), m_center.getM_point());
		if(newStartAngle>=0)
			newStartAngle=180 - newStartAngle;
		else
			newStartAngle = 180 + (-1)*newStartAngle;
			
		newEndAngle=Maths.angleInDegrees(m_end.getM_point(), m_center.getM_point());
		if(newEndAngle>=0)
			newEndAngle=180 - newEndAngle;
		else
			newEndAngle= 180 + (-1)*newEndAngle;

		
//		newStartAngle = Math.toDegrees(Maths.angle(-newStartPt.getY(), newStartPt.getX()));
//		newEndAngle = Math.toDegrees(Maths.angle(-newEndPt.getY(), newEndPt.getX()));

		if ((Double.isNaN(newStartAngle) || (Double.isNaN(newEndAngle))
				|| (Double.isInfinite(newStartAngle)) || (Double.isInfinite(newEndAngle))))
		{
			return;
		}

//		m_center.setM_point(newCenter);
		// cenAP.getM_point().setLocation(m_center);
		m_radius = newRad;
		m_startAngle = newStartAngle;
		if (m_startAngle < -360)
			m_startAngle += 360;
		if (m_startAngle > 360)
			m_startAngle -= 360;

		if (dummyArcAngle >= 0)
		{
			// antiClockwise
			if (dummyArcAngle <= 360)
			{
				if (newEndAngle <= newStartAngle)
				{
					dummyArcAngle = newEndAngle + (360 - newStartAngle);
				} else
				{
					dummyArcAngle = newEndAngle - newStartAngle;
				}

			}
		} else
		{
			// clockwise
			if (newEndAngle <= newStartAngle)
			{
				dummyArcAngle = newStartAngle - newEndAngle;
			} else
			{
				dummyArcAngle = newStartAngle + 360 - newEndAngle;
			}
			dummyArcAngle = -dummyArcAngle;
		}
		setM_arcAngle(dummyArcAngle);
		

		Arc2D arc = (Arc2D) m_shape;
		arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius),
				(2 * m_radius), (2 * m_radius), m_startAngle,dummyArcAngle, Arc2D.OPEN);

		setDetails();

		
		if(true)
			return;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		double sAngle,eAngle,aAngle=0,radius;
		System.out.println("\n\n\n");
		System.out.println("**************************************************************");
		System.out.println("radius : "+m_start.getM_point().distance(m_center.getM_point()));
		//System.out.println(m_start.getM_point().);
		System.out.println( ("start angle : " + Maths.angleInDegrees(m_start.getM_point(), m_center.getM_point())) );
		System.out.println( ("end angle : " +Maths.angleInDegrees(m_end.getM_point(), m_center.getM_point())) );
		System.out.println("**************************************************************");
		System.out.println("\n\n\n");
		radius = m_start.getM_point().distance(m_center.getM_point());
		sAngle=Maths.angleInDegrees(m_start.getM_point(), m_center.getM_point());
		eAngle=Maths.angleInDegrees(m_end.getM_point(), m_center.getM_point());
		m_startAngle= sAngle;
		m_radius=radius;
		setM_arcAngle(aAngle);
		if(sAngle==0 && eAngle==0)
			aAngle=360;
		if(sAngle==0 && eAngle<0)
			aAngle=360+eAngle;
		else if(sAngle==0 && eAngle>0)
			aAngle=eAngle;
		else if(eAngle==0 && sAngle<0)
			aAngle=-1*sAngle;
		else if(eAngle==0 && sAngle>0)
			aAngle=360-sAngle;
		if(sAngle<0 && eAngle>0)
			aAngle=eAngle-sAngle;
		else if(sAngle<0 && eAngle<0 && sAngle<eAngle)
			aAngle=(sAngle-eAngle) * -1;
		else if(sAngle<0 && eAngle<0 && sAngle>eAngle)
			aAngle=360 - ((eAngle-sAngle)*-1);
		else if(sAngle>0 && eAngle<0)
			aAngle=360+eAngle-sAngle;
		else if(sAngle>0 && eAngle>0 && sAngle<eAngle)
			aAngle=eAngle-sAngle;
		else if(sAngle>0 && eAngle>0 && sAngle>eAngle)
			aAngle=360+eAngle-sAngle;
		else
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%   PROBLEM   %%%%%%%%%%%%%%%%%%%%%%%");
			
//		Arc2D arc = (Arc2D) m_shape;
		arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius),
				(2 * m_radius), (2 * m_radius), 180-sAngle, -aAngle, Arc2D.OPEN);

		setDetails();

//		setM_shape(new Arc2D.Double((m_center.getX() - m_radius), (m_center.getY() - m_radius),
	//			(2 * m_radius), (2 * m_radius), sAngle, aAngle, Arc2D.OPEN));

	}
	
	public void movePt(ImpPoint ap, Point2D pt)
	{
		
		// System.out.println("SegCircleCurve.movePt()");
		// System.out.println(m_center+" :"+ap.getM_point());
		if (m_center.equals(ap))
		{
			// center is been moved, .. so move the whole arc
			boolean canMove = true;
			Iterator iter = m_impPoints.iterator();
			while (iter.hasNext())
			{
				ImpPoint ip = (ImpPoint) iter.next();
				if (ip.isFixed())
				{
					canMove = false;
					break;
				}
			}

			if (canMove)
			{
				moveSegThruCen(ap, pt);
				// cenAP.getM_point().setLocation(m_center);
				Arc2D arc = (Arc2D) m_shape;
				arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius),
						(2 * m_radius), (2 * m_radius), m_startAngle, m_arcAngle, Arc2D.OPEN);
				setDetails();
			} else
			{
				if ((m_start.isFixed()) && (m_end.isFixed()))
				{
					// both the points are fixed
					moveCenterWithEndPtFixed(ap, pt);
					// m_center.setEnabled(true);
					Arc2D arc = (Arc2D) m_shape;
					arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius),
							(2 * m_radius), (2 * m_radius), m_startAngle, m_arcAngle, Arc2D.OPEN);

					setDetails();
				} else
				{
					// revert back the change
					ap.getM_point().setLocation(pt);
				}
			}
		} else
		{
			if (!m_center.isFixed())
			{
				movePtCenterFree(ap, pt);
			} else
			{
				movePtCenterFixed(ap, pt);
			}
			// m_center.setEnabled(true);
			Arc2D arc = (Arc2D) m_shape;
			arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius), (2 * m_radius),
					(2 * m_radius), m_startAngle, m_arcAngle, Arc2D.OPEN);

			setDetails();
		}
		// updateConstarints();
	}


	private void moveCenterWithEndPtFixed(ImpPoint ap, Point2D pt)
	{
		// check if the ap is still between the two end points
		// get the nearest point from ap on the line joining the two end points
		Line2D line = new Line2D.Double(m_start.getM_point(), m_end.getM_point());
		double dist = line.ptLineDist(ap.getM_point());

		// get the new radius
		double newRad = Math.sqrt((m_start.distance(m_end) * m_start.distance(m_end) / 4) + dist
				* dist);
		Vector centers = GMethods.CircleCircleIntersections(m_start.getX(), m_start.getY(), newRad,
				m_end.getX(), m_end.getY(), newRad);
		Point2D newCenter = null;
		if (centers != null)
		{
			Point2D center1 = (Point2D) centers.get(0);
			Point2D center2 = (Point2D) centers.get(1);
			if (line.relativeCCW(center1) == line.relativeCCW(ap.getM_point()))
			{
				newCenter = center1;
			} else if (line.relativeCCW(center2) == line.relativeCCW(ap.getM_point()))
			{
				newCenter = center2;
			} else
			{
				newCenter = center1;
			}

			double newStartAngle = Math.toDegrees(Maths.angle(-m_start.getY() + newCenter.getY(),
					m_start.getX() - newCenter.getX()));
			double newEndAngle = Math.toDegrees(Maths.angle(-m_end.getY() + newCenter.getY(), m_end
					.getX()
					- newCenter.getX()));

			if (!((Double.isNaN(newStartAngle) || (Double.isNaN(newEndAngle))
					|| (Double.isInfinite(newStartAngle)) || (Double.isInfinite(newEndAngle)))))
			{
				m_center.getM_point().setLocation(newCenter);
				m_startAngle = newStartAngle;
				if (m_startAngle < -360)
					m_startAngle += 360;
				if (m_startAngle > 360)
					m_startAngle -= 360;
				if (m_arcAngle >= 0)
				{
					setM_arcAngle(newEndAngle - newStartAngle);
					if (m_arcAngle < 0)
						setM_arcAngle(m_arcAngle + 360);
				} else
				{
					setM_arcAngle(newEndAngle - newStartAngle);
					if (m_arcAngle > 0)
						setM_arcAngle(m_arcAngle - 360);
				}
				m_radius = newRad;
			}
		} else
		{
			// revert back the change
			ap.getM_point().setLocation(pt);
		}
	}

	private void moveSegThruCen(ImpPoint ap, Point2D pt)
	{
		// System.out.println("SegCircleCurve.moveSegThruCen()");
		Point2D newPt = (Point2D) ap.getM_point().clone();
		ap.getM_point().setLocation(pt);
		move(pt, newPt);
	}

	private void movePtCenterFixed(ImpPoint ap, Point2D pt)
	{
		// dummyArcAngle = m_arcAngle;
		// find the angle which the center make with the new point as well as
		// the old point
		double angle1 = Maths.angle(m_center.getX(), m_center.getY(), pt.getX(), pt.getY());
		double angle2 = Maths.angle(m_center.getX(), m_center.getY(), ap.getX(), ap.getY());

		// revert back the ap to the old point pt
		ap.getM_point().setLocation(pt);

		AffineTransform tx = new AffineTransform();
		tx.setToRotation(angle2 - angle1, m_center.getX(), m_center.getY());

		double angleChange = Math.toDegrees(angle2 - angle1);
		if (Math.abs(angleChange) > 180)
		{
			if (angleChange < -180)
				angleChange += 360;
			if (angleChange > 180)
				angleChange -= 360;
		}
		if (m_start.isFixed() || m_end.isFixed())
		{
			// the other end point is fixed
			if (ap.equals(m_start))
			{
				// end point is fixed, thus we are movinf the start point
				tx.transform(m_start.getM_point(), m_start.getM_point());
				m_startAngle += -angleChange;// Math.toDegrees(angle1-angle2);
				if (m_startAngle < -360)
					m_startAngle += 360;
				if (m_startAngle > 360)
					m_startAngle -= 360;
				dummyArcAngle += angleChange;// Math.toDegrees(angle2-angle1);
				setM_arcAngle(dummyArcAngle);
				// if(dummyArcAngle < -720) dummyArcAngle += 360;
				// if(dummyArcAngle > 720) dummyArcAngle -= 360;
				// if((dummyArcAngle < -360)||(dummyArcAngle > 360)) m_arcAngle
				// = 360;
				// else m_arcAngle = dummyArcAngle;
			} else
			{
				// start point is fixed, thus we are moving the end point
				tx.transform(m_end.getM_point(), m_end.getM_point());
				dummyArcAngle += -angleChange;// Math.toDegrees(angle1-angle2);
				setM_arcAngle(dummyArcAngle);
				// if(dummyArcAngle < -720) dummyArcAngle += 360;
				// if(dummyArcAngle > 720) dummyArcAngle -= 360;
				// if((dummyArcAngle < -360)||(dummyArcAngle > 360)) m_arcAngle
				// = 360;
				// else m_arcAngle = dummyArcAngle;
			}
		} else
		{
			tx.transform(m_start.getM_point(), m_start.getM_point());
			if(m_start!=m_end)
				tx.transform(m_end.getM_point(), m_end.getM_point());
			m_startAngle += -angleChange;
			if (m_startAngle < -360)
				m_startAngle += 360;
			if (m_startAngle > 360)
				m_startAngle -= 360;
		}
		// System.out.println("START: "+m_startAngle);
		// System.out.println("ARC: "+m_arcAngle);
		// System.out.println("DUMMY ARC: "+dummyArcAngle);
	}

	private void movePtCenterFree(ImpPoint ap, Point2D pt)
	{
		
		//6-5-2008
		//Removed the commented code below and added 4 lines.
		/*		if (ap.equals(m_start))
				{
					if ((ap.distance(m_end) < Constraint.MIN_ALLOWED_CONNECT_GAP) || (ap.distance(m_center) < Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						if ((Math.abs(dummyArcAngle) > 180) || (Math.abs(dummyArcAngle) < 1))
						{
							// // circle is complete so just move the whole circle
							// Point2D newPt = (Point2D)ap.getM_point().clone();
							// // revert back the ap to the old point pt
							// ap.getM_point().setLocation(pt);
							// move(pt, newPt);
							return;
						}
					}
				}
				else if (ap.equals(m_end))
				{
					if ((ap.distance(m_start) < Constraint.MIN_ALLOWED_CONNECT_GAP) || (ap.distance(m_center) < Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						if ((Math.abs(dummyArcAngle) > 180) || (Math.abs(dummyArcAngle) < 1))
						{
							// // circle is complete so just move the whole circle
							// Point2D newPt = (Point2D)ap.getM_point().clone();
							// // revert back the ap to the old point pt
							// ap.getM_point().setLocation(pt);
							// move(pt, newPt);
							return;
						}
					}
				}*/
				
		//If the start and the end points are the same, just recalculate the radius. Nothing else needs to be done.
		//if(m_start==m_end && ap.equals(m_start))
		//Removed above line and added the following line on 11-5-2008
		//When two points were at the same location, it created problem in transforming them.
		//So, I added the following chec.
		if(m_start.distance(m_end) < 1)
		{
			m_radius = m_start.distance(m_center);
			return;
		}
		
		
		
		
		
		
		Point2D newStartPt = new Point2D.Double();
		Point2D newEndPt = new Point2D.Double();
		Point2D newPt = new Point2D.Double();
		Point2D newCenter = new Point2D.Double();
		double startAngle = m_startAngle;
		double endAngle = dummyArcAngle + m_startAngle;
		double newStartAngle = 0;
		double newEndAngle = 0;
		double newRad = m_radius;

		// starting point moved/ adjust the arc
		double distCE = m_center.distance(pt);

		// calculate the avg radius
		newRad = (distCE + m_radius) / 2;

		// move the center
		// translate these points by placing center at origin
		AffineTransform tx = new AffineTransform();
		tx.setToTranslation(-m_center.getX(), -m_center.getY());
		tx.transform(m_start.getM_point(), newStartPt);
		tx.transform(m_end.getM_point(), newEndPt);
		tx.transform(pt, newPt);

		Point2D origin = new Point2D.Double(0, 0);

		Vector centers = null;
		Line2D seLine = null;

		// System.out.println(m_start+" "+m_end+" "+m_center+" "+ap);
		if (m_start.equals(ap))
		{
			centers = GMethods.CircleCircleIntersections(newEndPt.getX(), newEndPt.getY(), newRad,
					newPt.getX(), newPt.getY(), newRad);
			seLine = new Line2D.Double(newEndPt, newPt);
		} else if (m_end.equals(ap))
		{
			centers = GMethods.CircleCircleIntersections(newStartPt.getX(), newStartPt.getY(),
					newRad, newPt.getX(), newPt.getY(), newRad);
			seLine = new Line2D.Double(newStartPt, newPt);
		}

		if (centers != null)
		{
			Point2D center1 = (Point2D) centers.get(0);
			Point2D center2 = (Point2D) centers.get(1);
			if (seLine.relativeCCW(center1) == seLine.relativeCCW(origin))
			{
				newCenter = center1;
			} else if (seLine.relativeCCW(center2) == seLine.relativeCCW(origin))
			{
				newCenter = center2;
			} else
			{
				newCenter = center1;
			}

			// transform the points to new center
			tx.setToTranslation(-newCenter.getX(), -newCenter.getY());
			tx.transform(newStartPt, newStartPt);
			tx.transform(newEndPt, newEndPt);
			tx.transform(newPt, newPt);

			newStartAngle = Math.toDegrees(Maths.angle(-newStartPt.getY(), newStartPt.getX()));
			newEndAngle = Math.toDegrees(Maths.angle(-newEndPt.getY(), newEndPt.getX()));

			if ((Double.isNaN(newStartAngle) || (Double.isNaN(newEndAngle))
					|| (Double.isInfinite(newStartAngle)) || (Double.isInfinite(newEndAngle))))
			{
				// circle is complete so just move the whole circle
				Point2D tempPt = (Point2D) ap.getM_point().clone();
				// revert back the ap to the old point pt
				ap.getM_point().setLocation(pt);
				return;
			}
			// if(newStartAngle < 0) newStartAngle += 360;
			// if(newEndAngle < 0) newEndAngle += 360;
			// if(newStartAngle > 360) newStartAngle -= 360;
			// if(newEndAngle > 360) newEndAngle -= 360;

			// transform the points back from new center
			tx.setToTranslation(newCenter.getX(), newCenter.getY());
			tx.transform(newStartPt, newStartPt);
			tx.transform(newEndPt, newEndPt);
			tx.transform(newPt, newPt);

			tx.setToTranslation(m_center.getX(), m_center.getY());
			tx.transform(newStartPt, newStartPt);
			tx.transform(newEndPt, newEndPt);
			tx.transform(newPt, newPt);
			tx.transform(newCenter, newCenter);

			m_center.setM_point(newCenter);
			// cenAP.getM_point().setLocation(m_center);
			m_radius = newRad;
			m_startAngle = newStartAngle;
			if (m_startAngle < -360)
				m_startAngle += 360;
			if (m_startAngle > 360)
				m_startAngle -= 360;
			if (dummyArcAngle >= 0)
			{
				// antiClockwise
				if (dummyArcAngle <= 360)
				{
					if (newEndAngle <= newStartAngle)
					{
						dummyArcAngle = newEndAngle + (360 - newStartAngle);
					} else
					{
						dummyArcAngle = newEndAngle - newStartAngle;
					}

				}
			} else
			{
				// clockwise
				if (newEndAngle <= newStartAngle)
				{
					dummyArcAngle = newStartAngle - newEndAngle;
				} else
				{
					dummyArcAngle = newStartAngle + 360 - newEndAngle;
				}
				dummyArcAngle = -dummyArcAngle;
			}
			setM_arcAngle(dummyArcAngle);
			// if(dummyArcAngle < -720) dummyArcAngle += 360;
			// if(dummyArcAngle > 720) dummyArcAngle -= 360;
			// if((dummyArcAngle < -360)||(dummyArcAngle > 360)) m_arcAngle =
			// 360;
			// else m_arcAngle = dummyArcAngle;
		}
	}


	public void move(int x1, int y1, int x2, int y2)
	{
		// System.out.println("SegCircleCurve.move()");
		boolean canMove = true;
		Iterator iter = m_impPoints.iterator();
		while (iter.hasNext())
		{
			ImpPoint ip = (ImpPoint) iter.next();
			if ((!ip.equals(m_center)) && ip.isFixed())
			{
				canMove = false;
				break;
			}
		}

		if (canMove)
		{
			if (!m_center.isFixed())
				moveWholeSeg(x1, y1, x2, y2);
			else
				moveWithCenterFixed(x1, y1, x2, y2);
/*			beautifyForAngleHelper(  m_start.getM_point(),  m_center.getM_point());
			beautifyForAngleHelper(  m_end.getM_point(),  m_center.getM_point());*/
		} 
		else
		{
			// either or both of the end points are fixed
			if ((m_start.isFixed()) && (!m_end.isFixed()) && (!m_center.isFixed()))
			{	// check the angle, and rotate accordin to the change in angle
				// if required.
				moveWithOneEndPointFixed(m_start, m_end, x1, y1, x2, y2);
//				beautifyForAngleHelper(  m_end.getM_point(),  m_center.getM_point());
			}
			else if ((!m_start.isFixed()) && (m_end.isFixed()) && (!m_center.isFixed()))
			{
				// check the angle, and rotate accordin to the change in angle
				// if required.
				moveWithOneEndPointFixed(m_end, m_start, x1, y1, x2, y2);
//				beautifyForAngleHelper(  m_start.getM_point(),  m_center.getM_point());
			}
			else if ((m_start.isFixed()) && (m_end.isFixed()) && (!m_center.isFixed()))
				// both the end points are fixed.
				// moveWithEndPtFixed(new Point2D.Double(x2, y2));
				;
		}	
		if (!   ((m_start.isFixed()) && (m_end.isFixed())    &&   (!m_center.isFixed()))  )
		{
			Arc2D arc = (Arc2D) m_shape;
			arc.setArc((m_center.getX() - m_radius), (m_center.getY() - m_radius), (2 * m_radius),
				(2 * m_radius), m_startAngle, m_arcAngle, Arc2D.OPEN);
			setDetails();
		}
		
		updateConstarints();
	}

	private void moveWithOneEndPointFixed(AnchorPoint fixedPt, AnchorPoint freePt, int x1, int y1,
			int x2, int y2)
	{
		double angle1 = Maths.angle(fixedPt.getX(), fixedPt.getY(), x1, y1);
		double angle2 = Maths.angle(fixedPt.getX(), fixedPt.getY(), x2, y2);
		AffineTransform tx = new AffineTransform();
		tx.setToRotation(angle2 - angle1, fixedPt.getX(), fixedPt.getY());
		tx.transform(m_center.getM_point(), m_center.getM_point());
		tx.transform(freePt.getM_point(), freePt.getM_point());
		m_startAngle = m_startAngle + Math.toDegrees(angle1 - angle2);
		if (m_startAngle < -360)
			m_startAngle += 360;
		if (m_startAngle > 360)
			m_startAngle -= 360;
	}

	private void moveWholeSeg(int x1, int y1, int x2, int y2)
	{
		AffineTransform tx = new AffineTransform();
		tx.setToTranslation(x2 - x1, y2 - y1);
		tx.transform(m_start.getM_point(), m_start.getM_point());
		if(m_start!=m_end)
			tx.transform(m_end.getM_point(), m_end.getM_point());
		tx.transform(m_center.getM_point(), m_center.getM_point());
		// cenAP.getM_point().setLocation(m_center);
	}

	private void moveWithCenterFixed(int x1, int y1, int x2, int y2)
	{
		// expand the arc
		// calculate the new radius
		double oldRad = m_radius;
		m_radius = m_center.distance(x2, y2);
		if (oldRad >= m_radius)
		{
			// interpolate the end points of the arc
			double x = (m_start.getX() * m_radius + (oldRad - m_radius) * m_center.getX()) / oldRad;
			double y = (m_start.getY() * m_radius + (oldRad - m_radius) * m_center.getY()) / oldRad;
			m_start.getM_point().setLocation(x, y);
			if(m_start!=m_end)
			{
				x = (m_end.getX() * m_radius + (oldRad - m_radius) * m_center.getX()) / oldRad;
				y = (m_end.getY() * m_radius + (oldRad - m_radius) * m_center.getY()) / oldRad;
				m_end.getM_point().setLocation(x, y);
			}
		} else
		{
			// extrapolate the points
			double x = (m_radius * m_start.getX() - m_center.getX() * (m_radius - oldRad)) / oldRad;
			double y = (m_radius * m_start.getY() - m_center.getY() * (m_radius - oldRad)) / oldRad;
			m_start.getM_point().setLocation(x, y);
			if(m_start!=m_end)
			{
				x = (m_radius * m_end.getX() - m_center.getX() * (m_radius - oldRad)) / oldRad;
				y = (m_radius * m_end.getY() - m_center.getY() * (m_radius - oldRad)) / oldRad;
				m_end.getM_point().setLocation(x, y);
			}
		}

		// check the angle, and rotate accordin to the change in angle if
		// required.
		double angle1 = Maths.angle(m_center.getX(), m_center.getY(), x1, y1);
		double angle2 = Maths.angle(m_center.getX(), m_center.getY(), x2, y2);
		AffineTransform tx = new AffineTransform();
		tx.setToRotation(angle2 - angle1, m_center.getX(), m_center.getY());
		tx.transform(m_start.getM_point(), m_start.getM_point());
		if(m_start!=m_end)
			tx.transform(m_end.getM_point(), m_end.getM_point());
		m_startAngle = m_startAngle + Math.toDegrees(angle1 - angle2);
		if (m_startAngle < -360)
			m_startAngle += 360;
		if (m_startAngle > 360)
			m_startAngle -= 360;
		// System.out.println(m_startAngle);
	}

	private void setDetails()
	{
		setM_length((2 * Math.PI * getM_arcAngle() * getM_radius()) / 360);
	}

	private double getCurveAngle(double thisAngle, double nextAngle)
	{
		double diff = nextAngle - thisAngle;
		if (diff > 180)
		{
			diff = nextAngle - 360 - thisAngle;
		}
		if (diff < -180)
		{
			diff = 360 - thisAngle + nextAngle;
		}
		return diff;
	}

	public double getM_arcAngle()
	{
		return m_arcAngle;
	}

	public void setM_arcAngle(double angle)
	{
		dummyArcAngle = angle;// Math.toDegrees(angle1-angle2);
		if (dummyArcAngle < -720)
			dummyArcAngle += 360;
		if (dummyArcAngle > 720)
			dummyArcAngle -= 360;
		if (dummyArcAngle < -360)
			m_arcAngle = -360;
		else if (dummyArcAngle > 360)
			m_arcAngle = 360;
		else
			m_arcAngle = dummyArcAngle;
		// m_arcAngle = angle;
		// dummyArcAngle = m_arcAngle;
	}

	public double getM_radius()
	{
		return m_radius;
	}

	public Vector intersects(Segment seg)
	{
		// System.out.println("------------SegCircleCurve.intersects()");
		Vector intersects = new Vector();
		if (seg instanceof SegPoint)
		{
			// System.out.println("Curve and Point");
			// find the intersection of a point with the Circular curve. Check
			// if point lies on the circular curve
			// check the distance from the radius and check the angle(to check
			// if the point is on the curve)
			SegPoint segPt = (SegPoint) seg;
			Point2D pt = ((Line2D) segPt.getM_shape()).getP1();

			// do processing only of m_shape contains the point
			if (m_shape.contains(pt))
			{
				double dist = m_center.distance(pt);
				if (Math.abs(dist - m_radius) < Constraint.MIN_ALLOWED_CONNECT_GAP)
				{
					// find if its inside the angular extent
					if ((containsPt(pt)) && (seg.containsPt(pt)))
					{
						intersects.add(pt);
						System.out.println("Intersection Point: " + pt);
					}
				}
			}
		} else if (seg instanceof SegLine)
		{
			// System.out.println("Curve and Line");
			// finds the intersection point between a line and a Circular arc
			SegLine segLine = (SegLine) seg;
			Line2D line = (Line2D) segLine.getM_shape();
			// if((m_shape.contains(line.getP1())) ||
			// (m_shape.contains(line.getP2())))
			if (line.intersects(m_shape.getBounds2D()))
			{
				// find the distance of center of the arc from the line. If its
				// < radius then there is a possibility of an intersection
				// point.
				double dist = line.ptSegDist(m_center.getM_point());
				if (dist < (m_radius + Constraint.MIN_ALLOWED_CONNECT_GAP))
				{
					Vector intersections = lineCircleIntersections((SegLine) seg);
					if (intersections != null)
					{
						Point2D inter_p1 = (Point2D) intersections.get(0);
						Point2D inter_p2 = (Point2D) intersections.get(1);

//						System.out.println("Circle Intersection Points Found at: " + inter_p1
	//							+ ", " + inter_p2);
						// find if its inside the angular extent
						if ((containsPt(inter_p1)) && (seg.containsPt(inter_p1)))
						{
							intersects.add(inter_p1);
							// System.out.println("Point Selected: "+inter_p1);
						}

						if ((containsPt(inter_p2)) && (seg.containsPt(inter_p2)))
						{
							intersects.add(inter_p2);
							// System.out.println("Point Selected: "+inter_p2);
						}
					}
				}
			}
		} else if (seg instanceof SegCircleCurve)
		{
			// System.out.println("Circle and Circle");
			Vector intersections = cCurveIntersections((SegCircleCurve) seg);
			if (intersections != null)
			{
				Point2D inter_p1 = (Point2D) intersections.get(0);
				Point2D inter_p2 = (Point2D) intersections.get(1);

				// System.out.println("Circle Intersection Points Found at:
				// "+inter_p1+", "+inter_p2);
				// find if its inside the angular extent
				if ((containsPt(inter_p1)) && (seg.containsPt(inter_p1)))
				{
					intersects.add(inter_p1);
					// System.out.println("Point Selected : "+inter_p1);
				}

				if ((containsPt(inter_p2)) && (seg.containsPt(inter_p2)))
				{
					intersects.add(inter_p2);
					// System.out.println("Point Selected: "+inter_p2);
				}
			}
		} else
		{
			seg.intersects(this);
		}
		return intersects;
	}

	private Vector cCurveIntersections(SegCircleCurve seg)
	{
		// find the intersection between the two circular arcs
		// finds the intersection point between a line and a Circular arc
		// System.out.println("Circular segment detected");
		SegCircleCurve segCCurve = (SegCircleCurve) seg;
		Arc2D arc = (Arc2D) segCCurve.getM_shape();
		if ((arc.intersects(m_shape.getBounds2D())) || (arc.contains(m_shape.getBounds2D())))
		{
			// find the distance of center of the arc from the line. If its <
			// radius then there is a possibility of an intersection point.
			if ((segCCurve.getM_center().distance(m_center) <= (m_radius + segCCurve.getM_radius()))
					&& (segCCurve.getM_center().distance(m_center) >= Math.abs(m_radius
							- segCCurve.getM_radius())))
			{
				// curves MAY intersect. circles may be intersecting or one
				// circle is contained in the other
				double x0 = segCCurve.getM_center().getX();
				double y0 = segCCurve.getM_center().getY();
				double r0 = segCCurve.getM_radius();

				double x1 = m_center.getX();
				double y1 = m_center.getY();
				double r1 = m_radius;

				return GMethods.CircleCircleIntersections(x0, y0, r0, x1, y1, r1);
			}
		}
		return null;
	}

	public Vector touches(Segment seg)
	{
		Vector touches = new Vector();
		if (seg instanceof SegPoint)
		{
			SegPoint segPt = (SegPoint) seg;
			Point2D pt = ((Line2D) segPt.getM_shape()).getP1();

			Point2D interPt = getNearestPointOnSeg(pt);
			if ((interPt != null) && containsPt(interPt)
					&& (interPt.distance(pt) <= Constraint.MAX_ALLOWED_CONNECT_GAP))
			{
				touches.add(interPt);
				touches.add(pt);
			}
		}
		else if (seg instanceof SegLine)
		{
			Line2D line = (Line2D) seg.getM_shape();
			double lineCenterDist = line.ptSegDist(m_center.getM_point());
			double extra_dist = lineCenterDist - m_radius;
			Point2D pt = new Point2D.Double();
			Point2D interPt = new Point2D.Double();

			if ((extra_dist > 0) && (extra_dist < Constraint.MAX_ALLOWED_CONNECT_GAP))
			{
				// This could be a tangent constraint. get the point on the line
				pt = GMethods.nearestPointOnLineFromPoint(line, m_center.getM_point());

				// get the intersection point on curve
				interPt = GMethods.interpolate(m_center.getM_point(), pt, m_radius);

				// check for interPt closeness to the endpoints of the curve
				boolean insideSeg = false;
				if (m_start.distance(interPt) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					// Intesection point is very close to the start point of the curve
					interPt.setLocation(m_start.getM_point());
					insideSeg = true;
				}
				else if (m_end.distance(interPt) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					// Intesection point is very close to the end point of the curve
					interPt.setLocation(m_end.getM_point());
					insideSeg = true;
				}
				else if (containsPt(interPt))
					//Curve actually contains the point
					insideSeg = true;

				// Check if pt is close to the end points of the line
				if (insideSeg)
				{
					if (pt.distance(line.getP1()) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						//Intersection point is very close to the start point of the line
						pt.setLocation(line.getP1());
					else if (pt.distance(line.getP2()) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						//Intersection point is very close to the end point of the line
						pt.setLocation(line.getP2());

					// Add the point as a touch point
					touches.add(interPt);
					touches.add(pt);
				}
			}
			else
			{
				// the end points of the line are inside circular curve
				// NOTE: THE INTERSECTION ARE TO BE IGNORED
				Vector intersections = lineCircleIntersections((SegLine) seg);
				if (intersections != null)
				{
					Point2D inter_p1 = (Point2D) intersections.get(0);
					Point2D inter_p2 = (Point2D) intersections.get(1);

					// System.out.println("Circle Intersection Points Found at:
					// "+inter_p1+", "+inter_p2);
					// find if its inside the angular extent
					if ((!containsPt(inter_p1)) && (seg.containsPt(inter_p1)))
					{
						// System.out.println("######2222###");
						// this segment constaints this intersection point but
						// the line does not
						// check if the end points of the line are close to the
						// intersection point
						if (m_start.distance(inter_p1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("#####7777####");
							touches.add(m_start.getM_point());
							touches.add(inter_p1);
						} else if (m_end.distance(inter_p1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("#####8888####");
							touches.add(m_end.getM_point());
							touches.add(inter_p1);
						}
					} else if ((containsPt(inter_p1)) && (!seg.containsPt(inter_p1)))
					{
						// System.out.println("####11111#####");
						// this segment constaints this intersection point but
						// the line does not
						// check if the end points of the line are close to the
						// intersection point
						if (line.getP1().distance(inter_p1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("###5555######");
							touches.add(inter_p1);
							touches.add(line.getP1());
						} else if (line.getP2().distance(inter_p1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("######6666###");
							touches.add(inter_p1);
							touches.add(line.getP2());
						}
					}

					// find if its inside the angular extent
					if ((!containsPt(inter_p2)) && (seg.containsPt(inter_p2)))
					{
						// System.out.println("######4444###");
						// this segment constaints this intersection point but
						// the line does not
						// check if the end points of the line are close to the
						// intersection point
						if (m_start.distance(inter_p2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("#####tttt####");
							touches.add(m_start.getM_point());
							touches.add(inter_p2);
						} else if (m_end.distance(inter_p2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("####rrrr#####");
							touches.add(m_end.getM_point());
							touches.add(inter_p2);
						}
					} else if ((containsPt(inter_p2)) && (!seg.containsPt(inter_p2)))
					{
						// System.out.println("#######3333##");
						// this segment constaints this intersection point but
						// the line does not
						// check if the end points of the line are close to the
						// intersection point
						if (line.getP1().distance(inter_p2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("#####9999####");
							touches.add(inter_p2);
							touches.add(line.getP1());
						} else if (line.getP2().distance(inter_p2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
						{
							// System.out.println("######yyyy###");
							touches.add(inter_p2);
							touches.add(line.getP2());
						}
					}
				}
			}

		} else if (seg instanceof SegCircleCurve)
		{
			SegCircleCurve segCCurve = (SegCircleCurve) seg;

			// get the distance between the 2 centers
			double radDist = segCCurve.getM_center().distance(m_center);
			boolean s1Done = false;
			boolean e1Done = false;
			boolean s2Done = false;
			boolean e2Done = false;

			// check for possible external touching of the 2 arcs
			Point2D nearPt_1 = new Point2D.Double();
			Point2D nearPt_2 = new Point2D.Double();
			boolean isSegNear = false;

			if ((radDist <= (m_radius + segCCurve.getM_radius())
					+ Constraint.MAX_ALLOWED_CONNECT_GAP)
					&& (radDist > (m_radius + segCCurve.getM_radius())))
			{
				// there might be an external touching.. for outer tangent
				isSegNear = true;

				// find the point on the first curve
				nearPt_1 = GMethods.interpolate(m_center.getM_point(), segCCurve.getM_center()
						.getM_point(), m_radius);

				// find the point on the sec curve
				nearPt_2 = GMethods.interpolate(segCCurve.getM_center().getM_point(), m_center
						.getM_point(), segCCurve.getM_radius());
			}
			// check for possible internal touching of the 2 arcs
			else if ((radDist < Math.abs(m_radius - segCCurve.getM_radius()))
					&& (radDist >= (Math.abs(m_radius - segCCurve.getM_radius()) - Constraint.MAX_ALLOWED_CONNECT_GAP)))
			{
				// there might be an internal touching.. for inner tangent
				isSegNear = true;

				if (m_radius <= segCCurve.m_radius)
				{
					// this segment's radius is smaller than the other segment
					nearPt_1 = GMethods.extrapolate(segCCurve.getM_center().getM_point(), m_center
							.getM_point(), m_radius);

					nearPt_1 = GMethods.extrapolate(segCCurve.getM_center().getM_point(), m_center
							.getM_point(), segCCurve.getM_radius() - radDist);
				} else
				{
					// other segment's radius is smaller than this segment
					nearPt_2 = GMethods.extrapolate(m_center.getM_point(), segCCurve.getM_center()
							.getM_point(), segCCurve.getM_radius());

					nearPt_1 = GMethods.extrapolate(m_center.getM_point(), segCCurve.getM_center()
							.getM_point(), m_radius - radDist);
				}
			}

			// segments were found near
			if (isSegNear)
			{
				// check if those near points are actually on the curves or near
				// their end points
				if (m_start.distance(nearPt_1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					nearPt_1.setLocation(m_start.getM_point());
				} else if (m_end.distance(nearPt_1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					nearPt_1.setLocation(m_end.getM_point());
				}

				if (segCCurve.getM_start().distance(nearPt_2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					nearPt_2.setLocation(segCCurve.getM_start().getM_point());
				} else if (segCCurve.getM_end().distance(nearPt_2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					nearPt_2.setLocation(segCCurve.getM_end().getM_point());
				}

				if ((containsPt(nearPt_1)) && (segCCurve.containsPt(nearPt_2)))
				{
					touches.add(nearPt_1);
					touches.add(nearPt_2);
					// System.out.println("Points: "+nearPt_2+", "+nearPt_1);
				}
			} else
			{
				// segments were not near therefore no tangent.. check for
				// closeness of the end points to the circular arc
				// check for all the end points of the segment
				if (m_start.distance(segCCurve.m_start) < Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					// System.out.println("1111");
					touches.add(m_start.getM_point());
					touches.add(segCCurve.m_start.getM_point());
					s1Done = true;
					s2Done = true;
				} else if (m_start.distance(segCCurve.m_end) < Constraint.MAX_ALLOWED_CONNECT_GAP)
				{
					// System.out.println("2222");
					touches.add(m_start.getM_point());
					touches.add(segCCurve.m_end.getM_point());
					s1Done = true;
					e2Done = true;
				} else
				{
					nearPt_2 = segCCurve.getNearestPointOnSeg(m_start.getM_point());
					if ((nearPt_2 != null)
							&& (m_start.distance(nearPt_2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
							&& (m_start.distance(nearPt_2) > Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						// System.out.println("3333");
						touches.add(m_start.getM_point());
						touches.add(nearPt_2);
						s1Done = true;
					}
				}

				if ((!s2Done)
						&& (m_end.distance(segCCurve.m_start) < Constraint.MAX_ALLOWED_CONNECT_GAP))
				{
					// System.out.println("4444");
					touches.add(m_end.getM_point());
					touches.add(segCCurve.m_start.getM_point());
					e1Done = true;
					s2Done = true;
				} else if ((!e2Done)
						&& (m_end.distance(segCCurve.m_end) < Constraint.MAX_ALLOWED_CONNECT_GAP))
				{
					// System.out.println("5555");
					touches.add(m_end.getM_point());
					touches.add(segCCurve.m_end.getM_point());
					e1Done = true;
					e2Done = true;
				} else
				{
					nearPt_2 = segCCurve.getNearestPointOnSeg(m_end.getM_point());
					if ((nearPt_2 != null)
							&& (m_end.distance(nearPt_2) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
							&& (m_end.distance(nearPt_2) > Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						// System.out.println("6666");
						touches.add(m_end.getM_point());
						touches.add(nearPt_2);
						e1Done = true;
					}
				}

				if (!s2Done)
				{
					nearPt_1 = getNearestPointOnSeg(segCCurve.m_start.getM_point());
					if ((nearPt_1 != null)
							&& (segCCurve.m_start.distance(nearPt_1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
							&& (segCCurve.m_start.distance(nearPt_1) > Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						// System.out.println("7777");
						touches.add(nearPt_1);
						touches.add(segCCurve.m_start.getM_point());
						s2Done = true;
					}
				}
				// check for end point of seg2
				if (!e2Done)
				{
					nearPt_1 = getNearestPointOnSeg(segCCurve.m_end.getM_point());
					if ((nearPt_1 != null)
							&& (segCCurve.m_end.distance(nearPt_1) <= Constraint.MAX_ALLOWED_CONNECT_GAP)
							&& (segCCurve.m_end.distance(nearPt_1) <= Constraint.MIN_ALLOWED_CONNECT_GAP))
					{
						// System.out.println("8888");
						touches.add(nearPt_1);
						touches.add(segCCurve.m_end.getM_point());
						e2Done = true;
					}
				}
			}
		}
		// this is any other segment
		else
		{
			touches.addAll(reverseRelOrder(seg.touches(this)));
		}
		// TODO Auto-generated method stub
		return touches;
	}

	public Point2D getNearestPointOnSeg(Point2D pt)
	{
		// System.out.println("SegCircleCurve.getNearestPointOnSeg()");
		// none of hte end points are near the point
		double dist = m_center.distance(pt);
		double extra_dist = dist - m_radius;
		// find the intersection of the line joining the SegPoint and the center
		// of the circle
		// and the circular curve
		Point2D interPt = new Point2D.Double(0, 0);
		if (extra_dist >= 0)
		{
			interPt = GMethods.interpolate(m_center.getM_point(), pt, m_radius);
			//			
			// interPt.x = (pt.getX()*m_radius + m_center.getX()*extra_dist) /
			// (extra_dist+m_radius);
			// interPt.y = (pt.getY()*m_radius + m_center.getY()*extra_dist) /
			// (extra_dist+m_radius);
		} else
		{
			interPt = GMethods.extrapolate(m_center.getM_point(), pt, Math.abs(extra_dist));
			// note extra dist in negative
			// interPt.x = (pt.getX()*m_radius - m_center.getX()*(-extra_dist))
			// / (m_radius+extra_dist);
			// interPt.y = (pt.getY()*m_radius - m_center.getY()*(-extra_dist))
			// / (m_radius+extra_dist);
		}

		// do processing only of m_shape contains the point
		double p1Dist = m_start.distance(pt);
		double p2Dist = m_end.distance(pt);

		if (containsPt(interPt))
		{
			// both end points of line 1 are near p1 of line2, find whichone is
			// closer
			if (p1Dist <= p2Dist)
			{
				// P1 point of line 1 touches P1 of line 2
				if (p1Dist < Constraint.MAX_ALLOWED_CONNECT_GAP)
					return m_start.getM_point();
			} else
			{
				// P2 point of line 1 touches P1 of line 2
				if (p2Dist < Constraint.MAX_ALLOWED_CONNECT_GAP)
					return m_end.getM_point();
			}
			return interPt;
		} else
		{
			// both end points of line 1 are near p1 of line2, find whichone is
			// closer
			if (p1Dist <= p2Dist)
			{
				return m_start.getM_point();
			} else
			{
				return m_end.getM_point();
			}
		}
	}
	
	public boolean containsPt(Point2D pt)
	{
		return containsPt(pt.getX(),pt.getY());
	}
	public boolean containsPt(double x, double y)
	{
		Point2D pt = new Point2D.Double(x, y);
		
		if(m_start.containsPt(pt) || m_end.containsPt(pt))
			return true;
		
		// do processing only of m_shape contains the point
		double dist = m_center.distance(pt);
		double extra_dist = dist - m_radius;
		// check if the point is NEAR the Curve and not ON the line.
		if (Math.abs(extra_dist) <= Constraint.MIN_ALLOWED_CONNECT_GAP)
		{// TODO Auto-generated method stub
		// System.out.println("******************************");
		// System.out.println("Start Angle: "+m_startAngle);
		// System.out.println("Arc Angle: "+m_arcAngle);
		// System.out.println("Dummy Arc: "+dummyArcAngle);
			double angle = -Maths.angleInDegrees(m_center.getM_point(), pt);
			// System.out.println("Angle: "+angle);
			if (angle < 0)
				angle += 360;
			// System.out.println("Angle: "+angle);
			if (Math.abs(m_arcAngle) >= 360)
			{
				return true;
			} else
			{
				// rotate the angle equal to the deg of startangle
				double newAngle = angle - m_startAngle;
				if (m_startAngle < 0)
					newAngle -= 360;

				// check if the newAngle is within the arcAngle range
				if (m_arcAngle < 0)
				{
					if (newAngle > 0)
						newAngle -= 360;
					if (m_arcAngle <= newAngle)
					{
						return true;
					}
				} else
				{
					if (newAngle < 0)
						newAngle += 360;
					if (m_arcAngle >= newAngle)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	private Vector lineCircleIntersections(SegLine segLine)
	{
		Line2D line = (Line2D) segLine.getM_shape();
		// there could be 0, 1 or 2 intersections.
		// calculate the slope of the line
		double m = Maths.slope(line.getP1(), line.getP2());

		// calculate the line intercept with y axis
		double intercept = line.getY1() - (m * line.getX1());

		// find the point of intersection of line and the CIRCLE (not the curve
		// for the moment)
		double a = 1 + m * m;
		double b = (-2) * m_center.getX() + 2 * m * (intercept - m_center.getY());
		double c = m_center.getX() * m_center.getX() + (intercept - m_center.getY())
				* (intercept - m_center.getY()) - m_radius * m_radius;

		double disc = Math.sqrt(b * b - 4 * a * c);
		if (disc >= 0)
		{
			double inter_x1 = ((-1) * b + disc) / (2 * a);
			double inter_y1 = m * inter_x1 + intercept;
			double inter_x2 = ((-1) * b - disc) / (2 * a);
			double inter_y2 = m * inter_x2 + intercept;
			Point2D.Double inter_p1 = new Point2D.Double(inter_x1, inter_y1);
			Point2D.Double inter_p2 = new Point2D.Double(inter_x2, inter_y2);

//			System.out.println("$$$$$$$$$$$ THE INTERSECTION POINTS FOR CIRCLE AND A LINE "+inter_p1+", "+inter_p2);
			Vector intersections = new Vector();
			intersections.add(inter_p1);
			intersections.add(inter_p2);
			return intersections;
		}
		return null;
	}

	public AnchorPoint getM_center()
	{
		return m_center;
	}

	public AnchorPoint getM_end()
	{
		return m_end;
	}

	public AnchorPoint getM_start()
	{
		return m_start;
	}

	public void setM_center(AnchorPoint m_center)
	{
		this.m_center = m_center;
	}

	public void setM_end(AnchorPoint m_end)
	{
		this.m_end = m_end;
	}

	public void setM_start(AnchorPoint m_start)
	{
		this.m_start = m_start;
	}

	public int getSegmentPt(AnchorPoint ip)
	{
		if (ip.equals(getM_start()))
		{
			return getM_rawStartIdx();
		} else if (ip.equals(getM_end()))
		{
			return getM_rawEndIdx();
		}
		return -1;
	}

	public AnchorPoint getSegEnd()
	{
		return getM_end();
	}

	public AnchorPoint getSegStart()
	{
		return getM_start();
	}

	public GeometryElement copy()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void changePoint(ImpPoint ip1, ImpPoint ip2)
	{
		super.changePoint(ip1, ip2);
		changePoint4Segment(ip1,ip2);
	}
	public void changePoint4Segment(ImpPoint ip1,ImpPoint ip2)
	{
		if (m_start == ip1)
			m_start = (AnchorPoint) ip2;
		if(m_end==ip1)
			m_end = (AnchorPoint) ip2;
		if(m_center == ip1)
			m_center = (AnchorPoint) ip2;
	}


	//added on 24-05-10
	
	
	
/*	public Point2D beautifyForAngleHelper(Point2D endPoint, Point2D centerPt)
	{
		double angle1 = Math.toDegrees(Maths.angle(endPoint,centerPt));
		double angle2=-1;
		if( -202.5<angle1 && angle1<-157.5) angle2=-180;
		else if (-157.5<angle1 && angle1<-112.5) angle2=-135;
		else if (-112.5<angle1 && angle1<-67.5) angle2=-90;
		else if(-67.5<angle1 && angle1<-22.5) angle2=-45;
		else if(-22.5<angle1 && angle1<22.5) angle2=0;
		else if(22.5<angle1 && angle1<67.5) angle2=45;
		else if(67.5<angle1 && angle1<112.5) angle2=90;
		else if(112.5<angle1 && angle1<157.5) angle2=135;
		else if (157.5<angle1 && angle1<202.5) angle2=180;
		else ;
		
		AffineTransform tx = new AffineTransform();
		if(angle2!=-1)
		{
			tx.setToRotation(Math.toRadians(angle2-angle1), centerPt.getX(), centerPt.getY());
			tx.transform(endPoint, endPoint);
		}
		return endPoint;
	}

	public void beautifyForAngle()
	{
		m_points.set(0,beautifyForAngleHelper(  (Point2D)m_points.get(0),  (Point2D)m_points.get(2)));
		m_points.set(1,beautifyForAngleHelper(  (Point2D)m_points.get(1),  (Point2D)m_points.get(2)));
	}
	
*/
	
/*	private void moveWithEndPtFixed(Point2D pt)
	{
		// find the center: where the perpendicular bisectors of the PT-END and
		// PT-START sides of the triangle meet
		Point2D newCenter = new Point2D.Double(
				(m_start.getX() + m_end.getX() + pt.getX()) / 3 + 0.5, (m_start.getY()
						+ m_end.getY() + pt.getY()) / 3 + 0.5);

		double newStartAngle = Math.toDegrees(Maths.angle(-m_start.getY() + newCenter.getY(),
				m_start.getX() - newCenter.getX()));
		double newEndAngle = Math.toDegrees(Maths.angle(-m_end.getY() + newCenter.getY(), m_end
				.getX()
				- newCenter.getX()));

		if (!((Double.isNaN(newStartAngle) || (Double.isNaN(newEndAngle))
				|| (Double.isInfinite(newStartAngle)) || (Double.isInfinite(newEndAngle)))))
		{
			m_center.getM_point().setLocation(newCenter);
			m_startAngle = newStartAngle;
			if (m_startAngle < -360)
				m_startAngle += 360;
			if (m_startAngle > 360)
				m_startAngle -= 360;
			if (m_arcAngle >= 0)
			{
				setM_arcAngle(newEndAngle - newStartAngle);
				if (m_arcAngle < 0)
					setM_arcAngle(m_arcAngle + 360);
			} else
			{
				setM_arcAngle(newEndAngle - newStartAngle);
				if (m_arcAngle > 0)
					setM_arcAngle(m_arcAngle - 360);
			}
			m_radius = pt.distance(newCenter);
		}
	}*/

	

}
