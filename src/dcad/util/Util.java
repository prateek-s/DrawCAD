package dcad.util;
import java.util.Vector;

import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.GeometryElement;
import dcad.model.geometry.ImpPoint;

public class Util
{

	public Util()
	{
	}

	public static int max = 1000;

	public static double distance(ImpPoint a, ImpPoint b)
	{
		return a.getM_point().distance(b.getM_point());
	}

/*	public static double distance(BaseLine l, AnchorPoint a)
	{

		double x1 = l.getX1();
		double x2 = l.getX2();
		double y1 = l.getY1();
		double y2 = l.getY2();

		double A = (y1 - y2);
		double B = (x2 - x1);
		double C = x1 * y2 - y1 * x2;
		double pX = a.getX();
		double pY = a.getY();
		return Math.abs(A * pX + B * pY + C) / (Math.sqrt(A * A + B * B));
	}
*/
/*	public static double slope(BaseLine l)
	{
		double x1 = l.getX1();
		double x2 = l.getX2();
		double y1 = l.getY1();
		double y2 = l.getY2();

		if ((x2 - x1) != 0)
			return (y2 - y1) / (x2 - x1);
		else
			return (y2 - y1) / 0.000000001;
	}
*/
	/*
	 * public double slope(Line l) { double x1 = l.getX1(); double x2 =
	 * l.getX2(); double y1 = l.getY1(); double y2 = l.getY2(); if((x2 - x1) !=
	 * 0 ) return (y2 - y1)/(x2 - x1); else return (y2 -y1) / 0.000000001; }
	 */
/*	public static AnchorPoint getClosePointOnLine(BaseLine l, AnchorPoint ct)
	{
		AnchorPoint pt = null;
		if (l instanceof Line)
			pt = ((Line) l).getP1();
		else
			pt = new AnchorPoint(l.getX1(), l.getY1());

		return getClosePointOnLine(pt, Util.slope(l), ct);
	}
*/
	public static AnchorPoint getClosePointOnLine(ImpPoint lt, double s, ImpPoint ct)
	{
		// lt : line through point
		// s : slope of line
		// ct : close to point

		// converting into slope and point form y = mx + c
		double m1 = s;
		double m2 = -1 / m1;
		double x, y;

		if (Math.abs(m1) > 1000)
		{
			x = lt.getX();
			y = ct.getY();

		}
		else if (Math.abs(m2) > 1000)
		{

			x = ct.getX();
			y = lt.getY();
		}
		else
		{
			double c1 = lt.getY() - m1 * lt.getX();
			double c2 = ct.getY() - m2 * ct.getX();

			x = (c2 - c1) / (m1 - m2);
			y = m1 * x + c1;
			// /////System.out.println("\n X and y value obtained in the Util : (
			// "+x+" , "+y+" )");
		}
		return new AnchorPoint(x, y);
	}

	public static double slope(ImpPoint a, ImpPoint b)
	{

		double x1 = a.getX();
		double x2 = b.getX();
		double y1 = a.getY();
		double y2 = b.getY();

		if (Math.abs(x2 - x1) != 0)
			return (y2 - y1) / (x2 - x1);
		else
			return 1000000.0;
	}

	public static GeometryElement circleLineIntersectionPoint(ImpPoint center, double radius, ImpPoint p1, ImpPoint p2)
	{
		// center of circle and radius
		// line passing thr p1, p2
		// point close to p2 is returned
		double u1, u2;

		double x1 = p1.getX();
		double x2 = p2.getX();
		double x3 = center.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		double y3 = center.getY();

		double a = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		double b = 2 * ((x2 - x1) * (x1 - x3) + (y2 - y1) * (y1 - y3));
		double c = x3 * x3 + y3 * y3 + x1 * x1 + y1 * y1 - 2 * (x3 * x1 + y3 * y1) - radius * radius;
		double delta = b * b - 4 * a * c;
		if (delta < 0)
		{
			return null;
		}
		else
		{
			u1 = (-b + Math.sqrt(delta)) / (2 * a);
			u2 = (-b - Math.sqrt(delta)) / (2 * a);
		}
		double xnew1 = x1 + u1 * (x2 - x1);
		double xnew2 = x1 + u2 * (x2 - x1);
		double ynew1 = y1 + u1 * (y2 - y1);
		double ynew2 = y1 + u2 * (y2 - y1);

		ImpPoint newpt1 = new AnchorPoint(xnew1, ynew1);
		ImpPoint newpt2 = new AnchorPoint(xnew2, ynew2);

		return distance(newpt2, p2) < distance(newpt1, p2) ? newpt2 : newpt1;

	}

