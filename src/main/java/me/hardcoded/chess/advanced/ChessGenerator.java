package me.hardcoded.chess.advanced;

import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class will generate all moves possible
 *
 * @author HardCoded
 */
public class ChessGenerator {
	public static Set<ChessMove> generateGuiMoves(ChessBoard board) {
		final Set<ChessMove> moves = new HashSet<>();
		
		generate(board, true, (fromIdx, toIdx, special) -> {
			if (!isValid(board, fromIdx, toIdx, special)) {
				return true;
			}
			
			int piece = board.getPiece(fromIdx);
			moves.add(new ChessMove(piece, fromIdx, toIdx, special));
			return true;
		});
		
		return moves;
	}
	
	public static void generate(ChessBoard board, ChessConsumer consumer) {
		generate(board, false, consumer);
	}
	
	
	public static void generateQuiesce(ChessBoard board, ChessConsumer consumer) {
		final ChessBoardImpl b = (ChessBoardImpl)board;
		boolean isWhite = b.isWhite();
		
		long quiesceMask;
		long mask;
		if (isWhite) {
			mask = b.whiteMask;
			quiesceMask = b.blackMask;
		} else {
			mask = b.blackMask;
			quiesceMask = b.whiteMask;
		}
		
		boolean stopRunning = false;
		
		while (mask != 0) {
			// If we want to stop checking moves we exit
			if (stopRunning) {
				break;
			}
			
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = b.pieces[idx];
			// Quiesce moves can only capture other pieces
			long moves = ChessPieceManager.piece_move(b, piece, idx) & quiesceMask;
			
			while (moves != 0) {
				long move_bit = Long.lowestOneBit(moves);
				moves &= ~move_bit;
				int move_idx = Long.numberOfTrailingZeros(move_bit);
				stopRunning = stopRunning || !consumer.accept(idx, move_idx, 0);
			}
			
			if (piece * piece == 1 || piece * piece == 36) {
				int special = ChessPieceManager.special_piece_move(b, piece, isWhite, idx);
				int type = special & 0b11000000;
				if (type == ChessPieceManager.SM_EN_PASSANT) {
					stopRunning = stopRunning || !consumer.accept(idx, special & 0b111111, special);
				} else if (type == ChessPieceManager.SM_PROMOTION) {
					// Split promotion into multiple moves
					int specialFlag;
					int toIdx = idx + (isWhite ? 8 : -8);
					if ((specialFlag = (special & ChessPieceManager.PROMOTION_LEFT)) != 0) {
						for (int promotionPiece : Pieces.PROMOTION) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx - 1, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
						}
					}
					if ((specialFlag = (special & ChessPieceManager.PROMOTION_MIDDLE)) != 0) {
						for (int promotionPiece : Pieces.PROMOTION) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
						}
					}
					if ((specialFlag = (special & ChessPieceManager.PROMOTION_RIGHT)) != 0) {
						for (int promotionPiece : Pieces.PROMOTION) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx + 1, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
						}
					}
				}
			}
		}
	}
	
	/**
	 * If gui promotion is {@code true} then only one move will be shown for promotion instead of all piece types
	 */
	public static void generate(ChessBoard board, boolean guiPromotion, ChessConsumer consumer) {
		final ChessBoardImpl b = (ChessBoardImpl)board;
		boolean isWhite = b.isWhite();
		
		long mask;
		if (isWhite) {
			mask = b.whiteMask;
		} else {
			mask = b.blackMask;
		}
		
		boolean stopRunning = false;
		
		while (mask != 0) {
			// If we want to stop checking moves we exit
			if (stopRunning) {
				break;
			}
			
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = b.pieces[idx];
			long moves = ChessPieceManager.piece_move(b, piece, idx);
			
			while (moves != 0) {
				long move_bit = Long.lowestOneBit(moves);
				moves &= ~move_bit;
				int move_idx = Long.numberOfTrailingZeros(move_bit);
				stopRunning = stopRunning || !consumer.accept(idx, move_idx, 0);
			}
			
			if (piece * piece == 1 || piece * piece == 36) {
				int special = ChessPieceManager.special_piece_move(b, piece, isWhite, idx);
				int type = special & 0b11000000;
				if (type == ChessPieceManager.SM_CASTLING) {
					// Split the castling moves up into multiple moves
					int specialFlag;
					if ((specialFlag = (special & CastlingFlags.ANY_CASTLE_K)) != 0) {
						stopRunning = stopRunning || !consumer.accept(idx, idx + 2, ChessPieceManager.SM_CASTLING | specialFlag);
					}
					if ((specialFlag = (special & CastlingFlags.ANY_CASTLE_Q)) != 0) {
						stopRunning = stopRunning || !consumer.accept(idx, idx - 2, ChessPieceManager.SM_CASTLING | specialFlag);
					}
				} else if (type == ChessPieceManager.SM_EN_PASSANT) {
					stopRunning = stopRunning || !consumer.accept(idx, special & 0b111111, special);
				} else if (type == ChessPieceManager.SM_PROMOTION) {
					// Split promotion into multiple moves
					int specialFlag;
					int toIdx = idx + (isWhite ? 8 : -8);
					if (guiPromotion) {
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_LEFT)) != 0) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx - 1, ChessPieceManager.SM_PROMOTION | specialFlag);
						}
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_MIDDLE)) != 0) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx, ChessPieceManager.SM_PROMOTION | specialFlag);
						}
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_RIGHT)) != 0) {
							stopRunning = stopRunning || !consumer.accept(idx, toIdx + 1, ChessPieceManager.SM_PROMOTION | specialFlag);
						}
					} else {
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_LEFT)) != 0) {
							for (int promotionPiece : Pieces.PROMOTION) {
								stopRunning = stopRunning || !consumer.accept(idx, toIdx - 1, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
							}
						}
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_MIDDLE)) != 0) {
							for (int promotionPiece : Pieces.PROMOTION) {
								stopRunning = stopRunning || !consumer.accept(idx, toIdx, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
							}
						}
						if ((specialFlag = (special & ChessPieceManager.PROMOTION_RIGHT)) != 0) {
							for (int promotionPiece : Pieces.PROMOTION) {
								stopRunning = stopRunning || !consumer.accept(idx, toIdx + 1, ChessPieceManager.SM_PROMOTION | specialFlag | promotionPiece << 3);
							}
						}
					}
				}
			}
		}
	}
	
	public static boolean isValid(ChessBoard board, ChessMove move) {
		return move != null && isValid(board, move.from, move.to, move.special);
	}
	
	public static boolean isValid(ChessBoard chessBoard, int fromIdx, int toIdx, int special) {
		final ChessBoardImpl board = (ChessBoardImpl)chessBoard;
		final boolean isWhite = board.isWhite();
		
		if (special == 0) {
			int oldFrom = board.pieces[fromIdx];
			int oldTo = board.pieces[toIdx];
			
			board.setPiece(fromIdx, Pieces.NONE);
			board.setPiece(toIdx, oldFrom);
			
			boolean isValid = !ChessPieceManager.isKingAttacked(board, isWhite);
			
			board.setPiece(fromIdx, oldFrom);
			board.setPiece(toIdx, oldTo);
			
			return isValid;
		} else {
			int type = special & 0b11000000;
			
			switch (type) {
				case ChessPieceManager.SM_CASTLING -> {
					if ((special & CastlingFlags.ANY_CASTLE_K) != 0) {
						return !(ChessPieceManager.isAttacked(board, fromIdx)
							|| ChessPieceManager.isAttacked(board, fromIdx + 1)
							|| ChessPieceManager.isAttacked(board, fromIdx + 2));
					}
					
					if ((special & CastlingFlags.ANY_CASTLE_Q) != 0) {
						return !(ChessPieceManager.isAttacked(board, fromIdx)
							|| ChessPieceManager.isAttacked(board, fromIdx - 1)
							|| ChessPieceManager.isAttacked(board, fromIdx - 2));
					}
				}
				
				case ChessPieceManager.SM_EN_PASSANT -> {
					int oldFrom = board.pieces[fromIdx];
					int remIdx = toIdx + (isWhite ? -8 : 8);
					int oldRem = board.pieces[remIdx];
					int oldTo = board.pieces[toIdx];
					
					board.setPiece(fromIdx, Pieces.NONE);
					board.setPiece(remIdx, Pieces.NONE);
					board.setPiece(toIdx, oldFrom);
					
					boolean isValid = !ChessPieceManager.isKingAttacked(board, isWhite);
					
					board.setPiece(fromIdx, oldFrom);
					board.setPiece(remIdx, oldRem);
					board.setPiece(toIdx, oldTo);
					
					return isValid;
				}
				
				case ChessPieceManager.SM_PROMOTION -> {
					int oldFrom = board.pieces[fromIdx];
					int oldTo = board.pieces[toIdx];
					
					board.setPiece(fromIdx, Pieces.NONE);
					board.setPiece(toIdx, oldFrom);
					
					boolean isValid = !ChessPieceManager.isKingAttacked(board, isWhite);
					
					board.setPiece(fromIdx, oldFrom);
					board.setPiece(toIdx, oldTo);
					
					return isValid;
				}
			}
			
			return false;
		}
	}
	
	public static boolean playMove(ChessBoard board, ChessMove move) {
		return move != null && playMove(board, move.from, move.to, move.special);
	}
	
	public static boolean playMove(ChessBoard board, int fromIdx, int toIdx, int special) {
		final ChessBoardImpl b = (ChessBoardImpl)board;
		final boolean isWhite = b.isWhite();
		final int mul = isWhite ? 1 : -1;
		
		// Increase moves since last capture
		int nextLastCapture = b.lastCapture + 1;
		int nextHalfMove = b.halfMove + 1;
		int nextLastPawn = 0;
		
		switch (special & 0b11000000) {
			case ChessPieceManager.SM_NORMAL -> {
				int oldFrom = b.pieces[fromIdx];
				int oldTo = b.pieces[toIdx];
				int pieceSq = oldFrom * oldFrom;
				
				switch (pieceSq) {
					case Pieces.ROOK_SQ -> {
						if (isWhite) {
							if (fromIdx == CastlingFlags.WHITE_ROOK_K) {
								b.flags &= ~CastlingFlags.WHITE_CASTLE_K;
							}
							
							if (fromIdx == CastlingFlags.WHITE_ROOK_Q) {
								b.flags &= ~CastlingFlags.WHITE_CASTLE_Q;
							}
						} else {
							if (fromIdx == CastlingFlags.BLACK_ROOK_K) {
								b.flags &= ~CastlingFlags.BLACK_CASTLE_K;
							}
							
							if (fromIdx == CastlingFlags.BLACK_ROOK_Q) {
								b.flags &= ~CastlingFlags.BLACK_CASTLE_Q;
							}
						}
					}
					case Pieces.KING_SQ -> {
						if (isWhite) {
							if (fromIdx == CastlingFlags.WHITE_KING) {
								b.flags &= ~CastlingFlags.WHITE_CASTLE_ANY;
							}
						} else {
							if (fromIdx == CastlingFlags.BLACK_KING) {
								b.flags &= ~CastlingFlags.BLACK_CASTLE_ANY;
							}
						}
					}
					case Pieces.PAWN_SQ -> {
						// Only double jumps are saved
						int distance = (fromIdx - toIdx) * (fromIdx - toIdx);
						
						// Because double pawns jump two rows they will always have a distance of 256
						if (distance == 256) {
							nextLastPawn = toIdx + (isWhite ? -8 : 8);
						}
						
						nextLastCapture = 0;
					}
				}
				
				if (oldTo != Pieces.NONE) {
					// Capture
					nextLastCapture = 0;
				}
				
				if (b.flags != 0) {
					// Recalculate the castling flags
					if (isWhite) {
						if (toIdx == CastlingFlags.BLACK_ROOK_K) {
							b.flags &= ~CastlingFlags.BLACK_CASTLE_K;
						}
						
						if (toIdx == CastlingFlags.BLACK_ROOK_Q) {
							b.flags &= ~CastlingFlags.BLACK_CASTLE_Q;
						}
					} else {
						if (toIdx == CastlingFlags.WHITE_ROOK_K) {
							b.flags &= ~CastlingFlags.WHITE_CASTLE_K;
						}
						
						if (toIdx == CastlingFlags.WHITE_ROOK_Q) {
							b.flags &= ~CastlingFlags.WHITE_CASTLE_Q;
						}
					}
				}
				
				b.setPiece(fromIdx, Pieces.NONE);
				b.setPiece(toIdx, oldFrom);
			}
			
			case ChessPieceManager.SM_CASTLING -> {
				if ((special & CastlingFlags.ANY_CASTLE_K) != 0) {
					b.setPiece(fromIdx + 3, Pieces.NONE);
					b.setPiece(fromIdx + 2, Pieces.KING * mul);
					b.setPiece(fromIdx + 1, Pieces.ROOK * mul);
					b.setPiece(fromIdx, Pieces.NONE);
					b.flags &= isWhite ? ~CastlingFlags.WHITE_CASTLE_ANY : ~CastlingFlags.BLACK_CASTLE_ANY;
				}
				
				if ((special & CastlingFlags.ANY_CASTLE_Q) != 0) {
					b.setPiece(fromIdx - 4, Pieces.NONE);
					b.setPiece(fromIdx - 2, Pieces.KING * mul);
					b.setPiece(fromIdx - 1, Pieces.ROOK * mul);
					b.setPiece(fromIdx, Pieces.NONE);
					b.flags &= isWhite ? ~CastlingFlags.WHITE_CASTLE_ANY : ~CastlingFlags.BLACK_CASTLE_ANY;
				}
			}
			
			case ChessPieceManager.SM_EN_PASSANT -> {
				int oldFrom = b.pieces[fromIdx];
				int remIdx = toIdx + (isWhite ? -8 : 8);
				
				nextLastCapture = 0;
				b.setPiece(fromIdx, Pieces.NONE);
				b.setPiece(remIdx, Pieces.NONE);
				b.setPiece(toIdx, oldFrom);
			}
			
			case ChessPieceManager.SM_PROMOTION -> {
				int piece = (special & 0b111000) >>> 3;
				
				switch (piece) {
					case Pieces.QUEEN, Pieces.BISHOP, Pieces.KNIGHT, Pieces.ROOK -> {
						int oldFrom = b.pieces[fromIdx];
						b.setPiece(fromIdx, Pieces.NONE);
						b.setPiece(toIdx, piece * mul);
						
						if (oldFrom != 0) {
							nextLastCapture = 0;
						}
					}
					default -> {
						return false;
					}
				}
			}
		}
		
		b.lastCapture = nextLastCapture;
		b.lastPawn = nextLastPawn;
		b.halfMove = nextHalfMove;
		return true;
	}
	
	public static void debug(String title, long board) {
		JOptionPane.showMessageDialog(null, ChessPieceManager.BOARD_PANEL.setTargets(board), title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public static void debug(String title, int[] board) {
		JOptionPane.showMessageDialog(null, ChessPieceManager.BOARD_PANEL.setTargets(board), title, JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	 * This class is not allowed to modify the state of the board
	 */
	@FunctionalInterface
	public interface ChessConsumer {
		/**
		 * Return true if you want to keep checking moves or false if you want to stop
		 */
		boolean accept(int fromIdx, int toIdx, int special);
	}
}
