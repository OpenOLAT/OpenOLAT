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
package org.olat.modules.lecture.ui.coach;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.modules.lecture.ui.event.SelectLectureBlockEvent;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesSearchController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel stackPanel;
	
	private LecturesListController listCtrl;
	private TeacherRollCallController rollCallCtrl;
	private LecturesSearchFormController searchForm;
	private LecturesListSegmentController multipleUsersCtrl;
	
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LecturesSearchController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;

		searchForm = new LecturesSearchFormController(ureq, getWindowControl());
		listenTo(searchForm);
		
		stackPanel = new TooledStackedPanel("ca-lectures-search", getTranslator(), this);
		stackPanel.setNeverDisposeRootController(true);
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.pushController(translate("search.curriculums"), searchForm);
		putInitialPanel(stackPanel);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchForm == source) {
			if(event == Event.DONE_EVENT) {
				cleanUp();
				doSearch(ureq);
			}	
		} else if(listCtrl == source || multipleUsersCtrl == source) {
			if(event instanceof  SelectLectureBlockEvent) {
				SelectLectureBlockEvent slbe = (SelectLectureBlockEvent)event;
				doSelectLectureBlock(ureq, slbe.getLectureBlock());
			}
		} else if(rollCallCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				doCloseRollCall();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doCloseRollCall() {
		stackPanel.popController(rollCallCtrl);
		removeAsListenerAndDispose(rollCallCtrl);// don't clean up the others
		rollCallCtrl = null;
		
		List<LectureBlockIdentityStatistics> statistics = searchStatistics();
		if(listCtrl != null) {
			listCtrl.reloadModel(statistics);
		}
		if(multipleUsersCtrl != null) {
			multipleUsersCtrl.reloadModel(statistics);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(multipleUsersCtrl);
		removeAsListenerAndDispose(rollCallCtrl);
		removeAsListenerAndDispose(listCtrl);
		multipleUsersCtrl = null;
		rollCallCtrl = null;
		listCtrl = null;
	}
	
	private List<LectureBlockIdentityStatistics> searchStatistics() {
		LectureStatisticsSearchParameters params = searchForm.getSearchParameters();
		List<UserPropertyHandler> userPropertyHandlers = searchForm.getUserPropertyHandlers();
		return lectureService
				.getLecturesStatistics(params, userPropertyHandlers, getIdentity());
	}
	
	private void doSearch(UserRequest ureq) {
		List<UserPropertyHandler> userPropertyHandlers = searchForm.getUserPropertyHandlers();
		List<LectureBlockIdentityStatistics> statistics = searchStatistics();
		Set<Long> identities = statistics.stream().map(LectureBlockIdentityStatistics::getIdentityKey)
			     .collect(Collectors.toSet());
		
		Controller ctrl;
		if(identities.size() <= 1) {
			listCtrl = new LecturesListController(ureq, getWindowControl(), statistics,
					userPropertyHandlers, LecturesSearchFormController.PROPS_IDENTIFIER, true, true);
			listenTo(listCtrl);
			ctrl = listCtrl;
		} else {
			multipleUsersCtrl = new LecturesListSegmentController(ureq, getWindowControl(), statistics,
					userPropertyHandlers, LecturesSearchFormController.PROPS_IDENTIFIER);
			listenTo(multipleUsersCtrl);
			ctrl = multipleUsersCtrl;
		}

		stackPanel.pushController(translate("results"), ctrl);
	}
	
	private void doSelectLectureBlock(UserRequest ureq, LectureBlock lectureBlock) {
		removeAsListenerAndDispose(rollCallCtrl);
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		List<Identity> participants = lectureService.getParticipants(reloadedBlock);
		RollCallSecurityCallback rollCallSecCallback = new RollCallSecurityCallbackImpl(secCallback.canReopenLectureBlock(), secCallback.canReopenLectureBlock(),
				false, reloadedBlock, lectureModule);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(),
				reloadedBlock, participants, rollCallSecCallback, false);
		listenTo(rollCallCtrl);
		stackPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
	}
}
