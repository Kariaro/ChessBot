package me.hardcoded.chess.open2;

import static me.hardcoded.chess.open.Pieces.*;

import java.util.*;

import me.hardcoded.chess.open.*;

public class ChessProcesser {
	private static final Set<Move> EMPTY = Collections.emptySet();
	
	static Set<Move> getPieceMoves(Chess board, int index) {
		int pieceId = board.getPieceAt(index);
		if((pieceId == 0) || (pieceId > 0 != board.isWhiteTurn())) {
			return EMPTY;
		}
		Set<Move> moves = new HashSet<>();
		
		switch(ChessUtils.toPiece(pieceId)) {
			case PAWN -> getPawnMoves(board, moves, pieceId, index);
			case KNIGHT -> getKnightMoves(board, moves, pieceId, index);
			case BISHOP -> getBishopMoves(board, moves, pieceId, index);
			case ROOK -> getRookMoves(board, moves, pieceId, index);
			case QUEEN -> {
				getBishopMoves(board, moves, pieceId, index);
				getRookMoves(board, moves, pieceId, index);
			}
			case KING -> getKingMoves(board, moves, pieceId, index);
			default -> { return EMPTY; }
		}
		
		Iterator<Move> iter = moves.iterator();
		while(iter.hasNext()) {
			Move move = iter.next();
			
			if(move.to() < 0 || move.to() > 63) {
				iter.remove();
				continue;
			}
			
			if(board.isChecked(move)) {
				iter.remove();
			}
		}
		
		return moves;
	}
	
	static void getPawnMoves(Chess board, Set<Move> set, int pieceId, int index) {
		if(board.isWhiteTurn()) {
			getWhitePawnMoves(board, set, pieceId, index);
		} else {
			getBlackPawnMoves(board, set, pieceId, index);
		}
	}
	
	static void getWhitePawnMoves(Chess board, Set<Move> set, int pieceId, int index) {
		int rank = index / 8;
		int xpos = index & 7;
		
		if(!board.hasPiece(index + 8)) {
			if(rank == 6) {
				set.add(Move.of(QUEEN,  index, index + 8, Action.PROMOTE));
				set.add(Move.of(ROOK,   index, index + 8, Action.PROMOTE));
				set.add(Move.of(KNIGHT, index, index + 8, Action.PROMOTE));
				set.add(Move.of(BISHOP, index, index + 8, Action.PROMOTE));
			} else {
				set.add(Move.of(pieceId, index, index + 8));
			}
			
			if(rank == 1 && !board.hasPiece(index + 16)) {
				set.add(Move.of(pieceId, index, index + 16, Action.PAWN_JUMP));
			}
		}
		
		if(xpos > 0 && board.hasEnemyPiece(index + 7)) {
			if(rank == 6) {
				set.add(Move.of(QUEEN,  index, index + 7, Action.PROMOTE, true));
				set.add(Move.of(ROOK,   index, index + 7, Action.PROMOTE, true));
				set.add(Move.of(KNIGHT, index, index + 7, Action.PROMOTE, true));
				set.add(Move.of(BISHOP, index, index + 7, Action.PROMOTE, true));
			} else {
				set.add(Move.of(pieceId, index, index + 7, true));
			}
		}
		
		if(xpos < 7 && board.hasEnemyPiece(index + 9)) {
			if(rank == 6) {
				set.add(Move.of(QUEEN,  index, index + 9, Action.PROMOTE, true));
				set.add(Move.of(ROOK,   index, index + 9, Action.PROMOTE, true));
				set.add(Move.of(KNIGHT, index, index + 9, Action.PROMOTE, true));
				set.add(Move.of(BISHOP, index, index + 9, Action.PROMOTE, true));
			} else {
				set.add(Move.of(pieceId, index, index + 9, true));
			}
		}
		
		if(board.last_move.action() == Action.PAWN_JUMP && board.last_move.id() == -PAWN) {
			if(xpos > 0 && board.last_move.to() == index - 1) {
				set.add(Move.of(pieceId, index, index + 7, Action.EN_PASSANT));
			}
			
			if(xpos < 7 && board.last_move.to() == index + 1) {
				set.add(Move.of(pieceId, index, index + 9, Action.EN_PASSANT));
			}
		}
	}
	
	static void getBlackPawnMoves(Chess board, Set<Move> set, int pieceId, int index) {
		int rank = index / 8;
		int xpos = index & 7;
		
		if(!board.hasPiece(index - 8)) {
			if(rank == 1) {
				set.add(Move.of(-QUEEN,  index, index - 8, Action.PROMOTE));
				set.add(Move.of(-ROOK,   index, index - 8, Action.PROMOTE));
				set.add(Move.of(-KNIGHT, index, index - 8, Action.PROMOTE));
				set.add(Move.of(-BISHOP, index, index - 8, Action.PROMOTE));
			} else {
				set.add(Move.of(pieceId, index, index - 8));
			}
			
			if(rank == 6 && !board.hasPiece(index - 16)) {
				set.add(Move.of(pieceId, index, index - 16, Action.PAWN_JUMP));
			}
		}
		
		if(xpos > 0 && board.hasEnemyPiece(index - 9)) {
			if(rank == 1) {
				set.add(Move.of(-QUEEN,  index, index - 9, Action.PROMOTE, true));
				set.add(Move.of(-ROOK,   index, index - 9, Action.PROMOTE, true));
				set.add(Move.of(-KNIGHT, index, index - 9, Action.PROMOTE, true));
				set.add(Move.of(-BISHOP, index, index - 9, Action.PROMOTE, true));
			} else {
				set.add(Move.of(pieceId, index, index - 9, true));
			}
		}
		
		if(xpos < 7 && board.hasEnemyPiece(index - 7)) {
			if(rank == 1) {
				set.add(Move.of(-QUEEN,  index, index - 7, Action.PROMOTE, true));
				set.add(Move.of(-ROOK,   index, index - 7, Action.PROMOTE, true));
				set.add(Move.of(-KNIGHT, index, index - 7, Action.PROMOTE, true));
				set.add(Move.of(-BISHOP, index, index - 7, Action.PROMOTE, true));
			} else {
				set.add(Move.of(pieceId, index, index - 7, true));
			}
		}
		
		if(board.last_move.action() == Action.PAWN_JUMP && board.last_move.id() == PAWN) {
			if(xpos > 0 && board.last_move.to() == index - 1) {
				set.add(Move.of(pieceId, index, index - 9, Action.EN_PASSANT));
			}
			
			if(xpos < 7 && board.last_move.to() == index + 1) {
				set.add(Move.of(pieceId, index, index - 7, Action.EN_PASSANT));
			}
		}
	}
	
