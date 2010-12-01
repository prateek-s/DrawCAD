package dcad.util;

import ij.util.StringSorter;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JApplet;

import dcad.model.constraint.Constraint;
import dcad.model.constraint.constraintsHelper;
import dcad.model.geometry.AnchorPoint;
import dcad.model.geometry.Stroke;
import dcad.model.geometry.segment.SegLine;
import dcad.process.io.Command;
import dcad.ui.drawing.DrawingData;
import dcad.ui.drawing.DrawingView;
import dcad.ui.help.HelpView;
import dcad.ui.main.MainWindow;
import dcad.ui.recognize.RecognizedView;
import Jama.Matrix;

public class GMethods
{
    private static final int DOUBLE_PRECISION = 3;
	private static DecimalFormat format = new DecimalFormat();
	public static String codeBase="";
	protected static String propertyFilePath="";
	public static JApplet applet = null;
	
	private GMethods()
	{
	}
	
	public static void init(String propFilePath)
	{
	    format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	    format.setMinimumIntegerDigits(1);
	    format.setMaximumFractionDigits(DOUBLE_PRECISION);
	    format.setMinimumFractionDigits(DOUBLE_PRECISION);
	    format.setGroupingUsed(false);
		propertyFilePath=propFilePath;
		PropertyFileHandler.getInstance().loadData(propFilePath);
	}
	
	public static void initializeProperties()
	{
		PropertyFileHandler.getInstance().loadData(propertyFilePath);
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String path, String description)
	{
//		java.net.URL imgURL = Methods.class.getResource(path);
//		if (imgURL != null)
//		{
//			return new ImageIcon(imgURL, description);
//		} else
//		{
//			System.err.println("Couldn't find file: " + path);
//			return null;
//		}
		return new ImageIcon(path, description);
		
	}

	public static ImageIcon createImage(String path)
	{
		return null;
	}
	
	public static DrawingView getCurrentView()
	{
		return MainWindow.getDrawingView();
	}

	public static RecognizedView getRecognizedView()
	{
		return MainWindow.getRecognizedView();
	}
	
	//added on 30-05-10
	
	public static DrawingData getDrawingData(){
		return MainWindow.getDrawingView().getDrawData();
	}
	
	public static Stroke getLastStroke(){
		Vector stkList = MainWindow.getDrawingView().getM_drawData().getStrokeList();
		return (Stroke)stkList.get(stkList.size()-1);
	}
	public static HelpView getHelpView()
	{
		return MainWindow.getHelpView();
	}

	public static void printMat(double [][] mat)
	{
		///System.out.println("------- Printing Matrix -----------");
		int m = mat.length;
		int n = mat[0].length;
		for(int i = 0; i < m; i++)
		{
			for (int j = 0; j < n; j++)
			{
				System.out.print(mat[i][j]+", ");
			}
			///System.out.println();
		}
		///System.out.println();
	}

	public static void printArr(double [] arr)
	{
		///System.out.println("------- Printing Array -----------");
		for(int i = 0; i < arr.length; i++)
		{
			///System.out.println(arr[i]);
		}
		///System.out.println();
	}
	
	public static String formatNum(double num)
	{
	    return format.format(num);
	}
	
	public static double[] getCoords(double[][] points, int dim)
	{
		/////System.out.println(start+", "+end);
		// check if the values are legitimate
		double [] coords = new double[points.length];
		for(int i=0; i<points.length; i++)
		{
			coords[i] = points[i][dim];
		}

		return coords;
	}


	/** Sorts the array. */
	public static void sortStrings(String[] a)
	{
		StringSorter.sort(a);
	}
	
	public static int findMax(double[] arr)
	{
		int maxIdx = -1;
		double maxVal = -Double.MAX_VALUE;
		for (int i = 0; i < arr.length; i++)
		{
//			///System.out.println(arr[i] - maxVal);
			if((arr[i] != Double.NaN)&&(arr[i] > maxVal))
			{
				maxIdx = i;
				maxVal = arr[i];
			}
		}
		///System.out.println("Max Index: "+maxIdx+", MaxVal: "+maxVal);
		return maxIdx;
	}

	public static int findMin(double[] arr)
	{
		int minIdx = -1;
		double minVal = Double.MAX_VALUE;
		for (int i = 0; i < arr.length; i++)
		{
			if((arr[i] != Double.NaN)&&(arr[i] < minVal))
			{
				minIdx = i;
				minVal = arr[i];
			}
		}
		return minIdx;
	}

