/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.CourseNodeWithDefaults;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.ui.CourseNodesDefaultsDataModel.CourseNodesDefaultsCols;

/**
 * Initial date: Nov 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseNodesDefaultsAdminController extends FormBasicController {

	private final List<CourseNodeDefaultConfigRow> cnDefaultConfRows = new ArrayList<>();

	private FlexiTableElement tableEl;
	private CourseNodesDefaultsDataModel dataModel;
	private CourseNodeWithDefaults selectedCourseNode;

	private Controller defaultsCtrl;

	public CourseNodesDefaultsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "cn_admin");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.functionalGroup));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.courseElement));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.courseNodeManual));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.enabledToggle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.editConfig));

		dataModel = new CourseNodesDefaultsDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), formLayout);

		// Sort by editable courseElements first
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(CourseNodesDefaultsCols.courseElement.sortKey(), true));
		tableEl.setSortSettings(options);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "def-admin-course-node");
		loadModel();
		initFilters();
	}

	private void loadModel() {
		cnDefaultConfRows.clear();

		List<CourseNodeConfiguration> allCourseNodeConfigs = CourseNodeFactory.getInstance().getAllCourseNodeConfigs();

		for (CourseNodeConfiguration courseNodeConfig : allCourseNodeConfigs) {
			// skip deprecated courseNodes
			if (courseNodeConfig.isDeprecated()) {
				continue;
			}
			CourseNodeDefaultConfigRow row;
			// Title of courseNode
			String courseElement = courseNodeConfig.getLinkText(getLocale());
			// Enabled status toggle
			FormToggle enabledToggle = uifactory.addToggleButton("enabled", null, translate("on"), translate("off"), null);
			if (courseNodeConfig.isEnabled()) {
				enabledToggle.toggleOn();
			} else {
				enabledToggle.toggleOff();
			}
			// for now, it is disabled per default
			enabledToggle.setEnabled(false);

			Translator fnGroupTranslator = Util.createPackageTranslator(EditorMainController.class, getLocale());
			String functionalGroup = fnGroupTranslator.translate(courseNodeConfig.getGroup());
			if (courseNodeConfig.getInstance() instanceof CourseNodeWithDefaults cnConfig) {
				if (cnConfig instanceof GTACourseNode gtaCourseNode
						&& gtaCourseNode.getType().equalsIgnoreCase(GTACourseNode.TYPE_GROUP)) {
					// skip GTA, because GTA and ITA share same configuration; GTA Config is a subset of ITA
					continue;
				}
				// Info/Help
				String cnConfigManualUrl = null;
				if (CoreSpringFactory.getImpl(HelpModule.class).isManualEnabled()) {
					cnConfigManualUrl = CoreSpringFactory.getImpl(HelpModule.class)
							.getManualProvider().getURL(getLocale(), cnConfig.getCourseNodeConfigManualUrl());
				}

				ExternalLinkItem externalManualLinkItem = null;
				// if no URL is present, but manual is enabled, then starting page of OpenOlat Manual will be selected
				// if manual is disabled the space/row for info will be empty
				if (cnConfigManualUrl != null) {
					externalManualLinkItem = uifactory.addExternalLink("config.manual", cnConfigManualUrl, "_blank", null);
					externalManualLinkItem.setCssClass("o_icon o_icon-lg o_icon_help");
				}

				// edit defaults/settings link
				FormLink editDefaultsLink = uifactory.addFormLink("edit.defaults_" + CodeHelper.getRAMUniqueID(), "editConfig", "course.node.defaults.edit", null, null, Link.LINK);
				editDefaultsLink.setUserObject(cnConfig);
				row = new CourseNodeDefaultConfigRow(functionalGroup, courseElement, enabledToggle, externalManualLinkItem, editDefaultsLink);
			} else {
				row = new CourseNodeDefaultConfigRow(functionalGroup, courseElement, enabledToggle, null, null);
			}

			cnDefaultConfRows.add(row);
		}

		String funcGroupFilteredValue = "";
		FlexiTableFilter funcGroupFilter = tableEl.getFilters().stream().filter(f -> f.getFilter().equals("funcGroup")).findFirst().orElse(null);
		if (funcGroupFilter != null) {
			funcGroupFilteredValue = funcGroupFilter.getValue();
		}
		loadFilteredModel(funcGroupFilteredValue, tableEl.getQuickSearchString());

		dataModel.setObjects(cnDefaultConfRows);
		tableEl.reset(true, true, true);
	}

	private void loadFilteredModel(String funcGroupFilteredValue, String searchString) {
		List<CourseNodeDefaultConfigRow> rowsToRemove = new ArrayList<>();
		for (CourseNodeDefaultConfigRow row : cnDefaultConfRows) {
			// remove by filters first
			if (StringHelper.containsNonWhitespace(funcGroupFilteredValue)
					&& !funcGroupFilteredValue.contains(row.functionalGroup())) {
				rowsToRemove.add(row);
			}
			// remove by searchString 2nd
			if (StringHelper.containsNonWhitespace(searchString) &&
					(!row.functionalGroup().toLowerCase().contains(searchString.toLowerCase())
							&& !row.courseElement().toLowerCase().contains(searchString.toLowerCase()))) {
				rowsToRemove.add(row);
			}
		}
		cnDefaultConfRows.removeAll(rowsToRemove);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		List<String> distinctFunctionalGroups = dataModel.getObjects()
				.stream()
				.map(CourseNodeDefaultConfigRow::functionalGroup)
				.distinct()
				.toList();

		SelectionValues functionalGroupsKV = new SelectionValues();

		distinctFunctionalGroups.forEach(t -> functionalGroupsKV.add(SelectionValues.entry(t, t)));

		filters.add(new FlexiTableMultiSelectionFilter(translate("course.node.defaults.header.fn.group"),
				"funcGroup", functionalGroupsKV, true));

		tableEl.setFilters(true, filters, false, false);
		tableEl.expandFilters(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			selectedCourseNode = (CourseNodeWithDefaults) link.getUserObject();
			if ("editConfig".equalsIgnoreCase(link.getCmd()) && selectedCourseNode != null) {
				defaultsCtrl = selectedCourseNode.createDefaultsController(ureq, getWindowControl());
				doOpenEditDefaults(defaultsCtrl);
			}
		} else if (event instanceof FlexiTableSearchEvent) {
			loadModel();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event == Event.BACK_EVENT) {
			removeAsListenerAndDispose(defaultsCtrl);
			defaultsCtrl = null;
			initialPanel.popContent();
		} else if (event == Event.CHANGED_EVENT) {
			removeAsListenerAndDispose(defaultsCtrl);
			defaultsCtrl = null;
			defaultsCtrl = selectedCourseNode.createDefaultsController(ureq, getWindowControl());
			// pop content to clear stack
			initialPanel.popContent();
			doOpenEditDefaults(defaultsCtrl);
		}
	}

	private void doOpenEditDefaults(Controller defaultsCtrl) {
		listenTo(defaultsCtrl);
		initialPanel.pushContent(defaultsCtrl.getInitialComponent());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need currently
	}
}