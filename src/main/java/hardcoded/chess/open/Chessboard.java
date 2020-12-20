package hardcoded.chess.open;

import static hardcoded.chess.open.Flags.*;
import static hardcoded.chess.open.Pieces.*;

import java.util.HashSet;
import java.util.Set;

import hardcoded.chess.decoder.BoardUtils;

public class Chessboard {
	public static final State DEFAULT = BoardUtils.FEN.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	
	protected final int[] board;
	protected int flags = (1 | 2 | 4 | 8 | 16);
	protected Move last_move = Move.INVALID;
	
	protected State ls;
	
	public Chessboard() {
		this(
			"RNBQKBNR" +
			"PPPPPPPP" +
			"        " +
			"        " +
			"        " +
			"        " +
			"pppppppp" +
			"rnbqkbnr"
		);
		
//		this(
//			"R   K  R" +
//			"    P   " +
//			"    r   " +
//			"    r   " +
//			"    r   " +
//			"    r   " +
//			"    p  R" +
//			"r   k   "
//		);
		
//		this(
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"       p" +
//			"        " +
//			"k      K"
//		);
//		
//		setFlagsBit(CASTLE_BK, false);
//		setFlagsBit(CASTLE_BQ, false);
//		setFlagsBit(CASTLE_WK, false);
//		setFlagsBit(CASTLE_WQ, false);
		
		
//		this(
//			"K       " +
//			"     p  " +
//			" k      " +
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"        "
//		);
		
//		this(
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"        " +
//			"PPK    k" +
//			"        "
//		);
	}
	
	public Chessboard(String str) {
		if(str.length() != 64) throw new IllegalArgumentException();
		board = new int[64];
		
		for(int i = 0; i < 64; i++) {
			boolean lower = Character.isLowerCase(str.charAt(i));
			char c = Character.toUpperCase(str.charAt(i));
			switch(c) {
				case 'K': board[i] = (lower ? -1:1) * KING; break;
				case 'Q': board[i] = (lower ? -1:1) * QUEEN; break;
				case 'B': board[i] = (lower ? -1:1) * BISHOP; break;
				case 'N': board[i] = (lower ? -1:1) * KNIGHT; break;
				case 'R': board[i] = (lower ? -1:1) * ROOK; break;
				case 'P': board[i] = (lower ? -1:1) * PAWN; break;
			}
		}
	}
	
	protected Chessboard(State state) {
		board = new int[64];
		setState(state);
	}
	
	public Chessboard clone() {
		return new Chessboard(getState());
	}
	
	public boolean isPromoting() {
		return last_move.action() == Action.PROMOTE;
	}
	
	public int getPieceAt(int index) {
		if(index < 0 || index > 63) return 0;
		return board[index];
	}
	
	protected void setFlagsBit(int mask, boolean set) {
		flags &= ~mask;
		if(set) flags |= mask;
	}
	
	protected boolean isFlagSet(int mask) {
		return (flags & mask) != 0;
	}
	
	public boolean isWhiteTurn() {
		return isFlagSet(TURN);
	}
	
	protected boolean hasPiece(int index) {
		return getPieceAt(index) != 0;
	}
	
	public int findPiece(int id, int skip) {
		int count = 0;
		for(int i = 0; i < 64; i++) {
			int p = board[i];
			if(p == id) {
				if(count++ >= skip) return i;
			}
		}
		
		return -1;
	}
	
	protected boolean canTake(int index, boolean allowEmpty) {
		int pieceId = getPieceAt(index);
		if(pieceId == 0) return allowEmpty;
		if(isWhiteTurn()) return pieceId < 0;
		return pieceId > 0;
	}
	
	public boolean isChecked() {
		State ls = getState();
		boolean result = isChecked(!isWhiteTurn());
		setState(ls);
		return result;
	}
	
	// Returns true if the king is in check if this move was made.
	protected boolean isChecked(Move move) {
		State ls = getState();
		doMove(move, false);
		boolean result = isChecked(isWhiteTurn());
		setState(ls);
		return result;
	}
	
	protected boolean isChecked(boolean white) {
		int pm = 1;
		int king_idx = 0;
		
		if(white) {
			king_idx = findPiece(-KING, 0);
			setFlagsBit(TURN, false);
		} else {
			king_idx = findPiece(KING, 0);
			setFlagsBit(TURN, true);
			pm = -1;
		}
		
		if(king_idx == -1) {
			return true;
		}
		
		Set<Move> moves = new HashSet<>();
		ChessProcesser.getRookMoves(this, moves, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == ROOK) return true;
		}
		
