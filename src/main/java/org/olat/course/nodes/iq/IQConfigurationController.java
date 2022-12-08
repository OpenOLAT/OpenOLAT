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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
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
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.QTI21OverrideOptions;
import org.olat.ims.qti21.ui.event.RestartEvent;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
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
public class IQConfigurationController extends BasicController implements ReferenceContentProvider {

	private static final List<String> RESOURCE_TYPES = List.of(ImsQTI21Resource.TYPE_NAME);
	
	private VelocityContainer myContent;
	private final BreadcrumbPanel stackPanel;
	private final IconPanelLabelTextContent iconPanelContent;
	
	private final RepositoryEntryReferenceController referenceCtrl;
	private CloseableModalController cmc;
	private AssessmentTestDisplayController previewQTI21Ctrl;
	private ConfirmChangeResourceController confirmChangeResourceCtrl;
	
	private QTI21EditForm mod21ConfigForm;
	
	private ICourse course;
	private ModuleConfiguration moduleConfiguration;
	private AbstractAccessableCourseNode courseNode;

	@Autowired
	private QTI21Module qti21Module;
	@Autowired
	private UserManager userManager;
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
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.moduleConfiguration = courseNode.getModuleConfiguration();
		this.course = course;
		this.courseNode = courseNode;
		if (stackPanel != null) {
			stackPanel.addListener(this);
		}
		
		myContent = createVelocityContainer("edit");
		
		switch(type) {
			case QTI21Constants.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				myContent.contextPut("helpUrl", "manual_user/course_elements/Assessment/#course_element_test");
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				myContent.contextPut("helpUrl", "manual_user/tests/Tests_at_course_level/");
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				myContent.contextPut("helpUrl", "manual_user/course_elements/Assessment/#course_element_form");
				break;
			default:
				break;
		}
		
