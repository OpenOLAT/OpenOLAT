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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.portfolio.EfficiencyStatementArtefact;
import org.olat.group.BusinessGroup;
import org.olat.modules.co.ContactFormController;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
public class EfficiencyStatementController extends BasicController {
	
	private VelocityContainer userDataVC;
	private static final String usageIdentifyer = EfficiencyStatementController.class.getCanonicalName();
	
	private final EfficiencyStatement efficiencyStatement;
	private final Identity statementOwner;
	private final Long businessGroupKey;
	private final Long courseRepoKey;
	
	//to collect the eff.Statement as artefact
	private Link collectArtefactLink, homeLink, courseLink, groupLink, contactLink;
	private PortfolioModule portfolioModule;
	// the collect-artefact-wizard
	private Controller ePFCollCtrl;
	//contact
	private ContactFormController contactCtrl;
	private CloseableModalController cmc;
	
	/**
	 * The constructor shows the efficiency statement given as parameter for the current user
	 * @param wControl
	 * @param ureq
	 * @param courseId
	 */
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, EfficiencyStatement efficiencyStatement) {
		this(wControl, ureq, ureq.getIdentity(), null, null, efficiencyStatement, false);
	}
	
	/**
	 * This constructor show the efficiency statement for the course repository key and the current user
	 * @param wControl
	 * @param ureq
	 * @param courseRepoEntryKey
	 */
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, Long courseRepoEntryKey) {
		this(wControl, ureq, 
				ureq.getIdentity(), null, RepositoryManager.getInstance().lookupRepositoryEntry(courseRepoEntryKey, false),
				EfficiencyStatementManager.getInstance().getUserEfficiencyStatement(courseRepoEntryKey, ureq.getIdentity()), false);
	}
	
	public EfficiencyStatementController(WindowControl wControl, UserRequest ureq, Identity statementOwner,
			BusinessGroup businessGroup, RepositoryEntry courseRepo, EfficiencyStatement efficiencyStatement, boolean links) {
		super(ureq, wControl);
		
		this.courseRepoKey = courseRepo == null ? (efficiencyStatement == null ? null : efficiencyStatement.getCourseRepoEntryKey()) : courseRepo.getKey();
		if(courseRepo == null && courseRepoKey != null) {
			courseRepo = RepositoryManager.getInstance().lookupRepositoryEntry(courseRepoKey, false);
		}
		if(businessGroup == null && courseRepo != null) {
			ICourse course = CourseFactory.loadCourse(courseRepo.getOlatResource());
			List<BusinessGroup> groups = course.getCourseEnvironment().getCourseGroupManager().getParticipatingBusinessGroups(statementOwner);
			if(groups.size() > 0) {
				businessGroup = groups.get(0);
			}
		}
		this.businessGroupKey = businessGroup == null ? null : businessGroup.getKey();
		this.statementOwner = statementOwner;
		this.efficiencyStatement = efficiencyStatement;
		init(ureq, statementOwner, courseRepo, businessGroup, links);
	}
		
	private void init(UserRequest ureq, Identity statementOwner, RepositoryEntry courseRepo, BusinessGroup group, boolean links) { 
		//extract efficiency statement data
		//fallback translation for user properties 
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));		
		userDataVC = createVelocityContainer("efficiencystatement");
		if(efficiencyStatement != null) {
			userDataVC.contextPut("courseTitle", StringHelper.escapeHtml(efficiencyStatement.getCourseTitle()));
			userDataVC.contextPut("date", StringHelper.formatLocaleDateTime(efficiencyStatement.getLastUpdated(), ureq.getLocale()));
		} else if(courseRepo != null) {
			userDataVC.contextPut("courseTitle", StringHelper.escapeHtml(courseRepo.getDisplayname()));
		}
		
		if(courseRepoKey != null && links) {
			courseLink = LinkFactory.createButton("course.link", userDataVC, this);
			courseLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_course");
			userDataVC.put("course.link", courseLink);
		}
		
		userDataVC.contextPut("user", statementOwner.getUser());			
		userDataVC.contextPut("username", statementOwner.getName());
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());	
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		userDataVC.contextPut("userPropertyHandlers", userPropertyHandlers);

		if(!getIdentity().equals(statementOwner) && links) {
			homeLink = LinkFactory.createButton("home.link", userDataVC, this);
			homeLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_to_home");
			userDataVC.put("home.link", homeLink);
			
			contactLink = LinkFactory.createButton("contact.link", userDataVC, this);
			contactLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_mail");
			userDataVC.put("contact.link", contactLink);
		}

		if(group != null) {
			userDataVC.contextPut("groupName", group.getName());
			if(links) {
				groupLink = LinkFactory.createButton("group.link", userDataVC, this);
				groupLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_group");
				userDataVC.put("group.link", groupLink);
			}
		}
		
		if (efficiencyStatement != null) {
			Controller identityAssessmentCtr = new IdentityAssessmentOverviewController(ureq, getWindowControl(), efficiencyStatement.getAssessmentNodes());
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
		} else {
			//message, that no data is available. This may happen in the case the "open efficiency" link is available, while in the meantime an author
			//disabled the efficiency statement.
			String text = translate("efficiencystatement.nodata");
			Controller messageCtr = MessageUIFactory.createSimpleMessage(ureq, getWindowControl(), text);
			listenTo(messageCtr);//gets disposed as this controller gets disposed.
			userDataVC.put("assessmentOverviewTable",  messageCtr.getInitialComponent());
		}
		putInitialPanel(userDataVC);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	public void event(UserRequest ureq, Component source, Event event) {
		if(source.equals(collectArtefactLink)){
			popupArtefactCollector(ureq);
		} else if (source == homeLink) {
			openHome(ureq);
		} else if (source == courseLink) {
			openCourse(ureq);
		} else if (source == groupLink) {
			openGroup(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == cmc) {
				removeAsListenerAndDispose(cmc);
				removeAsListenerAndDispose(contactCtrl);
				cmc = null;
				contactCtrl = null;
			} else if (source == contactCtrl) {
				cmc.deactivate();
				removeAsListenerAndDispose(cmc);
				removeAsListenerAndDispose(contactCtrl);
				cmc = null;
				contactCtrl = null;
			}
	}

	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList contactList = new ContactList("to");
		contactList.add(statementOwner);
		cmsg.addEmailTo(contactList);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	private void openGroup(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<ContextEntry>(1);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", businessGroupKey);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
	  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
	
	private void openCourse(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<ContextEntry>(1);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", courseRepoKey);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
	  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
	
	private void openHome(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<ContextEntry>(1);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(statementOwner));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
	  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
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
