/*
 * Quadtree.java
 * Created by: William Tyas, portions credited to Geoffrey Matthews
 * Date: 8/9/17
 * Description: Used to speed up raytracing by dividing image plane
 * into quadrants and checking if spheres reside in a quadrant. If they
 * don't, there is no need to shoot a ray through any pixel in that
 * quadrant.
 */
import java.util.*;

public class Quadtree {
	private float minX, minY, maxX, maxY;
	private int level;
	private Quadtree ll, lr, ul, ur;
	private SphereList sphereList;
	private float camZ;

	public SphereList getSphereList() { return this.sphereList; }

	public Quadtree(float minX, float minY, float maxX, float maxY, int level, float camZ) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.level = level;
		this.camZ = camZ;
		this.sphereList = null;
		if (level == 0) {		// Leaf
			this.ll = null;
			this.lr = null;
			this.ul = null;
			this.ur = null;
		} else {				// interior node
			level--;
			float medX = 0.5f * (minX + maxX);
			float medY = 0.5f * (minY + maxY);
			this.ll = new Quadtree(minX, minY, medX, medY, level, camZ);
			this.lr = new Quadtree(medX, minY, maxX, medY, level, camZ);
			this.ul = new Quadtree(minX, medY, medX, maxY, level, camZ);
			this.ur = new Quadtree(medX, medY, maxX, maxY, level, camZ);
		}
	}

	public void addSphere(Sphere s) {
		if (this.level == 0) {		// leaf
			if (this.sphereList == null) {
				this.sphereList = new SphereList(s, null);
			} else {
				this.sphereList = this.sphereList.add(s);
			}
		} else {
			// Get bounding box
			float medX = 0.5f * (this.minX + this.maxX);
			float medY = 0.5f * (this.minY + this.maxY);
			float cx = s.getCenter().getX();
			float cy = s.getCenter().getY();
			float cz = s.getCenter().getZ();
			float r = s.getRadius();
			float pz = this.camZ;
	
			// x-extent
			float aX = (float) Math.sqrt(sqr(cx) + sqr(cz - pz));
			float thetaX = (float) Math.atan(r/aX);
			float psiX = (float) Math.asin(cx/aX); 
			float phiX = psiX - thetaX;
			float x1 = pz * (float) Math.tan(phiX);
			float x2 = pz * (float) Math.tan(phiX + 2*thetaX);
	
			// y-extent
			float aY = (float) Math.sqrt(sqr(cy) + sqr(cz - pz));
			float thetaY = (float) Math.atan(r/aY);
			float psiY = (float) Math.asin(cy/aY); 
			float phiY = psiY - thetaY;
			float y1 = pz * (float) Math.tan(phiY);
			float y2 = pz * (float) Math.tan(phiY + 2*thetaY);
	
			// Make sure x2 > x1 and y2 > y1
			if (x1 > x2) {
				float temp = x1;
				x1 = x2;
				x2 = temp; 
			}
			if (y1 > y2) {
				float temp = y1;
				y1 = y2;
				y2 = temp;
			}
	
			// Send down tree to bottom, adding sphere to each node it's in
			if (y1 < medY) {		// in bottom half
				if (x1 < medX) {	// in bottom left
					ll.addSphere(s);
				}
				if (x2 >= medX) {	// in bottom right
					lr.addSphere(s);
				}
			}
			if (y2 >= medY) {		// in top half
				if (x1 < medX) {	// in top left
					ul.addSphere(s);
				}
				if (x2 >= medX) {	// in top right
					ur.addSphere(s);
				}
			}
		}
	}

	// Add spheres that could cast shadows on one another
	public void addShadowSphere(Sphere s) {
		if (this.level == 0) {		// leaf
			if (this.sphereList == null) {
				this.sphereList = new SphereList(s, null);
			} else {
				this.sphereList = this.sphereList.add(s);
			}
		} else {
			// Get bounding box
			float medX = 0.5f * (this.minX + this.maxX);
			float medY = 0.5f * (this.minY + this.maxY);
			float r = s.getRadius();
	
			// u2-extent
			float a1 = s.getCenterShadow().getY() + r;
			float a2 = s.getCenterShadow().getY() - r;

			// u3-extent
			float b1 = s.getCenterShadow().getZ() + r;
			float b2 = s.getCenterShadow().getZ() - r;
	
			// Make sure a2 > a1 and b2 > b1
			if (a1 > a2) {
				float temp = a1;
				a1 = a2;
				a2 = temp; 
			}
			if (b1 > b2) {
				float temp = b1;
				b1 = b2;
				b2 = temp;
			}
	
			// Send down tree to bottom, adding sphere to each node it's in
			if (b1 < medY) {		// in bottom half
				if (a1 < medX) {	// in bottom left
					ll.addShadowSphere(s);
				}
				if (a2 >= medX) {	// in bottom right
					lr.addShadowSphere(s);
				}
			}
			if (b2 >= medY) {		// in top half
				if (a1 < medX) {	// in top left
					ul.addShadowSphere(s);
				}
				if (a2 >= medX) {	// in top right
					ur.addShadowSphere(s);
				}
			}
		}

	}

	// Return spheres that intersect a given point on the screen
	public SphereList getSpheres(float x, float y) {
		// Go down tree to leaves to find list of spheres
		if (this.level == 0) {
			return this.sphereList;
		}
		float medX = 0.5f * (this.minX + this.maxX);
		float medY = 0.5f * (this.minY + this.maxY);
			
		if ((this.minX <= x) && (x < medX)) {		// left half
			if ((this.minY <= y) && (y < medY)) {	// bottom half
				return ll.getSpheres(x, y);
			} else {
				return ul.getSpheres(x, y);
			}
		} else {									// right half
			if ((this.minY <= y) && (y < medY)) {	// bottom half
				return lr.getSpheres(x, y);
			} else {
				return ur.getSpheres(x, y);
			}
		}
	}

	public float sqr(float x) { return x * x; }
}