	public static double findSum(double[] arr)
	{
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++)
		{
			if(arr[i] != Double.NaN) sum += arr[i];
		}
		return sum;
	}

	public static double findSumIgnoreSign(double[] arr)
	{
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++)
		{
			if(arr[i] != Double.NaN) sum += Math.abs(arr[i]);
		}
		return sum;
	}

	public static double getColMean(double [][] mat, int colIndex)
	{
		double centriod = 0.0;
		for (int i = 0; i < mat.length; i++)
		{
			centriod += mat[i][colIndex];
		}
		return centriod/mat.length;
	}

	public static double[] getCetriod(double[][] points)
	{
		double[] centriod = new double[2];
		for (int i = 0; i < points.length; i++)
		{
			centriod[0] += points[i][0];
			centriod[1] += points[i][1];
		}
		centriod[0] = centriod[0]/points.length;
		centriod[1] = centriod[1]/points.length;
		
		return centriod;
	}
	
	public static Point2D getPointOnCircle(double angle, Point2D center, double radius)
	{
		Point2D.Double pt = new Point2D.Double(radius * Math.cos(angle), radius * Math.sin(angle));
		AffineTransform tx = new AffineTransform();
		tx.setToTranslation(center.getX(), center.getY());
		Point2D newPt = new Point2D.Double();
		tx.transform(pt, newPt);
		
		return newPt;
	}

	public static Point2D getClosePointOnLine(Line2D l, Point2D ct)
	{
		// converting into slope and point form y = mx + c
		double m1 = Maths.slope(l.getP1(), l.getP2());
		double m2 = -1 / m1;
		double x, y;

		if (Math.abs(m1) > 1000)
		{
			x = l.getX1();
			y = ct.getY();

		}
		else if (Math.abs(m2) > 1000)
		{

			x = ct.getX();	// private static final String DEF_FILENAME = "Untitled";

			y = l.getY1();
		}
		else
		{
			double c1 = l.getY1() - m1 * l.getX1();
			double c2 = ct.getY() - m2 * ct.getX();

			x = (c2 - c1) / (m1 - m2);
			y = m1 * x + c1;
		}
		return new Point2D.Double(x, y);
	}

	public static double slope(Point2D a, Point2D b)
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
	
	public static Point intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		Point pt = null;
		if((x1==x3) && (y1==y3))
		{
			pt = new Point((int)x1, (int)y1);
		}
		else if((x2==x4) && (y2==y4))
		{
			pt = new Point((int)x2, (int)y2);
		}
		else
		{
			double ua = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
			ua = ua / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

			double x = x1 + ua * (x2 - x1);
			double y = y1 + ua * (y2 - y1);

			pt = new Point((int)x, (int)y);
		}
		return pt;
	}

	public static Point2D intersection(Line2D l, Point2D p1, Point2D p2)
	{

		double x1 = l.getX1();
		double y1 = l.getY1();

		double x2 = l.getX2();
		double y2 = l.getY2();

		double x3 = p1.getX();
		double y3 = p1.getY();

		double x4 = p2.getX();
		double y4 = p2.getY();

		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public static Point2D intersection(Line2D l1, Line2D l2)
	{

		double x1 = l1.getX1();
		double y1 = l1.getY1();

		double x2 = l1.getX2();
		double y2 = l1.getY2();

		double x3 = l2.getX1();
		double y3 = l2.getY1();

		double x4 = l2.getX2();
		double y4 = l2.getY2();

		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public static Point2D intersection(Point2D p1, Point2D p2, Point2D p3, Point2D p4)
	{
		double x1 = p1.getX();
		double y1 = p1.getY();

		double x2 = p2.getX();
		double y2 = p2.getY();

		double x3 = p3.getX();
		double y3 = p3.getY();

		double x4 = p4.getX();
		double y4 = p4.getY();
		
		return intersection(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public static Point2D nearestPointOnLineFromPoint(Line2D line, Point2D pt)
	{
		double linePtDist = line.ptSegDist(pt);
		double hypo_1 = line.getP1().distance(pt);
		double hypo_2 = line.getP2().distance(pt);
		
		double dist_1 = Math.sqrt(hypo_1*hypo_1 - linePtDist*linePtDist); 
		double dist_2 = Math.sqrt(hypo_2*hypo_2 - linePtDist*linePtDist);
		
		return new Point2D.Double((dist_1*line.getX2() + dist_2*line.getX1())/(dist_1+dist_2), (dist_1*line.getY2() + dist_2*line.getY1())/(dist_1+dist_2));
	}

	public static void main(String[] args)
	{
		///System.out.println(intersection(100,100,1,10,1,1,1,10));
	}

	public static boolean equal(Point2D pt1, Point2D pt2)
	{
		if((((int)Math.abs(pt1.getX()-pt2.getX()) == 0)) && (((int)Math.abs(pt1.getY()-pt2.getY()) == 0))) return true;
		return false;
	}
	
	public static boolean equal(Line2D l1, Line2D l2)
	{
		if((equal(l1.getP1(), l2.getP1())) && (equal(l1.getP2(), l2.getP2()))) return true;
		else if((equal(l1.getP1(), l2.getP2())) && (equal(l1.getP2(), l2.getP1()))) return true;
		return false;
	}
	
	public static boolean near(Point2D pt1, Point2D pt2, double allowedGap)
	{
		if(pt1.distance(pt2) <= allowedGap) return true;
		return false;
	}
	
	public static void addConstraintToRecogView(Constraint c)
	{
	//	MainWindow.getRecognizedView().addConstraint(c);
	}

	public static void addCommandToCommandQueue(Command comm)
	{
		DrawingView dv = GMethods.getCurrentView();
		dv.logEvent(comm);
	}

	public static Vector CircleCircleIntersections(double x0, double y0, double r0, double x1, double y1, double r1)
	{
	    /////System.out.println("GMethods.CircleCircleIntersections()");
		/* dx and dy are the vertical and horizontal distances between
	     * the circle centers.
	     */
	    double dx = x1 - x0;
	    double dy = y1 - y0;

	    /* Determine the straight-line distance between the centers. */
	    double d = Math.sqrt((dy*dy) + (dx*dx));
	    /* 'point 2' is the point where the line through the circle
	     * intersection points crosses the line between the circle
	     * centers.  
	     */
	    /* Check for solvability. */
	    if ((d > (r0 + r1)) || (d < Math.abs(r0 - r1)))
	    {
	      /* no solution. circles do not intersect. */
	      return null;
	    }
	    /* Determine the distance from point 0 to point 2. */
	    double a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0 * d) ;

	    /* Determine the coordinates of point 2. */
	    double x2 = x0 + (dx * a/d);
	    double y2 = y0 + (dy * a/d);

	    /* Determine the distance from point 2 to either of the
	    * intersection points.
	    */
	    double h = Math.sqrt((r0*r0) - (a*a));

	    /* Now determine the offsets of the intersection points from
	    * point 2.
	    */
	    double rx = -dy * (h/d);
	    double ry = dx * (h/d);

	    /* Determine the absolute intersection points. */
	    Point2D.Double inter_p1 = new Point2D.Double(x2 + rx, y2 + ry);
	    Point2D.Double inter_p2 = new Point2D.Double(x2 - rx, y2 - ry);
		  
	    /////System.out.println("$$$$$$$$$$$ "+inter_p1+", "+inter_p2);
		Vector intersections = new Vector();
		intersections.add(inter_p1);
		intersections.add(inter_p2);
		return intersections;
	}
	
	public static String convertStringToUnicode(String s)
	{
		if (s == null)
		{
			return s;
		}
		String decoded = s;

		try
		{
			int sindex;
			int osindex;
			sindex = s.indexOf("&#");
			osindex = 0;

			if (sindex >= 0)
			{ // we can still have \\u, but we'll work it out.
				String dec = null;
				char tchar = '\u0000';

				// we will build up our new string in here:
				StringBuffer sb2 = new StringBuffer(s.length());

				while ((sindex >= 0) && (sindex < s.length()))
				{
					sb2.append(s.substring(osindex, sindex));
					osindex = sindex;

					// make sure we _have_ 4 more chars:
					dec = s.substring(sindex + 2, sindex + 6);
					try
					{
						tchar = (char) Integer.parseInt(dec, 10);
						sindex += 7;
					}
					catch (NumberFormatException nfe)
					{
						// if unicode is of 3 digits.
						dec = s.substring(sindex + 2, sindex + 5);
						tchar = (char) Integer.parseInt(dec, 10);
						sindex += 6;
					}

					// dec parsed to an int, now see if its a character...
					if (Character.isDefined(tchar))
					{
						sb2.append(tchar);
					}
					else
					{
						///System.out.println("'" + tchar + "' was not defined as a unicode character");
					}

					osindex = sindex;
					sindex = s.indexOf("&#", sindex);
				}
				if (sindex < 0)
				{// grab the rest of the string.
					sb2.append(s.substring(osindex));
				}

				decoded = sb2.toString();
				sb2 = null; // get rid of it
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			return s;
		}
		return decoded;
	}
	/**
	 * find the point of extrapolation, length is the distance desired between p2 and the new point
	 * @param p1 the first point
	 * @param p2 the second point
	 * @param len distance desired between p2 and the extrapolated point
	 * @return the extrapolation point
	 */
	public static Point2D extrapolate(Point2D p1, Point2D p2, double len)
	{
//		///System.out.println("GMethods.extrapolate()");
		double dist = p1.distance(p2);
		double x1 = (p2.getX()*(dist+len) - len*p1.getX())/(dist);
		double y1 = (p2.getY()*(dist+len) - len*p1.getY())/(dist);
		return new Point2D.Double(x1, y1);
	}
	
	/**
	 * find the point of interpolation, length is the distance desired between p1 and the new point
	 * @param p1 the first point
	 * @param p2 the second point
	 * @param len distance desired between p1 and the interpolated point
	 * @return the interpolation point
	 */
	public static Point2D interpolate(Point2D p1, Point2D p2, double len)
	{
//		///System.out.println("GMethods.interpolate()");
		double ratio = len/p1.distance(p2);
		double x1 = (p1.getX()*(1-ratio) + ratio*p2.getX());
		double y1 = (p1.getY()*(1-ratio) + ratio*p2.getY());
		return new Point2D.Double(x1, y1);
	}

	public static ImageIcon getImageIcon(String imageName, String text)
	{
		if(codeBase=="")
			return new ImageIcon(GConstants.IMAGE_SRC+imageName, text);
		else
		{
			String imageURLString = codeBase + GConstants.IMAGE_SRC + imageName;
			URL imageURL = null;
			try
			{
				imageURL = new URL(imageURLString);
				/////System.out.println(imageURLString);
			}
			catch(Exception e)
			{
				///System.out.println("Problem with the URL");				
			}
			return new ImageIcon(imageURL,text);
		}
	}
	
	public static InputStream getInputStream(String fileName)
	{
		if(codeBase=="")
		{
			try
			{
				FileInputStream fin=new FileInputStream(fileName);
				return (fin);
			}
			catch (Exception e)
			{
				return null;
			}
		}
		else
		{
			URL propertiesFileURL = null;
			try
			{
				propertiesFileURL = new URL(codeBase+fileName);
				return propertiesFileURL.openStream();
			}
			catch (Exception e)
			{
				return null;
			}
		}

	}
	
	public static void printMatrix(Matrix m, String name,boolean print)
	{
		int noOfRows=m.getRowDimension();
		int noOfColumns=m.getColumnDimension();
		if(print)
		{
			System.out.println("Printing matrix : " + name);
			
			for(int i=0;i<noOfRows;i++)
			{
				for(int j=0;j<noOfColumns;j++)
					System.out.print(m.get(i,j) + "  ");
				System.out.println("");
			}
			System.out.println("");
		}
	}	
	
	static long timeBeforeOperation=0;
	static long timeAfterOperation;
	public static void printTime(String text,boolean isBefore,boolean print)
	{
		String str1 = "After";
		if(isBefore)
		{
			str1="Before";
			timeBeforeOperation = System.currentTimeMillis();
		}
		if(print)
			///System.out.println(str1 + " " + text + " : " + System.currentTimeMillis());
		if(!isBefore);
			///System.out.println("The difference is : " + (System.currentTimeMillis() - timeBeforeOperation));
	}
	
	public static Point2D getMidPoint(Point2D p, Point2D q)
	{
		return  new Point2D.Double(  ( p.getX() + q.getX() ) / 2  , ( p.getY() + q.getY() ) / 2  );  
	}
}