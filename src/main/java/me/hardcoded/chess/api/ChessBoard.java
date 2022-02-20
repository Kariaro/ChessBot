package me.hardcoded.chess.api;

/**
 * This interface describes a chess board
 */
public interface ChessBoard {
	
	/**
	 * Returns if white has the current move
	 */
	boolean isWhite();
	
	boolean hasFlags(int flags);
	
	void setPiece(int idx, int piece);
	
	int getPiece(int idx);
}
