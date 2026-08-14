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
package org.olat.core.commons.services.ai.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.essay.AiEssayResponseTruncatedException;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Initial date: 2026-08-13<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class AiEssayGradingServiceImplTest extends OlatTestCase {

	private static final class NullContentChatModel implements ChatModel {
		@Override
		public ChatResponse doChat(ChatRequest chatRequest) {
			return ChatResponse.builder()
					.aiMessage(AiMessage.builder().text(null).build())
					.build();
		}
	}

	private static final class NullContentAiSpi implements AiSPI {
		@Override public String getId() { return "NullContentSpi"; }
		@Override public String getName() { return "NullContentSpi"; }
		@Override public boolean isEnabled() { return true; }
		@Override public void setEnabled(boolean enabled) { /* not needed for this test */ }
		@Override public org.olat.core.gui.control.Controller createAdminController(
				org.olat.core.gui.UserRequest ureq, org.olat.core.gui.control.WindowControl wControl) {
			return null;
		}
		@Override public ChatModel buildChatModel(String modelName, int maxTokens) {
			return new NullContentChatModel();
		}
		@Override public List<String> getAvailableModels() { return List.of(); }
	}

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiEssayGradingServiceImpl aiEssayGradingService;

	@Test
	public void gradeWithLog_emptyModelReply_throwsResponseTruncatedException() {
		aiModule.setSpringProviders(List.of(new NullContentAiSpi()));

		EssayAiGrading grading = new EssayAiGrading();
		grading.setReferenceExcerpt("The mitochondria is the powerhouse of the cell.");
		grading.setModelAnswer("Mitochondria produce ATP through cellular respiration.");

		AiUsageContext usageContext = AiUsageContext.builder().build();

		assertThatThrownBy(() -> aiEssayGradingService.gradeWithLog(
				usageContext, grading, "The mitochondria makes energy.", Locale.ENGLISH, null,
				"NullContentSpi", "fx-reasoning"))
				.isInstanceOf(AiEssayResponseTruncatedException.class);
	}

	@Test
	public void gradeWithLog_emptyModelReply_usageLogFlippedToFailed() {
		aiModule.setSpringProviders(List.of(new NullContentAiSpi()));

		EssayAiGrading grading = new EssayAiGrading();
		grading.setReferenceExcerpt("The mitochondria is the powerhouse of the cell.");
		grading.setModelAnswer("Mitochondria produce ATP through cellular respiration.");

		AiUsageContext usageContext = AiUsageContext.builder().build();

		AiEssayResponseTruncatedException exception = null;
		try {
			aiEssayGradingService.gradeWithLog(
					usageContext, grading, "The mitochondria makes energy.", Locale.ENGLISH, null,
					"NullContentSpi", "fx-reasoning");
		} catch (AiEssayResponseTruncatedException e) {
			exception = e;
		}

		assertThat(exception).isNotNull();
		assertThat(exception.getUsageLogKey()).isNotNull();
	}
}
