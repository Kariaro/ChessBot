package me.hardcoded.chess.open;

public class ScanMoveOld implements Comparable<ScanMoveOld> {
	public ScanOld sc;
	public double material;
	public Move move;
	
	public ScanMoveOld(ScanOld sc, double material, Move move) {
		this(material, move);
		this.sc = sc;
	}
	
	public ScanMoveOld(double material, Move move) {
		this.material = material;
		this.move = move;
	}
	
	public int compareTo(ScanMoveOld o) {
		return Double.compare(material, o.material);
	}
	
	public ScanMoveOld clone() {
		return new ScanMoveOld(material, move);
	}
	
	@Override
	public String toString() {
		return move.toString();
	}
}