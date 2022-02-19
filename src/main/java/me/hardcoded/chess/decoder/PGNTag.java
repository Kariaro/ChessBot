package me.hardcoded.chess.decoder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PGNTag {
	// Seven Tag Roster
	Event ("?"),
	Site  ("?"),
	Date  ("????.??.??"),
	Round ("?"),
	White ("?"),
	Black ("?"),
	Result("*"),
	
	// Optional
	FEN;
	
	public static final PGNTag[] REQUIRED = { Event, Site, Date, Round, White, Black, Result };
	
	private static final Map<String, PGNTag> VALUES = Arrays.stream(PGNTag.values()).collect(Collectors.toMap(PGNTag::name, v -> v));
	private final boolean required;
	private final String defaultValue;
	
	PGNTag(boolean required, String defaultValue) {
		this.required = required;
		this.defaultValue = defaultValue;
	}
	
	PGNTag(String defaultValue) {
		this(true, defaultValue);
	}
	
	PGNTag() {
		this(false, null);
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public static boolean hasTag(String name) {
		return VALUES.containsKey(name);
	}
	
	public static PGNTag getTag(String name) {
		return VALUES.get(name);
	}
}
