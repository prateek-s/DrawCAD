package dcad.model.geometry;

import java.text.DecimalFormat;

class ArithElement extends Element
{
	private double val;
	private String text = "";

	public ArithElement()
	{
	}

	public ArithElement(double v)
	{
		val = v;
	}

	public void setRecursive()
	{
		update = true;
	}

	public void setValue(double d)
	{
		val = d;
	}

	public void setValue(ArithElement d)
	{
		val = d.dvalue();
	}

	public int value()
	{
		if (isUpdate())
			this.update();
		return (int) val;
	}

	public double dvalue()
	{
		if (isUpdate())
			this.update();
		return val;
	}

	public void setdvalue(double d)
	{
		val = d;
	}

	public String svalue()
	{
		if (update)
			this.update();
		return text;
	}

	public String text()
	{
		DecimalFormat f = new DecimalFormat();
		f.setMaximumFractionDigits(3);
		text = f.format(dvalue());
		return text + "     ";
	}

/*	public void slowMove(ArithElement q)
	{
		val = 0;
		while (val < (q.dvalue() - 1))
		{
			val += 2;
			GlobalConstants.timer.colon("0.00001");
		}
		val = q.dvalue();
	}
*/
}
