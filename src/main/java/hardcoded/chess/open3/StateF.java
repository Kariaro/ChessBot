package hardcoded.chess.open3;

public class StateF {
	private long[] longs;
	
	public void read(ChessB board) {
		longs = new long[] {
			board.white_mask, board.white_mask,
			board.halfmove
		};
	}
	
	public void write(ChessB board) {
		board.white_mask = longs[0];
		board.black_mask = longs[1];
		board.piece_mask = board.white_mask | board.black_mask;
		board.halfmove = (int)longs[2];
	}
	
	public static StateF of(ChessB board) {
		StateF state = new StateF();
		state.read(board);
		return state;
	}
}
