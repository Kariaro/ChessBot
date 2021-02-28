package hardcoded.chess.utils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import hardcoded.chess.open.Action;
import hardcoded.chess.open.Move;
import hardcoded.chess.open.Pieces;
import hardcoded.chess.open3.ChessB;
import hardcoded.chess.utils.BoardEvent.DragEvent;
import hardcoded.chess.utils.BoardEvent.InitEvent;
import hardcoded.chess.utils.BoardEvent.PressEvent;

/**
 * This should only render the game but not do any computation.
 * 
 * @author HardCoded
 */
public class BoardPanel extends JPanel {
	private static final long serialVersionUID = 7754391354884633107L;
	
	private static final BufferedImage[] pieces = new BufferedImage[12];
	
	static {
		try {
			BufferedImage img = ImageIO.read(BoardPanel.class.getResourceAsStream("/chess_pieces.png"));
			
			double width = img.getWidth() / 6.0;
			double height = img.getHeight() / 2.0;
			int wi = (int)Math.floor(width);
			int hi = (int)Math.floor(height);
			
			for(int i = 0; i < 12; i++) {
				int x = (int)(width * (i % 6));
				int y = (int)(height * (i / 6));
				pieces[i] = img.getSubimage(x, y, wi, hi);
			}
		} catch(Exception e) {
			
		}
		
		// Fail safe way if we can't load the image
		int ds = 128;
		for(int i = 0; i < 12; i++) {
			if(pieces[i] == null) {
				int id = (i > 5 ? i - 5: -1 - i);
				String text = Pieces.toString(id);
				
				pieces[i] = new BufferedImage(ds, ds, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = pieces[i].createGraphics();
				
				if(id < 0) {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, ds, ds);
					g.setColor(Color.WHITE);
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, ds, ds);
					g.setColor(Color.BLACK);
				}
				
				g.setFont(g.getFont().deriveFont(34.0f));
				FontMetrics metrics = g.getFontMetrics(g.getFont());
				int x = (ds - metrics.stringWidth(text)) / 2;
				int y = ((ds - metrics.getHeight()) / 2) + metrics.getAscent();
				g.drawString(text, x, y);
			}
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		BoardPanel panel = new BoardPanel(64);
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		panel.start();
	}
	
	// This field should not be written to.
	// When changing this field call setFlipped(boolean)
	private boolean flipBoard;
	// This field should not be accessed by itself.
	// When changing this field call setPromoting(boolean)
	private boolean promoting;
	private Set<Move> moves;
	
	private BoardListener listener = BoardListener.DUMMY;
	private ChessB board;
	
	// Graphical grid for some hovered squares
	private float[] hoverSquares = new float[64];
	
	private int promotionIdx = -1;
	private int selectedIdx = -1;
	
	private boolean isDragging = false;
	private int dragIdx = -1;
	private int dragPosX;
	private int dragPosY;
	
	
	/**
	 * The size in pixels of a tile.
	 */
	private int size;
	
	private MouseAdapter adapter = new MouseAdapter() {
		private int dragOffsetX = 0;
		private int dragOffsetY = 0;
		private int drgIdx = -1;
		
		public void mouseReleased(MouseEvent e) {
			if(isDragging) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					int from = selectedIdx;
					int to = toIndex(e.getPoint());
					
					if(from != to) {
						listener.onDrag(new DragEvent(board, BoardPanel.this, from, to));
					}
					
					isDragging = false;
					dragIdx = -1;
					drgIdx = -1;
					dragPosX = 0;
					dragPosY = 0;
					
					repaint();
				}
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if(!isInside(e.getPoint())) {
				setPromoting(true);
				repaint();
				return;
			}
			
			if(e.getButton() == MouseEvent.BUTTON1) {
				onPress(e.getPoint());
			}
			
			repaint();
			
			
			/* Call the onPress event */ {
				Point mouse = e.getPoint();
				
				float x = (mouse.x - 15) / (size * 1.0f);
				x = (x < 0 ? 0:(x > 8 ? 8:x));
				
				float y = (mouse.y - 15) / (size * 1.0f);
				y = (y < 0 ? 0:(y > 8 ? 8:y));
				
				if(flipBoard) {
					x = 8 - x;
				} else {
					y = 8 - y;
				}
				
				x = (x < 0 ? 0:(x > 7.99 ? 7.99f:x));
				y = (y < 0 ? 0:(y > 7.99 ? 7.99f:y));
				
				listener.onPress(new PressEvent(board, BoardPanel.this, x, y, e.getButton()));
				repaint();
			}
		}
		
