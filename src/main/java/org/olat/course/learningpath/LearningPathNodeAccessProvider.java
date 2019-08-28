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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.learningpath.manager.UnsupportedLearningPathNodeHandler;
import org.olat.course.learningpath.ui.LeaningPathNodeConfigController;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LearningPathNodeAccessProvider implements NodeAccessProvider {

	private static final String UNSUPPORTED_LEARNING_PATH_TYPE = UnsupportedLearningPathNodeHandler.NODE_TYPE;
	
	@Autowired
	private List<LearningPathNodeHandler> loadedLearningPathNodeHandlers;
	private Map<String, LearningPathNodeHandler> learningPathNodeHandlers = new HashMap<>();
	private LearningPathNodeHandler nonLearningPathNodeHandler;
	
	@PostConstruct
	void initProviders() {
		for (LearningPathNodeHandler handler: loadedLearningPathNodeHandlers) {
			if (UNSUPPORTED_LEARNING_PATH_TYPE.equals(handler.acceptCourseNodeType())) {
				nonLearningPathNodeHandler = handler;
			} else {
				learningPathNodeHandlers.put(handler.acceptCourseNodeType(), handler);
			}
		}
	}
	
	private  LearningPathNodeHandler getLearningPathNodeHandler(String courseNodeType) {
		LearningPathNodeHandler handler = learningPathNodeHandlers.get(courseNodeType);
		if (handler == null) {
			handler = nonLearningPathNodeHandler;
		}
		return handler;
	}

	private  LearningPathNodeHandler getLearningPathNodeHandler(CourseNode courseNode) {
		return getLearningPathNodeHandler(courseNode.getType());
	}
	
	@Override
	public String getType() {
		return "learningpath";
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(LeaningPathNodeConfigController.class, locale);
		return translator.translate("access.provider.name");
	}

	@Override
	public boolean isSupported(String courseNodeType) {
		return getLearningPathNodeHandler(courseNodeType).isSupported();
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		Controller configCtrl = getLearningPathNodeHandler(courseNode).createEditController(ureq, wControl, courseNode);
		return new TabbableLeaningPathNodeConfigController(ureq, wControl, configCtrl);
	}

}
