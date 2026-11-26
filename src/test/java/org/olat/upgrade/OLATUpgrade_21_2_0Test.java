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
package org.olat.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.olat.core.commons.services.color.ColorFamily;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorServiceImpl;
import org.olat.core.commons.services.color.ColorSet;
import org.olat.core.commons.services.color.Swatch;
import org.olat.upgrade.OLATUpgrade_21_2_0.ColorColumn;

/**
 * The mapping of the colour upgrade of {@link OLATUpgrade_21_2_0}: the old
 * colour classes of 4 columns to the keys of the colour set M, the classes of
 * the predefined course colour categories to the swatch classes of these
 * keys (OO-9993). No database, no Spring.
 *
 * Initial date: 26 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class OLATUpgrade_21_2_0Test {

	/**
	 * The hue the old avatar colours and the old predefined colour categories
	 * rendered with before the upgrade (their aliases of option A, removed
	 * with the upgrade). The hue follows the name of the old key.
	 */
	private static final Map<String,String> AVATAR_AND_CATEGORY_HUES = map(
			"dark_blue", "blue",
			"light_blue", "cyan",
			"purple", "magenta",
			"red", "red",
			"orange", "orange",
			"yellow", "yellow",
			"light_green", "lime",
			"dark_green", "green");

	private final ColorService colorService = new ColorServiceImpl();

	@Test
	public void calendarColors() {
		assertThat(OLATUpgrade_21_2_0.CALENDAR_COLOR_UPGRADES).containsExactlyEntriesOf(map(
				"o_cal_grey", "gray",
				"o_cal_yellow", "yellow",
				"o_cal_orange", "orange",
				"o_cal_red", "red",
				"o_cal_maroon", "red",
				"o_cal_blue", "blue",
				"o_cal_navy", "blue",
				"o_cal_fuchsia", "magenta",
				"o_cal_rebeccapurple", "violet",
				"o_cal_lime", "lime",
				"o_cal_green", "green",
				"o_cal_olive", "lime"));
		// every old calendar colour
		assertThat(OLATUpgrade_21_2_0.CALENDAR_COLOR_UPGRADES.keySet())
			.containsExactlyElementsOf(ColorService.CALENDAR_COLORS.stream().map(key -> "o_cal_" + key).toList());
	}

	@Test
	public void avatarColors() {
		Map<String,String> expected = map(
				"dark_blue", "blue",
				"light_blue", "cyan",
				"purple", "magenta",
				"red", "red",
				"orange", "orange",
				"yellow", "yellow",
				"light_green", "lime",
				"dark_green", "green");
		assertThat(OLATUpgrade_21_2_0.PROJECT_AVATAR_COLOR_UPGRADES).containsExactlyEntriesOf(prefixed("o_proj_avatar_", expected));
		assertThat(OLATUpgrade_21_2_0.USER_INITIALS_COLOR_UPGRADES).containsExactlyEntriesOf(prefixed("o_user_initials_", expected));
	}

	@Test
	public void buildingColors() {
		assertThat(OLATUpgrade_21_2_0.BUILDING_COLOR_UPGRADES).containsExactlyEntriesOf(map(
				"o_color_lightgray", "gray",
				"o_color_yellow", "yellow",
				"o_color_orange", "orange",
				"o_color_brown", "brown",
				"o_color_red", "red",
				"o_color_orchid", "pink",
				"o_color_purple", "violet",
				"o_color_lightblue", "cyan",
				"o_color_cobaltblue", "blue",
				"o_color_darkblue", "blue",
				"o_color_lightgreen", "lime",
				"o_color_seagreen", "green"));
		// every key of the old color.list
		assertThat(OLATUpgrade_21_2_0.BUILDING_COLOR_UPGRADES.keySet())
			.containsExactlyElementsOf(ColorService.LEGACY_COLORS.stream().map(key -> "o_color_" + key).toList());
	}

	@Test
	public void colorCategories() {
		assertThat(OLATUpgrade_21_2_0.COLOR_CATEGORY_UPGRADES).containsExactlyEntriesOf(map(
				"o_colcat_dark_blue", "o_swatch_blue",
				"o_colcat_light_blue", "o_swatch_cyan",
				"o_colcat_purple", "o_swatch_magenta",
				"o_colcat_red", "o_swatch_red",
				"o_colcat_orange", "o_swatch_orange",
				"o_colcat_yellow", "o_swatch_yellow",
				"o_colcat_light_green", "o_swatch_lime",
				"o_colcat_dark_green", "o_swatch_green"));
		// the new value is the swatch class the colour chooser of the administration stores
		for (String newValue : OLATUpgrade_21_2_0.COLOR_CATEGORY_UPGRADES.values()) {
			String key = newValue.substring(ColorService.SWATCH_CSS_CLASS_PREFIX.length());
			assertThat(colorService.toSwatchCssClass(key)).isEqualTo(newValue);
		}
	}

	/**
	 * The new value is a hue key of the colour set M, the solid swatch of the
	 * hue the old value renders with: the hue of its alias, or the swatch
	 * itself if the old key is a swatch key. The avatars and the categories
	 * have no alias any more: the hue in the name of the old key.
	 */
	@Test
	public void newValuesAreHuesOfTheColorSetM() {
		assertSameHue(OLATUpgrade_21_2_0.CALENDAR_COLOR_UPGRADES, "o_cal_", "", ColorFamily.CALENDAR);
		assertSameHue(OLATUpgrade_21_2_0.BUILDING_COLOR_UPGRADES, "o_color_", "", ColorFamily.COLOR_SERVICE);
		assertSameHueAsName(OLATUpgrade_21_2_0.PROJECT_AVATAR_COLOR_UPGRADES, "o_proj_avatar_", "");
		assertSameHueAsName(OLATUpgrade_21_2_0.USER_INITIALS_COLOR_UPGRADES, "o_user_initials_", "");
		assertSameHueAsName(OLATUpgrade_21_2_0.COLOR_CATEGORY_UPGRADES, "o_colcat_", ColorService.SWATCH_CSS_CLASS_PREFIX);
	}

	private void assertSameHue(Map<String,String> upgrades, String prefix, String newPrefix, ColorFamily family) {
		for (Map.Entry<String,String> upgrade : upgrades.entrySet()) {
			String oldValue = upgrade.getKey();
			String newKey = assertSolidHue(oldValue, upgrade.getValue(), prefix, newPrefix);

			Swatch rendered = colorService.resolveStored(oldValue.substring(prefix.length()), family);
			assertThat(rendered).as(oldValue).isNotNull();
			assertThat(rendered.hue()).as(oldValue).isEqualTo(newKey);
		}
	}

	private void assertSameHueAsName(Map<String,String> upgrades, String prefix, String newPrefix) {
		assertThat(upgrades).hasSameSizeAs(AVATAR_AND_CATEGORY_HUES);
		for (Map.Entry<String,String> upgrade : upgrades.entrySet()) {
			String oldValue = upgrade.getKey();
			String newKey = assertSolidHue(oldValue, upgrade.getValue(), prefix, newPrefix);

			String oldKey = oldValue.substring(prefix.length());
			assertThat(AVATAR_AND_CATEGORY_HUES.get(oldKey)).as(oldValue).isEqualTo(newKey);
			// the old key names a hue of the palette: the new key is that hue, e.g. "red",
			// "dark_blue" (a light hue has a neighbour hue: "light_blue" is "cyan")
			String nameHue = oldKey.replaceFirst("^dark_", "");
			if (ColorService.HUES.contains(nameHue)) {
				assertThat(newKey).as(oldValue).isEqualTo(nameHue);
			}
		}
	}

	/**
	 * @return The key of the new value: a hue key of the colour set M, the solid swatch
	 */
	private static String assertSolidHue(String oldValue, String newValue, String prefix, String newPrefix) {
		assertThat(oldValue).isEqualTo(oldValue.toLowerCase(Locale.ROOT)).startsWith(prefix);
		assertThat(newValue).as(oldValue).startsWith(newPrefix);
		String newKey = newValue.substring(newPrefix.length());
		assertThat(ColorSet.M.getHueKeys()).as(oldValue).contains(newKey);
		assertThat(Swatch.fromKey(newKey)).as(oldValue).isEqualTo(new Swatch(newKey, Swatch.SOLID));
		return newKey;
	}

	@Test
	public void columns() {
		assertThat(OLATUpgrade_21_2_0.COLOR_COLUMNS).extracting(ColorColumn::name).containsExactly(
				"o_cal_use_config.c_cssclass",
				"o_proj_project.p_avatar_css_class",
				"o_user.u_initials_css_class",
				"o_rm_building.r_color_css");
		assertThat(OLATUpgrade_21_2_0.COLOR_CATEGORY_COLUMNS).extracting(ColorColumn::name).containsExactly(
				"o_course_color_category.c_css_class");
		for (ColorColumn column : allColumns()) {
			for (String newValue : column.upgrades().values()) {
				// no new value is an old value: the upgrade is idempotent
				assertThat(column.upgrades()).as(column.name()).doesNotContainKey(newValue);
				// a new value is no unknown value: no warning after the upgrade
				assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(newValue, column)).as(newValue).isFalse();
			}
		}
	}

	@Test
	public void unknownValues() {
		ColorColumn calendar = column("o_cal_use_config.c_cssclass");
		ColorColumn initials = column("o_user.u_initials_css_class");
		ColorColumn building = column("o_rm_building.r_color_css");

		// NULL, empty, keys of the chooser sets: unchanged, no warning
		for (String value : new String[] { null, "", " ", "blue", "teal", "theme2", "blue-light", "theme1-dark" }) {
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, calendar)).as(value).isFalse();
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, building)).as(value).isFalse();
		}
		// old values of the column, also in another case
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_cal_navy", calendar)).isFalse();
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_cal_Blue", calendar)).isFalse();
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("O_COLOR_LIGHTBLUE", building)).isFalse();
		// the grey initials stay
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_user_initials_grey", initials)).isFalse();
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_user_initials_grey", building)).isTrue();
		// unknown: an old value of another column, an unknown key, junk
		for (String value : new String[] { "o_color_lightblue", "o_cal_teal", "navy", "Blue", " blue", "blue-solid",
				"o_cal_blue\" x=\"", "x".repeat(10_000) }) {
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, calendar)).as(value).isTrue();
		}
	}

	@Test
	public void unknownColorCategoryValues() {
		ColorColumn category = column("o_course_color_category.c_css_class");

		// NULL, empty, the technical categories, swatch classes of the chooser sets: unchanged, no warning
		for (String value : new String[] { null, "", " ", "o_colcat_nocolor", "o_colcat_inherited", "o_colcat_course",
				"o_swatch_blue", "o_swatch_teal", "o_swatch_gray", "o_swatch_theme1", "o_swatch_theme2", "o_swatch_blue-light" }) {
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, category)).as(value).isFalse();
		}
		// old values of the column, also in another case
		for (String value : OLATUpgrade_21_2_0.COLOR_CATEGORY_UPGRADES.keySet()) {
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, category)).as(value).isFalse();
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value.toUpperCase(Locale.ROOT), category)).as(value).isFalse();
		}
		// unknown: classes an administrator entered, a bare key, a technical class in
		// another case, an unknown swatch, junk
		for (String value : new String[] { "my_category", "o_colcat_bg o_swatch_blue", "o_swatch_blue o_swatch_light",
				"blue", "theme1", "o_colcat_teal", "O_COLCAT_NOCOLOR", "o_swatch_", "o_swatch_lightblue", "o_swatch_Blue",
				"o_colcat_red\" x=\"", "x".repeat(10_000) }) {
			assertThat(OLATUpgrade_21_2_0.isUnknownColorValue(value, category)).as(value).isTrue();
		}
		// the classes of the technical categories are unknown in the other columns
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_colcat_nocolor", column("o_cal_use_config.c_cssclass"))).isTrue();
		// a swatch class is unknown in the columns of the bare keys
		assertThat(OLATUpgrade_21_2_0.isUnknownColorValue("o_swatch_blue", column("o_rm_building.r_color_css"))).isTrue();
	}

	private static List<ColorColumn> allColumns() {
		List<ColorColumn> columns = new ArrayList<>(OLATUpgrade_21_2_0.COLOR_COLUMNS);
		columns.addAll(OLATUpgrade_21_2_0.COLOR_CATEGORY_COLUMNS);
		return columns;
	}

	private static ColorColumn column(String name) {
		return allColumns().stream()
				.filter(column -> column.name().equals(name))
				.findFirst().orElseThrow();
	}

	private static Map<String,String> map(String... keyValues) {
		Map<String,String> map = new LinkedHashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			map.put(keyValues[i], keyValues[i + 1]);
		}
		return map;
	}

	private static Map<String,String> prefixed(String prefix, Map<String,String> keys) {
		Map<String,String> map = new LinkedHashMap<>();
		keys.forEach((key, value) -> map.put(prefix + key, value));
		return map;
	}
}
