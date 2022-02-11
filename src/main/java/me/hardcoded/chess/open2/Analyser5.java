package me.hardcoded.chess.open2;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.hardcoded.chess.open.*;
import me.hardcoded.chess.open.Analyser.Move0;
import me.hardcoded.chess.open.Analyser.Scan0;

public class Analyser5 {
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
		}
		
		return mat;
	}
	
	public static Scan0 analyse(Chess board) {
		return testScan(board.clone());
	}
	
	private static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}
	
	// private static final ChessTree[] TREE_TYPE = new ChessTree[0];
	private static final long timelimit = 5000;
	private static final int treepaths = 10;
	private static final int maxdepth = 3;
	private static final int lines = 500;
	private static long starttime = 0;
	
	private static Scan0 testScan(Chess oboard) {
		starttime = System.currentTimeMillis();
		
		log("===================================================");
		log("timelimit: %d ms", timelimit);
		log("treepaths: %d ms", treepaths);
		log("lines: %d", lines);
		
		DeltaTree[] depths = new DeltaTree[maxdepth]; {
			depths[0] = new DeltaTree(1);
			depths[0].nodes.add(new ChessTree(oboard));
		}
		
		DeltaScan scan = analyseMoves(oboard);
		DeltaScan firstScan = scan;
		
		for(int depth = 0; depth < maxdepth - 1; depth++) {
			// Get the depth tree for this depth and look through the nodes
			DeltaTree delta = depths[depth];
			
			if(delta.isEmpty()) break;
			boolean turn = oboard.isWhiteTurn() ^ ((depth % 2) == 1);
			
			// Generate a list of possible trees that we can enter.
			DeltaTree nextDelta = new DeltaTree(depth + 1);
			nextDelta.addAll(threadTree(delta.nodes, turn, depth + 1));
			depths[depth + 1] = nextDelta;
			
			if(starttime < System.currentTimeMillis() - timelimit) {
				break;
			}
			
			List<ChessTree> trees = nextDelta.nodes;
			
			// Sort the trees after a 2 depth analysis of how good each branch was and
			// go one step down and check each branch if they are good
			
			if(trees.size() > 1) {
				trees.sort(null);
			}
			
			{
				for(ChessTree tree : trees) {
					// material, played move, best next move
					log("tree: [%.2f] (%s) %s", tree.material(), tree.getPlayedMove(), tree.getBestMove());
					
					// Update the parent tree
					DeltaMove pmove = tree.parentMove;
					if(pmove != null) {
						pmove.material = tree.material();
					}
				}
				
				for(ChessTree tree : delta.nodes) {
					tree.nodes.sort(null);
					log("  tree: [%.2f] (%s) %s", tree.material(), tree.getPlayedMove(), tree.getBestMove());
					log("  ");
				}
			}
			
			if(!trees.isEmpty()) {
				System.out.println("BestBranch: " + trees.get(0).getPlayedMove());
			}
		}
		
//		if(scan != null && !scan.branches.isEmpty()) {
//			firstScan.branches.sort(null);
//			if(scan.white) {
//				scan.best = scan.branches.get(scan.branches.size() - 1);
//			} else {
//				scan.best = scan.branches.get(0);
//			}
//		}
		
		System.out.println("Follow: " + firstScan.follow);
		return firstScan.toScan0();
	}
	
	private static List<ChessTree> threadTree(List<ChessTree> trees, boolean turn, int depth) {
		ConcurrentLinkedQueue<ChessTree> queue = new ConcurrentLinkedQueue<>(trees);
		Vector<ChessTree> out = new Vector<>();
		List<Thread> threads = new ArrayList<>();
		for(int i = 0; i < 16; i++) {
			threads.add(new Thread(() -> {
				while(!queue.isEmpty()) {
					if(starttime < System.currentTimeMillis() - timelimit) {
						break;
					}
					
					ChessTree tree = queue.poll();
					if(tree == null) continue;
					
					if(tree.material() * tree.material() > 50000000) {
						// Probably checkmate
						ChessTree ntree = new ChessTree(tree.board);
						ntree.board.doMove(ntree.getBestMove().move);
						out.add(ntree);
					} else {
						ChessScan cscan = test(tree);
						out.addAll(cscan.trees);
					}
				}
			}));
		}
		
//		if(starttime < System.currentTimeMillis() - timelimit) {
//			log("Scanning time exceeded - %d ms", timelimit);
//			break;
//		}

		log("Scanning");
		log("   - depth: %d", depth);
		log("   - turn: %s", turn ? "white":"black");
		log("   - trees: %s", trees.size());
		
		long start = System.nanoTime();
		
		for(Thread thread : threads) {
			thread.setDaemon(true);
			thread.start();
		}
		
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		long ellapsed = System.nanoTime() - start;
		log("   - took: %.4f ms", ellapsed / 1000000.0f);
		log("\n----------");
		
		return out;
	}
	
	private static ChessScan test(ChessTree tree) {
		Chess board = tree.board;
		
		DeltaScan scan = analyseMoves(board);
		ChessScan chess_scan = new ChessScan(scan);
		
		// Cheep way of creating a list
		List<DeltaMove> check = cutList(board, scan);
		
		for(DeltaMove move : check) {
			move.material *= 0.9;
			Chess next = board.clone();
			next.doMove(move.move);
			
			DeltaScan step = analyseMoves(next);
			move.material = step.material();
			
			ChessTree tstep = new ChessTree(next, step, move, tree);
			chess_scan.trees.add(tstep);
		}
		
		if(!scan.branches.isEmpty()) {
			scan.branches.sort(null);
			
			DeltaMove best;
			if(board.isWhiteTurn()) {
				best = scan.branches.get(scan.branches.size() - 1);
			} else {
				best = scan.branches.get(0);
			}
			
			scan.best = best;
		}
		
		return chess_scan;
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
		if(board.getPieceAt(11) == Pieces.PAWN) result -= 11;
		if(board.getPieceAt(12) == Pieces.PAWN) result -= 11;
		if(board.getPieceAt(4) == Pieces.KING) result += 8;
		
		if(board.getPieceAt(57) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(58) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(61) == -Pieces.BISHOP) result += 10;
		if(board.getPieceAt(62) == -Pieces.KNIGHT) result += 10;
		if(board.getPieceAt(51) == -Pieces.PAWN) result += 11;
		if(board.getPieceAt(52) == -Pieces.PAWN) result += 11;
		if(board.getPieceAt(60) == -Pieces.KING) result -= 8;
		
		return result * penalty;
	}
	
	private static DeltaScan analyseMoves(Chess board) {
		return analyseMoves(board, 1);
	}
	
	private static DeltaScan analyseMoves(Chess board, int depth) {
		Chess copy = board.clone();
		
		// Create a new scanner container
		DeltaScan scan = new DeltaScan(copy);
		
		Set<Move> moves = getAllMoves(board);
		
		// Save the last state of the board
		State state = copy.getState();
		
		for(Move move : moves) {
			copy.doMove(move);
			double percent = getMaterial(copy) + un_developing(move) + non_developing(copy);
			
			if(move.action() == Action.QUEENSIDE_CASTLE || move.action() == Action.KINGSIDE_CASTLE) {
				percent += 50 * (copy.isWhiteTurn() ? -1:1);
			}
			
			DeltaMove delta_move = new DeltaMove(percent, move);
			if(depth > 0) {
				DeltaScan next = analyseMoves(copy, depth - 1);
				double material = next.material();
				percent = (material * 95 + percent * 5) / 100.0;
				delta_move.material = percent;
				
				if(next.best != null) {
					delta_move.next = next.best.move;
				}
			}
			
			// Add this move to the evaluation set
			scan.branches.add(delta_move);
			
			// Undo the move and continue
			copy.setState(state);
		}
		
		{
			List<DeltaMove> evaluation = scan.branches;
			
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
				
				scan.follow.add(new DeltaMove(scan.best.material, scan.best.next));
			}
			
			if(copy.isChecked(!copy.isWhiteTurn())) {
				double delta = copy.isWhiteTurn() ? 1:-1;
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
		}
		
		return scan;
	}
	
	private static List<DeltaMove> cutList(Chess board, DeltaScan scan) {
		List<DeltaMove> nodes = new ArrayList<>();
		
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
		public List<DeltaMove> nodes;
		
		public ChessTree parentTree;
		public DeltaMove parentMove;
		
		public ChessTree(Chess board, DeltaScan scan, DeltaMove pmove, ChessTree parentTree) {
			this.board = board;
			this.nodes = new ArrayList<>();
			this.parentMove = pmove;
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
				this.nodes.add(new DeltaMove(0, move));
			}
		}
		
		public Move getPlayedMove() {
			return board.last_move;
		}
		
		public DeltaMove getBestMove() {
			if(nodes.isEmpty()) return null;
			return board.isWhiteTurn() ? nodes.get(nodes.size() - 1):nodes.get(0);
		}
		
		public double material() {
			if(nodes.isEmpty()) return 0;
			return getBestMove().material;
		}
		
		public int compareTo(ChessTree tree) {
			if(!board.isWhiteTurn()) {
				return Double.compare(tree.material(), material());
			}
			
			return Double.compare(material(), tree.material());
		}
	}
	
	private static class ChessScan {
		public DeltaScan scan;
		public List<ChessTree> trees = new ArrayList<>();
		
		public ChessScan(DeltaScan scan) {
			this.scan = scan;
		}
	}
	
	public static class DeltaScan {
		public Chess board;
		
		/**
		 * The material of the current board
		 */
		public double base;
		
		/**
		 * If it is white to move
		 */
		public final boolean white;
		
		/**
		 * The list of branches
		 */
		public List<DeltaMove> branches = new ArrayList<>();
		
		public List<DeltaMove> follow = new ArrayList<>();
		
		/**
		 * The predicted best move
		 */
		public DeltaMove best;
		
		public DeltaScan(Chess board) {
			this.board = board.clone();
			this.base = getMaterial(board);
			this.white = board.isWhiteTurn();
		}
		
		public double material() {
			if(best != null) {
				return best.material;
			}
			
			return base;
		}
		
		private Scan0 scan;
		public Scan0 toScan0() {
			if(scan == null) {
				scan = new Scan0(board);
			}
			
			scan.base = base;
			if(best != null) {
				scan.best = best.toMove0();
			}
			
			scan.follow.clear();
			scan.branches.clear();
			for(DeltaMove move : follow) {
				Move0 m = move.toMove0();
				if(m == null) continue;
				scan.follow.add(m);
			}
			for(DeltaMove move : branches) {
				Move0 m = move.toMove0();
				if(m == null) continue;
				scan.branches.add(m);
			}
			return scan;
		}
	}
	
	/**
	 * Keeps track of a tree depth
	 */
	private static class DeltaTree {
		public final int depth;
		public final List<ChessTree> nodes;
		
		public DeltaTree(int depth) {
			this.depth = depth;
			this.nodes = new ArrayList<>();
		}

		public boolean isEmpty() {
			return nodes.isEmpty();
		}

		public void addAll(List<ChessTree> trees) {
			nodes.addAll(trees);
		}
	}
	
	public static class DeltaMove implements Comparable<DeltaMove> {
		public double material;
		public Move move;
		public Move next;
		
		public DeltaMove(DeltaScan sc, double material, Move move) {
			this(material, move);
		}
		
		public DeltaMove(double material, Move move) {
			this.material = material;
			this.move = move;
		}
		
		public int compareTo(DeltaMove o) {
			return Double.compare(material, o.material);
		}
		
		public DeltaMove clone() {
			return new DeltaMove(material, move);
		}
		
		public Move0 toMove0() {
			if(move == null) return null;
			return new Move0(material, move);
		}
		
		@Override
		public String toString() {
			if(move == null) return "none";
			return move.toString();
		}
	}
}
