package me.hardcoded.chess.visual;

import me.hardcoded.chess.open.Pieces;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
public class PlayableChessBoard extends JPanel implements ActionListener {
	private static final Color CHECKER_B = new Color(181, 136, 99);
	private static final Color CHECKER_D = new Color(181, 136, 99);
	private static final Color CHECKER_L = new Color(240, 217, 181);
	private static final Color GREEN_DOT = new Color(170, 162, 58);
	private static final BufferedImage[] PIECE_IMAGES = new BufferedImage[12];
	private final BufferedImage[] SIZED_IMAGES = new BufferedImage[12];
	private final PieceType[] board;
	
	/**
	 * This field contains the highlighting value of each square of the board.
	 *
	 * This value will for each frame that the mouse is hovering over this square
	 * increase by {@code dt} where {@code dt} is the delta amount of milliseconds
	 * since last frame.
	 *
	 * This counter will stop when it reaches {@code 100} or {@code 0}.
	 */
	private final float[] hoverArray;
	
	/**
	 * This field contains a bit field of what squares are hovered. Because chess has exactly
	 * {@code 64} squares we can represent this as a {@code long}.
	 */
	private long hoverMask;
	
	/**
	 * This field contains the millisecond value of when the last frame of this board was rendered.
	 */
	private long lastFrame;
	
	/**
	 * This field contains the render timer that will make sure that the display is rendered.
	 */
	private final Timer renderTimer;
	
	/**
	 * This field changes if the board should be viewed from white pieces perspective
	 */
	private boolean whitePov;
	
	/**
	 * This field changes the size of each square on the board.
	 */
	private int checkerSize;
	
	/**
	 * This field changes the size of the border around the board.
	 */
	private int borderSize;
	
	static {
		try {
			InputStream stream = ChessBoardPanel.class.getResourceAsStream("/chess_pieces.png");
			if (stream != null) {
				BufferedImage img = ImageIO.read(stream);
				
				double width = img.getWidth() / 6.0;
				double height = img.getHeight() / 2.0;
				int wi = (int) Math.floor(width);
				int hi = (int) Math.floor(height);
				
				for (int i = 0; i < 12; i++) {
					int x = (int) (width * (i % 6));
					int y = (int) (height * (i / 6));
					PIECE_IMAGES[i] = img.getSubimage(x, y, wi, hi);
				}
			}
		} catch (IOException e) {
			// TODO: Fallback create the images with text
			e.printStackTrace();
		}
	}
	
	public PlayableChessBoard() {
		this.setDoubleBuffered(true);
		this.setBackground(Color.DARK_GRAY);
		
		// Internal values
		this.board = new PieceType[64];
		this.hoverArray = new float[64];
		this.renderTimer = new Timer(25, this); // ~50 fps
		this.renderTimer.start();
		
		// Default settings
		this.whitePov = true;
		this.checkerSize = 90;
		this.borderSize = 16;
		
		for (int i = 0; i < 64; i++) {
			this.board[i] = PieceType.NONE;
		}
		
		this.recomputeImages();
		
		MouseAdapter boardAdapter = new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
			
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				whitePov = !whitePov;
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				hoverMask = 0;
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int square = getSquare(e.getPoint());
				
				if (square < 0) {
					hoverMask = 0;
				} else {
					hoverMask = 1L << square;
				}
			}
			
			public int getSquare(Point p) {
				int mx = (p.x - borderSize) / checkerSize;
				int my = (p.y - borderSize) / checkerSize;
				
				if (mx >= 0 && mx < 8 && my >= 0 && my < 8) {
					return mx + (my << 3);
				}
				
				return -1;
			}
		};
		this.addMouseListener(boardAdapter);
		this.addMouseMotionListener(boardAdapter);
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
	public void actionPerformed(ActionEvent e) {
		if (isDisplayable()) {
			repaint();
		}
	}
	
	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		
		// Calculate the delta time
		long now = System.currentTimeMillis();
		float deltaTime = now - (lastFrame == 0 ? now : lastFrame);
		lastFrame = now;
		
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw the background of the chess board
		paintBackground(g, deltaTime);
		
		// Draw pieces
		for (int i = 0; i < 64; i++) {
			int xp = whitePov ? (7 - (i >> 3)) : (i >> 3);
			int yp = whitePov ? (7 - (i & 7)) : (i & 7);
			int y = borderSize + xp * checkerSize;
			int x = borderSize + yp * checkerSize;
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
						// TODO: Resolve this when generating the sized images
						System.out.println("Bad configuration");
					}
				}
				default -> {
				
				}
			}
		}
	}
	
	private void paintBackground(Graphics2D g, float deltaTime) {
		g.setColor(CHECKER_D);
		g.fillRect(borderSize, borderSize, 8 * checkerSize, 8 * checkerSize);
		
		g.setColor(CHECKER_L);
		for (int i = 0; i < 64; i += 2) {
			int y = borderSize + (i >> 3) * checkerSize;
			int x = borderSize + ((i & 7) + ((i >> 3) & 1)) * checkerSize;
			g.fillRect(x, y, checkerSize, checkerSize);
		}
		
		// Update hover values
		for (int i = 0; i < 64; i++) {
			boolean hoverBit = ((hoverMask >>> i) & 1L) != 0;
			float value = hoverArray[i] + (hoverBit ? deltaTime : -deltaTime) / 2.0f;
			
			if (value < 0) {
				value = 0;
			} else if (value > 100) {
				value = 100;
			}
			
			int y = borderSize + (i >> 3) * checkerSize;
			int x = borderSize + (i & 7) * checkerSize;
			hoverArray[i] = value;
			
			// TODO: Cache color values
			g.setColor(new Color(0, 0, 0, value / 300.0f));
			g.fillRect(x, y, checkerSize, checkerSize);
		}
	}
	
	public void setCheckerSize(int size) {
		// TODO: Recompute sizes
		checkerSize = size;
	}
	
	public PlayableChessBoard setTargets(long value) {
		for (int i = 0; i < 64; i++) {
			boolean check = ((value >>> i) & 1L) != 0;
			board[i] = check ? PieceType.GREEN_DOT : PieceType.NONE;
		}
		
		repaint();
		return this;
	}
	
	public PlayableChessBoard setTargets(int[] pieces) {
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
