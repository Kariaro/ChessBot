package hardcoded.chess.open;

import static hardcoded.chess.open.Pieces.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChessProcesser {
	static Set<Move> getPieceMoves(Chessboard board, int index) {
		Set<Move> moves = new HashSet<>();
		
		int pieceId = board.getPieceAt(index);
		if((pieceId == 0) || (pieceId > 0 != board.isWhiteTurn())) return moves;
		
		
		switch(ChessUtils.toPiece(pieceId)) {
			case PAWN: getPawnMoves(board, moves, index); break;
			case KNIGHT: getKnightMoves(board, moves, index); break;
			case BISHOP: getBishopMoves(board, moves, index); break;
			case ROOK: getRookMoves(board, moves, index); break;
			case QUEEN: {
				getBishopMoves(board, moves, index);
				getRookMoves(board, moves, index);
				break;
			}
			case KING: getKingMoves(board, moves, index); break;
			default: return moves;
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
	
	static void getPawnMoves(Chessboard board, Set<Move> set, int index) {
		if(board.isWhiteTurn()) {
			getWhitePawnMoves(board, set, index);
		} else {
			getBlackPawnMoves(board, set, index);
		}
	}
	
	static void getWhitePawnMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);

		int rank = index / 8;
		int xpos = index & 7;
		
		if(!board.hasPiece(index + 8)) {
			if(rank == 6) {
				set.add(new Move(QUEEN, index, index + 8, Action.PROMOTE));
				set.add(new Move(ROOK, index, index + 8, Action.PROMOTE));
				set.add(new Move(KNIGHT, index, index + 8, Action.PROMOTE));
				set.add(new Move(BISHOP, index, index + 8, Action.PROMOTE));
			} else {
				set.add(new Move(pieceId, index, index + 8));
			}
			
			if(rank == 1 && !board.hasPiece(index + 16)) {
				set.add(new Move(pieceId, index, index + 16, Action.PAWN_JUMP));
			}
		}
		
		if(xpos > 0) {
			if(board.canTake(index + 7, false)) {
				set.add(new Move(pieceId, index, index + 7));
			}
		}
		
		if(xpos < 7) {
			if(board.canTake(index + 9, false)) {
				set.add(new Move(pieceId, index, index + 9));
			}
		}
		
		if(board.last_move.action() == Action.PAWN_JUMP && board.last_move.id() == -PAWN) {
			if(xpos > 0 && board.last_move.to() == index - 1) {
				set.add(new Move(pieceId, index, index + 7, Action.EN_PASSANT));
			}
			
			if(xpos < 7 && board.last_move.to() == index + 1) {
				set.add(new Move(pieceId, index, index + 9, Action.EN_PASSANT));
			}
		}
	}
	
	static void getBlackPawnMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);

		int rank = index / 8;
		int xpos = index & 7;
		
		if(!board.hasPiece(index - 8)) {
			if(rank == 1) {
				set.add(new Move(-QUEEN, index, index - 8, Action.PROMOTE));
				set.add(new Move(-ROOK, index, index - 8, Action.PROMOTE));
				set.add(new Move(-KNIGHT, index, index - 8, Action.PROMOTE));
				set.add(new Move(-BISHOP, index, index - 8, Action.PROMOTE));
			} else {
				set.add(new Move(pieceId, index, index - 8));
			}
			
			if(rank == 6 && !board.hasPiece(index - 16)) {
				set.add(new Move(pieceId, index, index - 16, Action.PAWN_JUMP));
			}
		}
		
		if(xpos > 0) {
			if(board.canTake(index - 9, false)) {
				set.add(new Move(pieceId, index, index - 9));
			}
		}
		
		if(xpos < 7) {
			if(board.canTake(index - 7, false)) {
				set.add(new Move(pieceId, index, index - 7));
			}
		}
		
		if(board.last_move.action() == Action.PAWN_JUMP && board.last_move.id() == PAWN) {
			if(xpos > 0 && board.last_move.to() == index - 1) {
				set.add(new Move(pieceId, index, index - 9, Action.EN_PASSANT));
			}
			
			if(xpos < 7 && board.last_move.to() == index + 1) {
				set.add(new Move(pieceId, index, index - 7, Action.EN_PASSANT));
			}
		}
	}
	
	static void getKnightMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);
		
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
			
			if(board.canTake(idx, true)) {
				set.add(new Move(pieceId, index, idx));
			}
		}
	}
	
	static void getBishopMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);
		
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
				
				if(!board.canTake(idx, true)) break;
				set.add(new Move(pieceId, index, idx));
				
				if(board.hasPiece(idx)) break;
			}
		}
	}
	
	static void getRookMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);
		
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
				
				if(!board.canTake(idx, true)) break;
				set.add(new Move(pieceId, index, idx));
				
				if(board.hasPiece(idx)) break;
			}
		}
	}
	
	static void getKingMoves(Chessboard board, Set<Move> set, int index) {
		int pieceId = board.getPieceAt(index);
		
		int ypos = index / 8;
		int xpos = index & 7;
		
		for(int j = 0; j < 9; j++) {
			if(j == 4) continue; // Middle
			
			int x = xpos + (j % 3) - 1;
			int y = ypos + (j / 3) - 1;
			if(x < 0 || x > 7 || y < 0 || y > 7) continue;
			int idx = x + y * 8;
			
			if(board.canTake(idx, true)) {
				set.add(new Move(pieceId, index, idx));
			}
		}
		
		// To castle we need to check if the squares inbetween
		// are not checked and that our king and rooks have not
		// previously moved.
		if(pieceId > 0) {
			if(board.isFlagSet(Flags.CASTLE_WQ)) {
				if(!(board.hasPiece(1) || board.hasPiece(2) || board.hasPiece(3)) && board.getPieceAt(0) == ROOK) {
					set.add(new Move(pieceId, index, 2, Action.QUEENSIDE_CASTLE));
				}
			}
			
			if(board.isFlagSet(Flags.CASTLE_WK)) {
				if(!(board.hasPiece(5) || board.hasPiece(6)) && board.getPieceAt(7) == ROOK) {
					set.add(new Move(pieceId, index, 6, Action.KINGSIDE_CASTLE));
				}
			}
		} else {
			
		}
		
		// TODO: Castling
	}
}
