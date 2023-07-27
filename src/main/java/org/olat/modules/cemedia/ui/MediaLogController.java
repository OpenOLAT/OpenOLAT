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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.SearchMediaLogParameters;
import org.olat.modules.cemedia.ui.MediaLogTableModel.MediaLogCols;
import org.olat.modules.cemedia.ui.component.MediaVersionComparator;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaLogController extends FormBasicController {
	
	private static final String TAB_ID_LAST_7_DAYS = "Last7Days";
	private static final String TAB_ID_LAST_4_WEEKS = "Last4Weeks";
	private static final String TAB_ID_LAST_12_MONTH = "Last12Month";
	private static final String TAB_ID_ALL = "All";
	private static final String FILTER_ACTIVITY = "activity";
	private static final String FILTER_USER = "user";
	
	private FlexiFiltersTab tabLast7Days;
	private FlexiFiltersTab tabLast4Weeks;
	private FlexiFiltersTab tabLast12Month;
	private FlexiFiltersTab tabAll;
	private MediaLogTableModel model;
	private FlexiTableElement tableEl;
	
	private Media media;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	
	public MediaLogController(UserRequest ureq, WindowControl wControl, Form mainForm, Media media) {
		super(ureq, wControl, LAYOUT_CUSTOM, "media_logs", mainForm);
		setTranslator(Util.createPackageTranslator(ActivityLogController.class, getLocale(), getTranslator()));
		this.media = media;
		initForm(ureq);
	}
	
	public int size() {
		return model == null ? 0 : model.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.comment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.version));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.author));
		
		model = new MediaLogTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		
		initFilterTabs(ureq);
		initFilters();
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);

		tabLast7Days = FlexiFiltersTabFactory.tab(TAB_ID_LAST_7_DAYS, translate("tab.last.7.days"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast7Days);

		tabLast4Weeks = FlexiFiltersTabFactory.tab(TAB_ID_LAST_4_WEEKS, translate("tab.last.4.weeks"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast4Weeks);

		tabLast12Month = FlexiFiltersTabFactory.tab(TAB_ID_LAST_12_MONTH, translate("tab.last.12.month"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabLast12Month);

		tabAll = FlexiFiltersTabFactory.tab(TAB_ID_ALL, translate("tab.all"), TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabLast7Days);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues actionValues = new SelectionValues();
		for(MediaLog.Action action: MediaLog.Action.values()) {
			actionValues.add(SelectionValues.entry(action.name(), translate(action.i18nKey())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.activity"), FILTER_ACTIVITY,
				actionValues, true));
		
		SelectionValues identityValues = new SelectionValues();
		List<Identity> filterIdentities = mediaService.getMediaDoers(media);
		if(!filterIdentities.contains(getIdentity())) {
			filterIdentities.add(getIdentity());
		}
		filterIdentities.stream().forEach(identity -> identityValues.add(
				SelectionValues.entry(identity.getKey().toString(),
						userManager.getUserDisplayName(identity.getKey()))));
		identityValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.user"), FILTER_USER,
				identityValues, true));

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void loadModel() {
		SearchMediaLogParameters params = getSearchParameters();
		List<MediaLog> logs = mediaService.getMediaLogs(media, params);
		List<MediaVersion> versions = mediaService.getVersions(media);
		Collections.sort(versions, new MediaVersionComparator());
		
		List<MediaLogRow> rows = new ArrayList<>(logs.size());
		for(MediaLog mLog:logs) {
			String fullName = mLog.getIdentity() == null
					? null : userManager.getUserDisplayName(mLog.getIdentity());
			String action = translate("log.action." + mLog.getAction().name().toLowerCase());
			String versioName = getVersionName(mLog, versions);
			rows.add(new MediaLogRow(mLog, fullName, versioName, action));
		}
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private String getVersionName(MediaLog mLog, List<MediaVersion> versions) {
		if(versions == null || versions.isEmpty() || versions.size() == 1) {
			return translate("last.version.short");
		}
		
		String versionName = null;
		for(MediaVersion version:versions) {
			Date date = mLog.getCreationDate();
			Date versionDate = version.getCollectionDate() == null ? version.getCreationDate() : version.getCollectionDate();
			if(date.compareTo(versionDate) <= 0) {
				versionName = version.getVersionName();
				break;
			}
		}
		
		if(versionName == null || "0".equals(versionName)) {
			return translate("last.version.short");
		}
		return versionName;
	}
	
	private SearchMediaLogParameters getSearchParameters() {
		SearchMediaLogParameters params = new SearchMediaLogParameters();
		params.setDateRange(getFilterDateRange());
		params.setIdentityKeys(getFilterIdentityKeys());
		params.setActions(getFilterActions());
		return params;
	}
	
	private DateRange getFilterDateRange() {
		if (tableEl.getSelectedFilterTab() != null) {
			if (tableEl.getSelectedFilterTab() == tabLast7Days) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addDays(today, -7), DateUtils.addDays(today, 1));
			} else if (tableEl.getSelectedFilterTab() == tabLast4Weeks) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addDays(today, -28), DateUtils.addDays(today, 1));
			} else if (tableEl.getSelectedFilterTab() == tabLast12Month) {
				Date today = DateUtils.setTime(new Date(), 0, 0, 0);
				return new DateRange(DateUtils.addMonth(today, -12), DateUtils.addDays(today, 1));
			}
		}
		return null;
	}
	
	private List<Long> getFilterIdentityKeys() {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_USER);
		if(filter instanceof FlexiTableMultiSelectionFilter userFilter) {
			List<String> values = userFilter.getValues();
			if (values != null && !values.isEmpty()) {
				return List.copyOf(values.stream().map(Long::valueOf).collect(Collectors.toSet()));
			}
		}
		return List.of();
	}
	
	private List<MediaLog.Action> getFilterActions() {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_ACTIVITY);
		if(filter instanceof FlexiTableMultiSelectionFilter activityFilter) {
			List<String> values = activityFilter.getValues();
			if (values != null && !values.isEmpty()) {
				return values.stream().map(MediaLog.Action::valueOf).toList();
			}
		}
		return List.of();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if (event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
