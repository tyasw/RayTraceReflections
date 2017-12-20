/**
 * RayTraceReflections illustrates some basics of Java 2D.
 * This version is compliant with Java 1.2 Beta 3, Jun 1998.
 * Please refer to: <BR>
 * http://www.javaworld.com/javaworld/jw-07-1998/jw-07-media.html
 * <P>
 * @author Bill Day <bill.day@javaworld.com>
 * @version 1.0
 * @see java.awt.Graphics2D
**/

/**
Geoffrey Matthews modified this code to show how to make
an image pixel by pixel.
13 April 2017
**/

/**
William Tyas modified this code to implement a ray tracer.
9 August 2017
**/

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class RayTraceReflections extends Frame {
    public static final int WIDTH = 1024;
	public static final int MAX_REFLECTION_DEPTH = 5;
    public static final int HEIGHT = 1024;
	public static nTuple background = new nTuple(0.4f, 0.6f, 0.8f);
	public static Quadtree tree;
	public static Quadtree shadowTree;
	public static ArrayList<Sphere> spheres = new ArrayList<Sphere>();
	public static nTuple light;
	public static nTuple lightBasis2;
	public static nTuple lightBasis3;
	public static int numSpheres;
	public static int treeDepth = 8;	
	public static float s = 10.0f;		// img plane size
	public static float camZ = 20.0f;	// camera position 
	public static boolean wantReflect = true;

	public static void main(String[] args) {
		tree = new Quadtree(-s, -s, s, s, treeDepth, camZ);
		shadowTree = new Quadtree(-s*5, -s*5, s*5, s*5, treeDepth, camZ);
		numSpheres = getUserParameters();
		System.out.println("Drawing " + numSpheres + " spheres.");
		light = new nTuple(1.0f, 1.0f, 1.0f).normalize();
		lightBasis2 = new nTuple(5.0f, -3.0f, -2.0f).normalize();
		lightBasis3 = new nTuple(1.0f, 7.0f, -8.0f).normalize();

		for (int i = 0; i < numSpheres; i++) {
			Sphere s = randSphere(light, lightBasis2, lightBasis3);
			tree.addSphere(s);
			shadowTree.addShadowSphere(s);
			spheres.add(s);
		}
		new RayTraceReflections();
	}

	/* Let user decide how many spheres to draw, and if relections 
	 * should be drawn
	 */
	public static int getUserParameters() {
		Scanner input = new Scanner(System.in);
		System.out.print("How many spheres do you want drawn? ");
		int numSpheres = input.nextInt();
		System.out.print("Do you want reflections drawn (yes/no)? ");
		if (input.next().equals("no")) {
			wantReflect = false;
		}
		return numSpheres;
	}

	public static Sphere randSphere(nTuple u1, nTuple u2, nTuple u3) {
		float x = (float) Math.random() * 16.0f - 8.0f;
		float y = (float) Math.random() * 16.0f - 8.0f;
		float z = (float) Math.random() * 16.0f - 8.0f;
		float radius = 1.0f;
		//float radius = (float) Math.random() * 0.1f + 0.05f;
		float r = (float) Math.random();
		float g = (float) Math.random();
		float b = (float) Math.random();
		return new Sphere(x, y, z, radius, r, g, b, u1, u2, u3);
	}

	// Find pixel color
	public Color getColor(int x, int y) {
		nTuple p = new nTuple(0.0f, 0.0f, camZ);	// camera point
		nTuple q = imagePlaneCoord(x, y);			// point on image plane
		ray ray = new ray(p, q.subtract(p), 0);
		double closestHit = Double.POSITIVE_INFINITY;
		float intersection = 0.0f;	// t-value for ray to intersect sphere
		Sphere closestSphere = null;
		SphereList isectSpheres = tree.getSpheres(q.getX(), q.getY());

		while (isectSpheres != null) { // Find closest sphere
			Sphere next = isectSpheres.getSphere();
			intersection = ray.intersectSphere(next);
			if (intersection > 0.01f && intersection < closestHit) {
				closestHit = intersection;
				closestSphere = next;
			}
			isectSpheres = isectSpheres.getNext();
		}

		if (closestSphere != null) {
			nTuple IntPt = ray.pointAlongRay((float) closestHit);
			boolean inShadow = false;
			return reflect(closestSphere, ray, IntPt);
			//boolean inShadow = inShadow(IntPt);
			//return closestSphere.shadeSphere(IntPt, light, inShadow);
		} else {
			return new Color(background.getX(), background.getY(), background.getZ());
		}
	}

	/* Reflect
	 * Calculate reflections if desired, and then shade the sphere
	 * accordingly.
	 */
	public Color reflect(Sphere s, ray incident, nTuple point) {
		if (wantReflect && incident.getDepth() < MAX_REFLECTION_DEPTH) {
			nTuple n = point.subtract(s.getCenter()).normalize();
			nTuple incDir = incident.getVector();
			nTuple reflectDir = new nTuple(incDir.subtract(n.scale(2*(n.dot(incDir)))));
			ray reflection = new ray(point, reflectDir, incident.getDepth() + 1);

			double closestHit = Double.NEGATIVE_INFINITY;
			float intersection = 0.0f;
			Sphere closestSphere = null;
			for (int i = 0; i < spheres.size(); i++) {
				Sphere next = new Sphere(spheres.get(i));
				if (!next.isEqual(s)) {
					intersection = reflection.reflectIntersect(next);
					if (intersection < -0.01f && intersection > closestHit) {
						closestHit = intersection;
						closestSphere = new Sphere(next);
					}
				}
			}
			if (closestSphere != null) {
				nTuple intPt = reflection.pointAlongRay((float) closestHit);
				return reflect(closestSphere, reflection, intPt);
			} else {
				return new Color(background.getX() * 0.8f, background.getY() * 0.8f, background.getZ() * 0.8f);
			}
		}
		return s.shadeSphere(point, light, false);
	}

	// Check if a point on a sphere is in shadow
	public boolean inShadow(nTuple point) {
		nTuple coords = new nTuple(point.coordChange(light, lightBasis2, lightBasis3, point));
		SphereList shadowIsect = shadowTree.getSpheres(coords.getY(), coords.getZ()); 
		ray shadowRay = new ray(new nTuple(), light, 0);	// terminal at origin makes math easier
		boolean inShadow = false;
		while (!inShadow && (shadowIsect != null)) {
			Sphere next = new Sphere(shadowIsect.getSphere());
			next.setCenter(next.getCenter().subtract(point));
			float intersection = shadowRay.intersectSphere(next);
			if (intersection > 0.0f) {
				inShadow = true;
			}
			shadowIsect = shadowIsect.getNext();
		}
		return inShadow;
	}

	public nTuple imagePlaneCoord(float u, float v) {
		return new nTuple(this.s * (2*u/(float)WIDTH - 1), -1.0f * this.s * (2*v/(float)HEIGHT - 1), 0.0f);
	}

    /**
     * Instantiates an RayTraceReflection object.
     **/

    /**
     * Our RayTraceReflections constructor sets the frame's size, adds the
     * visual components, and then makes them visible to the user.
     * It uses an adapter class to deal with the user closing
     * the frame.
     **/
    public RayTraceReflections() {
        //Title our frame.
        super("RayTracer");

        //Set the size for the frame.
        setSize(WIDTH, HEIGHT);

        //We need to turn on the visibility of our frame
        //by setting the Visible parameter to true.
        setVisible(true);

        //Now, we want to be sure we properly dispose of resources
        //this frame is using when the window is closed.  We use
        //an anonymous inner class adapter for this.
        addWindowListener(new WindowAdapter()
                          {public void windowClosing(WindowEvent e)
                          {dispose(); System.exit(0);}
                          }
        );
    }

    public int getWIDTH() { return this.WIDTH; }

    public int getHEIGHT() { return this.HEIGHT; }

    /**
     * The paint method provides the real magic.  Here we
     * cast the Graphics object to Graphics2D to illustrate
     * that we may use the same old graphics capabilities with
     * Graphics2D that we are used to using with Graphics.
     **/
    public void paint(Graphics g) {
		for (int u = 0; u < WIDTH; u++) {
			for (int v = 0; v < HEIGHT; v++) {
        		g.setColor(getColor(u, v));
        		g.drawLine(u, v, u, v);
			}
		}
    }
}

