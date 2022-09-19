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
package org.olat.course.learningpath;

/**
 * 
 * Initial date: 20 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathEditConfigsBuilder {
	
	private boolean triggerNodeVisited;
	private boolean triggerConfirmed;
	private boolean triggerScore;
	private boolean triggerPassed;
	private boolean triggerStatusInReview;
	private boolean triggerStatusDone;
	private boolean triggerNodeCompleted;
	private LearningPathTranslationsBuilder translationsBuilder;
	
	LearningPathEditConfigsBuilder() {
		this.translationsBuilder = new LearningPathTranslationsBuilder(this);
	}
	
	public LearningPathEditConfigsBuilder enableNodeVisited() {
		triggerNodeVisited = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enableConfirmed() {
		triggerConfirmed = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enableScore() {
		triggerScore = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enablePassed() {
		triggerPassed = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enableStatusInReview() {
		triggerStatusInReview = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enableStatusDone() {
		triggerStatusDone = true;
		return this;
	}
	
	public LearningPathEditConfigsBuilder enableNodeCompleted() {
		triggerNodeCompleted = true;
		return this;
	}
	
	public LearningPathTranslationsBuilder withTranslations(Class<?> translatorBaseClass) {
		this.translationsBuilder = LearningPathTranslations.builder(this)
				.withTranslatorBaseClass(translatorBaseClass);
		return this.translationsBuilder;
	}
	
	public LearningPathEditConfigs build() {
		return new LearningPathEditConfigsImpl(this);
	}
	
	private final static class LearningPathEditConfigsImpl implements LearningPathEditConfigs {
		
		private final boolean triggerNodeVisited;
		private final boolean triggerConfirmed;
		private final boolean triggerScore;
		private final boolean triggerPassed;
		private final boolean triggerStatusInReview;
		private final boolean triggerStatusDone;
		private final boolean triggerNodeCompleted;
		private final LearningPathTranslations translations;

		private LearningPathEditConfigsImpl(LearningPathEditConfigsBuilder builder) {
			this.triggerNodeVisited = builder.triggerNodeVisited;
			this.triggerConfirmed = builder.triggerConfirmed;
			this.triggerScore = builder.triggerScore;
			this.triggerPassed = builder.triggerPassed;
			this.triggerStatusInReview = builder.triggerStatusInReview;
			this.triggerStatusDone = builder.triggerStatusDone;
			this.triggerNodeCompleted = builder.triggerNodeCompleted;
			this.translations = builder.translationsBuilder.build();
		}

		@Override
		public boolean isTriggerNodeVisited() {
			return triggerNodeVisited;
		}

		@Override
		public boolean isTriggerConfirmed() {
			return triggerConfirmed;
		}

		@Override
		public boolean isTriggerScore() {
			return triggerScore;
		}

		@Override
		public boolean isTriggerPassed() {
			return triggerPassed;
		}

		@Override
		public boolean isTriggerStatusInReview() {
			return triggerStatusInReview;
		}

		@Override
		public boolean isTriggerStatusDone() {
			return triggerStatusDone;
		}

		@Override
		public boolean isTriggerNodeCompleted() {
			return triggerNodeCompleted;
		}

		@Override
		public LearningPathTranslations getTranslations() {
			return translations;
		}
		
	}

}
