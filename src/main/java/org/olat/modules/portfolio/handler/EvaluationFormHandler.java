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
package org.olat.modules.portfolio.handler;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.ui.EvaluationFormController;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.EvaluationFormPart;
import org.olat.modules.portfolio.ui.MultiEvaluationFormController;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
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
	public Component getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof EvaluationFormPart) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			
			// find assignment
			EvaluationFormPart eva = (EvaluationFormPart)element;
			PageBody body = eva.getBody();
			Assignment assignment = portfolioService.getAssignment(body);
			//find the evaluation form
			RepositoryEntry re = assignment.getFormEntry();

			Page page = assignment.getPage();
			PageStatus pageStatus = page.getPageStatus();
			
			List<AccessRights> accessRights = portfolioService.getAccessRights(page);
			boolean anonym = assignment.isAnonymousExternalEvaluation();

			if(pageStatus == null || pageStatus == PageStatus.draft) {
				if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
					return new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, false, false).getInitialComponent();
				}
			} else if (assignment.isOnlyAutoEvaluation()) {
				// only the auto evaluation is shown
				if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
					boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
					return new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, false).getInitialComponent();
				} else if(hasRole(PortfolioRoles.coach, ureq.getIdentity(), accessRights)) {
					Identity owner = getOwner(accessRights);
					return new EvaluationFormController(ureq, wControl, owner, body, re, true, false).getInitialComponent();
				} else if(hasRole(PortfolioRoles.reviewer, ureq.getIdentity(), accessRights)) {
					if(assignment.isReviewerSeeAutoEvaluation()) {
						Identity owner = getOwner(accessRights);
						return new EvaluationFormController(ureq, wControl, owner, body, re, true, false).getInitialComponent();
					}
				}
			} else {
				if(hasRole(PortfolioRoles.owner, ureq.getIdentity(), accessRights)) {
					boolean readOnly = (pageStatus == PageStatus.published) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
					Identity owner = getOwner(accessRights);
					List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
					if(coachesAndReviewers.size() > 0) {
						return new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, body, re, readOnly, anonym).getInitialComponent();
					}
					return new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, false).getInitialComponent();
				} else if(hasRole(PortfolioRoles.coach, ureq.getIdentity(), accessRights)) {
					Identity owner = getOwner(accessRights);
					List<Identity> coachesAndReviewers = getCoachesAndReviewers(accessRights);
					boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
					return new MultiEvaluationFormController(ureq, wControl, owner, coachesAndReviewers, body, re, readOnly, anonym).getInitialComponent();
				} else if(hasRole(PortfolioRoles.reviewer, ureq.getIdentity(), accessRights)) {
					boolean seeAutoEvaluation = assignment.isReviewerSeeAutoEvaluation();
					boolean readOnly = (pageStatus == PageStatus.draft) || (pageStatus == PageStatus.closed) || (pageStatus == PageStatus.deleted);
					if(seeAutoEvaluation) {
						Identity owner = getOwner(accessRights);
						List<Identity> reviewers = Collections.singletonList(ureq.getIdentity());
						return new MultiEvaluationFormController(ureq, wControl, owner, reviewers, body, re, readOnly, anonym).getInitialComponent();
					} else {
						return new EvaluationFormController(ureq, wControl, ureq.getIdentity(), body, re, readOnly, !readOnly).getInitialComponent();
					}
				}
			}
		}
		return null;
	}
	
	private Identity getOwner(List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(PortfolioRoles.owner == accessRight.getRole()) {
				return accessRight.getIdentity();
			}
		}
		return null;
	}
	
	private List<Identity> getCoachesAndReviewers(List<AccessRights> accessRights) {
		List<Identity> identities = new ArrayList<>(accessRights.size());
		for(AccessRights accessRight:accessRights) {
			if(PortfolioRoles.coach == accessRight.getRole() || PortfolioRoles.reviewer == accessRight.getRole()) {
				identities.add(accessRight.getIdentity());
			}
		}
		return identities;
	}
	
	private boolean hasRole(PortfolioRoles role, Identity identity, List<AccessRights> accessRights) {
		for(AccessRights accessRight:accessRights) {
			if(role == accessRight.getRole() && accessRight.getIdentity() != null && accessRight.getIdentity().equals(identity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof EvaluationFormPart) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			// find assignment
			EvaluationFormPart eva = (EvaluationFormPart)element;
			PageBody body = eva.getBody();
			Assignment assignment = portfolioService.getAssignment(body);
			
			//find the evaluation form
			RepositoryEntry re = assignment.getFormEntry();
			File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
			File formFile = new File(repositoryDir, FORM_XML_FILE);
			return new EvaluationFormController(ureq, wControl, formFile);
		}
		return null;
	}
}
