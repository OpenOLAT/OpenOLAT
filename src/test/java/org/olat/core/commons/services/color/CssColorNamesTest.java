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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the named colours of CSS and the nearest named colour in OKLab.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class CssColorNamesTest {

	/** The keys of the calendar colours, all of them CSS colour names */
	private static final String[] CALENDAR_KEYS = { "grey", "yellow", "orange", "red", "maroon",
			"blue", "navy", "fuchsia", "rebeccapurple", "lime", "green", "olive" };

	@Test
	public void namedColors() {
		Map<String,String> namedColors = CssColorNames.getNamedColors();
		// 147 names of CSS Color Level 3 and rebeccapurple
		Assert.assertEquals(148, namedColors.size());
		Assert.assertEquals("#000080", namedColors.get("navy"));
		Assert.assertEquals("#663399", namedColors.get("rebeccapurple"));
		for (Map.Entry<String,String> namedColor : namedColors.entrySet()) {
			Assert.assertTrue(namedColor.getKey(), CssColorNames.isHexColor(namedColor.getValue()));
		}
	}

	@Test
	public void isColorName() {
		for (String key : CALENDAR_KEYS) {
			Assert.assertTrue(key, CssColorNames.isColorName(key));
		}
		Assert.assertTrue(CssColorNames.isColorName("teal"));
		Assert.assertTrue(CssColorNames.isColorName("DarkSlateBlue"));
		
		Assert.assertFalse(CssColorNames.isColorName(null));
		Assert.assertFalse(CssColorNames.isColorName(""));
		Assert.assertFalse(CssColorNames.isColorName("cobaltblue"));
		Assert.assertFalse(CssColorNames.isColorName("seagreenish"));
		Assert.assertFalse(CssColorNames.isColorName("theme1"));
		Assert.assertFalse(CssColorNames.isColorName("slate"));
		Assert.assertFalse(CssColorNames.isColorName("o_cal_blue"));
		Assert.assertFalse(CssColorNames.isColorName("#0000FF"));
	}

	@Test
	public void nearestColorNameOfNamedColors() {
		for (Map.Entry<String,String> namedColor : CssColorNames.getNamedColors().entrySet()) {
			String nearest = CssColorNames.nearestColorName(namedColor.getValue());
			// the name itself or another name of the same colour, e.g. "grey" and "gray"
			Assert.assertEquals(namedColor.getKey(), namedColor.getValue(), CssColorNames.getNamedColors().get(nearest));
		}
		// the first name in alphabetical order
		Assert.assertEquals("aqua", CssColorNames.nearestColorName("#00FFFF"));
		Assert.assertEquals("fuchsia", CssColorNames.nearestColorName("#FF00FF"));
		Assert.assertEquals("gray", CssColorNames.nearestColorName("#808080"));
	}

	@Test
	public void nearestColorName() {
		Assert.assertEquals("navy", CssColorNames.nearestColorName("#000082"));
		Assert.assertEquals("red", CssColorNames.nearestColorName("#FE0101"));
		Assert.assertEquals("white", CssColorNames.nearestColorName("#FEFEFE"));
		Assert.assertEquals("black", CssColorNames.nearestColorName("#010101"));
		// lower case
		Assert.assertEquals("teal", CssColorNames.nearestColorName("#008081"));
	}

	@Test
	public void nearestColorNameInvalid() {
		Assert.assertNull(CssColorNames.nearestColorName(null));
		Assert.assertNull(CssColorNames.nearestColorName(""));
		Assert.assertNull(CssColorNames.nearestColorName("blue"));
		Assert.assertNull(CssColorNames.nearestColorName("#00F"));
		Assert.assertNull(CssColorNames.nearestColorName("#GGGGGG"));
	}

	@Test
	public void oklab() {
		assertOklab(new double[] { 0d, 0d, 0d }, CssColorNames.toOklab("#000000"));
		assertOklab(new double[] { 1d, 0d, 0d }, CssColorNames.toOklab("#FFFFFF"));
		// reference values of the OKLab definition
		assertOklab(new double[] { 0.62796, 0.22486, 0.12585 }, CssColorNames.toOklab("#FF0000"));
		assertOklab(new double[] { 0.86644, -0.23389, 0.17950 }, CssColorNames.toOklab("#00FF00"));
		assertOklab(new double[] { 0.45201, -0.03246, -0.31153 }, CssColorNames.toOklab("#0000FF"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void oklabInvalid() {
		CssColorNames.toOklab("blue");
	}

	@Test
	public void perceptualDistance() {
		// The same RGB distance, but the change in the blue channel is less visible
		// than the change in the green channel
		double[] gray = CssColorNames.toOklab("#808080");
		double blueChange = CssColorNames.distance(gray, CssColorNames.toOklab("#8080A0"));
		double greenChange = CssColorNames.distance(gray, CssColorNames.toOklab("#80A080"));
		Assert.assertTrue(blueChange < greenChange);
	}

	private static void assertOklab(double[] expected, double[] actual) {
		Assert.assertEquals(3, actual.length);
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("Coordinate " + i, expected[i], actual[i], 0.001);
		}
	}
}
