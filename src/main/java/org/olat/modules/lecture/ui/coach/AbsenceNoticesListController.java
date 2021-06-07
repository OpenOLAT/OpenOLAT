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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.UserListController;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeHelper.AbsenceNoticeCSS;
import org.olat.modules.lecture.ui.coach.AbsenceNoticesListTableModel.NoticeCols;
import org.olat.modules.lecture.ui.component.DailyDateCellRenderer;
import org.olat.modules.lecture.ui.component.StartEndDateCellRenderer;
import org.olat.modules.lecture.ui.event.OpenRepositoryEntryEvent;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.modules.lecture.ui.filter.AbsenceNoticeFilter;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticesListController extends FormBasicController {
	
	private static final String[] unauthorizedKeys = new String[] { "all", "unauthorized" };
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USER_USAGE_IDENTIFIER = UserListController.usageIdentifyer;
	
	private FormLink authorizeButton;
	private FlexiTableElement tableEl;
	private DailyDateCellRenderer dailyDateRenderer;
	private AbsenceNoticesListTableModel tableModel;
	private SingleSelection unauthorizedFilterEl;
	
	private String tableId;
	private int counter = 0;
	private Date currentDate;
	private boolean authorizedEnabled;
	/**
	 * Show per default the start / end date as whole date
	 */
	private final boolean wholeDateDefault;
	private final boolean withUserProperties;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final LecturesSecurityCallback secCallback;
	private AbsenceNoticeSearchParameters lastSearchParams;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private EditNoticeController editNoticeCtrl;
	private IdentityProfileController profileCtrl;
	private ContactTeachersController contactTeachersCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private AbsenceNoticeDetailsCalloutController detailsCtrl;
	private RepositoryEntriesCalloutController entriesCalloutCtrl;
	private ConfirmDeleteAbsenceNoticeController deleteNoticeCtrl;
	private ConfirmAuthorizeAbsenceNoticeController authorizeCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public AbsenceNoticesListController(UserRequest ureq, WindowControl wControl, Date currentDate,
			LecturesSecurityCallback secCallback, boolean withUserProperties,
			String tableId) {
		super(ureq, wControl, "absences_list", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.tableId = tableId;
		this.secCallback = secCallback;
		this.currentDate = currentDate;
		wholeDateDefault = (currentDate == null);
		this.withUserProperties = withUserProperties;
		authorizedEnabled = lectureModule.isAuthorizedAbsenceEnabled();

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public void setCurrentDate(Date date) {
		currentDate = date;
		if(dailyDateRenderer != null) {
			dailyDateRenderer.setCurrentDate(date);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] unauthorizedValues = new String[] { translate("all"), translate("unauthorized.filter") };
		unauthorizedFilterEl = uifactory.addRadiosHorizontal("unauthorized.filter", "unauthorized.filter.label", formLayout,
				unauthorizedKeys, unauthorizedValues);
		unauthorizedFilterEl.addActionListener(FormEvent.ONCHANGE);
		unauthorizedFilterEl.select(unauthorizedKeys[0], true);
		unauthorizedFilterEl.setVisible(authorizedEnabled);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoticeCols.id));

		if(withUserProperties) {
			initUserColumns(columnsModel);
		}
		initColumns(columnsModel);
		
		tableModel = new AbsenceNoticesListTableModel(columnsModel, userManager, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(NoticeCols.date.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCustomizeColumns(true);
		tableEl.setNumOfRowsEnabled(true);
		tableEl.setEmptyTableMessageKey("empty.notices.list");
		tableEl.setAndLoadPersistedPreferences(ureq, "absences-list-v3-" + tableId + "-" + secCallback.viewAs());
		
		if(authorizedEnabled && secCallback.canAuthorizeAbsence()) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			authorizeButton = uifactory.addFormLink("absences.batch.authorize", formLayout, Link.BUTTON);
			tableEl.addBatchButton(authorizeButton);
		}
	}
	
	protected void initUserColumns(FlexiTableColumnModel columnsModel) {
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(USER_USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select-user",
					true, "userProp-" + colIndex));
			colIndex++;
		}
	}
	
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoticeCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoticeCols.lectureBlocks));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoticeCols.numOfLectures));
		DefaultFlexiColumnModel teacherCol = new DefaultFlexiColumnModel(false, NoticeCols.teachers, new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(teacherCol);
		dailyDateRenderer = new DailyDateCellRenderer(currentDate, getLocale(), getTranslator());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoticeCols.date, dailyDateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(wholeDateDefault, NoticeCols.start,
				new StartEndDateCellRenderer(true, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(wholeDateDefault, NoticeCols.end,
				new StartEndDateCellRenderer(false, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NoticeCols.reason));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoticeCols.details));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NoticeCols.type));
		columnsModel.addFlexiColumnModel(new StickyActionColumnModel(NoticeCols.tools));
	}
	
	protected void reloadModel() {
		loadModel(lastSearchParams);
	}

	protected void loadModel(AbsenceNoticeSearchParameters searchParams) {
		lastSearchParams = searchParams;
		
		List<AbsenceNoticeInfos> infosList = lectureService.searchAbsenceNotices(searchParams);
		List<AbsenceNoticeRow> rows = new ArrayList<>(infosList.size());
		for(AbsenceNoticeInfos infos:infosList) {
			AbsenceNotice notice = infos.getAbsenceNotice();
			rows.add(forgeRow(notice));
		}
		
		List<AbsenceNotice> notices = infosList.stream()
				.map(AbsenceNoticeInfos::getAbsenceNotice)
				.collect(Collectors.toList());
		List<LectureBlockWithNotice> blockWithNotices = lectureService.getLectureBlocksWithAbsenceNotices(notices);
		Map<Long, List<LectureBlockWithNotice>> noticeKeyWithBlockMap = new HashMap<>();
		for(LectureBlockWithNotice blockWithNotice:blockWithNotices) {
			Long absenceNoticeKey = blockWithNotice.getAbsenceNotice().getKey();
			List<LectureBlockWithNotice> blockList = noticeKeyWithBlockMap
					.computeIfAbsent(absenceNoticeKey, key -> new ArrayList<>());
			blockList.add(blockWithNotice);
		}
		
		// decorate with teachers
		Map<Long, List<Identity>> blockKeyWithTeachersMap = new HashMap<>();
		if(!blockWithNotices.isEmpty()) {
			LecturesBlockSearchParameters searchTeachersParams = new LecturesBlockSearchParameters();
			List<LectureBlockRef> lectureBlocks = blockWithNotices.stream()
					.map(LectureBlockWithNotice::getLectureBlock).collect(Collectors.toList());
			searchTeachersParams.setLectureBlocks(lectureBlocks);
			List<LectureBlockWithTeachers> teacherBlocks = lectureService.getLectureBlocksWithTeachers(searchTeachersParams);
			Map<Identity,Identity> deduplicatesTeachers = new HashMap<>();
			for(LectureBlockWithTeachers teacherBlock:teacherBlocks) {
				Long lectureBlockKey = teacherBlock.getLectureBlock().getKey();
				List<Identity> blockList = blockKeyWithTeachersMap
						.computeIfAbsent(lectureBlockKey, key -> new ArrayList<>());
				// prevent to load 100X the same identity object
				for(Identity teacher:teacherBlock.getTeachers()) {
					Identity uniqueTeacher = deduplicatesTeachers.computeIfAbsent(teacher, t -> t);
					blockList.add(uniqueTeacher);
				}
			}
		}

		// decorate with courses and lecture blocks
		for(AbsenceNoticeRow row:rows) {
			Long absenceNoticeKey = row.getKey();
			List<LectureBlockWithNotice> blockList = noticeKeyWithBlockMap.get(absenceNoticeKey);
			if(blockList != null) {
				Set<RepositoryEntry> entrySet = new HashSet<>();
				for(LectureBlockWithNotice block:blockList) {
					entrySet.add(block.getEntry());
					LectureBlock lectureBlock = block.getLectureBlock();
					row.addLectureBlock(lectureBlock);
					List<Identity> teachers = blockKeyWithTeachersMap.get(lectureBlock.getKey());
					row.addTeachers(teachers);
				}
				row.addEntries(entrySet);
				forgeEntryLink(row, blockList);
			}
		}
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			final AbsenceNoticeFilter filter = new AbsenceNoticeFilter(searchParams.getSearchString(),
					userPropertyHandlers, getLocale());
			rows = rows.stream()
					.filter(filter)
					.collect(Collectors.toList());
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AbsenceNoticeRow forgeRow(AbsenceNotice notice) {
		AbsenceNoticeRow row = new AbsenceNoticeRow(notice, notice.getIdentity());
		
		// details
		String detailsLinkName = "details-" + counter++;
		FormLink detailsLink = uifactory.addFormLink(detailsLinkName, "details", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		detailsLink.setIconRightCSS("o_icon o_icon_info o_icon-lg");
		detailsLink.setUserObject(row);
		flc.add(detailsLinkName, detailsLink);
		row.setDetailsLink(detailsLink);
		
		// type
		String typeLinkName = "type-" + counter++;
		AbsenceNoticeCSS i18nAndCss = AbsenceNoticeHelper.valueOf(notice);
		FormLink typeLink = uifactory.addFormLink(typeLinkName, "type", i18nAndCss.getI18nKey(), null, flc, Link.LINK);
		typeLink.setIconLeftCSS("o_icon ".concat(i18nAndCss.getCssClass()));
		typeLink.setUserObject(row);
		flc.add(typeLinkName, typeLink);
		row.setTypeLink(typeLink);
		
		// tools
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setToolsLink(toolsLink);

		return row;
	}
	
	private void forgeEntryLink(AbsenceNoticeRow row, List<LectureBlockWithNotice> blockList) {
		Set<RepositoryEntry> entries = blockList.stream()
				.map(LectureBlockWithNotice::getEntry).collect(Collectors.toSet());
		
		String title;
		if(entries.size() == 1) {
			title = entries.iterator().next().getDisplayname();
		} else {
			title = translate("several.entries");
		}
		
		// entries
		String linkName = "entries-" + counter++;
		FormLink entriesLink = uifactory.addFormLink(linkName, "entries", title, null, flc, Link.LINK | Link.NONTRANSLATED);
		entriesLink.setUserObject(row);
		flc.add(linkName, entriesLink);
		row.setEntriesLink(entriesLink);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editNoticeCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(lastSearchParams);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source || entriesCalloutCtrl == source) {
			if(event instanceof OpenRepositoryEntryEvent) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
				doOpenEntryLectures(ureq, ((OpenRepositoryEntryEvent)event).getEntry());
			} else if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(deleteNoticeCtrl == source || authorizeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(lastSearchParams);
			}
			cmc.deactivate();
			cleanUp();
		} else if(contactTeachersCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(contactTeachersCtrl);
		removeAsListenerAndDispose(entriesCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(deleteNoticeCtrl);
		removeAsListenerAndDispose(editNoticeCtrl);
		removeAsListenerAndDispose(authorizeCtrl);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		contactTeachersCtrl = null;
		entriesCalloutCtrl = null;
		toolsCalloutCtrl = null;
		deleteNoticeCtrl = null;
		editNoticeCtrl = null;
		authorizeCtrl = null;
		detailsCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(authorizeButton == source) {
			doAuthorize(ureq);
		} else if(unauthorizedFilterEl == source) {
			doFilterUnauthorized();
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-user".equals(se.getCommand())) {
					doSelectUser(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("details".equals(link.getCmd())) {
				doOpenDetails(ureq, (AbsenceNoticeRow)link.getUserObject(), link);
			} else if("tools".equals(link.getCmd())) {
				doOpenTools(ureq, (AbsenceNoticeRow)link.getUserObject(), link);
			} else if("type".equals(link.getCmd())) {
				doOpenType(ureq, (AbsenceNoticeRow)link.getUserObject(), link);
			} else if("entries".equals(link.getCmd())) {
				doOpenEntries(ureq, (AbsenceNoticeRow)link.getUserObject(), link);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectUser(UserRequest ureq, AbsenceNoticeRow row) {
		fireEvent(ureq, new SelectLectureIdentityEvent(row.getIdentityKey()));
	}
	
	private void doFilterUnauthorized() {
		if(unauthorizedFilterEl.isOneSelected()) {
			Boolean authorized = unauthorizedFilterEl.isSelected(1) ? Boolean.FALSE : null;
			lastSearchParams.setAuthorized(authorized);
		}
		loadModel(lastSearchParams);
	}
	
	private void doAuthorize(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<AbsenceNotice> notices = selectedIndex.stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.map(AbsenceNoticeRow::getAbsenceNotice)
			.collect(Collectors.toList());
		
		if(notices.isEmpty()) {
			showWarning("warning.choose.at.least.one.notice");
		} else {
			authorizeCtrl = new ConfirmAuthorizeAbsenceNoticeController(ureq, getWindowControl(), notices);
			listenTo(authorizeCtrl);
	
			String title = translate("absences.batch.authorize");
			cmc = new CloseableModalController(getWindowControl(), "close", authorizeCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	protected void doSearch(AbsenceNoticeSearchParameters searchParams) {
		loadModel(searchParams);
	}
	
	private void doOpenTools(UserRequest ureq, AbsenceNoticeRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		AbsenceNotice notice = lectureService.getAbsenceNotice(row.getAbsenceNotice());
		if(notice == null) {
			tableEl.reloadData();
			showWarning("warning.absence.notice.not.existing");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenDetails(UserRequest ureq, AbsenceNoticeRow row, FormLink link) {
		if(detailsCtrl != null) return;
		
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		AbsenceNotice notice = lectureService.getAbsenceNotice(row.getAbsenceNotice());
		if(notice == null) {
			tableEl.reloadData();
			showWarning("warning.absence.notice.not.existing");
		} else {
			detailsCtrl = new AbsenceNoticeDetailsCalloutController(ureq, getWindowControl(), row);
			listenTo(detailsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					detailsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenType(UserRequest ureq, AbsenceNoticeRow row, FormLink link) {
		if(secCallback.viewAs() == LectureRoles.participant) {
			doOpenDetails(ureq, row, link);
		} else {
			doEdit(ureq, row);
		}
	}
	
	private void doEdit(UserRequest ureq, AbsenceNoticeRow row) {
		AbsenceNotice notice = lectureService.getAbsenceNotice(row.getAbsenceNotice());

		editNoticeCtrl = new EditNoticeController(ureq, getWindowControl(), notice, secCallback);
		listenTo(editNoticeCtrl);
		
		String title = translate(AbsenceNoticeHelper.getEditKey(row.getAbsenceNotice()));
		cmc = new CloseableModalController(getWindowControl(), "close", editNoticeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenProfile(UserRequest ureq, AbsenceNoticeRow row) {
		if(guardModalController(profileCtrl)) return;
		
		profileCtrl = new IdentityProfileController(ureq, getWindowControl(), row.getAbsentIdentity(), secCallback, false);
		listenTo(profileCtrl);

		String title = translate("profile");
		cmc = new CloseableModalController(getWindowControl(), "close", profileCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenEntries(UserRequest ureq, AbsenceNoticeRow row, FormLink link) {
		if(row == null || row.getEntriesList().isEmpty()) {
			// not possible
		} else if(row.getEntriesList().size() == 1) {
			doOpenEntryLectures(ureq, row.getEntriesList().get(0));
		} else {
			entriesCalloutCtrl = new RepositoryEntriesCalloutController(ureq, getWindowControl(), row.getEntriesList());
			listenTo(entriesCalloutCtrl);
			
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					entriesCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenEntry(UserRequest ureq, RepositoryEntry entry) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenEntryLectures(UserRequest ureq, RepositoryEntry entry) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		if(secCallback.viewAs() == LectureRoles.teacher || secCallback.viewAs() == LectureRoles.mastercoach) {
			businessPath += "[Lectures:0]";
		} else if(secCallback.viewAs() == LectureRoles.lecturemanager) {
			businessPath += "[LecturesAdmin:0][Participants:0]";
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doContactTeachers(UserRequest ureq, AbsenceNoticeRow row) {
		if(guardModalController(contactTeachersCtrl)) return;

		List<Identity> teachers = row.getTeachers();
		if(teachers.isEmpty()) {
			showWarning("warning.teachers.at.least.one.contact");
		} else {
			contactTeachersCtrl = new ContactTeachersController(ureq, getWindowControl(), teachers);
			listenTo(contactTeachersCtrl);
			
			String title = translate("contact.teachers");
			cmc = new CloseableModalController(getWindowControl(), "close", contactTeachersCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, AbsenceNoticeRow row) {
		AbsenceNotice notice = lectureService.getAbsenceNotice(row);
		deleteNoticeCtrl = new ConfirmDeleteAbsenceNoticeController(ureq, getWindowControl(), notice);
		listenTo(deleteNoticeCtrl);
		
		String title = translate("delete");
		cmc = new CloseableModalController(getWindowControl(), "close", deleteNoticeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private AbsenceNoticeRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AbsenceNoticeRow row) {
			super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools_notices");
			// edit absence, notice of absence, dispensation
			String editI18nKey = AbsenceNoticeHelper.getEditKey(row.getAbsenceNotice());
			if(secCallback.canEditAbsenceNotices()) {
				addLink(editI18nKey, "edit", "o_icon o_icon_edit", mainVC);
			}
			
			// open profile
			addLink("profile", "profile", "o_icon o_icon_user", mainVC);
			// contact teacher
			addLink("contact.teachers", "contact", "o_icon o_icon_mail", mainVC);
			
			// open courses
			List<RepositoryEntry> entries = row.getEntriesList();
			List<String> entryLinkIds = new ArrayList<>(entries.size());
			for(RepositoryEntry entry:entries) {
				String linkId = "entry_" + (counter++);
				String name = translate("open.specific.course", new String[] { entry.getDisplayname() });
				Link link = LinkFactory.createLink(linkId, linkId, "entry", name, getTranslator(), mainVC, this, Link.NONTRANSLATED | Link.LINK);
				link.setIconLeftCSS("o_icon o_CourseModule_icon");
				link.setUserObject(entry);
				mainVC.put(linkId, link);
				entryLinkIds.add(linkId);
			}
			mainVC.contextPut("entryLinkIds", entryLinkIds);
			
			if(secCallback.canDeleteAbsenceNotices()) {
				addLink("delete", "delete", "o_icon o_icon_delete_item", mainVC);
			}
	
			putInitialPanel(mainVC);
		}

		@Override
		protected void doDispose() {
			//
		}
		
		private void addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, name, cmd, mainVC, this);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				if("edit".equals(link.getCommand())) {
					doEdit(ureq, row);
				} else if("profile".equals(link.getCommand())) {
					doOpenProfile(ureq, row);
				} else if("contact".equals(link.getCommand())) {
					doContactTeachers(ureq, row);
				} else if("entry".equals(link.getCommand())) {
					doOpenEntry(ureq, (RepositoryEntry)link.getUserObject());
				} else if("delete".equals(link.getCommand())) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}
}
