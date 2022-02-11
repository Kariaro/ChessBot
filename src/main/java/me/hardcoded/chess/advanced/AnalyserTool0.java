package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.*;

import java.util.*;

public class AnalyserTool0 {
	public static class Move {
		int from;
		int to;
		int special;
		private int piece;
		private boolean attack;
		private boolean castle;
		
		public Move(ChessBoard board, int from, int to, int special) {
			this.from = from;
			this.to = to;
			this.special = special;
			this.piece = board.pieces[from];
			
			int type = special & 0b11_0000000;
			
			this.attack = switch (type) {
				case ChessPieceManager.SM_NORMAL -> board.pieces[to] != Pieces.NONE;
				case ChessPieceManager.SM_EN_PASSANT, ChessPieceManager.SM_CASTLING -> true;
				case ChessPieceManager.SM_PROMOTION -> castle = true;
				default -> false;
			};
		}
		
		public int to() {
			return to;
		}
		
		public int from() {
			return from;
		}
		
		public boolean attack() {
			return attack;
		}
	}
	private static Set<Move> getAllMoves(ChessBoard board) {
		Set<Move> moves = new HashSet<>();
		
		ChessGenerator.generate(board, (fromIdx, toIdx, special) -> {
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			moves.add(new Move(board, fromIdx, toIdx, special));
			return true;
		});
		
//		boolean turn = board.isWhite();
//		for (int i = 0; i < 64; i++) {
//			int piece = board.getPieceAt(i);
//			if(turn && piece < 1 || (!turn && piece > -1)) continue;
//
//			moves.addAll(board.getPieceMoves(i));
//		}
		
		return moves;
	}
	
	public static int getMaterial(ChessBoard board) {
		int mat = 0;
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_K)) mat -= 6;
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) mat -= 6;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_K)) mat -= 6;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) mat -= 6;
		
		long mask = board.pieceMask;
		int material = 0;
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			int piece = board.pieces[idx];
			int val = Pieces.value(piece);
			material += val;
			
//			if (piece == Pieces.PAWN) {
//				val += (int)(((idx >>> 3) / 24.0) * 20);
//			} else if (piece == -Pieces.PAWN) {
//				val -= (int)(((8 - (idx >>> 3)) / 24.0) * 20);
//			}
		}
		
		return material;
	}
	
	public static Scan0 analyse(ChessBoard board) {
		System.out.println(board.creteCopy());
		return analyseFrom(board.creteCopy(), DEPTH);
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static final int DEPTH = 4;
	
	private static Scan0 analyseFrom(ChessBoard board, int depth) {
		return analyseBranchMoves(board, depth, getAllMoves(board));
	}
	
	private static int un_developing(Move move) {
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
	
	private static Scan0 analyseBranchMoves(ChessBoard board, int depth, Set<Move> moves) {
		// Create a new scanner container
		Scan0 scan = new Scan0(board);
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		
//		if (depth == DEPTH) {
//			log("---------------------------------------------------");
//			log("Scanning...");
//			log("Player  : %s", scan.white ? "white":"black");
//		}
		
		for (Move move : moves) {
			if (!ChessGenerator.playMove(board,move.from, move.to, move.special)) {
				continue;
			}
			
			double material = getMaterial(board);
			
			Scan0 branch;
			if (depth < 1) {
				branch = new Scan0(board);
			} else {
				branch = analyseBranchMoves(board, depth - 1, getAllMoves(board));
			}
			
			double percent = ((branch.material()) * 95 + material * 5) / 100.0;
			percent = percent + un_developing(move) + non_developing(board);
			
			if (move.castle) {
				percent += 20 * (board.isWhite() ? -1:1);
			}
			
//			if (depth == DEPTH) {
//				log("  move: %-10s (%2.2f) -> (%2.2f)", move, material / 100.0, percent / 100.0);
//				log("      : (%s)", branch.follow);
//			}
			
			// Add this move to the evaluation set
			scan.branches.add(new Move0(branch, percent, move));
			
			// Undo the move and continue
			state.write(board);
		}
		
		evaluate(board, scan);
		state.write(board);
		return scan;
	}
	
	private static void evaluate(ChessBoard board, Scan0 scan) {
		List<Move0> evaluation = scan.branches;
		
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
			
			for (Move0 m : scan.best.sc.follow) {
				scan.follow.add(m.createCopy());
			}
			
			scan.best.sc = null;
			scan.follow.add(scan.best);
		}
		
		// TODO: This might be wrong
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
		
		for (Move0 m : evaluation) {
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
		
		public Move0 createCopy() {
			return new Move0(material, move);
		}
		
		@Override
		public String toString() {
			return move.toString();
		}
	}
	
	public static class Scan0 {
		public double base;
		public boolean draw;
		public final boolean white;
		public List<Move0> branches = new ArrayList<>();
		List<Move> behind = new ArrayList<>();
		public List<Move0> follow = new ArrayList<>();
		public int[] attacks = new int[64];
		public Move0 best;
		
		public Scan0(ChessBoard board) {
			this.base = getMaterial(board);
			this.white = board.isWhite();
		}
		
		public Scan0(ChessBoard board, Set<Move> enemy, Set<Move> moves) {
			this.base = getMaterial(board);
			this.white = board.isWhite();
			
			for (Move move : moves) {
				attacks[move.to()] += move.attack() ? 1 : 0;
			}
			
			for (Move move : enemy) {
				attacks[move.to()] -= move.attack() ? 1 : 0;
			}
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
}
