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
	public static final int WIDTH = 512;
	public static final int HEIGHT = 512;
	public static final nTuple BACKGROUND = new nTuple(0.4f, 0.6f, 0.8f);
	public static final nTuple LIGHT = new nTuple(1.0f, 1.0f, 1.0f).normalize();
	public static final nTuple LIGHT_BASIS_2 = new nTuple(5.0f, -3.0f, -2.0f).normalize();
	public static final nTuple LIGHT_BASIS_3 = new nTuple(1.0f, 7.0f, -8.0f).normalize();
	public static final float IMG_PLANE_SZ = 10.0f;
	public static final float CAM_Z = 20.0f;
	public static final int MAX_REFLECTION_DEPTH = 5;
	public static ArrayList<Sphere> spheres = new ArrayList<Sphere>();
	public static Quadtree tree;
	public static Quadtree shadowTree;
	public static boolean REFLECT = true;

	/*
	 * Main entry point
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		int numSpheres = howManySpheres(input);
		int treeDepth = howDeep(input);
		REFLECT = wantReflections(input);
		tree = new Quadtree(-IMG_PLANE_SZ,
							-IMG_PLANE_SZ,
							IMG_PLANE_SZ,
							IMG_PLANE_SZ,
							treeDepth,
							CAM_Z);
		shadowTree = new Quadtree(-IMG_PLANE_SZ * 5, 
									-IMG_PLANE_SZ * 5,
									IMG_PLANE_SZ * 5,
									IMG_PLANE_SZ * 5,
									treeDepth,
									CAM_Z);

		for (int i = 0; i < numSpheres; i++) {
			Sphere s = randSphere(LIGHT, LIGHT_BASIS_2, LIGHT_BASIS_3);
			tree.addSphere(s);
			shadowTree.addShadowSphere(s);
			spheres.add(s);
		}

		Statistics stats = new Statistics(spheres);
		stats.generateUsefulInfo();
		new RayTraceReflections();
	}

	public static int howManySpheres(Scanner input) {
		System.out.print("How many spheres do you want drawn? ");
		return input.nextInt();
	}

	public static int howDeep(Scanner input) {
		System.out.print("How deep do you want the quadtree to be? (Less than 10 recommended)? ");
		return input.nextInt();
	}

	public static boolean wantReflections(Scanner input) {
		System.out.print("Do you want reflections drawn (y/n)? ");
		String response = input.next();
		boolean answer = false;

		while (!response.equals("y") && !response.equals("yes") && !response.equals("n") && !response.equals("no")) {
			System.out.print("Please respond with one of the following: y, n, yes, or no:");
			response = input.next();
		}

		if (response.equals("y") || response.equals("yes")) {
			answer = true;	
		}

		return answer;
	}

	public static Sphere randSphere(nTuple u1, nTuple u2, nTuple u3) {
		float x = (float) Math.random() * 16.0f - 8.0f;
		float y = (float) Math.random() * 16.0f - 8.0f;
		float z = (float) Math.random() * 16.0f - 8.0f;
		float radius = 0.0f;
		if (REFLECT) {
			radius = (float) Math.random() * 0.4f + 0.2f;
		} else {
			radius = (float) Math.random() * 0.1f + 0.05f;
		}
		float r = (float) Math.random();
		float g = (float) Math.random();
		float b = (float) Math.random();
		return new Sphere(x, y, z, radius, r, g, b, u1, u2, u3);
	}

	public Color getColor(int x, int y) {
		nTuple p = new nTuple(0.0f, 0.0f, CAM_Z);	// camera point
		nTuple q = imagePlaneCoord(x, y);			// point on image plane
		ray ray = new ray(p, q.subtract(p), 0);
		double closestHit = Double.POSITIVE_INFINITY;
		float intersection = 0.0f;	// t-value for ray to intersect sphere
		Sphere closestSphere = null;
		SphereList intersectSpheres = tree.getSpheres(q.getX(), q.getY());

		while (intersectSpheres != null) { // Find closest sphere
			Sphere s = intersectSpheres.getSphere();
			intersection = ray.intersectSphere(s);
			if (intersection > 0.01f && intersection < closestHit) {
				closestHit = intersection;
				closestSphere = s;
			}
			intersectSpheres = intersectSpheres.getNext();
		}

		if (closestSphere != null) {
			nTuple IntPt = ray.pointAlongRay((float) closestHit);
			boolean inShadow = false;
			if (REFLECT) {
				return reflect(closestSphere, ray, IntPt);
			} else {
				inShadow = inShadow(IntPt);
				return closestSphere.shadeSphere(IntPt, LIGHT, inShadow);
			}
		} else {
			return new Color(BACKGROUND.getX(), BACKGROUND.getY(), BACKGROUND.getZ());
		}
	}

	/* Reflect
	 * Calculate reflections if desired, and then shade the sphere
	 * accordingly.
	 */
	public Color reflect(Sphere current, ray incident, nTuple point) {
		boolean inShadow = false;
		if (REFLECT && incident.getDepth() < MAX_REFLECTION_DEPTH) {
			nTuple n = point.subtract(current.getCenter()).normalize();
			nTuple incDir = incident.getVector();
			nTuple reflectDir = new nTuple(incDir.subtract(n.scale(2*(n.dot(incDir)))));
			ray reflection = new ray(point, reflectDir, incident.getDepth() + 1);

			double closestHit = Double.NEGATIVE_INFINITY;
			float intersection = 0.0f;
			Sphere closestSphere = null;
			for (int i = 0; i < spheres.size(); i++) {
				Sphere s = new Sphere(spheres.get(i));
				if (!s.isEqual(current)) {
					intersection = reflection.reflectIntersect(s);
					if (intersection < -0.01f && intersection > closestHit) {
						closestHit = intersection;
						closestSphere = new Sphere(s);
					}
				}
			}
			if (closestSphere != null) {
				nTuple intPt = reflection.pointAlongRay((float) closestHit);
				return reflect(closestSphere, reflection, intPt);
			} else {
				return new Color(BACKGROUND.getX() * 0.8f,
									BACKGROUND.getY() * 0.8f,
									BACKGROUND.getZ() * 0.8f);
			}
		}
		return current.shadeSphere(point, LIGHT, inShadow);
	}

	// Check if a point on a sphere is in shadow
	public boolean inShadow(nTuple point) {
		nTuple coords = new nTuple(point.coordChange(LIGHT,
														LIGHT_BASIS_2,
														LIGHT_BASIS_3,
														point));
		SphereList shadowIsect = shadowTree.getSpheres(coords.getY(), coords.getZ()); 
		ray shadowRay = new ray(new nTuple(), LIGHT, 0);	// terminal at origin makes math easier
		boolean inShadow = false;
		while (!inShadow && (shadowIsect != null)) {
			Sphere s = new Sphere(shadowIsect.getSphere());
			s.setCenter(s.getCenter().subtract(point));
			float intersection = shadowRay.intersectSphere(s);
			if (intersection > 0.0f) {
				inShadow = true;
			}
			shadowIsect = shadowIsect.getNext();
		}
		return inShadow;
	}

	public nTuple imagePlaneCoord(float u, float v) {
		return new nTuple(this.IMG_PLANE_SZ * (2*u/(float)WIDTH - 1),
							-1.0f * this.IMG_PLANE_SZ * (2*v/(float)HEIGHT - 1),
							0.0f);
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
        super("RayTracer");
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        addWindowListener(new WindowAdapter()
                          {public void windowClosing(WindowEvent e)
                          {dispose(); System.exit(0);}
                          }
        );
    }

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
