package hardcoded.chess.open;

import java.util.*;

import hardcoded.chess.open.Analyser.Move0;
import hardcoded.chess.open.Analyser.Scan0;

public class Analyser2 {
	private static Set<Move> getAllMoves(Chessboard b) {
		Set<Move> moves = new HashSet<>();
		
		for(int i = 0; i < 64; i++) {
			moves.addAll(b.getPieceMoves(i));
		}
		
		return moves;
	}
	
//	public static double getMaterial(Chessboard b) {
//		double mat = 0;
//		for(int i = 0; i < 64; i++) {
//			int pieceId = b.getPieceAt(i);
//			double val = Pieces.value(pieceId);
//			
//			if(pieceId == Pieces.PAWN) {
//				val += (i / 8) / 24.0;
//			}
//			
//			if(pieceId == -Pieces.PAWN) {
//				val -= (8 - (i / 8)) / 24.0;
//			}
//			mat += val;
//		}
//		
//		return mat;
//	}
	
	public static double getMaterial(Chessboard b) {
		double mat = 0;
		if(b.isFlagSet(Flags.CASTLE_BK)) mat -= 0.15;
		if(b.isFlagSet(Flags.CASTLE_BQ)) mat -= 0.15;
		if(b.isFlagSet(Flags.CASTLE_WK)) mat += 0.15;
		if(b.isFlagSet(Flags.CASTLE_WQ)) mat += 0.15;
		
		for(int i = 0; i < 64; i++) {
			int pieceId = b.getPieceAt(i);
			double val = Pieces.value(pieceId);
			mat += val;
			
			// Pushed pawns get's more points only if the row has no other pawn on it
			if(pieceId == Pieces.PAWN) {
				val += (i / 8) / 24.0;
			}
			
			if(pieceId == -Pieces.PAWN) {
				val -= ((i / 8)) / 24.0;
			}
		}
		
		return mat;
	}
	
	public static Scan0 analyse(Chessboard board) {
		Scan0 scan = analyseFrom(board.clone(), DEPTH);
		return scan;
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static final int DEPTH = 3;
	
	private static Scan0 analyseFrom(Chessboard board, int depth) {
		return analyseBranchMoves(board, depth, getAllMoves(board));
	}
	
	private static double un_developing(Move move) {
		int id = move.id();
		
		double result = 0;
		
		switch(id) {
			case Pieces.KNIGHT: {
				if(move.to() == 1 || move.to() == 6) {
					result -= 0.1;
				}
				break;
			}
			case -Pieces.KNIGHT: {
				if(move.to() == 57 || move.to() == 62) {
					result += 0.1;
				}
				break;
			}
			case Pieces.BISHOP: {
				if(move.to() == 2 || move.to() == 5) {
					result -= 0.1;
				}
				break;
			}
			case -Pieces.BISHOP: {
				if(move.to() == 58 || move.to() == 61) {
					result += 0.1;
				}
				break;
			}
			case Pieces.QUEEN: {
				result -= 0.05;
				break;
			}
			case -Pieces.QUEEN: {
				result += 0.05;
				break;
			}
		}
		
		return result;
	}
	
	private static double non_developing(Chessboard board) {
		double result = 0;
		double penalty = 1;
		if(board.getPieceAt(1) == Pieces.KNIGHT) result -= 0.1;
		if(board.getPieceAt(2) == Pieces.BISHOP) result -= 0.1;
		if(board.getPieceAt(5) == Pieces.BISHOP) result -= 0.1;
		if(board.getPieceAt(6) == Pieces.KNIGHT) result -= 0.1;
		if(board.getPieceAt(11) == Pieces.PAWN) result -= 0.09;
		if(board.getPieceAt(12) == Pieces.PAWN) result -= 0.09;
		
		if(board.getPieceAt(57) == -Pieces.KNIGHT) result += 0.1;
		if(board.getPieceAt(58) == -Pieces.BISHOP) result += 0.1;
		if(board.getPieceAt(61) == -Pieces.BISHOP) result += 0.1;
		if(board.getPieceAt(62) == -Pieces.KNIGHT) result += 0.1;
		if(board.getPieceAt(51) == -Pieces.PAWN) result += 0.09;
		if(board.getPieceAt(52) == -Pieces.PAWN) result += 0.09;
		
		return result * penalty;
	}
	
	private static Scan0 analyseBranchMoves(Chessboard board, int depth, Set<Move> moves) {
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
			
			double percent = ((branch.material()) * 0.9 + material * 0.1);
			percent = percent + un_developing(move) + non_developing(board);
			
//			int aid = Math.abs(move.id());
//			if(aid != Pieces.PAWN && aid != Pieces.KING) {
//				percent += 0.11 * (scan.white ? 1:-1);
//			}
			
//			if(depth == DEPTH) {
//				log("  move: %-10s (%2.4f) -> (%2.4f)", move, material, percent);
//				log("      : (%s)", branch.follow);
//			}
			
			// Add this move to the evaluation set
			scan.branches.add(new Move0(branch, percent, move));
			
			// Undo the move and continue
			board.setState(state);
		}
		
		if(!scan.branches.isEmpty()) {
			double median = 0;
			for(Move0 move : scan.branches) {
				median += move.material;
			}
			
			median = median / (0.0 + scan.branches.size());
			
			Iterator<Move0> iter = scan.branches.iterator();
			while(iter.hasNext()) {
				Move0 move = iter.next();
				
				if(scan.white) {
					if(move.material < median - 1) {
						iter.remove();
					}
				} else {
					if(move.material > median + 1) {
						iter.remove();
					}
				}
			}
		}
		
		evaluate(board, scan);
		board.setState(state);
		
//		if(depth == DEPTH) {
//			log("Material: %.4f", scan.base);
//		}
//		
//		if(depth == DEPTH) {
//			if(scan.follow.size() > 2) {
//				while(scan.follow.size() > 2) {
//					scan.follow.remove(2);
//				}
//			}
//			Scan0 next = analyseBranchMoves(board, depth + 1, scand - 1, Set.of(scan.best.move));
//			
//			scan.follow.addAll(next.follow);
//			log("  move: %-10s (%2.4f) -> (%2.4f)", scan.best, scan.material(), scan.best.material);
//			log("      : (%s)", scan.follow);
//		}
		
		return scan;
	}
	
	private static void evaluate(Chessboard board, Scan0 scan) {
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
}