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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Ratchet for the colours of the light theme modules: the number of old
 * colour references per module (hex values, named colours, $brand-*,
 * Bootstrap and $o-* colour variables, colour maths) must not change
 * against <code>theme_color_baseline.properties</code>. New code reads
 * the colour tokens with <code>var(--o-*)</code> (doc/openolat-frontend.md,
 * chapter 26). When a module reads more tokens, lower its count in the
 * baseline. The patterns are the same as in the OO-colors inventory
 * script color_inventory.py.
 *
 * Initial date: 8 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ThemeColorRatchetTest {

	/** The SCSS modules of the light theme, relative to the project directory */
	public static final Path LIGHT_MODULES = Path.of("src/main/webapp/static/themes/light/modules");

	private static final String BASELINE = "theme_color_baseline.properties";

	/**
	 * Not counted: the state wiring and the token sources may use colour
	 * values, and _bootstrap.scss hands the Sass variables to the Bootstrap
	 * import. The own rules of _bootstrap.scss are not counted either.
	 */
	private static final Set<String> SKIPPED = Set.of("_bootstrap.scss", "_states.scss", "_palette_vars.scss", "_tokens.scss");

	private static final List<Pattern> OLD_COLOR_REFERENCES = List.of(
			Pattern.compile("#[0-9a-fA-F]{3,8}\\b"),
			Pattern.compile(":\\s*(?:red|green|blue|yellow|orange|white|black|gray|grey|lightgrey|lightgray|silver|purple)\\b"),
			Pattern.compile("\\$brand-[a-z]+"),
			Pattern.compile("\\$(?![a-z0-9-]*(?:radius|width|height|padding|size|margin|offset|style))(?:gray(?:-[a-z]+)?|text-color|text-muted|link-color|link-hover-color|body-bg|state-[a-z]+-[a-z]+|panel-[a-z-]+|table-[a-z-]+|input-[a-z-]+|btn-[a-z-]+|navbar-[a-z-]+|label-[a-z-]+|alert-[a-z-]+)\\b"),
			Pattern.compile("\\$(?![a-z0-9-]*(?:radius|width|height|padding|size|margin|offset|style))o-[a-z0-9-]*(?:color|bg|border|background)[a-z0-9-]*"),
			Pattern.compile("\\b(?:darken|lighten|rgba|fade-in|fade-out|mix|adjust-color|scale-color|transparentize|opacify)\\("),
			Pattern.compile("\\b(?:rgb|hsla?)\\("),
			Pattern.compile("\\bo-(?:a11y-color|state-color|palette|swatch-color)\\("));

	private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
	private static final Pattern LINE_COMMENT = Pattern.compile("//[^\\n]*");

	@Test
	public void colorReferencesMatchBaseline() throws IOException {
		Properties baseline = loadBaseline();
		List<String> errors = new ArrayList<>();
		try (Stream<Path> modules = Files.list(LIGHT_MODULES)) {
			for (Path module : modules.filter(p -> p.getFileName().toString().endsWith(".scss")).sorted().toList()) {
				String name = module.getFileName().toString();
				if (SKIPPED.contains(name)) {
					continue;
				}
				int count = countOldColorReferences(Files.readString(module, StandardCharsets.UTF_8));
				int allowed = Integer.parseInt(baseline.getProperty(name, "0"));
				if (count > allowed) {
					errors.add(name + ": " + count + " old colour references, baseline " + allowed
							+ ". Use a colour token var(--o-*), see doc/openolat-frontend.md chapter 26.");
				} else if (count < allowed) {
					errors.add(name + ": " + count + " old colour references, baseline " + allowed
							+ ". Lower the count in " + BASELINE + " to " + count + ".");
				}
			}
		}
		Assert.assertTrue(String.join("\n", errors), errors.isEmpty());
	}

	@Test
	public void countReferences() {
		String scss = """
				/* #FFFFFF in a comment */
				.a { color: #333; background: $brand-primary; } // $text-color
				.b { border-color: darken($o-navbar-bg, 10%); color: var(--o-text); }
				.c { color: white; }
				""";
		// #333, $brand-primary, darken(, $o-navbar-bg, : white
		Assert.assertEquals(5, countOldColorReferences(scss));
	}

	// The SCSS source without block and line comments
	static String withoutComments(String scss) {
		return LINE_COMMENT.matcher(BLOCK_COMMENT.matcher(scss).replaceAll("")).replaceAll("");
	}

	static int countOldColorReferences(String scss) {
		String code = withoutComments(scss);
		int count = 0;
		for (Pattern pattern : OLD_COLOR_REFERENCES) {
			Matcher matcher = pattern.matcher(code);
			while (matcher.find()) {
				count++;
			}
		}
		return count;
	}

	private Properties loadBaseline() throws IOException {
		Properties baseline = new Properties();
		try (InputStream in = ThemeColorRatchetTest.class.getResourceAsStream(BASELINE)) {
			Assert.assertNotNull("Missing " + BASELINE, in);
			baseline.load(in);
		}
		return baseline;
	}
}
