package me.hardcoded.chess.open;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to save the current state of a chess board
 */
public class State {
	public final Move last_move;
	public final int flags;
	public final int[] board;
	public final int halfmove;
	public final int fullmove;
	public final List<Move> lastMoves;
	
	protected State(Chessboard board) {
		this.last_move = board.last_move;
		this.flags = board.flags;
		this.board = board.board.clone();
		this.halfmove = 0;
		this.fullmove = 0;
		this.lastMoves = new ArrayList<>();
	}
	
	protected State(int[] board, int flags, Move last_move, int halfmove, int fullmove) {
		this.last_move = last_move;
		this.flags = flags;
		this.board = board.clone();
		this.halfmove = halfmove;
		this.fullmove = fullmove;
		this.lastMoves = new ArrayList<>();
	}
	
	protected State(int[] board, int flags, Move last_move, int halfmove, int fullmove, List<Move> lastMoves) {
		this.last_move = last_move;
		this.flags = flags;
		this.board = board.clone();
		this.halfmove = halfmove;
		this.fullmove = fullmove;
		this.lastMoves = Collections.unmodifiableList(lastMoves);
	}
	
	public int getFlags() {
		return flags;
	}
	
	public static State of(int[] board, int flags, Move last_move) {
		return new State(board, flags, last_move, 0, 0);
	}
	
	public static State of(int[] board, int flags, Move last_move, int halfmove, int fullmove) {
		return new State(board, flags, last_move, halfmove, fullmove);
	}
	
	public static State of(int[] board, int flags, Move last_move, int halfmove, int fullmove, List<Move> lastMoves) {
		return new State(board, flags, last_move, halfmove, fullmove, lastMoves);
	}
}
