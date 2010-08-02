package dcad.util;
import java.awt.Color;

public class MyColor
{
	/**
	 * Used for getting a Color object given the name of that color.<br>
	 * Valid names are <br>
	 * white <br>
	 * blue <br>
	 * cyan <br>
	 * gray <br>
	 * lightGray <br>
	 * darkGray <br>
	 * green <br>
	 * magenta <br>
	 * pink <br>
	 * orange <br>
	 * red <br>
	 * yellow <br>
	 * <br>
	 * If the given name is not one of these, the returned object is for black
	 * color.
	 */
	public static Color getColor(String color)
	{
		Color col;
		if (color.equals("white"))
			col = Color.white;
		else if (color.equals("blue"))
			col = Color.blue;
		else if (color.equals("cyan"))
			col = Color.cyan;
		else if (color.equals("gray"))
			col = Color.gray;
		else if (color.equals("lightGray"))
			col = Color.lightGray;
		else if (color.equals("darkGray"))
			col = Color.darkGray;
		else if (color.equals("green"))
			col = Color.green;
		else if (color.equals("magenta"))
			col = Color.magenta;
		else if (color.equals("pink"))
			col = Color.pink;
		else if (color.equals("orange"))
			col = Color.orange;
		else if (color.equals("red"))
			col = Color.red;
		else if (color.equals("yellow"))
			col = Color.yellow;
		else
			col = Color.black;
		return col;
	}

	/**
	 * Used for getting a Color object given the RGB values.
	 */
	public static Color getColor(String R, String G, String B)
	{
		int red = Integer.parseInt(R.trim());
		int green = Integer.parseInt(G.trim());
		int blue = Integer.parseInt(B.trim());
		return new Color(red, green, blue);
	}

	public static Color transparentColor(String color)
	{
		Color c = getColor(color);
		return transparentColor(c);
	}

	public static Color transparentColor(Color c)
	{
		int green = c.getGreen();
		int blue = c.getBlue();
		int red = c.getRed();
		return new Color(red, green, blue, 50);
	}

	public static String getTransparentColor(String color)
	{
		String col;
		if (color.equals("red"))
			col = "FF9966";
		else if (color.equals("blue"))
			col = "6699FF";
		else if (color.equals("green"))
			col = "66FF99";
		else
			col = color;
		return col;
	}
}