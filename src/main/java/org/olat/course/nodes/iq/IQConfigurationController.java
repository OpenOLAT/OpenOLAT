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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.INodeFilter;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishProcess;
import org.olat.course.editor.PublishSetInformations;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.PublishTreeModel;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.AssessmentTestInfos;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
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
import org.olat.resource.OLATResource;
import org.olat.resource.references.ReferenceHistory;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private final ICourse course;
	private final boolean newReference;
	private final ModuleConfiguration moduleConfiguration;
	private final AbstractAccessableCourseNode courseNode;
	private final boolean selfAssessment;

	@Autowired
	private QTI21Module qti21Module;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qti21service;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private ReferenceManager referenceManager;
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
		this.selfAssessment = courseNode instanceof IQSELFCourseNode;
		if (stackPanel != null) {
			stackPanel.addListener(this);
		}
		
		myContent = createVelocityContainer("edit");
		
		switch(type) {
			case QTI21Constants.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				myContent.contextPut("helpUrl", "manual_user/learningresources/Course_Element_Test/#test-configuration");
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				myContent.contextPut("helpUrl", "manual_user/learningresources/Course_Element_Self_Test/");
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				myContent.contextPut("helpUrl", "manual_user/learningresources/Course_Element_Form/");
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
		RepositoryEntry iqEntry = getIQReference();
		newReference = iqEntry == null;
		IQCourseNodeReferenceProvider referenceProvider = new IQCourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, iqEntry, referenceProvider);
		listenTo(referenceCtrl);
		myContent.put("reference", referenceCtrl.getInitialComponent());
		
		putInitialPanel(myContent);
		updateEditController(ureq, false);
	}
	
	public class IQCourseNodeReferenceProvider extends CourseNodeReferenceProvider {
		
		public IQCourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
			EmptyStateConfig emptyStateConfig, String selectionTitle, ReferenceContentProvider referenceContentProvider) {
			super(repositoryService, resourceTypes, emptyStateConfig, selectionTitle, referenceContentProvider);
		}
		
		@Override
		public boolean canCreate() {
			return newReference;
		}
		
		@Override
		public boolean hasReferencesHistory() {
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			List<ReferenceHistory> refs = referenceManager.getReferencesHistoryOf(courseEntry.getOlatResource(), courseNode.getIdent());
			return refs.size() > 1;
		}
		
		@Override
		public Controller getReferencesHistoryController(UserRequest ureq, WindowControl wControl) {
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			return new ReferencesHistoryController(ureq, wControl, courseEntry, courseNode.getIdent(), getIQReference());
		}

		@Override
		public Confirm confirmCanReplace(UserRequest ureq) {
			if(newReference || isNewCourseNode() || canPublish(ureq)) {
				return new Confirm(true, null);
			}
			String warning = translate("warning.publish");
			return new Confirm(false, warning);
		}
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
				String infoI18nKey;
				if(correctionGrading) {
					correctionMode = IQEditController.CORRECTION_GRADING;
					infoI18nKey = "replaced.grading";
				} else if(needManualCorrection) {
					correctionMode = IQEditController.CORRECTION_MANUAL;
					infoI18nKey = "replaced.manual";
				} else if(getPassedType(re, deliveryOptions) == PassedType.manually) {
					correctionMode = IQEditController.CORRECTION_MANUAL;
					infoI18nKey = "replaced.manual.passed";
				} else {
					correctionMode = IQEditController.CORRECTION_AUTO;
					infoI18nKey = "replaced.auto";
				}
				if (!selfAssessment) {
					showInfo(infoI18nKey);
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
			
			AssessmentTestInfos assessmentTestInfos = qti21service.getAssessmentTestInfos(re);
			if (assessmentTestInfos != null) {
				maxValue = assessmentTestInfos.estimatedMaxScore();
				if(maxValue == null) {
					maxValue = assessmentTestInfos.maxScore();
				}
				cutValue = assessmentTestInfos.cutValue();
				minValue = assessmentTestInfos.minScore();
			}
			Float min = minValue == null ? null : minValue.floatValue();
			Float max = maxValue == null ? null : maxValue.floatValue();
			
			updateReferenceContentUI(re, deliveryOptions, needManualCorrection, correctionGrading, min, max, cutValue);
			
			mod21ConfigForm = new QTI21EditForm(ureq, getWindowControl(), course, courseNode,
					NodeAccessType.of(course), deliveryOptions, needManualCorrection, correctionGrading,
					selfAssessment, min, max);
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
		
		if (!selfAssessment) {
			Long sessionsCount = qti21service.getAssessmentTestSessionsCount(
					course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent(),
					testEntry);
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.sessions"), String.valueOf(sessionsCount)));
		}
		
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
				if(re != null && re.equals(getIQReference())) {
					showWarning("warning.same.resource");
				} else {
					referenceCtrl.setRepositoryEntry(urequest, getIQReference());
					doConfirmChangeTestAndSurvey(urequest, re);
				}
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(urequest);
			}
		} else if(source == confirmChangeResourceCtrl) {
			if(event == Event.DONE_EVENT) {
				RepositoryEntry newEntry = confirmChangeResourceCtrl.getNewTestEntry();
				doChangeResource(urequest, newEntry, true);
			}
			cmc.deactivate();
			cleanUp();
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
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmChangeResourceCtrl);
		removeAsListenerAndDispose(cmc);
		confirmChangeResourceCtrl = null;
		cmc = null;
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
			CourseNode publishedNode = course.getRunStructure().getNode(courseNode.getIdent());
		
			Set<Identity> assessedIdentities = new HashSet<>();
			// If there is already a test linked to this course element and the element was published at least once
			if(currentEntry != null && publishedNode != null) {
				List<AssessmentTestSession> assessmentTestSessions = qti21service.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), currentEntry);
				for(AssessmentTestSession assessmentTestSession:assessmentTestSessions) {
					if(assessmentTestSession.getIdentity() != null) {
						assessedIdentities.add(assessmentTestSession.getIdentity());
					}
				}
				
				confirmChangeResourceCtrl = new ConfirmChangeResourceController(ureq, getWindowControl(), course,
						(QTICourseNode) courseNode, newEntry, currentEntry, new ArrayList<>(assessedIdentities));
				listenTo(confirmChangeResourceCtrl);
				String title = translate("replace.entry");
				cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeResourceCtrl.getInitialComponent(), title);
				listenTo(cmc);
				cmc.activate();
			} else {
				doChangeResource(ureq, newEntry, false);
			}
		} catch (Exception e) {
			logError("", e);
			showError("error.resource.corrupted");
		}
	}
	
	private boolean isNewCourseNode() {
		CourseNode publishedNode = course.getRunStructure().getNode(courseNode.getIdent());
		return publishedNode == null;
	}
	
	private boolean canPublish(UserRequest ureq) {
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		PublishProcess publishProcess = PublishProcess.getInstance(course, cetm, getLocale());
		PublishTreeModel publishTreeModel = publishProcess.getPublishTreeModel();
 
		boolean hasErrors = false;
		if (publishTreeModel.hasPublishableChanges()) {
			List<String> nodeToPublish = new ArrayList<>();
			visitPublishModel(publishTreeModel.getRootNode(), publishTreeModel, nodeToPublish);

			//only add selection if changes were possible
			for(Iterator<String> selectionIt=nodeToPublish.iterator(); selectionIt.hasNext(); ) {
				String ident = selectionIt.next();
				TreeNode node = publishProcess.getPublishTreeModel().getNodeById(ident);
				if(!publishTreeModel.isSelectable(node)) {
					selectionIt.remove();
				}
			}

			publishProcess.createPublishSetFor(nodeToPublish);
			
			PublishSetInformations set = publishProcess.testPublishSet(getLocale());
			StatusDescription[] status = set.getWarnings();
			for(int i = 0; i < status.length; i++) {
				if(status[i].isError()) {
					hasErrors |= true;
				}
			}
		} else {
			List<String> changes = new ArrayList<>();
			visitModel(cetm.getRootNode(), changes);
			// Changes but not publishable -> some part of the tree is cannot be published
			hasErrors = !changes.isEmpty();
		}
		return !hasErrors;
	}
	
	private void doChangeResource(UserRequest ureq, RepositoryEntry newEntry, boolean publish) {
		try {
			boolean needManualCorrection = checkManualCorrectionNeeded(newEntry);
			doIQReference(ureq, newEntry, needManualCorrection, publish);
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
			BigDecimal scoreScale = ScoreScalingHelper.getScoreScale(courseNode);
			QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
			QTI21OverrideOptions overrideOptions = QTI21OverrideOptions.nothingOverriden();
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			previewQTI21Ctrl = new AssessmentTestDisplayController(ureq, getWindowControl(), new InMemoryOutcomeListener(),
					re, courseEntry, courseNode.getIdent(), deliveryOptions, overrideOptions, scoreScale, true, true, true);
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

		AssessmentTestInfos assessmentTestInfos = qti21service.getAssessmentTestInfos(re);
		Double cutValue = assessmentTestInfos == null ? null : assessmentTestInfos.cutValue();
		return deliveryOptions.getPassedType(cutValue);
	}
	
	private void doIQReference(UserRequest urequest, RepositoryEntry re, boolean manualCorrection, boolean publish) {
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
			IQEditController.setIQReference(re, moduleConfiguration);
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
			
			if(manualCorrection) {
				myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "manual");
			} else {
				myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "auto");
			}
			
			OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			referenceManager.addReferenceToHistory(courseResource, re.getOlatResource(), courseNode.getIdent(), getIdentity());

			referenceCtrl.setRepositoryEntry(urequest, re);
			
			if(publish) {
				fireEvent(urequest, NodeEditController.NODECONFIG_PUBLISH_EVENT);
			} else {
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
		cleanUpQti21PreviewSession();
        super.doDispose();
	}
	
	private static void visitPublishModel(TreeNode node, INodeFilter filter, Collection<String> nodeToPublish) {
		int numOfChildren = node.getChildCount();
		for (int i = 0; i < numOfChildren; i++) {
			INode child = node.getChildAt(i);
			if (child instanceof TreeNode && filter.isVisible(child)) {
				nodeToPublish.add(child.getIdent());
				visitPublishModel((TreeNode)child, filter, nodeToPublish);
			}
		}
	}
	
	private static void visitModel(TreeNode node,  Collection<String> nodeToPublish) {
		int numOfChildren = node.getChildCount();
		for (int i = 0; i < numOfChildren; i++) {
			INode child = node.getChildAt(i);
			if (child instanceof CourseEditorTreeNode courseEditorTreeNode) {
				if(courseEditorTreeNode.isDeleted() || courseEditorTreeNode.isDirty() || courseEditorTreeNode.isNewnode()) {
					nodeToPublish.add(child.getIdent());
				}
				visitModel((TreeNode)child, nodeToPublish);
			}
		}
	}
}