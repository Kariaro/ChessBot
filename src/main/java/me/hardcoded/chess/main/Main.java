package me.hardcoded.chess.main;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import me.hardcoded.chess.gui.ChessWindow;

public class Main {
	public static void main(String[] args) {
		try {
			ChessWindow window = new ChessWindow();
			window.start();
		} catch(Throwable t) {
			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));
			String error = writer.getBuffer().toString();
			JOptionPane.showMessageDialog(null, error);
		}
	}
}
