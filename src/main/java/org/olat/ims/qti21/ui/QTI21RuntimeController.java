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
package org.olat.ims.qti21.ui;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.ims.qti21.ui.event.RestartEvent;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.AssessableResource;
import org.olat.modules.assessment.ui.AssessmentToolController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 23.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21RuntimeController extends RepositoryEntryRuntimeController  {
	
	private Link assessmentLink, testStatisticLink, qtiOptionsLink;

	private QTI21DeliveryOptionsController optionsCtrl;
	private AssessmentToolController assessmentToolCtrl;
	private QTI21RuntimeStatisticsController statsToolCtr;
	
	private boolean reloadRuntime = false;

	@Autowired
	private QTI21Service qtiService;

	public QTI21RuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initRuntimeTools(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
			editLink = LinkFactory.createToolLink("edit.cmd", translate("details.openeditor"), this, "o_sel_repository_editor");
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			editLink.setEnabled(!managed);
			toolsDropdown.addComponent(editLink);
			
			membersLink = LinkFactory.createToolLink("members", translate("details.members"), this, "o_sel_repo_members");
			membersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_membersmanagement");
			toolsDropdown.addComponent(membersLink);
		}
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCourseCoach() || reSecurity.isGroupCoach()) {
			assessmentLink = LinkFactory.createToolLink("assessment", translate("command.openassessment"), this, "o_icon_assessment_tool");
			assessmentLink.setElementCssClass("o_sel_course_assessment_tool");
			toolsDropdown.addComponent(assessmentLink);

			testStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.openteststatistic"), this, "o_icon_statistics_tool");
			toolsDropdown.addComponent(testStatisticLink);
		}
		
		if (reSecurity.isEntryAdmin()) {
			RepositoryEntry re = getRepositoryEntry();
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = acService.isResourceAccessControled(re.getOlatResource(), null);
			ordersLink.setEnabled(booking);
			toolsDropdown.addComponent(ordersLink);	
		}
	}
	
	@Override
	protected void initSettingsTools(Dropdown settingsDropdown) {
		super.initSettingsTools(settingsDropdown);
		if (reSecurity.isEntryAdmin()) {
			settingsDropdown.addComponent(new Spacer(""));

			qtiOptionsLink = LinkFactory.createToolLink("options", translate("tab.options"), this, "o_sel_repo_options");
			qtiOptionsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_options");
			qtiOptionsLink.setElementCssClass("o_sel_qti_resource_options");
			settingsDropdown.addComponent(qtiOptionsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof AssessmentTestComposerController) {
			if(event == Event.CHANGED_EVENT) {
				reloadRuntime = true;
			}
		} else if(source instanceof AssessmentTestDisplayController) {
			if(event instanceof RestartEvent) {
				AssessmentTestDisplayController ctrl = (AssessmentTestDisplayController)source;
				doRestartRunningRuntimeController(ureq, ctrl.getCandidateSession());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(testStatisticLink == source) {
			doAssessmentTestStatistics(ureq);
		} else if(assessmentLink == source) {
			doAssessmentTool(ureq);
		} else if(qtiOptionsLink == source) {
			doQtiOptions(ureq);
		} else if(toolbarPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				Controller popedCtrl = pe.getController();
				if(popedCtrl instanceof AssessmentTestComposerController) {
					AssessmentTestComposerController composerCtrl = (AssessmentTestComposerController)popedCtrl;
					if(composerCtrl.hasChanges() || reloadRuntime) {
						doReloadRuntimeController(ureq);
					}
				} else if (popedCtrl instanceof QTI21DeliveryOptionsController) {
					QTI21DeliveryOptionsController optCtrl = (QTI21DeliveryOptionsController)popedCtrl;
					if(optCtrl.hasChanges() || reloadRuntime) {
						doReloadRuntimeController(ureq);
					}
				} else if(reloadRuntime) {
					doReloadRuntimeController(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	/**
	 * The method will clean up only the test session of the author.
	 * 
	 * @param ureq The user request
	 * @param candidateSession The test session of the author
	 */
	private void doRestartRunningRuntimeController(UserRequest ureq, AssessmentTestSession candidateSession) {
		disposeRuntimeController();
		if(reSecurity.isEntryAdmin()) {
			qtiService.deleteAuthorAssessmentTestSession(getRepositoryEntry(), candidateSession);
		}
		launchContent(ureq, reSecurity);
		if(toolbarPanel.getTools().isEmpty()) {
			initToolbar();
		}
		reloadRuntime = false;
	}

	/**
	 * The method will clean up all authors sessions.
	 * 
	 * @param ureq The user request
	 */
	private void doReloadRuntimeController(UserRequest ureq) {
		disposeRuntimeController();
		if(reSecurity.isEntryAdmin()) {
			qtiService.deleteAuthorsAssessmentTestSession(getRepositoryEntry());
		}
		launchContent(ureq, reSecurity);
		if(toolbarPanel.getTools().isEmpty()) {
			initToolbar();
		}
		reloadRuntime = false;
	}
	
	private Activateable2 doQtiOptions(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("Options");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin()) {
			QTI21DeliveryOptionsController ctrl = new QTI21DeliveryOptionsController(ureq, swControl, getRepositoryEntry());
			listenTo(ctrl);
			optionsCtrl = pushController(ureq, "Options", ctrl);
			currentToolCtr = optionsCtrl;
			setActiveTool(qtiOptionsLink);
			return optionsCtrl;
		}
		return null;
	}
	
	private Activateable2 doAssessmentTestStatistics(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("TestStatistics");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCourseCoach() || reSecurity.isGroupCoach()) {
			AssessmentToolOptions asOptions = new AssessmentToolOptions();
			asOptions.setAdmin(reSecurity.isEntryAdmin());
			QTI21RuntimeStatisticsController ctrl = new QTI21RuntimeStatisticsController(ureq, swControl, toolbarPanel,
					getRepositoryEntry(), asOptions);
			listenTo(ctrl);

			statsToolCtr = pushController(ureq, translate("command.openteststatistic"), ctrl);
			currentToolCtr = ctrl;
			setActiveTool(testStatisticLink);
			return statsToolCtr;
		}
		return null;
	}
	
	private Activateable2 doAssessmentTool(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("TestStatistics");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCourseCoach() || reSecurity.isGroupCoach()) {
			AssessmentToolSecurityCallback secCallback
				= new AssessmentToolSecurityCallback(reSecurity.isEntryAdmin(), reSecurity.isEntryAdmin(),
						reSecurity.isCourseCoach(), reSecurity.isGroupCoach(), null);

			AssessableResource el = getAssessableElement(getRepositoryEntry());
			AssessmentToolController ctrl = new AssessmentToolController(ureq, swControl, toolbarPanel,
					getRepositoryEntry(), el, secCallback);
			listenTo(ctrl);
			assessmentToolCtrl = pushController(ureq, translate("command.openassessment"), ctrl);
			currentToolCtr = assessmentToolCtrl;
			setActiveTool(assessmentLink);
			return assessmentToolCtrl;
		}
		return null;
	}
	
	private AssessableResource getAssessableElement(RepositoryEntry testEntry) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentTest);
		Double minScore = QtiNodesExtractor.extractMinScore(assessmentTest);
		boolean hasScore = assessmentTest.getOutcomeDeclaration(QTI21Constants.SCORE_IDENTIFIER) != null;
		boolean hasPassed = assessmentTest.getOutcomeDeclaration(QTI21Constants.PASS_IDENTIFIER) != null;
		return new QTI21AssessableResource(hasScore, hasPassed, true, true, minScore, maxScore, null);
	}
}