package me.hardcoded.chess.pgn;

import me.hardcoded.chess.decoder.ChessCodec;
import me.hardcoded.chess.decoder.PGNGame;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * This class validates that the PGN reader works as intended.
 *
 * @author HardCoded
 */
public class PGNValidator {
	private static final String DATABASE = "lichess_db_standard_rated_2013-01.gz";
	private static final int COUNT = 121332;
	
	private GZIPInputStream stream;
	
	@Before
	public void initialize() throws IOException {
		InputStream resourceStream = PGNValidator.class.getResourceAsStream("/pgn/" + DATABASE);
		if (resourceStream == null) {
			throw new RuntimeException("Could not find the database file '%s'".formatted(DATABASE));
		}
		
		this.stream = new GZIPInputStream(resourceStream);
	}
	
	private String readPGN() {
		try {
			if (stream.available() == 0) {
				return null;
			}
			
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			
			int prev = 0;
			int curr;
			
			int lineFeeds = 0;
			while (lineFeeds < 2 && (curr = stream.read()) != -1) {
				if (curr == '\n' && prev == '\n') {
					lineFeeds++;
				}
				
				prev = curr;
				bs.write(curr);
			}
			
			String result = bs.toString(StandardCharsets.ISO_8859_1).trim();
			bs.reset();
			bs.close();
			
			return result;
		} catch (IOException e) {
			return null;
		}
	}
	
	@Test
	public void testPGN() {
		int valid = 0;
		String pgn;
		while ((pgn = readPGN()) != null) {
			PGNGame game;
			try {
				game = ChessCodec.PGN.from(pgn);
				valid ++;
			} catch (Throwable t) {
				System.out.println(pgn);
				throw t;
			}
			
			if ((valid % 1000) == 0) {
				System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + valid + "/" + COUNT);
				System.out.flush();
			}
		}
	}
}
