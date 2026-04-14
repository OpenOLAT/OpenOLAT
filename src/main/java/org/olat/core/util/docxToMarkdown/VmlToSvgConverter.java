/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.docxToMarkdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Converts VML (Vector Markup Language) shape data to SVG.
 * Handles simple shapes with path data, fill, and stroke.
 * VML path commands are similar to SVG but use a different coordinate system.
 *
 * @author gnaegi, https://www.frentix.com
 */
class VmlToSvgConverter {

	private static final Logger log = Tracing.createLoggerFor(VmlToSvgConverter.class);

	private VmlToSvgConverter() {
		// utility
	}

	/**
	 * Convert a VML shape to an SVG string.
	 *
	 * @param path VML path data (from v:shape path attribute)
	 * @param style VML style string (contains width/height in pt)
	 * @param strokeColor stroke color (e.g., "#030e13")
	 * @param fillColor fill color (e.g., "#156082")
	 * @param filled whether the shape is filled (null = default/yes)
	 * @return SVG string, or null if conversion fails
	 */
	static String convert(String path, String style, String strokeColor, String fillColor, String filled) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		try {
			// Parse dimensions from style
			double widthPt = extractDimension(style, "width");
			double heightPt = extractDimension(style, "height");
			if (widthPt <= 0) widthPt = 100;
			if (heightPt <= 0) heightPt = 100;

			// VML uses EMU-like coordinates; the coordsize is typically 21600x21600
			// but freehand shapes use absolute coordinates. We use viewBox to scale.
			String svgPath = vmlPathToSvg(path);
			if (svgPath == null || svgPath.isEmpty()) {
				return null;
			}

			// Determine fill/stroke
			boolean isFilled = !"f".equals(filled) && fillColor != null;
			String fill = isFilled ? cleanColor(fillColor) : "none";
			String stroke = strokeColor != null ? cleanColor(strokeColor) : "#000000";

			// Build SVG
			int svgWidth = (int) Math.ceil(widthPt * 1.333); // pt to px approx
			int svgHeight = (int) Math.ceil(heightPt * 1.333);

			return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + svgWidth
				+ "\" height=\"" + svgHeight + "\" viewBox=\"0 0 " + svgWidth + " " + svgHeight + "\">"
				+ "<path d=\"" + svgPath + "\" fill=\"" + fill + "\" stroke=\"" + stroke
				+ "\" stroke-width=\"1\"/></svg>";
		} catch (Exception e) {
			log.debug("VML to SVG conversion failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Convert VML path commands to SVG path commands.
	 * VML uses: m (moveTo), l (lineTo), c (curveTo), v (relative curveTo),
	 * r (relative lineTo), x (close), e (end), nf/ns (new fill/stroke)
	 * Most are compatible with SVG except the coordinate scaling.
	 */
	static String vmlPathToSvg(String vmlPath) {
		if (vmlPath == null) return null;

		StringBuilder svg = new StringBuilder();
		// VML coordinates can be very large (EMU-like). We need to scale them.
		// Find the max coordinate to determine scale factor.
		String cleaned = vmlPath.replaceAll("[mlcvrhaqstzeMCVRHAQSTZnfse,x]", " ").trim();
		String[] nums = cleaned.split("\\s+");
		double maxCoord = 1;
		for (String n : nums) {
			try {
				double v = Math.abs(Double.parseDouble(n));
				if (v > maxCoord) maxCoord = v;
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		// Scale factor: map max coordinate to ~100 units
		double scale = (maxCoord > 1000) ? 100.0 / maxCoord : 1.0;

		// Process VML path character by character
		StringBuilder token = new StringBuilder();
		for (int i = 0; i < vmlPath.length(); i++) {
			char ch = vmlPath.charAt(i);
			if (ch == 'x') {
				svg.append('Z'); // close path
			} else if (ch == 'e') {
				// end - ignore
			} else if (ch == 'n' && i + 1 < vmlPath.length() && (vmlPath.charAt(i+1) == 'f' || vmlPath.charAt(i+1) == 's')) {
				i++; // skip nf/ns
			} else if (ch == 'm' || ch == 'M') {
				svg.append('M');
			} else if (ch == 'l' || ch == 'L') {
				svg.append('L');
			} else if (ch == 'c' || ch == 'C') {
				svg.append('C');
			} else if (ch == 'v') {
				svg.append('c'); // relative curveto
			} else if (ch == 'r') {
				svg.append('l'); // relative lineto
			} else if (ch == ',' || ch == ' ') {
				if (token.length() > 0) {
					try {
						double val = Double.parseDouble(token.toString()) * scale;
						svg.append(String.format("%.1f", val));
					} catch (NumberFormatException e) {
						svg.append(token);
					}
					token.setLength(0);
				}
				svg.append(' ');
			} else if (ch == '-' && token.length() > 0) {
				// Negative number starts - flush previous
				try {
					double val = Double.parseDouble(token.toString()) * scale;
					svg.append(String.format("%.1f", val));
				} catch (NumberFormatException e) {
					svg.append(token);
				}
				token.setLength(0);
				token.append(ch);
				svg.append(' ');
			} else {
				token.append(ch);
			}
		}
		// Flush last token
		if (token.length() > 0) {
			try {
				double val = Double.parseDouble(token.toString()) * scale;
				svg.append(String.format("%.1f", val));
			} catch (NumberFormatException e) {
				svg.append(token);
			}
		}

		return svg.toString().trim();
	}

	private static double extractDimension(String style, String property) {
		if (style == null) return 0;
		Pattern p = Pattern.compile(property + ":([\\d.]+)pt");
		Matcher m = p.matcher(style);
		if (m.find()) {
			try {
				return Double.parseDouble(m.group(1));
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	/** Remove VML color index suffix like " [484]" */
	private static String cleanColor(String color) {
		if (color == null) return "#000000";
		int bracket = color.indexOf(' ');
		return bracket > 0 ? color.substring(0, bracket) : color;
	}
}
