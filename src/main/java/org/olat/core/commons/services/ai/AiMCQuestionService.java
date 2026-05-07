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

import java.util.List;

import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;

/**
 * Spring service for multiple choice question generation via AI.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface AiMCQuestionService {
	
	boolean isEnabled();

	default AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number) {
		return generateMCQuestionsResponse(usageContext, input, number, null, null, List.of());
	}

	AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number,
			List<AiBloomLevel> bloomLevels, Integer targetDifficulty, List<String> learningObjectives);

	AiMCQuestionsResponse generateMCQuestionsResponse(AiUsageContext usageContext, String input, int number, String spiId, String modelName);

}
