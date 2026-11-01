/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.color;

/**
 * WCAG 2 contrast ratio of two colours, the same formula as
 * <code>o-on-color()</code> in <code>themes/light/_functions.scss</code>.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorContrast {

	private ColorContrast() {
		//
	}

	/**
	 * @param hex A colour as "#RRGGBB"
	 * @return The WCAG 2 relative luminance, 0 (black) to 1 (white)
	 */
	public static double relativeLuminance(String hex) {
		if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
			throw new IllegalArgumentException("Not a colour #RRGGBB: " + hex);
		}
		int rgb = Integer.parseInt(hex.substring(1), 16);
		return 0.2126 * channel((rgb >> 16) & 0xFF)
				+ 0.7152 * channel((rgb >> 8) & 0xFF)
				+ 0.0722 * channel(rgb & 0xFF);
	}

	/**
	 * @return The WCAG 2 contrast ratio of the two colours, 1 to 21
	 */
	public static double contrast(String hex1, String hex2) {
		double l1 = relativeLuminance(hex1);
		double l2 = relativeLuminance(hex2);
		return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
	}

	private static double channel(int value) {
		double c = value / 255d;
		return c < 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}
}
