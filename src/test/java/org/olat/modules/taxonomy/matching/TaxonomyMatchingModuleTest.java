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
package org.olat.modules.taxonomy.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * Unit tests for prefix resolution in {@link TaxonomyMatchingModule}.
 *
 * Initial date: 2026-06-10<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyMatchingModuleTest {

	private TaxonomyMatchingModule module(String model, String queryOverride, String passageOverride) throws Exception {
		TaxonomyMatchingModule m = new TaxonomyMatchingModule(null);
		setField(m, "model", model);
		setField(m, "queryPrefixOverride", queryOverride);
		setField(m, "passagePrefixOverride", passageOverride);
		return m;
	}

	private static void setField(Object target, String name, String value) throws Exception {
		Field f = TaxonomyMatchingModule.class.getDeclaredField(name);
		f.setAccessible(true);
		f.set(target, value);
	}

	@Test
	public void noModel_emptyPrefixes() throws Exception {
		TaxonomyMatchingModule m = module("", "", "");
		assertThat(m.getQueryPrefix()).isEmpty();
		assertThat(m.getPassagePrefix()).isEmpty();
	}

	@Test
	public void e5Model_queryAndPassagePrefix() throws Exception {
		TaxonomyMatchingModule m = module("multilingual-e5-large", "", "");
		assertThat(m.getQueryPrefix()).isEqualTo("query: ");
		assertThat(m.getPassagePrefix()).isEqualTo("passage: ");
	}

	@Test
	public void nomicModel_queryAndPassagePrefix() throws Exception {
		TaxonomyMatchingModule m = module("nomic-embed-text-v1.5", "", "");
		assertThat(m.getQueryPrefix()).isEqualTo("search_query: ");
		assertThat(m.getPassagePrefix()).isEqualTo("search_document: ");
	}

	@Test
	public void qwenModel_instructOnQueryOnly() throws Exception {
		TaxonomyMatchingModule m = module("Qwen3-Embedding-8B", "", "");
		assertThat(m.getQueryPrefix()).isEqualTo(
				"Instruct: Given a topic, retrieve the most relevant taxonomy level\nQuery: ");
		assertThat(m.getPassagePrefix()).isEmpty();
	}

	@Test
	public void qwenModel_caseInsensitive() throws Exception {
		TaxonomyMatchingModule m = module("qwen3-embedding-0.6b", "", "");
		assertThat(m.getQueryPrefix()).startsWith("Instruct:");
		assertThat(m.getPassagePrefix()).isEmpty();
	}

	@Test
	public void bgeM3Model_emptyPrefixes() throws Exception {
		TaxonomyMatchingModule m = module("BAAI/bge-m3", "", "");
		assertThat(m.getQueryPrefix()).isEmpty();
		assertThat(m.getPassagePrefix()).isEmpty();
	}

	@Test
	public void override_winsOverAutoDetect() throws Exception {
		TaxonomyMatchingModule m = module("Qwen3-Embedding-8B", "custom-query: ", "custom-passage: ");
		assertThat(m.getQueryPrefix()).isEqualTo("custom-query: ");
		assertThat(m.getPassagePrefix()).isEqualTo("custom-passage: ");
	}
}
