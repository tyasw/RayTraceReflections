/*
 * Sphere.java
 * Created by: William Tyas
 * Date: 8/9/17
 * Description: A sphere object, with methods to modify the sphere's
 * attributes
 */
import java.awt.*;

public class Sphere {
	private nTuple center;
	private nTuple center_shadow;		// coords in shadow coord sys
	private float radius;
	private nTuple color;
	private float ambFactor = 0.1f;		// ambient lighting

	public nTuple getCenter() { return this.center; }

	public nTuple getCenterShadow() { return this.center_shadow; }

	public float getRadius() { return this.radius; }

	public nTuple getColor() { return this.color; }

	public void print() {
		System.out.println("Center: (" + this.center.getX() + ", " + this.center.getY() + ", " + this.center.getZ() + ")");
		System.out.println("Radius: " + this.radius); 
		System.out.println("Color: (" + this.color.getX() + ", " + this.color.getY() + ", " + this.color.getZ() + ")");
	}

	public Sphere(float x, float y, float z, float radius, float r,
				float g, float b, nTuple lightBasis1, nTuple lightBasis2, nTuple lightBasis3) {
		this.center = new nTuple(x, y, z);
		this.center_shadow = this.center.coordChange(lightBasis1, lightBasis2, lightBasis3, this.center); 
		this.radius = radius;
		this.color = new nTuple(r, g, b);
	}

	public Sphere(Sphere other) {
		this.center = new nTuple(other.getCenter());
		this.center_shadow = new nTuple(other.getCenterShadow());
		this.radius = other.getRadius();
		this.color = new nTuple(other.getColor());
	}

	public boolean isEqual(Sphere other) {
		return (this.center.isEqual(other.getCenter()) && (this.radius == other.getRadius()) && (this.color.isEqual(other.getColor())));
	}

	//////////////////////////////////////////////////////////////////
	//						SPHERE MODIFICATIONS					//
	//////////////////////////////////////////////////////////////////
	public void setSphere(Sphere other) {
		this.center.setNTuple(other.getCenter());
		this.center_shadow.setNTuple(other.getCenterShadow());
		this.radius = other.getRadius();
		this.color.setNTuple(other.getColor());
	}
	
	public void setCenter(nTuple other) {
		this.center.setNTuple(other);
	}

	public void setCenter(float newX, float newY, float newZ) {
		this.center.setNTuple(newX, newY, newZ);
	}

	public void setColor(nTuple newColor) {
		this.color.setNTuple(newColor);
	}

	public void reflectXY() {
		this.center.setNTuple(this.center.reflectXY());
	}

	public void reflectXZ() {
		this.center.setNTuple(this.center.reflectXZ());
	}

	public void reflectYZ() {
		this.center.setNTuple(this.center.reflectYZ());
	}

	public void projectXY() {
		this.center.setNTuple(this.center.projectXY());
	}

	public void projectXZ() {
		this.center.setNTuple(this.center.projectXZ());
	}

	public void projectYZ() {
		this.center.setNTuple(this.center.projectYZ());
	}

	public void rotateX(float angle) {
		this.center.setNTuple(this.center.rotateX(angle));
	}

	public void rotateY(float angle) {
		this.center.setNTuple(this.center.rotateY(angle));
	}

	public void rotateZ(float angle) {
		this.center.setNTuple(this.center.rotateZ(angle));
	}

	public void rotateAxis(nTuple unit, float angle) {
		this.center.setNTuple(this.center.rotateAxis(unit, angle));
	}

	public Color shadeSphere(nTuple point, nTuple light, boolean inShadow) {
		//return new Color(this.color.getX(), this.color.getY(), this.color.getZ());
		return lambertian(point, light, inShadow);
	}

	// Lambertian shading
	public Color lambertian(nTuple point, nTuple light, boolean inShadow) {
		float cosVectors = ambFactor;
		if (!inShadow) {
			nTuple n = point.subtract(this.center).normalize();	// surface normal
			cosVectors = n.dot(light);
			if (cosVectors < ambFactor) {
				cosVectors = ambFactor;
			}
		}
		return new Color(cosVectors * this.color.getX(), cosVectors * this.color.getY(), cosVectors * this.color.getZ());
	}
}
