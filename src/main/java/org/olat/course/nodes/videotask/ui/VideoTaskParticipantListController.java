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
package org.olat.course.nodes.videotask.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.manager.VideoTaskArchiveFormat;
import org.olat.course.nodes.videotask.model.VideoTaskArchiveSearchParams;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.resultexport.IdentitiesList;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskParticipantListController extends IdentityListCourseNodeController {
	
	private FormLink statsButton;
	private FormLink playAllButton;
	private FormLink deleteAllDataButton;
	private FormLink exportResultsButton;
	
	private VideoTaskAssessmentPlayController playCtrl;
	private VideoTaskDeleteDataController deleteDataCtrl;
	
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public VideoTaskParticipantListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}
	
	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		super.initGradeScaleEditButton(formLayout);
		super.initBulkStatusTools(ureq, formLayout);
		super.initResetDataTool(formLayout);

		String mode = courseNode.getModuleConfiguration().getStringValue(VideoTaskEditController.CONFIG_KEY_MODE,
				VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
		if(VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode)) {
			exportResultsButton = uifactory.addFormLink("button.export", formLayout, Link.BUTTON);
			exportResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			
			statsButton = uifactory.addFormLink("button.stats", formLayout, Link.BUTTON);
			statsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_statistics_tool");
		} else {
			playAllButton = uifactory.addFormLink("play.all", formLayout, Link.BUTTON);
			playAllButton.setIconLeftCSS("o_icon o_icon_video_play");
		}
		
		if(!coachCourseEnv.isCourseReadOnly()) {
			if(getAssessmentCallback().canDeleteData()) {
				DropdownItem moreDropdown = uifactory.addDropdownMenuMore("more.menu", formLayout, getTranslator());
				moreDropdown.setEmbbeded(true);
				moreDropdown.setButton(true);

				deleteAllDataButton = uifactory.addFormLink("tool.delete.data", formLayout, Link.LINK);
				deleteAllDataButton.setIconLeftCSS("o_icon o_icon_delete_item");
				moreDropdown.addElement(deleteAllDataButton);
			}
		}
	}
	
	@Override
	protected void initCalloutColumns(FlexiTableColumnModel columnsModel) {
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(IdentityCourseElementCols.tools);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(playCtrl == source) {
			if(event == Event.BACK_EVENT) {
				stackPanel.popController(playCtrl);
				cleanUp();
			}
		} else if(deleteDataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reload(ureq);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(deleteDataCtrl);
		removeAsListenerAndDispose(playCtrl);
		deleteDataCtrl = null;
		playCtrl = null;
		super.cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteAllDataButton == source) {
			doConfirmDeleteAllData(ureq);
		} else if(exportResultsButton == source) {
			doExportResults(ureq);
		} else if(statsButton == source) {
			doLaunchStatistics(ureq);
		} else if(playAllButton == source) {
			doPlayAll(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmDeleteAllData(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		CourseEnvironment courseEnv = getCourseEnvironment();
		deleteDataCtrl = new VideoTaskDeleteDataController(ureq, getWindowControl(),
				courseEnv, asOptions, (VideoTaskCourseNode)courseNode);
		listenTo(deleteDataCtrl);
		
		String title = translate("delete.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, deleteDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportResults(UserRequest ureq) {
		IdentitiesList identities = getIdentities(true);
		doExportResults(ureq, identities);
	}
	
	private void doExportResults(UserRequest ureq, IdentitiesList identities) {
		if (!identities.isEmpty()) {
			VideoTaskArchiveSearchParams searchParams = new VideoTaskArchiveSearchParams(courseEntry,
					getReferencedRepositoryEntry(), (VideoTaskCourseNode)courseNode);
			VideoTaskArchiveFormat archive = new VideoTaskArchiveFormat( getLocale(), searchParams);
			MediaResource results = archive.exportCourseElement();
			ureq.getDispatchResult().setResultingMediaResource(results);
		} else {
			showWarning("error.no.assessed.users");
		}
	}
	
	private void doLaunchStatistics(UserRequest ureq) {
		IdentitiesList identities = getIdentities(false);
		boolean canDelete = !coachCourseEnv.isCourseReadOnly() && getAssessmentCallback().isAdmin();
		Controller statisticsCtrl = new VideoTaskAssessmentStatisticsController(ureq, getWindowControl(), 
				stackPanel, coachCourseEnv, identities, (VideoTaskCourseNode)courseNode, canDelete);
		listenTo(statisticsCtrl);
		stackPanel.pushController(translate("button.stats"), statisticsCtrl);
	}
	
	private void doPlayAll(UserRequest ureq) {
		RepositoryEntry videoEntry = courseNode.getReferencedRepositoryEntry();
		IdentitiesList identities = getIdentities(false);
		List<VideoTaskSession> taskSessions = videoAssessmentService
				.getTaskSessions(courseEntry, courseNode.getIdent(), identities.getIdentities());
		playCtrl = new VideoTaskAssessmentPlayController(ureq, getWindowControl(), videoEntry, taskSessions,
				null, (VideoTaskCourseNode) courseNode, true);
		listenTo(playCtrl);

		stackPanel.pushController(translate("play"), playCtrl);
	}
	
}
