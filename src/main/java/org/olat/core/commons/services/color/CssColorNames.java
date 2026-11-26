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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The named colours of CSS (the 147 names of CSS Color Level 3 and
 * "rebeccapurple" of Level 4) and the nearest named colour of a colour.
 * The distance is measured in the perceptual colour space OKLab.
 * <p>
 * Used where a format accepts only a colour name, e.g. the property
 * <code>COLOR</code> of iCalendar (RFC 7986).
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class CssColorNames {

	/** Name to colour "#RRGGBB", in alphabetical order */
	private static final Map<String,String> NAMED_COLORS = Collections.unmodifiableMap(namedColors(
		"aliceblue", "#F0F8FF", "antiquewhite", "#FAEBD7", "aqua", "#00FFFF", "aquamarine", "#7FFFD4",
		"azure", "#F0FFFF", "beige", "#F5F5DC", "bisque", "#FFE4C4", "black", "#000000",
		"blanchedalmond", "#FFEBCD", "blue", "#0000FF", "blueviolet", "#8A2BE2", "brown", "#A52A2A",
		"burlywood", "#DEB887", "cadetblue", "#5F9EA0", "chartreuse", "#7FFF00", "chocolate", "#D2691E",
		"coral", "#FF7F50", "cornflowerblue", "#6495ED", "cornsilk", "#FFF8DC", "crimson", "#DC143C",
		"cyan", "#00FFFF", "darkblue", "#00008B", "darkcyan", "#008B8B", "darkgoldenrod", "#B8860B",
		"darkgray", "#A9A9A9", "darkgreen", "#006400", "darkgrey", "#A9A9A9", "darkkhaki", "#BDB76B",
		"darkmagenta", "#8B008B", "darkolivegreen", "#556B2F", "darkorange", "#FF8C00", "darkorchid", "#9932CC",
		"darkred", "#8B0000", "darksalmon", "#E9967A", "darkseagreen", "#8FBC8F", "darkslateblue", "#483D8B",
		"darkslategray", "#2F4F4F", "darkslategrey", "#2F4F4F", "darkturquoise", "#00CED1",
		"darkviolet", "#9400D3", "deeppink", "#FF1493", "deepskyblue", "#00BFFF", "dimgray", "#696969",
		"dimgrey", "#696969", "dodgerblue", "#1E90FF", "firebrick", "#B22222", "floralwhite", "#FFFAF0",
		"forestgreen", "#228B22", "fuchsia", "#FF00FF", "gainsboro", "#DCDCDC", "ghostwhite", "#F8F8FF",
		"gold", "#FFD700", "goldenrod", "#DAA520", "gray", "#808080", "green", "#008000", "greenyellow", "#ADFF2F",
		"grey", "#808080", "honeydew", "#F0FFF0", "hotpink", "#FF69B4", "indianred", "#CD5C5C",
		"indigo", "#4B0082", "ivory", "#FFFFF0", "khaki", "#F0E68C", "lavender", "#E6E6FA",
		"lavenderblush", "#FFF0F5", "lawngreen", "#7CFC00", "lemonchiffon", "#FFFACD", "lightblue", "#ADD8E6",
		"lightcoral", "#F08080", "lightcyan", "#E0FFFF", "lightgoldenrodyellow", "#FAFAD2", "lightgray", "#D3D3D3",
		"lightgreen", "#90EE90", "lightgrey", "#D3D3D3", "lightpink", "#FFB6C1", "lightsalmon", "#FFA07A",
		"lightseagreen", "#20B2AA", "lightskyblue", "#87CEFA", "lightslategray", "#778899",
		"lightslategrey", "#778899", "lightsteelblue", "#B0C4DE", "lightyellow", "#FFFFE0", "lime", "#00FF00",
		"limegreen", "#32CD32", "linen", "#FAF0E6", "magenta", "#FF00FF", "maroon", "#800000",
		"mediumaquamarine", "#66CDAA", "mediumblue", "#0000CD", "mediumorchid", "#BA55D3",
		"mediumpurple", "#9370DB", "mediumseagreen", "#3CB371", "mediumslateblue", "#7B68EE",
		"mediumspringgreen", "#00FA9A", "mediumturquoise", "#48D1CC", "mediumvioletred", "#C71585",
		"midnightblue", "#191970", "mintcream", "#F5FFFA", "mistyrose", "#FFE4E1", "moccasin", "#FFE4B5",
		"navajowhite", "#FFDEAD", "navy", "#000080", "oldlace", "#FDF5E6", "olive", "#808000",
		"olivedrab", "#6B8E23", "orange", "#FFA500", "orangered", "#FF4500", "orchid", "#DA70D6",
		"palegoldenrod", "#EEE8AA", "palegreen", "#98FB98", "paleturquoise", "#AFEEEE", "palevioletred", "#DB7093",
		"papayawhip", "#FFEFD5", "peachpuff", "#FFDAB9", "peru", "#CD853F", "pink", "#FFC0CB", "plum", "#DDA0DD",
		"powderblue", "#B0E0E6", "purple", "#800080", "rebeccapurple", "#663399", "red", "#FF0000",
		"rosybrown", "#BC8F8F", "royalblue", "#4169E1", "saddlebrown", "#8B4513", "salmon", "#FA8072",
		"sandybrown", "#F4A460", "seagreen", "#2E8B57", "seashell", "#FFF5EE", "sienna", "#A0522D",
		"silver", "#C0C0C0", "skyblue", "#87CEEB", "slateblue", "#6A5ACD", "slategray", "#708090",
		"slategrey", "#708090", "snow", "#FFFAFA", "springgreen", "#00FF7F", "steelblue", "#4682B4",
		"tan", "#D2B48C", "teal", "#008080", "thistle", "#D8BFD8", "tomato", "#FF6347", "turquoise", "#40E0D0",
		"violet", "#EE82EE", "wheat", "#F5DEB3", "white", "#FFFFFF", "whitesmoke", "#F5F5F5", "yellow", "#FFFF00",
		"yellowgreen", "#9ACD32"
	));

	/** Name to the OKLab coordinates of its colour */
	private static final Map<String,double[]> NAMED_COLORS_OKLAB = Collections.unmodifiableMap(oklabOfNamedColors());

	private CssColorNames() {
		//
	}

	/**
	 * @return The named colours, name to "#RRGGBB", in alphabetical order
	 */
	public static Map<String,String> getNamedColors() {
		return NAMED_COLORS;
	}

	/**
	 * @param name A colour name, e.g. "blue". The case is ignored.
	 * @return true if the name is a named colour of CSS
	 */
	public static boolean isColorName(String name) {
		return name != null && NAMED_COLORS.containsKey(name.trim().toLowerCase(Locale.ROOT));
	}

	/**
	 * @param hex A colour as "#RRGGBB"
	 * @return The named colour with the smallest OKLab distance to the colour.
	 * 		Of two names with the same colour (e.g. "gray" and "grey"), the name
	 * 		first in alphabetical order. Null if the value is not "#RRGGBB".
	 */
	public static String nearestColorName(String hex) {
		if (!isHexColor(hex)) {
			return null;
		}
		
		double[] lab = toOklab(hex);
		String nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Map.Entry<String,double[]> namedColor : NAMED_COLORS_OKLAB.entrySet()) {
			double distance = distance(lab, namedColor.getValue());
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = namedColor.getKey();
			}
		}
		return nearest;
	}

	/**
	 * @return true if the value is a colour "#RRGGBB"
	 */
	public static boolean isHexColor(String hex) {
		return hex != null && hex.matches("#[0-9A-Fa-f]{6}");
	}

	/**
	 * Converts a sRGB colour to OKLab.
	 * 
	 * @param hex A colour as "#RRGGBB"
	 * @return The coordinates L, a, b
	 */
	public static double[] toOklab(String hex) {
		if (!isHexColor(hex)) {
			throw new IllegalArgumentException("Not a colour #RRGGBB: " + hex);
		}
		int rgb = Integer.parseInt(hex.substring(1), 16);
		double r = linear((rgb >> 16) & 0xFF);
		double g = linear((rgb >> 8) & 0xFF);
		double b = linear(rgb & 0xFF);
		
		double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
		double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
		double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
		return new double[] {
			0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
			1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
			0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
		};
	}

	/**
	 * @return The euclidean distance of two OKLab colours
	 */
	public static double distance(double[] lab1, double[] lab2) {
		double dl = lab1[0] - lab2[0];
		double da = lab1[1] - lab2[1];
		double db = lab1[2] - lab2[2];
		return Math.sqrt(dl * dl + da * da + db * db);
	}

	private static double linear(int value) {
		double c = value / 255d;
		return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	private static Map<String,double[]> oklabOfNamedColors() {
		Map<String,double[]> oklab = new LinkedHashMap<>();
		for (Map.Entry<String,String> namedColor : NAMED_COLORS.entrySet()) {
			oklab.put(namedColor.getKey(), toOklab(namedColor.getValue()));
		}
		return oklab;
	}

	private static Map<String,String> namedColors(String... nameHex) {
		Map<String,String> colors = new LinkedHashMap<>();
		for (int i = 0; i < nameHex.length; i += 2) {
			colors.put(nameHex[i], nameHex[i + 1]);
		}
		return colors;
	}
}