	public static GeometryElement intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double ua = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		ua = ua / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

		double x = x1 + ua * (x2 - x1);
		double y = y1 + ua * (y2 - y1);

		GeometryElement bp = new AnchorPoint(x, y);
		return (bp);

	}

/*	public static AnchorPoint intersection(BaseLine l, AnchorPoint p1, AnchorPoint p2)
	{

		double x1 = l.getX1();
		double x2 = l.getX2();
		double y1 = l.getY1();
		double y2 = l.getY2();

		double x3 = p1.getX();
		double x4 = p2.getX();
		double y3 = p1.getY();
		double y4 = p2.getY();

		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);

	}
*/
/*	public static AnchorPoint intersection(BaseLine l1, BaseLine l2)
	{

		double x1 = l1.getX1();
		double x2 = l1.getX2();
		double y1 = l1.getY1();
		double y2 = l1.getY2();

		double x3 = l2.getX1();
		double x4 = l2.getX2();
		double y3 = l2.getY1();
		double y4 = l2.getY2();

		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);

	}
*/
	public static GeometryElement intersection(ImpPoint p3, ImpPoint p4, ImpPoint p1, ImpPoint p2)
	{

		double x1 = p3.getX();
		double x2 = p4.getX();
		double y1 = p3.getY();
		double y2 = p4.getY();

		double x3 = p1.getX();
		double x4 = p2.getX();
		double y3 = p1.getY();
		double y4 = p2.getY();
		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public static boolean cross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double ua = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		ua = ua / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

		double x = x1 + ua * (x2 - x1);
		double y = y1 + ua * (y2 - y1);

		long xi = Math.round(x);
		long x1i = Math.round(x1);
		long x2i = Math.round(x2);
		long yi = Math.round(y);
		long y1i = Math.round(y1);
		long y2i = Math.round(y2);

		long x3i = Math.round(x3);
		long x4i = Math.round(x4);

		long y3i = Math.round(y3);
		long y4i = Math.round(y4);

		/*
		 * ///System.out.println("\n x1 - x - x2 : "+x1i + " " +xi + " " +x2i);
		 * ///System.out.println("\n x3 - x - x4 : "+x3i + " " +xi + " " +x4i);
		 * ///System.out.println("\n y1 - y - y2 : "+y1i + " " +yi + " " +y2i);
		 * ///System.out.println("\n y3 - y - y4 : "+y3i + " " +yi + " " +y4i);
		 */
		// /////System.out.println("\nin cross ( "+x1+","+y1+") ("+x2+" , "+y2+")
		// ("+x+" , "+y +")");
		if (((xi <= x1i && xi >= x2i) || (xi >= x1i && xi <= x2i)) && ((yi <= y1i && yi >= y2i) || (yi >= y1i && yi <= y2i)) && (((xi <= x3i && xi >= x4i) || (xi >= x3i && xi <= x4i)) && ((yi <= y3i && yi >= y4i) || (yi >= y3i && yi <= y4i)))) // on
			// second
			// line
			return true;

		///System.out.println("\nCross failed :((");
		return (false);
	}

