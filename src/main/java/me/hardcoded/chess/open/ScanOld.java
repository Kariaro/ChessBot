package me.hardcoded.chess.open;

import me.hardcoded.chess.open2.Chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScanOld {
	/**
	 * The material of the current board
	 */
	public double base;
	
	public boolean draw;
	
	/**
	 * If it is white to move
	 */
	public final boolean white;
	
	/**
	 * The list of branches
	 */
	public List<ScanMoveOld> branches = new ArrayList<>();
	
	List<Move> behind = new ArrayList<>();
	public List<ScanMoveOld> follow = new ArrayList<>();
	
	public int[] attacks = new int[64];
	
	/**
	 * The predicted best move
	 */
	public ScanMoveOld best;
	
	public static double getMaterial(Chess b) {
		double mat = 0;
		for(int i = 0; i < 64; i++) {
			int pieceId = b.getPieceAt(i);
			double val = Pieces.value(pieceId);
			
			if(pieceId == Pieces.PAWN) {
				val += (i / 8) / 24.0;
			}
			
			if(pieceId == -Pieces.PAWN) {
				val -= (8 - (i / 8)) / 24.0;
			}
			mat += val;
		}
		
		return mat;
	}
	
	public ScanOld(Chess board) {
		this.base = getMaterial(board);
		this.white = board.isWhiteTurn();
	}
	
	public ScanOld(Chess board, Set<Move> enemy, Set<Move> moves) {
		this.base = getMaterial(board);
		this.white = board.isWhiteTurn();
		
		for(Move move : moves) {
			attacks[move.to()] += move.attack() ? 1:0;
		}
		
		for(Move move : enemy) {
			attacks[move.to()] -= move.attack() ? 1:0;
		}
	}
	
	public double material() {
		if(draw) return 0;
		if(best != null) {
			return best.material;
		}
		
		return base;
	}
}
