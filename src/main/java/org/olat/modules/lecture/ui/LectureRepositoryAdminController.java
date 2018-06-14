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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.export.LecturesBlocksEntryExport;
import org.olat.modules.lecture.ui.export.RepositoryEntryAuditLogExport;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositoryAdminController extends BasicController implements TooledController, Activateable2 {
	
	private Link archiveLink, logLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	private final Link lecturesLink, appealsLink, settingsLink, participantsLink;
	
	private AppealListRepositoryController appealsCtrl;
	private LectureListRepositoryController lecturesCtrl;
	private final LectureRepositorySettingsController settingsCtrl;
	private ParticipantListRepositoryController participantsCtrl;
	
	private RepositoryEntry entry;
	private boolean configurationChanges = false;
	private final boolean isAdministrativeUser;
	private final boolean authorizedAbsenceEnabled;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public LectureRepositoryAdminController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		this.stackPanel = stackPanel;
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		mainVC = createVelocityContainer("admin_repository");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);

		lecturesLink = LinkFactory.createLink("repo.lectures.block", mainVC, this);
		participantsLink = LinkFactory.createLink("repo.participants", mainVC, this);
		appealsLink = LinkFactory.createLink("repo.lectures.appeals", mainVC, this);
		settingsLink = LinkFactory.createLink("repo.settings", mainVC, this);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Settings"), null);
		settingsCtrl = new LectureRepositorySettingsController(ureq, swControl, entry);
		listenTo(settingsCtrl);
		
		if(settingsCtrl.isLectureEnabled()) {
			segmentView.addSegment(lecturesLink, true);
			segmentView.addSegment(participantsLink, false);
			if(lectureModule.isAbsenceAppealEnabled()) {
				segmentView.addSegment(appealsLink, false);
			}
			doOpenLectures(ureq);
		} else {
			doOpenSettings(ureq);
		}
		segmentView.addSegment(settingsLink, !settingsCtrl.isLectureEnabled());

		putInitialPanel(mainVC);
	}
	
	public boolean hasConfigurationChanges() {
		return configurationChanges;
	}
	
	public void configurationChangesConsumed() {
		configurationChanges = false;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void initTools() {
		archiveLink = LinkFactory.createToolLink("archive.entry", translate("archive.entry"), this);
		archiveLink.setIconLeftCSS("o_icon o_icon_archive_tool");
		archiveLink.setVisible(settingsCtrl.isLectureEnabled());
		stackPanel.addTool(archiveLink, Align.right);
		
		logLink = LinkFactory.createToolLink("log", translate("log"), this);
		logLink.setIconLeftCSS("o_icon o_icon_log");
		logLink.setVisible(settingsCtrl.isLectureEnabled());
		stackPanel.addTool(logLink, Align.right);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("LectureBlocks".equalsIgnoreCase(name)) {
			doOpenLectures(ureq);
			segmentView.select(lecturesLink);
		} else if("Participants".equalsIgnoreCase(name)) {
			doOpenParticipants(ureq);
			segmentView.select(participantsLink);
		} else if("Settings".equalsIgnoreCase(name)) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		} else if("Appeals".equalsIgnoreCase(name)) {
			if(lectureModule.isAbsenceAppealEnabled()) {
				doOpenAppeals(ureq);
				segmentView.select(appealsLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == lecturesLink) {
					doOpenLectures(ureq);
				} else if (clickedLink == settingsLink){
					doOpenSettings(ureq);
				} else if(clickedLink == participantsLink) {
					doOpenParticipants(ureq);
				} else if(clickedLink == appealsLink) {
					doOpenAppeals(ureq);
				}
			}
		} else if(archiveLink == source) {
			doExportArchive(ureq);
		} else if(logLink == source) {
			doExportLog(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(settingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateSegments();
				configurationChanges = true;
			}
		}
	}
	
	/**
	 * Update the segment view after a change in the configuration.
	 */
	private void updateSegments() {
		if(settingsCtrl.isLectureEnabled()) {
			if(segmentView.getSegments().size() == 1) {
				if(lectureModule.isAbsenceAppealEnabled()) {
					segmentView.addSegment(0, appealsLink, false);
				}
				segmentView.addSegment(0, participantsLink, false);
				segmentView.addSegment(0, lecturesLink, false);
			}
		} else if(segmentView.getSegments().size() > 1) {
			// remove the unused segments
			segmentView.removeSegment(lecturesLink);
			segmentView.removeSegment(participantsLink);
			segmentView.removeSegment(appealsLink);
		}	
	}

	private void doOpenLectures(UserRequest ureq) {
		if(lecturesCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("LectureBlocks");
			WindowControl swControl = addToHistory(ureq, ores, null);
			lecturesCtrl = new LectureListRepositoryController(ureq, swControl, entry);
			listenTo(lecturesCtrl);
		} else {
			addToHistory(ureq, lecturesCtrl);
		}
		mainVC.put("segmentCmp", lecturesCtrl.getInitialComponent());
	}
	
	private void doOpenSettings(UserRequest ureq) {
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
		addToHistory(ureq, settingsCtrl);
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		if(participantsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("Participants");
			WindowControl swControl = addToHistory(ureq, ores, null);
			participantsCtrl = new ParticipantListRepositoryController(ureq, swControl, entry, false, true);
			listenTo(participantsCtrl);
		} else {
			addToHistory(ureq, participantsCtrl);
		}
		mainVC.put("segmentCmp", participantsCtrl.getInitialComponent());
	}
	
	private void doOpenAppeals(UserRequest ureq) {
		if(appealsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("Appeals");
			WindowControl swControl = addToHistory(ureq, ores, null);
			appealsCtrl = new AppealListRepositoryController(ureq, swControl, entry);
			listenTo(appealsCtrl);
		} else {
			addToHistory(ureq, appealsCtrl);
		}
		mainVC.put("segmentCmp", appealsCtrl.getInitialComponent());
	}
	
	private void doExportArchive(UserRequest ureq) {
		LecturesBlocksEntryExport archive = new LecturesBlocksEntryExport(entry, isAdministrativeUser, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(archive);
	}
	
	private void doExportLog(UserRequest ureq) {
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(entry);
		RepositoryEntryAuditLogExport archive = new RepositoryEntryAuditLogExport(entry, auditLog, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(archive);
	}
}