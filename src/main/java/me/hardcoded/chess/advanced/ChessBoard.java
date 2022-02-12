package me.hardcoded.chess.advanced;

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
	long flags;
	int halfMove;
	int lastPawn;
	
	public ChessBoard() {
//		pieces = new int[] {
//			 ROOK,  KNIGHT,  BISHOP,  KING,  QUEEN,  BISHOP,  KNIGHT,  ROOK,
//			 PAWN,    PAWN,    PAWN,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
//			-ROOK, -KNIGHT, -BISHOP, -KING, -QUEEN, -BISHOP, -KNIGHT, -ROOK,
//		};
		
		pieces = new int[] {
			ROOK,       0,       0,  KING,  QUEEN,  BISHOP,       0,  ROOK,
			PAWN,    PAWN,    PAWN,     0,   PAWN,    PAWN,    PAWN,  PAWN,
			   0,       0,  KNIGHT,  PAWN,      0,       0,       0,     0,
			   0,       0,       0, -PAWN,      0,       0, -BISHOP,     0,
			   0,       0,       0,     0, KNIGHT,       0,  BISHOP,     0,
		 -KNIGHT,       0,       0,     0,      0, -KNIGHT,       0,     0,
		   -PAWN,   -PAWN,   -PAWN,     0,  -PAWN,   -PAWN,   -PAWN, -PAWN,
		   -ROOK,       0,       0, -KING, -QUEEN, -BISHOP,       0,     -ROOK,
		};
		
//		ChessGenerator.debug("Board", pieces);
		
		// Fast init
		whiteMask = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L;
		blackMask = 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieceMask = whiteMask | blackMask;
		flags = 0b1111; // Castling
		halfMove = 0;
		lastPawn = 0;
		
//		pieces = new int[] {
//			 ROOK,       0,       0,  KING,      0,       0,       0,  ROOK,
//			 PAWN,    PAWN,    PAWN,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
//			-ROOK,       0,       0, -KING,      0,       0,       0, -ROOK,
//		};
		
//		pieces = new int[] {
//			 ROOK,       0,       0,  KING,      0,       0,       0,  ROOK,
//			    0,   -PAWN,       0,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
//				0,       0,       0,     0,      0, -KNIGHT,       0,     0,
//				0,       0,       0,     0,      0,       0,       0,     0,
//			    0,   -ROOK,   -ROOK,     0,      0,       0,       0,     0,
//				0,       0,       0,     0,      0,       0,       0,     0,
//				0,       0,       0,     0,      0,       0,       0,     0,
//				0,       0,       0,     0,      0,       0,       0,     0,
////			    0,       0,       0,     0,      0,       0,       0,     0,
////			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
////			-ROOK,       0,       0, -KING,      0,       0,       0, -ROOK,
//		};
		
//		pieces = new int[] {
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//		-ROOK,    PAWN,    KING,     0,   PAWN,       0,       0,     0,
//			0,       0,       0,     0,      0, -KNIGHT,       0,     0,
//		};
		
		initializeMasks();
	}
	
	private void initializeMasks() {
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
}
