import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.SQLOutput;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Webcam-based drawing 
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 */

//Callie Moody and Xiao Li 1/24/2021
public class CamPaint extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private RegionFinder finder;			// handles the finding
	private Color targetColor;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
	private BufferedImage painting;			// the resulting masterpiece

	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaint() {
		finder= new RegionFinder();
		clearPainting();
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting, 
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {

		if(displayMode=='w')//shows unprocessed webcam
			{g.drawImage(image, 0, 0, null);}

		else if(displayMode=='p')//shows the painting
			{g.drawImage(painting, 0, 0, null);}

		else if(displayMode=='r')//shows the image with the recolored brush
			{ g.drawImage(finder.getRecoloredImage(), 0, 0, null); }
	}

	/**
	 * Webcam method, here finding regions and updating painting.
	 */
	@Override
	public void processImage() {
		if (image != null && targetColor!=null) {

			//pass the image into region finder and find the regions matching targetColor
			finder.setImage(image);
			finder.findRegions(targetColor);
			//recolor the image so that the brush appears as paintColor
			finder.recolorImage();
			//for each point in the selected region, recolor the painting to be paintColor
			for(Point pt : finder.largestRegion())
			{painting.setRGB((int) pt.getX(),(int) pt.getY(),paintColor.getRGB());}


	}	}

	/**
	 * Overrides the DrawingGUI method to set targetColor.
	 */
	@Override
	public void handleMousePress(int x, int y) {
		// to be safe, make sure webcam is grabbing an image
		if (image != null) {
			targetColor= new Color (image.getRGB(x,y));
		}
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { // clear
			clearPainting();
		}
		else if (k == 'o') { // save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { // save the painting
			saveImage(painting, "pictures/painting.png", "png");
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaint();
			}
		});
	}
}