/*	public static boolean cross(BaseLine l1, BaseLine l2)
	{
		double x1 = l1.getX1();
		double x2 = l1.getX2();
		double y1 = l1.getY1();
		double y2 = l1.getY2();

		double x3 = l2.getX1();
		double x4 = l2.getX2();
		double y3 = l2.getY1();
		double y4 = l2.getY2();
		return cross(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public static boolean cross(BaseLine l, AnchorPoint p1, AnchorPoint p2)
	{

		double x1 = l.getX1();
		double x2 = l.getX2();
		double y1 = l.getY1();
		double y2 = l.getY2();

		double x3 = p1.getX();
		double x4 = p2.getX();
		double y3 = p1.getY();
		double y4 = p2.getY();

		return cross(x1, y1, x2, y2, x3, y3, x4, y4);
	}
*/
	public static boolean cross(ImpPoint p3, ImpPoint p4, ImpPoint p1, ImpPoint p2)
	{

		double x1 = p3.getX();
		double x2 = p4.getX();
		double y1 = p3.getY();
		double y2 = p4.getY();

		double x3 = p1.getX();
		double x4 = p2.getX();
		double y3 = p1.getY();
		double y4 = p2.getY();
		return cross(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	/*
	 * public double distanceFromLine(Line l, AnchorPoint p) { double dist = 1000;
	 * //max considered ... double pX = p.getX(); double pY = p.getY(); double
	 * x1 = l.getX1(); double x2 = l.getX2(); double y1 = l.getY1(); double y2 =
	 * l.getY2(); double A = (y1 - y2); double B = (x2 - x1); double C = x1*y2 -
	 * y1*x2; if(((x1 < x2 && x1 < pX && x2 > pX) || (x1 > x2 && x2 < pX && x1 >
	 * pX)) && ((y1 < y2 && y1 < pY && y2 > pY) || (y1 > y2 && y2 < pY && y1 >
	 * pY)) ) { dist = Math.abs((A*pX + B*pY + C)/(Math.sqrt(A*A + B*B)));
	 * /////System.out.println("DIstance : "+dist); } return dist; }
	 */
	public static int sign(double no)
	{
		return no < 0 ? -1 : 1;
	}

	public static boolean sameside(ImpPoint p, ImpPoint p1, ImpPoint p2)
	{
		if (sign(p1.getX() - p.getX()) == sign(p2.getX() - p.getX()) && sign(p1.getY() - p.getY()) == sign(p2.getY() - p.getY()))
			return true;

		return false;

	}

/*	public static double distanceFromLine(Line l, AnchorPoint p)
	{

		double dist = 1000; // max considered ...
		double pX = p.getX();
		double pY = p.getY();

		double x1 = l.getP1().getX();
		double x2 = l.getP2().getX();
		double y1 = l.getP1().getY();
		double y2 = l.getP2().getY();

		
		 * double x1 = l.getX1(); double x2 = l.getX2(); double y1 = l.getY1();
		 * double y2 = l.getY2();
		 
		double A = (y1 - y2);
		double B = (x2 - x1);
		double C = x1 * y2 - y1 * x2;

		dist = Math.abs((A * pX + B * pY + C) / (Math.sqrt(A * A + B * B)));

		// /////System.out.println("\n distanceFromLine( "+x1+","+y1+") ("+x2+" ,
		// "+y2+") ("+pX+" , "+pY +")");
		if (x1 == x2 && Math.abs(pX - x1) <= 3.0 && ((pY <= y1 && pY >= y2) || (pY <= y2 && pY >= y1)))
		{
			// /////System.out.println("DIstance : "+dist);

			return dist;
		}
		else if (y1 == y2 && Math.abs(pY - y1) <= 3.0 && ((pX <= x1 && pX >= x2) || (pX <= x2 && pX >= x1)))
		{
			// /////System.out.println("DIstance : "+dist);
			return dist;

		}
		else if (((x1 <= x2 && x1 <= pX && x2 >= pX) || (x1 >= x2 && x2 <= pX && x1 >= pX)) && ((y1 <= y2 && y1 <= pY && y2 >= pY) || (y1 >= y2 && y2 <= pY && y1 >= pY)))

		{
			// /////System.out.println("DIstance : "+dist);
			return dist;

		}
		return 1000;

	}
*/
/*	public static double distanceFromLine(BaseLine l, AnchorPoint p)
	{

		double dist = 1000; // max considered ...
		double pX = p.getX();
		double pY = p.getY();

		double x1 = l.getX1();
		double x2 = l.getX2();
		double y1 = l.getY1();
		double y2 = l.getY2();
		double A = (y1 - y2);
		double B = (x2 - x1);
		double C = x1 * y2 - y1 * x2;

		dist = Math.abs((A * pX + B * pY + C) / (Math.sqrt(A * A + B * B)));

		// /////System.out.println("\n distanceFromLine( "+x1+","+y1+") ("+x2+" ,
		// "+y2+") ("+pX+" , "+pY +")");
		if (x1 == x2 && Math.abs(pX - x1) <= 3.0 && ((pY <= y1 && pY >= y2) || (pY <= y2 && pY >= y1)))
		{
			// /////System.out.println("DIstance : "+dist);

			return dist;
		}
		else if (y1 == y2 && Math.abs(pY - y1) <= 3.0 && ((pX <= x1 && pX >= x2) || (pX <= x2 && pX >= x1)))
		{
			// /////System.out.println("DIstance : "+dist);
			return dist;

		}
		else if (((x1 <= x2 && x1 <= pX && x2 >= pX) || (x1 >= x2 && x2 <= pX && x1 >= pX)) && ((y1 <= y2 && y1 <= pY && y2 >= pY) || (y1 >= y2 && y2 <= pY && y1 >= pY)))

		{
			// /////System.out.println("DIstance : "+dist);
			return dist;

		}
		return 1000;

	}
*/
	public static double distanceFromLine(ImpPoint lp1, ImpPoint lp2, ImpPoint p)
	{

		double dist = 1000; // max considered ...
		double pX = p.getX();
		double pY = p.getY();

		double x1 = lp1.getX();
		double x2 = lp2.getX();
		double y1 = lp1.getY();
		double y2 = lp2.getY();

		double A = (y1 - y2);
		double B = (x2 - x1);
		double C = x1 * y2 - y1 * x2;

		dist = Math.abs((A * pX + B * pY + C) / (Math.sqrt(A * A + B * B)));

		// /////System.out.println("\n distanceFromLine( "+x1+","+y1+") ("+x2+" ,
		// "+y2+") ("+pX+" , "+pY +")");
		return dist;

	}

/*	public static AnchorPoint closePoint(BaseLine l, AnchorPoint p)
	{

		double x, y;
		AnchorPoint bp;
		if (slope(l) > 1000)
		{
			x = p.getX();
			y = l.getY1();
			bp = new AnchorPoint(x, y);
			return bp;
		}

		x = p.getX();
		y = slope(l) * (x - l.getX1()) + l.getY1();
		bp = new AnchorPoint(x, y);
		return bp;

	}
*/
	/*
	 * public static AnchorPoint closePoint(Line l, AnchorPoint p) { double x, y;
	 * AnchorPoint bp; if( slope(l) > 1000) { x = p.getX(); y = l.getY1(); bp =
	 * new AnchorPoint(x,y); return bp; } x = p.getX(); y = slope(l) * (x -
	 * l.getX1()) + l.getY1(); bp = new AnchorPoint(x,y); return bp; }
	 */
	public static GeometryElement midpoint(ImpPoint p1, ImpPoint p2)
	{
		double x1 = p1.getX();
		double x2 = p2.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		double x = (x1 + x2) / 2;
		double y = (y1 + y2) / 2;
		GeometryElement bp;
		bp = new AnchorPoint(x, y);
		return bp;

	}

	public static GeometryElement getCenter(ImpPoint pt1, ImpPoint pt2, ImpPoint pt3)
	{

		double mx, my;
		double yDelta_a = pt2.getY() - pt1.getY();
		double xDelta_a = pt2.getX() - pt1.getX();
		double yDelta_b = pt3.getY() - pt2.getY();
		double xDelta_b = pt3.getX() - pt2.getX();

		if (Math.abs(xDelta_a) <= 0.000000001 && Math.abs(yDelta_b) <= 0.000000001)
		{
			mx = 0.5 * (pt2.getX() + pt3.getX());
			my = 0.5 * (pt1.getY() + pt2.getY());
			GeometryElement c = new AnchorPoint(mx, my);
			return c;
		}

		// IsPerpendicular() assure that xDelta(s) are not zero
		double aSlope = slope(pt1, pt2); // 
		double bSlope = slope(pt2, pt3);
		// /////System.out.println("\n The points in get center are");
		// pt1.print();
		// pt2.print();
		// pt3.print();
		// ///System.out.println("\n slopes : aSlope "+aSlope+" bSlope "+bSlope);
		// double aSlope=yDelta_a/xDelta_a; //
		// double bSlope=yDelta_b/xDelta_b;
		if (Math.abs(aSlope - bSlope) <= 0.000000001)
		{ // checking whether the given points are colinear.
			// ///System.out.println("The three pts are colinear\n");
			return null;
		}

		mx = (aSlope * bSlope * (pt1.getY() - pt3.getY()) + bSlope * (pt1.getX() + pt2.getX()) - aSlope * (pt2.getX() + pt3.getX())) / (2 * (bSlope - aSlope));
		if (aSlope != 0)
			my = -1 * (mx - (pt1.getX() + pt2.getX()) / 2) / aSlope + (pt1.getY() + pt2.getY()) / 2;
		else
			my = -1 * (mx - (pt2.getX() + pt3.getX()) / 2) / bSlope + (pt2.getY() + pt3.getY()) / 2;
		GeometryElement c = new AnchorPoint(mx, my);
		return c;
	}

	public static double getAngleInLines(ImpPoint p1, ImpPoint p2, ImpPoint p3, ImpPoint p4)
	{

		double m1 = getAngle(p1, p2);
		double m2 = getAngle(p3, p4);

		return Math.toDegrees(Math.atan((m2 - m1) / (1 + m1 * m2)));

	}

	public static double getAngle(ImpPoint p1, ImpPoint p2)
	{

		double x1 = p1.getX();
		double x2 = p2.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		double deltax = x2 - x1;
		double deltay = y2 - y1;
		double ang = 0;

		if (deltax != 0)
		{
			ang = Math.toDegrees(Math.atan(deltay / deltax));
			// ///System.out.println("\natan o/p "+ang+" deltax "+deltax+" deltay
			// "+deltay);
		}
		else
			ang = deltay > 0 ? 90 : -90;

		if (deltay > 0)
		{
			if (deltax < 0)
			{
				ang = 180.0 + ang;
			}

		}
		else
		{
			if (deltax < 0)
			{
				ang = 180 + ang;
			}
			else
			{
				ang = 360 + ang;
			}

		}

		return (360 - ang);

	}

	public static double side(AnchorPoint p1, AnchorPoint p2, AnchorPoint p3)
	{

		// first end middle
		double x1 = p1.getX();
		double x2 = p2.getX();
		double x3 = p3.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		double y3 = p3.getY();
		double s = (y3 - y1) - (((y2 - y1) / (x2 - x1)) * (x3 - x1));
		// ///System.out.println("\n");
		p1.print();
		p2.print();
		p3.print();

		// ///System.out.println("value : "+s);
		return s;
	}

/*	public static boolean Online(AnchorPoint bp, BaseLine bl)
	{

		AnchorPoint b1 = new AnchorPoint(bl.getX1(), bl.getY1());

		if (slope(bl) == slope(b1, bp))
			return true;
		else
			return false;

	}
*/
	public static int getPointNoWithSmallestWeight(Vector pt)
	{
		// 0 signifies all points have heighest weight
		// return value = 1 signifies that pt(0) has smallest value
		int j = 0, i;
		int currmax = 1000;
		for (i = 0; i < pt.size(); i++)
		{
			if (((AnchorPoint) pt.get(i)).getWeight() == 0)
			{
				j = i;
				break;
			}
			else if (((AnchorPoint) pt.get(i)).getWeight() != 1000)
			{
				// maxWeight
				if (((AnchorPoint) pt.get(i)).getWeight() < currmax)
				{
					currmax = ((AnchorPoint) pt.get(i)).getWeight();
					j = i;
				}
			}

		}
		return j + 1;
	}

/*	public static void showss(StyleSheet styles, String sname)
	{
		///System.out.println("\n Stylesheet " + sname);
		Enumeration rules = styles.getStyleNames();
		while (rules.hasMoreElements())
		{
			String name = (String) rules.nextElement();
			Style rule = styles.getStyle(name);
			///System.out.println(rule.toString());
		}
	}

	public static StyleSheet makess()
	{
		StyleSheet css = new StyleSheet();

		css.addRule("nobr {white-space: nowrap; name: nobr;}");
		css.addRule("ol {list-style-type: decimal;margin-top: 10;name: ol;margin-right: 50;margin-bottom: 10;margin-left: 50;}");
		css.addRule("u {name: u;text-decoration: underline;}");
		css.addRule("s {name: s;text-decoration: line-through;}");
		css.addRule("p {margin-top: 15;name: p;}");
		css.addRule("dd p {margin-top: 0;name: dd p;margin-left: 0;margin-bottom: 0;}");
		css.addRule("ol li p {margin-top: 0;name: ol li p;margin-bottom: 0;}");
		css.addRule("address {color: blue;font-style: italic;name: address;}");
		css.addRule("i {font-style: italic;name: i;}");
		css.addRule("h6 {font-size: xx-small;font-weight: bold;margin-top: 10;name: h6;margin-bottom: 10;}");
		css.addRule("h5 {font-size: x-small;font-weight: bold;margin-top: 10;name: h5;margin-bottom: 10;}");
		css.addRule("h4 {font-size: small;font-weight: bold;margin-top: 10;name: h4;margin-bottom: 10;}");
		css.addRule("h3 {font-size: medium;font-weight: bold;margin-top: 10;name: h3;margin-bottom: 10;}");
		css.addRule("dir li p {margin-top: 0;name: dir li p;margin-bottom: 0;}");
		css.addRule("h2 {font-size: large;font-weight: bold;margin-top: 10;name: h2;margin-bottom: 10;}");
		css.addRule("b {font-weight: bold;name: b;}");
		css.addRule("h1 {font-size: x-large;font-weight: bold;margin-top: 10;name: h1;margin-bottom: 10;}");
		css.addRule("caption {text-align: center;name: caption;caption-side: top;}");
		css.addRule("a {color: blue;name: a;text-decoration: underline;}");
		css.addRule("ul li ul li ul li {margin-top: 0;name: ul li ul li ul li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("menu {margin-top: 10;name: menu;margin-left: 40;margin-bottom: 10;}");
		css.addRule("menu li p {margin-top: 0;name: menu li p;margin-bottom: 0;}");
		css.addRule("sup {name: sup;vertical-align: sup;}");
		css.addRule("body {color: black;font-size: 14pt;font-weight: normal;name: body;font-family: Serif;margin-right: 0;margin-left: 0;}");
		css.addRule("ul li ul li ul {list-style-type: square;name: ul li ul li ul;margin-left: 25;}");
		css.addRule("blockquote {margin-top: 5;name: blockquote;margin-right: 35;margin-bottom: 5;margin-left: 35;}");
		css.addRule("samp {font-size: small;font-family: Monospaced;name: samp;}");
		css.addRule("cite {font-style: italic;name: cite;}");
		css.addRule("sub {name: sub;vertical-align: sub;}");
		css.addRule("em {font-style: italic;name: em;}");
		css.addRule("ul li p {margin-top: 0;name: ul li p;margin-bottom: 0;}");
		css.addRule("ul li ul li {margin-top: 0;name: ul li ul li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("var {font-style: italic;font-weight: bold;name: var;}");
		css.addRule("table {border-color: Gray;name: table;border-style: outset;}");
		css.addRule("dfn {font-style: italic;name: dfn;}");
		css.addRule("menu li {margin-top: 0;name: menu li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("strong {font-weight: bold;name: strong;}");
		css.addRule("ul {list-style-type: disc;margin-top: 10;name: ul;margin-right: 50;margin-bottom: 10;margin-left: 50;}");
		css.addRule("center {text-align: center;name: center;}");
		css.addRule("ul li ul {list-style-type: circle;name: ul li ul;margin-left: 25;}");
		css.addRule("kbd {font-size: small;font-family: Monospaced;name: kbd;}");
		css.addRule("dir li {margin-top: 0;name: dir li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("ul li menu {list-style-type: circle;name: ul li menu;margin-left: 25;}");
		css.addRule("dt {margin-top: 0;name: dt;margin-bottom: 0;}");
		css.addRule("ol li {margin-top: 0;name: ol li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("li p {margin-top: 0;name: li p;margin-bottom: 0;}");
		css.addRule("default {name: default;}");
		css.addRule("strike {name: strike;text-decoration: line-through;}");
		css.addRule("dl {margin-top: 10;name: dl;margin-left: 0;margin-bottom: 10;}");
		css.addRule("tt {font-family: Monospaced;name: tt;}");
		css.addRule("ul li {margin-top: 0;name: ul li;margin-right: 0;margin-left: 0;margin-bottom: 0;}");
		css.addRule("dir {margin-top: 10;name: dir;margin-left: 40;margin-bottom: 10;}");
		css.addRule("tr {text-align: left;name: tr;}");
		css.addRule("pre p {margin-top: 0;name: pre p;}");
		css.addRule("dd {margin-top: 0;name: dd;margin-left: 40;margin-bottom: 0;}");
		css.addRule("th {padding-top: 3;padding-left: 3;text-align: center;font-weight: bold;border-color: Gray;padding-right: 3;name: th;border-style: inset;padding-bottom: 3;}");
		css.addRule("pre {margin-top: 5;font-family: Monospaced;name: pre;margin-bottom: 5;}");
		css.addRule("td {padding-top: 3;padding-left: 3;border-color: Gray;padding-right: 3;name: td;border-style: inset;padding-bottom: 3;}");
		css.addRule("code {font-size: small;font-family: Monospaced;name: code;}");
		css.addRule("small {font-size: x-small;name: small;}");
		css.addRule("big {font-size: x-large;name: big;}");
		return css;
	}
*/
	/*
	 * public boolean Online(AnchorPoint bp, Line bl) { AnchorPoint b1 = new
	 * AnchorPoint (bl.getX1(), bl.getY1()); if(slope(bl) == slope(b1,bp)) return
	 * true; else return false; }
	 */
}
