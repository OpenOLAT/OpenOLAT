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
package org.olat.course.editor.overview;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.config.ui.CourseScoreController;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.overview.OverviewListController.Model;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 17 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewController extends BasicController {

	private OverviewListController overviewListCtrl;

	public OverviewController(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("overview");
		if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
			initInfos(ureq, mainVC, course);
		}
		
		overviewListCtrl = new OverviewListController(ureq, getWindowControl(), course, Model.EDITOR);
		listenTo(overviewListCtrl);
		mainVC.put("list", overviewListCtrl.getInitialComponent());
		
		StackedPanel initialPanel = putInitialPanel(new SimpleStackedPanel("overviewPanel", "o_edit_mode"));
		initialPanel.setContent(mainVC);
	}
	
	private void initInfos(UserRequest ureq, VelocityContainer mainVC, ICourse course) {
		InfoPanel panel = new InfoPanel("assessmentInfos");
		panel.setTitle(translate("overview.description.title"));
		panel.setPersistedStatusId(ureq, "course-infos-overview-v1-" + course.getResourceableId());
		mainVC.put("assessmentInfos", panel);
		
		String orSeparator = " " + translate("or") + " ";
		
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		StringBuilder infos = new StringBuilder();
		infos.append("<p>").append(translate("overview.description.infos")).append("</p><ul><li>");
		
		String scoreKey = moduleConfig.has(STCourseNode.CONFIG_SCORE_KEY)
				? moduleConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY)
				: null;
		String scoreOptions;
		if(STCourseNode.CONFIG_SCORE_VALUE_SUM.equals(scoreKey)) {
			scoreOptions = translate("options.score.points.sum");
		} else if(STCourseNode.CONFIG_SCORE_VALUE_SUM_WEIGHTED.equals(scoreKey)) {
			scoreOptions = translate("options.score.points.sum.weighted");
		} else if(STCourseNode.CONFIG_SCORE_VALUE_AVG.equals(scoreKey)) {
			scoreOptions = translate("options.score.points.average");
		} else {
			scoreOptions = "";
		}
		String scoreI18n = scoreKey == null || CourseScoreController.SCORE_VALUE_NONE.equals(scoreKey)
				? "options.score.disabled" : "options.score.enabled";
		infos.append(translate(scoreI18n, scoreOptions))
			.append("</li><li>");
		
		List<String> options = new ArrayList<>();
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS)) {
			options.add(translate("options.passed.progress"));
		}
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)) {
			options.add(translate("options.passed.all"));
		}
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)) {
			Integer passedNumberCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_NUMBER_CUT)
					? moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_NUMBER_CUT, 1)
					: null;
			if(passedNumberCut == null) {
				options.add(translate("options.passed.number"));
			} else if(passedNumberCut.intValue() <= 1) {
				options.add(translate("options.passed.number.singular", passedNumberCut.toString()));
			} else {
				options.add(translate("options.passed.number.plural", passedNumberCut.toString()));
			}
		}
		if(moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS)) {
			Integer passedPointsCut = moduleConfig.has(STCourseNode.CONFIG_PASSED_POINTS_CUT)
					? moduleConfig.getIntegerSafe(STCourseNode.CONFIG_PASSED_POINTS_CUT, 1)
					: null;
			if(passedPointsCut == null) {
				options.add(translate("options.passed.points"));
			} else if(passedPointsCut.intValue() <= 1) {
				options.add(translate("options.passed.points.singular", passedPointsCut.toString()));
			} else {
				options.add(translate("options.passed.points.plural", passedPointsCut.toString()));
			}
		}
		String passedI18n = options.isEmpty() ? "overview.description.passed.disabled" : "overview.description.passed.enabled";
		String passedOptions = String.join(orSeparator, options);
		infos.append(translate(passedI18n, passedOptions))
			.append("</li></ul>");
		
		panel.setInformations(infos.toString());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == overviewListCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
