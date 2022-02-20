package me.hardcoded.chess.advanced;

public class ChessState {
	private long[] longs;
	private int[] pieces;
	
	public void read(ChessBoardImpl board) {
		if (longs == null) {
			longs = new long[6];
		}
		
		if (pieces == null) {
			pieces = new int[64];
		}
		
		longs[0] = board.whiteMask;
		longs[1] = board.blackMask;
		longs[2] = board.lastCapture;
		longs[3] = board.halfMove;
		longs[4] = board.lastPawn;
		longs[5] = board.flags;
		
		System.arraycopy(board.pieces, 0, pieces, 0, 64);
	}
	
	public void write(ChessBoardImpl board) {
		board.whiteMask = longs[0];
		board.blackMask = longs[1];
		board.pieceMask = board.whiteMask | board.blackMask;
		board.lastCapture = (int)longs[2];
		board.halfMove = (int)longs[3];
		board.lastPawn = (int)longs[4];
		board.flags = (int)longs[5];
		
		// We could optimize this with the masks?
		System.arraycopy(pieces, 0, board.pieces, 0, 64);
	}
	
	public static ChessState of(ChessBoardImpl board) {
		ChessState state = new ChessState();
		state.read(board);
		return state;
	}
}
