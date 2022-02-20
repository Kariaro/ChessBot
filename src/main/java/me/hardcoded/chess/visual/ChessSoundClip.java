package me.hardcoded.chess.visual;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;

/**
 * This class provides functions to play multiple sounds at the same time.
 *
 * @author HardCoded
 */
public class ChessSoundClip {
	/**
	 * An array of clips that can be played independently.
	 * The value {@code 16} is an upper bound of how many clips can be played at the same time.
	 */
	private final Clip[] clips = new Clip[16];
	
	/**
	 * This field contains the current index of the clip that is played.
	 */
	private int index;
	
	public ChessSoundClip(String name) throws Exception {
		for (int i = 0; i < clips.length; i++) {
			clips[i] = createClip(name);
		}
		
		setVolume(-20F);
	}
	
	private Clip createClip(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		InputStream is = ChessSoundClip.class.getResourceAsStream(path);
		AudioInputStream stream = AudioSystem.getAudioInputStream(is);
		Clip clip = AudioSystem.getClip();
		clip.open(stream);
		return clip;
	}
	
	public synchronized void playChessMove() {
		int idx = index++;
		
		Clip clip = clips[idx % clips.length];
		clip.setFramePosition(0);
		clip.loop(0);
		clip.flush();
	}
	
	public synchronized void setVolume(float volume) {
		for (Clip clip : clips) {
			FloatControl control = (FloatControl)clip.getControl(Type.MASTER_GAIN);
			control.setValue(-20f);
		}
	}
}
