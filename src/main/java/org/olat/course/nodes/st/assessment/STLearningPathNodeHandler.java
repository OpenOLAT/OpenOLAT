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
package org.olat.course.nodes.st.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathNodeHandler;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class STLearningPathNodeHandler implements LearningPathNodeHandler {

	private static final STLearningPathConfigs LEARNING_PATH_CONFIGS = new STLearningPathConfigs();

	@Override
	public String acceptCourseNodeType() {
		return STCourseNode.TYPE;
	}

	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public LearningPathConfigs getConfigs(CourseNode courseNode) {
		return LEARNING_PATH_CONFIGS;
	}

	@Override
	public Controller createConfigEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		Translator translator = Util.createPackageTranslator(TabbableLeaningPathNodeConfigController.class, ureq.getLocale());
		return MessageUIFactory.createInfoMessage(ureq, wControl, null, translator.translate("no.configurations"));
	}

}
