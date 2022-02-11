package me.hardcoded.chess.advanced;

import me.hardcoded.chess.open.Pieces;

import javax.swing.*;

/**
 * This class will generate all moves possible
 *
 * @author HardCoded
 */
public class ChessGenerator {
	public static int material(ChessBoard board) {
		long mask = board.pieceMask;
		int material = 0;
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			material += Pieces.value(board.pieces[idx]);
		}
		
		return material;
	}
	
	public static void generate(ChessBoard board) {
		long mask;
		if (board.isWhite()) {
			mask = board.whiteMask;
		} else {
			mask = board.blackMask;
		}
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			long moves = ChessPieceManager.piece_move(board, piece, idx);
			
			System.out.printf("%9s: %s\n", Pieces.toString(piece), ChessUtils.toBitFancyString(moves));
//			ChessGenerator.debug("Generator %s".formatted(Pieces.toString(piece)), moves);
			if (piece * piece == 1 || piece * piece == 36) {
				long special = ChessPieceManager.special_piece_move(board, piece, board.isWhite(), idx);
				int fields = (int)(special & 0b00111111);
				int type   = (int)(special & 0b11000000);
				switch (type >> 6) {
					case 1, 2 -> {
						System.out.printf("    ----: (%s) (%s) %s\n\n", ChessUtils.toSpecialString(type), ChessUtils.toSquare(fields), ChessUtils.toBitString(special));
					}
					case 3 -> {
						System.out.printf("    ----: (%s) (%s)\n\n", ChessUtils.toSpecialString(type), ChessUtils.toCastlingMove(fields));
					}
				}
			}
		}
	}
	
	public static void generate(ChessBoard board, ChessConsumer consumer) {
		long mask;
		if (board.isWhite()) {
			mask = board.whiteMask;
		} else {
			mask = board.blackMask;
		}
		
		while (mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			long moves = ChessPieceManager.piece_move(board, piece, idx);
			
			while (moves != 0) {
				long move_bit = Long.lowestOneBit(moves);
				moves &= ~move_bit;
				int move_idx = Long.numberOfTrailingZeros(move_bit);
				consumer.accept(idx, move_idx, 0);
			}
			
			if (piece * piece == 1 || piece * piece == 36) {
				int special = (int) ChessPieceManager.special_piece_move(board, piece, board.isWhite(), idx);
				int type = special & 0b11000000;
				if (type == ChessPieceManager.sm_castling) {
					// Split the castling moves up into multiple moves
					int specialFlag;
					if ((specialFlag = (special & CastlingFlags.ANY_CASTLE_K)) != 0) {
						consumer.accept(idx, 0, ChessPieceManager.sm_castling | specialFlag);
					}
					if ((specialFlag = (special & CastlingFlags.ANY_CASTLE_Q)) != 0) {
						consumer.accept(idx, 0, ChessPieceManager.sm_castling | specialFlag);
					}
				} else if (type != 0) {
					consumer.accept(idx, special & 0b111111, special);
				}
			}
		}
	}
	
	public static boolean isValid(ChessBoard board, int fromIdx, int toIdx, int special) {
		if (special == 0) {
			int oldFrom = board.pieces[fromIdx];
			int oldTo = board.pieces[toIdx];
			
			board.setPiece(fromIdx, Pieces.NONE);
			board.setPiece(toIdx, oldFrom);
			
			boolean isValid = ChessPieceManager.isKingAttacked(board, board.isWhite());
			
			board.setPiece(fromIdx, oldFrom);
			board.setPiece(toIdx, oldTo);
			
			return isValid;
		} else {
			return false;
		}
	}
	
	public static void debug(String title, long board) {
		JOptionPane.showConfirmDialog(null, ChessPieceManager.BOARD_PANEL.setTargets(board), title, JOptionPane.OK_CANCEL_OPTION);
	}
	
	public static void debug(String title, int[] board) {
		JOptionPane.showConfirmDialog(null, ChessPieceManager.BOARD_PANEL.setTargets(board), title, JOptionPane.OK_CANCEL_OPTION);
	}
	
	/**
	 * This class is not allowed to modify the state of the board
	 */
	@FunctionalInterface
	public interface ChessConsumer {
		void accept(int fromIdx, int tooIdx, int special);
	}
}
