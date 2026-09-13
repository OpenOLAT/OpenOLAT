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
package org.olat.core.commons.services.ai;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: Apr 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public enum AiFeature {
	
	MCQuestionGenerator("mc-question-generator", false),
	ImageDescriptionGenerator("image-description-generator", true),
	EssayGeneration("essay-generation", false),
	EssayGrading("essay-grading", true),
	TaxonomyMatching("taxonomy-matching", false)
	;
	
	public static List<AiFeature> VALUES = List.of(values());
	
	private final String type;
	private final boolean userControlled;
	
	private AiFeature(String type, boolean userControlled) {
		this.type = type;
		this.userControlled = userControlled;
	}

	public String getType() {
		return type;
	}
	
	/**
	 * @return true: a person can switch this feature on or off in the user
	 *         settings, on top of the system default of the administrator
	 */
	public boolean isUserControlled() {
		return userControlled;
	}

	public String getI18nKey() {
		return "ai.feature." + type;
	}
	
	/**
	 * @return the key of the one-sentence description of this feature
	 */
	public String getI18nDescriptionKey() {
		return getI18nKey() + ".desc";
	}

	/**
	 * @return the key of the learner-facing name of this feature. The name of
	 *         {@link #getI18nKey()} names the tool and is used in the administration.
	 */
	public String getI18nUserNameKey() {
		return getI18nKey() + ".user";
	}

	private final static Map<String, AiFeature> typeToEnum = List.of(values()).stream()
			.collect(Collectors.toMap(AiFeature::getType, Function.identity()));

	public static final AiFeature ofType(String type) {
		return typeToEnum.getOrDefault(type, null);
	}

}
