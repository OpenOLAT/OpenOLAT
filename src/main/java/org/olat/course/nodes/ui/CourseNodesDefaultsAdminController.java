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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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

	private FlexiTableElement tableEl;
	private CourseNodesDefaultsDataModel dataModel;

	private Controller defaultsCtrl;
	private CloseableModalController cmc;


	public CourseNodesDefaultsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.courseElement));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.enabledToggle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.editConfig.i18nHeaderKey(),
				CourseNodesDefaultsCols.editConfig.ordinal(), "editConfig", new StaticFlexiCellRenderer("", "editConfig",
				"o_icon o_icon-lg o_icon_edit", null, translate("edit"))));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseNodesDefaultsCols.courseNodeManual));

		dataModel = new CourseNodesDefaultsDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);

		loadModel();
	}

	private void loadModel() {
		List<CourseNodeConfiguration> allCourseNodeConfigs = CourseNodeFactory.getInstance().getAllCourseNodeConfigs();

		List<CourseNodeDefaultConfigRow> rows = new ArrayList<>();
		for (CourseNodeConfiguration courseNodeConfig : allCourseNodeConfigs) {
			if (courseNodeConfig.getInstance() instanceof CourseNodeWithDefaults cnConfig) {
				if (cnConfig instanceof GTACourseNode gtaCourseNode
						&& gtaCourseNode.getType().equalsIgnoreCase(GTACourseNode.TYPE_GROUP)) {
					// skip GTA, because GTA and ITA share same configuration; GTA Config is a subset of ITA
					continue;
				}
				String courseElement = courseNodeConfig.getLinkText(getLocale());
				FormToggle enabledToggle = uifactory.addToggleButton("enabled", null, translate("on"), translate("off"), null);
				if (courseNodeConfig.isEnabled()) {
					enabledToggle.toggleOn();
				} else {
					enabledToggle.toggleOff();
				}
				// for now, it is disabled per default
				enabledToggle.setEnabled(false);
				String cnConfigManualUrl = cnConfig.getCourseNodeConfigManualUrl(getLocale());
				ExternalLinkItem externalManualLinkItem = null;
				// if no URL is present then keep the column in that row empty
				if (cnConfigManualUrl != null) {
					externalManualLinkItem = uifactory.addExternalLink("config.manual", cnConfigManualUrl, "_blank", null);
					externalManualLinkItem.setCssClass("o_icon o_icon-lg o_icon_help");
				}
				CourseNodeDefaultConfigRow row = new CourseNodeDefaultConfigRow(courseElement, enabledToggle, externalManualLinkItem);
				row.setCourseNodeWithDefaults(cnConfig);
				rows.add(row);
			}
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && (event instanceof SelectionEvent se)) {
			CourseNodeDefaultConfigRow row = dataModel.getObject(se.getIndex());
			CourseNodeWithDefaults rowCNConfig = row.getCourseNodeWithDefaults();
			if ("editConfig".equalsIgnoreCase(se.getCommand())) {
				defaultsCtrl = rowCNConfig.createDefaultsController(ureq, getWindowControl());
				doOpenEditDefaults(defaultsCtrl);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == defaultsCtrl && event == Event.DONE_EVENT) {
			cmc.deactivate();
		}
	}

	private void doOpenEditDefaults(Controller defaultsCtrl) {
		listenTo(defaultsCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), defaultsCtrl.getInitialComponent(), true, translate("course.node.defaults.edit.config"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need currently
	}
}
