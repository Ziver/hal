package zutil.image.filters;

import java.awt.image.BufferedImage;

import zutil.image.ImageFilterProcessor;
import zutil.math.ZMath;


public class DitheringFilter extends ImageFilterProcessor{
	// default palette is black and white
	private int[][] palette = {
			{255,0,0,0},
			{255,255,255,255}
	};


	/**
	 * Sets up a default DitheringEffect
	 */
	public DitheringFilter(BufferedImage img){
		super(img);
	}
	
	/**
	 * Creates a Dithering Effect object
	 * @param img The image to apply the effect on
	 * @param palette The palette to use on the image 
	 * int[colorCount][4]
	 * 0 -> Alpha data
	 * 		Red data
	 * 		Green data
	 * 4 ->	Blue data
	 */
	public DitheringFilter(BufferedImage img, int[][] palette){
		super(img);
		this.palette = palette;
	}

	@Override
	public int[][][] process(final int[][][] data, int cols, int rows) {
		int error, index;
		int[] currentPixel;

		int[][][] output = copyArray(data, cols, rows);
		
		for(int y=0; y<rows ;y++){
			setProgress(ZMath.percent(0, rows-1, y));
			for(int x=0; x<cols ;x++){
				currentPixel = output[y][x];
				index = findNearestColor(currentPixel, palette);
				output[y][x] = palette[index];

				for (int i = 1; i < 4; i++)	{
					error = currentPixel[i] - palette[index][i];
					if (x + 1 < cols) {
						output[y+0][x+1][i] =	clip( output[y+0][x+1][i] + (error*7)/16 );
					}
					if (y + 1 < rows) {
						if (x - 1 > 0) 
							output[y+1][x-1][i] = clip( output[y+1][x-1][i] + (error*3)/16 );
						output[y+1][x+0][i] = clip( output[y+1][x+0][i] + (error*5)/16 );
						if (x + 1 < cols) 
							output[y+1][x+1][i] = clip( output[y+1][x+1][i] + (error*1)/16 );
					}
				}
			}
		}

		return output;
	}
	
    private static int findNearestColor(int[] color, int[][] palette) {
        int minDistanceSquared = 255*255 + 255*255 + 255*255 + 1;
        int bestIndex = 0;
        for (byte i = 0; i < palette.length; i++) {
            int Rdiff = color[1] - palette[i][0];
            int Gdiff = color[2] - palette[i][1];
            int Bdiff = color[3] - palette[i][2];
            int distanceSquared = Rdiff*Rdiff + Gdiff*Gdiff + Bdiff*Bdiff;
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
