package me.hardcoded.chess.open;

public class Move {
	public static final Move INVALID = new Move(0, 0, 0);
	
	private final int pieceId;
	private final int from;
	private final int to;
	private final Action action;
	private boolean attack;
	
	private Move(int pieceId, int from, int to) {
		this(pieceId, from, to, Action.NONE);
	}
	
	private Move(int pieceId, int from, int to, Action action) {
		this.action = action;
		this.pieceId = pieceId;
		this.from = from;
		this.to = to;
	}
	
	private Move(int pieceId, int from, int to, Action action, boolean attack) {
		this.action = action;
		this.pieceId = pieceId;
		this.attack = attack;
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
	
	public boolean attack() {
		return attack;
	}
	
	@Override
	public String toString() {
		Piece piece = ChessUtils.toPiece(pieceId);
		
		String square = ChessUtils.toSquare(from) + ChessUtils.toSquare(to);
		if(action == Action.PROMOTE) {
			return square + piece.letter();
		}
		
		return square;
	}
	
	public static Move of(int pieceId, int from, int to) {
		return new Move(pieceId, from, to, Action.NONE);
	}
	
	public static Move of(int pieceId, int from, int to, Action action) {
		return new Move(pieceId, from, to, action);
	}
	
	public static Move of(int pieceId, int from, int to, boolean attack) {
		return new Move(pieceId, from, to, Action.NONE, attack);
	}
	
	public static Move of(int pieceId, int from, int to, Action action, boolean attack) {
		return new Move(pieceId, from, to, action, attack);
	}
}
