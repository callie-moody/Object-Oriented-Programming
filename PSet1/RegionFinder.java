import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * @author Travis W. Peters, Dartmouth CS 10, Updated Winter 2015
 * @author CBK, Spring 2015, updated for CamPaint
 */

//Callie Moody and Xiao Li 1/24/2021

public class RegionFinder {
	// how similar a pixel color must be to the target color, to belong to a region
	// suitable value for maxColorDiff depends on your implementation of colorMatch()
	// and how much difference in color you want to allow
	private static final int maxColorDiff = 20;
	// how many points in a region to be worth considering
	private static final int minRegion = 50;
	// the image in which to find regions
	private BufferedImage image;
	// the image with identified regions recolored
	private BufferedImage recoloredImage;
	// a region is a list of points so the identified regions are in a list of lists of points
	private ArrayList<ArrayList<Point>> regions;

	//Constructors
	public RegionFinder() {
		this.image = null;
	}
	public RegionFinder(BufferedImage image) {
		this.image = image;		
	}

	//getters and setters
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	public BufferedImage getImage() {
		return image;
	}
	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	//Get the RGB value at point pt for Image img
	public int getRGBPoint(Point pt, BufferedImage img)
	{
		int y= (int)pt.getY();
		int x= (int)pt.getX();

		return img.getRGB(x,y);
	}


	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary)
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		//get RGB for both colors
		int Red1= c1.getRed();
		int Red2= c2.getRed();
		int Blue1= c1.getBlue();
		int Blue2= c2.getBlue();
		int Green1= c1.getGreen();
		int Green2= c2.getGreen();
		//check difference between each channel
		if(Math.abs(Red1-Red2)<=maxColorDiff && Math.abs(Blue1-Blue2)<=maxColorDiff
				&& Math.abs(Green1-Green2)<=maxColorDiff)
		{return true;} //colorMatch is true if the colors are close enough

		else{return false;}
	}
	/**
	 * Sets regions to the flood fill regions in the image, similar enough to the targetColor.
	 */
	public void findRegions(Color targetColor) {

		//initialize our list of all regions
		regions= new ArrayList<ArrayList<Point>>();
		//create an image that stores whether each pixel has been visited: set all values to 0
		 BufferedImage visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		 //Check every pixel of the image for target color match
		for(int x=0; x<image.getWidth(); x++)
		{for(int y=0; y< image.getHeight(); y++) {

			Color imagecolor= new Color (image.getRGB(x,y));

				//if the color of the pixel matches and the pixel is not yet part of a region
			if (colorMatch(targetColor,imagecolor) && visited.getRGB(x,y)==0)
				{	Point startpt= new Point(x,y);

					//create a new region beginning with the start point
					ArrayList<Point> thisregion = new ArrayList<Point>();
					thisregion.add(startpt);

					//create queue of points to check their neighbors
					ArrayList<Point> queue = new ArrayList<Point>();
					queue.add(startpt);

					//check the neighbors of every point in the queue
					do
					{
						//begin with the first point in the queue
						Point m= queue.get(0);

						//check all the neighbors of the point
							for(int i=-1; i<=1; i++) {
							for(int j=-1; j<=1; j++) {

								int nx=(int)(m.getX()+i);
								int ny=(int)(m.getY()+j);

								//if the neighbor is within bounds and unvisited, get the colors
								if(nx<image.getWidth()&&nx>=0 && ny<image.getHeight() && ny>=0
										&& visited.getRGB(nx,ny)==0){

										Color nbr= new Color (image.getRGB(nx,ny));

									//if this neighbor matches, add it to the region
										if (colorMatch(targetColor,nbr)) {

											Point newnbr = new Point(nx, ny);
											thisregion.add(newnbr);

											//add neighbor to to check queue and mark as visited
											queue.add(newnbr);
											visited.setRGB(nx, ny, 1);

										}
								}
							}
							}

						//after checking all neighbors, mark center as visited
						visited.setRGB(x,y,1);

							//take the checked pixel off the queue to check
						queue.remove(0);


					}while(queue.size()>0);

					//if the final region after all points are checked is big enough, add to the list of regions
					if(thisregion.size()>=minRegion){
					regions.add(thisregion);}



				}
		}}

	}



	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		int maxsize=0;

		ArrayList<Point> biggest= new ArrayList<Point>();

		//check the size of each region found and save the biggest one
		for(ArrayList<Point> rgn: regions)
		{
			if (rgn.size()>maxsize)
			{
				maxsize=rgn.size();
				biggest=rgn;

			}
		}

		return biggest;
	}

	/**
	 * Sets recoloredImage to be a copy of image, 
	 * but with each region a uniform random color, 
	 * so we can see where they are
	 */

	public void recolorImage() {
		// First copy the original image
		recoloredImage = new BufferedImage(image.getColorModel(),image.copyData(null),
				image.getColorModel().isAlphaPremultiplied(),null);

		for(ArrayList<Point> region : regions) {

				//Create a random color
				int randomrgb = (int) (Math.random() * 16777217);
				Color randColor = new Color(randomrgb);

				// Now recolor the each region with the random color
				for (Point pt : region) {
					recoloredImage.setRGB((int) pt.getX(), (int) pt.getY(), randColor.getRGB());
					}
		}
	}
}
