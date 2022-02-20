package me.hardcoded.chess.api;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.uci.Pieces;
import me.hardcoded.chess.utils.ChessUtils;

/**
 * This class is a storage class for a chess move.
 *
 * @author HardCoded
 */
public class ChessMove {
	/**
	 * This field contains the piece that was moved.
	 */
	public final int piece;
	
	/**
	 * This field contains the square that the piece was moved from.
	 */
	public final int from;
	
	/**
	 * This field contains the square that the piece was moved to.
	 */
	public final int to;
	
	/**
	 * This field contains special data for custom moves:
	 *
	 * <ul>
	 *     <li>En passant</li>
	 *     <li>Castling</li>
	 *     <li>Promotion</li>
	 * </ul>
	 */
	public final int special;
	
	public ChessMove(int piece, int from, int to, int special) {
		this.from = from;
		this.to = to;
		this.special = special;
		this.piece =  piece;
	}
	
	@Override
	public String toString() {
		int type = special & 0b11_000000;
		return switch (type) {
			case ChessPieceManager.SM_NORMAL -> Pieces.printable(piece) + "" + ChessUtils.toSquare(from) + "" + ChessUtils.toSquare(to);
			case ChessPieceManager.SM_CASTLING -> ((special & CastlingFlags.ANY_CASTLE_K) != 0) ? "O-O" : "O-O-O";
			case ChessPieceManager.SM_EN_PASSANT -> ChessUtils.toSquare(from) + "" + ChessUtils.toSquare(to) + " (en passant)";
			case ChessPieceManager.SM_PROMOTION -> ChessUtils.toSquare(to) + "=" + Pieces.printable((special >> 3) & 7) + " (promotion)";
			
			// This branch should be impossible to reach because we can only get four different paths.
			// If the engine breaks in the future we will print all internal values to make it easier to debug.
			default -> "from: %d, to: %d, special: %s <UNKNOWN>".formatted(from, to, Integer.toBinaryString(special));
		};
	}
}
