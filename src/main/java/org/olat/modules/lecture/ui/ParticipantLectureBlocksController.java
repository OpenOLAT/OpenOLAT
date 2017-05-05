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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.ParticipantLectureBlocksDataModel.ParticipantCols;
import org.olat.modules.lecture.ui.component.AbsenceCellRenderer;
import org.olat.modules.lecture.ui.component.PresenceCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Appeal button: after 5 day (coach can change the block) start, end after 15 day (appeal period)
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLectureBlocksController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private ParticipantLectureBlocksDataModel tableModel;
	
	private final RepositoryEntry entry;
	private final boolean appealEnabled;
	private final AppealCallback appealCallback;
	
	private CloseableModalController cmc;
	private ContactFormController appealCtrl;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public ParticipantLectureBlocksController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "participant_blocks");
		this.entry = entry;
		appealEnabled = lectureModule.isAbsenceAppealEnabled();
		
		int appealOffset = lectureModule.getRollCallAutoClosePeriod();//TODO absence or is it reminder period
		int appealPeriod = lectureModule.getAbsenceAppealPeriod();
		appealCallback = new AppealCallback(appealEnabled, appealOffset, appealPeriod);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", entry.getDisplayname());
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.coach));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.presence, new PresenceCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.absence, new AbsenceCellRenderer()));
		if(appealEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("appeal", ParticipantCols.appeal.ordinal(), "appeal",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("appeal"), "appeal"), null)));
		}

		tableModel = new ParticipantLectureBlocksDataModel(columnsModel, appealCallback, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ParticipantCols.date.name(), true));
		tableEl.setSortSettings(options);
		//TODO absence tableEl.setAndLoadPersistedPreferences(ureq, "participant-roll-call-appeal");
	}
	
	private void loadModel() {
		List<LectureBlockAndRollCall> rollCalls = lectureService.getParticipantLectureBlocks(entry, getIdentity());
		tableModel.setObjects(rollCalls);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LectureBlockAndRollCall row = tableModel.getObject(se.getIndex());
				if("appeal".equals(cmd)) {
					doAppeal(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(appealCtrl == source) {
			if(event == Event.DONE_EVENT) {
				LectureBlockAndRollCall row = (LectureBlockAndRollCall)appealCtrl.getUserObject();
				logAudit("Appeal send for lecture block: " + row.getLectureBlockTitle() + " (" + row.getLectureBlockRef().getKey() + ")", null);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appealCtrl);
		removeAsListenerAndDispose(cmc);
		appealCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAppeal(UserRequest ureq, LectureBlockAndRollCall row) {
		if(appealCtrl != null) return;
		
		LectureBlock block = lectureService.getLectureBlock(row.getLectureBlockRef());
		List<Identity> teachers = lectureService.getTeachers(block);
		List<Identity> onwers = repositoryService.getMembers(entry, GroupRoles.owner.name());
		
		ContactList contactList = new ContactList(translate("appeal.contact.list"));
		contactList.addAllIdentites(teachers);
		contactList.addAllIdentites(onwers);
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		cmsg.addEmailTo(contactList);
		cmsg.setSubject(translate("appeal.subject", new String[]{ row.getLectureBlockTitle() }));
		appealCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		appealCtrl.setUserObject(row);
		appealCtrl.setContactFormTitle(translate("new.appeal.title"));
		listenTo(appealCtrl);
		
		String title = translate("appeal.title", new String[]{ row.getLectureBlockTitle() });
		cmc = new CloseableModalController(getWindowControl(), "close", appealCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	public class AppealCallback {
		
		private final boolean enabled;
		private final int appealOffset;
		private final int appealPeriod;
		private final Date now;
		
		public AppealCallback(boolean enabled, int appealOffset, int appealPeriod) {
			this.enabled = enabled;
			this.appealOffset = appealOffset;
			this.appealPeriod = appealPeriod;
			now = new Date();
		}
		
		public boolean appealAllowed(LectureBlockAndRollCall row) {
			if(enabled) {
				int lectures = row.getEffectiveLecturesNumber();
				if(lectures <= 0) {
					lectures = row.getPlannedLecturesNumber();
				}
				int attended = row.getLecturesAttendedNumber();
				if(attended < lectures) {
					Date date = row.getDate();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal = CalendarUtils.getEndOfDay(cal);
					cal.add(Calendar.DATE, appealOffset);
					Date beginAppeal = cal.getTime();
					cal.add(Calendar.DATE, appealPeriod);
					Date endAppeal = cal.getTime();
					return now.compareTo(beginAppeal) >= 0 && now.compareTo(endAppeal) <= 0;
				}
			}
			return false;
		}
	}
}
