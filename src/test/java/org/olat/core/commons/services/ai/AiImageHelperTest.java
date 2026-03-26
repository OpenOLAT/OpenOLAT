/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AiImageHelper}.
 * Tests the MIME type mapping and null handling.
 *
 * Initial date: 19.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class AiImageHelperTest {

	private AiImageHelper helper;

	@Before
	public void setUp() {
		helper = new AiImageHelper();
	}

	@Test
	public void getMimeType_jpg() {
		assertEquals("image/jpeg", helper.getMimeType("jpg"));
	}

	@Test
	public void getMimeType_jpeg() {
		assertEquals("image/jpeg", helper.getMimeType("jpeg"));
	}

	@Test
	public void getMimeType_png() {
		assertEquals("image/png", helper.getMimeType("png"));
	}

	@Test
	public void getMimeType_gif() {
		assertEquals("image/gif", helper.getMimeType("gif"));
	}

	@Test
	public void getMimeType_webp() {
		assertEquals("image/webp", helper.getMimeType("webp"));
	}

	@Test
	public void getMimeType_caseInsensitive() {
		assertEquals("image/jpeg", helper.getMimeType("JPG"));
		assertEquals("image/png", helper.getMimeType("PNG"));
	}

	@Test
	public void getMimeType_unsupported_returnsNull() {
		assertNull(helper.getMimeType("svg"));
		assertNull(helper.getMimeType("bmp"));
		assertNull(helper.getMimeType("tiff"));
	}

	@Test
	public void getMimeType_null_returnsNull() {
		assertNull(helper.getMimeType(null));
	}

	@Test
	public void getMimeType_empty_returnsNull() {
		assertNull(helper.getMimeType(""));
	}

	@Test
	public void prepareImageBase64_nullFile_returnsNull() {
		assertNull(helper.prepareImageBase64(null, "jpg"));
	}

	@Test
	public void prepareImageBase64_nonExistentFile_returnsNull() {
		java.io.File nonExistent = new java.io.File("/tmp/does_not_exist_" + System.nanoTime() + ".jpg");
		assertNull(helper.prepareImageBase64(nonExistent, "jpg"));
	}
}
