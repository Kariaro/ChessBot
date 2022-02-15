package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessAnalyser;
import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;

public class AnalyserTool4 implements ChessAnalyser {
	private final int DEPTH = 4;
	private final Move[][] MOVES = new Move[DEPTH + 1][1024];
	
	private Move[] getAllMoves(ChessBoard board, int depth) {
		final Move[] moves = MOVES[depth];
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
		
		// Make sure we cap the moves
		moves[ptr[0]] = null;
		
		return moves;
	}
	
	public int getMaterial(ChessBoard board) {
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
	
	private int non_developing(ChessBoard board) {
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
	
	private int un_developing(Move move) {
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
	
	public static ChessAnalysis analyseTest(ChessBoard board) {
		return new AnalyserTool4().analyse(board);
	}
	
	@Override
	public ChessAnalysis analyse(ChessBoard board) {
		ChessBoard copy = board.creteCopy();
		Scanner scanner = analyseBranchMoves(copy, DEPTH, getMaterial(copy));
		
		ChessAnalysis analysis = new ChessAnalysis();
		analysis.setBestMove(scanner.best);
		analysis.setMaterial((int) scanner.material());
		return analysis;
	}
	
	private Scanner analyseBranchMoves(ChessBoard board, int depth, double currentMaterial) {
		// Create a new scanner container
		Scanner scan = new Scanner(board, currentMaterial);
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board, depth);
		
		for (int i = 0, len = moves.length; i < len; i++) {
			Move move = moves[i];
			if (move == null) {
				break;
			}
			
			if (!ChessGenerator.playMove(board, move.from, move.to, move.special)) {
				continue;
			}
			
			double boardMaterial = getMaterial(board);
			
			Scanner branch;
			if (depth < 1) {
				branch = new Scanner(board, boardMaterial);
			} else {
				branch = analyseBranchMoves(board, depth - 1, boardMaterial);
			}
			
			double scannedResult = ((branch.material()) * 80 + boardMaterial * 20) / 100.0;
			scannedResult += un_developing(move) + non_developing(board);
			if (move.castle) {
				scannedResult += 30 * (board.isWhite() ? -1 : 1);
			}
			
			move.material = scannedResult;
			
			// Add this move to the evaluation set
			if (scan.white) {
				if (scan.best == null || scan.best.material < scannedResult) {
					scan.best = move;
				}
			} else {
				if (scan.best == null || scan.best.material > scannedResult) {
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
	
	private void evaluate(ChessBoard board, Scanner scan) {
		if (ChessPieceManager.isKingAttacked(board, board.isWhite())) {
			double delta = board.isWhite() ? -1 : 1;
			scan.base += 10 * delta;
			
			if (scan.best == null) {
				// Checkmate
				scan.base = 10000 * delta;
			}
		} else {
			if (scan.best == null) {
				// Stalemate
				scan.base = 0;
			}
		}
		
		if (board.getLastCapture() >= 50) {
			// The game should end
			scan.base = 0;
			scan.best = null;
		}
	}
	
	private static class Move extends ChessMove {
		final boolean castle;
		
		// Material value
		double material;
		
		public Move(int piece, int from, int to, int special) {
			super(piece, from, to, special);
			this.castle = (special & 0b11_0000000) == ChessPieceManager.SM_PROMOTION;
		}
	}
	
	private static class Scanner {
		public final boolean white;
		public boolean draw;
		public double base;
		public Move best;
		
		public Scanner(ChessBoard board, double material) {
			this.white = board.isWhite();
			this.base = material;
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
