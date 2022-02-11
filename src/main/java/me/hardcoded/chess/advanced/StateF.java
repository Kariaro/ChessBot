package me.hardcoded.chess.advanced;

public class StateF {
	private long[] longs;
	
	public void read(ChessBoard board) {
		longs = new long[] {
			board.whiteMask, board.whiteMask,
			board.halfMove
		};
	}
	
	public void write(ChessBoard board) {
		board.whiteMask = longs[0];
		board.blackMask = longs[1];
		board.pieceMask = board.whiteMask | board.blackMask;
		board.halfMove = (int)longs[2];
	}
	
	public static StateF of(ChessBoard board) {
		StateF state = new StateF();
		state.read(board);
		return state;
	}
}
