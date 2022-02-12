package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.Pieces;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyserTool3 {
	private static Move[] getAllMoves(ChessBoard board) {
		final Move[] moves = new Move[96];
		final int[] ptr = new int[1];
		
		ChessGenerator.generate(board, false, (fromIdx, toIdx, special) -> {
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			int next = ptr[0];
			moves[next] = new Move(board.pieces[fromIdx], fromIdx, toIdx, special);
			ptr[0] = next + 1;
			return true;
		});
		
		return moves;
	}
	
	public static int getMaterial(ChessBoard board) {
		long mask = board.pieceMask;
		int material = 0;
		
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_K)) material -= 6;
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) material -= 6;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_K)) material += 6;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) material += 6;
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			int piece = board.pieces[idx];
			int val = Pieces.value(piece);
			
			if (piece == Pieces.PAWN) {
				val += (int)(((idx >>> 3) / 24.0) * 5);
			} else if (piece == -Pieces.PAWN) {
				val -= (int)(((8 - (idx >>> 3)) / 24.0) * 5);
			}
			material += val;
		}
		
		return material;
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
	
	// TODO: Even numbers will mess up the algorithm
	private static final int DEPTH = 4;
	public static Scanner analyse(ChessBoard board) {
		ChessBoard copy = board.creteCopy();
		System.out.println(copy);
		return analyseBranchMoves(copy, DEPTH, getMaterial(copy));
	}
	
	private static Scanner analyseBranchMoves(ChessBoard board, int depth, double currentMaterial) {
		// Create a new scanner container
		Scanner scan = new Scanner(board, currentMaterial);
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board);
		
		int idx = 0;
		for (Move move : moves) {
			if (move == null || !ChessGenerator.playMove(board, move.from, move.to, move.special)) {
				continue;
			}
			idx++;
			
			double boardMaterial = getMaterial(board);
			
			Scanner branch;
			if (depth < 1) {
				branch = new Scanner(board, boardMaterial);
			} else {
				branch = analyseBranchMoves(board, depth - 1, boardMaterial);
			}
			
			double percent = ((branch.material()) * 80 + boardMaterial * 20) / 100.0;
			percent += un_developing(move) + non_developing(board);
			if (move.castle) {
				percent += 30 * (board.isWhite() ? -1 : 1);
			}
			
			move.material = percent;
			
			// Add this move to the evaluation set
			if (board.isWhite()) {
				if (scan.best == null || scan.best.material > percent) {
					scan.best = move;
				}
			} else {
				if (scan.best == null || scan.best.material < percent) {
					scan.best = move;
				}
			}
			
			// Undo the move and continue
			state.write(board);
		}
		
		evaluate(board, scan);
		state.write(board);
		return scan;
	}
	
	private static void evaluate(ChessBoard board, Scanner scan) {
//		List<Move> evaluation = scan.branches;
//
//		if (!evaluation.isEmpty()) {
//			// If it's white's turn then we want to pick the best move for black
//			Move bestMove = null;
//			double bestScore;
//			if (scan.white) {
//				bestScore = -100000;
//				for (Move m : evaluation) {
//					if (m.material > bestScore) {
//						bestScore = m.material;
//						bestMove = m;
//					}
//				}
//			} else {
//				bestScore = 100000;
//				for (Move m : evaluation) {
//					if (m.material < bestScore) {
//						bestScore = m.material;
//						bestMove = m;
//					}
//				}
//			}
//
//			scan.best = bestMove;
//		}
		
		// TODO: This might be wrong
		if (ChessPieceManager.isKingAttacked(board, !board.isWhite())) {
			double delta = board.isWhite() ? 1:-1;
			scan.base += 10 * delta;
			
			if (scan.best == null) { // evaluation.isEmpty()) {
				// Checkmate
				scan.base = -10000 * delta;
			}
		} else {
			if (scan.best == null) { // evaluation.isEmpty()) {
				// Stalemate
				scan.base = 0;
			}
		}
	}
	
	
	public static class Move {
		final int from;
		final int to;
		final int special;
		final int piece;
		final boolean castle;
		
		// Material value
		double material;
		
		public Move(int piece, int from, int to, int special) {
			this.from = from;
			this.to = to;
			this.special = special;
			this.piece =  piece;
			this.castle = (special & 0b11_0000000) == ChessPieceManager.SM_PROMOTION;
		}
		
		@Override
		public String toString() {
			int type = special & 0b11_000000;
			return switch (type) {
				case ChessPieceManager.SM_NORMAL -> ChessUtils.toSquare(from) + " " + ChessUtils.toSquare(to);
				case ChessPieceManager.SM_CASTLING -> ((special & CastlingFlags.ANY_CASTLE_K) != 0) ? "O-O" : "O-O-O";
				case ChessPieceManager.SM_EN_PASSANT -> ChessUtils.toSquare(from) + " " + ChessUtils.toSquare(to) + " (en passant)";
				case ChessPieceManager.SM_PROMOTION -> ChessUtils.toSquare(to) + " (promotion)";
				default -> "";
			};
		}
	}
	
	public static class Scanner {
		public final boolean white;
		public boolean draw;
		public double base;
//		public List<Move> branches;
		public Move best;
		
//		public Scanner(ChessBoard board) {
////			this.branches = new ArrayList<>();
//			this.base = getMaterial(board);
//			this.white = board.isWhite();
//		}
		
		public Scanner(ChessBoard board, double material) {
//			this.branches = new ArrayList<>();
			this.base = material;
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
}
