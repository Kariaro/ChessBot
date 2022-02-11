package me.hardcoded.chess.utils;

import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.open3.ChessB;
import me.hardcoded.chess.open3.ChessPM;
import me.hardcoded.chess.open3.UtilsF;

public interface BoardListener {
	/**
	 * Called when the board is initialized
	 * @param event a init event
	 */
	void onInit(BoardEvent.InitEvent event);
	
	/**
	 * Called when a user has dragged a piece over the board.
	 * @param event a drag event
	 */
	void onDrag(BoardEvent.DragEvent event);
	
	/**
	 * Called when the user has pressed inside the chess board.
	 * @param event a press event
	 */
	void onPress(BoardEvent.PressEvent event);
	
	static final BoardListener DUMMY = new BoardListener() {
		public void onInit(BoardEvent.InitEvent event) {
			System.out.println("Default: onInit();");
		}
		
		public void onDrag(BoardEvent.DragEvent event) {
			int from = event.from;
			int to = event.to;
			
			System.out.printf("Default: onDrag(%s, %s);\n", UtilsF.toSquare(from), UtilsF.toSquare(to));
		}
		
		public void onPress(BoardEvent.PressEvent event) {
			float x = event.x;
			float y = event.y;
			
			if(event.getPanel().isPromoting()) {
				if(event.getButton() == BoardEvent.BUTTON1) {
					int piece = event.getPromotingPiece();
					System.out.println("piece: " + Pieces.toString(piece));
				}
				
				event.getPanel().setPromoting(false);
			} else {
				// Add the valid moves for the pressed piece.
				if(event.getButton() == BoardEvent.BUTTON1) {
					int index = event.getSelectedIndex();
					
					ChessB board = event.getBoard();
					
				}
			}
			
			System.out.printf("Default: onPress(%.2f, %.2f);\n", x, y);
		}
	};
}