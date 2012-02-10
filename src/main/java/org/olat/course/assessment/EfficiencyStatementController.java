/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.portfolio.EfficiencyStatementArtefact;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * Displays the users efficiency statement
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class EfficiencyStatementController extends MainLayoutBasicController {
	
	private VelocityContainer userDataVC;
	private static final String usageIdentifyer = EfficiencyStatementController.class.getCanonicalName();
	
	private EfficiencyStatement efficiencyStatement;
	
	//to collect the eff.Statement as artefact
	private Link collectArtefactLink;
	private PortfolioModule portfolioModule;
	// the collect-artefact-wizard
	private Controller ePFCollCtrl; 

	/**
	 * Constructor
	 * @param wControl
	 * @param ureq
	 * @param courseId
	 */
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, Long courseRepoEntryKey) {
		this(wControl, ureq, EfficiencyStatementManager.getInstance().getUserEfficiencyStatement(courseRepoEntryKey, ureq.getIdentity()));
	}
	
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, EfficiencyStatement efficiencyStatement) {
		this(wControl, ureq, ureq.getIdentity(), efficiencyStatement);
	}
	
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, Identity statementOwner, EfficiencyStatement efficiencyStatement) {
		super(ureq, wControl);

		//either the efficiency statement or the error message, that no data is available goes to the content area
		final Component content;
		if (efficiencyStatement != null) {
			this.efficiencyStatement = efficiencyStatement;
			
			//extract efficiency statement data
			//fallback translation for user properties 
			setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));		
			userDataVC = createVelocityContainer("efficiencystatement");
			userDataVC.contextPut("courseTitle", efficiencyStatement.getCourseTitle() + " (" + efficiencyStatement.getCourseRepoEntryKey().toString() + ")");
			userDataVC.contextPut("user", statementOwner.getUser());			
			userDataVC.contextPut("username", statementOwner.getName());
			userDataVC.contextPut("date", StringHelper.formatLocaleDateTime(efficiencyStatement.getLastUpdated(), ureq.getLocale()));
			
			Roles roles = ureq.getUserSession().getRoles();
			boolean isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());	
			List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
			userDataVC.contextPut("userPropertyHandlers", userPropertyHandlers);
			
			Controller identityAssessmentCtr = new IdentityAssessmentOverviewController(ureq, wControl, efficiencyStatement.getAssessmentNodes());
			listenTo(identityAssessmentCtr);//dispose it when this one is disposed
			userDataVC.put("assessmentOverviewTable", identityAssessmentCtr.getInitialComponent());
			
			//add link to collect efficiencyStatement as artefact
			if(statementOwner.equals(ureq.getIdentity())) {
				portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
				EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
				if(portfolioModule.isEnabled() && artHandler != null && artHandler.isEnabled()) {
					collectArtefactLink = LinkFactory.createCustomLink("collectArtefactLink", "collectartefact", "", Link.NONTRANSLATED, userDataVC, this);
					collectArtefactLink.setCustomEnabledLinkCSS("b_eportfolio_add_again");
				}
			}
			
			content = userDataVC;
		} else {
			//message, that no data is available. This may happen in the case the "open efficiency" link is available, while in the meantime an author
			//disabled the efficiency statement.
			String text = translate("efficiencystatement.nodata");
			Controller messageCtr = MessageUIFactory.createErrorMessage(ureq, wControl, null, text);
			listenTo(messageCtr);//gets disposed as this controller gets disposed.
			content = messageCtr.getInitialComponent();
		}
		//Content goes to a 3 cols layout without left and right column
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, content, null);
		listenTo(layoutCtr);
		putInitialPanel(layoutCtr.getInitialComponent());
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	public void event(UserRequest ureq, Component source, Event event) {
		if(source.equals(collectArtefactLink)){
			popupArtefactCollector(ureq);
		}
		
	}

	/**
	 * opens the collect-artefact wizard 
	 * 
	 * @param ureq
	 */
	private void popupArtefactCollector(UserRequest ureq) {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
		if(artHandler != null && artHandler.isEnabled()) {
			AbstractArtefact artefact = artHandler.createArtefact();
			artefact.setAuthor(getIdentity());//only author can create artefact
			//no business path becouse we cannot launch an efficiency statement
			artefact.setCollectionDate(new Date());
			artefact.setTitle(translate("artefact.title", new String[]{efficiencyStatement.getCourseTitle()}));
			artHandler.prefillArtefactAccordingToSource(artefact, efficiencyStatement);
			ePFCollCtrl = new ArtefactWizzardStepsController(ureq, getWindowControl(), artefact, (VFSContainer)null);
			listenTo(ePFCollCtrl);
			
			//set flag for js-window-resizing (see velocity)
			userDataVC.contextPut("collectwizard", true);
		}
	}
	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}
