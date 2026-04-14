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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for PresetGeometryPath utility.
 *
 * @author gnaegi, https://www.frentix.com
 */
public class PresetGeometryPathTest {

	private static final int CX = 500000;
	private static final int CY = 300000;

	// --- getPath tests ---

	@Test
	public void getPathRect() {
		String path = PresetGeometryPath.getPath("rect", CX, CY, null);
		assertNotNull("rect path must not be null", path);
		assertFalse("rect path must not be empty", path.isEmpty());
		assertTrue("rect path must contain M", path.contains("M"));
		assertTrue("rect path must be a closed path (Z)", path.contains("Z"));
	}

	@Test
	public void getPathEllipse() {
		String path = PresetGeometryPath.getPath("ellipse", CX, CY, null);
		assertNotNull("ellipse path must not be null", path);
		assertFalse("ellipse path must not be empty", path.isEmpty());
		assertTrue("ellipse path must contain arc command (A)", path.contains("A"));
	}

	@Test
	public void getPathDiamond() {
		String path = PresetGeometryPath.getPath("diamond", CX, CY, null);
		assertNotNull("diamond path must not be null", path);
		assertFalse("diamond path must not be empty", path.isEmpty());
		assertTrue("diamond path must contain M", path.contains("M"));
		assertTrue("diamond path must contain L or Z", path.contains("L") || path.contains("Z"));
	}

	@Test
	public void getPathChevron() {
		String path = PresetGeometryPath.getPath("chevron", CX, CY, null);
		assertNotNull("chevron path must not be null", path);
		assertFalse("chevron path must not be empty", path.isEmpty());
		assertTrue("chevron path must contain M", path.contains("M"));
	}

	@Test
	public void getPathWithAdjustments() {
		String defaultPath = PresetGeometryPath.getPath("roundRect", CX, CY, null);
		Map<String, Integer> adjustments = new HashMap<>();
		adjustments.put("adj", 50000);
		String customPath = PresetGeometryPath.getPath("roundRect", CX, CY, adjustments);
		assertNotNull("roundRect default path must not be null", defaultPath);
		assertNotNull("roundRect custom path must not be null", customPath);
		assertNotEquals("roundRect with custom adj must differ from default", defaultPath, customPath);
	}

	@Test
	public void getPathRightArrow() {
		String path = PresetGeometryPath.getPath("rightArrow", CX, CY, null);
		assertNotNull("rightArrow path must not be null", path);
		assertFalse("rightArrow path must not be empty", path.isEmpty());
		assertTrue("rightArrow path must contain M", path.contains("M"));
	}

	@Test
	public void getPathStar5() {
		String path = PresetGeometryPath.getPath("star5", CX, CY, null);
		assertNotNull("star5 path must not be null", path);
		assertFalse("star5 path must not be empty", path.isEmpty());
		assertTrue("star5 path must contain M", path.contains("M"));
		assertTrue("star5 path must contain L or Z", path.contains("L") || path.contains("Z"));
	}

	@Test
	public void getPathFlowChartProcess() {
		String rectPath = PresetGeometryPath.getPath("rect", CX, CY, null);
		String flowPath = PresetGeometryPath.getPath("flowChartProcess", CX, CY, null);
		assertNotNull("flowChartProcess path must not be null", flowPath);
		assertEquals("flowChartProcess must map to same path as rect", rectPath, flowPath);
	}

	@Test
	public void getPathFlowChartDecision() {
		String diamondPath = PresetGeometryPath.getPath("diamond", CX, CY, null);
		String flowPath = PresetGeometryPath.getPath("flowChartDecision", CX, CY, null);
		assertNotNull("flowChartDecision path must not be null", flowPath);
		assertEquals("flowChartDecision must map to same path as diamond", diamondPath, flowPath);
	}

	@Test
	public void getPathUnknown() {
		String path = PresetGeometryPath.getPath("unknownShapeXYZ", CX, CY, null);
		assertNull("Unknown preset must return null", path);
	}

	@Test
	public void getPathInvalidDimensions() {
		assertNull("cx=0 must return null", PresetGeometryPath.getPath("rect", 0, CY, null));
		assertNull("cy=0 must return null", PresetGeometryPath.getPath("rect", CX, 0, null));
		assertNull("cx<0 must return null", PresetGeometryPath.getPath("rect", -1, CY, null));
		assertNull("cy<0 must return null", PresetGeometryPath.getPath("rect", CX, -1, null));
	}

	@Test
	public void getPathNullAdjustments() {
		String pathNull = PresetGeometryPath.getPath("roundRect", CX, CY, null);
		String pathEmpty = PresetGeometryPath.getPath("roundRect", CX, CY, new HashMap<>());
		assertNotNull("roundRect with null adjustments must return path", pathNull);
		assertNotNull("roundRect with empty adjustments must return path", pathEmpty);
		assertEquals("null and empty adjustments must produce same path", pathNull, pathEmpty);
	}

	@Test
	public void getPathHeptagon() {
		String path = PresetGeometryPath.getPath("heptagon", CX, CY, null);
		assertNotNull("heptagon path must not be null", path);
		assertFalse("heptagon path must not be empty", path.isEmpty());
		assertTrue("heptagon path must contain M", path.contains("M"));
		assertTrue("heptagon path must contain L or Z", path.contains("L") || path.contains("Z"));
	}

	@Test
	public void getPathGear6() {
		String path = PresetGeometryPath.getPath("gear6", CX, CY, null);
		assertNotNull("gear6 path must not be null", path);
		assertFalse("gear6 path must not be empty", path.isEmpty());
		assertTrue("gear6 path must contain M", path.contains("M"));
	}

	@Test
	public void getPathMoon() {
		String path = PresetGeometryPath.getPath("moon", CX, CY, null);
		assertNotNull("moon path must not be null", path);
		assertFalse("moon path must not be empty", path.isEmpty());
		assertTrue("moon path must contain M", path.contains("M"));
	}

	@Test
	public void getPathBevel() {
		String path = PresetGeometryPath.getPath("bevel", CX, CY, null);
		assertNotNull("bevel path must not be null", path);
		assertFalse("bevel path must not be empty", path.isEmpty());
		assertTrue("bevel path must contain M", path.contains("M"));
		assertTrue("bevel path must contain L or Z", path.contains("L") || path.contains("Z"));
	}

	// --- isEvenOddShape tests ---

	@Test
	public void isEvenOddDonut() {
		assertTrue("donut must be an even-odd shape", PresetGeometryPath.isEvenOddShape("donut"));
	}

	@Test
	public void isEvenOddFrame() {
		assertTrue("frame must be an even-odd shape", PresetGeometryPath.isEvenOddShape("frame"));
	}

	@Test
	public void isEvenOddRect() {
		assertFalse("rect must not be an even-odd shape", PresetGeometryPath.isEvenOddShape("rect"));
	}
}
