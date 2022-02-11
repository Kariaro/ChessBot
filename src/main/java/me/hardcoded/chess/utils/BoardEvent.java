package me.hardcoded.chess.utils;

import java.awt.event.MouseEvent;

import me.hardcoded.chess.open3.ChessB;

/**
 * @author HardCoded
 */
public interface BoardEvent {
	int BUTTON1 = MouseEvent.BUTTON1;
	int BUTTON2 = MouseEvent.BUTTON2;
	int BUTTON3 = MouseEvent.BUTTON3;
	
	class InitEvent implements BoardEvent {
		private final ChessB board;
		private final BoardPanel panel;
		
		protected InitEvent(ChessB board, BoardPanel panel) {
			this.board = board;
			this.panel = panel;
		}
		
		public ChessB getBoard() {
			return board;
		}
		
		public BoardPanel getPanel() {
			return panel;
		}
	}
	
	class PressEvent implements BoardEvent {
		private final ChessB board;
		private final BoardPanel panel;
		private final int button;
		
		/** A value between 0 and 8 (exclusive) */
		public final float x;
		
		/** A value between 0 and 8 (exclusive) */
		public final float y;
		
		protected PressEvent(ChessB board, BoardPanel panel, float x, float y, int button) {
			this.board = board;
			this.panel = panel;
			this.x = x;
			this.y = y;
			this.button = button;
		}
		
		public ChessB getBoard() {
			return board;
		}
		
		public BoardPanel getPanel() {
			return panel;
		}
		
		public int getButton() {
			return button;
		}
		
		/**
		 * If the board is promoting a pawn this will return the value
		 * of the piece being promoted to.
		 * @return the promoted piece id
		 */
		public int getPromotingPiece() {
			if(x >= 2.0 && x < 6.0 && y >= 3.5 && y < 4.5) {
				int idx = MathUtils.clamp((int)(x - 2), 0, 3);
				return idx + 2;
			}
			
			return 0;
		}
		
		/**
		 * @return the square that was pressed on the board
		 */
		public int getSelectedIndex() {
			int ix = (int)x;
			int iy = (int)y;
			return ix + (iy << 8);
		}
	}
	
	class DragEvent implements BoardEvent {
		private final ChessB board;
		private final BoardPanel panel;
		
		public final int from;
		public final int to;
		
		protected DragEvent(ChessB board, BoardPanel panel, int from, int to) {
			this.board = board;
			this.panel = panel;
			this.from = from;
			this.to = to;
		}
		
		public ChessB getBoard() {
			return board;
		}
		
		public BoardPanel getPanel() {
			return panel;
		}
	}
	
	/**
	 * @return the chess board
	 */
	ChessB getBoard();
	
	/**
	 * @return the component that fired this event
	 */
	BoardPanel getPanel();
}
