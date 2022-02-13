package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.*;

import java.util.*;

public class AnalyserConverted7 {
	private static final int DEPTH = 3;
	
	private static Set<ChessMove> getAllMoves(ChessBoard board) {
		Set<ChessMove> moves = new LinkedHashSet<>();
		
		ChessGenerator.generate(board, false, (fromIdx, toIdx, special) -> {
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			moves.add(new ChessMove(board.pieces[fromIdx], fromIdx, toIdx, special));
			return true;
		});
		
		return moves;
	}
	
	public static int getMaterial(ChessBoard b) {
		int mat = 0;
		if (b.hasFlags(CastlingFlags.BLACK_CASTLE_K)) mat -= 6;
		if (b.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) mat -= 6;
		if (b.hasFlags(CastlingFlags.WHITE_CASTLE_K)) mat += 6;
		if (b.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) mat += 6;
		
		for (int i = 0; i < 64; i++) {
			int pieceId = b.getPiece(i);
			int val = Pieces.value(pieceId);
			mat += val;
			
			// Pushed pawns gets more points only if the row has no other pawn on it
			if (pieceId == Pieces.PAWN) {
				val += (int)(((i / 8) / 24.0) * 20);
			}
			
			if (pieceId == -Pieces.PAWN) {
				val -= (int)(((8 - (i / 8)) / 24.0) * 20);
			}
		}
		
		return mat;
	}
	