	static void getKnightMoves(Chess board, Set<Move> set, int pieceId, int index) {
		int[] offset_x = { -1, 1, 2, 2, 1, -1, -2, -2 };
		int[] offset_y = { -2, -2, -1, 1, 2, 2, 1, -1 };
		
		int ypos = index / 8;
		int xpos = index & 7;
		for(int i = 0; i < 8; i++) {
			int xo = offset_x[i];
			int yo = offset_y[i];
			
			int xp = xpos + xo;
			int yp = ypos + yo;
			
			if(xp < 0 || xp > 7 || yp < 0 || yp > 7) continue;
			int idx = xp + yp * 8;
			
			if(board.hasEnemyOrSpace(idx)) {
				set.add(Move.of(pieceId, index, idx, true));
			}
		}
	}
	
	static void getBishopMoves(Chess board, Set<Move> set, int pieceId, int index) {
		int ypos = index / 8;
		int xpos = index & 7;
		
		for(int j = 0; j < 4; j++) {
			int dx = (j % 2) * 2 - 1;
			int dy = (j / 2) * 2 - 1;
			
			for(int i = 1; i < 8; i++) {
				int xp = xpos + dx * i;
				int yp = ypos + dy * i;
				
				if(xp < 0 || xp > 7 || yp < 0 || yp > 7) break;
				int idx = xp + yp * 8;
				
				if(!board.hasEnemyOrSpace(idx)) break;
				set.add(Move.of(pieceId, index, idx, true));
				
				if(board.hasPiece(idx)) break;
			}
		}
	}
	
	static void getRookMoves(Chess board, Set<Move> set, int pieceId, int index) {
		int ypos = index / 8;
		int xpos = index & 7;
		
		for(int j = 0; j < 4; j++) {
			int dr = 1 - (j & 2);
			int dx = ((j    ) & 1) * dr;
			int dy = ((j + 1) & 1) * dr;
			
			for(int i = 1; i < 8; i++) {
				int xp = xpos + dx * i;
				int yp = ypos + dy * i;
				
				if(xp < 0 || xp > 7 || yp < 0 || yp > 7) break;
				int idx = xp + yp * 8;
				
				if(!board.hasEnemyOrSpace(idx)) break;
				set.add(Move.of(pieceId, index, idx, true));
				
				if(board.hasPiece(idx)) break;
			}
		}
	}
	
	static void getKingMoves(Chess board, Set<Move> set, int pieceId, int index) {
		getKingMovesBasic(board, set, pieceId, index);
		
		// To castle we need to check if the squares inbetween
		// are not checked and that our king and rooks have not
		// previously moved.
		
		// FIXME: No castling if the king is checked.
		if(!board.isChecked()) {
			if(pieceId > 0) {
				if(board.isFlagSet(Flags.CASTLE_WQ)) {
					if(!(board.hasPiece(1) || board.hasPiece(2) || board.hasPiece(3)) && board.getPieceAt(0) == ROOK) {
						if(!(board.isAttacked(1, true) || board.isAttacked(2, true) || board.isAttacked(3, true))) {
							set.add(Move.of(pieceId, index, 0, Action.QUEENSIDE_CASTLE));
						}
					}
				}
				
				if(board.isFlagSet(Flags.CASTLE_WK)) {
					if(!(board.hasPiece(5) || board.hasPiece(6)) && board.getPieceAt(7) == ROOK) {
						if(!(board.isAttacked(5, true) || board.isAttacked(6, true))) {
							set.add(Move.of(pieceId, index, 7, Action.KINGSIDE_CASTLE));
						}
					}
				}
			} else {
				if(board.isFlagSet(Flags.CASTLE_BQ)) {
					if(!(board.hasPiece(57) || board.hasPiece(58) || board.hasPiece(59)) && board.getPieceAt(56) == -ROOK) {
						set.add(Move.of(pieceId, index, 56, Action.QUEENSIDE_CASTLE));
					}
				}
				
				if(board.isFlagSet(Flags.CASTLE_BK)) {
					if(!(board.hasPiece(61) || board.hasPiece(62)) && board.getPieceAt(63) == -ROOK) {
						set.add(Move.of(pieceId, index, 63, Action.KINGSIDE_CASTLE));
					}
				}
			}
		}
	}
	
	static void getKingMovesBasic(Chess board, Set<Move> set, int pieceId, int index) {
		int ypos = index / 8;
		int xpos = index & 7;
		
		for(int j = 0; j < 9; j++) {
			if(j == 4) continue; // Middle
			
			int x = xpos + (j % 3) - 1;
			int y = ypos + (j / 3) - 1;
			if(x < 0 || x > 7 || y < 0 || y > 7) continue;
			int idx = x + y * 8;
			
			if(board.hasEnemyOrSpace(idx)) {
				set.add(Move.of(pieceId, index, idx, true));
			}
		}
	}
}
