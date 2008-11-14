package zutil.image.filters;

import java.awt.image.BufferedImage;

import zutil.image.ImageFilterProcessor;
import zutil.math.ZMath;

public class ColorIntensityFilter extends ImageFilterProcessor{
	private boolean invert;
	private int redScale;
	private int greenScale;
	private int blueScale;

	public ColorIntensityFilter(BufferedImage img){
		this(img, 50, 50, 50, false);
	}

	/**
	 * Creates a ColorIntensityEffect object with the given values
	 * @param img The image data
	 * @param inv If the image color should be inverted
	 */
	public ColorIntensityFilter(BufferedImage img, boolean inv){
		this(img, 100, 100, 100, inv);
	}

	/**
	 * Creates a ColorIntensityEffect object with the given values
	 * @param img The image data
	 * @param red The scale of red (0-100)
	 * @param green The scale of green (0-100)
	 * @param blue The scale of blue (0-100)
	 */
	public ColorIntensityFilter(BufferedImage img, int red, int green, int blue){
		this(img, red, green, blue, false);
	}

	/**
	 * Creates a ColorIntensityEffect object with the given values
	 * @param img The image data
	 * @param red The scale of red (0-100)
	 * @param green The scale of green (0-100)
	 * @param blue The scale of blue (0-100)
	 * @param inv If the image color should be inverted
	 */
	public ColorIntensityFilter(BufferedImage img, int red, int green, int blue, boolean inv){
		super(img);
		invert = false;
		redScale = red;
		greenScale = green;
		blueScale = blue;
	}

	@Override
	public int[][][] process(final int[][][] data, int cols, int rows) {
		// making sure the scales are right
		if(redScale > 100) redScale = 100;
		else if(redScale < 0) redScale = 0;

		if(greenScale > 100) greenScale = 100;
		else if(greenScale < 0) greenScale = 0;

		if(blueScale > 100) blueScale = 100;
		else if(blueScale < 0) blueScale = 0;

		int[][][] output = new int[rows][cols][4];
		
		// Applying the color intensity to the image
		for(int y=0; y<rows ;y++){
			setProgress(ZMath.percent(0, rows-1, y));
			for(int x=0; x<cols ;x++){
				if(!invert){
					// inversion
					output[y][x][0] = data[y][x][0];
					output[y][x][1] = 255 - data[y][x][1] * redScale/100;
					output[y][x][2] = 255 - data[y][x][2] * greenScale/100;
					output[y][x][3] = 255 - data[y][x][3] * blueScale/100;
				}
				else{
					output[y][x][0] = data[y][x][0];
					output[y][x][1] = data[y][x][1] * redScale/100;
					output[y][x][2] = data[y][x][2] * greenScale/100;
					output[y][x][3] = data[y][x][3] * blueScale/100;
				}
			}
		}
		return output;
	}

}
