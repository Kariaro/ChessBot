package me.hardcoded.chess.open3;

import static me.hardcoded.chess.open.Pieces.*;
import static me.hardcoded.chess.open3.FlagsF.*;

public class ChessB {
	protected final int[] pieces;
	protected long piece_mask;
	protected long white_mask;
	protected long black_mask;
	protected long flags;
	protected int halfmove;
	protected int lastpawn;
	
	public ChessB() {
		/*pieces = new int[] {
			 ROOK,  KNIGHT,  BISHOP,  KING,  QUEEN,  BISHOP,  KNIGHT,  ROOK,
			 PAWN,    PAWN,    PAWN,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
			-ROOK, -KNIGHT, -BISHOP, -KING, -QUEEN, -BISHOP, -KNIGHT, -ROOK,
		};*/
		
		// Fast init
		piece_mask = 0b11111111_11111111_00000000_00000000_00000000_00000000_11111111_11111111L;
		white_mask = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L;
		black_mask = 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		flags = 0b1111; // Castling
		halfmove = 0;
		lastpawn = 0;
		
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
//			 PAWN,       0,       0,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			    0,   -ROOK,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
//			-ROOK,       0,       0, -KING,      0,       0,       0, -ROOK,
//		};
		
		pieces = new int[] {
			    0,    PAWN,       0,  KING,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
		  -KNIGHT,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0, -BISHOP,     0,
			    0,   -ROOK,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
			    0,       0,       0,     0,      0,       0,       0,     0,
		};
		
		
		// For debug
		init_masks();
	}
	
	void init_masks() {
		white_mask = 0;
		black_mask = 0;
		for(int i = 0; i < 64; i++) {
			long idx = 1L << (i + 0L);
			int piece = pieces[i];
			if(piece > 0) white_mask |= idx;
			if(piece < 0) black_mask |= idx;
		}
		
		piece_mask = white_mask | black_mask;
	}
	
	/**
	 * Returns if it's whites move to play
	 */
	public boolean isWhite() {
		return (halfmove & 1) == 0;
	}
	
	public boolean canWC()  { return (flags & WHITE_CASTLE_BOTH) != 0; }
	public boolean canWCK() { return (flags & WHITE_CASTLE_KING) != 0; }
	public boolean canWCQ() { return (flags & WHITE_CASTLE_QUEEN) != 0; }
	public boolean canBC()  { return (flags & BLACK_CASTLE_BOTH) != 0; }
	public boolean canBCK() { return (flags & BLACK_CASTLE_KING) != 0; }
	public boolean canBCQ() { return (flags & BLACK_CASTLE_QUEEN) != 0; }
	
	
	public int getPiece(int i) {
		return this.pieces[i];
	}
}
