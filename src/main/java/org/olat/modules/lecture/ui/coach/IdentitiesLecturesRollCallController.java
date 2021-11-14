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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.UserListController;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.DailyRollCall;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.SingleParticipantRollCallsController;
import org.olat.modules.lecture.ui.coach.IdentitiesLecturesRollCallTableModel.IdentitiesLecturesCols;
import org.olat.modules.lecture.ui.component.ImmunityProofLevelCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.modules.lecture.ui.wizard.AbsenceNotice3LecturesEntriesStep;
import org.olat.modules.lecture.ui.wizard.AbsenceNoticeCancelStepCallback;
import org.olat.modules.lecture.ui.wizard.AbsenceNoticeFinishStepCallback;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesLecturesRollCallController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final int LECTURES_OFFSET = 1000;
	public static final String USER_USAGE_IDENTIFIER = UserListController.usageIdentifyer;
	
	private FormLink backLink;
	private FormLink closeButton;
	private FlexiTableElement tableEl;
	private IdentitiesLecturesRollCallTableModel tableModel;
	
	private int counter = 0;
	private final Formatter format;
	private final boolean immunoStatusEnabled;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<Identity> participants;
	private List<LectureBlock> lectureBlocks;
	private final Map<LectureBlock, List<Identity>> lectureBlocksMap;
	private final Map<Identity, List<LectureBlock>> reversedLectureBlocksMap;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	private final LecturesSecurityCallback secCallback;
	private final RollCallSecurityCallback rollCallSecCallback;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private StepsMainRunController addNoticeCtrl;
	private IdentityProfileController profileCtrl;
	private CloseLecturesController closeLecturesCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private SingleParticipantRollCallsController rollCallsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;
	
	public IdentitiesLecturesRollCallController(UserRequest ureq, WindowControl wControl,
			List<Identity> participants, Map<LectureBlock,List<Identity>> lectureBlocks, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "identities_lectures", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.secCallback = secCallback;
		this.participants = participants;
		this.lectureBlocksMap = lectureBlocks;
		format = Formatter.getInstance(getLocale());
		this.lectureBlocks = new ArrayList<>(lectureBlocksMap.keySet());
		reversedLectureBlocksMap = reverse(lectureBlocks);
		boolean teacher = secCallback.viewAs() == LectureRoles.teacher;
		boolean masterCoach = secCallback.viewAs() == LectureRoles.mastercoach;
		rollCallSecCallback = new RollCallSecurityCallbackImpl(false, masterCoach, teacher, null, lectureModule);
		
		Collections.sort(this.lectureBlocks, new LectureBlockStartDateComparator());
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_USAGE_IDENTIFIER, isAdministrativeUser);
		
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		
		immunoStatusEnabled = immunityProofModule.isEnabled() && lectureBlocks.keySet().stream()
				.anyMatch(block -> CalendarUtils.isSameDay(block.getStartDate(), ureq.getRequestTimestamp()));

		initForm(ureq);
		loadModel();
		updateCanClose();
	}
	
	private Map<Identity, List<LectureBlock>> reverse(Map<LectureBlock, List<Identity>> blockMap) {
		Map<Identity, List<LectureBlock>> identMap = new HashMap<>();
		for(Map.Entry<LectureBlock, List<Identity>> blockEntry : blockMap.entrySet()) {
			List<Identity> identities = blockEntry.getValue();
			for(Identity identity:identities) {
				identMap
					.computeIfAbsent(identity, i -> new ArrayList<>())
					.add(blockEntry.getKey());
			}
		}
		return identMap;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("date", getDate());
			String numOfLectureBlocksI18n = lectureBlocks.size() > 1 ? "rollcall.lecture.blocks" : "rollcall.lecture.block";
			layoutCont.contextPut("numOfLectureBlocks", translate(numOfLectureBlocksI18n, new String[] { Integer.toString(lectureBlocks.size()) }));
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(immunoStatusEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentitiesLecturesCols.immunoStatus,
					new ImmunityProofLevelCellRenderer(getTranslator())));
		}

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USER_USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "rollcall",
					true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentitiesLecturesCols.numOfAbsences));
		
		colIndex = LECTURES_OFFSET;
		for(int i=0; i<lectureBlocks.size(); i++) {
			DefaultFlexiColumnModel col = new DefaultFlexiColumnModel("", colIndex++);
			col.setHeaderLabel(getLectureLabel(lectureBlocks.get(i)));
			columnsModel.addFlexiColumnModel(col);
		}
		
		DefaultFlexiColumnModel rollCallCol = new DefaultFlexiColumnModel("rollcall.do", translate("table.header.details"), "rollcall");
		rollCallCol.setIconHeader("o_icon o_icon_lecture o_icon-lg");
		rollCallCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(rollCallCol);
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(IdentitiesLecturesCols.tools);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);

		tableModel = new IdentitiesLecturesRollCallTableModel(columnsModel, userPropertyHandlers, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 26, false, getTranslator(), formLayout);

		initCloseButton(formLayout);
	}
	
	private String getDate() {
		Date startDate = lectureBlocks.get(0).getStartDate();
		Formatter formatter = Formatter.getInstance(getLocale());
		String date = formatter.formatDate(startDate);
		String startDayOfWeek = formatter.dayOfWeek(startDate);
		
		String[] args = new String[] {
				date,						// 0
				startDayOfWeek				// 1
		};
		return translate("lecture.block.date", args);
	}
	
	private void initCloseButton(FormItemContainer formLayout) {
		if(secCallback.viewAs() == LectureRoles.teacher) {
			closeButton = uifactory.addFormLink("close", "close.lecture.blocks", null, formLayout, Link.BUTTON);
			closeButton.setVisible(false);
		}
	}
	
	private void updateCanClose() {
		if(closeButton == null) return;
		
		boolean canClose = lectureModule.getDailyRollCall() == DailyRollCall.daily;
		if(canClose) {
			canClose &= rollCallSecCallback.canClose(lectureBlocks);
		}
		closeButton.setVisible(canClose);
	}
	
	private String getLectureLabel(LectureBlock lectureBlock) {
		StringBuilder sb = new StringBuilder();
		if(lectureBlock.getStartDate() != null) {
			sb.append(format.formatTimeShort(lectureBlock.getStartDate()));
		}
		if(lectureBlock.getEndDate() != null) {
			if(sb.length() > 0) sb.append(" - ");
			sb.append(format.formatTimeShort(lectureBlock.getEndDate()));
		}
		if(sb.length() > 0) sb.append("<br>");
		sb.append(StringHelper.escapeHtml(lectureBlock.getTitle()));
		return sb.toString();
	}
	
	private void loadModel() {
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setLectureBlockRefs(lectureBlocks);
		List<LectureBlockRollCall> rollCallList = lectureService.getRollCalls(searchParams);
		Map<RollCallKey,LectureBlockRollCall> rollCallMap = rollCallList.stream()
				.collect(Collectors.toMap(r -> new RollCallKey(r.getIdentity().getKey(), r.getLectureBlock().getKey()), r -> r, (u, v) -> u));
		Map<Long,ImmunityProofLevel> immunoLevels = getImmunoLevels();
		
		List<IdentityLecturesRollCallsRow> rows = new ArrayList<>(participants.size());
		for(Identity participant:participants) {
			ImmunityProofLevel immunoStatus = immunoLevels.get(participant.getKey());
			IdentityLecturesRollCallsRow row = forgeRow(participant, immunoStatus);
			List<LectureBlock> participatingLectureBlocks = reversedLectureBlocksMap.get(participant);
			for(LectureBlock lectureBlock:lectureBlocks) {
				LectureBlockRollCall rollCall = rollCallMap.get(new RollCallKey(participant.getKey(), lectureBlock.getKey()));
				boolean participate = participatingLectureBlocks.contains(lectureBlock);
				
				IdentityLecturesRollCallPart part = new IdentityLecturesRollCallPart(lectureBlock, participate, rollCall);
				LectureBlockRollCallStatusItem statusItem = new LectureBlockRollCallStatusItem("status_" + (counter++), part,
						authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator());
				statusItem.setWithNumOfLectures(true);
				part.setStatusItem(statusItem);
				row.add(part);
			}
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Map<Long,ImmunityProofLevel> getImmunoLevels() {
		final Map<Long, ImmunityProofLevel> immunoStatusMap;
		if(immunoStatusEnabled && !lectureBlocks.isEmpty()) {
			final List<ImmunityProof> proofs = immunityProofService.getImmunityProofs(participants);
			final Map<Long,ImmunityProof> identityKeyToProof = proofs.stream()
					.collect(Collectors.toMap(p -> p.getIdentity().getKey(), p -> p, (u, v) -> u));
			immunoStatusMap = participants.stream().collect(Collectors.toMap(Identity::getKey, participant ->
				immunityProofService.getImmunityProofLevel(identityKeyToProof.get(participant.getKey()))
			));
		} else {
			immunoStatusMap = Map.of();
		}
		return immunoStatusMap;
	}
	
	private IdentityLecturesRollCallsRow forgeRow(Identity participant, ImmunityProofLevel immunoStatus) {
		IdentityLecturesRollCallsRow row = new IdentityLecturesRollCallsRow(participant, immunoStatus);

		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setTools(toolsLink);

		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(addNoticeCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
				cleanUp();
			}
		} else if(rollCallsCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
				cmc.deactivate();
				cleanUp();
			}
		} else if(closeLecturesCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doCloseLectures();
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(closeLecturesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(rollCallsCtrl);
		removeAsListenerAndDispose(addNoticeCtrl);
		removeAsListenerAndDispose(profileCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		rollCallsCtrl = null;
		addNoticeCtrl = null;
		profileCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(closeButton == source) {
			doConfirmCloseLectures(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				IdentityLecturesRollCallsRow row = (IdentityLecturesRollCallsRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("rollcall".equals(cmd)) {
					IdentityLecturesRollCallsRow row = tableModel.getObject(se.getIndex());
					doRollCall(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, IdentityLecturesRollCallsRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doConfirmCloseLectures(UserRequest ureq) {
		int numOfLectures = lectureBlocks.size();
		Date date = null;
		if(!lectureBlocks.isEmpty()) {
			date = lectureBlocks.get(0).getStartDate();
		}
		closeLecturesCtrl = new CloseLecturesController(ureq, getWindowControl(), numOfLectures, date);
		listenTo(closeLecturesCtrl);

		String title = translate("close.lecture.blocks");
		cmc = new CloseableModalController(getWindowControl(), "close", closeLecturesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCloseLectures() {
		lectureBlocks = lectureService.saveDefaultRollCalls(lectureBlocks, getIdentity(), true);
		if(!lectureBlocks.isEmpty()) {
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureBlocks(lectureBlocks);
			lectureBlocks = lectureService.getLectureBlocks(searchParams);
		}
	}
	
	private void doRollCall(UserRequest ureq, IdentityLecturesRollCallsRow row) {
		Identity calledIdentity = row.getIdentity();
		List<LectureBlock> calledLectureBlocks = row.getLectureBlocks();
		List<LectureBlockRollCall> rollCalls = row.getRollCalls();
		rollCallsCtrl = new SingleParticipantRollCallsController(ureq, this.getWindowControl(),
				calledIdentity, calledLectureBlocks, rollCalls);
		listenTo(rollCallsCtrl);
		
		String title = translate("multi.rollcall.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), rollCallsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenProfil(UserRequest ureq, IdentityLecturesRollCallsRow row) {
		profileCtrl = new IdentityProfileController(ureq, getWindowControl(), row.getIdentity(), secCallback, false);
		listenTo(profileCtrl);
		
		String title = userManager.getUserDisplayEmail(row.getIdentity(), getLocale());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), profileCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddNotice(UserRequest ureq, IdentityLecturesRollCallsRow row, AbsenceNoticeType type) {
		List<LectureBlock> lectureBlocksForIdentity = row.getLectureBlocks();
		Date currentDate = null;
		if(!lectureBlocksForIdentity.isEmpty()) {
			currentDate = lectureBlocksForIdentity.get(0).getStartDate();
		}

		final EditAbsenceNoticeWrapper noticeWrapper = new EditAbsenceNoticeWrapper(type);
		noticeWrapper.setIdentity(row.getIdentity());
		noticeWrapper.setPredefinedLectureBlocks(lectureBlocksForIdentity);
		noticeWrapper.setCurrentDate(currentDate);
		
		AbsenceNotice3LecturesEntriesStep step = new AbsenceNotice3LecturesEntriesStep(ureq, noticeWrapper, secCallback, true);
		StepRunnerCallback stop = new AbsenceNoticeFinishStepCallback(noticeWrapper, getTranslator());
		StepRunnerCallback cancel = new AbsenceNoticeCancelStepCallback(noticeWrapper);
		
		String title = translate("add.dispensation.title");
		if(type == AbsenceNoticeType.notified) {
			title = translate("add.notice.absence.title");
		} else if(type == AbsenceNoticeType.absence) {
			title = translate("add.absence.title");
		}
		
		removeAsListenerAndDispose(addNoticeCtrl);
		addNoticeCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, cancel, title, "");
		listenTo(addNoticeCtrl);
		getWindowControl().pushAsModalDialog(addNoticeCtrl.getInitialComponent());
	}
	
	private class ToolsController extends BasicController {
		
		private final IdentityLecturesRollCallsRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, IdentityLecturesRollCallsRow row) {
			super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("multi_roll_call_tools");
			addLink("user.profil", "profil", mainVC);
			addLink("add.dispensation", "dispensation", mainVC);
			addLink("add.notice.absence", "notice.absence", mainVC);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd,  VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("profil".equals(cmd)) {
					doOpenProfil(ureq, row);
				} else if("dispensation".equals(cmd)) {
					doAddNotice(ureq, row, AbsenceNoticeType.dispensation);
				} else if("notice.absence".equals(cmd)) {
					doAddNotice(ureq, row, AbsenceNoticeType.notified);
				}
			}
		}
	}
	
	private static class LectureBlockStartDateComparator implements Comparator<LectureBlock> {
		@Override
		public int compare(LectureBlock o1, LectureBlock o2) {
			Date s1 = o1.getStartDate();
			Date s2 = o2.getStartDate();
			if(s1 == null && s2 == null) return 0;
			if(s1 == null) return 1;
			if(s2 == null) return -1;
			return s1.compareTo(s2);
		}
	}
	
	private static class RollCallKey {
		
		private final Long identityKey;
		private final Long lectureBlockKey;
		
		public RollCallKey(Long identityKey, Long lectureBlockKey) {
			this.identityKey = identityKey;
			this.lectureBlockKey = lectureBlockKey;
		}

		public Long getIdentityKey() {
			return identityKey;
		}

		public Long getLectureBlockKey() {
			return lectureBlockKey;
		}
		
		@Override
		public int hashCode() {
			return identityKey.intValue() + lectureBlockKey.intValue();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof RollCallKey) {
				RollCallKey call = (RollCallKey)obj;
				return identityKey.equals(call.getIdentityKey())
						&& lectureBlockKey.equals(call.getLectureBlockKey());
			}
			return false;
		}
	}
}
