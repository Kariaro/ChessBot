package me.hardcoded.chess.advanced;

public class ChessState {
	private long[] longs;
	private int[] pieces;
	
	public void read(ChessBoard board) {
		if (longs == null) {
			longs = new long[5];
		}
		
		if (pieces == null) {
			pieces = new int[64];
		}
		
		longs[0] = board.whiteMask;
		longs[1] = board.blackMask;
		longs[2] = board.halfMove;
		longs[3] = board.lastPawn;
		longs[4] = board.flags;
		
		System.arraycopy(board.pieces, 0, pieces, 0, 64);
	}
	
	public void write(ChessBoard board) {
		board.whiteMask = longs[0];
		board.blackMask = longs[1];
		board.pieceMask = board.whiteMask | board.blackMask;
		board.halfMove = (int)longs[2];
		board.lastPawn = (int)longs[3];
		board.flags = (int)longs[4];
		System.arraycopy(pieces, 0, board.pieces, 0, 64);
	}
	
	public static ChessState of(ChessBoard board) {
		ChessState state = new ChessState();
		state.read(board);
		return state;
	}
}
