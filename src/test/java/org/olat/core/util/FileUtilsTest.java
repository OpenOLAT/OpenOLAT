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
package org.olat.core.util;

import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 26.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileUtilsTest {
	
	@Test
	public void normalizeFilename_umlaut() {
		String normalizedUmlaut = FileUtils.normalizeFilename("S\u00E4l\u00FCt toi");
		Assert.assertEquals(normalizedUmlaut, "Saeluet_toi");
		
		String normalizedAccents = FileUtils.normalizeFilename("Dépéchons-nous!");
		Assert.assertEquals(normalizedAccents, "Depechonsnous");
	}
	
	@Test
	public void normalizedFilename_danischDish() {
		String smorrebrod = "Sm\u2205rrebr\u00F8d";
		String normalized = FileUtils.normalizeFilename(smorrebrod);
		Assert.assertEquals(normalized, "Smorrebrod");
	}
	
	@Test
	public void normalizeFilename_atLeastOneChar() {
		String cyrillic = "и́мя фа́йла";
		String normalized = FileUtils.normalizeFilename(cyrillic);
		Assert.assertEquals(normalized, "_");
	}
	
	@Test
	public void cleanedFilename() {
		assertCleanedFilename("test.xml", "test.xml");
		assertCleanedFilename("abc/abc", "abc_abc");
		assertCleanedFilename("abc\\abc", "abc_abc");
		assertCleanedFilename("qwe\nqwe", "qwe_qwe");
		assertCleanedFilename("wer\rwer", "wer_wer");
		assertCleanedFilename("ert\tert", "ert_ert");
		assertCleanedFilename("rtz\frtz", "rtz_rtz");
		assertCleanedFilename("tzu'tzu", "tzu'tzu");
		assertCleanedFilename("zui?zui", "zui_zui");
		assertCleanedFilename("uio*uio", "uio_uio");
		assertCleanedFilename("asd<asd", "asd_asd");
		assertCleanedFilename("sdf>sdf", "sdf_sdf");
		assertCleanedFilename("dfg|dfg", "dfg_dfg");
		assertCleanedFilename("fgh\"fgh", "fgh_fgh");
		assertCleanedFilename("fgh:ghj", "fgh_ghj");
		assertCleanedFilename("fgh,ghj", "fgh_ghj");
		assertCleanedFilename("fgh=ghj", "fgh_ghj");
	}

	private void assertCleanedFilename(String raw, String expected) {
		String cleaned = FileUtils.cleanFilename(raw);
		Assert.assertEquals(expected, cleaned);
	}

	@Test
	public void testMetaFiles() {
		Assert.assertFalse(FileUtils.isMetaFilename(null));
		Assert.assertFalse(FileUtils.isMetaFilename(""));
		Assert.assertFalse(FileUtils.isMetaFilename("gugus"));
		Assert.assertFalse(FileUtils.isMetaFilename(".Jüdelidü"));
		Assert.assertFalse(FileUtils.isMetaFilename("./dings"));
		
		Assert.assertTrue(FileUtils.isMetaFilename(".DS_Store"));
		Assert.assertTrue(FileUtils.isMetaFilename(".CVS"));
		Assert.assertTrue(FileUtils.isMetaFilename(".nfs"));
		Assert.assertTrue(FileUtils.isMetaFilename(".sass-cache"));
		Assert.assertTrue(FileUtils.isMetaFilename(".hg"));

		Assert.assertTrue(FileUtils.isMetaFilename("._"));
		Assert.assertTrue(FileUtils.isMetaFilename("._gugus"));
	}
	
	@Test
	public void testInsertBeforeSuffix() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(FileUtils.insertBeforeSuffix("test.second.html", "_copy")).isEqualTo("test.second_copy.html");
		softly.assertThat(FileUtils.insertBeforeSuffix("test.html", "_copy")).isEqualTo("test_copy.html");
		softly.assertThat(FileUtils.insertBeforeSuffix("test.html", "")).isEqualTo("test.html");
		softly.assertThat(FileUtils.insertBeforeSuffix("test.html", null)).isEqualTo("test.html");
		softly.assertThat(FileUtils.insertBeforeSuffix("test", "_copy")).isEqualTo("test_copy");
		softly.assertThat(FileUtils.insertBeforeSuffix("", "_copy")).isEqualTo("_copy");
		softly.assertThat(FileUtils.insertBeforeSuffix(null, "_copy")).isEqualTo("_copy");
		softly.assertThat(FileUtils.insertBeforeSuffix("", "")).isEqualTo("");
		softly.assertThat(FileUtils.insertBeforeSuffix("test.", "_copy")).isEqualTo("test_copy.");
		softly.assertThat(FileUtils.insertBeforeSuffix(null, null)).isEqualTo(null);
		softly.assertAll();
	}

}
