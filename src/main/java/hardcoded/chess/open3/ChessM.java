package hardcoded.chess.open3;

import hardcoded.chess.open.Pieces;

// This should be around 50 times fater
public class ChessM {
	public static int material(ChessB board) {
		long mask = board.piece_mask;
		int material = 0;
		
		while(mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			material += Pieces.value(board.pieces[idx]);
		}
		
		return material;
	}
	
	public static void generate(ChessB board) {
		long mask;
		if(board.isWhite()) {
			mask = board.white_mask;
		} else {
			mask = board.black_mask;
		}
		
		while(mask != 0) {
			long pick = Long.lowestOneBit(mask);
			mask &= ~pick;
			int idx = Long.numberOfTrailingZeros(pick);
			
			int piece = board.pieces[idx];
			long moves = ChessPM.piece_move(board, piece, idx);
			
			System.out.printf("%8s: %s\n", Pieces.toString(piece), UtilsF.toBitString(moves));
			if(piece * piece == 1 || piece * piece == 36) {
				long special = ChessPM.special_piece_move(board, piece, idx);
				int move_to = (int)(special & 0b00111111);
				int type    = (int)(special & 0b11000000);
				if(type > 0) {
					System.out.printf("    ----: (%s) (%s) %s\n\n", UtilsF.toSpecialString(type), UtilsF.toSquare(move_to), UtilsF.toBitString(special));
				}
			}
		}
	}
}
