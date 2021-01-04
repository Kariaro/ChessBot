package hardcoded.chess.open2;

import static hardcoded.chess.open.Flags.*;
import static hardcoded.chess.open.Pieces.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import hardcoded.chess.decoder.BoardUtils;
import hardcoded.chess.open.Action;
import hardcoded.chess.open.Flags;
import hardcoded.chess.open.Move;
import hardcoded.chess.open.State;

public class Chess {
	public static final State DEFAULT = BoardUtils.FEN.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	
	protected final int[] board = new int[64];
	protected Move last_move = Move.INVALID;
	protected int flags = Flags.DEFAULT;
	// TODO: Fifty Move rule
	// TODO: Threefold repetition
	
	public Chess() {
		this(DEFAULT);
	}
	
	protected Chess(State state) {
		setState(state);
	}
	
	public int getPieceAt(int index) {
		if(index < 0 || index > 63) return 0;
		return board[index];
	}
	
	protected void setFlagsBit(int mask, boolean set) {
		flags &= ~mask;
		if(set) flags |= mask;
	}
	
	protected void setFlagsBit(int mask, int set) {
		flags &= ~mask;
		if(set != 0) flags |= mask;
	}
	
	public boolean isFlagSet(int mask) {
		return (flags & mask) != 0;
	}
	
	public boolean isWhiteTurn() {
		return isFlagSet(TURN);
	}
	
	public Chess clone() {
		return new Chess(getState());
	}
	
	protected boolean hasPiece(int index) {
		return getPieceAt(index) != 0;
	}
	
	protected boolean hasEnemyOrSpace(int index) {
		int piece = getPieceAt(index);
		if(piece == 0) return true;
		return isWhiteTurn() ? (piece < 0):(piece > 0);
	}
	
	protected boolean hasEnemyPiece(int index) {
		int piece = getPieceAt(index);
		return isWhiteTurn() ? (piece < 0):(piece > 0);
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
	
	public State getState() {
		return State.of(board, flags, last_move, 0, 0);
	}
	
	public void setState(State state) {
		for(int i = 0; i < 64; i++) board[i] = state.board[i];
		last_move = state.last_move;
		flags = state.flags;
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
	
	public boolean isChecked(boolean white) {
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
		ChessProcesser.getRookMoves(this, moves, 0, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == ROOK) return true;
		}
		
		moves.clear();
		ChessProcesser.getKnightMoves(this, moves, 0, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KNIGHT) return true;
		}

		moves.clear();
		ChessProcesser.getBishopMoves(this, moves, 0, king_idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == BISHOP) return true;
		}

		moves.clear();
		ChessProcesser.getKingMovesBasic(this, moves, 0, king_idx);
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
		ChessProcesser.getRookMoves(this, moves, 0, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == ROOK) return true;
		}
		
		moves.clear();
		ChessProcesser.getKnightMoves(this, moves, 0, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == KNIGHT) return true;
		}

		moves.clear();
		ChessProcesser.getBishopMoves(this, moves, 0, idx);
		for(Move m : moves) {
			int pieceId = getPieceAt(m.to()) * pm;
			if(pieceId == QUEEN || pieceId == BISHOP) return true;
		}

		moves.clear();
		ChessProcesser.getKingMovesBasic(this, moves, 0, idx);
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
		if(index < 0 || index > 63) return Collections.emptySet();
		return ChessProcesser.getPieceMoves(this, index);
	}
	
	public synchronized void doMove(Move move) {
		doMove(move, true);
	}
	
	protected State ls;
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
			int turn = (isWhiteTurn() ? 1:-1);
			board[move.to()    ] = 0;
			board[move.to() - 1] = KING * turn;
			board[move.to() - 2] = ROOK * turn;
			board[move.to() - 3] = 0;
		}
		
		if(move.action() == Action.QUEENSIDE_CASTLE) {
			int turn = (isWhiteTurn() ? 1:-1);
			board[move.to()    ] = 0;
			board[move.to() + 2] = KING * turn;
			board[move.to() + 3] = ROOK * turn;
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