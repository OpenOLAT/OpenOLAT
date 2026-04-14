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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Renders SmartArt diagrams (from word/diagrams/drawing1.xml) to SVG files.
 * SmartArt in OOXML consists of shapes with preset geometries, positions,
 * fills, strokes, and text. This renderer converts them to a single SVG image.
 * Supports group transforms, gradient fills, color transforms, custom geometry
 * paths, rotation/flip, and rich text runs.
 *
 * @author gnaegi, https://www.frentix.com
 */
class SmartArtRenderer {

	private static final Logger log = Tracing.createLoggerFor(SmartArtRenderer.class);

	private SmartArtRenderer() {
		// utility
	}

	// --- Public API ---

	/**
	 * Render a SmartArt diagram to an SVG file.
	 *
	 * @param zipFile     the DOCX ZIP file (open)
	 * @param drawingRel  the relationship target for the diagram drawing (e.g., "diagrams/drawing1.xml")
	 * @param mediaDir    directory to write the SVG file to
	 * @param widthEmu    display width from wp:extent cx (EMU)
	 * @param heightEmu   display height from wp:extent cy (EMU)
	 * @param themeColors map of OOXML scheme color names to hex RGB strings (may be null)
	 * @return the filename of the generated SVG (relative to media/), or null if rendering fails
	 */
	static String render(ZipFile zipFile, String drawingRel, File mediaDir,
			int widthEmu, int heightEmu, Map<String, String> themeColors) {
		if (zipFile == null || drawingRel == null || mediaDir == null) {
			return null;
		}

		String entryPath = drawingRel.startsWith("word/") ? drawingRel : "word/" + drawingRel;
		ZipEntry entry = zipFile.getEntry(entryPath);
		if (entry == null) {
			log.debug("SmartArt drawing not found: {}", entryPath);
			return null;
		}

		try {
			byte[] xml;
			try (var is = zipFile.getInputStream(entry)) {
				xml = is.readAllBytes();
			}

			SmartArtShapeCollector collector = new SmartArtShapeCollector();
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(xml), collector);

			List<ShapeInfo> shapes = collector.getShapes();
			if (shapes.isEmpty()) {
				return null;
			}

			// Log shape details for diagnosing rendering issues
			if (log.isDebugEnabled()) {
				for (ShapeInfo s : shapes) {
					log.debug("SmartArt shape: preset={}, customPath={}, pos=({},{}) size={}x{}",
						s.presetGeom, s.customPath != null ? "yes(" + s.customPath.length() + " chars)" : "no",
						s.x, s.y, s.cx, s.cy);
				}
			}

			String svg = buildSvg(shapes, widthEmu, heightEmu, themeColors);
			String filename = "smartart_" + System.nanoTime() + ".svg";
			File svgFile = new File(mediaDir, filename);
			Files.writeString(svgFile.toPath(), svg, StandardCharsets.UTF_8);
			return filename;
		} catch (Exception e) {
			log.debug("SmartArt rendering failed: {}", e.getMessage());
			return null;
		}
	}

	// --- Inner data classes ---

	static class TextRun {
		String text = "";
		boolean bold;
		boolean italic;
		int fontSize;   // 100ths of a point, 0 = use default
		String colorHex; // null = use default
	}

	static class GradientStop {
		int position;    // 0-100000
		String colorHex; // resolved hex color
		float alpha = 1.0f;
	}

	static class ShapeInfo {
		// Geometry
		String presetGeom = "rect";
		String customPath;
		int customPathW, customPathH;
		Map<String, Integer> adjustments = new HashMap<>();

		// Position & size (EMU)
		int x, y, cx, cy;
		int rotation;
		boolean flipH, flipV;

		// Clip bounds from parent group (EMU, 0=no clip)
		int clipX, clipY, clipCx, clipCy;

		// Fill
		String fillScheme;
		String fillColorHex;
		boolean noFill;
		int fillTint = -1;
		int fillShade = -1;
		int fillLumMod = -1;
		int fillLumOff = -1;
		float fillAlpha = 1.0f;
		List<GradientStop> gradientStops;
		int gradientAngle;
		boolean radialGradient;

		// Stroke
		boolean noStroke;
		int strokeWidth;
		String strokeScheme;
		String strokeColorHex;
		String dashStyle;
		String headEndType;
		String tailEndType;

		// Text
		List<TextRun> textRuns = new ArrayList<>();
		String textAlign;
		String textAnchor;
		int lIns, tIns, rIns, bIns;
		int txX, txY, txCx, txCy;

		String getText() {
			if (textRuns.isEmpty()) return null;
			StringBuilder sb = new StringBuilder();
			for (TextRun tr : textRuns) {
				if (sb.length() > 0 && !tr.text.isEmpty()) sb.append(' ');
				sb.append(tr.text);
			}
			String result = sb.toString().trim();
			return result.isEmpty() ? null : result;
		}
	}

	static class GroupTransform {
		int offX, offY;
		int extCx, extCy;
		int chOffX, chOffY;
		int chExtCx, chExtCy;
	}

	// --- SVG builder ---

	private static String buildSvg(List<ShapeInfo> shapes, int widthEmu, int heightEmu,
			Map<String, String> themeColors) {
		int pxW = Math.max(20, widthEmu / 9525);
		int pxH = Math.max(20, heightEmu / 9525);

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = 0, maxY = 0;
		int maxStrokeEmu = 0;
		for (ShapeInfo s : shapes) {
			minX = Math.min(minX, s.x);
			minY = Math.min(minY, s.y);
			maxX = Math.max(maxX, s.x + s.cx);
			maxY = Math.max(maxY, s.y + s.cy);
			if (!s.noStroke) {
				maxStrokeEmu = Math.max(maxStrokeEmu, s.strokeWidth > 0 ? s.strokeWidth : 12700);
			}
		}
		if (minX == Integer.MAX_VALUE) { minX = 0; minY = 0; }
		// Pad viewBox by half the max stroke width so strokes at edges aren't clipped
		int pad = maxStrokeEmu / 2;
		minX -= pad; minY -= pad;
		maxX += pad; maxY += pad;
		int viewW = Math.max(1, maxX - minX);
		int viewH = Math.max(1, maxY - minY);

		StringBuilder defs = new StringBuilder();
		StringBuilder body = new StringBuilder();

		// Arrow marker defs
		defs.append("<marker id=\"arrow_triangle\" markerWidth=\"10\" markerHeight=\"7\" refX=\"10\" refY=\"3.5\" orient=\"auto\">");
		defs.append("<polygon points=\"0 0, 10 3.5, 0 7\" fill=\"#555\"/></marker>");
		defs.append("<marker id=\"arrow_stealth\" markerWidth=\"10\" markerHeight=\"7\" refX=\"10\" refY=\"3.5\" orient=\"auto\">");
		defs.append("<polygon points=\"0 0, 10 3.5, 0 7, 3 3.5\" fill=\"#555\"/></marker>");
		defs.append("<marker id=\"arrow_start_triangle\" markerWidth=\"10\" markerHeight=\"7\" refX=\"0\" refY=\"3.5\" orient=\"auto-start-reverse\">");
		defs.append("<polygon points=\"0 0, 10 3.5, 0 7\" fill=\"#555\"/></marker>");

		int gradIndex = 0;
		int clipIndex = 0;
		for (ShapeInfo s : shapes) {
			// Build gradient def if needed
			String gradId = null;
			if (s.gradientStops != null && !s.gradientStops.isEmpty()) {
				gradId = "grad_" + gradIndex++;
				defs.append(buildGradientDef(gradId, s));
			}

			// Build clip-path def if shape extends beyond its group bounds
			String clipId = null;
			if (s.clipCx > 0 && s.clipCy > 0) {
				// Check if shape actually needs clipping (extends beyond clip bounds)
				boolean needsClip = s.x < s.clipX || s.y < s.clipY
					|| s.x + s.cx > s.clipX + s.clipCx
					|| s.y + s.cy > s.clipY + s.clipCy;
				if (needsClip) {
					clipId = "clip_" + clipIndex++;
					defs.append("<clipPath id=\"").append(clipId).append("\">");
					defs.append("<rect x=\"").append(s.clipX).append("\" y=\"").append(s.clipY)
						.append("\" width=\"").append(s.clipCx).append("\" height=\"").append(s.clipCy).append("\"/>");
					defs.append("</clipPath>");
				}
			}

			// Determine fill color
			String fillColor;
			if (s.noFill) {
				fillColor = "none";
			} else if (gradId != null) {
				fillColor = "url(#" + gradId + ")";
			} else {
				String baseColor = resolveColor(s.fillScheme, s.fillColorHex, themeColors, "#4472C4");
				baseColor = applyColorTransforms(baseColor, s.fillTint, s.fillShade, s.fillLumMod, s.fillLumOff);
				fillColor = baseColor;
			}

			// Fill opacity
			float fillOpacity = s.fillAlpha;

			// Stroke
			String strokeColor;
			int sw;
			if (s.noStroke) {
				strokeColor = "none";
				sw = 0;
			} else {
				strokeColor = resolveColor(s.strokeScheme, s.strokeColorHex, themeColors, "#2F528F");
				// strokeWidth is in EMU; keep it in EMU since viewBox is in EMU.
				// Default to 12700 EMU (= 1 pt) when not explicitly set.
				sw = s.strokeWidth > 0 ? s.strokeWidth : 12700;
			}

			// Outer wrapper: clip-path (if needed)
			if (clipId != null) {
				body.append("<g clip-path=\"url(#").append(clipId).append(")\">");
			}

			// Transform wrapper
			boolean needsTransform = s.rotation != 0 || s.flipH || s.flipV;
			String transformAttr = null;
			if (needsTransform) {
				transformAttr = buildTransform(s);
			}

			if (needsTransform) {
				body.append("<g transform=\"").append(transformAttr).append("\">");
			}

			// Shape path or primitive
			String presetPath = PresetGeometryPath.getPath(s.presetGeom, s.cx, s.cy, s.adjustments);
			boolean useEvenOdd = PresetGeometryPath.isEvenOddShape(s.presetGeom);

			if (s.customPath != null && !s.customPath.isEmpty()) {
				// Custom geometry: scale to shape bounds
				double scaleX = s.customPathW > 0 ? (double) s.cx / s.customPathW : 1.0;
				double scaleY = s.customPathH > 0 ? (double) s.cy / s.customPathH : 1.0;
				body.append("<path d=\"").append(s.customPath).append("\"");
				body.append(" transform=\"translate(").append(s.x).append(",").append(s.y)
					.append(") scale(").append(fmt(scaleX)).append(",").append(fmt(scaleY)).append(")\"");
				appendShapeStyle(body, fillColor, fillOpacity, strokeColor, sw, s.dashStyle,
						s.headEndType, s.tailEndType, useEvenOdd);
				body.append("/>");
			} else if (presetPath != null) {
				body.append("<path d=\"").append(presetPath).append("\"");
				body.append(" transform=\"translate(").append(s.x).append(",").append(s.y).append(")\"");
				appendShapeStyle(body, fillColor, fillOpacity, strokeColor, sw, s.dashStyle,
						s.headEndType, s.tailEndType, useEvenOdd);
				body.append("/>");
			} else {
				// Fallback: rectangle
				body.append("<rect x=\"").append(s.x).append("\" y=\"").append(s.y)
					.append("\" width=\"").append(s.cx).append("\" height=\"").append(s.cy).append("\"");
				appendShapeStyle(body, fillColor, fillOpacity, strokeColor, sw, s.dashStyle,
						s.headEndType, s.tailEndType, false);
				body.append("/>");
			}

			// Text rendering
			String text = s.getText();
			if (text != null && !text.isBlank()) {
				renderText(body, s, themeColors, fillColor);
			}

			if (needsTransform) {
				body.append("</g>");
			}
			if (clipId != null) {
				body.append("</g>");
			}
		}

		StringBuilder svg = new StringBuilder();
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(pxW)
			.append("\" height=\"").append(pxH)
			.append("\" viewBox=\"").append(minX).append(' ').append(minY)
			.append(' ').append(viewW).append(' ').append(viewH).append("\">");
		svg.append("<defs>").append(defs).append("</defs>");
		svg.append(body);
		svg.append("</svg>");
		return svg.toString();
	}

	private static String buildGradientDef(String gradId, ShapeInfo s) {
		StringBuilder gb = new StringBuilder();
		if (s.radialGradient) {
			gb.append("<radialGradient id=\"").append(gradId)
				.append("\" cx=\"50%\" cy=\"50%\" r=\"50%\">");
		} else {
			double angleDeg = s.gradientAngle / 60000.0;
			double angleRad = Math.toRadians(angleDeg);
			String x1 = fmt(50 - 50 * Math.sin(angleRad));
			String y1 = fmt(50 - 50 * Math.cos(angleRad));
			String x2 = fmt(50 + 50 * Math.sin(angleRad));
			String y2 = fmt(50 + 50 * Math.cos(angleRad));
			gb.append("<linearGradient id=\"").append(gradId)
				.append("\" x1=\"").append(x1).append("%\" y1=\"").append(y1)
				.append("%\" x2=\"").append(x2).append("%\" y2=\"").append(y2).append("%\">");
		}
		for (GradientStop gs : s.gradientStops) {
			double offset = gs.position / 100000.0;
			gb.append("<stop offset=\"").append(fmt(offset * 100)).append("%\"");
			gb.append(" stop-color=\"#").append(gs.colorHex != null ? gs.colorHex : "000000").append("\"");
			if (gs.alpha < 1.0f) {
				gb.append(" stop-opacity=\"").append(fmt(gs.alpha)).append("\"");
			}
			gb.append("/>");
		}
		gb.append(s.radialGradient ? "</radialGradient>" : "</linearGradient>");
		return gb.toString();
	}

	private static void appendShapeStyle(StringBuilder sb, String fillColor, float fillOpacity,
			String strokeColor, int sw, String dashStyle, String headEndType, String tailEndType,
			boolean evenOdd) {
		sb.append(" fill=\"").append(fillColor).append("\"");
		if (fillOpacity < 1.0f) {
			sb.append(" fill-opacity=\"").append(fmt(fillOpacity)).append("\"");
		}
		if (evenOdd) {
			sb.append(" fill-rule=\"evenodd\"");
		}
		sb.append(" stroke=\"").append(strokeColor).append("\"");
		if (sw > 0 && !"none".equals(strokeColor)) {
			sb.append(" stroke-width=\"").append(sw).append("\"");
			String da = dashArray(dashStyle, sw);
			if (da != null) sb.append(" stroke-dasharray=\"").append(da).append("\"");
		}
		if (headEndType != null && !"none".equals(headEndType)) {
			sb.append(" marker-start=\"url(#arrow_start_triangle)\"");
		}
		if (tailEndType != null && !"none".equals(tailEndType)) {
			String markerId = "stealth".equals(tailEndType) ? "arrow_stealth" : "arrow_triangle";
			sb.append(" marker-end=\"url(#").append(markerId).append(")\"");
		}
	}

	private static String buildTransform(ShapeInfo s) {
		int centerX = s.x + s.cx / 2;
		int centerY = s.y + s.cy / 2;
		StringBuilder t = new StringBuilder();
		if (s.rotation != 0) {
			double degrees = s.rotation / 60000.0;
			t.append("rotate(").append(fmt(degrees)).append(",")
				.append(centerX).append(",").append(centerY).append(")");
		}
		if (s.flipH || s.flipV) {
			if (t.length() > 0) t.append(" ");
			double sx = s.flipH ? -1.0 : 1.0;
			double sy = s.flipV ? -1.0 : 1.0;
			t.append("translate(").append(centerX).append(",").append(centerY).append(")")
				.append(" scale(").append((int)sx).append(",").append((int)sy).append(")")
				.append(" translate(").append(-centerX).append(",").append(-centerY).append(")");
		}
		return t.toString();
	}

	private static void renderText(StringBuilder body, ShapeInfo s,
			Map<String, String> themeColors, String shapeFillColor) {
		// Determine text bounding box
		int textX, textY, textW, textH;
		if (s.txCx > 0) {
			textX = s.txX;
			textY = s.txY;
			textW = s.txCx;
			textH = s.txCy;
		} else {
			textX = s.x + s.lIns;
			textY = s.y + s.tIns;
			textW = s.cx - s.lIns - s.rIns;
			textH = s.cy - s.tIns - s.bIns;
		}

		// SVG text anchor from textAlign
		String anchor = switch (s.textAlign != null ? s.textAlign : "ctr") {
			case "l" -> "start";
			case "r" -> "end";
			default -> "middle";
		};

		// X position
		int svgTextX = switch (s.textAlign != null ? s.textAlign : "ctr") {
			case "l" -> textX;
			case "r" -> textX + textW;
			default -> textX + textW / 2;
		};

		// Dominant baseline from textAnchor
		String baseline = switch (s.textAnchor != null ? s.textAnchor : "ctr") {
			case "t" -> "text-before-edge";
			case "b" -> "text-after-edge";
			default -> "central";
		};

		// Y position
		int svgTextY = switch (s.textAnchor != null ? s.textAnchor : "ctr") {
			case "t" -> textY;
			case "b" -> textY + textH;
			default -> textY + textH / 2;
		};

		// Default font size from available space
		String allText = s.getText();
		int defaultFontSize = Math.min(textH / 3, textW / Math.max(1, allText != null ? allText.length() : 1));
		defaultFontSize = Math.max(defaultFontSize, textH / 6);
		defaultFontSize = Math.max(defaultFontSize, 50000); // minimum readable

		// Default text color
		boolean lightFill = "none".equals(shapeFillColor)
			|| "#FFFFFF".equalsIgnoreCase(shapeFillColor)
			|| "#FAFAFA".equalsIgnoreCase(shapeFillColor)
			|| s.noFill;
		String defaultTextColor = lightFill ? "#333333" : "white";

		body.append("<text x=\"").append(svgTextX).append("\" y=\"").append(svgTextY)
			.append("\" text-anchor=\"").append(anchor)
			.append("\" dominant-baseline=\"").append(baseline)
			.append("\" font-family=\"sans-serif\"");

		// Check if all runs share same simple formatting
		boolean allSame = s.textRuns.size() <= 1 ||
			s.textRuns.stream().allMatch(r -> r.fontSize == s.textRuns.get(0).fontSize
				&& r.bold == s.textRuns.get(0).bold
				&& r.italic == s.textRuns.get(0).italic
				&& (r.colorHex == null || r.colorHex.equals(s.textRuns.get(0).colorHex)));

		if (allSame && !s.textRuns.isEmpty()) {
			TextRun first = s.textRuns.get(0);
			int fs = first.fontSize > 0 ? first.fontSize * 12700 / 100 : defaultFontSize;
			body.append(" font-size=\"").append(fs).append("\"");
			if (first.bold) body.append(" font-weight=\"bold\"");
			if (first.italic) body.append(" font-style=\"italic\"");
			String tc = first.colorHex != null ? "#" + first.colorHex : defaultTextColor;
			body.append(" fill=\"").append(tc).append("\"");
			body.append(">");
			body.append(escapeXml(allText != null ? allText : ""));
		} else {
			body.append(" font-size=\"").append(defaultFontSize).append("\"");
			body.append(" fill=\"").append(defaultTextColor).append("\">");
			for (TextRun tr : s.textRuns) {
				if (tr.text == null || tr.text.isEmpty()) continue;
				body.append("<tspan");
				if (tr.fontSize > 0) {
					int fs = tr.fontSize * 12700 / 100;
					body.append(" font-size=\"").append(fs).append("\"");
				}
				if (tr.bold) body.append(" font-weight=\"bold\"");
				if (tr.italic) body.append(" font-style=\"italic\"");
				if (tr.colorHex != null) body.append(" fill=\"#").append(tr.colorHex).append("\"");
				body.append(">").append(escapeXml(tr.text)).append("</tspan>");
			}
		}
		body.append("</text>");
	}

	// --- Color helpers ---

	private static String resolveColor(String scheme, String explicitHex,
			Map<String, String> themeColors, String fallback) {
		if (explicitHex != null) return "#" + explicitHex;
		if (scheme != null && themeColors != null) {
			String hex = themeColors.get(scheme);
			if (hex != null) return "#" + hex;
		}
		if (scheme != null) {
			return switch (scheme) {
				case "lt1", "bg1" -> "#FFFFFF";
				case "dk1", "tx1" -> "#000000";
				case "lt2", "bg2" -> "#E7E6E6";
				case "dk2", "tx2" -> "#44546A";
				case "accent1" -> "#4472C4";
				case "accent2" -> "#ED7D31";
				case "accent3" -> "#A5A5A5";
				case "accent4" -> "#FFC000";
				case "accent5" -> "#5B9BD5";
				case "accent6" -> "#70AD47";
				case "hlink" -> "#0563C1";
				case "folHlink" -> "#954F72";
				default -> fallback;
			};
		}
		return fallback;
	}

	private static String applyColorTransforms(String hexColor, int tint, int shade,
			int lumMod, int lumOff) {
		if (hexColor == null || hexColor.length() < 7) return hexColor;
		int r = Integer.parseInt(hexColor.substring(1, 3), 16);
		int g = Integer.parseInt(hexColor.substring(3, 5), 16);
		int b = Integer.parseInt(hexColor.substring(5, 7), 16);

		if (tint >= 0) {
			double t = tint / 100000.0;
			r = (int)(r * t + 255 * (1 - t));
			g = (int)(g * t + 255 * (1 - t));
			b = (int)(b * t + 255 * (1 - t));
		}
		if (shade >= 0) {
			double sc = shade / 100000.0;
			r = (int)(r * sc);
			g = (int)(g * sc);
			b = (int)(b * sc);
		}
		if (lumMod >= 0 || lumOff >= 0) {
			float[] hsl = rgbToHsl(r, g, b);
			if (lumMod >= 0) hsl[2] *= lumMod / 100000f;
			if (lumOff >= 0) hsl[2] += lumOff / 100000f;
			hsl[2] = Math.max(0, Math.min(1, hsl[2]));
			int[] rgb = hslToRgb(hsl[0], hsl[1], hsl[2]);
			r = rgb[0]; g = rgb[1]; b = rgb[2];
		}
		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));
		return String.format("#%02X%02X%02X", r, g, b);
	}

	private static float[] rgbToHsl(int r, int g, int b) {
		float rf = r / 255f, gf = g / 255f, bf = b / 255f;
		float max = Math.max(rf, Math.max(gf, bf));
		float min = Math.min(rf, Math.min(gf, bf));
		float l = (max + min) / 2f;
		float h = 0, s = 0;
		if (max != min) {
			float d = max - min;
			s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
			if (max == rf) h = (gf - bf) / d + (gf < bf ? 6 : 0);
			else if (max == gf) h = (bf - rf) / d + 2;
			else h = (rf - gf) / d + 4;
			h /= 6f;
		}
		return new float[]{h, s, l};
	}

	private static int[] hslToRgb(float h, float s, float l) {
		float r, g, b;
		if (s == 0) {
			r = g = b = l;
		} else {
			float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
			float p = 2 * l - q;
			r = hueToRgb(p, q, h + 1f / 3);
			g = hueToRgb(p, q, h);
			b = hueToRgb(p, q, h - 1f / 3);
		}
		return new int[]{Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)};
	}

	private static float hueToRgb(float p, float q, float t) {
		if (t < 0) t += 1;
		if (t > 1) t -= 1;
		if (t < 1f / 6) return p + (q - p) * 6 * t;
		if (t < 1f / 2) return q;
		if (t < 2f / 3) return p + (q - p) * (2f / 3 - t) * 6;
		return p;
	}

	// --- Dash array ---

	private static String dashArray(String dashStyle, int strokeWidth) {
		if (dashStyle == null || "solid".equals(dashStyle)) return null;
		int sw = Math.max(1, strokeWidth);
		return switch (dashStyle) {
			case "dot" -> sw + " " + sw;
			case "dash" -> (3 * sw) + " " + (2 * sw);
			case "lgDash" -> (6 * sw) + " " + (2 * sw);
			case "dashDot" -> (3 * sw) + " " + sw + " " + sw + " " + sw;
			case "lgDashDot" -> (6 * sw) + " " + sw + " " + sw + " " + sw;
			case "sysDash" -> (2 * sw) + " " + sw;
			case "sysDot" -> sw + " " + sw;
			default -> null;
		};
	}


	// --- Formatting helpers ---

	private static String fmt(double value) {
		if (value == Math.floor(value) && !Double.isInfinite(value)) {
			return String.valueOf((long) value);
		}
		return String.format("%.2f", value);
	}

	private static String escapeXml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			.replace("\"", "&quot;").replace("'", "&apos;");
	}

	// --- SAX handler ---

	private static class SmartArtShapeCollector extends DefaultHandler {
		private final List<ShapeInfo> shapes = new ArrayList<>();
		private final Deque<GroupTransform> groupStack = new ArrayDeque<>();

		// Current shape state
		private ShapeInfo current;
		private boolean inShape;

		// Context flags
		private boolean inGroupSpPr;
		private boolean inLn;
		private boolean inTxBody;
		private boolean inTxXfrm;
		private boolean inParagraph;
		private boolean inRun;
		private boolean inRunProps;
		private boolean inText;
		private boolean inSolidFill;
		private boolean inGradFill;
		private boolean inGsLst;
		private boolean inGradientStop;
		private boolean inCustGeom;
		private boolean inPathLst;
		private boolean inCustPath;

		// Fill context tracking (where does the color go?)
		// "shape", "stroke", "text", "gradStop"
		private String fillContext = "shape";

		// Current text run
		private TextRun currentRun;
		private final StringBuilder textBuf = new StringBuilder();

		// Current gradient stop being parsed
		private GradientStop currentGradStop;

		// Custom geometry path building
		private int custPathW, custPathH;
		private final StringBuilder custPathBuf = new StringBuilder();
		private String currentPathCommand;
		private final List<int[]> pathPoints = new ArrayList<>();
		private int pathPointsNeeded;
		private int lastPathX, lastPathY; // track current point for arcTo

		List<ShapeInfo> getShapes() { return shapes; }

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {
			String name = stripPrefix(qName);
			switch (name) {
				case "sp" -> {
					current = new ShapeInfo();
					inShape = true;
					fillContext = "shape";
				}
				case "grpSp" -> {
					GroupTransform gt = new GroupTransform();
					groupStack.push(gt);
					inGroupSpPr = true;
				}
				case "grpSpPr" -> inGroupSpPr = true;
				case "prstGeom" -> {
					if (inShape && current != null) {
						String prst = attrs.getValue("prst");
						if (prst != null) current.presetGeom = prst;
					}
				}
				case "custGeom" -> {
					if (inShape && current != null) {
						inCustGeom = true;
						custPathBuf.setLength(0);
						custPathW = 0; custPathH = 0;
						lastPathX = 0; lastPathY = 0;
					}
				}
				case "pathLst" -> {
					if (inCustGeom) inPathLst = true;
				}
				case "path" -> {
					if (inPathLst) {
						// Custom geometry path element
						String ws = attrs.getValue("w");
						String hs = attrs.getValue("h");
						if (ws != null) custPathW = intAttr(attrs, "w");
						if (hs != null) custPathH = intAttr(attrs, "h");
						if (custPathW == 0) custPathW = current != null ? current.cx : 100000;
						if (custPathH == 0) custPathH = current != null ? current.cy : 100000;
						inCustPath = true;
					} else if (inGradFill) {
						// Radial gradient
						if (current != null) current.radialGradient = true;
					}
				}
				case "moveTo" -> {
					if (inCustPath) {
						currentPathCommand = "M";
						pathPoints.clear();
						pathPointsNeeded = 1;
					}
				}
				case "lnTo" -> {
					if (inCustPath) {
						currentPathCommand = "L";
						pathPoints.clear();
						pathPointsNeeded = 1;
					}
				}
				case "cubicBezTo" -> {
					if (inCustPath) {
						currentPathCommand = "C";
						pathPoints.clear();
						pathPointsNeeded = 3;
					}
				}
				case "quadBezTo" -> {
					if (inCustPath) {
						currentPathCommand = "Q";
						pathPoints.clear();
						pathPointsNeeded = 2;
					}
				}
				case "arcTo" -> {
					if (inCustPath) {
						int wR = intAttr(attrs, "wR");
						int hR = intAttr(attrs, "hR");
						int stAng = intAttr(attrs, "stAng");
						int swAng = intAttr(attrs, "swAng");
						appendArcTo(wR, hR, stAng, swAng);
					}
				}
				case "close" -> {
					if (inCustPath) custPathBuf.append(" Z");
				}
				case "pt" -> {
					if (inCustPath && currentPathCommand != null) {
						int px = intAttr(attrs, "x");
						int py = intAttr(attrs, "y");
						pathPoints.add(new int[]{px, py});
						if (pathPoints.size() >= pathPointsNeeded) {
							flushPathCommand();
						}
					}
				}
				case "xfrm" -> {
					if (inGroupSpPr && !groupStack.isEmpty()) {
						// Group transform - reads handled by off/ext/chOff/chExt
					} else if (inShape && current != null) {
						if (inTxXfrm) {
							// already set
						} else {
							String rot = attrs.getValue("rot");
							if (rot != null) try { current.rotation = Integer.parseInt(rot); } catch (NumberFormatException e) { /* */ }
							String flipH = attrs.getValue("flipH");
							if ("1".equals(flipH) || "true".equals(flipH)) current.flipH = true;
							String flipV = attrs.getValue("flipV");
							if ("1".equals(flipV) || "true".equals(flipV)) current.flipV = true;
						}
					}
				}
				case "txXfrm" -> inTxXfrm = true;
				case "off" -> {
					if (inShape && current != null) {
						if (inTxXfrm) {
							current.txX = intAttr(attrs, "x");
							current.txY = intAttr(attrs, "y");
						} else if (!inGroupSpPr) {
							current.x = intAttr(attrs, "x");
							current.y = intAttr(attrs, "y");
						}
					} else if (inGroupSpPr && !groupStack.isEmpty()) {
						GroupTransform gt = groupStack.peek();
						gt.offX = intAttr(attrs, "x");
						gt.offY = intAttr(attrs, "y");
					}
				}
				case "ext" -> {
					if (inShape && current != null) {
						if (inTxXfrm) {
							current.txCx = intAttr(attrs, "cx");
							current.txCy = intAttr(attrs, "cy");
						} else if (!inGroupSpPr) {
							current.cx = intAttr(attrs, "cx");
							current.cy = intAttr(attrs, "cy");
						}
					} else if (inGroupSpPr && !groupStack.isEmpty()) {
						GroupTransform gt = groupStack.peek();
						gt.extCx = intAttr(attrs, "cx");
						gt.extCy = intAttr(attrs, "cy");
					}
				}
				case "chOff" -> {
					if (inGroupSpPr && !groupStack.isEmpty()) {
						GroupTransform gt = groupStack.peek();
						gt.chOffX = intAttr(attrs, "x");
						gt.chOffY = intAttr(attrs, "y");
					}
				}
				case "chExt" -> {
					if (inGroupSpPr && !groupStack.isEmpty()) {
						GroupTransform gt = groupStack.peek();
						gt.chExtCx = intAttr(attrs, "cx");
						gt.chExtCy = intAttr(attrs, "cy");
					}
				}
				case "solidFill" -> {
					inSolidFill = true;
					// fillContext is already set based on where we are
				}
				case "gradFill" -> {
					if (inShape && current != null) {
						inGradFill = true;
						current.gradientStops = new ArrayList<>();
					}
				}
				case "gsLst" -> inGsLst = true;
				case "gs" -> {
					if (inGsLst && current != null) {
						currentGradStop = new GradientStop();
						currentGradStop.position = intAttr(attrs, "pos");
						inGradientStop = true;
						fillContext = "gradStop";
					}
				}
				case "lin" -> {
					if (inGradFill && current != null) {
						current.gradientAngle = intAttr(attrs, "ang");
					}
				}
				case "noFill" -> {
					if (inShape && current != null) {
						if (inLn) {
							current.noStroke = true;
						} else {
							current.noFill = true;
						}
					}
				}
				case "schemeClr" -> {
					if (inShape && current != null) {
						String val = attrs.getValue("val");
						if (val != null) {
							if (inGradientStop && currentGradStop != null) {
								// will be resolved when stop is finalized
								currentGradStop.colorHex = resolveSchemeColorHex(val, null);
							} else if (inLn || "stroke".equals(fillContext)) {
								if (current.strokeScheme == null) current.strokeScheme = val;
							} else if (inRunProps) {
								if (currentRun != null) {
									// store temporarily, resolve at end
									currentRun.colorHex = resolveSchemeColorHex(val, null);
								}
							} else if (inSolidFill && !inLn) {
								if (current.fillScheme == null && !current.noFill) {
									current.fillScheme = val;
								}
							}
						}
					}
				}
				case "srgbClr" -> {
					if (inShape && current != null) {
						String val = attrs.getValue("val");
						if (val != null) {
							if (inGradientStop && currentGradStop != null) {
								currentGradStop.colorHex = val;
							} else if (inLn || "stroke".equals(fillContext)) {
								if (current.strokeColorHex == null) current.strokeColorHex = val;
							} else if (inRunProps) {
								if (currentRun != null) currentRun.colorHex = val;
							} else if (inSolidFill && !inLn) {
								if (current.fillColorHex == null && !current.noFill) {
									current.fillColorHex = val;
								}
							}
						}
					}
				}
				case "tint" -> {
					if (inShape && current != null) {
						int val = intAttr(attrs, "val");
						if (inGradientStop && currentGradStop != null) {
							// apply tint to gradient stop color
						} else if (!inLn) current.fillTint = val;
					}
				}
				case "shade" -> {
					if (inShape && current != null) {
						int val = intAttr(attrs, "val");
						if (!inLn) current.fillShade = val;
					}
				}
				case "lumMod" -> {
					if (inShape && current != null) {
						int val = intAttr(attrs, "val");
						if (!inLn) current.fillLumMod = val;
					}
				}
				case "lumOff" -> {
					if (inShape && current != null) {
						int val = intAttr(attrs, "val");
						if (!inLn) current.fillLumOff = val;
					}
				}
				case "alpha" -> {
					if (inShape && current != null) {
						int val = intAttr(attrs, "val");
						float alpha = val / 100000f;
						if (inGradientStop && currentGradStop != null) {
							currentGradStop.alpha = alpha;
						} else {
							current.fillAlpha = alpha;
						}
					}
				}
				case "ln" -> {
					inLn = true;
					fillContext = "stroke";
					if (inShape && current != null) {
						String w = attrs.getValue("w");
						if (w != null) try { current.strokeWidth = Integer.parseInt(w); } catch (NumberFormatException e) { /* */ }
					}
				}
				case "prstDash" -> {
					if (inShape && current != null) {
						String val = attrs.getValue("val");
						if (val != null) current.dashStyle = val;
					}
				}
				case "headEnd" -> {
					if (inShape && current != null) {
						String type = attrs.getValue("type");
						if (type != null) current.headEndType = type;
					}
				}
				case "tailEnd" -> {
					if (inShape && current != null) {
						String type = attrs.getValue("type");
						if (type != null) current.tailEndType = type;
					}
				}
				case "txBody" -> inTxBody = true;
				case "bodyPr" -> {
					if (inShape && current != null && inTxBody) {
						String anchor = attrs.getValue("anchor");
						if (anchor != null) current.textAnchor = anchor;
						current.lIns = intAttrOrDefault(attrs, "lIns", 91440);
						current.tIns = intAttrOrDefault(attrs, "tIns", 45720);
						current.rIns = intAttrOrDefault(attrs, "rIns", 91440);
						current.bIns = intAttrOrDefault(attrs, "bIns", 45720);
					}
				}
				case "p" -> {
					if (inTxBody) inParagraph = true;
				}
				case "pPr" -> {
					if (inParagraph && current != null) {
						String algn = attrs.getValue("algn");
						if (algn != null) current.textAlign = algn;
					}
				}
				case "r" -> {
					if (inParagraph) {
						inRun = true;
						currentRun = new TextRun();
					}
				}
				case "rPr" -> {
					if (inRun && currentRun != null) {
						inRunProps = true;
						String sz = attrs.getValue("sz");
						if (sz != null) try { currentRun.fontSize = Integer.parseInt(sz); } catch (NumberFormatException e) { /* */ }
						String b = attrs.getValue("b");
						if ("1".equals(b) || "true".equals(b)) currentRun.bold = true;
						String i = attrs.getValue("i");
						if ("1".equals(i) || "true".equals(i)) currentRun.italic = true;
						fillContext = "text";
					}
				}
				case "gd" -> {
					if (inShape && current != null) {
						String gName = attrs.getValue("name");
						String fmla = attrs.getValue("fmla");
						if (gName != null && fmla != null && fmla.startsWith("val ")) {
							try {
								int v = Integer.parseInt(fmla.substring(4).trim());
								current.adjustments.put(gName, v);
							} catch (NumberFormatException e) { /* */ }
						}
					}
				}
				case "t" -> {
					if (inRun) {
						inText = true;
						textBuf.setLength(0);
					}
				}
				default -> { /* ignore */ }
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inText) textBuf.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String name = stripPrefix(qName);
			switch (name) {
				case "sp" -> {
					if (current != null && current.cx > 0 && current.cy > 0) {
						applyGroupTransforms(current);
						shapes.add(current);
					}
					current = null;
					inShape = false;
					inCustGeom = false;
					inPathLst = false;
					inCustPath = false;
					fillContext = "shape";
				}
				case "grpSp" -> {
					if (!groupStack.isEmpty()) groupStack.pop();
				}
				case "grpSpPr" -> inGroupSpPr = false;
				case "custGeom" -> {
					if (current != null && custPathBuf.length() > 0) {
						current.customPath = custPathBuf.toString().trim();
						current.customPathW = custPathW;
						current.customPathH = custPathH;
					}
					inCustGeom = false;
				}
				case "pathLst" -> inPathLst = false;
				case "path" -> inCustPath = false;
				case "txXfrm" -> inTxXfrm = false;
				case "txBody" -> {
					inTxBody = false;
					inParagraph = false;
				}
				case "p" -> inParagraph = false;
				case "ln" -> {
					inLn = false;
					fillContext = "shape";
				}
				case "solidFill" -> {
					inSolidFill = false;
					if (!inGradientStop) fillContext = inLn ? "stroke" : "shape";
				}
				case "gradFill" -> {
					inGradFill = false;
					inGsLst = false;
				}
				case "gs" -> {
					if (currentGradStop != null && current != null && current.gradientStops != null) {
						current.gradientStops.add(currentGradStop);
					}
					currentGradStop = null;
					inGradientStop = false;
					fillContext = "shape";
				}
				case "r" -> {
					if (currentRun != null && current != null) {
						current.textRuns.add(currentRun);
					}
					currentRun = null;
					inRun = false;
					inRunProps = false;
					fillContext = "shape";
				}
				case "rPr" -> inRunProps = false;
				case "t" -> {
					if (inText && currentRun != null) {
						currentRun.text = textBuf.toString();
					}
					inText = false;
				}
				default -> { /* ignore */ }
			}
		}

		private void flushPathCommand() {
			if (currentPathCommand == null || pathPoints.isEmpty()) return;
			StringBuilder cmd = new StringBuilder();
			cmd.append(" ").append(currentPathCommand);
			for (int[] pt : pathPoints) {
				cmd.append(" ").append(pt[0]).append(",").append(pt[1]);
			}
			custPathBuf.append(cmd);
			// Update last known position
			int[] last = pathPoints.get(pathPoints.size() - 1);
			lastPathX = last[0];
			lastPathY = last[1];
			pathPoints.clear();
			currentPathCommand = null;
		}

		private void appendArcTo(int wR, int hR, int stAng, int swAng) {
			double startRad = stAng * Math.PI / (180.0 * 60000);
			double sweepRad = swAng * Math.PI / (180.0 * 60000);
			double endRad = startRad + sweepRad;
			// Endpoint relative to center
			// Center can be inferred: center = currentPoint - (wR*cos(stAng), hR*sin(stAng))
			int centerX = lastPathX - (int)(wR * Math.cos(startRad));
			int centerY = lastPathY - (int)(hR * Math.sin(startRad));
			int endX = centerX + (int)(wR * Math.cos(endRad));
			int endY = centerY + (int)(hR * Math.sin(endRad));
			int largeArc = Math.abs(swAng) > 180 * 60000 ? 1 : 0;
			int sweep = swAng > 0 ? 1 : 0;
			custPathBuf.append(" A ").append(wR).append(",").append(hR)
				.append(" 0 ").append(largeArc).append(" ").append(sweep)
				.append(" ").append(endX).append(",").append(endY);
			lastPathX = endX;
			lastPathY = endY;
		}

		private void applyGroupTransforms(ShapeInfo shape) {
			// Apply from innermost (top of stack) to outermost.
			// The innermost group's bounds become the clip region for the shape.
			for (GroupTransform gt : groupStack) {
				if (gt.chExtCx > 0 && gt.chExtCy > 0) {
					// Set clip bounds from the innermost group (first iteration only)
					if (shape.clipCx == 0) {
						shape.clipX = gt.offX;
						shape.clipY = gt.offY;
						shape.clipCx = gt.extCx;
						shape.clipCy = gt.extCy;
					}
					shape.x = gt.offX + (int)((long)(shape.x - gt.chOffX) * gt.extCx / gt.chExtCx);
					shape.y = gt.offY + (int)((long)(shape.y - gt.chOffY) * gt.extCy / gt.chExtCy);
					shape.cx = (int)((long)shape.cx * gt.extCx / gt.chExtCx);
					shape.cy = (int)((long)shape.cy * gt.extCy / gt.chExtCy);
					if (shape.txCx > 0) {
						shape.txX = gt.offX + (int)((long)(shape.txX - gt.chOffX) * gt.extCx / gt.chExtCx);
						shape.txY = gt.offY + (int)((long)(shape.txY - gt.chOffY) * gt.extCy / gt.chExtCy);
						shape.txCx = (int)((long)shape.txCx * gt.extCx / gt.chExtCx);
						shape.txCy = (int)((long)shape.txCy * gt.extCy / gt.chExtCy);
					}
					// Also transform clip bounds through outer groups
					if (shape.clipCx > 0) {
						shape.clipX = gt.offX + (int)((long)(shape.clipX - gt.chOffX) * gt.extCx / gt.chExtCx);
						shape.clipY = gt.offY + (int)((long)(shape.clipY - gt.chOffY) * gt.extCy / gt.chExtCy);
						shape.clipCx = (int)((long)shape.clipCx * gt.extCx / gt.chExtCx);
						shape.clipCy = (int)((long)shape.clipCy * gt.extCy / gt.chExtCy);
					}
				}
			}
		}

		private static String resolveSchemeColorHex(String scheme, Map<String, String> themeColors) {
			if (themeColors != null) {
				String hex = themeColors.get(scheme);
				if (hex != null) return hex;
			}
			return switch (scheme) {
				case "lt1", "bg1" -> "FFFFFF";
				case "dk1", "tx1" -> "000000";
				case "lt2", "bg2" -> "E7E6E6";
				case "dk2", "tx2" -> "44546A";
				case "accent1" -> "4472C4";
				case "accent2" -> "ED7D31";
				case "accent3" -> "A5A5A5";
				case "accent4" -> "FFC000";
				case "accent5" -> "5B9BD5";
				case "accent6" -> "70AD47";
				default -> "4472C4";
			};
		}

		private static String stripPrefix(String qName) {
			int idx = qName.indexOf(':');
			return idx >= 0 ? qName.substring(idx + 1) : qName;
		}

		private static int intAttr(Attributes attrs, String name) {
			String v = attrs.getValue(name);
			if (v == null) return 0;
			try { return Integer.parseInt(v); } catch (NumberFormatException e) { return 0; }
		}

		private static int intAttrOrDefault(Attributes attrs, String name, int defaultValue) {
			String v = attrs.getValue(name);
			if (v == null) return defaultValue;
			try { return Integer.parseInt(v); } catch (NumberFormatException e) { return defaultValue; }
		}
	}
}
