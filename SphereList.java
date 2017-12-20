public class SphereList {
	private Sphere current;
	private SphereList next;

	public SphereList(Sphere s, SphereList next) {
		this.current = s;
		this.next = next;
	}

	public SphereList add(Sphere s) {
		return new SphereList(s, this);
	}

	public Sphere getSphere() {
		return this.current;
	}

	public SphereList getNext() {
		return this.next;
	}

	public int length() {
		if (this.next == null) {
			return 1;
		} else {
			return 1 + this.next.length();
		}
	}
}