		private void onPress(Point mouse) {
			if(!promoting) {
				int iidx = toIndex(mouse);
				selectedIdx = iidx;
				
				if(selectedIdx != -1) {
					moves = null; //board.getPieceMoves(iidx);
				}
				
				isDragging = true;
				drgIdx = iidx;
				if(moves == null || moves.isEmpty()) {
					//drgIdx = -1;
				}
				
				dragOffsetX = ((mouse.x - 15) / size) * size + size / 2 + 15;
				dragOffsetY = ((mouse.y - 15) / size) * size + size / 2 + 15;
				dragPosX = 0;
				dragPosY = 0;
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			onMove(e);
			if(!isDragging) return;
			
			dragPosX = e.getX() - dragOffsetX;
			dragPosY = e.getY() - dragOffsetY;
			dragIdx = drgIdx;
			repaint();
		}
		
		public void mouseMoved(MouseEvent e) {
			onMove(e);
		}
		
		private void onMove(MouseEvent e) {
			if(promoting) {
				promotionIdx = toPromoteIndex(e.getPoint());
			}
			
			repaint();
		}
		
		int toIndex(Point point) {
			if(!isInside(point)) return -1;
			
			int x = (point.x - 15) / size;
			int y = (point.y - 15) / size;
			if(x >= 0 && x < 8 && y >= 0 && y < 8) {
				
				if(flipBoard) {
					x = 7 - x;
				} else {
					y = 7 - y;
				}
				
				int idx = x + y * 8;
				return idx;
			}
			
			return -1;
		}
		
		int toPromoteIndex(Point point) {
			if(!isInside(point)) return -1;
			
			int x = (point.x - 15) / size;
			if(x > 1 && x < 6 && point.y > (15 + size * 3.5) && point.y <= (15 + size * 4.5)) {
				return x - 2;
			}
			
			return -1;
		}
		
		boolean isInside(Point mouse) {
			return !(mouse.x < 15 || mouse.x > (size * 8 + 15)
				  || mouse.y < 15 || mouse.y > (size * 8 + 15));
		}
	};
	
