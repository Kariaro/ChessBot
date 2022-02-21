package me.hardcoded.chess.analysis;

import me.hardcoded.chess.advanced.*;
import me.hardcoded.chess.api.ChessAnalyser;
import me.hardcoded.chess.api.ChessAnalysis;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.uci.Pieces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is version 4 of alpha-beta pruning move checking.
 *
 * @author HardCoded
 */
public class AlphaBetaPruningV4 implements ChessAnalyser {
	// Maximum of 45 seconds for a move
	private static final int MAX_TIME = 45000;
	
	private final int DEPTH = 6;
	private final int QUIESCE_DEPTH = 2;
	private final Move[][] MOVES = new Move[DEPTH + 1][1024];
	private final Move[][] QUIESCE_MOVES = new Move[QUIESCE_DEPTH][1024];
	
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
	
	private Move[] getQuiesceMoves(ChessBoardImpl board, int depth) {
		final Move[] moves = QUIESCE_MOVES[depth];
		final int[] ptr = new int[1];
		
		ChessGenerator.generateQuiesce(board, (fromIdx, toIdx, special) -> {
			if (!ChessGenerator.isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			if (((special & 0b11_000000) == ChessPieceManager.SM_EN_PASSANT) || (board.pieces[toIdx] != Pieces.NONE)) {
				int next = ptr[0];
				moves[next] = new Move(board.pieces[fromIdx], fromIdx, toIdx, special);
				ptr[0] = next + 1;
			}
			
			return true;
		});
		
		// Make sure we cap the moves
		moves[ptr[0]] = null;
		
		return moves;
	}
	
	public int getMaterial(ChessBoardImpl board) {
		long mask = board.pieceMask;
		int material = 0;
		
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_K)) material -= 30;
		if (board.hasFlags(CastlingFlags.BLACK_CASTLE_Q)) material -= 30;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_K)) material += 30;
		if (board.hasFlags(CastlingFlags.WHITE_CASTLE_Q)) material += 30;
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			int piece = board.pieces[idx];
			int val = Pieces.value(piece);
			
			if (piece == Pieces.PAWN) {
				val += (idx >> 3) * 10;
			} else if (piece == -Pieces.PAWN) {
				val -= (8 - (idx >> 3)) * 10;
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
			case Pieces.QUEEN -> result -= 5;
			case -Pieces.QUEEN -> result += 5;
			case Pieces.KING -> result -= 15;
			case -Pieces.KING -> result += 15;
		}
		
		return result * 3;
	}
	
	public static ChessAnalysis analyseTest(ChessBoard board) {
		return new AlphaBetaPruningV4().analyse((ChessBoardImpl)board);
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
	
	private double quiesce(ChessBoardImpl board, ChessMove lastMove, int depth, double alpha, double beta, boolean white) {
		double evaluation = getAdvancedMaterial(board, lastMove);
		if (depth == 0) {
			return evaluation;
		}
		
		if (white) {
			if (evaluation >= beta) {
				return beta;
			}
			
			if (evaluation > alpha) {
				alpha = evaluation;
			}
		} else {
			if (evaluation <= alpha) {
				return alpha;
			}
			
			if (evaluation < beta) {
				beta = evaluation;
			}
		}
		
		ChessState state = ChessState.of(board);
		Move[] moves = getQuiesceMoves(board, depth - 1);
		double value = evaluation;
		for (Move move : moves) {
			if (move == null) {
				break;
			}
			
			if (!ChessGenerator.playMove(board, move)) {
				// This should never happen
				continue;
			}
			
			if (white) {
				double score = quiesce(board, move, depth - 1, alpha, beta, false);
				
				if (score >= beta) {
					return beta;
				}
				
				if (score > alpha) {
					alpha = score;
					value = score;
				}
			} else {
				double score = quiesce(board, move, depth - 1, alpha, beta, true);
				
				if (score <= alpha) {
					return alpha;
				}
				
				if (score < beta) {
					beta = score;
					value = score;
				}
			}
			
			state.write(board);
		}
		
		return value;
	}
	
	private long nodes;
	private BranchResult analyseBranches(ChessBoardImpl board, ChessMove lastMove, int depth, double alpha, double beta, boolean white) {
		nodes++;
		if (depth == 0) {
			return new BranchResult(quiesce(board, lastMove, QUIESCE_DEPTH, alpha, beta, white));
		}
		
		// Default state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board, depth - 1);
		double value;
		
		BranchResult result = new BranchResult(0);
		
		int countMoves = 0;
		value = (100000 * (depth + 1)) * (white ? -1 : 1);
		for (Move move : moves) {
			if (move == null) {
				break;
			}
			
			if (!ChessGenerator.playMove(board, move)) {
				continue;
			}
			
			countMoves++;
			
			BranchResult scannedResult = analyseBranches(board, move, depth - 1, alpha, beta, !white);
			if (white) {
				if (value < scannedResult.value) {
					result.updateMoves(move, scannedResult);
					value = scannedResult.value;
				}
				
				if (value >= beta) {
					break;
				}
				
				alpha = Math.max(alpha, value);
			} else {
				if (value > scannedResult.value) {
					result.updateMoves(move, scannedResult);
					value = scannedResult.value;
				}
				
				if (value <= alpha) {
					break;
				}
				
				beta = Math.min(beta, value);
			}
			
			state.write(board);
		}
		result.value = value;
		state.write(board);
		
		if (countMoves == 0) {
			// There are no moves therefore stalemate
			// Check if the king is in check
			
			if (ChessPieceManager.isKingAttacked(board, white)) {
				// Checkmate
				result.value = (100000 * (depth + 1)) * (white ? -1 : 1);
			} else {
				// Stalemate
				result.value = 0;
			}
		}
		
//		{
//			ChessMove best = result.moves[0];
//			evaluate(board, scan);
//		}
		
		state.write(board);
		return result;
	}
	
	private Scanner analyseBranchMoves(ChessBoardImpl board) {
		// Create a new scanner container
		Scanner scan = new Scanner(board, getMaterial(board));
		
		// Save the last state of the board
		ChessState state = ChessState.of(board);
		Move[] moves = getAllMoves(board, DEPTH);
		
		List<Move> moves_list = new ArrayList<>();
		for (Move move : moves) {
			if (move == null) {
				break;
			}
			
			moves_list.add(move);
		}
		
		if (moves_list.isEmpty()) {
			return scan;
		}
		
		long startTime = System.currentTimeMillis();
		boolean isWhite = board.isWhite();
		int mul = (isWhite ? -1 : 1);
		
		for (int i = 4; i < DEPTH; i++) {
			int div = ((i - 4) * 2);
			int max_moves = Math.min(moves_list.size(), Math.max(4, moves_list.size() / (div == 0 ? 1 : div)));
			
			boolean keep_trying;
			do {
				keep_trying = false;
				
				for (int j = 0; j < max_moves; j++) {
					Move move = moves_list.get(j);
					if (move.scanned) {
						continue;
					}
					
					move.scanned = true;
					
					if (!ChessGenerator.playMove(board, move)) {
						// This should never happen
						moves_list.remove(j--);
						continue;
					}
					
					long now = System.nanoTime();
					nodes = 0;
					BranchResult branchResult = analyseBranches(board, move, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, board.isWhite());
					long time = System.nanoTime() - now;
					move.material = branchResult.value;
					move.moves = branchResult.moves;
					move.nps = (long)(nodes / (time / 1000000000.0));
//					if (System.currentTimeMillis() - startTime > MAX_TIME) {
//						break;
//					}
					
//					if (scan.white) {
//						if (scan.best == null || scan.best.material < scannedResult) {
//							scan.best = move;
//						}
//					} else {
//						if (scan.best == null || scan.best.material > scannedResult) {
//							scan.best = move;
//						}
//					}
					// Undo the move and continue
					state.write(board);
				}
				
				moves_list.sort((a, b) -> Double.compare(a.material, b.material) * mul);
				for (int j = 0; j < max_moves; j++) {
					if (!moves_list.get(j).scanned) {
						keep_trying = true;
						break;
					}
				}
			} while (keep_trying);
			
			scan.best = moves_list.get(0);
			System.out.printf("Depth: %d, best=%s\n", i, scan.best);
			for (int j = 0; j < max_moves; j++) {
				Move move = moves_list.get(j);
				System.out.printf("move: %-5s, (%.2f), %s\t%d nodes / sec\n", move, move.material / 100.0, Arrays.toString(move.moves), move.nps);
			}
			
			for (Move move : moves_list) {
				move.scanned = false;
			}
			
			System.out.println();
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
		long nps;
		Move[] moves;
		boolean scanned;
		
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
