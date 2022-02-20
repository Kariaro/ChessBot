package me.hardcoded.chess.open2;

import java.util.*;

import me.hardcoded.chess.open.*;
import me.hardcoded.chess.open.ScanMoveOld;
import me.hardcoded.chess.open.ScanOld;

public class Analyser3 {
	public static Set<Move> getAllMoves(Chess b) {
		Set<Move> moves = new HashSet<>();
		
		for(int i = 0; i < 64; i++) {
			moves.addAll(b.getPieceMoves(i));
		}
		
		return moves;
	}
	
//	public static double getMaterial(Chess b) {
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
//			if(pieceId == Pieces.PAWN) {
//				val += (int)(((i / 8) / 24.0) * 50);
//			}
//			
//			if(pieceId == -Pieces.PAWN) {
//				val -= (int)(((8 - (i / 8)) / 24.0) * 50);
//			}
		}
		
		return mat;
	}
	
	public static ScanOld analyse(Chess board) {
		System.out.println(board.clone());
		ScanOld scan = analyseFrom(board.clone(), DEPTH);
		return scan;
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static final int DEPTH = 3;
	
	private static ScanOld analyseFrom(Chess board, int depth) {
		boolean turn = board.isWhiteTurn();
		board.setFlagsBit(Flags.TURN, !turn);
		Set<Move> enemyMoves = getAllMoves(board);
		board.setFlagsBit(Flags.TURN, turn);
		
		return analyseBranchMoves(board, depth, enemyMoves, getAllMoves(board));
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
		
		return result;
	}
	
	private static int non_developing(Chess board) {
		int result = 0;
		int penalty = 3;
		if(board.getPieceAt(1) == Pieces.KNIGHT) result -= 10;
		if(board.getPieceAt(2) == Pieces.BISHOP) result -= 10;
		if(board.getPieceAt(5) == Pieces.BISHOP) result -= 10;
		if(board.getPieceAt(6) == Pieces.KNIGHT) result -= 10;
		if(board.getPieceAt(11) == Pieces.PAWN) result -= 9;
		if(board.getPieceAt(12) == Pieces.PAWN) result -= 9;
		if(board.getPieceAt(4) == Pieces.KING) result -= 8;
		
		if(board.getPieceAt(57) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(58) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(61) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(62) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(51) == -Pieces.PAWN) result += 9;
		if(board.getPieceAt(52) == -Pieces.PAWN) result += 9;
		if(board.getPieceAt(60) == -Pieces.KING) result += 8;
		
		return result * penalty;
	}
	
	private static ScanOld analyseBranchMoves(Chess board, int depth, Set<Move> enemy, Set<Move> moves) {
		// Create a new scanner container
		ScanOld scan = new ScanOld(board, enemy, moves);
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
			
			ScanOld branch;
			if(depth < 1) {
				branch = new ScanOld(board);
			} else {
				branch = analyseBranchMoves(board, depth - 1, moves, getAllMoves(board));
			}
			
			double percent = ((branch.material()) * 90 + material * 10) / 100;
			percent = percent + un_developing(move) + non_developing(board);
			
			{
				int delta = (board.isWhiteTurn() ? 1:-1);
				
				for(int i = 0; i < 64; i++) {
					percent += delta * scan.attacks[i] / 2;
				}
			}
//			{
//				int attacks = scan.attacks[move.to()];
//				int delta = (board.isWhiteTurn() ? 1:-1);
//				int value = Pieces.value(move.id());
//				if(delta < 0) {
//					// black
//				
//				} else {
//					
//				}
//				
//				if(attacks < 0 && value * value > 1) {
//					percent += value / 3;
//				}
//				
//				percent += attacks * delta * 5;
//			}
			
			if(move.action() == Action.QUEENSIDE_CASTLE || move.action() == Action.KINGSIDE_CASTLE) percent += 50 * (board.isWhiteTurn() ? -1:1);
			
			if(depth == DEPTH) {
				log("  move: %-10s (%2.2f) -> (%2.2f)", move, material / 100.0, percent / 100.0);
				log("      : (%s)", branch.follow);
			}
			
			// Add this move to the evaluation set
			scan.branches.add(new ScanMoveOld(branch, percent, move));
			
			// Undo the move and continue
			board.setState(state);
		}
		
		if(!scan.branches.isEmpty()) {
			double median = 0;
			for(ScanMoveOld move : scan.branches) {
				median += move.material;
			}
			
			median = median / (0.0 + scan.branches.size());
			
			Iterator<ScanMoveOld> iter = scan.branches.iterator();
			while(iter.hasNext()) {
				ScanMoveOld move = iter.next();
				
				if(scan.white) {
					if(move.material < median - 100) {
						iter.remove();
					}
				} else {
					if(move.material > median + 100) {
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
	
	private static void evaluate(Chess board, ScanOld scan) {
		List<ScanMoveOld> evaluation = scan.branches;
		
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
			
			for(ScanMoveOld m : scan.best.sc.follow) {
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
		
		for(ScanMoveOld m : evaluation) {
			m.sc = null;
		}
	}
}
