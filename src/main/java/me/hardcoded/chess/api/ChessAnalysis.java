package me.hardcoded.chess.api;

/**
 * This class contains information about an analysed chess board.
 *
 * @author HardCoded
 */
public class ChessAnalysis {
	/**
	 * This field contains the best move computed from a {@link ChessAnalyser}.
	 */
	private ChessMove move;
	
	/**
	 * This field contains the computed material of the board.
	 */
	private int material;
	
	public ChessAnalysis() {
	
	}
	
	public ChessMove getBestMove() {
		return move;
	}
	
	public void setBestMove(ChessMove move) {
		this.move = move;
	}
	
	public int getMaterial() {
		return material;
	}
	
	public void setMaterial(int material) {
		this.material = material;
	}
}
