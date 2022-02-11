package me.hardcoded.chess.open2;

import java.util.*;

import me.hardcoded.chess.open.*;
import me.hardcoded.chess.open.Analyser.Move0;
import me.hardcoded.chess.open.Analyser.Scan0;

public class Analyser4 {
	private static Set<Move> getAllMoves(Chess b) {
		Set<Move> moves = new HashSet<>();
		
		for(int i = 0; i < 64; i++) {
			moves.addAll(b.getPieceMoves(i));
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
	
	public static Scan0 analyse(Chess board) {
		// Scan0 scan = analyseFrom(board.clone(), DEPTH);
		Scan0 scan = testScan(board.clone());
		
		return scan;
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	private static final long timelimit = 60000;
	private static final int treepaths = 10;
	private static final int lines = 500;
	
//	private static Scan0 analyseFrom(Chess board, int depth) {
//		return analyseBranchMoves(board, depth, getAllMoves(board));
//	}
	
	private static Scan0 testScan(Chess oboard) {
		ChessTree startTree = new ChessTree(oboard);
		
		log("===================================================");
		log("timelimit: %d ms", timelimit);
		log("treepaths: %d ms", treepaths);
		log("lines: %d", lines);
		
		List<ChessTree> trees = new ArrayList<>();
		trees.add(startTree);
		
		long starttime = System.currentTimeMillis();
		
		int lowest = 2;
		Scan0 firstScan = null;
		Scan0 scan = null;
		for(int depth = 1; depth < 20; depth++) {
			ChessTree[] array = trees.toArray(new ChessTree[0]);
			trees.clear();
			
			for(ChessTree tree : array) {
				if(tree.material() * tree.material() > 50000000) {
					// Probably checkmate
					break;
				}
				
				if(starttime < System.currentTimeMillis() - timelimit) {
					log("Scanning time exceeded - %d ms", timelimit);
					break;
				}
				
				Chess board = tree.board;
				log("Scanning");
				log("   - depth: %d", depth + 1);
				log("   - turn: %s", board.isWhiteTurn() ? "white":"black");
				log("   - tree: %s", tree);
				
				long start = System.nanoTime();
				scan = scanDepth3(board);
				
				if(tree == startTree) {
					firstScan = scan;
				}
				
				// Cheep way of creating a list
				List<Move0> check = cutList(board, scan);
				
				log("   - moves:");
				for(Move0 move : check) {
					log("       |- [%s] (%.0f)", move.move, move.material);
					
					move.material *= 0.9;
					Chess next = board.clone();
					next.doMove(move.move);
					
					Scan0 step = scanDepth3(next);
					move.material = step.material();
					log("       |- -> (%.0f)", step.material());
					
					ChessTree tstep = new ChessTree(next, step, tree.parentMove, tree);
					trees.add(tstep);
				}
				
				if(!scan.branches.isEmpty()) {
					scan.branches.sort(null);
					
					Move0 best;
					if(board.isWhiteTurn()) {
						best = scan.branches.get(scan.branches.size() - 1);
					} else {
						best = scan.branches.get(0);
					}
					
					scan.best = best;
					best.sc = null;
				}
				
				long ellapsed = System.nanoTime() - start;
				log("   - took: %.4f ms", ellapsed / 1000000.0f);
				log("\n----------");
			}
			
			if(starttime < System.currentTimeMillis() - timelimit) {
				//break;
			} else {
				lowest = depth + 1;
			}
			
			// Maybe only keep 5 or 10 of the branches is good?
			trees.sort(null);
			//trees = cutList(trees);
			
			
			{
				List<ChessTree> list = new ArrayList<>();
				list.addAll(trees);
				
				while(!list.isEmpty()) {
					ChessTree tree = list.get(0);
					list.remove(0);
					tree.update();
					if(tree.parentTree != null) list.add(tree.parentTree);
				}
			}
		}
		
		{
			firstScan.branches.sort(null);
			if(scan.white) {
				scan.best = scan.branches.get(scan.branches.size() - 1);
			} else {
				scan.best = scan.branches.get(0);
			}
		}
		
		System.out.println("LowestBranch: " + lowest);
		System.out.println("OverrideScore: " + startTree.overrideMat);
		
		return firstScan;
	}
	
	private static Scan0 scanDepth3(Chess board) {
		Set<Move> moves = getAllMoves(board);
		return analyseBranchMoves(board, 2, moves);
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
		if(board.getPieceAt(4) == Pieces.KING) result += 8;
		
		if(board.getPieceAt(57) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(58) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(61) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(62) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(51) == -Pieces.PAWN) result += 9;
		if(board.getPieceAt(52) == -Pieces.PAWN) result += 9;
		if(board.getPieceAt(60) == -Pieces.KING) result -= 8;
		
		return result * penalty;
	}
	
	private static Scan0 analyseBranchMoves(Chess board, int depth, Set<Move> moves) {
		// Create a new scanner container
		Scan0 scan = new Scan0(board);
		if(depth < 0) return scan;
		
		// Save the last state of the board
		State state = board.getState();
		
		for(Move move : moves) {
			board.doMove(move);
			double material = getMaterial(board);
			
			Scan0 branch;
			if(depth < 1) {
				branch = new Scan0(board);
			} else {
				branch = analyseBranchMoves(board, depth - 1, getAllMoves(board));
			}
			
			double percent = ((branch.material()) * 90 + material * 10) / 100;
			percent = percent + un_developing(move) + non_developing(board);
			
			if(move.action() == Action.QUEENSIDE_CASTLE || move.action() == Action.KINGSIDE_CASTLE) percent += 50 * (board.isWhiteTurn() ? -1:1);
			
			// Add this move to the evaluation set
			scan.branches.add(new Move0(branch, percent, move));
			
			// Undo the move and continue
			board.setState(state);
		}
		
		evaluate(board, scan);
		board.setState(state);
		
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
	
	private static List<Move0> cutList(Chess board, Scan0 scan) {
		List<Move0> nodes = new ArrayList<>();
		
		if(board.isWhiteTurn()) {
			int bs = scan.branches.size();
			int st = Math.max(0, bs - lines);
			for(int i = st; i < bs; i++) {
				nodes.add(nodes.size(), scan.branches.get(i));
			}
		} else {
			int bs = scan.branches.size();
			int et = Math.min(lines, bs);
			for(int i = 0; i < et; i++) {
				nodes.add(scan.branches.get(i));
			}
		}
		
		return nodes;
	}
	
	private static List<ChessTree> cutList(List<ChessTree> trees) {
		List<ChessTree> list = new ArrayList<>();
		if(trees.isEmpty()) return list;
		Chess board = trees.get(0).board;
		int bs = trees.size();
		
		if(board.isWhiteTurn()) {
			int st = Math.max(0, bs - treepaths);
			for(int i = st; i < bs; i++) {
				list.add(list.size(), trees.get(i));
			}
		} else {
			int et = Math.min(treepaths, bs);
			for(int i = 0; i < et; i++) {
				list.add(trees.get(i));
			}
		}
		
		return list;
	}
	
	private static class ChessTree implements Comparable<ChessTree> {
		public Chess board;
		public List<Move0> nodes;
		
		public ChessTree parentTree;
		public Move0 parentMove;
		public double overrideMat;
		public boolean om;
		
		public ChessTree(Chess board, Scan0 scan, Move0 parentMove, ChessTree parentTree) {
			this.board = board;
			this.nodes = new ArrayList<>();
			this.parentMove = parentMove;
			this.parentTree = parentTree;
			if(scan == null) return;
			
			if(board.isWhiteTurn()) {
				int bs = scan.branches.size();
				int st = Math.max(0, bs - lines);
				for(int i = st; i < bs; i++) {
					nodes.add(nodes.size(), scan.branches.get(i));
				}
			} else {
				int bs = scan.branches.size();
				int et = Math.min(lines, bs);
				for(int i = 0; i < et; i++) {
					nodes.add(scan.branches.get(i));
				}
			}
		}
		
		public ChessTree(Chess board) {
			this.board = board;
			this.nodes = new ArrayList<>();
			
			Set<Move> moves = getAllMoves(board);
			
			for(Move move : moves) {
				this.nodes.add(new Move0(0, move));
			}
		}
		
		public Move0 getBestMove() {
			if(nodes.isEmpty()) return null;
			return board.isWhiteTurn() ? nodes.get(nodes.size() - 1):nodes.get(0);
		}
		
		public double material() {
			if(om) return overrideMat;
			if(nodes.isEmpty()) return 0;
			return getBestMove().material;
		}
		
		/**
		 * Update the values
		 */
		public void update() {
			if(nodes.isEmpty()) return;
			nodes.sort(null);
			
			Move0 best = getBestMove();
			double mat = best.material;
			om = true;
			overrideMat = mat;
			if(parentTree != null) {
				parentTree.om = true;
				parentTree.overrideMat = mat;
			}
			if(parentMove != null) parentMove.material = mat;
		}
		
		public int compareTo(ChessTree tree) {
			return Double.compare(material(), tree.material());
		}
	}
}
