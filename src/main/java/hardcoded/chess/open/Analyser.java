package hardcoded.chess.open;

import java.util.*;

import hardcoded.chess.open2.Chess;

public class Analyser {
	private static Set<Move> getAllMoves(Chess b) {
		Set<Move> moves = new HashSet<>();
		
		for(int i = 0; i < 64; i++) {
			moves.addAll(b.getPieceMoves(i));
		}
		
		return moves;
	}
	
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
	
	public static Scan0 analyse(Chess board) {
		Scan0 scan = analyseFrom(board.clone(), DEPTH);
		return scan;
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static final int DEPTH = 3;
	
	private static Scan0 analyseFrom(Chess board, int depth) {
		return analyseBranchMoves(board, depth, getAllMoves(board));
	}
	
	private static Scan0 analyseBranchMoves(Chess board, int depth, Set<Move> moves) {
		// Create a new scanner container
		Scan0 scan = new Scan0(board);
		if(depth < 0) return scan;
		
		// Save the last state of the board
		State state = board.getState();
		
		if(depth == DEPTH) {
			log("---------------------------------------------------");
			log("Scanning...");
			log("Player  : %s", scan.white ? "white":"black");
		}
		
		for(Move move : moves) {
			board.doMove(move);
			
			double material = getMaterial(board);
			Scan0 branch;
			if(depth < 1) {
				branch = new Scan0(board);
			} else {
				branch = analyseBranchMoves(board, depth - 1, getAllMoves(board));
			}
			
			// TODO: Discard bad moves
			double percent = ((branch.material()) * 0.9 + material * 0.1);
			if(Math.abs(move.id()) != Pieces.PAWN) {
				percent += 0.11 * (scan.white ? 1:-1);
			}
			
			if(depth == DEPTH) {
				log("  move: %-10s (%2.4f) -> (%2.4f)", move, material, percent);
				log("      : (%s)", branch.follow);
			}
			
			// Add this move to the evaluation set
			scan.branches.add(new Move0(branch, percent, move));
			
			// Undo the move and continue
			board.setState(state);
		}
		
		evaluate(board, scan);
		board.setState(state);
		if(depth == DEPTH) {
			log("Material: %.4f", scan.base);
		}
		
		return scan;
	}
	
	private static void evaluate(Chess board, Scan0 scan) {
		List<Move0> evaluation = scan.branches;
		
		if(!evaluation.isEmpty()) {
			// Only sort the list if we have more than one move
			if(evaluation.size() > 1) {
				evaluation.sort(null);
			}
			
			// If it's white's turn then we want to pick the best move for black
			if(scan.white) {
				scan.best = evaluation.get(evaluation.size() - 1);
			} else {
				scan.best = evaluation.get(0);
			}
			
			for(Move0 m : scan.best.sc.follow) {
				scan.follow.add(m.clone());
			}
			
			scan.best.sc = null;
			scan.follow.add(scan.best);
		}
		
		if(board.isChecked(!board.isWhiteTurn())) {
			double delta = board.isWhiteTurn() ? 1:-1;
			scan.base += 0.1 * delta;
			
			if(evaluation.isEmpty()) {
				// Checkmate
				scan.base = -100 * delta;
			}
		} else {
			if(evaluation.isEmpty()) {
				// Stalemate
				scan.base = 0;
			}
		}
		
		for(Move0 m : evaluation) {
			m.sc = null;
		}
	}
	
	public static class Move0 implements Comparable<Move0> {
		public Scan0 sc;
		public double material;
		public Move move;
		
		public Move0(Scan0 sc, double material, Move move) {
			this(material, move);
			this.sc = sc;
		}
		
		public Move0(double material, Move move) {
			this.material = material;
			this.move = move;
		}
		
		public int compareTo(Move0 o) {
			return Double.compare(material, o.material);
		}
		
		public Move0 clone() {
			return new Move0(material, move);
		}
		
		@Override
		public String toString() {
			return move.toString();
		}
	}
	
	public static class Scan0 {
		/**
		 * The material of the current board
		 */
		public double base;
		
		/**
		 * If it is white to move
		 */
		public final boolean white;
		
		/**
		 * The list of branches
		 */
		public List<Move0> branches = new ArrayList<>();
		
		List<Move> behind = new ArrayList<>();
		public List<Move0> follow = new ArrayList<>();
		
		public int[] attacks = new int[64];
		
		/**
		 * The predicted best move
		 */
		public Move0 best;
		
		public Scan0(Chess board) {
			this.base = getMaterial(board);
			this.white = board.isWhiteTurn();
		}
		
		public Scan0(Chess board, Set<Move> enemy, Set<Move> moves) {
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
			if(best != null) {
				return best.material;
			}
			
			return base;
		}
	}
}
