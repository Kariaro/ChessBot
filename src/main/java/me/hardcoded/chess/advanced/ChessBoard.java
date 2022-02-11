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
	long piece_mask;
	long white_mask;
	long black_mask;
	long flags;
	int halfmove;
	int lastpawn;
	
	public ChessBoard() {
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
		white_mask = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L;
		black_mask = 0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		piece_mask = white_mask | black_mask;
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
		
		pieces = new int[] {
			 ROOK,       0,       0,  KING,      0,       0,       0,  ROOK,
			    0,   -PAWN,       0,  PAWN,   PAWN,    PAWN,    PAWN,  PAWN,
				0,       0,       0,     0,      0, -KNIGHT,       0,     0,
				0,       0,       0,     0,      0,       0,       0,     0,
//			 PAWN,       0,       0,     0,      0,       0,       0,     0,
			    0,   -ROOK,   -ROOK,     0,      0,       0,       0,     0,
				0,       0,       0,     0,      0,       0,       0,     0,
				0,       0,       0,     0,      0,       0,       0,     0,
				0,       0,       0,     0,      0,       0,       0,     0,
//			    0,       0,       0,     0,      0,       0,       0,     0,
//			-PAWN,   -PAWN,   -PAWN, -PAWN,  -PAWN,   -PAWN,   -PAWN, -PAWN,
//			-ROOK,       0,       0, -KING,      0,       0,       0, -ROOK,
		};
		
//		pieces = new int[] {
//			0,       0,       0,  KING,      0,       0,       0,  ROOK,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//			0,       0,       0,     0,      0,       0,       0,     0,
//		};
		
		initializeMasks();
	}
	
	private void initializeMasks() {
		white_mask = 0;
		black_mask = 0;
		for (int i = 0; i < 64; i++) {
			long idx = 1L << (long)i;
			int piece = pieces[i];
			if (piece > 0) {
				white_mask |= idx;
			}
			
			if (piece < 0) {
				black_mask |= idx;
			}
		}
		piece_mask = white_mask | black_mask;
		flags = 0;
		
		// Update castling flags
		if (pieces[3] == KING) {
			flags |= ((pieces[0] == ROOK) ? CastlingFlags.WHITE_CASTLE_K : 0)
				   | ((pieces[7] == ROOK) ? CastlingFlags.WHITE_CASTLE_Q : 0);
		}
		if (pieces[59] == KING && pieces[0] == ROOK) {
			flags |= ((pieces[56] == ROOK) ? CastlingFlags.BLACK_CASTLE_K : 0)
				   | ((pieces[63] == ROOK) ? CastlingFlags.BLACK_CASTLE_Q : 0);
		}
	}
	
	/**
	 * Returns if white has the current move
	 */
	public boolean isWhite() {
		return (halfmove & 1) == 0;
	}
	
	public boolean hasFlags(int flags) {
		return (this.flags & flags) != 0;
	}
	
	public void swap(int idx, int piece) {
		int old = pieces[idx];
		long mask = 1L << (long)idx;
		
	}
	
	public int getPiece(int i) {
		return pieces[i];
	}
}
