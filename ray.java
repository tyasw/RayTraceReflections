/*
 * ray.java
 * Created by: William Tyas
 * Date: 8/9/17
 * Description: A ray used for raytracing.
 */
public class ray {
	private nTuple camera;	// terminal
	private nTuple vector;	// direction
	private int reflectDepth;	// Reflection depth

	public nTuple getCamera() { return this.camera; }

	public nTuple getVector() { return this.vector; }

	public int getDepth() { return this.reflectDepth; }

	public ray(nTuple p, nTuple v, int depth) {
		this.camera = p;
		this.vector = v.normalize();
		this.reflectDepth = depth;
	}

	public void setDepth(int newDepth) {
		this.reflectDepth = newDepth;
	}

	public float intersectSphere(Sphere s) {
		// Move sphere and ray by same amount to simplify math
		nTuple q = this.camera.subtract(s.getCenter());

		float a = 1.0f;		// dot product of normalized vector with itself 
		float b = 2.0f * (q.dot(this.vector));
		float c = q.dot(q) - (s.getRadius() * s.getRadius());
		float discriminant = (b*b) - 4*a*c;

		if (discriminant < 0.0f) {		// no intersection
			return -1.0f;
		} else {			// no need to check both solutions, this is always largest  
			return (0.5f * (-b - (float) Math.sqrt(discriminant)));
		}
	}

	public float intersectShadowSphere(Sphere s) {
		nTuple q = this.camera.subtract(s.getCenter());
		
		float a = 1.0f;
		float b = 2.0f * (q.dot(this.vector));
		float c = q.dot(q) - (s.getRadius() * s.getRadius());
		float discriminant = (b*b) - 4*a*c;

		if (discriminant < 0.0f) {
			return 1.0f;
		} else {
			float value1 = 0.5f * (-b - (float) Math.sqrt(discriminant));
			float value2 = 0.5f * (-b + (float) Math.sqrt(discriminant));
			if (value2 < value1) {
				return value2;
			}
			return value1;
		}
	}

	public float reflectIntersect(Sphere s) {
		nTuple q = this.camera.subtract(s.getCenter());
		
		float a = 1.0f;
		float b = 2.0f * (q.dot(this.vector));
		float c = q.dot(q) - (s.getRadius() * s.getRadius());
		float discriminant = (b*b) - 4*a*c;

		if (discriminant < 0.0f) {
			return 1.0f;
		} else {
			float value1 = 0.5f * (-b - (float) Math.sqrt(discriminant));
			float value2 = 0.5f * (-b + (float) Math.sqrt(discriminant));
			if (value2 < value1) {
				return value2;
			}
			return value1;
		}
	}

	public nTuple pointAlongRay(float t) {
		return this.camera.add(this.vector.scale(t));
	}
}
