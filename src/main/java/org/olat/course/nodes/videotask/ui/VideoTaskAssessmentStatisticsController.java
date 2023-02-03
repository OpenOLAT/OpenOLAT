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

import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.duration;
import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.format;
import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.getModeString;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.ui.VideoTaskAssessmentDetailsTableModel.DetailsCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.ims.qti21.resultexport.IdentitiesList;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentTestStatisticsController;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 1 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentStatisticsController extends AbstractVideoTaskSessionListController {

	private FormLink resetButton;
	
	private final boolean canReset;
	private final IdentitiesList assessedIdentities;
	
	private VideoTaskResetDataController resetDataCtrl;

	public VideoTaskAssessmentStatisticsController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseEnvironment courseEnv,
			IdentitiesList assessedIdentities, VideoTaskCourseNode courseNode, boolean canReset) {
		super(ureq, wControl, "assessment_statistics", stackPanel, courseNode, courseEnv);
		setTranslator(Util.createPackageTranslator(QTI21AssessmentTestStatisticsController.class, getLocale(), getTranslator()));

		this.canReset = canReset;
		this.assessedIdentities = assessedIdentities;
		
		initForm(ureq);
		loadModel();
		
		int maxAttempts = courseNode.getModuleConfiguration()
				.getIntegerSafe(VideoTaskEditController.CONFIG_KEY_ATTEMPTS, tableModel.getMaxAttempts());
		initFilters(true, maxAttempts);
		updateStatistics(flc);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(canReset) {
			resetButton = uifactory.addFormLink("tool.delete.data", formLayout, Link.BUTTON); 
			resetButton.setIconLeftCSS("o_icon o_icon_delete_item");
		}
		super.initForm(formLayout, listener, ureq);
	}

	@Override
	protected void initColumnsIds(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DetailsCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.assessedIdentity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.attempt));
	}

	@Override
	protected void loadModel() {
		List<Identity> identities = assessedIdentities.getIdentities();
		List<VideoTaskSession> taskSessions = videoAssessmentService.getTaskSessions(entry, courseNode.getIdent(), identities);
		loadModel(taskSessions);
	}
	
	private void updateStatistics(FormItemContainer formLayout) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("courseId",  entry.getKey());
			layoutCont.contextPut("videoId",  videoEntry.getKey());

			layoutCont.contextPut("maxScore", AssessmentHelper.getRoundedScore(maxScore));
			String cutScore = cutValue == null ? "" : AssessmentHelper.getRoundedScore(cutValue);
			layoutCont.contextPut("cutScore", cutScore);

			List<VideoTaskSessionRow> rows = getVideoTaskSessionRows();
			List<VideoTaskSession> sessions = rows.stream()
					.map(VideoTaskSessionRow::getTaskSession)
					.collect(Collectors.toList());
			StatisticAssessment statistics = videoAssessmentService
					.getAssessmentStatistics(sessions, maxScore, cutValue, rounding);
			
			layoutCont.contextPut("numOfParticipants",  Integer.toString(statistics.getNumOfParticipants()));
			
			layoutCont.contextPut("numOfPassed", statistics.getNumOfPassed());
			layoutCont.contextPut("numOfFailed", statistics.getNumOfFailed());

			layoutCont.contextPut("average", format(statistics.getAverage()));
			layoutCont.contextPut("range", format(statistics.getRange()));
			layoutCont.contextPut("standardDeviation", format(statistics.getStandardDeviation()));
			layoutCont.contextPut("mode", getModeString(statistics.getMode()));
			layoutCont.contextPut("median", format(statistics.getMedian()));
			
			String duration = duration(statistics.getAverageDuration());
			layoutCont.contextPut("averageDuration", duration);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resetDataCtrl == source) {
			
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(resetDataCtrl);
		resetDataCtrl = null;
		super.cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetButton == source) {
			doConfirmResetData(ureq);
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void doInvalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
		UserCourseEnvironment assessedUserCourseEnv = getUserCourseEnvironment(taskSession.getAssessedIdentity());
		super.doInvalidate(ureq, taskSession, assessedUserCourseEnv);
	}

	@Override
	protected void doRevalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
		UserCourseEnvironment assessedUserCourseEnv = getUserCourseEnvironment(taskSession.getAssessedIdentity());
		super.doRevalidate(ureq, taskSession, assessedUserCourseEnv);
	}
	
	private UserCourseEnvironment getUserCourseEnvironment(Identity assessedIdentity) {
		IdentityEnvironment ienv = new IdentityEnvironment(assessedIdentity, Roles.userRoles());
		return new UserCourseEnvironmentImpl(ienv, courseEnv);
	}
	
	private void doConfirmResetData(UserRequest ureq) {
		List<Identity> identities = getIdentities();
		resetDataCtrl = new VideoTaskResetDataController(ureq, getWindowControl(),
				courseEnv, identities, courseNode);
		listenTo(resetDataCtrl);
		
		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
