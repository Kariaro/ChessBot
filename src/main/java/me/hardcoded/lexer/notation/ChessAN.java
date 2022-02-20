package me.hardcoded.lexer.notation;

import me.hardcoded.chess.open.Pieces;

public class ChessAN {
	public static final int CAPTURE = 1,
							CHECKMATE = 2,
							CHECK = 4,
							PROMOTION = 8,
							KINGSIDE_CASTLING = 16,
							QUEENSIDE_CASTLING = 32;
	
	private int targetFile = -1;
	private int targetRank = -1;
	private int fromFile = -1;
	private int fromRank = -1;
	private int piece = Pieces.NONE;
	private int promotionPiece = Pieces.NONE;
	private int flags;
	
	public ChessAN(String notation) {
		// (Piece?)(FromFile?)(FromRank?)(ToFile)(ToRank)(=Piece)?(#|+)?(Suffix)?
		if (!processNotation(notation)) {
			throw new InvalidNotation("Invalid notation '%s'", notation);
		}
	}
	
	public int getFromFile() {
		return fromFile;
	}
	
	public int getFromRank() {
		return fromRank;
	}
	
	public int getTargetFile() {
		return targetFile;
	}
	
	public int getTargetRank() {
		return targetRank;
	}
	
	public int getPiece() {
		return piece;
	}
	
	public int getPromotionPiece() {
		return promotionPiece;
	}
	
	public boolean isCheckmate() {
		return (flags & CHECKMATE) != 0;
	}
	
	public boolean isCheck() {
		return (flags & CHECK) != 0;
	}
	
	public boolean isCapture() {
		return (flags & CAPTURE) != 0;
	}
	
	public boolean isPromotion() {
		return (flags & PROMOTION) != 0;
	}
	
	public boolean isCastling() {
		return (flags & (KINGSIDE_CASTLING | QUEENSIDE_CASTLING)) != 0;
	}
	
	public boolean isKingsideCastling() {
		return (flags & KINGSIDE_CASTLING) != 0;
	}
	
	public boolean isQueensideCastling() {
		return (flags & QUEENSIDE_CASTLING) != 0;
	}
	
	private boolean processNotation(String notation) {
		// First we try to remove the suffix
		notation = removeSuffix(notation);
		
		// Then we try to remove checks and checkmates
		notation = removeAction(notation);
		
		// If the move was a promotion then we have fully processed the annotation
		return checkPromotion(notation) || checkNormal(notation);
	}
	
	/**
	 * Terminating method.
	 */
	private boolean checkNormal(String str) {
		if (str.equals("O-O")) {
			piece = Pieces.KING;
			flags |= KINGSIDE_CASTLING;
			return true;
		}
		
		if (str.equals("O-O-O")) {
			piece = Pieces.KING;
			flags |= QUEENSIDE_CASTLING;
			return true;
		}
		
		str = removeTargetPosition(str);
		str = removeFromPosition(str);
		
		if (str.isEmpty()) {
			piece = Pieces.PAWN;
		} else if (str.length() == 1) {
			piece = Pieces.fromPrintable(str.charAt(0));
			if (piece == Pieces.NONE) {
				throw new InvalidNotation("Invalid piece '%s'", str.charAt(0));
			}
		} else {
			throw new InvalidNotation("Invalid move annotation '%s'", str);
		}
		
		return true;
	}
	
	/**
	 * Terminating method.
	 */
	private boolean checkPromotion(String str) {
		final int length = str.length();
		if (length < 3 || str.charAt(length - 2) != '=') {
			return false;
		}
		
		flags |= PROMOTION;
		promotionPiece = Pieces.fromPrintable(str.charAt(length - 1));
		if (promotionPiece == Pieces.NONE) {
			throw new InvalidNotation("Expected a promotion piece but got '%s'", str.charAt(length - 1));
		}
		
		str = removeTargetPosition(str.substring(0, length - 2));
		str = removeFromPosition(str);
		
		piece = Pieces.PAWN;
		return str.isEmpty();
	}
	
