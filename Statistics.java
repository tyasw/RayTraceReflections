/* Statistics.java
 * Created by William Tyas
 * 1/6/18
 *
 * This class contains methods to calculate the statistics associated
 * with spheres used in the ray tracer.
 */
import java.util.*;
import java.io.*;

public class Statistics {
	private ArrayList<Sphere> spheres;

	public Statistics(ArrayList<Sphere> spheres) {
		this.spheres = spheres;
	}

	/*
	 * Generates a csv file containing the (x, y, z) coordinates of the
	 * center of each sphere in the ray tracer.
	 */
	public void generateUsefulInfo() {
		try {
			PrintStream output = new PrintStream(new File("stats.csv"));
			output.println("x,y,z");

			for (int i = 0; i < spheres.size(); i++) {
				output.println(spheres.get(i));
			}

		} catch (FileNotFoundException f) {
			System.err.println("File could not be created.");
		}
	}
}
