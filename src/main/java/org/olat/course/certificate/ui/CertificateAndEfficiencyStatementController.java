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

package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.portfolio.EfficiencyStatementMediaHandler;
import org.olat.course.assessment.ui.tool.IdentityAssessmentOverviewController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.component.MediaCollectorComponent;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Displays the users efficiency statement
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class CertificateAndEfficiencyStatementController extends BasicController {

	private static final String usageIdentifyer = "org.olat.course.assessment.EfficiencyStatementController";
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link certificateLink;
	private Link courseDetailsLink;
	private Link homeLink, courseLink, groupLink, contactLink;
	
	private final Certificate certificate;
	private final EfficiencyStatement efficiencyStatement;
	private final Identity statementOwner;
	private final BusinessGroup businessGroup;
	private final RepositoryEntry courseRepoEntry;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private CertificateController certificateCtrl;
	private IdentityAssessmentOverviewController courseDetailsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private EfficiencyStatementMediaHandler mediaHandler;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * The constructor shows the efficiency statement given as parameter for the current user
	 * @param wControl
	 * @param ureq
	 * @param courseId
	 */
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, EfficiencyStatement efficiencyStatement) {
		this(wControl, ureq, ureq.getIdentity(), null, null, null, efficiencyStatement, null, false);
	}
	
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, RepositoryEntry entry) {
		this(wControl, ureq, 
				ureq.getIdentity(), null, entry.getOlatResource().getKey(), entry,
				CoreSpringFactory.getImpl(EfficiencyStatementManager.class).getUserEfficiencyStatementByResourceKey(entry.getOlatResource().getKey(), ureq.getIdentity()),
				null, false);
	}
	
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, Identity statementOwner,
			BusinessGroup businessGroup, Long resourceKey, RepositoryEntry courseRepo,
			EfficiencyStatement efficiencyStatement, Certificate preloadedCertificate, boolean links) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseRepoEntry = courseRepo;
		this.businessGroup = businessGroup;

		if(businessGroup == null && courseRepo != null) {
			SearchBusinessGroupParams params = new SearchBusinessGroupParams(statementOwner, false, true);
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, courseRepo, 0, -1);
			if(!groups.isEmpty()) {
				businessGroup = groups.get(0);
			}
		}

		this.statementOwner = statementOwner;
		this.efficiencyStatement = efficiencyStatement;
		if(preloadedCertificate == null) {
			this.certificate = certificatesManager.getLastCertificate(statementOwner, resourceKey);
		} else {
			this.certificate = preloadedCertificate;
		}
		
		mainVC = createVelocityContainer("certificate_efficiencystatement");
		populateAssessedIdentityInfos(ureq, courseRepo, businessGroup, links);
		
		if(efficiencyStatement != null && certificate != null) {
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			certificateLink = LinkFactory.createLink("details.certificate", mainVC, this);
			certificateLink.setElementCssClass("o_select_certificate_segement");
			segmentView.addSegment(certificateLink, true);
			selectCertificate(ureq);
			
			courseDetailsLink = LinkFactory.createLink("details.course.infos", mainVC, this);
			courseDetailsLink.setElementCssClass("o_select_statement_segment");
			segmentView.addSegment(courseDetailsLink, false);
		} else if(efficiencyStatement != null) {
			selectCourseInfos(ureq);
		} else if(certificate != null) {
			selectCertificate(ureq);
		}
		
		if(efficiencyStatement != null && statementOwner.equals(ureq.getIdentity()) && portfolioV2Module.isEnabled()) {
			String businessPath = "[RepositoryEntry:" + efficiencyStatement.getCourseRepoEntryKey() + "]";
			MediaCollectorComponent collectorCmp = new MediaCollectorComponent("collectArtefactLink", getWindowControl(), efficiencyStatement,
					mediaHandler, businessPath);
			mainVC.put("collectArtefactLink", collectorCmp);
		}

		putInitialPanel(mainVC);
	}

	public void disableMediaCollector() {
		Component component = mainVC.getComponent("collectArtefactLink");
		if(component != null) {
			mainVC.remove(component);
		}
	}

	private void populateAssessedIdentityInfos(UserRequest ureq, RepositoryEntry courseRepo, BusinessGroup group, boolean links) { 
		if(efficiencyStatement != null) {
			mainVC.contextPut("courseTitle", StringHelper.escapeHtml(efficiencyStatement.getCourseTitle()));
		} else if(courseRepo != null) {
			mainVC.contextPut("courseTitle", StringHelper.escapeHtml(courseRepo.getDisplayname()));
		}
		
		if(courseRepoEntry != null && links) {
			courseLink = LinkFactory.createButtonXSmall("course.link", mainVC, this);
			courseLink.setIconLeftCSS("o_icon o_CourseModule_icon");
			mainVC.put("course.link", courseLink);
		}
		
		mainVC.contextPut("user", statementOwner.getUser());
		String username = StringHelper.containsNonWhitespace(statementOwner.getUser().getNickName())
				? statementOwner.getUser().getNickName() : statementOwner.getName();
		mainVC.contextPut("username", username);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);

		if(!getIdentity().equals(statementOwner) && links) {
			homeLink = LinkFactory.createButton("home.link", mainVC, this);
			homeLink.setIconLeftCSS("o_icon o_icon_home");
			mainVC.put("home.link", homeLink);
			
			contactLink = LinkFactory.createButton("contact.link", mainVC, this);
			contactLink.setIconLeftCSS("o_icon o_icon_mail");
			mainVC.put("contact.link", contactLink);
		}

		if(group != null) {
			mainVC.contextPut("groupName", StringHelper.escapeHtml(group.getName()));
			if(links) {
				groupLink = LinkFactory.createButtonXSmall("group.link", mainVC, this);
				groupLink.setIconLeftCSS("o_icon o_icon_group");
				mainVC.put("group.link", groupLink);
			}
		}
		
		List<AssessmentEntryScoring> completions = Collections.emptyList();
		if(courseRepoEntry != null) {
			completions = assessmentService.loadRootAssessmentEntriesByAssessedIdentity(statementOwner,
				Collections.singletonList(courseRepoEntry.getKey()));
		}
		if (!completions.isEmpty()) {
			AssessmentEntryScoring assessmentEntryScoring = completions.get(0);
			Double completion = assessmentEntryScoring.getCompletion();
			if (completion != null) {
				ProgressBar completionItem = new ProgressBar("completion", 100, completion.floatValue() * 100,
						Float.valueOf(100), "%");
				completionItem.setWidthInPercent(true);
				completionItem.setLabelAlignment(LabelAlignment.right);
				completionItem.setLabelMaxEnabled(false);
				completionItem.setRenderStyle(RenderStyle.radial);
				completionItem.setRenderSize(RenderSize.inline);
				BarColor barColor =  assessmentEntryScoring.getPassed() == null || assessmentEntryScoring.getPassed().booleanValue()
						? BarColor.success
						: BarColor.danger;
				completionItem.setBarColor(barColor);
				mainVC.put("completion", completionItem);
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == homeLink) {
			doOpenHome(ureq);
		} else if (source == courseLink) {
			doOpenCourse(ureq);
		} else if (source == groupLink) {
			doOpenGroup(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		} else if(source == segmentView && event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			if(certificateLink.getComponentName().equals(sve.getComponentName())) {
				selectCertificate(ureq);
			} else if(courseDetailsLink.getComponentName().equals(sve.getComponentName())) {
				selectCourseInfos(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == contactCtrl) {
			cmc.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);
		cmc = null;
		contactCtrl = null;
	}
	
	private void selectCertificate(UserRequest ureq) {
		if(certificateCtrl == null) {
			certificateCtrl = new CertificateController(ureq, getWindowControl(), certificate);
			listenTo(certificateCtrl);
		}
		
		mainVC.contextPut("certCreation", Formatter.getInstance(getLocale()).formatDateAndTime(certificate.getCreationDate()));
		if (certificate.getNextRecertificationDate() != null) {
			mainVC.contextPut("certRecertification", Formatter.getInstance(getLocale()).formatDate(certificate.getNextRecertificationDate()));
		}
		
		mainVC.put("segmentCmp", certificateCtrl.getInitialComponent());
	}
	
	private void selectCourseInfos(UserRequest ureq) {
		if(courseDetailsCtrl == null) {
			List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
			List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.assessmentNodeDataMapToList(assessmentNodes);
			courseDetailsCtrl = new IdentityAssessmentOverviewController(ureq, getWindowControl(), assessmentNodeList);
			listenTo(courseDetailsCtrl);
		}
		mainVC.put("segmentCmp", courseDetailsCtrl.getInitialComponent());
	}

	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList contactList = new ContactList("to");
		contactList.add(statementOwner);
		cmsg.addEmailTo(contactList);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenGroup(UserRequest ureq) {
		if(businessGroup != null) {
			List<ContextEntry> ces = new ArrayList<>(1);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", businessGroup.getKey());
			ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));
	
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	private void doOpenCourse(UserRequest ureq) {
		if(courseRepoEntry != null) {
			List<ContextEntry> ces = new ArrayList<>(1);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", courseRepoEntry.getKey());
			ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));
	
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	private void doOpenHome(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<>(1);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(statementOwner));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}

}