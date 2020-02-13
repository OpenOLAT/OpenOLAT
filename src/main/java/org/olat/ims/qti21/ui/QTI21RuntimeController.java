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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
import org.olat.modules.grading.ui.GradingRepositoryOverviewController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
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
	
	private Link gradingLink;
	private Link assessmentLink;
	private Link testStatisticLink;

	private AssessmentToolController assessmentToolCtrl;
	private QTI21RuntimeStatisticsController statsToolCtr;
	private GradingRepositoryOverviewController gradingCtr;
	
	private boolean reloadRuntime = false;

	@Autowired
	private QTI21Service qtiService;

	public QTI21RuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initToolsMenuRuntime(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			assessmentLink = LinkFactory.createToolLink("assessment", translate("command.openassessment"), this, "o_icon_assessment_tool");
			assessmentLink.setElementCssClass("o_sel_course_assessment_tool");
			toolsDropdown.addComponent(assessmentLink);
		}
		if (reSecurity.isEntryAdmin()) {
			gradingLink = LinkFactory.createToolLink("grading", translate("command.grading"), this, "o_icon_assessment_tool");
			gradingLink.setElementCssClass("o_sel_grading");
			toolsDropdown.addComponent(gradingLink);
		}
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			testStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.openteststatistic"), this, "o_icon_statistics_tool");
			toolsDropdown.addComponent(testStatisticLink);
		}
		
		super.initToolsMenuRuntime(toolsDropdown);
	}

	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new QTI21SettingsController(ureq, bwControl, toolbarPanel, refreshedEntry);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Grading".equalsIgnoreCase(type)) {
				entries = entries.subList(1, entries.size());
				Activateable2 ctrl = doGrading(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, entries, null);
				}
			} else if("TestStatistics".equalsIgnoreCase(type)) {
				entries = entries.subList(1, entries.size());
				Activateable2 ctrl = doAssessmentTestStatistics(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, entries, null);
				}
			} else if("AssessmentTool".equalsIgnoreCase(type)) {
				entries = entries.subList(1, entries.size());
				Activateable2 ctrl = doAssessmentTool(ureq);
				if(ctrl != null) {
					ctrl.activate(ureq, entries, null);
				}
			}
		}
		super.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ReloadSettingsEvent) {
			reloadRuntime = true;
		} else if(source instanceof AssessmentTestComposerController) {
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
		} else if(gradingLink == source) {
			doGrading(ureq);
		} else if(toolbarPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				Controller popedCtrl = pe.getController();
				if(popedCtrl instanceof AssessmentTestComposerController) {
					AssessmentTestComposerController composerCtrl = (AssessmentTestComposerController)popedCtrl;
					if(composerCtrl.hasChanges() || reloadRuntime) {
						doReloadRuntimeController(ureq);
					}
				} else if (popedCtrl instanceof QTI21SettingsController) {
					if(reloadRuntime) {
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
		launchContent(ureq);
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
		launchContent(ureq);
		if(toolbarPanel.getTools().isEmpty()) {
			initToolbar();
		}
		reloadRuntime = false;
	}
	
	private Activateable2 doAssessmentTestStatistics(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("TestStatistics");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
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
		OLATResourceable ores = OresHelper.createOLATResourceableType("AssessmentTool");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		if (reSecurity.isEntryAdmin() || reSecurity.isCoach()) {
			AssessmentToolSecurityCallback secCallback
				= new AssessmentToolSecurityCallback(reSecurity.isEntryAdmin(), reSecurity.isEntryAdmin(),
						reSecurity.isCourseCoach(), reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), null);

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
	
	private Activateable2 doGrading(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("Grading");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);

		if (reSecurity.isEntryAdmin()) {
			GradingRepositoryOverviewController ctrl = new GradingRepositoryOverviewController(ureq, swControl, toolbarPanel,
					getRepositoryEntry());
			listenTo(ctrl);
			gradingCtr = pushController(ureq, translate("command.grading"), ctrl);
			currentToolCtr = gradingCtr;
			setActiveTool(gradingLink);
			return gradingCtr;
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