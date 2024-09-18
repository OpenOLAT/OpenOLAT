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
package org.olat.modules.ceditor.handler;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ceditor.Assignment;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.ceditor.model.jpa.EvaluationFormPart;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.MultiEvaluationFormController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormHandler implements PageElementHandler {

	@Override
	public String getType() {
		return "evaluationform";
	}

	@Override
	public String getIconCssClass() {
		return null;
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public int getSortOrder() {
		return 0;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints hints) {
		Controller ctrl = null;
		if(element instanceof EvaluationFormPart eva) {
			PageService pageService = CoreSpringFactory.getImpl(PageService.class);
			
			// find assignment
			PageBody body = eva.getBody();
			Assignment assignment = pageService.getAssignment(body);
			if(assignment != null) {
				ctrl = getControllerForAssignment(ureq, wControl, body, assignment, hints.isOnePage());
			}
		}
		
		if(ctrl == null) {
			Translator translator = Util.createPackageTranslator(PortfolioHomeController.class, ureq.getLocale());
			String title = translator.translate("warning.evaluation.not.visible.title");
			String text = translator.translate("warning.evaluation.not.visible.text");
			ctrl = MessageUIFactory.createWarnMessage(ureq, wControl, title, text);
		}
		return new PageRunControllerElement(ctrl);
	}
	

	private Controller getControllerForAssignment(UserRequest ureq, WindowControl wControl, PageBody body, Assignment assignment, boolean onePage) {
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);

		//find the evaluation form
		RepositoryEntry re = assignment.getFormEntry();
		EvaluationFormSurvey survey = portfolioService.loadOrCreateSurvey(body, re);

		Page page = assignment.getPage();
		PageStatus pageStatus = page.getPageStatus();
		
		Controller ctrl = null;
		List<AccessRights> accessRights = portfolioService.getAccessRights(page);
		boolean anonym = assignment.isAnonymousExternalEvaluation();
		if(pageStatus == null || pageStatus == PageStatus.draft) {
			if(hasRole(ContentRoles.owner, ureq.getIdentity(), accessRights)) {
				EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, ureq.getIdentity());
				ctrl = new EvaluationFormExecutionController(ureq, wControl, session, false, true, true, null);
			}
		} else if (assignment.isOnlyAutoEvaluation()) {
			// only the auto evaluation is shown
			if(hasRole(ContentRoles.owner, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
				EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, ureq.getIdentity());
				ctrl =  new EvaluationFormExecutionController(ureq, wControl, session, readOnly, !readOnly, true, null);
			} else if(hasRole(ContentRoles.coach, ureq.getIdentity(), accessRights)
					|| hasRole(ContentRoles.reviewer, ureq.getIdentity(), accessRights)
					|| hasRole(ContentRoles.invitee, ureq.getIdentity(), accessRights)) {
				Identity owner = getOwner(accessRights);
				EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, owner);
				ctrl =  new EvaluationFormExecutionController(ureq, wControl, session, true, false, true, null);
			}
		} else {
			if(hasRole(ContentRoles.owner, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted) || onePage;
				Identity owner = getOwner(accessRights);
				List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
				if(!coachesAndReviewers.isEmpty()) {
					ctrl = new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, survey, false, readOnly, onePage, anonym);
				} else {
					EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, ureq.getIdentity());
					ctrl = new EvaluationFormExecutionController(ureq, wControl, session, readOnly, !readOnly, true, null);
				}
			} else if(hasRole(ContentRoles.coach, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted) || onePage;
				if(assignment.isReviewerSeeAutoEvaluation()) {
					Identity owner = getOwner(accessRights);
					List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
					ctrl = new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, survey, false, readOnly, onePage, anonym);
				} else {
					EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, ureq.getIdentity());
					ctrl = new EvaluationFormExecutionController(ureq, wControl, session, readOnly, !readOnly, true, null);
				}
			} else if(hasRole(ContentRoles.reviewer, ureq.getIdentity(), accessRights)
					|| hasRole(ContentRoles.invitee, ureq.getIdentity(), accessRights)) {
				boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted) || onePage;
				if(assignment.isReviewerSeeAutoEvaluation()) {
					Identity owner = getOwner(accessRights);
					List<Identity> reviewers = Collections.singletonList(ureq.getIdentity());
					ctrl = new MultiEvaluationFormController(ureq, wControl, owner, reviewers, survey, true, readOnly, onePage, anonym);
				} else {
					EvaluationFormSession session = portfolioService.loadOrCreateSession(survey, ureq.getIdentity());
					ctrl = new EvaluationFormExecutionController(ureq, wControl, session, readOnly, !readOnly, true, null);
				}
			}
		}
		return ctrl;
	}

	private Identity getOwner(List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(ContentRoles.owner == accessRight.getRole()) {
				return accessRight.getIdentity();
			}
		}
		return null;
	}
	
	private List<Identity> getCoachesAndReviewers(List<AccessRights> accessRights) {
		List<Identity> identities = new ArrayList<>(accessRights.size());
		for(AccessRights accessRight:accessRights) {
			if(ContentRoles.coach == accessRight.getRole() || ContentRoles.reviewer == accessRight.getRole() || ContentRoles.invitee == accessRight.getRole()) {
				identities.add(accessRight.getIdentity());
			}
		}
		return identities;
	}
	
	private boolean hasRole(ContentRoles role, Identity identity, List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(role == accessRight.getRole() && accessRight.getIdentity() != null && accessRight.getIdentity().equals(identity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof EvaluationFormPart eva) {
			PageService pageService = CoreSpringFactory.getImpl(PageService.class);
			
			PageBody body = eva.getBody();
			Assignment assignment = pageService.getAssignment(body);
			
			//find the evaluation form
			EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
			RepositoryEntry re = assignment.getFormEntry();
			if(re == null) {
				Translator translator = Util.createPackageTranslator(PortfolioHomeController.class, ureq.getLocale());
				String title = translator.translate("error.form.missing.title");
				String text = translator.translate("error.form.missing.description");	
				return MessageUIFactory.createErrorMessage(ureq, wControl, title, text);
			} else {
				File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
				File formFile = new File(repositoryDir, FORM_XML_FILE);
				DataStorage storage = evaluationFormManager.loadStorage(re);
				return new EvaluationFormExecutionController(ureq, wControl, formFile, storage, null);
			}
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}
}
