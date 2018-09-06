package com.miqtech.master.utils;

import com.google.common.base.Joiner;

public class SqlJoiner {

	private SqlJoiner() {
		super();
	}

	private final static Joiner JOINER = Joiner.on("");
	private final static Joiner JOINER_WITHOUT_SPACE = Joiner.on("");

	public final static String join(String... strs) {
		return JOINER.join(strs);
	}

	public final static String joinWithoutSpace(Object... objects) {
		return JOINER_WITHOUT_SPACE.join(objects);
	}
}
