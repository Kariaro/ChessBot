package me.hardcoded.chess.visual;

import javax.swing.*;
import java.awt.*;
import java.lang.annotation.AnnotationTypeMismatchException;

/**
 * This is a visualization of a chess board.
 *
 * @author HardCoded
 */
public class ChessBoardPanel extends JPanel {
	private static final Color CHECKER_B = new Color(181, 136, 99);
	private static final Color CHECKER_D = new Color(181, 136, 99);
	private static final Color CHECKER_L = new Color(240, 217, 181);
	private static final Color GREEN_DOT = new Color(170, 162, 58);
	private final PieceType[] board;
	private int checker_size = 96;
	private int checker_border = 16;
	
	public ChessBoardPanel() {
		this.setDoubleBuffered(true);
		this.setBackground(Color.DARK_GRAY);
		this.board = new PieceType[64];
		
		for (int i = 0; i < 64; i++) {
			this.board[i] = PieceType.NONE;
		}
	}
	
	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(CHECKER_D);
		g.fillRect(checker_border, checker_border, 8 * checker_size, 8 * checker_size);
		
		g.setColor(CHECKER_L);
		for (int i = 0; i < 64; i += 2) {
			int y = checker_border + (i >> 3) * checker_size;
			int x = checker_border + ((i & 7) + ((i >> 3) & 1)) * checker_size;
			g.fillRect(x, y, checker_size, checker_size);
		}
		
		// Draw pieces
		for (int i = 0; i < 64; i++) {
			int y = checker_border + (i >> 3) * checker_size;
			int x = checker_border + (i & 7) * checker_size;
			PieceType type = board[i];
			
			switch (type) {
				case GREEN_DOT -> {
					int size = checker_size / 2;
					g.setColor(GREEN_DOT);
					g.fillOval(x + size / 2, y + size / 2, size, size);
				}
				default -> {
				
				}
			}
		}
	}
	
	public ChessBoardPanel setTargets(long value) {
		for (int i = 0; i < 64; i++) {
			boolean check = ((value >>> (long)i) & 1L) != 0;
			board[i] = check ? PieceType.GREEN_DOT : PieceType.NONE;
		}
		
		repaint();
		return this;
	}
	
	@Override
	public Dimension getPreferredSize() {
		int side = checker_border * 2 + checker_size * 8;
		return new Dimension(side, side);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	public enum PieceType {
		NONE,
		GREEN_DOT,
	}
}
