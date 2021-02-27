package hardcoded.chess.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import hardcoded.chess.open.*;
import hardcoded.chess.open.Analyser.Move0;
import hardcoded.chess.open.Analyser.Scan0;
import hardcoded.chess.open2.Chess;

public class ChessPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String[] buttons = {
		"Force Computer",
		"Hide Arrows",
		"Restart Game",
		"Flip Board"
	};
	
	private final BufferedImage[] pieces = new BufferedImage[12];
	
	private boolean hideArrows = true;
	private boolean flipBoard;
	private boolean promoting;
	private Set<Move> moves;
	private Scan0 scan;
	private int size;
	private ChessAudio audio;
	
	private ChessListener listener;
	private Chess board;
	
	private float[] hoverSquares = new float[64];
	
	private int promoteIdx = -1;
	private int selectedIdx = -1;
	private int buttonIdx = -1;
	
	private int dragOffsetX;
	private int dragOffsetY;
	private int dragPosX;
	private int dragPosY;
	private int dragIdx = -1;
	
	private MouseAdapter adapter = new MouseAdapter() {
		private int drgIdx = -1;
		
		public void mouseMoved(MouseEvent e) {
			promoteIdx = toPromoteIndex(e.getPoint());
			buttonIdx = toButtonIndex(e.getPoint());
		}
		
		public void mouseReleased(MouseEvent e) {
			if(dragIdx != -1) {
				if(listener != null) {
					listener.onSelectedSquare(toIndex(e.getPoint()));
					moves = Collections.emptySet();
				}
			}
			
			dragIdx = -1;
			drgIdx = -1;
			dragPosX = 0;
			dragPosY = 0;
		}
		
		public void mousePressed(MouseEvent e) {
			Point point = e.getPoint();
			
			buttonIdx = toButtonIndex(point);
			if(buttonIdx != -1) {
				onButtonPressed(buttonIdx);
				return;
			}
			
			if(promoting) {
				promoteIdx = toPromoteIndex(point);
				
				if(promoteIdx == 5) {
					promoting = false;
				}
				
				if(promoteIdx != -1) {
					if(listener != null) {
						listener.onPromoting(promoteIdx);
					}
					
					promoting = false;
				}
			} else {
				selectedIdx = toIndex(point);
				
				if(selectedIdx != -1) {
					moves = board.getPieceMoves(selectedIdx);
				}
				
				{
					drgIdx = selectedIdx;
					if(moves == null || moves.isEmpty()) {
						drgIdx = -1;
					}
					dragOffsetX = ((point.x - 15) / size) * size + size / 2 + 15;
					dragOffsetY = ((point.y - 15) / size) * size + size / 2 + 15;
					dragPosX = 0;
					dragPosY = 0;
				}
				
				if(listener != null) {
					listener.onSelectedSquare(selectedIdx);
				}
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			dragPosX = e.getX() - dragOffsetX;
			dragPosY = e.getY() - dragOffsetY;
			dragIdx = drgIdx;
			repaint();
		}
		
		
		private void onButtonPressed(int idx) {
			if(idx < 0 || idx >= buttons.length) return;
			String text = buttons[idx];
			
			switch(text) {
				case "Force Computer": {
					if(listener != null) {
						listener.onForceMove();
					}
					break;
				}
				case "Restart Game": {
					scan = null;
					board.setState(Chess.DEFAULT);
					if(listener != null) {
						listener.onRestartGame();
					}
					break;
				}
				case "Hide Arrows": {
					hideArrows = !hideArrows;
					break;
				}
				case "Flip Board": {
					flipBoard = !flipBoard;
					break;
				}
			}
		}
		
		int toIndex(Point point) {
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
			int x = (point.x - 15) / size;
			if(x > 1 && x < 7 && point.y > (15 + size * 3.5) && point.y <= (15 + size * 4.5)) {
				return x - 2;
			}
			
			return -1;
		}
		
		int toButtonIndex(Point point) {
			int start = buttons.length * 30 - 30;
			for(int i = 0; i < buttons.length; i++) {
				Rectangle rect = new Rectangle(
					size * 8 + 30 + 16,
					size * 8 - start + 30 * i + 3,
					142,
					28
				);
				if(rect.contains(point)) return i;
			}
			
			return -1;
		}
	};
	
	public ChessPanel(int size) {
		this.size = size;
		
		Locale.setDefault(Locale.US);
		Dimension dim = new Dimension(size * 8 + 30 + 200, size * 8 + 30);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
		addMouseListener(adapter);
		setBackground(Color.gray);
		
		try {
			audio = new ChessAudio();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			BufferedImage img = ImageIO.read(ChessPanel.class.getResourceAsStream("/chess_pieces.png"));
			
			double width = img.getWidth() / 6.0;
			double height = img.getHeight() / 2.0;
			int wi = (int)Math.floor(width);
			int hi = (int)Math.floor(height);
			
			for(int i = 0; i < 12; i++) {
				int x = (int)(width * (i % 6));
				int y = (int)(height * (i / 6));
				pieces[i] = img.getSubimage(x, y, wi, hi);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setListener(ChessListener listener) {
		this.listener = listener;
	}
	
	public void setChessboard(Chess board) {
		this.board = board;
	}
	
	public void setPromoting(boolean promoting) {
		this.promoting = promoting;
	}
	
	public void setScan(Scan0 scan) {
		this.scan = scan;
	}
	
	public ChessAudio getAudio() {
		return audio;
	}
	
	private static final Color dark = new Color(181, 136, 99);
	private static final Color light = new Color(240, 217, 181);
	private static final Color select = new Color(170, 162, 58);
	private static final Color select_press = new Color(0, 0, 0, 80);
	private static final Color scan_blue = new Color(0, 0, 128, 128);
	private static final Color scan_red = new Color(128, 0, 0, 128);
	private static final Color checked = new Color(240, 0, 0);
	
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		Point point = getMousePosition();
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
		paintInformation(g);
	}
	
	private void paintInformation(Graphics2D g) {
		g.translate(size * 8 + 15, -15);
		
		double baseline = 0;
		{
			Scan0 sc = scan;
			
			if(sc != null) {
				baseline = scan.base;
				
				if(scan.best != null) {
					baseline = scan.best.material;
				}
			}
			
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, 14, size * 8 + 30);
			
			if(flipBoard) {
				g.setColor(Color.black);
				g.fillRect(2, 2, 10, size * 8 + 26);
				
				double p = baseline / 1600.0;
				p += 0.5;
				if(p < 0) p = 0;
				if(p > 1) p = 1;
				
				p *= (size * 8 + 26.0);
				
				g.setColor(Color.white);
				g.fillRect(2, 2, 10, (int)p);
			} else {
				g.setColor(Color.white);
				g.fillRect(2, 2, 10, size * 8 + 26);
				
				double p = -baseline / 1600.0;
				p += 0.5;
				if(p < 0) p = 0;
				if(p > 1) p = 1;
				
				p *= (size * 8 + 26.0);
				
				g.setColor(Color.black);
				g.fillRect(2, 2, 10, (int)p);
			}
		}
		
		{
			int start = buttons.length * 30 - 30;
			for(int i = 0; i < buttons.length; i++) {
				
				if(buttonIdx == i) {
					g.setColor(Color.black);
				} else {
					g.setColor(Color.darkGray);
				}
				g.fillRect(16, size * 8 - start + 30 * i, 142, 28);
				
				g.setColor(Color.white);
				String text = buttons[i];
				Rectangle rect = new Rectangle(16, size * 8 - start + 30 * i + 3, 142, 28);
				
				drawCenteredString(g, text, rect);
			}
		}
		
		{
			g.setColor(Color.white);
			g.setFont(new Font("Calibri", Font.BOLD, 18));
			Rectangle rect = new Rectangle(5 + 14, 5, 100, 20);
			
			drawVCenteredString(g, String.format("Score %2.2f", baseline / 100.0), rect);
		}
	}
	
	
	
	private void paintBackground(Graphics2D g) {
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, size * 8 + 30, size * 8 + 30);
		g.translate(15, 15);
		
		g.setColor(light);
		g.fillRect(0, 0, size * 8, size * 8);
		g.setColor(dark);
		
		for(int i = 0; i < 64; i++) {
			int x = i & 7;
			int y = i / 8;
			
			if(!flipBoard) {
				y = 7 - y;
			} else {
				x = 7 - x;
			}
			
			if(((x + y) & 1) == 1) {
				g.fillRect(x * size, y * size, size, size);
			}
		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Calibri", Font.BOLD, 14));
		
		// Drawing the rows and ranks
		Rectangle rect;
		for(int i = 0; i < 8; i++) {
			
			String row;
			String rank;
			
			if(flipBoard) {
				row = String.valueOf((char)('a' + i));
				rank = String.valueOf(i + 1);
			} else {
				row = String.valueOf((char)('a' + i));
				rank = String.valueOf(8 - i);
			}
			
			rect = new Rectangle(size * i, size * 8 + 2, size, 15);
			drawCenteredString(g, row, rect);
			
			rect = new Rectangle(size * 8, size * i + 3, 15, size);
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
			int x = selectedIdx & 7;
			int y = selectedIdx / 8;
			
			if(flipBoard) {
				x = 7 - x;
			} else {
				y = 7 - y;
			}
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
			
			int mul = board.isWhiteTurn() ? 1:-1;
			if(board.isChecked()) {
				int index = board.findPiece(Pieces.KING * mul, 0); {
					int x = index & 7;
					int y = index / 8;
					
					if(flipBoard) {
						x = 7 - x;
					} else {
						y = 7 - y;
					}
					
					g.fillRect(x * size, y * size, size, size);
				}
			}
		}
		
		if(board.getLastMove() != null) {
			Move move = board.getLastMove();
			
			g.setColor(new Color(0, 0, 0, 60));
			drawMove(g, move, 13.0f);
		}
		
		for(int i = 0; i < 64; i++) {
			int x = i & 7;
			int y = i / 8;
			
			if(!flipBoard) {
				y = 7 - y;
			} else {
				x = 7 - x;
			}
			
			int piece = board.getPieceAt(i);
			if(piece != 0) {
				if(piece < 0) piece = 6 - piece;
				if(dragIdx != i) {
					g.drawImage(pieces[piece - 1], x * size, y * size, size, size, null);
				}
			}
		}
		
		if(dragIdx != -1) {
			int x = dragIdx & 7;
			int y = dragIdx / 8;
			
			if(!flipBoard) {
				y = 7 - y;
			} else {
				x = 7 - x;
			}
			
			int piece = board.getPieceAt(dragIdx);
			if(piece != 0) {
				if(piece < 0) piece = 6 - piece;
				
				Shape old = g.getClip();
				g.setClip(0, 0, size * 8, size * 8);
				g.drawImage(pieces[piece - 1], x * size + dragPosX, y * size + dragPosY, size, size, null);
				g.setClip(old);
			}
		}
		
		if(!hideArrows) {
			Scan0 sc = scan;
//			double baseline = 0;
//			if(sc != null) {
//				baseline = scan.base;
//				
//				if(scan.best != null) {
//					baseline = scan.best.material;
//				}
//			}
			
			if(sc != null) {
				Stroke old = g.getStroke();
				g.setColor(Color.black);
				
				int max = 10;
				int bs = sc.branches.size();
				int st = sc.white ? Math.max(0, bs - max):0;
				int et = sc.white ? Math.min(st + max, bs):Math.min(max, bs);
				
				for(int i = st; i < et; i++) {
					Move0 dmove = sc.branches.get(i);
					
					float mat = (float)(dmove.material - sc.base);
					g.setColor(scan_blue);
//					if(sc.white) {
//						g.setColor(mat < 0 ? scan_red:scan_blue);
//					} else {
//						g.setColor(mat > 0 ? scan_red:scan_blue);
//					}
					
					mat /= 100;
					if(mat < 0) mat = -mat;
					
					if(Math.abs(mat) < 2) {
						mat = 2;
					}
					
					float size = (Math.abs(mat) + 0.5f) * 2.0f;
					if(size > this.size / 5) size = this.size / 5;
					
					size = 4 + 20 / (3 * size + 0.1f);
					
					drawMoveDebug(g, dmove.move, size, (int)dmove.material);
				}
				
				g.setStroke(old);
			}
		}

		if(!hideArrows) {
			Scan0 sc = scan;
			if(sc != null) {
				java.util.List<Move0> list = sc.follow;
				for(int i = 0; i < list.size(); i++) {
					Move0 m = list.get(i);
					g.setColor(new Color(255, 255, 0, 60));
					drawMove(g, m.move, ((list.size() - i + 1) * 5));
				}
			}
		}
		
		if(promoting) {
			g.setColor(new Color(0, 0, 0, 64));
			g.fillRect(0, 0, size * 8, size * 8);
			
			g.setColor(new Color(0, 0, 0, 128));
			for(int i = 0; i < 5; i++) {
				int xp = size * 2 + i * size;
				int yp = size * 4 - size / 2;
				
				if(i == promoteIdx) {
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
}
