package me.hardcoded.chess.advanced;

import me.hardcoded.chess.decoder.ChessCodec;

import static me.hardcoded.chess.open.Pieces.*;

/**
 * This class contains the information about the current chess board
 * This class does not save information about the game
 * 
 * @author HardCoded
 */
public class ChessBoard {
	final int[] pieces;
	long pieceMask;
	long whiteMask;
	long blackMask;
	int lastCapture;
	int halfMove;
	int lastPawn;
	int flags;
	
	public ChessBoard() {
		pieces = new int[] {
			 ROOK,  KNIGHT,  BISHOP,  KING,  QUEEN,  BISHOP,  KNIGHT,  ROOK,
			 PAWN,    PAWN,    PAWN,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
			-ROOK, -KNIGHT, -BISHOP, -KING, -QUEEN, -BISHOP, -KNIGHT, -ROOK,
		};
		
		// Fast init
		whiteMask = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L;
		blackMask = 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieceMask = whiteMask | blackMask;
		flags = 0b1111; // Castling
		lastCapture = 0;
		halfMove = 0;
		lastPawn = 0;
		
		initializeMasks();
	}
	
	private void initializeMasks() {
		recalculateMasks();
		flags = 0;
		
		// Update castling flags
		if (pieces[CastlingFlags.WHITE_KING] == KING) {
			flags |= ((pieces[CastlingFlags.WHITE_ROOK_K] == ROOK) ? CastlingFlags.WHITE_CASTLE_K : 0)
				   | ((pieces[CastlingFlags.WHITE_ROOK_Q] == ROOK) ? CastlingFlags.WHITE_CASTLE_Q : 0);
		}
		if (pieces[CastlingFlags.BLACK_KING] == -KING) {
			flags |= ((pieces[CastlingFlags.BLACK_ROOK_K] == -ROOK) ? CastlingFlags.BLACK_CASTLE_K : 0)
				   | ((pieces[CastlingFlags.BLACK_ROOK_Q] == -ROOK) ? CastlingFlags.BLACK_CASTLE_Q : 0);
		}
	}
	
	/**
	 * Returns if white has the current move
	 */
	public boolean isWhite() {
		return (halfMove & 1) == 0;
	}
	
	public boolean hasFlags(int flags) {
		return (this.flags & flags) != 0;
	}
	
	public void setPiece(int idx, int piece) {
		int old = pieces[idx];
		long mask = 1L << (long)idx;
		
		pieces[idx] = piece;
		if (old < 0 && piece >= 0) {
			blackMask &= ~mask;
		}
		if (old > 0 && piece <= 0) {
			whiteMask &= ~mask;
		}
		if (piece > 0) {
			whiteMask |= mask;
		}
		if (piece < 0) {
			blackMask |= mask;
		}
		
		pieceMask = blackMask | whiteMask;
	}
	
	@Deprecated
	public ChessBoard creteCopy() {
		// TODO: Fill a class instead of cloning it
		ChessBoard copy = new ChessBoard();
		System.arraycopy(pieces, 0, copy.pieces, 0, 64);
		copy.lastPawn = lastPawn;
		copy.whiteMask = whiteMask;
		copy.blackMask = blackMask;
		copy.pieceMask = pieceMask;
		copy.halfMove = halfMove;
		copy.flags = flags;
		return copy;
	}
	
	public int getPiece(int i) {
		return pieces[i];
	}
	
	/// FOR SERIALIZATION AND DESERIALIZATION
	public void recalculateMasks() {
		whiteMask = 0;
		blackMask = 0;
		for (int i = 0; i < 64; i++) {
			long idx = 1L << i;
			int piece = pieces[i];
			if (piece > 0) {
				whiteMask |= idx;
			}
			
			if (piece < 0) {
				blackMask |= idx;
			}
		}
		pieceMask = whiteMask | blackMask;
	}
	
	@Deprecated
	public int[] getPieces() {
		return pieces;
	}
	
	@Deprecated
	public int getFlags() {
		return flags;
	}
	
	@Deprecated
	public int getLastCapture() {
		return lastCapture;
	}
	
	@Deprecated
	public int getHalfMove() {
		return halfMove;
	}
	
	@Deprecated
	public int getFullMove() {
		return halfMove / 2;
	}
	
	@Deprecated
	public int getLastPawn() {
		return lastPawn;
	}
	
	@Deprecated
	public void setStates(int flags, int halfMove, int lastPawn) {
		this.flags = flags;
		this.halfMove = halfMove;
		this.lastPawn = lastPawn;
	}
}
