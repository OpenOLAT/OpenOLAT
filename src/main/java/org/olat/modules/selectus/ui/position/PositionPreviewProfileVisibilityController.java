/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingPositionSecurityCallbackForReviewer;
import org.olat.modules.selectus.ui.feedback.appsfeedback.ApplicationMemberFeedbackDetailsController;
import org.olat.modules.selectus.ui.position.model.EditVisibilityStepSettings;
import org.olat.modules.selectus.ui.reference.ReferenceApplicationController;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;

/**
 * 
 * Initial date: 19 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionPreviewProfileVisibilityController extends BasicController {
	
	private final TabbedPane tabPane;

	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionPreviewProfileVisibilityController(UserRequest ureq, WindowControl wControl,
			Position position, List<ApplicationsFeedbackConfiguration> configurations, EditVisibilityStepSettings settings) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		tabPane = new TabbedPane("previewTabPane", getLocale());
		tabPane.setHideDisabledTab(true);
		tabPane.addListener(this);
		
		Application app = ReferenceHelper.generateDummyApplicationExtended(position);
		
		if(recruitingModule.isReferenceEnabled()) {
			if( position.isRefereeRecommendationEnabled()) {
				RecruitingPositionSecurityCallback refereesSecCallback = RecruitingPositionSecurityCallbackForReviewer
						.valueOf(settings.getRefereesFields(), settings.getRefereesDocuments());
				ReferenceApplicationController refereesCtrl = new ReferenceApplicationController(ureq, getWindowControl(),
						position, app, refereesSecCallback, true);
				listenTo(refereesCtrl);
				tabPane.addTab(translate("edit.step.referees"), refereesCtrl);
			}
			
			if(position.isExpertRecommendationEnabled()) {
				RecruitingPositionSecurityCallback expertsSecCallback = RecruitingPositionSecurityCallbackForReviewer
						.valueOf(settings.getExpertsFields(), settings.getExpertsDocuments());
				ReferenceApplicationController expertsCtrl = new ReferenceApplicationController(ureq, getWindowControl(),
						position, app, expertsSecCallback, true);
				listenTo(expertsCtrl);
				tabPane.addTab(translate("edit.step.experts"), expertsCtrl);
			}
			
			if(recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
				RecruitingPositionSecurityCallback comparativeExpertsSecCallback = RecruitingPositionSecurityCallbackForReviewer
						.valueOf(settings.getComparativeExpertFields(), settings.getComparativeExpertDocuments());
				ReferenceApplicationController comparativeExpertsCtrl = new ReferenceApplicationController(ureq, getWindowControl(),
						position, app, comparativeExpertsSecCallback, true);
				listenTo(comparativeExpertsCtrl);
				tabPane.addTab(translate("edit.step.comparative.experts"), comparativeExpertsCtrl);
			}
		}

		if(configurations != null && !configurations.isEmpty() && recruitingModule.isMembersFeedbackEnabled()) {
			int numOfConfigurations = configurations.size();
			for(int i=0; i<numOfConfigurations; i++) {
				ApplicationsFeedbackConfiguration configuration = configurations.get(i);
				if(configuration.isEnabled()) {
					List<String> fields = settings.getFacultyMembersFields(i);
					List<String> documents = settings.getFacultyMembersDocuments(i);
					RecruitingPositionSecurityCallback configSecCallback = RecruitingPositionSecurityCallbackForReviewer
							.membersFeedback(fields, documents, settings.getFacultyMembersExpertsDocuments(i),
									settings.getFacultyMembersRefereesDocuments(i), settings.getFacultyMembersExpertsComparativeAssessmentsDocuments(i));
					ApplicationMemberFeedbackDetailsController configCtrl = new ApplicationMemberFeedbackDetailsController(ureq, getWindowControl(),
							position, app, configSecCallback, true);
					listenTo(configCtrl);
					tabPane.addTab(configuration.getConfigurationName(), configCtrl);
				}
			}
		}
		
		if(recruitingModule.isPublicFeedbackEnabled() && position.isPublicFeedbackEnabled()) {
			Set<String> minimalSets = new HashSet<>();
			minimalSets.add(RecruitingModule.APP_PERSON_TITLE);
			minimalSets.add(RecruitingModule.APP_PERSON_FIRSTNAME);
			minimalSets.add(RecruitingModule.APP_PERSON_LASTNAME);

			RecruitingPositionSecurityCallback publicFeedbackSecCallback = RecruitingPositionSecurityCallbackForReviewer.valueOf(minimalSets, new HashSet<>());
			ReferenceApplicationController publicFeedbackCtrl = new ReferenceApplicationController(ureq, getWindowControl(),
					position, app, publicFeedbackSecCallback, true);
			listenTo(publicFeedbackCtrl);
			tabPane.addTab(translate("edit.step.public.feedback"), publicFeedbackCtrl);
		}

		putInitialPanel(tabPane);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
