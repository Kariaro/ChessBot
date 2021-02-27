package hardcoded.chess.open2;

import java.util.*;

import hardcoded.chess.open.*;
import hardcoded.chess.open.Analyser.Move0;
import hardcoded.chess.open.Analyser.Scan0;

public class Analyser7 {
	private static Set<Move> getAllMoves(Chess board) {
		Set<Move> moves = new HashSet<>();
		
		boolean turn = board.isWhiteTurn();
		for(int i = 0; i < 64; i++) {
			int piece = board.getPieceAt(i);
			if(turn && piece < 1 || (!turn && piece > -1)) continue;
			
			moves.addAll(board.getPieceMoves(i));
		}
		
		return moves;
	}
	
	public static int getMaterial(Chess b) {
		int mat = 0;
		if(b.isFlagSet(Flags.CASTLE_BK)) mat -= 6;
		if(b.isFlagSet(Flags.CASTLE_BQ)) mat -= 6;
		if(b.isFlagSet(Flags.CASTLE_WK)) mat += 6;
		if(b.isFlagSet(Flags.CASTLE_WQ)) mat += 6;
		
		for(int i = 0; i < 64; i++) {
			int pieceId = b.getPieceAt(i);
			int val = Pieces.value(pieceId);
			mat += val;
			
			// Pushed pawns get's more points only if the row has no other pawn on it
			if(pieceId == Pieces.PAWN) {
				val += (int)(((i / 8) / 24.0) * 20);
			}
			
			if(pieceId == -Pieces.PAWN) {
				val -= (int)(((8 - (i / 8)) / 24.0) * 20);
			}
		}
		
		return mat;
	}
	
	public static Scan0 analyse(Chess board) {
		System.out.println(board.clone());
		Scan0 scan = analyseFrom(board.clone(), DEPTH);
		return scan;
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static Random random = new Random();
	private static final int DEPTH = 3;
	
	private static Scan0 analyseFrom(Chess board, int depth) {
		Scan0 scan = analyseBranchMoves(board, depth, getAllMoves(board));
		customizeEnds(scan);
		return scan;
	}
	
	private static int un_developing(Move move) {
		int id = move.id();
		
		int result = 0;
		
		switch(id) {
			case Pieces.KNIGHT: {
				if(move.to() == 1 || move.to() == 6) {
					result -= 10;
				}
				break;
			}
			case -Pieces.KNIGHT: {
				if(move.to() == 57 || move.to() == 62) {
					result += 10;
				}
				break;
			}
			case Pieces.BISHOP: {
				if(move.to() == 2 || move.to() == 5) {
					result -= 10;
				}
				break;
			}
			case -Pieces.BISHOP: {
				if(move.to() == 58 || move.to() == 61) {
					result += 10;
				}
				break;
			}
			case Pieces.QUEEN: {
				result -= 5;
				break;
			}
			case -Pieces.QUEEN: {
				result += 5;
				break;
			}
			case Pieces.KING: {
				result -= 5;
				break;
			}
			case -Pieces.KING: {
				result += 5;
				break;
			}
		}
		
		return result * 3;
	}
	
	private static int non_developing(Chess board) {
		int result = 0;
		if(board.getPieceAt(1) == Pieces.KNIGHT) result -= 10;
		if(board.getPieceAt(2) == Pieces.BISHOP) result -= 10;
		if(board.getPieceAt(5) == Pieces.BISHOP) result -= 10;
		if(board.getPieceAt(6) == Pieces.KNIGHT) result -= 10;
		if(board.getPieceAt(11) == Pieces.PAWN) result -= 11;
		if(board.getPieceAt(12) == Pieces.PAWN) result -= 11;
		if(board.getPieceAt(4) == Pieces.KING) result -= 8;
		
		if(board.getPieceAt(57) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(58) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(61) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(62) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(51) == -Pieces.PAWN) result += 11;
		if(board.getPieceAt(52) == -Pieces.PAWN) result += 11;
		if(board.getPieceAt(60) == -Pieces.KING) result += 8;
		
		return result * 3;
	}
	
	private static Scan0 analyseBranchMoves(Chess board, int depth, Set<Move> moves) {
		// Create a new scanner container
		Scan0 scan = new Scan0(board);
		
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
			
			double percent = ((branch.material()) * 95 + material * 5) / 100.0;
			percent = percent + un_developing(move) + non_developing(board);
			
			if(move.action() == Action.QUEENSIDE_CASTLE || move.action() == Action.KINGSIDE_CASTLE) percent += 20 * (board.isWhiteTurn() ? -1:1);
			
			if(depth == DEPTH) {
				log("  move: %-10s (%2.2f) -> (%2.2f)", move, material / 100.0, percent / 100.0);
				log("      : (%s)", branch.follow);
			}
			
			// Add this move to the evaluation set
			scan.branches.add(new Move0(branch, percent, move));
			
			// Undo the move and continue
			board.setState(state);
		}
		
		evaluate(board, scan);
		board.setState(state);
		return scan;
	}
	
	
	/**
	 * Add some custom and random moves to the bot
	 */
	private static void customizeEnds(Scan0 scan) {
		List<Move0> branches = scan.branches;
		if(branches.size() < 2) return;
		
		double score = scan.best.material;
		if(random.nextFloat() < 0.3) {
			if(!scan.white) {
				Move0 move = branches.get(branches.size() - 2);
				
				if(Math.abs(move.material - score) < 10) {
					scan.branches.remove(branches.size() - 1);
					scan.best = move;
				}
			} else {
				Move0 move = branches.get(1);
				if(Math.abs(move.material - score) < 10) {
					scan.branches.remove(0);
					scan.best = move;
				}
			}
		}
		
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
			scan.base += 10 * delta;
			
			if(evaluation.isEmpty()) {
				// Checkmate
				scan.base = -10000 * delta;
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
}
