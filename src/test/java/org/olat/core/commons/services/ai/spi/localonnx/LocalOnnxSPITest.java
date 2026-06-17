/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.spi.localonnx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;

/**
 * Unit tests for {@link LocalOnnxSPI}.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class LocalOnnxSPITest {

	@Test
	public void shouldReportNotEnabledWhenNoModel() {
		TaxonomyMatchingModule module = mock(TaxonomyMatchingModule.class);
		when(module.getLocalModelDir()).thenReturn("/tmp/nonexistent-models-dir");
		when(module.getModel()).thenReturn("some-model");

		LocalOnnxSPI spi = new LocalOnnxSPI();
		injectField(spi, "matchingModule", module);

		assertThat(spi.isEmbeddingEnabled()).isFalse();
	}

	@Test
	public void shouldReportNotEnabledWhenDirIsBlank() {
		TaxonomyMatchingModule module = mock(TaxonomyMatchingModule.class);
		when(module.getLocalModelDir()).thenReturn("");
		when(module.getModel()).thenReturn("some-model");

		LocalOnnxSPI spi = new LocalOnnxSPI();
		injectField(spi, "matchingModule", module);

		assertThat(spi.isEmbeddingEnabled()).isFalse();
	}

	@Test
	public void shouldStripPathTraversalCharacters() {
		assertThat(LocalOnnxSPI.sanitize("../../../etc")).isEqualTo("etc");
		assertThat(LocalOnnxSPI.sanitize("foo/bar/baz")).isEqualTo("foobarbaz");
		assertThat(LocalOnnxSPI.sanitize("my.model")).isEqualTo("mymodel");
	}

	@Test
	public void shouldPreserveAllowedCharacters() {
		assertThat(LocalOnnxSPI.sanitize("multilingual-e5-small_int8")).isEqualTo("multilingual-e5-small_int8");
		assertThat(LocalOnnxSPI.sanitize("Qwen3-Embedding-8B")).isEqualTo("Qwen3-Embedding-8B");
	}

	@Test
	public void shouldReturnEmptyForNullOrBlankName() {
		assertThat(LocalOnnxSPI.sanitize(null)).isEmpty();
		assertThat(LocalOnnxSPI.sanitize("...")).isEmpty();
	}

	private void injectField(Object target, String fieldName, Object value) {
		try {
			var field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
