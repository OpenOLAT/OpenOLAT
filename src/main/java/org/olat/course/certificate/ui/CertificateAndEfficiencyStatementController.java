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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.portfolio.EfficiencyStatementMediaHandler;
import org.olat.course.assessment.ui.tool.IdentityAssessmentOverviewController;
import org.olat.course.assessment.ui.tool.IdentityBadgesAssertionsController;
import org.olat.course.assessment.ui.tool.IdentityCertificatesController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.component.MediaCollectorComponent;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Displays the users efficiency statement. After a reset of the course's data,
 * the current efficiency statement doesn't exist.
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class CertificateAndEfficiencyStatementController extends BasicController {

	public static final String usageIdentifyer = "org.olat.course.assessment.EfficiencyStatementController";
	
	private VelocityContainer mainVC;
	private Link homeLink;
	private Link contactLink;
	private Link downloadArchiveLink;
	private Dropdown historyOfStatementsDropdown;

	private Boolean learningPath;
	private Boolean scoreScalingEnabled;
	private Certificate certificate;
	private final Identity statementOwner;
	private final RepositoryEntry courseRepoEntry;
	private EfficiencyStatement efficiencyStatement;
	private final BadgeEntryConfiguration badgeConfig;
	private UserEfficiencyStatement userEfficiencyStatement;
	private RepositoryEntryCertificateConfiguration certificateConfig;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private IdentityBadgesAssertionsController badgesCtrl;
	private IdentityCertificatesController certificatesCtrl;
	private IdentityAssessmentOverviewController courseDetailsCtrl;
	private final IdentityAssessmentProgressController assessmentProgressCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private EfficiencyStatementMediaHandler mediaHandler;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;

	/**
	 * The constructor shows the efficiency statement given as parameter for the current user
	 * @param wControl
	 * @param ureq
	 * @param courseId
	 */
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, EfficiencyStatement efficiencyStatement) {
		this(wControl, ureq, ureq.getIdentity(), null, null, null, efficiencyStatement, null, false, false, false, true, true);
	}
	
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, RepositoryEntry entry) {
		this(wControl, ureq, 
				ureq.getIdentity(), null, entry.getOlatResource().getKey(), entry,
				CoreSpringFactory.getImpl(EfficiencyStatementManager.class).getUserEfficiencyStatementByResourceKey(entry.getOlatResource().getKey(), ureq.getIdentity()),
				null, false, true, false, true, true);
	}
	
	/**
	 * 
	 * @param wControl
	 * @param ureq
	 * @param statementOwner
	 * @param businessGroup
	 * @param resourceKey
	 * @param courseRepo
	 * @param efficiencyStatement
	 * @param preloadedCertificate
	 * @param links
	 * @param history The history of efficiency statements
	 */
	public CertificateAndEfficiencyStatementController(WindowControl wControl, UserRequest ureq, Identity statementOwner,
			BusinessGroup businessGroup, Long resourceKey, RepositoryEntry courseRepo,
			EfficiencyStatement efficiencyStatement, Certificate preloadedCertificate,
			boolean links, boolean history, boolean downloadArchive, boolean title, boolean userData) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseRepoEntry = courseRepo;
		if(courseRepo != null) {
			certificateConfig = certificatesManager.getConfiguration(courseRepo);
		}

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
			certificate = certificatesManager.getLastCertificate(statementOwner, resourceKey);
		} else {
			certificate = preloadedCertificate;
		}
		
		learningPath = isLearningPathCourse();
		mainVC = createVelocityContainer("certificate_efficiencystatement");
	
		UserCourseEnvironment assessedCourseEnv = null;
		if(courseRepoEntry != null) {
			try {
				ICourse course = CourseFactory.loadCourse(courseRepo);
				assessedCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(statementOwner, course);
				scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
			} catch (Exception e) {
				logError("Course corrupted", e);
			}
		}
		assessmentProgressCtrl = new IdentityAssessmentProgressController(ureq, getWindowControl(), assessedCourseEnv,
				businessGroup, efficiencyStatement, links);
		listenTo(assessmentProgressCtrl);
		mainVC.put("assessment.progress", assessmentProgressCtrl.getInitialComponent());
		
		mainVC.contextPut("withTitle", Boolean.valueOf(title));
		mainVC.contextPut("withUserData", Boolean.valueOf(userData));
		if(userData) {
			populateAssessedIdentityInfos(ureq, courseRepo, links);
		}
	
		certificatesCtrl = new IdentityCertificatesController(ureq, getWindowControl(),
				courseRepoEntry, certificateConfig, statementOwner, true);
		listenTo(certificatesCtrl);
		mainVC.put("certificates", certificatesCtrl.getInitialComponent());
		if(courseRepoEntry == null && certificate != null) {
			certificatesCtrl.loadModel(List.of(certificate));
		} else if(certificateConfig == null || !certificateConfig.isCertificateEnabled()) {
			certificatesCtrl.getInitialComponent().setVisible(false);
		}
		
		badgeConfig = courseRepoEntry == null ? null : openBadgesManager.getConfiguration(courseRepoEntry);
		badgesCtrl = new IdentityBadgesAssertionsController(ureq, getWindowControl(),
				courseRepoEntry, statementOwner, true) ;
		listenTo(badgesCtrl);
		mainVC.put("badges", badgesCtrl.getInitialComponent());
		badgesCtrl.getInitialComponent().setVisible(badgesCtrl.hasBadgesAssertions()
				|| (badgeConfig != null && badgeConfig.isAwardEnabled()));
		
		populateCourseDetails(ureq);
		if(certificate != null) {
			populateCertificateInfos(certificate);
		}
		
		if(downloadArchive) {
			downloadArchiveLink = LinkFactory.createLink("download.archive", "download.archive", getTranslator(), mainVC, this, Link.BUTTON);
			downloadArchiveLink.setIconLeftCSS("o_icon o_icon_download");
			downloadArchiveLink.setVisible(false);
		}
		
		if(efficiencyStatement != null && statementOwner.equals(ureq.getIdentity()) && portfolioV2Module.isEnabled()) {
			String businessPath = "[RepositoryEntry:" + efficiencyStatement.getCourseRepoEntryKey() + "]";
			MediaCollectorComponent collectorCmp = new MediaCollectorComponent("collectArtefactLink", getWindowControl(), efficiencyStatement,
					mediaHandler, businessPath);
			collectorCmp.setDomReplacementWrapperRequired(false);
			mainVC.put("collectArtefactLink", collectorCmp);
		}
		
		if(history) {
			loadEfficiencyStatementsHistory();
		}

		putInitialPanel(mainVC);
	}
	
	private boolean isLearningPathCourse() {
		boolean lPath = false;
		if (courseRepoEntry != null) {
			try {
				ICourse course = CourseFactory.loadCourse(courseRepoEntry);
				if (course != null) {
					learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
				}
			} catch (CorruptedCourseException e) {
				logError("Course corrupted", e);
			}
		}
		return lPath;
	}
	
	private void loadEfficiencyStatementsHistory() {
		if(courseRepoEntry == null) return;
		
		List<UserEfficiencyStatement> statements = efficiencyStatementManager.getHistoryOfUserEfficiencyStatementsLightByRepositoryEntry(courseRepoEntry, statementOwner);
		if(!statements.isEmpty()) {
			historyOfStatementsDropdown = new Dropdown("statements.list", null, false, getTranslator());
			historyOfStatementsDropdown.setTranslatedLabel(translate("current.version"));
			historyOfStatementsDropdown.setButton(true);
			historyOfStatementsDropdown.setIconCSS("o_icon o_icon_reset_data");
			historyOfStatementsDropdown.setOrientation(DropdownOrientation.right);
			mainVC.put(historyOfStatementsDropdown.getComponentName(), historyOfStatementsDropdown);
			
			int counter = statements.size();
			Formatter formatter = Formatter.getInstance(getLocale());
			// After a course reset, the efficiency doesn't exist, it will be created with the first user interaction (participant or coach)
			if(!statements.get(0).isLastStatement()) {
				Link statementLink = LinkFactory.createCustomLink("statement_" + (--counter), "statement", "current.version", Link.LINK, mainVC, this);
				statementLink.setUserObject(Long.valueOf(-1l));
				historyOfStatementsDropdown.addComponent(statementLink);
			}

			for(UserEfficiencyStatement statement:statements) {
				String label;
				if(statement.isLastStatement()) {
					label = translate("current.version");
				} else {
					label = translate("statement.version",
							formatter.formatDate(statement.getCreationDate()),
							formatter.formatDate(statement.getLastModified()),
							Integer.toString(counter));
				}
				Link statementLink = LinkFactory.createCustomLink("statement_" + (--counter), "statement", label, Link.LINK | Link.NONTRANSLATED, mainVC, this);
				statementLink.setUserObject(statement.getKey());
				historyOfStatementsDropdown.addComponent(statementLink);
			}
			
			historyOfStatementsDropdown.setVisible(historyOfStatementsDropdown.size() > 1);
		}
	}

	public void disableMediaCollector() {
		Component component = mainVC.getComponent("collectArtefactLink");
		if(component != null) {
			mainVC.remove(component);
		}
	}

	private void populateAssessedIdentityInfos(UserRequest ureq, RepositoryEntry courseRepo, boolean links) { 
		if(efficiencyStatement != null) {
			mainVC.contextPut("courseTitle", StringHelper.escapeHtml(efficiencyStatement.getCourseTitle()));
		} else if(courseRepo != null) {
			mainVC.contextPut("courseTitle", StringHelper.escapeHtml(courseRepo.getDisplayname()));
		}
		
		mainVC.contextPut("user", statementOwner.getUser());
		
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

		populateAssessedIdentityCompletion();
	}
	
	private void populateAssessedIdentityCompletion() {
		if (assessmentProgressCtrl.hasCompletion()) {	
			setIdentityCompletion(assessmentProgressCtrl.getCompletion(), assessmentProgressCtrl.getBarColor());
		} else {
			mainVC.remove("completion");
		}
	}
	
	private void setIdentityCompletion(float completion, BarColor barColor) {
		ProgressBar completionItem = new ProgressBar("completion", 100, completion,
				Float.valueOf(100), "%");
		completionItem.setWidthInPercent(true);
		completionItem.setLabelAlignment(LabelAlignment.right);
		completionItem.setLabelMaxEnabled(false);
		completionItem.setRenderStyle(RenderStyle.radial);
		completionItem.setRenderSize(RenderSize.inline);
		completionItem.setBarColor(barColor);
		mainVC.put("completion", completionItem);
	}
	
	private void populateCertificateInfos(Certificate certificateToShow) {
		mainVC.contextRemove("certCreation");
		mainVC.contextRemove("certRecertification");
		mainVC.contextRemove("nextRecertificationWindow");
		
		if(certificateToShow != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			mainVC.contextPut("certCreation", formatter.formatDateAndTime(certificateToShow.getCreationDate()));
			
			if(certificateToShow.isLast() && ((certificateConfig != null && certificateConfig.isValidityEnabled())
					|| (certificateConfig == null && certificateToShow.getNextRecertificationDate() != null))) {
				Date nextRecertificationDate = certificateToShow.getNextRecertificationDate();
				if(nextRecertificationDate != null) {
					mainVC.contextPut("certRecertification", formatter.formatDate(nextRecertificationDate));
				}
				
				if(certificateConfig != null && certificateConfig.isRecertificationEnabled() && certificateConfig.isRecertificationLeadTimeEnabled()) {
					Date nextRecertificationWindow = certificatesManager.nextRecertificationWindow(certificateToShow, certificateConfig);
					if(nextRecertificationWindow != null) {
						mainVC.contextPut("nextRecertificationWindow", translate("certificate.recertification.start",
								formatter.formatDate(nextRecertificationWindow)));
					}
				}
			}
		}
	}
	
	private void populateCourseDetails(UserRequest ureq) {
		removeAsListenerAndDispose(courseDetailsCtrl);
		courseDetailsCtrl = null;
		
		if(efficiencyStatement != null) {
			List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
			List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.assessmentNodeDataMapToList(assessmentNodes);
			courseDetailsCtrl = new IdentityAssessmentOverviewController(ureq, getWindowControl(), assessmentNodeList,
					learningPath, scoreScalingEnabled);
			listenTo(courseDetailsCtrl);
			mainVC.put("courseDetails", courseDetailsCtrl.getInitialComponent());
		} else {
			mainVC.remove("courseDetails");
		}
	}
	
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == homeLink) {
			doOpenHome(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		} else if (source instanceof Link link && "statement".equals(link.getCommand())
				&& link.getUserObject() instanceof Long statementKey) {
			doLoadStatement(ureq, link, statementKey);
		} else if(downloadArchiveLink == source) {
			doDownloadArchive(ureq);
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
	
	private void doLoadStatement(UserRequest ureq, Link statementLink, Long statementKey) {
		if(statementKey != null && statementKey.longValue() > 0) {
			userEfficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementByKey(statementKey);
			efficiencyStatement = efficiencyStatementManager.getEfficiencyStatement(userEfficiencyStatement);
			if(userEfficiencyStatement.isLastStatement()) {
				historyOfStatementsDropdown.setTranslatedLabel(translate("current.version"));
				mainVC.contextRemove("version");
				
			} else {
				historyOfStatementsDropdown.setTranslatedLabel(statementLink.getI18n());
				mainVC.contextPut("version", statementLink.getI18n());
			}
			assessmentProgressCtrl.updateFromStatement(efficiencyStatement);
			if(assessmentProgressCtrl.hasCompletion()) {
				setIdentityCompletion(assessmentProgressCtrl.getCompletion(), assessmentProgressCtrl.getBarColor());
			} else {
				mainVC.remove("completion");
			}
		} else {
			historyOfStatementsDropdown.setTranslatedLabel(translate("current.version"));
			userEfficiencyStatement = null;
			efficiencyStatement = null;
			mainVC.remove("completion");
			mainVC.contextRemove("version");
		}
		
		populateCourseDetails(ureq);

		if(userEfficiencyStatement != null) {
			if(userEfficiencyStatement.isLastStatement()) {
				certificate = certificatesManager.getLastCertificate(statementOwner, courseRepoEntry.getOlatResource().getKey());
			} else if(userEfficiencyStatement.getArchiveCertificateKey() != null) {
				certificate = certificatesManager.getCertificateById(userEfficiencyStatement.getArchiveCertificateKey());
			} else {
				certificate = null;
			}
		} else {
			certificate = certificatesManager.getLastCertificate(statementOwner, courseRepoEntry.getOlatResource().getKey());
		}
		
		populateCertificateInfos(certificate);
		
		if(downloadArchiveLink != null) {
			downloadArchiveLink.setVisible(userEfficiencyStatement != null && StringHelper.containsNonWhitespace(userEfficiencyStatement.getArchivePath()));
		}
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
	
	private void doOpenHome(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<>(1);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(statementOwner));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
	
	private void doDownloadArchive(UserRequest ureq) {
		if(userEfficiencyStatement == null || !StringHelper.containsNonWhitespace(userEfficiencyStatement.getArchivePath())) {
			return;
		}
		String archivePath = userEfficiencyStatement.getArchivePath();
		VFSLeaf archive = VFSManager.olatRootLeaf(archivePath);
		MediaResource resource = new VFSMediaResource(archive);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}