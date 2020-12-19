package hardcoded.chess.open;

public class Move {
	public static final Move INVALID = new Move(0, 0, 0);
	
	private final int pieceId;
	private final int from;
	private final int to;
	private final Action action;
	
	private Move(int pieceId, int from, int to) {
		this(pieceId, from, to, Action.NONE);
	}
	
	private Move(int pieceId, int from, int to, Action action) {
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
	
	public int hashCode() {
		return from | (to << 6) | ((pieceId & 15) << 12) | (action.ordinal() << 16);
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
	
	
	
	public static Move of(int pieceId, int from, int to) {
		return new Move(pieceId, from, to, Action.NONE);
	}
	
	public static Move of(int pieceId, int from, int to, Action action) {
		return new Move(pieceId, from, to, action);
	}
}
