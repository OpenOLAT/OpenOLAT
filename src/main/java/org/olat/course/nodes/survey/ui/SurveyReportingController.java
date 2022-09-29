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
package org.olat.course.nodes.survey.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyManager;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.FiguresBuilder;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormReportsController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyReportingController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private EvaluationFormReportsController reportsCtrl;

	@Autowired
	private SurveyManager surveyManager;

	public SurveyReportingController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			SurveyCourseNode courseNode, EvaluationFormSurvey survey) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("reporting");

		Form form = surveyManager.loadForm(survey);
		DataStorage storage = surveyManager.loadStorage(survey);
		SessionFilter filter = SessionFilterFactory.createSelectDone(survey);
		
		Figures figures = FiguresBuilder.builder()
				.addCustomFigure(translate("figure.course"), courseEntry.getDisplayname())
				.addCustomFigure(translate("figure.course.node"), courseNode.getShortTitle())
				.build();
		reportsCtrl = new EvaluationFormReportsController(ureq, wControl, form, storage, filter, figures);
		listenTo(reportsCtrl);
		mainVC.put("report", reportsCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == reportsCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