	public static ScanConverted analyse(ChessBoard board) {
		return analyseFrom(board.creteCopy(), DEPTH);
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static Random random = new Random();
	
	private static ScanConverted analyseFrom(ChessBoard board, int depth) {
		ScanConverted scan = analyseBranchMoves(board, depth, getAllMoves(board));
		customizeEnds(scan);
		return scan;
	}
	
	private static int non_developing(ChessBoard board) {
		int result = 0;
		if (board.getPiece(1) == Pieces.KNIGHT) result -= 10;
		if (board.getPiece(2) == Pieces.BISHOP) result -= 10;
		if (board.getPiece(5) == Pieces.BISHOP) result -= 10;
		if (board.getPiece(6) == Pieces.KNIGHT) result -= 10;
		if (board.getPiece(11) == Pieces.PAWN) result -= 11;
		if (board.getPiece(12) == Pieces.PAWN) result -= 11;
		if (board.getPiece(4) == Pieces.KING) result -= 8;
		
		if (board.getPiece(57) == -Pieces.KNIGHT) result += 10;
		if (board.getPiece(58) == -Pieces.BISHOP) result += 10;
		if (board.getPiece(61) == -Pieces.BISHOP) result += 10;
		if (board.getPiece(62) == -Pieces.KNIGHT) result += 10;
		if (board.getPiece(51) == -Pieces.PAWN) result += 11;
		if (board.getPiece(52) == -Pieces.PAWN) result += 11;
		if (board.getPiece(60) == -Pieces.KING) result += 8;
		
		return result * 3;
	}
	
	private static int un_developing(ChessMove move) {
		int id = move.piece;
		int move_to = move.to;
		int result = 0;
		
		switch (id) {
			case Pieces.KNIGHT -> {
				if (move_to == 1 || move_to == 6) {
					result -= 10;
				}
			}
			case -Pieces.KNIGHT -> {
				if (move_to == 57 || move_to == 62) {
					result += 10;
				}
			}
			case Pieces.BISHOP -> {
				if (move_to == 2 || move_to == 5) {
					result -= 10;
				}
			}
			case -Pieces.BISHOP -> {
				if (move_to == 58 || move_to == 61) {
					result += 10;
				}
			}
			case Pieces.QUEEN -> {
				result -= 5;
			}
			case -Pieces.QUEEN -> {
				result += 5;
			}
			case Pieces.KING -> {
				result -= 5;
			}
			case -Pieces.KING -> {
				result += 5;
			}
		}
		
		return result * 3;
	}
	
	private static ScanConverted analyseBranchMoves(ChessBoard board, int depth, Set<ChessMove> moves) {
		// Create a new scanner container
		ScanConverted scan = new ScanConverted(board);
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		if (depth == DEPTH) {
			log("---------------------------------------------------");
			log("Scanning...");
			log("Player  : %s", scan.white ? "white":"black");
		}
		
		for (ChessMove move : moves) {
			if (!ChessGenerator.playMove(board, move.from, move.to, move.special)) {
				continue;
			}
			
			double material = getMaterial(board);
			
			ScanConverted branch;
			if (depth < 1) {
				branch = new ScanConverted(board);
			} else {
				branch = analyseBranchMoves(board, depth - 1, getAllMoves(board));
			}
			
			double percent = ((branch.material()) * 95 + material * 5) / 100.0;
			percent = percent + un_developing(move) + non_developing(board);
			
			if ((move.special & 0b11_000000) == ChessPieceManager.SM_CASTLING) {
				percent += 20 * (board.isWhite() ? -1:1);
			}
			
			if (depth == DEPTH) {
				log("  move: %-10s (%2.2f) -> (%2.2f)", move, material / 100.0, percent / 100.0);
				log("      : (%s)", branch.follow);
			}
			
			// Add this move to the evaluation set
			scan.branches.add(new MoveConverted(branch, percent, move));
			
			// Undo the move and continue
			state.write(board);
		}
		
		evaluate(board, scan);
		state.write(board);
		return scan;
	}
	
	
	/**
	 * Add some custom and random moves to the bot
	 */
	private static void customizeEnds(ScanConverted scan) {
		List<MoveConverted> branches = scan.branches;
		if (branches.size() < 2) {
			return;
		}
		
		double score = scan.best.material;
		if (random.nextFloat() < 0.3) {
			if (!scan.white) {
				MoveConverted move = branches.get(branches.size() - 2);
				
				if (Math.abs(move.material - score) < 10) {
					scan.branches.remove(branches.size() - 1);
					scan.best = move;
				}
			} else {
				MoveConverted move = branches.get(1);
				if (Math.abs(move.material - score) < 10) {
					scan.branches.remove(0);
					scan.best = move;
				}
			}
		}
		
	}
	
	private static void evaluate(ChessBoard board, ScanConverted scan) {
		List<MoveConverted> evaluation = scan.branches;
		
		if (!evaluation.isEmpty()) {
			// Only sort the list if we have more than one move
			if (evaluation.size() > 1) {
				evaluation.sort(null);
			}
			
			// If it's white's turn then we want to pick the best move for black
			if (scan.white) {
				scan.best = evaluation.get(evaluation.size() - 1);
			} else {
				scan.best = evaluation.get(0);
			}
			
			for (MoveConverted m : scan.best.sc.follow) {
				scan.follow.add(m.createCopy());
			}
			
			scan.best.sc = null;
			scan.follow.add(scan.best);
		}
		
		if (ChessPieceManager.isKingAttacked(board, !board.isWhite())) {
			double delta = board.isWhite() ? 1:-1;
			scan.base += 10 * delta;
			
			if (evaluation.isEmpty()) {
				// Checkmate
				scan.base = -10000 * delta;
			}
		} else {
			if (evaluation.isEmpty()) {
				// Stalemate
				scan.base = 0;
			}
		}
		
		for (MoveConverted m : evaluation) {
			m.sc = null;
		}
	}
	
	public static class ScanConverted {
		public double base;
		public boolean draw;
		public final boolean white;
		public List<MoveConverted> branches = new ArrayList<>();
		public List<MoveConverted> follow = new ArrayList<>();
		List<Move> behind = new ArrayList<>();
		
		/**
		 * The predicted best move
		 */
		public MoveConverted best;
		
		public ScanConverted(ChessBoard board) {
			this.base = getMaterial(board);
			this.white = board.isWhite();
		}
		
		public ScanConverted(ChessBoard board, Set<Move> enemy, Set<Move> moves) {
			this.base = getMaterial(board);
			this.white = board.isWhite();
		}
		
		public double material() {
			if (draw) {
				return 0;
			}
			
			if (best != null) {
				return best.material;
			}
			
			return base;
		}
	}
	
	public static class MoveConverted implements Comparable<MoveConverted> {
		public ScanConverted sc;
		public double material;
		public ChessMove move;
		
		public MoveConverted(ScanConverted sc, double material, ChessMove move) {
			this(material, move);
			this.sc = sc;
		}
		
		public MoveConverted(double material, ChessMove move) {
			this.material = material;
			this.move = move;
		}
		
		public int compareTo(MoveConverted o) {
			return Double.compare(material, o.material);
		}
		
		public MoveConverted createCopy() {
			return new MoveConverted(material, move);
		}
		
		@Override
		public String toString() {
			return move.toString();
		}
	}
}
