package org.olat.core.util.filter.impl;

import org.olat.core.util.filter.Filter;

public class XMLValidCharacterFilter implements Filter {

	@Override
	public String filter(String in) {
		StringBuilder out = new StringBuilder(); // Used to hold the output.
		int codePoint; // Used to reference the current character.

		int i = 0;
		while (i < in.length()) {
			codePoint = in.codePointAt(i); // This is the unicode code of the
										   // character.

			if ((codePoint == 0x9) // Consider testing larger ranges first to improve speed.
					|| (codePoint == 0xA) || (codePoint == 0xD)
					|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
					|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
					|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
				out.append(Character.toChars(codePoint));
			}
			i += Character.charCount(codePoint); // Increment with the number of
												 // code units(java chars)
												 // needed to represent a
												 // Unicode char.
		}
		return out.toString();
	}
}
