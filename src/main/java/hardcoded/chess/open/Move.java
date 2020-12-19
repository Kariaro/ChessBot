package hardcoded.chess.open;

public class Move {
	private final int pieceId;
	private final int from;
	private final int to;
	private final Action action;
	
	public Move(int pieceId, int from, int to) {
		this(pieceId, from, to, Action.NONE);
	}
	
	public Move(int pieceId, int from, int to, Action action) {
		this.action = action;
		this.pieceId = pieceId;
		this.from = from;
		this.to = to;
	}
	
	public int id() {
		return  pieceId;
	}
	
	public int from() {
		return from;
	}
	
	public int to() {
		return to;
	}
	
	public Action action() {
		return action;
	}
	
	@Override
	public String toString() {
		if(action == Action.KINGSIDE_CASTLE) return "O-O";
		if(action == Action.QUEENSIDE_CASTLE) return "O-O-O";
		Piece piece = ChessUtils.toPiece(pieceId);
		char letter = Character.toUpperCase(piece.letter());
		
		if(action == Action.PROMOTE) {
			return ChessUtils.toSquare(to) + "=" + letter;
		}
		
		// Sometimes we need to print this because otherwise the move would become ambiguous
		String square0 = ChessUtils.toSquare(from);
		String square1 = ChessUtils.toSquare(to);
		
		if(letter == 0) return square0 + " -> " + square1;
		return letter + square0 + " -> " + square1;
	}
}
