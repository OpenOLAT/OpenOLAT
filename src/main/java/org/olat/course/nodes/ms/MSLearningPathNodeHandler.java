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
package org.olat.course.nodes.ms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.model.ModuleLearningPathConfigs;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController.LearningPathControllerConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MSLearningPathNodeHandler implements LearningPathNodeHandler {

	@Override
	public String acceptCourseNodeType() {
		return MSCourseNode.TYPE;
	}

	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public LearningPathConfigs getConfigs(CourseNode courseNode) {
		return new ModuleLearningPathConfigs(courseNode.getModuleConfiguration(), false);
	}

	@Override
	public Controller createConfigEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode) {
		Translator translator = Util.createPackageTranslator(MSEditFormController.class, ureq.getLocale());
		LearningPathControllerConfig ctrlConfig = LearningPathNodeConfigController.builder()
				.enableNodeVisited()
				.enableConfirmed()
				.enableScore()
				.enablePassed()
				.enableStatusDone(translator.translate("fully.assessed.trigger.status.done"))
				.build();
		return new LearningPathNodeConfigController(ureq, wControl, courseEntry, courseNode.getModuleConfiguration(), ctrlConfig);
	}
	
	@Override
	public void onMigrated(CourseNode courseNode) {
		//
	}

}
