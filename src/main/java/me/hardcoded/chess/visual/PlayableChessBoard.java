package me.hardcoded.chess.visual;

import me.hardcoded.chess.advanced.ChessGenerator;
import me.hardcoded.chess.advanced.ChessPieceManager;
import me.hardcoded.chess.api.ChessBoard;
import me.hardcoded.chess.api.ChessMove;
import me.hardcoded.chess.open.Pieces;
import me.hardcoded.chess.utils.ReadReset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
	
	/**
	 * This field contains the highlighting value for each square on the board.
	 *
	 * This value will for each frame that the mouse is hovering over a square increase by
	 * {@code deltaTime / 2} where {@code deltaTime} is the amount of milliseconds since the last frame.
	 *
	 * This counter will stop when it reaches {@code 0} or {@code 100}.
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
	
	/**
	 * This field contains the position of the currently dragged piece.
	 */
	private final Point draggedPosition;
	
	/**
	 * This field contains the index of the piece on the board that is currently dragged.
	 */
	private int draggedIndex;
	
	/**
	 * This field contains the currently displayed chess board.
	 */
	private ChessBoard board;
	
	/**
	 * This field contains a bit field of what positions on the board are playable.
	 */
	private long movesMask;
	
	/**
	 * This field contains the index of the selected square on the board.
	 */
	private int playerSelection;
	
	/**
	 * This field contains a map of player moves.
	 *
	 * Creating a local reference copy of this object before using it.
	 * This field must never be {@code null}
	 */
	private Map<Integer, Set<ChessMove>> playerMoves;
	
	/**
	 * This field is {@code true} if this board is waiting for player input.
	 */
	private boolean awaitPlayer;
	
	/**
	 * This field contains the move that was played by the player.
	 */
	private final ReadReset<ChessMove> playerMoveTest;
	
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
		this.hoverArray = new float[64];
		this.renderTimer = new Timer(16, this); // ~50 fps
		this.renderTimer.start();
		this.draggedPosition = new Point();
		this.draggedIndex = -1;
		this.playerSelection = -1;
		this.playerMoveTest = new ReadReset<>();
		this.playerMoves = Map.of();
		
		// Default settings
		this.whitePov = true;
		this.checkerSize = 90;
		this.borderSize = 16;
		this.movesMask = 0;
		
		this.recomputeImages();
		
		MouseAdapter boardAdapter = new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				int half = checkerSize / 2;
				draggedIndex = playerSelection;
				draggedPosition.setLocation(e.getX() - half, e.getY() - half);
				
				// Repaint for smoother dragging
				repaint();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					whitePov = !whitePov;
				}
				
				if (e.getButton() == MouseEvent.BUTTON1) {
					int half = checkerSize / 2;
					draggedPosition.setLocation(e.getX() - half, e.getY() - half);
					int selection = getPieceIndex(e.getPoint());
					
					// Check if we have a piece selected and that the square we pressed is a valid move
					if (playerSelection != -1 && ((movesMask >>> selection) & 1L) != 0) {
						playMove(playerSelection, selection);
						playerSelection = -1;
					} else {
						// If the player selects the same piece we deselect it
						playerSelection = (playerSelection == selection) ? -1 : selection;
					}
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (draggedIndex != -1) {
					int selection = getPieceIndex(e.getPoint());
					if (((movesMask >>> selection) & 1L) != 0) {
						playMove(playerSelection, selection);
					}
					playerSelection = -1;
				}
				
				draggedIndex = -1;
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
			
			public int getPieceIndex(Point p) {
				int mx = (p.x - borderSize) / checkerSize;
				int my = (p.y - borderSize) / checkerSize;
				
				if (whitePov) {
					mx = 7 - mx;
					my = 7 - my;
				}
				
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
			// Only repaint when we need
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
		paintPieces(g, deltaTime);
	}
	
	private void paintPieces(Graphics2D g, float deltaTime) {
		// Save a local reference of the object
		final int draggedIndex = this.draggedIndex;
		final ChessBoard board = this.board;
		
		// Paint dots
		for (int i = 0; i < 64; i++) {
			if (((movesMask >> i) & 1L) != 0) {
				int xp = whitePov ? (7 - (i >> 3)) : (i >> 3);
				int yp = whitePov ? (7 - (i & 7)) : (i & 7);
				int y = borderSize + xp * checkerSize;
				int x = borderSize + yp * checkerSize;
				
				drawPiece(g, x, y, PieceType.GREEN_DOT);
			}
		}
		
		// Paint pieces
		if (board != null) {
			for (int i = 0; i < 64; i++) {
				if (i == draggedIndex) {
					continue;
				}
				
				int xp = whitePov ? (7 - (i >> 3)) : (i >> 3);
				int yp = whitePov ? (7 - (i & 7)) : (i & 7);
				int y = borderSize + xp * checkerSize;
				int x = borderSize + yp * checkerSize;
				drawPiece(g, x, y, getPiece(board.getPiece(i)));
			}
			
			if (draggedIndex != -1) {
				PieceType piece = getPiece(board.getPiece(draggedIndex));
				drawPiece(g, draggedPosition.x, draggedPosition.y, piece);
			}
		}
	}
	
	private void drawPiece(Graphics2D g, int x, int y, PieceType type) {
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
		
		g.setColor(Color.white);
		// TODO: Cache and make sure this font exist
		g.setFont(new Font("Calibri", Font.BOLD, 14));
		
		// Drawing the rows and ranks
		Rectangle rect;
		for (int i = 0; i < 8; i++) {
			String row;
			String rank;
			
			if (whitePov) {
				row = String.valueOf((char)('a' + i));
				rank = String.valueOf(8 - i);
			} else {
				row = String.valueOf((char)('h' - i));
				rank = String.valueOf(i + 1);
			}
			
			rect = new Rectangle(borderSize + checkerSize * i, borderSize + checkerSize * 8 + 2, checkerSize, 15);
			drawCenteredString(g, row, rect);
			
			rect = new Rectangle(borderSize + checkerSize * 8, borderSize + checkerSize * i + 3, 15, checkerSize);
			drawCenteredString(g, rank, rect);
		}
	}
	
	private void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g.drawString(text, x, y);
	}
	
	public void setCheckerSize(int size) {
		// TODO: Recompute sizes
		checkerSize = size;
	}
	
	public void setTargets(long value) {
		movesMask = value;
	}
	
	public void setDisplayedBoard(ChessBoard board) {
		this.board = board;
	}
	
	
	/**
	 * This method will wait for the player to make a move.
	 * If the move was not allowed it will throw an exception.
	 *
	 * @return the move that was played
	 */
	public synchronized ChessMove awaitMoveNonNull() {
		return Objects.requireNonNull(awaitMove());
	}
	
	/**
	 * This method will wait for the player to make a move.
	 *
	 * @return the move that was played
	 */
	public synchronized ChessMove awaitMove() {
		if (board == null) {
			return null;
		}
		
		awaitPlayer = true;
		playerMoveTest.read();
		
		// Display the allowed player moves
		Map<Integer, Set<ChessMove>> moves = new HashMap<>();
		{
			Set<ChessMove> allMoves = ChessGenerator.generateGuiMoves(board);
			for (ChessMove move : allMoves) {
				moves.computeIfAbsent(move.from, v -> new HashSet<>()).add(move);
			}
		}
		playerMoves = Collections.unmodifiableMap(moves);
		
		ChessMove move;
		int lastSelection = -1;
		try {
			while (true) {
				Thread.sleep(50);
				
				if (lastSelection != playerSelection) {
					lastSelection = playerSelection;
					
					Set<ChessMove> set = moves.get(playerSelection);
					
					long mask = 0;
					if (set != null) {
						for (ChessMove m : set) {
							mask |= 1L << m.to;
						}
					}
					
					movesMask = mask;
					repaint();
					continue;
				}
				
				// A player move was selected
				move = playerMoveTest.read();
				if (move != null) {
					if (!ChessGenerator.playMove(board, move)) {
						// The move was invalid so we try again
						continue;
					}
					
					break;
				}
			}
		} catch (InterruptedException ignore) {
			move = null;
		}
		
		playerMoves = Map.of();
		awaitPlayer = false;
		movesMask = 0;
		return move;
	}
	
	private void playMove(int from, int to) {
		if (!awaitPlayer) {
			return;
		}
		
		// There should not be multiple moves possible
		ChessMove move = playerMoves.getOrDefault(from, Set.of())
			.stream().filter(m -> m.from == from && m.to == to).findFirst().orElse(null);
		
		if (move == null) {
			return;
		}
		
		ChessBoard board = this.board;
		if (board == null) {
			return;
		}
		
		if ((move.special & 0b11_000000) == ChessPieceManager.SM_PROMOTION) {
			// Prompt the user for some choices
			int option = JOptionPane.showOptionDialog(this, null, "Promotion",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				new String[] { "Queen", "Rook", "Bishop", "Knight", "None" },
				"None"
			);
			
			int value = switch (option) {
				case 0 -> Pieces.QUEEN;
				case 1 -> Pieces.ROOK;
				case 2 -> Pieces.BISHOP;
				case 3 -> Pieces.KNIGHT;
				default -> Pieces.NONE;
			} << 3;
			
			// Update the move with the new promotion value
			move = new ChessMove(move.piece, move.from, move.to, ChessPieceManager.SM_PROMOTION | value);
		}
		
		// Validate the move
		if (!ChessGenerator.isValid(board, move)) {
			return;
		}
		
		// Remove the awaited move
		playerMoveTest.write(move);
	}
	
	@Deprecated
	private PieceType getPiece(int id) {
		return switch (id) {
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
