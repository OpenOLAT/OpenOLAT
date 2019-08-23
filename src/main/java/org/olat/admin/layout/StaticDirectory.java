package org.olat.admin.layout;

import org.springframework.stereotype.Component;

/**
 * Initial date: 2017-04-13<br />
 * @author sev26 (UZH)
 */
@Component
public class StaticDirectory {

	private final String name;

	public StaticDirectory() {
		this.name = "static";
	}

	protected StaticDirectory(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
