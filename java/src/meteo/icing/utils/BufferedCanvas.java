package meteo.icing.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BufferedCanvas
{
	public final BufferedImage image;

	public final Graphics2D g2d;

	public boolean antialias = true;

	public String format;

	public BufferedCanvas(int width, int height)
	{
		this(width, height, BufferedImage.TYPE_INT_RGB, "png");
	}

	public BufferedCanvas(int width, int height, int imageType, String format)
	{
		image = new BufferedImage(width, height, imageType);
		g2d = image.createGraphics();
		this.format = format;
	}

	public void writeImage(String name)
	{
		this.writeImage(new File(name));
	}

	public void writeImage(File file)
	{
		try
		{
			ImageIO.write( image, format, file);
		}
		catch (IOException e) { throw new RuntimeException("Failed to write image to " + file.getPath()); }
	}

	public Graphics2D g2d() { return g2d; }

	public int getWidth() { return image.getWidth(); }
	public int getHeight() { return image.getHeight(); }

	public void init( Color color )
	{
		if(antialias)
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setPaint( color );

		g2d.fill( new Rectangle( 0, 0, this.getWidth(), this.getHeight() ) );
	}

	public void dispose()
	{
		g2d.dispose();
	}

}
