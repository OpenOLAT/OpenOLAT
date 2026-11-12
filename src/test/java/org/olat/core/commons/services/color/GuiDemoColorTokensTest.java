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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.olat.gui.demo.guidemo.GuiDemoColorTokens;
import org.olat.gui.demo.guidemo.GuiDemoColorTokens.Token;
import org.olat.gui.demo.guidemo.GuiDemoColorTokens.TokenGroup;

/**
 * Checks the tokens of the GUI demo tab "Greys and tokens"
 * ({@link GuiDemoColorTokens}) against the theme: every custom property the
 * tab lists or a sample reads is declared in the compiled
 * <code>light/theme.css</code>, the neutral steps and the state roles match
 * the Sass, every semantic token of <code>$o-token-base</code> is on the tab,
 * and the element tokens have the layer and the default of
 * <code>modules/_tokens.scss</code>. No Spring context.
 *
 * Initial date: 11 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class GuiDemoColorTokensTest {

	private static final Path LIGHT = Path.of("src/main/webapp/static/themes/light");
	private static final Path THEME_CSS = LIGHT.resolve("theme.css");
	private static final Path TOKENS_SCSS = LIGHT.resolve("modules/_tokens.scss");
	private static final Path NEUTRAL_SCSS = LIGHT.resolve("_palette_neutral.scss");

	private static final Pattern DECLARATION = Pattern.compile("--o-([a-z0-9-]+)\\s*:");
	private static final Pattern VAR_REFERENCE = Pattern.compile("var\\(--o-([a-z0-9-]+)\\)");
	private static final Pattern MAP_KEY = Pattern.compile("^\\s*\"([a-z0-9-]+)\"\\s*:\\s*(.+?),?\\s*$", Pattern.MULTILINE);
	// upper-case letters in a name: $o-qti-textEntryInteraction-filled-bg (OO-10043 slice 19)
	private static final Pattern SASS_VARIABLE = Pattern.compile("\\$[a-zA-Z0-9-]+");
	private static final Pattern FOLLOWS = Pattern.compile("follows ([a-z0-9-]+)");
	// "N% darker at most, ...": the amount of o-contrast-shade-amount() (OO-10043, decision D9)
	private static final Pattern SHADE = Pattern.compile("^shade: ([a-z0-9-]+), (\\d+)% darker( at most, .*)?$");

	@Test
	public void tokensDeclared() {
		Set<String> declared = declared();
		List<String> missing = new ArrayList<>();
		for (String name : GuiDemoColorTokens.allTokenNames()) {
			if (!declared.contains(name)) {
				missing.add("--o-" + name);
			}
		}
		Assert.assertTrue("Tokens of the tab not declared in light/theme.css: " + missing, missing.isEmpty());
	}

	@Test
	public void sampleReferencesDeclared() {
		Set<String> declared = declared();
		List<String> missing = new ArrayList<>();
		for (String style : GuiDemoColorTokens.allSampleStyles()) {
			Assert.assertFalse("A sample has a literal colour: " + style, style.contains("#"));
			Matcher m = VAR_REFERENCE.matcher(style);
			while (m.find()) {
				if (!declared.contains(m.group(1))) {
					missing.add("--o-" + m.group(1));
				}
			}
		}
		Assert.assertTrue("Custom properties of the samples not declared in light/theme.css: " + missing, missing.isEmpty());
	}

	@Test
	public void neutralSteps() {
		String scss = read(NEUTRAL_SCSS);
		Matcher m = Pattern.compile("\\$o-neutral-steps:\\s*\\(([^)]*)\\)").matcher(scss);
		Assert.assertTrue("$o-neutral-steps not found in _palette_neutral.scss", m.find());
		List<Integer> steps = new ArrayList<>();
		for (String step : m.group(1).split(",")) {
			steps.add(Integer.valueOf(step.trim()));
		}
		Assert.assertEquals(steps, GuiDemoColorTokens.NEUTRAL_STEPS);
	}

	@Test
	public void stateRoles() {
		String scss = read(TOKENS_SCSS);
		Matcher m = Pattern.compile("\\$o-token-state-roles:\\s*\\(([^)]*)\\)").matcher(scss);
		Assert.assertTrue("$o-token-state-roles not found in _tokens.scss", m.find());
		List<String> roles = new ArrayList<>();
		for (String role : m.group(1).split(",")) {
			roles.add(role.trim().replace("\"", ""));
		}
		Assert.assertEquals(roles, GuiDemoColorTokens.STATE_ROLES);
	}

	/**
	 * Every semantic token of $o-token-base is on the tab, with its Sass
	 * variable as source.
	 */
	@Test
	public void semanticTokens() {
		String base = mapBlock(read(TOKENS_SCSS), "$o-token-base");
		List<String> onTab = new ArrayList<>();
		for (TokenGroup group : GuiDemoColorTokens.semanticGroups()) {
			for (Token token : group.tokens()) {
				onTab.add(token.name());
				String def = definition(base, token.name());
				if (def == null) {
					continue;// an element token in the semantic groups: link-subtle
				}
				Matcher var = SASS_VARIABLE.matcher(token.defaultValue());
				if (var.find()) {
					Assert.assertTrue("--o-" + token.name() + ": " + var.group() + " is not its source in $o-token-base: " + def,
							def.contains(var.group()));
				}
			}
		}
		Matcher m = MAP_KEY.matcher(base);
		while (m.find()) {
			Assert.assertTrue("The semantic token --o-" + m.group(1) + " is not on the tab", onTab.contains(m.group(1)));
		}
	}

	/**
	 * The element tokens and their shade tokens have the layer and the
	 * default of _tokens.scss.
	 */
	@Test
	public void elementTokens() {
		String scss = read(TOKENS_SCSS);
		String baseLayer = mapBlock(scss, "$o-token-layer-base");
		String frameworkLayer = mapBlock(scss, "$o-token-layer-framework");
		String componentLayer = mapBlock(scss, "$o-token-layer-component");
		String shades = mapBlock(scss, "$o-token-shades");

		List<TokenGroup> groups = new ArrayList<>(GuiDemoColorTokens.elementGroups());
		// link-subtle of the semantic group Link is an element token of the base layer
		groups.add(new TokenGroup("Link", "base layer", GuiDemoColorTokens.semanticGroups().stream()
				.flatMap(group -> group.tokens().stream())
				.filter(token -> definition(mapBlock(scss, "$o-token-base"), token.name()) == null)
				.toList()));

		for (TokenGroup group : groups) {
			// a group of the framework layer may show a component token too (OO-10043 slice 19)
			String layer = "base layer".equals(group.layer()) ? baseLayer : frameworkLayer + componentLayer;
			for (Token token : group.tokens()) {
				String name = "--o-" + token.name();
				String value = token.defaultValue();
				Matcher shade = SHADE.matcher(value);
				if (shade.matches()) {
					String def = definition(shades, token.name());
					Assert.assertNotNull(name + " is not a shade token in $o-token-shades", def);
					String expected = shade.group(3) == null
							? "(\"" + shade.group(1) + "\"," + shade.group(2) + "%"
							: "(\"" + shade.group(1) + "\",o-contrast-shade-amount(";
					Assert.assertTrue(name + ": the shade is not " + value + " in _tokens.scss: " + def,
							def.replace(" ", "").startsWith(expected));
					// the state fills: o-contrast-shade-amount() and the function "state"
					// (lighter when the fill cannot get darker, decision D20)
					Assert.assertTrue(name + ": the shade is not " + value + " in _tokens.scss: " + def,
							shade.group(3) == null || def.replace(" ", "").endsWith("," + shade.group(2) + "%),\"state\")"));
					Assert.assertNotNull(name + ": the token of the shade is not in the " + group.layer(),
							definition(layer, shade.group(1)));
					continue;
				}
				String def = definition(layer, token.name());
				Assert.assertNotNull(name + " is not in the map of the " + group.layer() + " in _tokens.scss", def);
				if (value.startsWith("\"")) {
					Assert.assertEquals(name + ": reference default", value, def);
				} else {
					Matcher var = SASS_VARIABLE.matcher(value);
					Assert.assertTrue(name + ": no Sass variable in the default of the tab: " + value, var.find());
					Assert.assertTrue(name + ": " + var.group() + " is not the default in _tokens.scss: " + def,
							def.contains(var.group() + ",") || def.contains(var.group() + ")"));
					Matcher follows = FOLLOWS.matcher(value);
					if (follows.find()) {
						Assert.assertTrue(name + ": does not follow " + follows.group(1) + " in _tokens.scss: " + def,
								def.contains("\"" + follows.group(1) + "\""));
					}
				}
			}
		}
	}

	/**
	 * @return The map of a Sass variable: the text from "$name: (" to the
	 * 		first line ");"
	 */
	private static String mapBlock(String scss, String variable) {
		int start = scss.indexOf(variable + ": (");
		Assert.assertTrue(variable + " not found in _tokens.scss", start >= 0);
		int end = scss.indexOf("\n);", start);
		Assert.assertTrue("End of " + variable + " not found in _tokens.scss", end > start);
		return scss.substring(start, end);
	}

	/**
	 * @return The default of a key in a map block, without the trailing comma, or null
	 */
	private static String definition(String block, String name) {
		Matcher m = MAP_KEY.matcher(block);
		while (m.find()) {
			if (m.group(1).equals(name)) {
				return m.group(2).trim();
			}
		}
		return null;
	}

	private static Set<String> declared() {
		Set<String> declared = new HashSet<>();
		Matcher m = DECLARATION.matcher(read(THEME_CSS));
		while (m.find()) {
			declared.add(m.group(1));
		}
		return declared;
	}

	private static String read(Path file) {
		try {
			return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError("Cannot read " + file.toAbsolutePath(), e);
		}
	}
}
