package me.hardcoded.chess.open;

public enum Piece {
	KING(0, 'k'),
	QUEEN(900, 'q'),
	BISHOP(300, 'b'),
	KNIGHT(300, 'n'),
	ROOK(500, 'r'),
	PAWN(100),
	NONE(0),
	;
	
	private final int value;
	private final char letter;
	
	private Piece(int value) {
		this(value, '\0');
	}
	
	private Piece(int value, char letter) {
		this.value = value;
		this.letter = letter;
	}
	
	public int value() {
		return value;
	}
	
	public char letter() {
		return letter;
	}
}
