package me.hardcoded.chess.decoder;

import me.hardcoded.chess.advanced.CastlingFlags;
import me.hardcoded.chess.advanced.ChessBoardImpl;
import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.utils.ChessUtils;
import me.hardcoded.lexer.GenericLexerContext.LexerToken;
import me.hardcoded.lexer.PGNLexer;
import me.hardcoded.lexer.notation.ChessAN;

import java.util.*;

/**
 * Internal class for handling PGN
 *
 * @author HardCoded
 */
public class ChessPGN {
	static String get(PGNGame game) {
		String fen = game.getTag(PGNTag.FEN);
		ChessBoardImpl board = new ChessBoardImpl(fen == null ? "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0" : fen);
		List<ChessMove> moves = game.moves;
		
		StringBuilder sb = new StringBuilder();
		{
			/* Append all the tags to the output pgn text.
			 * First the 'Seven tag roster' is written.
			 *   Event, Site, Date, Round, White, Black, Result
			 */
			for (PGNTag tag : PGNTag.REQUIRED) {
				if (tag.isRequired()) {
					// TODO: Correctly escape the text
					String value = game.getTag(tag);
					
					if (value != null) {
						sb.append("[").append(tag.name()).append(" \"").append(value).append("\"]\n");
					}
				}
			}
			
			// After the 'Seven tag roster' has been written we add all the custom tags.
			for (Map.Entry<String, String> entry : game.tags.entrySet()) {
				String name = entry.getKey();
				if (PGNTag.hasTag(name) && PGNTag.getTag(name).isRequired()) {
					// If this tag is required it has already been added to the ouput.
					continue;
				}
				
				// TODO: Correctly escape the text
				String value = entry.getValue();
				sb.append("[").append(name).append(" \"").append(value == null ? "" : value).append("\"]\n");
			}
		}
		sb.append("\n");
		
		// Go trough all the moves and correctly annotate them.
		for (int i = 0, len = moves.size(); i < len; i++) {
			ChessMove move = moves.get(i);
			
			int prevPiece = board.getPiece(move.to);
			String unique = CodecHelper.getUniquePiecePosition(board, move);
			if (!ChessGenerator.playMove(board, move)) {
				// Invalid match
				return null;
			}
			
			if ((i & 1) == 0) {
				sb.append(1 + (i / 2)).append(". ");
			}
			
			int pieceSq = move.piece * move.piece;
			int type = move.special & 0b11_000000;
			
			switch (type) {
				case ChessPieceManager.SM_CASTLING -> {
					sb.append(((move.special & CastlingFlags.ANY_CASTLE_K) != 0) ? "O-O" : "O-O-O");
				}
				case ChessPieceManager.SM_NORMAL -> {
					if (pieceSq != Pieces.PAWN_SQ) {
						char c = Pieces.printable(Math.abs(move.piece));
						sb.append(c);
					}
					
					if (prevPiece != Pieces.NONE) {
						if (pieceSq == Pieces.PAWN_SQ) {
							sb.append(ChessUtils.toFileChar(move.from & 7));
						} else {
							sb.append(unique);
						}
						sb.append('x');
					} else {
						sb.append(unique);
					}
					
					sb.append(ChessUtils.toSquare(move.to));
				}
				case ChessPieceManager.SM_EN_PASSANT -> {
					char from = ChessUtils.toColumn(move.from);
					sb.append(from).append('x').append(ChessUtils.toSquare(move.to));
				}
				case ChessPieceManager.SM_PROMOTION -> {
					char c = Pieces.printable(Math.abs(move.special & 0b111000) >> 3);
					sb.append(ChessUtils.toSquare(move.to)).append('=').append(c);
				}
			}
			
			if (CodecHelper.isGameCheckmate(board)) {
				sb.append('#');
			} else if (CodecHelper.isKingChecked(board)) {
				sb.append('+');
			}
			
			sb.append(' ');
		}
		
		return sb.toString();
	}
	
