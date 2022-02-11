package me.hardcoded.chess.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;

public class ChessAudio {
	private Clip[] clips = new Clip[16];
	private volatile int index;
	
	
	public ChessAudio() throws Exception {
		for(int i = 0; i < clips.length; i++) {
			clips[i] = createClip("/chess_move.wav");
		}
	}
	
	private Clip createClip(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		InputStream is = ChessAudio.class.getResourceAsStream(path);
		AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
		Clip clip = AudioSystem.getClip();
		clip.open(stream);
		
		FloatControl control = (FloatControl)clip.getControl(Type.MASTER_GAIN);
		control.setValue(-20f);
		
		return clip;
	}
	
	// Hopefully this works with threads
	public synchronized void playChessMove() {
		int idx = index++;
		
		Clip clip = clips[idx % clips.length];
		clip.setFramePosition(0);
		clip.loop(0);
		clip.flush();
	}
}
