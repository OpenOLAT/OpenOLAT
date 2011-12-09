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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.restapi.config;

import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.repository.course.AbstractCourseNodeWebService.CustomConfigDelegate;
import org.olat.restapi.repository.course.config.CustomConfigFactory.ICustomConfigCreator;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.course.nodes.iq.IQEditController;

public class OnyxCustomConfigCreator implements ICustomConfigCreator {
	
	public OnyxCustomConfigCreator() {
		//
	}

	@Override
	public CustomConfigDelegate getTestCustomConfig(RepositoryEntry repoEntry) {
		return new OnyxTestCustomConfig(repoEntry);
	}

	@Override
	public CustomConfigDelegate getSurveyCustomConfig(RepositoryEntry repoEntry) {
		return new OnyxSurveyCustomConfig(repoEntry);
	}
	
	/* CustomConfigDelegate implementations */
	public class OnyxTestCustomConfig implements CustomConfigDelegate {
		private RepositoryEntry testRepoEntry;

		@Override
		public boolean isValid() {
			return testRepoEntry != null;
		}

		public OnyxTestCustomConfig(RepositoryEntry testRepoEntry) {
			this.testRepoEntry = testRepoEntry;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			moduleConfig.set(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY, testRepoEntry.getSoftkey());
			if (OnyxModule.isOnyxTest(testRepoEntry.getOlatResource())) {
				moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
			}

		}
	}

	public class OnyxSurveyCustomConfig implements CustomConfigDelegate {
		private RepositoryEntry surveyRepoEntry;

		public OnyxSurveyCustomConfig(RepositoryEntry surveyRepoEntry) {
			this.surveyRepoEntry = surveyRepoEntry;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void configure(ICourse course, CourseNode newNode, ModuleConfiguration moduleConfig) {
			moduleConfig.set(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY, surveyRepoEntry.getSoftkey());
			moduleConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
			moduleConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
			moduleConfig.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);
			moduleConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
			if (OnyxModule.isOnyxTest(surveyRepoEntry)) {
				moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
			}
		}
	}
}
