package me.hardcoded.chess.visual;

import me.hardcoded.chess.open.Pieces;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a visualization of a chess board.
 *
 * @author HardCoded
 */
class ChessBoardPanel extends JPanel {
	private static final Color CHECKER_B = new Color(181, 136, 99);
	private static final Color CHECKER_D = new Color(181, 136, 99);
	private static final Color CHECKER_L = new Color(240, 217, 181);
	private static final Color GREEN_DOT = new Color(170, 162, 58);
	private static final BufferedImage[] PIECE_IMAGES = new BufferedImage[12];
	private final BufferedImage[] SIZED_IMAGES = new BufferedImage[12];
	private final PieceType[] board;
	private int checkerSize = 96;
	private int borderSize = 16;
	
	static {
		try {
			InputStream stream = ChessBoardPanel.class.getResourceAsStream("/chess_pieces.png");
			if (stream != null) {
				BufferedImage img = ImageIO.read(stream);
				
				double width = img.getWidth() / 6.0;
				double height = img.getHeight() / 2.0;
				int wi = (int)Math.floor(width);
				int hi = (int)Math.floor(height);
				
				for (int i = 0; i < 12; i++) {
					int x = (int) (width * (i % 6));
					int y = (int) (height * (i / 6));
					PIECE_IMAGES[i] = img.getSubimage(x, y, wi, hi);
				}
			}
		} catch (IOException e) {
			// TODO: Fallback create the images but with text
			e.printStackTrace();
		}
	}
	
	public ChessBoardPanel() {
		this.setDoubleBuffered(true);
		this.setBackground(Color.DARK_GRAY);
		this.board = new PieceType[64];
		
		for (int i = 0; i < 64; i++) {
			this.board[i] = PieceType.NONE;
		}
		
		this.recomputeImages();
	}
	
	private void recomputeImages() {
		for (int i = 0; i < 12; i++) {
			BufferedImage image = PIECE_IMAGES[i];
			if (image == null) {
				continue;
			}
			
			BufferedImage scaledImage = new BufferedImage(checkerSize, checkerSize, BufferedImage.TYPE_INT_ARGB);
			final AffineTransform at = AffineTransform.getScaleInstance(checkerSize / (double)image.getWidth(), checkerSize / (double)image.getHeight());
			final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			SIZED_IMAGES[i] = ato.filter(image, scaledImage);
		}
	}
	
	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(CHECKER_D);
		g.fillRect(borderSize, borderSize, 8 * checkerSize, 8 * checkerSize);
		
		g.setColor(CHECKER_L);
		for (int i = 0; i < 64; i += 2) {
			int y = borderSize + (i >> 3) * checkerSize;
			int x = borderSize + ((i & 7) + ((i >> 3) & 1)) * checkerSize;
			g.fillRect(x, y, checkerSize, checkerSize);
		}
		
		// Draw pieces
		for (int i = 0; i < 64; i++) {
			int y = borderSize + (i >> 3) * checkerSize;
			int x = borderSize + (i & 7) * checkerSize;
			PieceType type = board[i];
			
			switch (type) {
				case GREEN_DOT -> {
					int size = checkerSize / 2;
					g.setColor(GREEN_DOT);
					g.fillOval(x + size / 2, y + size / 2, size, size);
				}
				case W_KING, W_QUEEN,W_BISHOP, W_KNIGHT, W_ROOK, W_PAWN, B_KING, B_QUEEN, B_BISHOP, B_KNIGHT, B_ROOK, B_PAWN -> {
					int idx = type.ordinal() - PieceType.W_KING.ordinal();
					
					Image image;
					if (idx >= 0 && idx < SIZED_IMAGES.length && (image = SIZED_IMAGES[idx]) != null) {
						g.drawImage(image, x, y, checkerSize, checkerSize, null);
					} else {
						// Error
						System.out.println("Bad configuration");
					}
				}
				default -> {
				
				}
			}
		}
	}
	
	public void setCheckerSize(int size) {
		checkerSize = size;
	}
	
	public ChessBoardPanel setTargets(long value) {
		for (int i = 0; i < 64; i++) {
			boolean check = ((value >>> (long)i) & 1L) != 0;
			board[i] = check ? PieceType.GREEN_DOT : PieceType.NONE;
		}
		
		repaint();
		return this;
	}
	
	public ChessBoardPanel setTargets(int[] pieces) {
		for (int i = 0; i < 64; i++) {
			board[i] = switch (pieces[i]) {
				case Pieces.KING -> PieceType.W_KING;
				case Pieces.QUEEN -> PieceType.W_QUEEN;
				case Pieces.BISHOP -> PieceType.W_BISHOP;
				case Pieces.KNIGHT -> PieceType.W_KNIGHT;
				case Pieces.ROOK -> PieceType.W_ROOK;
				case Pieces.PAWN -> PieceType.W_PAWN;
				case -Pieces.KING -> PieceType.B_KING;
				case -Pieces.QUEEN -> PieceType.B_QUEEN;
				case -Pieces.BISHOP -> PieceType.B_BISHOP;
				case -Pieces.KNIGHT -> PieceType.B_KNIGHT;
				case -Pieces.ROOK -> PieceType.B_ROOK;
				case -Pieces.PAWN -> PieceType.B_PAWN;
				default -> PieceType.NONE;
			};
		}
		
		repaint();
		return this;
	}
	
	@Override
	public Dimension getPreferredSize() {
		int side = borderSize * 2 + checkerSize * 8;
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
		
		// White
		W_KING,
		W_QUEEN,
		W_BISHOP,
		W_KNIGHT,
		W_ROOK,
		W_PAWN,
		
		// Black
		B_KING,
		B_QUEEN,
		B_BISHOP,
		B_KNIGHT,
		B_ROOK,
		B_PAWN,
	}
}