	private String removeTargetPosition(String str) {
		final int length = str.length();
		targetRank = str.charAt(length - 1) - '1';
		targetFile = str.charAt(length - 2) - 'a';
		if (targetRank < 0 || targetRank > 7 || targetFile < 0 || targetFile > 7) {
			throw new InvalidNotation("Invalid square '%s'", str.substring(length - 4, length - 2));
		}
		
		return str.substring(0, length - 2);
	}
	
	private String removeFromPosition(String str) {
		if (str.endsWith("x")) {
			flags |= CAPTURE;
			str = str.substring(0, str.length() - 1);
		}
		
		if (str.isEmpty()) {
			return "";
		}
		
		char c = str.charAt(str.length() - 1);
		
		if (Character.isDigit(c)) {
			fromRank = c - '1';
			if (fromRank < 0 || fromRank > 7) {
				throw new InvalidNotation("Invalid rank '%s'", c);
			}
			
			str = str.substring(0, str.length() - 1);
		}
		
		if (!str.isEmpty()) {
			c = str.charAt(str.length() - 1);
			
			if (Character.isLetter(c) && Character.isLowerCase(c)) {
				fromFile = c - 'a';
				if (fromFile < 0 || fromFile > 7) {
					throw new InvalidNotation("Invalid file '%s'", c);
				}
				
				str = str.substring(0, str.length() - 1);
			}
		}
		
		return str;
	}
	
	private String removeAction(String str) {
		final int length = str.length();
		if (str.endsWith("#")) {
			flags |= CHECKMATE;
			return str.substring(0, length - 1);
		}
		if (str.endsWith("+")) {
			flags |= CHECK;
			return str.substring(0, length - 1);
		}
		
		return str;
	}
	
	private String removeSuffix(String str) {
		final int length = str.length();
		
		if (str.endsWith("?!")) {
			// suffix = NumericAnnotationGlyphs.NAG_6;
			return str.substring(0, length - 2);
		}
		if (str.endsWith("!?")) {
			// suffix = NumericAnnotationGlyphs.NAG_5;
			return str.substring(0, length - 2);
		}
		if (str.endsWith("??")) {
			// suffix = NumericAnnotationGlyphs.NAG_4;
			return str.substring(0, length - 2);
		}
		if (str.endsWith("!!")) {
			// suffix = NumericAnnotationGlyphs.NAG_3;
			return str.substring(0, length - 2);
		}
		if (str.endsWith("?")) {
			// suffix = NumericAnnotationGlyphs.NAG_2;
			return str.substring(0, length - 1);
		}
		if (str.endsWith("!")) {
			// suffix = NumericAnnotationGlyphs.NAG_1;
			return str.substring(0, length - 1);
		}
		
		return str;
	}
	
	
	@Override
	public String toString() {
		StringBuilder suffixString = new StringBuilder();
		if (isCheckmate()) {
			suffixString.append("#");
		} else if (isCheck()) {
			suffixString.append("+");
		}
		
		if (isQueensideCastling()) return "O-O-O" + suffixString;
		if (isKingsideCastling()) return "O-O" + suffixString;
		
		StringBuilder prefixString = new StringBuilder();
		if (piece != Pieces.PAWN) {
			char pieceChar = Pieces.printable(piece);
			if (pieceChar != '\0') {
				prefixString.append(pieceChar);
			}
		}
		
		if (fromFile != -1) prefixString.append((char)(fromFile + 'a'));
		if (fromRank != -1) prefixString.append((char)(fromRank + '1'));
		if (isCapture()) prefixString.append('x');
		prefixString.append((char)(targetFile + 'a'));
		prefixString.append((char)(targetRank + '1'));
		
		if (isPromotion()) {
			prefixString.append('=');
			char promotionChar = Pieces.printable(promotionPiece);
			if (promotionChar != '\0') {
				prefixString.append(promotionChar);
			}
		}
		
		return prefixString.toString() + suffixString;
	}
}
