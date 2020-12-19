package hardcoded.chess.open;

/**
 * Used to save the current state of a chess board
 */
public class State {
	protected final Move last_move;
	protected final int flags;
	protected final int[] board;
	
	protected State(Chessboard board) {
		this.last_move = board.last_move;
		this.flags = board.flags;
		this.board = board.board.clone();
	}
	
	protected State(int[] board, int flags, Move last_move) {
		this.last_move = last_move;
		this.flags = flags;
		this.board = board.clone();
	}
	
	public static State of(int[] board, int flags, Move last_move) {
		return new State(board, flags, last_move);
	}
}