		moves.clear();
		ChessProcesser.getKnightMoves(this, moves, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KNIGHT) return true;
		}

		moves.clear();
		ChessProcesser.getBishopMoves(this, moves, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == BISHOP) return true;
		}

		moves.clear();
		ChessProcesser.getKingMovesBasic(this, moves, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KING) return true;
		}
		
		if(isWhiteTurn()) {
			int xpos = king_idx & 7;
			if(xpos > 0 && getPieceAt(king_idx + 7) == -PAWN) return true;
			if(xpos < 7 && getPieceAt(king_idx + 9) == -PAWN) return true;
		} else {
			int xpos = king_idx & 7;
			if(xpos > 0 && getPieceAt(king_idx - 9) == PAWN) return true;
			if(xpos < 7 && getPieceAt(king_idx - 7) == PAWN) return true;
		}
		
		return false;
	}
	
	
	public boolean isAttacked(int idx, boolean white) {
		State state = getState();
		boolean result = isAttacked0(idx, white);
		setState(state);
		return result;
	}
	
	private boolean isAttacked0(int idx, boolean white) {
		setFlagsBit(TURN, white);
		int pm = white ? -1:1;
		
		Set<Move> moves = new HashSet<>();
		ChessProcesser.getRookMoves(this, moves, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == ROOK) return true;
		}
		
		moves.clear();
		ChessProcesser.getKnightMoves(this, moves, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KNIGHT) return true;
		}

		moves.clear();
		ChessProcesser.getBishopMoves(this, moves, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == BISHOP) return true;
		}

		moves.clear();
		ChessProcesser.getKingMovesBasic(this, moves, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KING) return true;
		}
		
		if(isWhiteTurn()) {
			int xpos = idx & 7;
			if(xpos > 0 && getPieceAt(idx + 7) == -PAWN) return true;
			if(xpos < 7 && getPieceAt(idx + 9) == -PAWN) return true;
		} else {
			int xpos = idx & 7;
			if(xpos > 0 && getPieceAt(idx - 9) == PAWN) return true;
			if(xpos < 7 && getPieceAt(idx - 7) == PAWN) return true;
		}
		
		return false;
	}
	
	public synchronized Set<Move> getPieceMoves(int index) {
		if(index < 0 || index > 63) return new HashSet<>();
		return ChessProcesser.getPieceMoves(this, index);
	}
	
	public State getState() {
		return new State(this);
	}
	
	public void setState(State state) {
		for(int i = 0; i < 64; i++) board[i] = state.board[i];
		last_move = state.last_move;
		flags = state.flags;
	}
	
	public synchronized void doMove(Move move) {
		doMove(move, true);
	}
	
	protected synchronized void doMove(Move move, boolean updateLastState) {
		if(updateLastState) {
			ls = getState();
		}
		
		int p0 = move.id();
		board[move.to()] = p0;
		board[move.from()] = 0;
		
		if(move.action() == Action.EN_PASSANT) {
			board[move.to() - (isWhiteTurn() ? 8:-8)] = 0;
		}
		
		if(move.action() == Action.KINGSIDE_CASTLE) {
			board[move.to() + 1] = 0;
			board[move.to() - 1] = ROOK * (isWhiteTurn() ? 1:-1);
		}
		
		if(move.action() == Action.QUEENSIDE_CASTLE) {
			board[move.to() - 2] = 0;
			board[move.to() + 1] = ROOK * (isWhiteTurn() ? 1:-1);
		}
		
		if(p0 == -KING) {
			setFlagsBit(CASTLE_BK, false);
			setFlagsBit(CASTLE_BQ, false);
		}
		if(p0 == KING) {
			setFlagsBit(CASTLE_WK, false);
			setFlagsBit(CASTLE_WQ, false);
		}
		
		if(p0 == -ROOK) {
			if(move.from() == 63) setFlagsBit(CASTLE_BK, false);
			if(move.from() == 56) setFlagsBit(CASTLE_BQ, false);
		}
		if(p0 == ROOK) {
			if(move.from() == 0) setFlagsBit(CASTLE_WQ, false);
			if(move.from() == 7) setFlagsBit(CASTLE_WK, false);
		}
		
		setFlagsBit(TURN, !isWhiteTurn());
		last_move = move;
	}

	public Move getLastMove() {
		return last_move;
	}
}