package hardcoded.chess.gui;

import java.awt.*;
import java.util.Set;

import javax.swing.JFrame;

import hardcoded.chess.open.*;
import hardcoded.chess.open.Analyser.Scan0;

public class ChessWindow implements Runnable {
	private static final int size = 80;
	
	private JFrame frame;
	private boolean running;
	
	private Chessboard board;
	private int selectedIndex = -1;
	private Set<Move> moves;
	private ChessPanel panel;
	
	private ChessListener listener = new ChessListener() {
		public void onPromoting(int idx) {
			if(idx == 4) {
				moves = null;
				return;
			}
			
			System.out.println(moves);
			Move move = null;
			for(Move m : moves) {
				if(m.action() != Action.PROMOTE) continue;
				
				int id = m.id();
				id = id < 0 ? -id:id;
				
				if(idx == 0 && id == Pieces.QUEEN) move = m;
				else if(idx == 1 && id == Pieces.BISHOP) move = m;
				else if(idx == 2 && id == Pieces.KNIGHT) move = m;
				else if(idx == 3 && id == Pieces.ROOK) move = m;
				else {
					// BAD MOVE????
				}
			}
			
			board.doMove(move);
			onMovePlayed(move, idx);
			panel.getAudio().playChessMove();
			moves = null;
		}
		
		public void onSelectedSquare(int idx) {
			if(idx == -1) {
				selectedIndex = -1;
				moves = null;
				return;
			}
			
			System.out.println("Selected: " + ChessUtils.toSquare(idx));
			Move move = getMove(idx);
			if(move == null) {
				selectedIndex = idx;
				moves = board.getPieceMoves(selectedIndex);
			} else {
				System.out.println("move: " + move + " action:(" + move.action() + ")");
				
				if(move.action() == Action.PROMOTE) {
					panel.setPromoting(true);
				} else {
					board.doMove(move);
					onMovePlayed(move, idx);
					
					panel.getAudio().playChessMove();
					moves = null;
					selectedIndex = -1;
				}
			}
		}
		
		public void onMovePlayed(Move move, int idx) {
			onForceMove();
		}
		
		private boolean hasMove = false;
		public void onForceMove() {
			if(hasMove) return;
			hasMove = true;
			
			Thread thread = new Thread(() -> {
				scan = Analyser2.analyse(board);
				if(scan.best != null) {
					board.doMove(scan.best.move);
					panel.setScan(scan);
					panel.getAudio().playChessMove();
				}
				
				hasMove = false;
			});
			thread.setDaemon(true);
			thread.start();
		}
		
		private Move getMove(int index) {
			if(moves == null || moves.isEmpty()) return null;
			
			for(Move m : moves) {
				if(m.to() == index) {
					return m;
				}
			}
			
			return null;
		}
	};
	
	public ChessWindow() {
		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		
		frame = new JFrame("Chess window");
		
		Dimension dim = new Dimension(size * 8 + 30 + 16 + 160, size * 8 + 30 + 39);
		frame.setMinimumSize(dim);
		frame.setPreferredSize(dim);
		frame.setSize(dim);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		panel = new ChessPanel(size);
		panel.setListener(listener);
		
		board = new Chessboard();
		panel.setChessboard(board);
		frame.add(panel);
		frame.pack();
	}

	private Scan0 scan;
	
	@SuppressWarnings("unused")
	public void run() {
		long last = System.currentTimeMillis();
		double tick = last;
		int frame = 0;
		int times = 0;
		
		while(running) {
			long now = System.currentTimeMillis();
			
			if(now > last + 1000) {
				last += ((now - last) / 1000) * 1000;
				// System.out.println("fps: " + frame);
				
				// doMove();
				
				frame = 0;
				times++;
			}
			
			if(now > tick + 20) {
				tick += 20;
				frame ++;
				
				tick();
			}
		}
	}
	
	public void tick() {
		frame.repaint(20);
	}
	
//	public void doMove() {
//		boolean hasMove = false;
//		if(board.isWhiteTurn()) {
//			//scan = Analyser.analyse(board);
//			//hasMove = true;
//		} else {
//			scan = Analyser2.analyse(board);
//			hasMove = true;
//		}
//		
//		if(hasMove) {
//			if(scan.best != null) {
//				Move move = scan.best.move;
//				board.doMove(move);
//				tick();
//				
//				System.out.println("Moving: " + move);
//			} else {
//				System.out.println("Stalemate or checkmate!!!");
//			}
//			
//			panel.setScan(scan);
//		}
//	}
	
	public void paintDetails(Graphics2D g) {
		double baseline = 0;
		{
			g.translate(size * 8, 0);
			
			g.setColor(Color.black);
			g.setFont(new Font("Calibri", Font.PLAIN, 20));
			g.setColor(Color.black);
			g.drawString("Score", 30, 30);
			g.drawString(String.format("%.4f", baseline), 30, 60);
			
			g.drawString("Status", 30, 100);
			g.drawString(String.format("%s", "PLAYING"), 30, 130);
			
			
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, 14, size * 8);
			
			g.setColor(Color.white);
			g.fillRect(2, 2, 10, size * 8 - 4);
			
			double p = -baseline / 16.0;
			p += 0.5;
			if(p < 0) p = 0;
			if(p > 1) p = 1;
			
			p *= (size * 8 - 4.0);
			
			g.setColor(Color.black);
			g.fillRect(2, 2, 10, (int)p);
			
			g.translate(-size * 8, 0);
		}
	}
	
	public void start() {
		if(running) return;
		running = true;
		Thread thread = new Thread(this);
		thread.start();
		
		frame.setVisible(true);
	}
	
	public void stop() {
		running = false;
		frame.setVisible(false);
	}
	
}
