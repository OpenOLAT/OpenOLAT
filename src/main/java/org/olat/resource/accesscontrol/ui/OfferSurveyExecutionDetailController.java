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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.forms.CoachCandidates;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.user.UserPropertiesInfoController;

/**
 *
 * Initial date: 5 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyExecutionDetailController extends BasicController {

	private final EvaluationFormExecutionController executionCtrl;

	public OfferSurveyExecutionDetailController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			EvaluationFormSurvey survey, String offerLabel, String orderNr, Date submissionDate,
			EvaluationFormParticipationStatus status, Identity executor,
			boolean readOnly, boolean allowEditDoneSessions, boolean showDoneButton, boolean doneSavesOnly) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("offer_survey_execution_detail");
		putInitialPanel(mainVC);

		UserPropertiesInfoController userCtrl = new UserPropertiesInfoController(ureq, wControl, executor);
		listenTo(userCtrl);
		mainVC.put("user", userCtrl.getInitialComponent());

		OfferSurveyExecutionInfoCardController infoCtrl = new OfferSurveyExecutionInfoCardController(ureq, wControl,
				survey, offerLabel, orderNr, submissionDate, status);
		listenTo(infoCtrl);
		mainVC.put("implementation", infoCtrl.getInitialComponent());

		executionCtrl = new EvaluationFormExecutionController(ureq, wControl, session, CoachCandidates.NONE, readOnly,
				allowEditDoneSessions, showDoneButton, doneSavesOnly, null);
		listenTo(executionCtrl);
		mainVC.put("evaluationForm", executionCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
