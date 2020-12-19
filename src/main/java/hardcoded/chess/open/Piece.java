package hardcoded.chess.open;

public enum Piece {
	KING(0, 'k'),
	QUEEN(9, 'q'),
	BISHOP(3, 'b'),
	KNIGHT(3, 'n'),
	ROOK(5, 'r'),
	PAWN(1),
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
