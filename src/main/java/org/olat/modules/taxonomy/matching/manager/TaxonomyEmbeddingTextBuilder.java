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
package org.olat.modules.taxonomy.matching.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * Builds the embedding text for a taxonomy level in a given locale and form.
 * <p>
 * Three forms are supported:
 * <ul>
 *   <li>{@link TaxonomyEmbeddingTextVariant#NAME} — translated leaf displayName only</li>
 *   <li>{@link TaxonomyEmbeddingTextVariant#PATH_NAME} — ancestor path + leaf displayName, comma-separated</li>
 *   <li>{@link TaxonomyEmbeddingTextVariant#FULL} — PATH_NAME + description (omitted when description is blank)</li>
 * </ul>
 * Returns {@code null} when the form produces no useful text (blank name for NAME;
 * blank description for FULL — the caller should skip indexing that row).
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyEmbeddingTextBuilder {

	private TaxonomyEmbeddingTextBuilder() {
	}

	public static String build(TaxonomyLevel level, Translator translator) {
		return build(level, translator, null, TaxonomyEmbeddingTextVariant.FULL);
	}

	public static String build(TaxonomyLevel level, Translator translator, Map<Long, TaxonomyLevel> levelMap) {
		return build(level, translator, levelMap, TaxonomyEmbeddingTextVariant.FULL);
	}

	public static String build(TaxonomyLevel level, Translator translator, Map<Long, TaxonomyLevel> levelMap,
			TaxonomyEmbeddingTextVariant textVariant) {
		String displayName = TaxonomyUIFactory.translateDisplayName(translator, level);
		if (!StringHelper.containsNonWhitespace(displayName)) {
			displayName = level.getIdentifier();
		}

		if (textVariant == TaxonomyEmbeddingTextVariant.NAME) {
			return StringHelper.containsNonWhitespace(displayName) ? displayName : null;
		}

		String pathText = buildPathText(level, translator, displayName, levelMap);

		if (textVariant == TaxonomyEmbeddingTextVariant.PATH_NAME) {
			return pathText;
		}

		String description = TaxonomyUIFactory.translateDescription(translator, level);
		if (!StringHelper.containsNonWhitespace(description)) {
			return null;
		}
		return pathText + ". " + description;
	}

	private static String buildPathText(TaxonomyLevel level, Translator translator, String displayName,
			Map<Long, TaxonomyLevel> levelMap) {
		List<String> parentNames = new ArrayList<>();

		if (levelMap != null) {
			String pathKeys = level.getMaterializedPathKeys();
			if (StringHelper.containsNonWhitespace(pathKeys)) {
				String[] keys = pathKeys.split("/");
				for (String key : keys) {
					if (!StringHelper.containsNonWhitespace(key)) {
						continue;
					}
					try {
						Long parentKey = Long.parseLong(key.trim());
						if (parentKey.equals(level.getKey())) {
							continue;
						}
						TaxonomyLevel parent = levelMap.get(parentKey);
						if (parent != null) {
							String parentName = TaxonomyUIFactory.translateDisplayName(translator, parent);
							if (!StringHelper.containsNonWhitespace(parentName)) {
								parentName = parent.getIdentifier();
							}
							parentNames.add(parentName);
						}
					} catch (NumberFormatException e) {
						// skip non-numeric path segments
					}
				}
			}
		} else {
			TaxonomyLevel parent = level.getParent();
			while (parent != null) {
				String parentName = TaxonomyUIFactory.translateDisplayName(translator, parent);
				if (!StringHelper.containsNonWhitespace(parentName)) {
					parentName = parent.getIdentifier();
				}
				parentNames.add(parentName);
				parent = parent.getParent();
			}
			Collections.reverse(parentNames);
		}

		if (parentNames.isEmpty()) {
			return displayName;
		}

		StringBuilder sb = new StringBuilder();
		for (String part : parentNames) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(part);
		}
		sb.append(", ").append(displayName);
		return sb.toString();
	}
}
