import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, explicit rectangle
 * @author CBK, Fall 2016, generic with Point2D interface
 * 
 */
public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// lower-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */
	public void insert(E p2) {
	//check which quadrant the point will go into
		//then check if that quadrant already has a child
		//if there already is a child, recurse insert using that child
		//else make p2 a new child

		//quadrant1
		if (p2.getX()>point.getX()&&p2.getY()<=point.getY())
			{if(hasChild(1)) {c1.insert(p2);}
			else {c1= new PointQuadtree<E>(p2,(int) point.getX(),y1,x2, (int) point.getY());}}

		//quadrant2
		else if (p2.getX()<=point.getX()&&p2.getY()<=point.getY())
			{if(hasChild(2)) {c2.insert(p2);}
			else {c2= new PointQuadtree<E>(p2,x1, y1,(int)point.getX(), (int) point.getY());}}

		//quadrant3
		else if (p2.getX()<=point.getX()&&p2.getY()>point.getY())
			{if(hasChild(3)){c3.insert(p2);}
			else{c3= new PointQuadtree<E>(p2,x1,(int) point.getY(),(int) point.getX(),y2);}}

		//quadrant4
		else if (p2.getX()>point.getX()&&p2.getY()>point.getY())
			{if(hasChild(4)){c4.insert(p2);}
			else{c4= new PointQuadtree<E>(p2,(int) point.getX(),(int) point.getY(),x2, y2);}}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		//initialize the size to be 1 to account for the root
		int size=1;

		//check each quadrant for children
		//if there is a child, add its size to the total
		if(hasChild(1))
			{size+=c1.size();}

		if(hasChild(2))
			{size+=c2.size();}

		if(hasChild(3))
			{size+=c3.size();}

		if(hasChild(4))
			{size+=c4.size();}

		return size;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {

		//create list that holds all points
		List<E> allpts = new ArrayList<E>();

		//call helper method
		addtoallPoints(allpts);

		return allpts;
	}

	//Helper method for all points
	//recursively adds all the points of each child to the total
	public void addtoallPoints(List<E> allpts)
	{	//add the root to the list of all points
		allpts.add(point);

		//check each quadrant for children
		//if children present, recurse to add their points to the list
		if(hasChild(1))
		{c1.addtoallPoints(allpts);}
		if(hasChild(2))
		{c2.addtoallPoints(allpts);}
		if(hasChild(3))
		{c3.addtoallPoints(allpts);}
		if(hasChild(4))
		{c4.addtoallPoints(allpts);}

	}

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {

		//create list that will hold all points in circle
		List<E> inside= new ArrayList<E>();

		//call helper method
		findInCircleHelper(inside, cx, cy, cr);

		return inside;

	}

	//helper method for find in circle
	//recursively adds all the points of each child inside the circle to the total
	public void findInCircleHelper (List<E> inside, double cx, double cy, double cr)
	{
		//checks if circle has area within the bounds of the tree
		if(Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2))

		{	//checks if root is inside circle
			//if so, adds to list
			if (Geometry.pointInCircle(point.getX(), point.getY(),  cx,  cy, cr))
			{inside.add(point);}

			//check each quadrant for children
			//recursively check if children are inside circle
			if(hasChild(1))
			{c1.findInCircleHelper(inside,cx,cy,cr);}
			if(hasChild(2))
			{c2.findInCircleHelper(inside,cx,cy,cr);}
			if(hasChild(3))
			{c3.findInCircleHelper(inside,cx,cy,cr);}
			if(hasChild(4))
			{c4.findInCircleHelper(inside,cx,cy,cr);}
	}}



	/**
	 * Returns a string representation of the tree
	 */
	public String toString() {

		//calls to string helper
		return toStringHelper("");
	}

	/**
	 * Recursively constructs a String representation of the tree from this node,
	 * starting with the given indentation and indenting further going down the tree
	 */
	public String toStringHelper(String indent) {

		//adds indent so each level has a distinct number of indentations
		//adds root to string
		//creates new line break
		String res = indent + point.toString() + "\n";

		//checks each quadrant for children
		//if there are children, recursively convert them to string

		if (hasChild(1)) res += c1.toStringHelper(indent+"  ");
		if (hasChild(2)) res += c2.toStringHelper(indent+"  ");
		if (hasChild(3)) res += c3.toStringHelper(indent+"  ");
		if (hasChild(4)) res += c4.toStringHelper(indent+"  ");

		return res;
	}
}
