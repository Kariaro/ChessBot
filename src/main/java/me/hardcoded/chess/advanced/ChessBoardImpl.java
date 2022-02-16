package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.decoder.ChessCodec;

import static me.hardcoded.chess.open.Pieces.*;

/**
 * This class contains the information about the current chess board
 * This class does not save information about the game
 * 
 * @author HardCoded
 */
public class ChessBoardImpl implements ChessBoard {
	public final int[] pieces;
	public long pieceMask;
	public long whiteMask;
	public long blackMask;
	public int lastCapture;
	public int halfMove;
	public int lastPawn;
	public int flags;
	
	public ChessBoardImpl() {
		this.pieces = new int[] {
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
		flags = 0b1111;
		lastCapture = 0;
		halfMove = 0;
		lastPawn = 0;
	}
	
	public ChessBoardImpl(String fen) {
		this.pieces = new int[64];
		ChessCodec.FEN.load(this, fen);
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
	
	@Override
	public boolean isWhite() {
		return (halfMove & 1) == 0;
	}
	
	@Override
	public boolean hasFlags(int flags) {
		return (this.flags & flags) != 0;
	}
	
	@Override
	public void setPiece(int idx, int piece) {
		int old = pieces[idx];
		long mask = 1L << idx;
		
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
	public ChessBoardImpl creteCopy() {
		// TODO: Fill a class instead of cloning it
		ChessBoardImpl copy = new ChessBoardImpl();
		System.arraycopy(pieces, 0, copy.pieces, 0, 64);
		copy.lastPawn = lastPawn;
		copy.whiteMask = whiteMask;
		copy.blackMask = blackMask;
		copy.pieceMask = pieceMask;
		copy.halfMove = halfMove;
		copy.lastCapture = lastCapture;
		copy.flags = flags;
		return copy;
	}
	
	@Override
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
	
	public int[] getPieces() {
		return pieces;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public int getLastCapture() {
		return lastCapture;
	}
	
	public int getHalfMove() {
		return halfMove;
	}
	
	public int getFullMove() {
		return halfMove / 2;
	}
	
	public int getLastPawn() {
		return lastPawn;
	}
	
	public void setStates(int flags, int halfMove, int lastPawn, int lastCapture, int[] pieces) {
		this.flags = flags;
		this.halfMove = halfMove;
		this.lastPawn = lastPawn;
		this.lastCapture = lastCapture;
		System.arraycopy(pieces, 0, this.pieces, 0, 64);
		
		// Recalculate the piece masks
		recalculateMasks();
	}
}
