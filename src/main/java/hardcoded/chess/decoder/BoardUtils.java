package hardcoded.chess.decoder;

import hardcoded.chess.open.State;
import static hardcoded.chess.open.Pieces.*;
import static hardcoded.chess.open.Flags.*;

import hardcoded.chess.open.ChessUtils;
import hardcoded.chess.open.Flags;

public class BoardUtils {
	
	public static void main(String[] args) {
		State state = FEN.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
	}
	
	public static class FEN {
		private FEN() {}
		
		public static State decode(String string) {
			
			// rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
			// First split by the spaces
			String[] parts = string.split(" ");
			if(parts.length != 6) {
				throw new IllegalArgumentException("This is not a valid FEN '" + string + "'");
			}

			
			String[] str_ranks = parts[0].split("/");
			if(str_ranks.length != 8) {
				throw new IllegalArgumentException("Invalid FEN: Expected 8 ranks but got (" + str_ranks.length + ")");
			}
			
			int[] board = new int[64];
			for(int i = 0; i < 8; i++) {
				String rank = str_ranks[i];
				
				int offset = i * 8;
				
				for(int x = 0; x < rank.length(); x++) {
					char c = rank.charAt(x);
					
					char type = Character.toLowerCase(c);
					boolean u = Character.isUpperCase(c);
					
					int idx = offset + x;
					int dir = u ? 1:-1;
					switch(type) {
						case 'r': board[idx] = ROOK * dir; break;
						case 'n': board[idx] = KNIGHT * dir; break;
						case 'b': board[idx] = BISHOP * dir; break;
						case 'q': board[idx] = QUEEN * dir; break;
						case 'k': board[idx] = KING * dir; break;
						case 'p': board[idx] = PAWN * dir; break;
						
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
							x += type - '0';
							break;
						
						default: {
							throw new IllegalArgumentException("Invalid FEN: Undefined piece type '" + type + "'");
						}
					}
				}
			}
			
			int flags = 0;
			System.out.println("---");
			ChessUtils.printBoard(board);
			System.out.println("---");
			
			// Turn
			switch(parts[1]) {
				case "w": flags |= Flags.TURN;
				case "b": break;
				default: {
					throw new IllegalArgumentException("Invalid FEN: Undefined turn '" + parts[1] + "' only [wb] allowed");
				}
			}
			
			// Castling availability
			{
				String part = parts[2];
				if(part.equals("-")) {
					// Castling is not possible
				} else {
					for(int i = 0; i < part.length(); i++) {
						char c = part.charAt(i);
						
						switch(c) {
							case 'K': flags |= Flags.CASTLE_WK; break;
							case 'Q': flags |= Flags.CASTLE_WQ; break;
							case 'k': flags |= Flags.CASTLE_BK; break;
							case 'q': flags |= Flags.CASTLE_BQ; break;
						}
					}
				}
			}
			
			String str_surn = parts[1];
			String str_castl = parts[2];
			String str_lmove = parts[3];
			
			
			return State.of(board, flags, null);
		}
		
		public static String encode(State state) {
			return null;
		}
	}
}
