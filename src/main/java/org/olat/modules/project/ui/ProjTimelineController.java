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
package org.olat.modules.project.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDateRange;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.manager.ProjectXStream;
import org.olat.modules.project.ui.ProjTimelineDataModel.TimelineCols;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.user.UserManager;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final int NUM_LOAD_MORE = 20;
	private static final String TAB_ID_MY = "My";
	private static final String TAB_ID_ALL = "All";
	private static final String FILTER_TYPE = "type";
	private static final String FILTER_USER = "user";
	private static final String CMD_RANGE = "range";
	private static final String CMD_MORE = "more";
	private static final String CMD_ARTEFACT = "artefact";
	
	private FlexiFiltersTab tabMy;
	private FlexiFiltersTab tabAll;
	private FormLink next7DaysLink;
	private FormLink todayLink;
	private FormLink last7DaysLink;
	private FormLink last4WeeksLink;
	private FormLink last12MonthLink;
	private FormLink more12MonthLink;
	private FlexiTableElement tableEl;
	private ProjTimelineDataModel dataModel;

	private final ProjProjectRef project;
	private final List<Identity> members;
	private final MapperKey avatarMapperKey;
	private int counter;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserManager userManager;

	public ProjTimelineController(UserRequest ureq, WindowControl wControl, ProjProjectRef project, List<Identity> members, MapperKey avatarMapperKey) {
		super(ureq, wControl, "timeline");
		this.project = project;
		this.members = members;
		this.avatarMapperKey = avatarMapperKey;
		initForm(ureq);
		loadModel(ureq, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TimelineCols.message));
		
		dataModel = new ProjTimelineDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 2000, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setSearchEnabled(true);

		tableEl.setCssDelegate(new ProjTimelineListCssDelegate());
		// If you ever want to enable the classic renderer, ensure that the range row links are not displayed.
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("timeline_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initFilterTabs(ureq);
		initFilters();
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabMy = FlexiFiltersTabFactory.tab(
				TAB_ID_MY,
				translate("timeline.tab.my"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabMy);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(ActionTarget.project.name(), translate("timeline.filter.type.project")));
		typeValues.add(SelectionValues.entry(ActionTarget.file.name(), translate("timeline.filter.type.file")));
		typeValues.add(SelectionValues.entry(ActionTarget.note.name(), translate("timeline.filter.type.note")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("timeline.filter.type"), FILTER_TYPE, typeValues, true));
		
		SelectionValues userValues = new SelectionValues();
		members.stream()
				.filter(member -> {
					if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabMy) {
						return !member.getKey().equals(getIdentity().getKey());
					}
					return true;
				})
				.forEach(member -> userValues.add(SelectionValues.entry(
						member.getKey().toString(),
						userManager.getUserDisplayName(member))));
		userValues.sort(SelectionValues.VALUE_ASC);
		if (!userValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("timeline.filter.user"), FILTER_USER, userValues, true));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	public void reload(UserRequest ureq) {
		loadModel(ureq, false);
	}

	private void loadModel(UserRequest ureq, boolean resetRanges) {
		if (resetRanges) {
			createRangeLinks();
		}
		
		// Load all open todos
		// Load all appointments
		// Future range: add todos and appointments
		// Today: add todos and appointments, load today activities if open
		// Past: add todos and appointments, add preloaded activities
		loadActivites(ureq, todayLink);
		addRows();
	}
	
	private void addRows() {
		List<ProjTimelineRow> rows = new ArrayList<>();
		
		addRangeLinks(rows, next7DaysLink);
		addRangeLinks(rows, todayLink);
		addRangeLinks(rows, last7DaysLink);
		addRangeLinks(rows, last4WeeksLink);
		addRangeLinks(rows, last12MonthLink);
		addRangeLinks(rows, more12MonthLink);
		
		addRows(rows, next7DaysLink);
		addRows(rows, todayLink);
		addRows(rows, last7DaysLink);
		addRows(rows, last4WeeksLink);
		addRows(rows, last12MonthLink);
		addRows(rows, more12MonthLink);
		
		applyFilters(rows);
		rows.sort((r1, r2) -> r2.getDate().compareTo(r1.getDate()));
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void addRows(List<ProjTimelineRow> rows, FormLink rangeLink) {
		RangeUserObject rangeUserObject = (RangeUserObject)rangeLink.getUserObject();
		if (rangeUserObject.isShow()) {
			if (rangeUserObject.getRows() != null && !rangeUserObject.getRows().isEmpty()) {
				rows.addAll(rangeUserObject.getRows());
			} else {
				ProjTimelineRow row = new ProjTimelineRow();
				row.setRangeEmpty(translate("timeline.range.empty"));
				row.setDate(DateUtils.addSeconds(rangeUserObject.getDateRange().getTo(), -1));
				rows.add(row);
			}
		}
	}
	
	private void loadActivites(UserRequest ureq, FormLink rangeLink) {
		boolean today = rangeLink == todayLink;
		RangeUserObject rangeUserObject = (RangeUserObject)rangeLink.getUserObject();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setActions(ProjActivity.TIMELINE_ACTIONS);
		searchParams.setCreatedDateRanges(List.of(rangeUserObject.getDateRange()));
		applyFilters(searchParams);
		
		int maxResults = today? -1: NUM_LOAD_MORE;
		List<ProjActivity> activities = projectService.getActivities(searchParams, rangeUserObject.getOffset(), maxResults);
		Set<ProjArtefact> artefacts = activities.stream()
				.map(ProjActivity::getArtefact)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		ProjArtefactSearchParams artefactSearchParams = new ProjArtefactSearchParams();
		artefactSearchParams.setProject(project);
		artefactSearchParams.setArtefacts(artefacts);
		ProjArtefactItems artefactItems = projectService.getArtefactItems(artefactSearchParams);
		
		Map<Long, Set<Long>> artefactKeyToIdentityKeys = projectService.getArtefactKeyToIdentityKeys(artefacts);
		
		List<ProjTimelineRow> rows = new ArrayList<>();
		for (ProjActivity activity : activities) {
			addActivityRows(ureq, rows, activity, artefactItems, artefactKeyToIdentityKeys);
		}
		if (today) {
			rangeUserObject.setRows(rows);
		} else {
			rangeUserObject.getRows().addAll(rows);
			rangeUserObject.setOffset(rangeUserObject.getOffset() + activities.size());
			if (activities.size() < NUM_LOAD_MORE) {
				rangeUserObject.setMoreAvailable(false);
			}
		}
	}

	private void applyFilters(ProjActivitySearchParams searchParams) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_TYPE.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<ActionTarget> targets = values.stream().filter(ActionTarget::isValid).map(ActionTarget::valueOf).collect(Collectors.toList());
					searchParams.setTargets(targets);
				} else {
					searchParams.setTargets(null);
				}
			}
		}
	}
	
	private void applyFilters(List<ProjTimelineRow> rows) {
		String searchString = tableEl.getQuickSearchString().toLowerCase();
		rows.removeIf(row -> row.getMessage() != null && row.getMessage().toLowerCase().indexOf(searchString) < 0);
		
		if (tableEl.getSelectedFilterTab() != null && tableEl.getSelectedFilterTab() == tabMy) {
			Long identityKey = getIdentity().getKey();
			rows.removeIf(row -> row.getIdentityKeys() != null && !row.getIdentityKeys().contains(identityKey));
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_USER.equals(filter.getFilter())) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedIdentityKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					rows.removeIf(row -> row.getIdentityKeys() != null && !row.getIdentityKeys().stream().anyMatch(key -> selectedIdentityKeys.contains(key)));
				}
			}
		}
	}

	private void addActivityRows(UserRequest ureq, List<ProjTimelineRow> rows, ProjActivity activity,
			ProjArtefactItems artefactItems, Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		switch (activity.getActionTarget()) {
		case project: addActivityProjectRows(ureq, rows, activity);
		case file: addActivityFileRows(rows, activity, artefactItems, artefactKeyToIdentityKeys);
		case note: addActivityNoteRows(rows, activity, artefactItems, artefactKeyToIdentityKeys);
		default: //
		}
	}

	private void addActivityProjectRows(UserRequest ureq, List<ProjTimelineRow> rows, ProjActivity activity) {
		ProjTimelineRow row = new ProjTimelineRow();
		
		switch (activity.getAction()) {
		case projectCreate: row.setMessage(translate("activity.project.create.timeline")); break;
		case projectContentContent: row.setMessage(translate("activity.project.content.update.timeline")); break;
		case projectStatusActive: row.setMessage(translate("activity.project.status.active.timeline")); break;
		case projectStatusDone: row.setMessage(translate("activity.project.status.done.timeline")); break;
		case projectStatusDelete: row.setMessage(translate("activity.project.status.deleted.timeline")); break;
		case projectMemberAdd: {
			Identity member = activity.getMember();
			if (member != null) {
				row.setMessage(translate("activity.project.member.add.timeline", userManager.getUserDisplayName(member.getKey())));
				addAvatarIcon(ureq, row, member);
			}
			break;
		}
		case projectMemberRemove: {
			Identity member = activity.getMember();
			if (member != null) {
				row.setMessage(translate("activity.project.member.remove.timeline", userManager.getUserDisplayName(member.getKey())));
				addAvatarIcon(ureq, row, member);
			}
			break;
		}
		default: //
		}
		
		if (row.getMessage() == null) {
			return;
		}
		
		Set<Long> identityKeys = new HashSet<>(2);
		identityKeys.add(activity.getDoer().getKey());
		if (activity.getMember() != null) {
			identityKeys.add(activity.getMember().getKey());
		}
		row.setIdentityKeys(identityKeys);
		
		row.setDate(activity.getCreationDate());
		row.setDoerDisplyName(userManager.getUserDisplayName(activity.getDoer().getKey()));
		addStaticMessageItem(row);
		
		if (row.getIconItem() == null) {
			addActionIconItem(row, activity);
		}
		
		rows.add(row);
	}
	
	private void addActivityFileRows(List<ProjTimelineRow> rows, ProjActivity activity, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		if (activity.getArtefact() == null) {
			return;
		}
		ProjFile file = artefactItems.getFile(activity.getArtefact());
		if (file == null) {
			return;
		}
		
		String displayName = ProjectUIFactory.getDisplayName(file);
		switch (activity.getAction()) {
		case fileCreate: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.create.timeline", displayName)); break;
		case fileUpload: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.upload.timeline", displayName)); break;
		case fileEdit: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.edit.timeline", displayName)); break;
		case fileStatusDelete: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.delete.timeline", displayName)); break;
		case fileContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjFile before = ProjectXStream.fromXml(activity.getBefore(), ProjFile.class);
				ProjFile after = ProjectXStream.fromXml(activity.getAfter(), ProjFile.class);
				if (!Objects.equals(before.getVfsMetadata().getTitle(), after.getVfsMetadata().getTitle())) {
					addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.update.title.timeline", displayName));
				}
				if (!Objects.equals(before.getVfsMetadata().getFilename(), after.getVfsMetadata().getFilename())) {
					addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.file.update.filename.timeline", displayName));
				}
			}
			 break;
		}
		default: //
		}
	}
	
	private void addActivityNoteRows(List<ProjTimelineRow> rows, ProjActivity activity, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		if (activity.getArtefact() == null) {
			return;
		}
		ProjNote note = artefactItems.getNote(activity.getArtefact());
		if (note == null) {
			return;
		}
		
		String displayName = ProjectUIFactory.getDisplayName(getTranslator(), note);
		switch (activity.getAction()) {
		case noteCreate: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.note.create.timeline", displayName)); break;
		case noteContentUpdate: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.note.update.content.timeline", displayName)); break;
		case noteStatusDelete: addArtefactRow(rows, activity, artefactKeyToIdentityKeys, translate("activity.note.delete.timeline", displayName)); break;
		default: //
		}
	}

	private void addArtefactRow(List<ProjTimelineRow> rows, ProjActivity activity, Map<Long, Set<Long>> artefactKeyToIdentityKeys, String message) {
		ProjTimelineRow row = new ProjTimelineRow();
		
		Set<Long> identityKeys = new HashSet<>(2);
		identityKeys.add(activity.getDoer().getKey());
		identityKeys.addAll(artefactKeyToIdentityKeys.getOrDefault(activity.getArtefact().getKey(), Set.of()));
		row.setIdentityKeys(identityKeys);
		
		row.setMessage(message);
		row.setDate(activity.getCreationDate());
		row.setDoerDisplyName(userManager.getUserDisplayName(activity.getDoer().getKey()));
	
		addArtefactMesssageItem(row, activity.getArtefact());
		addActionIconItem(row, activity);
		
		rows.add(row);
	}

	private void addArtefactMesssageItem(ProjTimelineRow row, ProjArtefact artefact) {
		if (artefact != null && ProjectStatus.deleted != artefact.getStatus()) {
			FormLink link = uifactory.addFormLink("art_" + counter++, CMD_ARTEFACT, row.getMessage(), null, flc, Link.LINK + Link.NONTRANSLATED);
			String url = ProjectBCFactory.getArtefactUrl(project, artefact.getType(), artefact.getKey());
			link.setUrl(url);
			link.setUserObject(artefact);
			row.setMessageItem(link);
		} else {
			addStaticMessageItem(row);
		}
	}

	private void addStaticMessageItem(ProjTimelineRow row) {
		StaticTextElement messageItem = uifactory.addStaticTextElement("o_tl_" + counter++, row.getMessage(), flc);
		messageItem.setDomWrapperElement(DomWrapperElement.span);
		row.setMessageItem(messageItem);
	}

	private void addActionIconItem(ProjTimelineRow row, ProjActivity activity) {
		String icon = "<i class=\"o_icon o_icon-lg " + ProjectUIFactory.getActionIconCss(activity.getAction()) +"\"> </i>";
		StaticTextElement iconItem = uifactory.addStaticTextElement("o_tl_" + counter++, icon, flc);
		iconItem.setDomWrapperElement(DomWrapperElement.span);
		row.setIconItem(iconItem);
	}

	private void addAvatarIcon(UserRequest ureq, ProjTimelineRow row, Identity member) {
		UsersPortraitsComponent portraitComp = UsersPortraitsFactory.create(ureq, "portrair_" + counter++, flc.getFormItemComponent(), null, avatarMapperKey);
		portraitComp.setUsers(UsersPortraitsFactory.createPortraitUsers(List.of(member)));
		portraitComp.setSize(PortraitSize.small);
		row.setIconItem(new ComponentWrapperElement(portraitComp));
	}

	private void createRangeLinks() {
		Date today = DateUtils.setTime(new Date(), 0, 0, 0);
		
		next7DaysLink = createRangeLink(DateUtils.addDays(today, 8), DateUtils.addDays(today, 1), "timeline.range.next.7.days", false, false);
		todayLink = createRangeLink(DateUtils.addDays(today, 1), today, "timeline.range.today", true, false);
		last7DaysLink = createRangeLink(today, DateUtils.addDays(today, -7), "timeline.range.last.7.days", false, true);
		last4WeeksLink = createRangeLink(DateUtils.addDays(today, -7), DateUtils.addDays(today, -28), "timeline.range.last.4.weeks", false, true);
		last12MonthLink = createRangeLink(DateUtils.addDays(today, -28), DateUtils.addMonth(today, -12), "timeline.range.last.12.month", false, true);
		more12MonthLink = createRangeLink(DateUtils.addMonth(today, -12), DateUtils.toDate(LocalDate.of(2023, 1, 1)), "timeline.range.more.12.month", false, true);
	}

	private FormLink createRangeLink(Date to, Date from, String i18nLink, boolean show, boolean createMoreLink) {
		RangeUserObject rangeUserObject = new RangeUserObject(from, to);
		rangeUserObject.setShow(show);
		
		FormLink rangeLink = uifactory.addFormLink("range_" + counter++, CMD_RANGE, i18nLink, null, flc, Link.LINK);
		updateRangeIcon(rangeLink, rangeUserObject);
		rangeLink.setUserObject(rangeUserObject);
		
		if (createMoreLink) {
			FormLink moreLink = uifactory.addFormLink("more_" + counter++, CMD_MORE, "timeline.show.more", null, flc, Link.LINK);
			moreLink.setUserObject(rangeUserObject);
			rangeUserObject.setMoreLink(moreLink);
		}
		
		return rangeLink;
	}
	
	private void updateRangeIcon(FormLink link, RangeUserObject rangeUserObject) {
		String iconCss = rangeUserObject.isShow()
				? "o_icon o_icon-lg o_icon_details_collaps"
				: "o_icon o_icon-lg o_icon_details_expand";
		link.setIconLeftCSS(iconCss);
	}
	
	private void addRangeLinks(List<ProjTimelineRow> rows, FormLink rangeLink) {
		RangeUserObject rangeUserObject = (RangeUserObject)rangeLink.getUserObject();
		
		ProjTimelineRow rangeRow = new ProjTimelineRow();
		rangeRow.setRangeLink(rangeLink);
		rangeRow.setDate(rangeUserObject.getDateRange().getTo());
		rows.add(rangeRow);
		
		if (rangeUserObject.isShow() && rangeUserObject.isMoreAvailable() && rangeUserObject.getMoreLink() != null) {
			ProjTimelineRow moreRow = new ProjTimelineRow();
			moreRow.setMoreLink(rangeUserObject.getMoreLink());
			moreRow.setDate(rangeUserObject.getDateRange().getFrom());
			rows.add(moreRow);
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof ProjTimelineRow) {
			ProjTimelineRow timelineRow = (ProjTimelineRow)rowObject;
			if (timelineRow.getIconItem() != null) {
				cmps.add(timelineRow.getIconItem().getComponent());
			}
			if (timelineRow.getMessageItem() != null) {
				cmps.add(timelineRow.getMessageItem().getComponent());
			}
			if (timelineRow.getRangeLink() != null) {
				cmps.add(timelineRow.getRangeLink().getComponent());
			}
			if (timelineRow.getMoreLink() != null) {
				cmps.add(timelineRow.getMoreLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				if (FlexiTableReduceEvent.FILTER.equals(event.getCommand())) {
					// Filter selected
					loadModel(ureq, true);
				} else {
					// Quick search
					addRows();
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				initFilters();
				loadModel(ureq, true);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_RANGE.equals(link.getCmd()) && link.getUserObject() instanceof RangeUserObject) {
				doToggleRange(ureq, link);
			} else if (CMD_MORE.equals(link.getCmd()) && link.getUserObject() instanceof RangeUserObject) {
				loadActivites(ureq, link);
				addRows();
			} else if (CMD_ARTEFACT.equals(link.getCmd()) && link.getUserObject() instanceof ProjArtefact) {
				fireEvent(ureq, new OpenArtefactEvent((ProjArtefact)link.getUserObject()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doToggleRange(UserRequest ureq, FormLink link) {
		RangeUserObject rangeUserObject = (RangeUserObject)link.getUserObject();
		rangeUserObject.setShow(!rangeUserObject.isShow());
		updateRangeIcon(link, rangeUserObject);
		
		if (rangeUserObject.isShow()) {
			loadActivites(ureq, link);
		} else {
			rangeUserObject.getRows().clear();
			rangeUserObject.setOffset(0);
			rangeUserObject.setMoreAvailable(true);
		}
		
		addRows();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private final class ProjTimelineListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_proj_timeline_list";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			ProjTimelineRow row = dataModel.getObject(pos);
			if (row != null && row.getRangeLink() != null) {
				return "o_proj_timeline_row o_proj_timeline_range_row";
			}
			if (row != null && row.getMoreLink() != null) {
				return "o_proj_timeline_row o_proj_timeline_more_row";
			}
			return "o_proj_timeline_row";
		}
	}
	
	private final static class RangeUserObject {
		
		private final ProjDateRange dateRange;
		private boolean show;
		private List<ProjTimelineRow> rows = new ArrayList<>();
		private int offset = 0;
		private boolean moreAvailable = true;
		private FormLink moreLink;
		
		public RangeUserObject(Date from, Date to) {
			this.dateRange = new ProjDateRange(from, to);
		}
		
		public ProjDateRange getDateRange() {
			return dateRange;
		}

		public boolean isShow() {
			return show;
		}

		public void setShow(boolean show) {
			this.show = show;
		}

		public List<ProjTimelineRow> getRows() {
			return rows;
		}

		public void setRows(List<ProjTimelineRow> rows) {
			this.rows = rows;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public boolean isMoreAvailable() {
			return moreAvailable;
		}

		public void setMoreAvailable(boolean moreAvailable) {
			this.moreAvailable = moreAvailable;
		}

		public FormLink getMoreLink() {
			return moreLink;
		}

		public void setMoreLink(FormLink moreLink) {
			this.moreLink = moreLink;
		}
		
	}

}
