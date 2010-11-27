package dcad.model.geometry;

import java.awt.Point;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PixelInfo extends Point
{
	private long time = 0L;
	private double speed = 0;
	private double curvature = 0;
	private double slope = 0;
	
	public PixelInfo(Point pt, long time)
	{
		this(pt.x, pt.y, time);
		this.time = time;
	}

	public PixelInfo(int x, int y, long time)
	{
		this.setLocation(x, y);
		this.time = time;
	}
	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public double getCurvature()
	{
		return curvature;
	}

	public void setCurvature(double curvature)
	{
		this.curvature = curvature;
	}

	public double getSpeed()
	{
		return speed;
	}

	public void setSpeed(double speed)
	{
		// convert to distance per second
		this.speed = speed*1000;
	}

	/**
	 * speed, curvature etc.
	 */
	public String toXMLString()
	{
	    DecimalFormat format = new DecimalFormat();
	    format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	    format.setMinimumIntegerDigits(1);
	    format.setMaximumFractionDigits(5);
	    format.setMinimumFractionDigits(5);
	    format.setGroupingUsed(false);
		String retStr = this.getClass().getName()+":("+x+", "+y+", "+time+")  speed: "+format.format(speed)+ "  Slope: "+format.format(slope)+"  curvature: "+format.format(curvature);
		return retStr;
	}

	/**
	 * x,y,t
	 * @return
	 */
	public String toString()
	{
		String retStr = "<POINT>"+": (" + x + "," + y + "," + time + ")";
		return retStr;
	}

	public double getSlope()
	{
		return slope;
	}

	public void setSlope(double slope)
	{
		this.slope = slope;
	}
}