	public BoardPanel(int size) {
		this.size = size;
		
		Locale.setDefault(Locale.US);
		Dimension dim = new Dimension(size * 8 + 30, size * 8 + 30);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
		addMouseListener(adapter);
		setBackground(Color.gray);
		
		board = new ChessB();
		
		try {
			//audio = new ChessAudio();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setListener(BoardListener listener) {
		this.listener = listener;
	}
	
	public void setChessboard(ChessB board) {
		this.board = board;
	}
	
	private static final Color dark = new Color(181, 136, 99);
	private static final Color light = new Color(240, 217, 181);
	private static final Color select = new Color(170, 162, 58);
	private static final Color select_press = new Color(0, 0, 0, 80);
	// private static final Color scan_blue = new Color(0, 0, 128, 128);
	// private static final Color scan_red = new Color(128, 0, 0, 128);
	private static final Color checked = new Color(240, 0, 0);
	private static final Color shaded = new Color(0, 0, 0, 64);
	
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		Point point = getMousePosition();
		
		// TODO: This has inconsistent shading times.. Try use something that animates.
		if(point != null) {
			int x = (point.x - 15) / size;
			int y = (point.y - 15) / size;
			if(x >= 0 && x < 8 && y >= 0 && y < 8) {
				int idx = x + y * 8;
				hoverSquares[idx] += 1;
			}
		}
		
		for(int i = 0; i < 64; i++) hoverSquares[i] *= 0.6f;
		
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), getHeight());
		paintBackground(g);
	}
	
	private void paintBackground(Graphics2D g) {
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, size * 8 + 30, size * 8 + 30);
		g.translate(15, 15);
		
		g.setColor(light);
		g.fillRect(0, 0, size * 8, size * 8);
		g.setColor(dark);
		
		for(int i = 1; i < 64; i += 2) {
			int y = i >> 3;
			int x = (i & 7) - (y & 1);
			
			g.fillRect(x * size, y * size, size, size);
		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Calibri", Font.BOLD, 13));
		
		// Drawing the rows and ranks
		Rectangle rect;
		for(int i = 0; i < 8; i++) {
			String row;
			String rank;
			
			if(flipBoard) {
				row = String.valueOf((char)('h' - i));
				rank = String.valueOf(i + 1);
			} else {
				row = String.valueOf((char)('a' + i));
				rank = String.valueOf(8 - i);
			}
			
			rect = new Rectangle(size * i, size * 8 + 1, size, 15);
			drawCenteredString(g, row, rect);
			
			rect = new Rectangle(size * 8, size * i + 1, 15, size);
			drawCenteredString(g, rank, rect);
		}
		
		if(!promoting) {
			for(int i = 0; i < 64; i++) {
				int x = i & 7;
				int y = i / 8;
				
				double opacity = hoverSquares[i] * 20;
				if(opacity > 255) opacity = 255;
				g.setColor(new Color(0, 0, 0, (int)opacity));
				g.fillRect(x * size, y * size, size, size);
			}
		}
		
		if(selectedIdx >= 0) {
			g.setColor(select_press);
			int x = flipBoard ? (7 - (selectedIdx & 7)):(selectedIdx & 7);
			int y = flipBoard ? (selectedIdx>> 3):(7 - (selectedIdx>> 3));
			
			g.fillRect(x * size, y * size, size, size);
			
			int orb = size / 3;
			int mrg = (size - orb) / 2;
			if(moves != null) {
				g.setColor(select);
				for(Move move : moves) {
					x = move.to() & 7;
					y = move.to() / 8;
					if(flipBoard) {
						x = 7 - x;
					} else {
						y = 7 - y;
					}
					
					g.fillOval(x * size + mrg, y * size + mrg, orb, orb);
					
					if(move.action() == Action.KINGSIDE_CASTLE || move.action() == Action.QUEENSIDE_CASTLE) {
						Area a = new Area(new Rectangle2D.Float(x * size, y * size, size, size));
						a.subtract(new Area(new Ellipse2D.Float(x * size, y * size, size, size)));
						g.fill(a);
					}
				}
			}
		}
		
		{
			g.setColor(checked);
			
//			int mul = board.isWhite() ? 1:-1;
//			if(board.isChecked()) {
//				int index = board.findPiece(Pieces.KING * mul, 0); {
//					int x = (flipBoard ? (7 - (index & 7)):(index & 7));
//					int y = (flipBoard ? (index>> 3):(7 - (index>> 3)));
//					
//					g.fillRect(x * size, y * size, size, size);
//				}
//			}
		}
		
		for(int i = 0; i < 64; i++) {
			int x = flipBoard ? (7 - (i & 7)):(i & 7);
			int y = flipBoard ? (i>> 3):(7 - (i>> 3));
			
			int piece = board.getPiece(i);
			if(piece != 0) {
				if(piece < 0) piece = 6 - piece;
				if(dragIdx != i) {
					g.drawImage(pieces[piece - 1], x * size, y * size, size, size, null);
				}
			}
		}
		
		if(dragIdx != -1) {
			int x = flipBoard ? (7 - (dragIdx & 7)):(dragIdx & 7);
			int y = flipBoard ? (dragIdx>> 3):(7 - (dragIdx>> 3));
			
			int piece = board.getPiece(dragIdx);
			if(piece != 0) {
				if(piece < 0) piece = 6 - piece;
				
				// Create a cut so that the piece does not render outside the board
				Shape old = g.getClip();
				g.setClip(0, 0, size * 8, size * 8);
				g.drawImage(pieces[piece - 1], x * size + dragPosX, y * size + dragPosY, size, size, null);
				g.setClip(old);
			}
		}
		
		if(promoting) {
			g.setColor(shaded);
			g.fillRect(0, 0, size * 8, size * 8);
			
			g.setColor(new Color(0, 0, 0, 128));
			for(int i = 0; i < 4; i++) {
				int xp = size * 2 + i * size;
				int yp = size * 4 - size / 2;
				
				if(i == promotionIdx) {
					g.fillRect(xp, yp, size, size);
				}
				g.drawImage(pieces[i + 1], xp, yp, size, size, null);
			}
		}
	}
	
	private void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g.drawString(text, x, y);
	}
	
	private void drawVCenteredString(Graphics2D g, String text, Rectangle rect) {
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g.drawString(text, rect.x, y);
	}
	
	private void drawMove(Graphics2D g, Move move, float scale) {
		int a = move.from();
		int b = move.to();
		
		if(a == b) return;
		
		int x0 = a & 7;
		int y0 = a / 8;
		if(!flipBoard) y0 = 7 - y0; else x0 = 7 - x0;
		
		int x1 = b & 7;
		int y1 = b / 8;
		if(!flipBoard) y1 = 7 - y1; else x1 = 7 - x1;
		
		x0 = x0 * size + size / 2;
		y0 = y0 * size + size / 2;
		x1 = x1 * size + size / 2;
		y1 = y1 * size + size / 2;
		drawArrow(g, x0, y0, x1, y1, scale);
	}
	
	private void drawMoveDebug(Graphics2D g, Move move, float scale, int mat) {
		int a = move.from();
		int b = move.to();
		
		if(a == b) return;
		
		int x0 = a & 7;
		int y0 = a / 8;
		if(!flipBoard) y0 = 7 - y0; else x0 = 7 - x0;
		
		int x1 = b & 7;
		int y1 = b / 8;
		if(!flipBoard) y1 = 7 - y1; else x1 = 7 - x1;
		
		x0 = x0 * size + size / 2;
		y0 = y0 * size + size / 2;
		x1 = x1 * size + size / 2;
		y1 = y1 * size + size / 2;
		drawArrow(g, x0, y0, x1, y1, scale);
		
		g.setColor(Color.white);
		Rectangle rect = new Rectangle(x0, y0, x1 - x0, y1 - y0);
		drawCenteredString(g, String.format("%.2f", mat / 100.0), rect);
	}
	
	private void drawArrow(Graphics2D g, int x0, int y0, int x1, int y1, double size) {
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke((float)size));
		
		double off = 0.25;
		double m = size * 4;
		double c = 1.14;
		double angle = Math.atan2(y1 - y0, x1 - x0);
		double sa1 = Math.sin(angle -     Math.PI / 4.0 - off) * m;
		double ca1 = Math.cos(angle -     Math.PI / 4.0 - off) * m;
		double sa2 = Math.sin(angle - 3 * Math.PI / 4.0 + off) * m;
		double ca2 = Math.cos(angle - 3 * Math.PI / 4.0 + off) * m;
		
		double mx = ((x1 + sa1 * c) + (x1 + sa2 * c)) / 2.0;
		double my = ((y1 - ca1 * c) + (y1 - ca2 * c)) / 2.0;
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo(x0, y0);
		path.lineTo(mx, my);
		g.draw(path);
		
		Path2D.Double arrow = new Path2D.Double();
		arrow.moveTo(x1, y1);
		arrow.lineTo(x1 + sa1, y1 - ca1);
		arrow.lineTo(x1 + sa2, y1 - ca2);
		g.fill(arrow);
		
		g.setStroke(old);
	}
	
	public boolean isPromoting() {
		return promoting;
	}
	
	public void setPromoting(boolean promoting) {
		// Remove dragging if we dragged a piece
		if(dragIdx != -1) {
			isDragging = false;
			dragIdx = -1;
			dragPosX = 0;
			dragPosY = 0;
		}
		
		this.promotionIdx = -1;
		this.promoting = promoting;
	}
	
	public boolean isFlipped() {
		return flipBoard;
	}
	
	public void setFlipped(boolean flipped) {
		this.flipBoard = flipped;
	}
	
	public void start() {
		listener.onInit(new InitEvent(board, this));
	}
}