		iconPanelContent = new IconPanelLabelTextContent("content");
		
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.test.resource.selected"))
				.withIconCss("o_icon o_FileResource-IMSQTI21_icon")
				.build();
		String selectionTitle = translate("select.test");
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, getIQReference(), referenceProvider);
		listenTo(referenceCtrl);
		myContent.put("reference", referenceCtrl.getInitialComponent());
		
		putInitialPanel(myContent);
		updateEditController(ureq, false);
	}
	
	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}
	
	/**
	 * Update the edit and layout controllers.
	 * 
	 * @param ureq The user request
	 * @param replacedTest Set true if the test was replaced by a new one.
	 */
	protected void updateEditController(UserRequest ureq, boolean replacedTest) {
		removeAsListenerAndDispose(mod21ConfigForm);
		mod21ConfigForm = null;
		
		RepositoryEntry re = getIQReference();
		myContent.contextPut("resouceAvailable", Boolean.valueOf(re != null));
		if(re == null) {
			myContent.remove("iqeditform");
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			boolean needManualCorrection = false;
			boolean correctionGrading = gradingService.isGradingEnabled(re, null);
			try {// in case of an unreadable test
				needManualCorrection = needManualCorrectionQTI21(re);
			} catch (Exception e) {
				logError("Test cannot be read: " + re, e);
				showError("error.resource.corrupted");
			}
			QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
			if(replacedTest) {// set some default settings in case the user don't save the next panel
				String correctionMode;
				if(correctionGrading) {
					correctionMode = IQEditController.CORRECTION_GRADING;
					showInfo("replaced.grading");
				} else if(needManualCorrection || getPassedType(re, deliveryOptions) == PassedType.manually) {
					correctionMode = IQEditController.CORRECTION_MANUAL;
					showInfo("replaced.manual");
				} else {
					correctionMode = IQEditController.CORRECTION_AUTO;
					showInfo("replaced.auto");
				}
				moduleConfiguration.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionMode);
				if(IQEditController.CORRECTION_GRADING.equals(correctionMode) || IQEditController.CORRECTION_MANUAL.equals(correctionMode)) {
					String userVisible = qti21Module.isResultsVisibleAfterCorrectionWorkflow()
							? IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION
							: IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION;
					moduleConfiguration.setStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, userVisible);
				} else {
					moduleConfiguration.remove(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION);
				}
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			
			Double minValue = null;
			Double maxValue = null;
			Double cutValue = null;
			
			FileResourceManager frm = FileResourceManager.getInstance();
			File unzippedDirRoot = frm.unzipFileResource(re.getOlatResource());
			ResolvedAssessmentTest resolvedAssessmentTest = qti21service.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			if (assessmentTest != null) {
				AssessmentTestBuilder testBuilder = new AssessmentTestBuilder(assessmentTest);
				maxValue = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
				if(maxValue == null) {
					maxValue = testBuilder.getMaxScore();
				}
				cutValue = testBuilder.getCutValue();
				if(maxValue != null && "OpenOLAT".equals(assessmentTest.getToolName())) {
					minValue = 0d;
				}
			}
			Float min = minValue == null ? null : minValue.floatValue();
			Float max = maxValue == null ? null : maxValue.floatValue();
			
			updateReferenceContentUI(re, deliveryOptions, needManualCorrection, correctionGrading, min, max, cutValue);
			
			mod21ConfigForm = new QTI21EditForm(ureq, getWindowControl(),
					course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode,
					NodeAccessType.of(course), deliveryOptions, needManualCorrection, correctionGrading,
					courseNode instanceof IQSELFCourseNode, min, max);
			mod21ConfigForm.updateUI();
			listenTo(mod21ConfigForm);
			myContent.put("iqeditform", mod21ConfigForm.getInitialComponent());
		} else {
			myContent.remove("iqeditform");
			showError("error.qti12");
		}
	}
	
	private void updateReferenceContentUI(RepositoryEntry testEntry, QTI21DeliveryOptions deliveryOptions,
			boolean needManualCorrection, boolean correctionGrading, Float min, Float max, Double cutValue) {
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		
		String correctionText = needManualCorrection
				? translate("correction.test.entry.manually")
				: translate("correction.test.entry.auto");
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("correction.test.entry"), correctionText));
		
		String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), min, max);
		if (scoreMinMax != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.min.max"), scoreMinMax));
		}
		
		PassedType passedType = deliveryOptions.getPassedType(cutValue);
		String passedTypeValue;
		switch (passedType) {
		case cutValue:
			passedTypeValue = translate("score.passed.cut.value", AssessmentHelper.getRoundedScore(cutValue));
			break;
		case manually:
			passedTypeValue = translate("score.passed.manually");
			break;
		default:
			passedTypeValue = translate("score.passed.none");
			break;
		}
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("show.passed"), passedTypeValue));
		
		if (correctionGrading) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("correction.workflow"),
					translate("correction.workflow.on")));
		}
		
		Long sessionsCount = qti21service.getAssessmentTestSessionsCount(
				course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent(),
				testEntry);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.sessions"), String.valueOf(sessionsCount)));
		
		iconPanelContent.setLabelTexts(labelTexts);
		
		String warning = qti21service.isAssessmentTestActivelyUsed(testEntry) ? translate("error.edit.restricted.in.use") : null;
		iconPanelContent.setWarning(warning);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (stackPanel == source) {
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
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				// Reset reference until the new entry is confirmed
				RepositoryEntry re = referenceCtrl.getRepositoryEntry();
				referenceCtrl.setRepositoryEntry(urequest, getIQReference());
				doConfirmChangeTestAndSurvey(urequest, re);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(urequest);
			}
		} else if(source == confirmChangeResourceCtrl) {
			if(event == Event.DONE_EVENT) {
				RepositoryEntry newEntry = confirmChangeResourceCtrl.getNewTestEntry();
				doChangeResource(urequest, newEntry);
			}
			cmc.deactivate();
		} else if (source == mod21ConfigForm) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(urequest, event);
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
			Set<Identity> assessedIdentities = new HashSet<>(); 
			if(currentEntry != null) {
				List<AssessmentTestSession> assessmentTestSessions = qti21service.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), currentEntry);
				
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
				confirmChangeResourceCtrl = new ConfirmChangeResourceController(ureq, getWindowControl(), course,
						(QTICourseNode) courseNode, newEntry, currentEntry, new ArrayList<>(assessedIdentities),
						numOfAssessedIdentities);
				listenTo(confirmChangeResourceCtrl);
				String title = translate("replace.entry");
				cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeResourceCtrl.getInitialComponent(), title);
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
		removeAsListenerAndDispose(previewQTI21Ctrl);
		
		RepositoryEntry re = getIQReference();
		if(re != null && ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
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
			showError("error.qti12");
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

	private boolean checkManualCorrectionNeeded(RepositoryEntry re) {
		if(courseNode instanceof IQSURVCourseNode || courseNode instanceof IQSELFCourseNode) {
			//nothing to do
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())
				&& needManualCorrectionQTI21(re)) {
			return true;
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
	
	private void doIQReference(UserRequest urequest, RepositoryEntry re, boolean manualCorrection) {
		if (re == null) {
			return;
		}
		
		if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(re.getOlatResource(), null)) {
			LockResult lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(re.getOlatResource(), urequest.getIdentity(), null, getWindow());
			String fullName = userManager.getUserDisplayName(lockResult.getOwner());
			showError("error.entry.locked", fullName);
			if(lockResult.isSuccess()) {
				//improbable concurrency security
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
			}
		} else if(!ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			showError("error.qti12");
		} else {
			referenceCtrl.setRepositoryEntry(urequest, re);
		
			IQEditController.setIQReference(re, moduleConfiguration);
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
			
			if(manualCorrection) {
				myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "manual");
			} else {
				myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "auto");
			}
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
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
		cleanUpQti21PreviewSession();
        super.doDispose();
	}
}