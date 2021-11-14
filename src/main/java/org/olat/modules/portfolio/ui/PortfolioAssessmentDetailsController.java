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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.portfolio.PortfolioResultDetailsController;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the assessment view for a portfolio resource. The coaches can see
 * all sections.
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioAssessmentDetailsController extends BasicController {
	
	private Binder binder;
	
	private BinderAssessmentController assessmentCtrl;

	private CourseNode courseNode;
	private final VelocityContainer mainVC;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioAssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry templateEntry, Identity assessedIdentity) {
		super(ureq, wControl, Util.createPackageTranslator(PortfolioResultDetailsController.class, ureq.getLocale()));

		mainVC = createVelocityContainer("binder_result_details");
		
		if(templateEntry != null && BinderTemplateResource.TYPE_NAME.equals(templateEntry.getOlatResource().getResourceableTypeName())) {
			List<Binder> binders = portfolioService.getBinders(assessedIdentity, templateEntry, null);
			if(binders.size() == 1) {
				binder = binders.get(0);
			} else if(binders.size() > 1) {
				//warning
				binder = binders.get(0);
			}
		}
		
		loadModel(ureq, binder);
		putInitialPanel(mainVC);
	}
	
	public PortfolioAssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode, RepositoryEntry templateEntry, Identity assessedIdentity) {
		super(ureq, wControl);

		this.courseNode = courseNode;
		mainVC = createVelocityContainer("binder_result_details");
		
		if(templateEntry != null && BinderTemplateResource.TYPE_NAME.equals(templateEntry.getOlatResource().getResourceableTypeName())) {
			List<Binder> binders = portfolioService.getBinders(assessedIdentity, courseEntry, courseNode.getIdent());
			if(binders.size() == 1) {
				binder = binders.get(0);
			} else if(binders.size() > 1) {
				//warning
				binder = binders.get(0);
			}
		}
		
		loadModel(ureq, binder);
		putInitialPanel(mainVC);
	}
	
	private void loadModel(UserRequest ureq, Binder loadedBinder) {
		if(loadedBinder == null) {
			mainVC.contextPut("noMap", Boolean.TRUE);
		} else {
			Formatter formatter = Formatter.getInstance(getLocale());
			String templateTitle = loadedBinder.getTemplate().getTitle();
			mainVC.contextPut("templateTitle", templateTitle);
			
			String copyDate = "";
			if(loadedBinder.getCopyDate() != null) {
				copyDate = formatter.formatDateAndTime(loadedBinder.getCopyDate());
			}
			mainVC.contextPut("copyDate", copyDate);

			String returnDate = "";
			if(loadedBinder.getReturnDate() != null) {
				returnDate = formatter.formatDateAndTime(loadedBinder.getReturnDate());
			}
			mainVC.contextPut("returnDate", returnDate);
			
			List<AccessRights> rights = portfolioService.getAccessRights(loadedBinder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCourseCoach(loadedBinder, rights);
			BinderConfiguration config = BinderConfiguration.createConfig(loadedBinder);
			
			assessmentCtrl = new BinderAssessmentController(ureq, getWindowControl(),
					secCallback, loadedBinder, config);
			listenTo(assessmentCtrl);
			mainVC.put("assessment", assessmentCtrl.getInitialComponent());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
			if(courseNode != null) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		fireEvent(ureq, event);
	}

	/*
	private void doOpenMap(UserRequest ureq, Binder binder) {
		if(stackPanel instanceof TooledStackedPanel) {
			List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(binder, rights);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			BinderController binderCtrl = new BinderController(ureq, getWindowControl(), (TooledStackedPanel)stackPanel, secCallback, binder, config);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackPanel.pushController(displayName, binderCtrl);
			binderCtrl.activate(ureq, null, null);
		}
	}
	*/
}