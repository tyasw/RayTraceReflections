/*
 * nTuple.java
 * Created by: William Tyas
 * Date: 8/9/17
 * Description: A vector, with various methods to implement vector
 * operations.
 */
public class nTuple {
	private float x;
	private float y;
	private float z;

	public float getX() { return this.x; }

	public float getY() { return this.y; }

	public float getZ() { return this.z; }

	public nTuple() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}

	public nTuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z; 
	}

	public nTuple(nTuple other) {
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}

	public void setNTuple(nTuple other) {
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	} 

	public void setNTuple(float newX, float newY, float newZ) {
		this.x = newX;
		this.y = newY;
		this.z = newZ;
	}

	public int size() {
		return 3;
	}

	public void print() {
		System.out.println("(" + this.x + ", " + this.y + ", " + this.z + ")");
	}

	public boolean isEqual(nTuple other) {
		return ((this.x == other.getX()) && (this.y == other.getY()) && (this.z == other.getZ()));
	}

	//////////////////////////////////////////////////////////////////
	// BASIC VECTOR OPERATIONS										//
	// Dot product, scalar multiplication, addition, subtraction,	//
	// normalize 													//
	//////////////////////////////////////////////////////////////////
	public float dot(nTuple other) {
		float newX = this.getX() * other.getX();
		float newY = this.getY() * other.getY();
		float newZ = this.getZ() * other.getZ();
		return (newX + newY + newZ);
	}

	public nTuple scale(float factor) {
		float newX = this.x * factor;
		float newY = this.y * factor;
		float newZ = this.z * factor;
		return new nTuple(newX, newY, newZ);
	}

	// Compute the product of two matrices: a 3x3 and a 3x1
	public nTuple product(nTuple a, nTuple b, nTuple c) {
		float x = a.dot(this);
		float y = b.dot(this);
		float z = c.dot(this);
		return new nTuple(x, y, z);
	}

	public nTuple add(nTuple other) {
		float newX = this.x + other.getX();
		float newY = this.y + other.getY();
		float newZ = this.z + other.getZ();
		return new nTuple(newX, newY, newZ);
	}

	public nTuple subtract(nTuple other) {
		float newX = this.x - other.getX();
		float newY = this.y - other.getY();
		float newZ = this.z - other.getZ();
		return new nTuple(newX, newY, newZ);
	} 

	public float sqr(float x) { return x * x; }

	public nTuple normalize() {
		float len = (float) Math.sqrt(this.dot(this));
		return new nTuple(this.x / len, this.y / len, this.z / len); 
	}

	//////////////////////////////////////////////////////////////////
	// MATRIX TRANSFORMATIONS										//
	// Reflections, projections, rotations,							//
	// dilations/contractions, expansions/compressions, shears,		//
	// orthogonal projections on lines thru the origin, reflections	//
	// about lines thru the origin									//
	////////////////////////////////////////////////////////////////// 

	/*
	 * REFLECTIONS	
	 */
	public nTuple reflectXY() {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, -1.0f);
		return this.product(a, b, c);
	}

	public nTuple reflectXZ() {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, -1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	public nTuple reflectYZ() {
		nTuple a = new nTuple(-1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	/*
	 * ORTHOGONAL PROJECTIONS ON PLANES
	 */
	public nTuple projectXY() {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 0.0f);
		return this.product(a, b, c);
	}

	public nTuple projectXZ() {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 0.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	public nTuple projectYZ() {
		nTuple a = new nTuple(0.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	/*
	 * ROTATION OPERATORS
	 */
	public nTuple rotateX(float angle) {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, (float) Math.cos(angle), -1.0f * (float) Math.sin(angle));
		nTuple c = new nTuple(0.0f, (float) Math.sin(angle), (float) Math.cos(angle));
		return this.product(a, b, c);
	}

	public nTuple rotateY(float angle) {
		nTuple a = new nTuple((float) Math.cos(angle), 0.0f, (float) Math.sin(angle));
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(-1.0f * (float) Math.sin(angle), 0.0f, (float) Math.cos(angle));
		return this.product(a, b, c);
	}

	public nTuple rotateZ(float angle) {
		nTuple a = new nTuple((float) Math.cos(angle), -1.0f * (float) Math.sin(angle), 0.0f);
		nTuple b = new nTuple((float) Math.sin(angle), (float) Math.cos(angle), 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	// Rotate about any axis in R3, given a unit vector
	public nTuple rotateAxis(nTuple unitVector, float angle) {
		float uX = unitVector.getX();
		float uY = unitVector.getY();
		float uZ = unitVector.getZ();
		float cosAngle = (float) Math.cos(angle);
		float sinAngle = (float) Math.sin(angle);
		nTuple a = new nTuple(sqr(uX) * (1 - cosAngle) + cosAngle,
								uX * uY * (1 - cosAngle) - uZ * sinAngle,
								uX * uZ * (1 - cosAngle) + uY * sinAngle); 
		nTuple b = new nTuple(uX * uY * (1 - cosAngle) + uZ * sinAngle,
								sqr(uY) * (1 - cosAngle) + cosAngle,
								uY * uZ * (1 - cosAngle) - uX * sinAngle);
		nTuple c = new nTuple(uX * uZ * (1 - cosAngle) - uY * sinAngle,
								uY * uZ * (1 - cosAngle) + uX * sinAngle,
								sqr(uZ) * (1 - cosAngle) + cosAngle);
		return this.product(a, b, c);	
	}

	/*
	 * CONTRACTIONS/DILATIONS
	 */
	public nTuple contractDilate(float factor) {
		nTuple a = new nTuple(factor, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, factor, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, factor);
		return this.product(a, b, c);
	}

	/*
	 * COMPRESSIONS/EXPANSIONS
	 */
	public nTuple compressExpandX(float factor) {
		nTuple a = new nTuple(factor, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f); 
		return this.product(a, b, c);
	}	

	public nTuple compressExpandY(float factor) {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, factor, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, 1.0f);
		return this.product(a, b, c);
	}

	public nTuple compressExpandZ(float factor) {
		nTuple a = new nTuple(1.0f, 0.0f, 0.0f);
		nTuple b = new nTuple(0.0f, 1.0f, 0.0f);
		nTuple c = new nTuple(0.0f, 0.0f, factor);
		return this.product(a, b, c);
	}

	//////////////////////////////////////////////////////////////////
	// GAUSSIAN ELIMINATION											//
	//////////////////////////////////////////////////////////////////

	// Calculate the reduced row echelon form of a matrix
	public nTuple rref(float[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			float pivot = matrix[i][i];
			
			// Make sure pivot is 1
			if (pivot != 1.0f) {
				pivot = 1.0f / matrix[i][i];
				for (int j = 0; j < (matrix.length + 1); j++) {
					matrix[i][j] *= pivot;
				}
			}

			// Multiply other rows so that only one one in each column
			for (int j = 0; j < matrix.length; j++) {
				float value = matrix[j][i];
				if (j != i && value != 0) {
					float first = matrix[j][i];
					for (int el = 0; el < (matrix.length + 1); el++) {
						matrix[j][el] += -first * matrix[i][el];
					}
				}
			}
		}
		return new nTuple(matrix[0][3], matrix[1][3], matrix[2][3]);
	}

	// Find coordinates of d in coordinate system defined by basis
	// vectors a, b, and c
	public nTuple coordChange(nTuple a, nTuple b, nTuple c, nTuple d) {
		float[][] basisChange = new float[3][4];
		basisChange[0][0] = a.getX();
		basisChange[0][1] = b.getX();
		basisChange[0][2] = c.getX();
		basisChange[0][3] = d.getX();
		basisChange[1][0] = a.getY();
		basisChange[1][1] = b.getY();
		basisChange[1][2] = c.getY();
		basisChange[1][3] = d.getY();
		basisChange[2][0] = a.getZ();
		basisChange[2][1] = b.getZ();
		basisChange[2][2] = c.getZ();
		basisChange[2][3] = d.getZ();
		return this.rref(basisChange);
	}
}
