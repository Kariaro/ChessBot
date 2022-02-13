package me.hardcoded.chess.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Set;

import javax.swing.JFrame;

import me.hardcoded.chess.open.*;
import me.hardcoded.chess.open.ScanMoveOld;
import me.hardcoded.chess.open.ScanOld;
import me.hardcoded.chess.open2.Analyser3;
import me.hardcoded.chess.open2.Analyser7;
import me.hardcoded.chess.open2.Chess;

@Deprecated(forRemoval = true)
public class ChessWindow implements Runnable {
	private static final int size = 80;
	
	private JFrame frame;
	private boolean running;
	
	private Chess board;
	private int selectedIndex = -1;
	private Set<Move> moves;
	private ChessPanel panel;
	
	private ChessListener listener = new ChessListener() {
		public void onPromoting(int idx) {
			if(idx == 4) {
				moves = null;
				return;
			}
			
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
					tick();
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
		
		@Override
		public void onRestartGame() {
			
		}
		
		private boolean hasMove = false;
		public void onForceMove() {
			if(hasMove) return;
			hasMove = true;
			
			Thread thread = new Thread(() -> {
				boolean comp = false;
				ScanOld scan = null;
				
				if(comp) {
					if(board.isWhiteTurn()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						scan = Analyser3.analyse(board);
					} else {
						scan = Analyser3.analyse(board);
					}
				} else {
					scan = Analyser7.analyse(board);
				}
				
				ScanMoveOld best = scan.best;
				if(best != null) {
					try {
						// Play the sound eitherway
						Thread.sleep(20);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					board.doMove(best.move);
					panel.setScan(scan);
					panel.getAudio().playChessMove();
					
					hasMove = false;
					
					if(comp) onForceMove();
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
		
		board = new Chess();
		panel.setChessboard(board);
		frame.add(panel);
		frame.pack();
	}

	private ScanOld scan;
	
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