	static ChessMove findChessMove(ChessBoard board, String name) {
		final ChessAN notation = new ChessAN(name);
		final int targetIdx = (notation.getTargetRank() << 3) + (7 - notation.getTargetFile());
		
		final List<ChessMove> matches = new ArrayList<>();
		ChessGenerator.generate(board, false, (from, to, special) -> {
			if (!ChessGenerator.isValid(board, from, to, special)) {
				return true;
			}
			
			int piece = board.getPiece(from);
			if (notation.isCastling()) {
				if (Math.abs(piece) == Pieces.KING && (special & ChessPieceManager.SM_CASTLING) != 0) {
					int castlingFlag = notation.isKingsideCastling()
						? CastlingFlags.ANY_CASTLE_K
						: CastlingFlags.ANY_CASTLE_Q;
					
					if ((special & castlingFlag) != 0) {
						matches.add(new ChessMove(piece, from, to, special));
					}
				}
				
				return true;
			}
			
			if (to != targetIdx || Math.abs(piece) != notation.getPiece()) {
				return true;
			}
			
			if (notation.isPromotion()) {
				if ((special & ChessPieceManager.SM_PROMOTION) != 0) {
					if (((special >> 3) & 7) == notation.getPromotionPiece()) {
						matches.add(new ChessMove(piece, from, to, special));
					}
				}
				
				return true;
			}
			
			matches.add(new ChessMove(piece, from, to, special));
			return true;
		});
		
		if (matches.size() == 1) {
			return matches.get(0);
		} else {
			ChessMove selected = null;
			boolean warning = false;
			
			int notationFile = notation.getFromFile();
			int notationRank = notation.getFromRank();
			for (ChessMove move : matches) {
				int moveFromFile = 7 - (move.from & 7);
				int moveFromRank = move.from >> 3;
				
				if (notationFile != -1 && moveFromFile != notationFile) {
					continue;
				}
				
				if (notationRank != -1 && moveFromRank != notationRank) {
					continue;
				}
				
				if (selected != null) {
					// Not enough information. We could throw errors here
					warning = true;
				}
				
				selected = move;
			}
			
			if (warning) {
				for (ChessMove move : matches) {
					System.out.println("Potential: (" + move + ")   ==   (" + notation + ")");
				}
				
				System.out.println();
			}
			
			return selected;
		}
	}
	
	static PGNGame from(String text) {
		PGNGame game = new PGNGame();
		
		List<LexerToken<PGNLexer.Type>> tokens = PGNLexer.LEXER.parse(text, Set.of(
			PGNLexer.Type.WHITESPACE,
			PGNLexer.Type.COMMENT
		));
		
		try {
			/* The import format of PGN is less strict and certain things needs to be handled if we
			 * want to be able to import already annotated games.
			 */
			int i = 0;
			for (int len = tokens.size(); i < len; ) {
				LexerToken<PGNLexer.Type> token = tokens.get(i);
				
				if (token.type != PGNLexer.Type.TAG_ENTER) {
					break;
				}
				
				if (i + 4 >= len) {
					throw new RuntimeException("Not enough symbols for tag" + tokens.subList(i, i + 4));
				}
				
				if (tokens.get(i + 3).type != PGNLexer.Type.TAG_LEAVE) {
					throw new RuntimeException("Unclosed tag. Missing ']'");
				}
				
				LexerToken<PGNLexer.Type> name = tokens.get(i + 1);
				
				if (name.type != PGNLexer.Type.SYMBOL) {
					throw new RuntimeException("Invalid tag name '%s'".formatted(name.content));
				}
				
				LexerToken<PGNLexer.Type> value = tokens.get(i + 2);
				if (value.type != PGNLexer.Type.STRING) {
					throw new RuntimeException("Invalid tag value '%s'".formatted(value.content));
				}
				
				game.setTag(name.content, value.content.substring(1, value.content.length() - 1));
				i += 4;
			}
			
			ChessBoardImpl board;
			String fen = game.getTag(PGNTag.FEN);
			if (fen != null) {
				board = new ChessBoardImpl(fen);
			} else {
				board = new ChessBoardImpl();
			}
			
			boolean isTerminated = false;
			int var_depth = 0;
			int moveIndex = 0;
			for (int len = tokens.size(); i < len; i ++) {
				LexerToken<PGNLexer.Type> token = tokens.get(i);
				
				switch (token.type) {
					case WHITESPACE, COMMENT -> { continue; }
					case VAR_ENTER -> {
						var_depth++;
						continue;
					}
					case VAR_LEAVE -> {
						var_depth--;
						continue;
					}
				}
				
				if (var_depth != 0) {
					continue;
				}
				
				if (isTerminated) {
					throw new RuntimeException("Game was terminated");
				}
				
				switch (token.type) {
					case NAG -> {
						i ++;
						if (i < len) {
							if (tokens.get(i).type != PGNLexer.Type.INTEGER) {
								throw new RuntimeException("Expected integer after '$'");
							}
						}
						continue;
					}
					case INTEGER -> {
						if (!Integer.toString((moveIndex / 2) + 1).equals(token.content)) {
							System.out.println(moveIndex);
							throw new RuntimeException("Invalid move index '%s' should have been '%d'".formatted(token.content, (moveIndex / 2) + 1));
						}
						
						// Consume all dots
						while (i + 1 < len && tokens.get(i + 1).type == PGNLexer.Type.DOT) {
							i++;
						}
						continue;
					}
					case TERMINATION -> isTerminated = true;
					case SYMBOL -> {
						moveIndex ++;
						ChessMove move = findChessMove(board, token.content);
						if (move == null || !ChessGenerator.playMove(board, move.from, move.to, move.special)) {
							throw new RuntimeException("Invalid move '%s'".formatted(token.content));
						} else {
							game.addMove(move);
						}
					}
					default ->
						throw new RuntimeException("Invalid token '%s'".formatted(token));
				}
			}
			
			if (var_depth != 0) {
				throw new RuntimeException("Unclosed '('");
			}
		} catch (RuntimeException e) {
			throw e;
		}
		
		return game;
	}
}
