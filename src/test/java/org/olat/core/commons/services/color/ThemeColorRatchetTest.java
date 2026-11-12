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
	 * values, and _bootstrap.scss only hands the Sass variables to the
	 * Bootstrap import. _bootstrap.scss has no own rules: they are in the
	 * modules and count there (test bootstrapHasNoOwnRules).
	 */
	private static final Set<String> SKIPPED = Set.of("_bootstrap.scss", "_states.scss", "_palette_vars.scss", "_tokens.scss");

	private static final Path BOOTSTRAP = LIGHT_MODULES.resolve("_bootstrap.scss");

	/*
	 * The lookahead (?!...) of the $ variable patterns skips a variable whose
	 * name holds a colour word (border, bg, color, background) but whose value
	 * is no colour: a dimension word anywhere in the name ($btn-border-radius-base,
	 * $o-cn-style-border-width, $o-proj-header-background-height-max), "style"
	 * only at the end ($o-qti-match-source-border-style: dashed,
	 * $o-hs-podium-bg-gradientstyle: true). "style" is also the namespace of
	 * the course element styles: $o-cn-style-border and $o-cn-style-bg count.
	 * No colour either at the end of a name (OO-10043 slice 17): a gradient
	 * direction ($o-login-bg-gradient-to), a gradient stop list
	 * ($o-login-form-bg-gradient-steps), an image path ($o-login-form-bg-img)
	 * and a background position ($o-login-form-bg-position-left, -top).
	 * OO-10043 slice 18 (same kind, decision D31 Q4): a ratio or a break point
	 * of a size ($o-proj-header-background-ratio, -break) and a blend mode
	 * ($o-cat2-header-noimage-bg-blend-mode).
	 * OO-10043 slice 19 (decision D32 E1): the name classes accept upper-case
	 * letters ($o-qti-textEntryInteraction-bg, $o-qti-badResponse-color).
	 */
	private static final List<Pattern> OLD_COLOR_REFERENCES = List.of(
			Pattern.compile("#[0-9a-fA-F]{3,8}\\b"),
			Pattern.compile(":\\s*(?:red|green|blue|yellow|orange|white|black|gray|grey|lightgrey|lightgray|silver|purple)\\b"),
			// $brand-primary, not a local variable $brand-primary-bg
			Pattern.compile("\\$brand-[a-zA-Z]+(?![\\w-])"),
			Pattern.compile("\\$(?![a-zA-Z0-9-]*(?:radius|width|height|padding|size|margin|offset)|[a-zA-Z0-9-]*(?:style|gradient-to|gradient-steps|-img|-position-left|-position-top|-ratio|-break|-blend-mode)(?![\\w-]))(?:gray(?:-[a-zA-Z]+)?|text-color|text-muted|link-color|link-hover-color|body-bg|state-[a-zA-Z]+-[a-zA-Z]+|panel-[a-zA-Z-]+|table-[a-zA-Z-]+|input-[a-zA-Z-]+|btn-[a-zA-Z-]+|navbar-[a-zA-Z-]+|label-[a-zA-Z-]+|alert-[a-zA-Z-]+)\\b"),
			// The colour variables of the other Bootstrap components: the name ends with color, bg or border
			// ($dropdown-link-hover-bg, $hr-border, $modal-content-border-color), not $modal-lg or $popover-arrow-width
			Pattern.compile("\\$(?![a-zA-Z0-9-]*(?:radius|width|height|padding|size|margin|offset)|[a-zA-Z0-9-]*(?:style|gradient-to|gradient-steps|-img|-position-left|-position-top|-ratio|-break|-blend-mode)(?![\\w-]))(?:dropdown|popover|modal|pre|hr|legend|blockquote|thumbnail|progress|tooltip|code|kbd|badge|pagination|pager|breadcrumb|list-group|well|jumbotron(?=-bg)|close|nav|carousel|component-active|headings-small|abbr|page-header)-[a-zA-Z-]*?(?:color|bg|border)(?![\\w-])"),
			Pattern.compile("\\$(?![a-zA-Z0-9-]*(?:radius|width|height|padding|size|margin|offset)|[a-zA-Z0-9-]*(?:style|gradient-to|gradient-steps|-img|-position-left|-position-top|-ratio|-break|-blend-mode)(?![\\w-]))o-[a-zA-Z0-9-]*(?:color|bg|border|background)[a-zA-Z0-9-]*"),
			// color-mix( is the decided form of a translucent token colour (M3): only a Sass mix( or color.mix( counts
			Pattern.compile("\\b(?:darken|lighten|rgba|fade-in|fade-out|adjust-color|scale-color|transparentize|opacify)\\(|(?<![\\w-])mix\\("),
			Pattern.compile("\\b(?:rgb|hsla?)\\("),
			Pattern.compile("\\bo-(?:a11y-color|state-color|palette|swatch-color)\\("));

	private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
	private static final Pattern LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern SASS_NAME = Pattern.compile("\\$[\\w-]+");

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

	@Test
	public void countBootstrapComponentReferences() {
		String scss = """
				.d { color: $dropdown-link-hover-color; border-top: 1px solid $hr-border; width: $modal-lg; }
				.e { background: $brand-primary-bg; margin: $popover-arrow-width; color: $brand-info; }
				.f { border-color: $modal-content-border-color; opacity: $modal-backdrop-opacity; color: $jumbotron-color; }
				""";
		// $dropdown-link-hover-color, $hr-border, $brand-info, $modal-content-border-color
		Assert.assertEquals(4, countOldColorReferences(scss));
	}

	@Test
	public void countUnderscoreVariables() {
		String scss = """
				.l { border-color: $o-o_speedometer-danger-border-color; color: $o_color_note; }
				.m { margin-left: $label_right_margin; height: $o_proj-header-background-height-max; }
				""";
		// the two colour variables, not the margin and the height
		Assert.assertEquals(2, countOldColorReferences(scss));
	}

	@Test
	public void countStyleVariables() {
		String scss = """
				.g { border: $o-cn-style-border; background-color: $o-cn-style-bg; color: $o-cn-style-cover-text-bg; }
				.h { border-radius: $o-cn-style-border-radius; border-width: $o-cn-style-border-width; }
				.i { border-style: $o-qti-match-source-border-style; @if $o-hs-podium-bg-gradientstyle { color: inherit; } }
				.j { border-radius: $btn-border-radius-base; height: $o-proj-header-background-height-max; }
				.k { border: $o-cn-style-border-line-width $o-cn-style-border-line-style var(--o-course-style-border); }
				""";
		// $o-cn-style-border, $o-cn-style-bg, $o-cn-style-cover-text-bg; not the width and the
		// style of the course style border (decision D19 of OO-10043)
		Assert.assertEquals(3, countOldColorReferences(scss));
	}

	/**
	 * _bootstrap.scss only hands the Sass variables to the Bootstrap import and
	 * imports Bootstrap. A rule there would escape the ratchet: put it into the
	 * module of its component.
	 */
	@Test
	public void bootstrapHasNoOwnRules() throws IOException {
		String code = withoutComments(Files.readString(BOOTSTRAP, StandardCharsets.UTF_8));
		Assert.assertFalse("_bootstrap.scss has an own rule. Put it into the module of its component.", code.contains("{"));
	}

	/**
	 * Sass treats "_" and "-" in a name as equal: $o-o_speedometer-danger-border-color
	 * is $o-o-speedometer-danger-border-color (OO-10043 slice 19, follow-up).
	 * The variable names get "-" before the count.
	 */
	static String sassNames(String code) {
		Matcher m = SASS_NAME.matcher(code);
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			m.appendReplacement(sb, Matcher.quoteReplacement(m.group().replace('_', '-')));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	// The SCSS source without block and line comments
	static String withoutComments(String scss) {
		return LINE_COMMENT.matcher(BLOCK_COMMENT.matcher(scss).replaceAll("")).replaceAll("");
	}

	static int countOldColorReferences(String scss) {
		String code = sassNames(withoutComments(scss));
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
