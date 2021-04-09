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
package org.olat.course.nodes.iq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.QTI21OverrideOptions;
import org.olat.ims.qti21.ui.event.RestartEvent;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.grading.GradingService;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQPreviewSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 26.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQConfigurationController extends BasicController {

	private static final String VC_CHOSENTEST = "chosentest";
	
	private static final String[] QTI_21_AND_12_RESOURCE = new String[] { TestFileResource.TYPE_NAME,
			ImsQTI21Resource.TYPE_NAME };
	private static final String[] QTI_21_RESOURCE = new String[] { ImsQTI21Resource.TYPE_NAME };

	private VelocityContainer myContent;
	private final BreadcrumbPanel stackPanel;
	
	private Link previewLink;
	private Link previewButton;
	private Link chooseTestButton;
	private Link changeTestButton;
	private Link editTestButton;

	private Controller previewLayoutCtr;
	private CloseableModalController cmc;
	private IQEditReplaceWizard replaceWizard;
	private AssessmentTestDisplayController previewQTI21Ctrl;
	private ReferencableEntriesSearchController searchController;
	private ConfirmChangeResourceController confirmChangeResourceCtrl;
	
	private IQ12EditForm mod12ConfigForm;
	private QTI21EditForm mod21ConfigForm;
	
	private String type;
	private ICourse course;
	private ModuleConfiguration moduleConfiguration;
	private AbstractAccessableCourseNode courseNode;

	@Autowired
	private IQManager iqManager;
	@Autowired
	private QTIModule qtiModule;
	@Autowired
	private QTI21Module qti21Module;
	@Autowired
	private QTI21Service qti21service;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public IQConfigurationController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			AbstractAccessableCourseNode courseNode, String type) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.moduleConfiguration = courseNode.getModuleConfiguration();
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		this.courseNode = courseNode;
		this.type = type;
		
		
		myContent = createVelocityContainer("edit");		
		chooseTestButton = LinkFactory.createButtonSmall("command.chooseRepFile", myContent, this);
		chooseTestButton.setElementCssClass("o_sel_test_choose_repofile");
		changeTestButton = LinkFactory.createButtonSmall("command.changeRepFile", myContent, this);
		changeTestButton.setElementCssClass("o_sel_test_change_repofile");

		// fetch repository entry
		RepositoryEntry re = getIQReference();
		if(re == null) {
			myContent.contextPut(VC_CHOSENTEST, translate("no.file.chosen"));
		} else {
			String displayName = StringHelper.escapeHtml(re.getDisplayname());
			myContent.contextPut(VC_CHOSENTEST, displayName);
			myContent.contextPut("dontRenderRepositoryButton", Boolean.valueOf(true));
			// Put values to velocity container
			if (isEditable(re)) {
				editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
			}
			previewLink = LinkFactory.createCustomLink("command.preview.link", "command.preview", displayName, Link.NONTRANSLATED, myContent, this);
			previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
			previewLink.setCustomEnabledLinkCSS("o_preview");
			previewLink.setTitle(translate("command.preview"));
			previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);
			previewButton.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		}
		
		if(stackPanel != null) {
			stackPanel.addListener(this);
		}

		myContent.contextPut("type", type);
		
		putInitialPanel(myContent);	
		updateEditController(ureq, false);
		
		switch(type) {
			case AssessmentInstance.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				chooseTestButton.setCustomDisplayText(translate("command.createSurvey"));
				break;
		}
	}
	
	/**
	 * Update the edit and layout controllers.
	 * 
	 * @param ureq The user request
	 * @param replacedTest Set true if the test was replaced by a new one.
	 */
	protected void updateEditController(UserRequest ureq, boolean replacedTest) {
		removeAsListenerAndDispose(mod12ConfigForm);
		removeAsListenerAndDispose(mod21ConfigForm);
		mod12ConfigForm = null;
		mod21ConfigForm = null;
		
		RepositoryEntry re = getIQReference();
		if(re == null) {
			myContent.remove("iqeditform");
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			boolean needManualCorrection = false;
			try {// in case of an unreadable test
				needManualCorrection = needManualCorrectionQTI21(re);
			} catch (Exception e) {
				logError("Test cannot be read: " + re, e);
				showError("error.resource.corrupted");
			}
			QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
			if(replacedTest) {// set some default settings in case the user don't save the next panel
				String correctionMode;
				if(gradingService.isGradingEnabled(re, null)) {
					correctionMode = IQEditController.CORRECTION_GRADING;
				} else if(needManualCorrection || getPassedType(re, deliveryOptions) == PassedType.manually) {
					correctionMode = IQEditController.CORRECTION_MANUAL;
				} else {
					correctionMode = IQEditController.CORRECTION_AUTO;
				}
				moduleConfiguration.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionMode);
				if(IQEditController.CORRECTION_GRADING.equals(correctionMode) ||  IQEditController.CORRECTION_MANUAL.equals(correctionMode)) {
					String userVisible = qti21Module.isResultsVisibleAfterCorrectionWorkflow()
							? IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION : IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION;
					moduleConfiguration.setStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, userVisible);
				} else {
					moduleConfiguration.remove(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION);
				}
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			mod21ConfigForm = new QTI21EditForm(ureq, getWindowControl(), moduleConfiguration,
					NodeAccessType.of(course), deliveryOptions, needManualCorrection, courseNode instanceof IQSELFCourseNode);
			mod21ConfigForm.update(re);
			listenTo(mod21ConfigForm);
			myContent.put("iqeditform", mod21ConfigForm.getInitialComponent());
		} else if(QTIResourceTypeModule.isOnyxTest(re.getOlatResource())) {
			myContent.remove("iqeditform");
			showError("error.onyx");
		} else {
			boolean hasEssay = needManualCorrectionQTI12(re);
			if(replacedTest) {// set some default settings in case the user don't save the next panel
				moduleConfiguration.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, hasEssay ? "manual" : "auto");
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			mod12ConfigForm = new IQ12EditForm(ureq, getWindowControl(), moduleConfiguration, hasEssay);
			mod12ConfigForm.update(re.getOlatResource());
			listenTo(mod12ConfigForm);
			myContent.put("iqeditform", mod12ConfigForm.getInitialComponent());
		}
	}

	/**
	 * @param identity
	 * @param repository entry
	 * @return
	 */
	private boolean isEditable(RepositoryEntry re) {
		boolean isOnyx = QTIResourceTypeModule.isOnyxTest(re.getOlatResource());
		if (isOnyx) {
			return false;
		}
		return repositoryService.hasRoleExpanded(getIdentity(), re,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				GroupRoles.owner.name());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (previewLink == source || previewButton == source) {
			doPreview(ureq);
		} else if (chooseTestButton == source){
			doChooseTestAndSurvey(ureq);
		} else if (changeTestButton == source) {
			RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
			if(re == null) {
				showError("error.test.undefined.long", courseNode.getShortTitle());
			} else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {
				doChangeSelfTest(ureq);
			} else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) || type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
				doChangeTestAndSurvey(ureq, re);
			}	
		} else if (editTestButton == source) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		} else if (stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == previewQTI21Ctrl) {
					cleanUpQti21PreviewSession();
				}
			}
		}
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done				
				cmc.deactivate();
				RepositoryEntry re = searchController.getSelectedEntry();
				doConfirmChangeTestAndSurvey(urequest, re);
			}
		} else if(source == confirmChangeResourceCtrl) {
			if(event == Event.DONE_EVENT) {
				RepositoryEntry newEntry = confirmChangeResourceCtrl.getNewTestEntry();
				doChangeResource(urequest, newEntry);
			}
			cmc.deactivate();
		} else if (source == replaceWizard) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if(event == Event.DONE_EVENT) {
				cmc.deactivate();
				String repositorySoftKey = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
				Long repKey = repositoryManager.lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
				QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), courseNode.getIdent(), repKey);
				IQEditController.removeIQReference(moduleConfiguration);
				VFSStatus isDeleted = iqManager.removeQtiSerFiles(course.getResourceableId(), courseNode.getIdent());
				if (!isDeleted.equals(VFSConstants.YES)) {
					// couldn't removed qtiser files
					logWarn("Couldn't removed course node folder! Course resourceable id: " + course.getResourceableId() + ", Course node ident: " + courseNode.getIdent(), null);
				}
				
				RepositoryEntry re = replaceWizard.getSelectedRepositoryEntry();
				boolean needManualCorrection = checkManualCorrectionNeeded(re);
				doIQReference(urequest, re, needManualCorrection);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod12ConfigForm) {
			if (event == Event.DONE_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod21ConfigForm) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(source == previewQTI21Ctrl) {
			if(event instanceof RestartEvent) {
				stackPanel.popContent();
				cleanUpQti21PreviewSession();
				doPreview(urequest);
			}
		}
	}
	
	/**
	 * This check if there is some QTI 2.1 results for the current selected test.
	 * 
	 * @param ureq
	 * @param newEntry
	 */
	private void doConfirmChangeTestAndSurvey(UserRequest ureq, RepositoryEntry newEntry) {
		try {
			RepositoryEntry currentEntry = courseNode.getReferencedRepositoryEntry();
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			
			int numOfAssessedIdentities = 0;
			if(currentEntry != null) {
				List<AssessmentTestSession> assessmentTestSessions = qti21service.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), currentEntry);
				Set<Identity> assessedIdentities = new HashSet<>(); 
				for(AssessmentTestSession assessmentTestSession:assessmentTestSessions) {
					if(StringHelper.containsNonWhitespace(assessmentTestSession.getAnonymousIdentifier())) {
						numOfAssessedIdentities++;
					} else if(assessmentTestSession.getIdentity() != null) {
						assessedIdentities.add(assessmentTestSession.getIdentity());
					}
				}
				numOfAssessedIdentities += assessedIdentities.size();
			}
			
			if(numOfAssessedIdentities > 0) {
				confirmChangeResourceCtrl = new ConfirmChangeResourceController(ureq, getWindowControl(),
						course, (QTICourseNode)courseNode, newEntry, currentEntry, numOfAssessedIdentities);
				listenTo(confirmChangeResourceCtrl);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeResourceCtrl.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			} else {
				doChangeResource(ureq, newEntry);
			}
		} catch (Exception e) {
			logError("", e);
			showError("error.resource.corrupted");
		}
	}
	
	private void doChangeResource(UserRequest ureq, RepositoryEntry newEntry) {
		try {
			boolean needManualCorrection = checkManualCorrectionNeeded(newEntry);
			doIQReference(ureq, newEntry, needManualCorrection);
			updateEditController(ureq, true);
		} catch (Exception e) {
			logError("", e);
			showError("error.resource.corrupted");
		}
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewLayoutCtr);
		removeAsListenerAndDispose(previewQTI21Ctrl);
		
		RepositoryEntry re = getIQReference();
		if(re != null) {
			if(QTIResourceTypeModule.isOnyxTest(re.getOlatResource())) {
				showError("error.onyx");
			} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				cleanUpQti21PreviewSession();//clean up last session
				// need to clean up the assessment test session
				QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
				QTI21OverrideOptions overrideOptions = QTI21OverrideOptions.nothingOverriden();
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				previewQTI21Ctrl = new AssessmentTestDisplayController(ureq, getWindowControl(), new InMemoryOutcomeListener(),
						re, courseEntry, courseNode.getIdent(),
						deliveryOptions, overrideOptions, true, true, true);
				listenTo(previewQTI21Ctrl);
				stackPanel.pushController(translate("preview"), previewQTI21Ctrl);
			} else {
				if (qtiModule.isRunEnabled()) {
					long courseResId = course.getResourceableId().longValue();
					Controller previewController = iqManager.createIQDisplayController(moduleConfiguration, new IQPreviewSecurityCallback(), ureq, getWindowControl(), courseResId, courseNode.getIdent(), null);
					previewLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), previewController);
					stackPanel.pushController(translate("preview"), previewLayoutCtr);
				} else {
					showError("error.qti12");
				}
			}
		}
	}
	
	/**
	 * Delete the test session created by the preview controller
	 * for the QTI 2.1 tests.
	 * 
	 */
	private void cleanUpQti21PreviewSession() {
		if(previewQTI21Ctrl != null) {
			AssessmentTestSession previewSession = previewQTI21Ctrl.getCandidateSession();
			qti21service.deleteAssessmentTestSession(previewSession);
		}
	}
	
	private void doChooseTestAndSurvey(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					SurveyFileResource.TYPE_NAME, translate("command.chooseSurvey"));
		} else { // test and selftest use same repository resource type
			String[] types = qtiModule.isRunEnabled() ? QTI_21_AND_12_RESOURCE : QTI_21_RESOURCE;
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types,
					translate("command.chooseTest"));
		}			
		listenTo(searchController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.chooseRepFile"));
		cmc.activate();
	}
	
	private void doChangeSelfTest(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		String[] types = qtiModule.isRunEnabled() ? QTI_21_AND_12_RESOURCE : QTI_21_RESOURCE;
		searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
		listenTo(searchController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
		listenTo(searchController);
		cmc.activate();
	}
	
	private void doChangeTestAndSurvey(UserRequest ureq, RepositoryEntry re) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		String[] types;
		if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {//test
			types = qtiModule.isRunEnabled() ? QTI_21_AND_12_RESOURCE : QTI_21_RESOURCE;
		} else {//survey
			types = new String[]{ SurveyFileResource.TYPE_NAME };
		}
		
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) == null) {
			updateQtiType(re);
		}

		int onyxSuccess = 0;
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null && moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2)) {
			if (courseNode.getClass().equals(IQSURVCourseNode.class)) {
				File surveyDir = new File(course.getCourseEnvironment().getCourseBaseContainer()
						.getBasefile() + File.separator + courseNode.getIdent() + File.separator);
				if (surveyDir != null && surveyDir.exists() && surveyDir.listFiles().length > 0) {
					onyxSuccess = surveyDir.listFiles().length;
				}
			} else {
				onyxSuccess = QTIResultManager.getInstance().countResults(course.getResourceableId(), courseNode.getIdent(), re.getKey());
			}
		}
	
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
				&& moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2)
				&& onyxSuccess > 0) {
			showError("error.onyx");
		} else {
			
			List<QTIResult> results = QTIResultManager.getInstance().selectResults(course.getResourceableId(), courseNode.getIdent(), re.getKey(), null, 1);
			// test was passed from an user
			boolean passed = (results != null && !results.isEmpty()) ? true : false;
			// test was started and not passed
			// it exists partly results for this test
			List<Identity> identitiesWithQtiSerEntry = iqManager.getIdentitiesWithQtiSerEntry(course.getResourceableId(), courseNode.getIdent());
			if(passed || !identitiesWithQtiSerEntry.isEmpty()) {
				List<Identity> learners = new ArrayList<>();
				for(QTIResult result : results) {
					Identity identity = result.getResultSet().getIdentity();
					if(identity != null && !learners.contains(identity)){
						learners.add(identity);
					}
				}
				// add identities with qti.ser entry
				for (Identity identity : identitiesWithQtiSerEntry) {
					if(!learners.contains(identity)) {
						learners.add(identity);
					}
				}
				replaceWizard = new IQEditReplaceWizard(ureq, getWindowControl(), course, courseNode, types, learners, results, identitiesWithQtiSerEntry.size());
				replaceWizard.addControllerListener(this);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), replaceWizard.getInitialComponent());
				cmc.activate();
			} else {
				if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {//test					
					searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
				} else {//survey
					searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseSurvey"));
				}
				listenTo(searchController);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
				cmc.activate();
			}
		}	
	}

	/**
	 * This method updates the QTI Type in the editortreemodel.
	 * 
	 * @param re Needed to check if this Test is of QTI Type 2.1
	 */
	private void updateQtiType(RepositoryEntry re) {
		if (QTIResourceTypeModule.isOnyxTest(re.getOlatResource())) {
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
		} else if(QTIResourceTypeModule.isQtiWorks(re.getOlatResource())) {
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
		} else {
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI1);
		}
	}

	private boolean checkManualCorrectionNeeded(RepositoryEntry re) {
		if(QTIResourceTypeModule.isOnyxTest(re.getOlatResource())) {
			return false;
		}
		if(courseNode instanceof IQSURVCourseNode || courseNode instanceof IQSELFCourseNode) {
			//nothing to do
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			if(needManualCorrectionQTI21(re)) {
				showWarning("warning.test.with.essay");
				return true;
			}
		} else {
			if(needManualCorrectionQTI12(re)) {
				showWarning("warning.test.with.essay");
				return true;
			}
		}
		return false;
	}
	
	private boolean needManualCorrectionQTI21(RepositoryEntry re) {
		return qti21service.needManualCorrection(re);
	}
	
	private PassedType getPassedType(RepositoryEntry re, QTI21DeliveryOptions deliveryOptions) {
		if(deliveryOptions == null) return PassedType.none;
	
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(re.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qti21service.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		
		Double cutValue = null;
		if(assessmentTest != null) {
			cutValue = new AssessmentTestBuilder(assessmentTest).getCutValue();
		}
		return deliveryOptions.getPassedType(cutValue);
	}
	
	private boolean needManualCorrectionQTI12(RepositoryEntry re) {
		boolean needManualCorrection = false;
		QTIDocument doc = TestFileResource.getQTIDocument(re.getOlatResource());
		if(doc != null && doc.getAssessment() != null) {
			Assessment ass = doc.getAssessment();
			//Sections with their Items
			List<Section> sections = ass.getSections();
			for (Section section:sections) {
				List<Item> items = section.getItems();
				for (Item item:items) {
					String ident = item.getIdent();
					if(ident != null && ident.startsWith("QTIEDIT:ESSAY")) {
						needManualCorrection = true;
						break;
					}
				}
			}
		}
		return needManualCorrection;
	}
	
	private void doIQReference(UserRequest urequest, RepositoryEntry re, boolean manualCorrection) {
		// repository search controller done				
		if (re != null) {
			if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(re.getOlatResource(), null)) {
				LockResult lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(re.getOlatResource(), urequest.getIdentity(), null, getWindow());
				String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(lockResult.getOwner());
				showError("error.entry.locked", fullName);
				if(lockResult.isSuccess()) {
					//improbable concurrency security
					CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
				}
			} else {
				if(editTestButton != null) {
					myContent.remove(editTestButton);
				}

				IQEditController.setIQReference(re, moduleConfiguration);
				String displayName = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview.link", "command.preview", displayName, Link.NONTRANSLATED, myContent, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setCustomEnabledLinkCSS("o_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
				previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);
				previewButton.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				myContent.contextPut("dontRenderRepositoryButton", Boolean.valueOf(true));
				// If of type test, get min, max, cut - put in module config and push
				// to velocity
				
				boolean isOnyx = QTIResourceTypeModule.isOnyxTest(re.getOlatResource());
				myContent.contextPut("isOnyx", Boolean.valueOf(isOnyx));
				if(isOnyx) {
					myContent.contextPut("onyxDisplayName", displayName);
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
				} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
					if (isEditable(re)) {
						editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
					}
				} else {
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI1);
					if (isEditable(re)) {
						editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
					}
				}
				
				if(manualCorrection) {
					myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "manual");
				} else {
					myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "auto");
				}
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * Get the qti file soft key repository reference 
	 * @param config
	 * @param strict
	 * @return RepositoryEntry
	 */
	private RepositoryEntry getIQReference() {
		if (moduleConfiguration == null) return null;
		String repoSoftkey = (String)moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) return null;
		return repositoryManager.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		//child controllers registered with listenTo() get disposed in BasicController
		if (previewLayoutCtr != null) {
			previewLayoutCtr.dispose();
			previewLayoutCtr = null;
		}
		cleanUpQti21PreviewSession();
	}
}