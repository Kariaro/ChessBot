package me.hardcoded.chess.analysis;

import me.hardcoded.chess.advanced.*;
import me.hardcoded.chess.api.ChessAnalyser;
import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;

import java.util.Arrays;

/**
 * This class is version 1 of alpha-beta pruning move checking.
 *
 * @author HardCoded
 */
public class AnalyserV1_AlphaBetaPruning implements ChessAnalyser {
	private final int DEPTH = 6;
	private final Move[][] MOVES = new Move[DEPTH + 1][1024];
	
	private Move[] getAllMoves(ChessBoardImpl board, int depth) {
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
	
	public int getMaterial(ChessBoardImpl board) {
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
	
	private int non_developing(ChessBoardImpl board) {
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
	
	private int un_developing(ChessMove move) {
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
		return new AnalyserV1_AlphaBetaPruning().analyse((ChessBoardImpl)board);
	}
	
	@Override
	public ChessAnalysis analyse(ChessBoardImpl board) {
		ChessBoardImpl copy = board.creteCopy();
		Scanner scanner = analyseBranchMoves(copy);
		
		ChessAnalysis analysis = new ChessAnalysis();
		analysis.setBestMove(scanner.best);
		analysis.setMaterial((int) scanner.material());
		return analysis;
	}
	
	private double getAdvancedMaterial(ChessBoardImpl board, ChessMove lastMove) {
		double material = getMaterial(board);
		material += un_developing(lastMove);
		material += non_developing(board);
		return material;
	}
	
	private long nodes;
	private BranchResult analyseBranches(ChessBoardImpl board, ChessMove lastMove, int depth, double alpha, double beta, boolean white) {
		nodes++;
		if (depth == 0) {
			return new BranchResult(getAdvancedMaterial(board, lastMove));
		}
		
		// Default state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board, depth);
		double value;
		
		BranchResult result = new BranchResult(0);
		
		if (white) {
			value = Double.NEGATIVE_INFINITY;
			
			for (Move move : moves) {
				if (move == null) {
					break;
				}
				
				if (!ChessGenerator.playMove(board, move)) {
					continue;
				}
				
				BranchResult scannedResult = analyseBranches(board, move, depth - 1, alpha, beta, false);
				if (value < scannedResult.value) {
					result.updateMoves(move, scannedResult);
				}
				
				value = Math.max(value, scannedResult.value);
				if (value >= beta) {
					break;
				}
				
				state.write(board);
				alpha = Math.max(alpha, value);
			}
		} else {
			value = Double.POSITIVE_INFINITY;
			
			for (Move move : moves) {
				if (move == null) {
					break;
				}
				
				if (!ChessGenerator.playMove(board, move)) {
					continue;
				}
				
				BranchResult scannedResult = analyseBranches(board, move, depth - 1, alpha, beta, true);
				if (value > scannedResult.value) {
					result.updateMoves(move, scannedResult);
				}
				
				value = Math.min(value, scannedResult.value);
				if (value <= alpha) {
					break;
				}
				
				state.write(board);
				beta = Math.min(beta, value);
			}
		}
		result.value = value;
		
		state.write(board);
		return result;
	}
	
	private Scanner analyseBranchMoves(ChessBoardImpl board) {
		// Create a new scanner container
		Scanner scan = new Scanner(board, getMaterial(board));
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board, DEPTH);
		
		for (Move move : moves) {
			if (move == null) {
				break;
			}
			
			if (!ChessGenerator.playMove(board, move)) {
				continue;
			}
			
			long now = System.nanoTime();
			nodes = 0;
			BranchResult branchResult = analyseBranches(board, move, DEPTH - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, board.isWhite());
			long time = System.nanoTime() - now;
			double scannedResult = branchResult.value;
			move.material = scannedResult;
			
//			System.out.printf("move: %s, (%.2f)\n", move, scannedResult);
			System.out.printf("move: %s, (%.2f), %s\t%d nodes / sec\n", move, scannedResult, Arrays.toString(branchResult.moves), (long)(nodes / (time / 1000000000.0)));
			
			if (scan.white) {
				if (scan.best == null || scan.best.material < scannedResult) {
					scan.best = move;
				}
			} else {
				if (scan.best == null || scan.best.material > scannedResult) {
					scan.best = move;
				}
			}
//			double boardMaterial = getMaterial(board);
//
//			Scanner branch;
//			if (depth < 1) {
//				branch = new Scanner(board, boardMaterial);
//			} else {
//				branch = analyseBranchMoves(board, depth - 1, boardMaterial);
//			}
//
//			double scannedResult = ((branch.material()) * 80 + boardMaterial * 20) / 100.0;
//			scannedResult += un_developing(move) + non_developing(board);
//			if (move.castle) {
//				scannedResult += 30 * (board.isWhite() ? -1 : 1);
//			}
//
//			// Add this move to the evaluation set
//			if (scan.white) {
//				if (scan.best == null || scan.best.material < scannedResult) {
//					scan.best = move;
//				}
//			} else {
//				if (scan.best == null || scan.best.material > scannedResult) {
//					scan.best = move;
//				}
//			}
			
			// Undo the move and continue
			state.write(board);
		}
		
		evaluate(board, scan);
		state.write(board);
		return scan;
	}
	
	private void evaluate(ChessBoardImpl board, Scanner scan) {
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
		
		public Scanner(ChessBoardImpl board, double material) {
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
	
	private static class BranchResult {
		private static final Move[] EMPTY = new Move[0];
		
		public double value;
		public Move[] moves = EMPTY;
		
		public BranchResult(double value) {
			this.value = value;
		}
		
		public void updateMoves(Move move, BranchResult branch) {
			Move[] branchMoves = branch.moves;
			Move[] next = new Move[branchMoves.length + 1];
			next[0] = move;
			System.arraycopy(branchMoves,0,next, 1, branchMoves.length);
			moves = next;
		}
	}
}
