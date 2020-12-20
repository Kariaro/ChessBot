package hardcoded.chess.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;

public class ChessAudio {
	private AudioInputStream stream;
	private Clip clip;
	
	public ChessAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		InputStream is = ChessAudio.class.getResourceAsStream("/chess_move.wav");
		stream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
		clip = AudioSystem.getClip();
		
		clip.open(stream);
		
		FloatControl control = (FloatControl)clip.getControl(Type.MASTER_GAIN);
		control.setValue(-20f);
	}
	
	public void playChessMove() {
		clip.setFramePosition(0);
		clip.loop(0);
		clip.flush();
	}
}
