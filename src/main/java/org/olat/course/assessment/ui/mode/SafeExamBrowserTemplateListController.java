/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultConfigCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.SafeExamBrowserTemplateSearchParams;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.course.assessment.ui.mode.SafeExamBrowserTemplateDataModel.SEBTemplateCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateListController extends FormBasicController {

	private FormLink createTemplateButton;
	private FlexiTableElement tableEl;
	private SafeExamBrowserTemplateDataModel model;

	private CloseableModalController cmc;
	private SafeExamBrowserTemplateEditController editCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	@Autowired
	private AssessmentModeManager assessmentModeManager;

	public SafeExamBrowserTemplateListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "seb_templates_admin");

		initForm(ureq);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createTemplateButton = uifactory.addFormLink("add.seb.template", formLayout, Link.BUTTON);
		createTemplateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SEBTemplateCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SEBTemplateCols.active, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SEBTemplateCols.isDefault, new DefaultConfigCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SEBTemplateCols.usages));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.browserViewMode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowQuit, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.enableReload, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.showTaskBar, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.showReloadButton, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.showTime, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.showKeyboard, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowWlan, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.audioControl, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.audioMute, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowAudioCapture, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowVideoCapture, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowSpellCheck, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.allowZoom, new YesNoCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SEBTemplateCols.urlFilter, new YesNoCellRenderer()));

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(
				SEBTemplateCols.edit.i18nHeaderKey(), SEBTemplateCols.edit.ordinal(), "edit-template",
				new BooleanCellRenderer(new StaticFlexiCellRenderer("", "edit-template", null, "o_icon-lg o_icon_edit", translate("edit")), null));
		editColumn.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_edit");
		editColumn.setHeaderLabel(translate("edit"));
		editColumn.setAlwaysVisible(true);
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);

		columnsModel.addFlexiColumnModel(new ActionsColumnModel(SEBTemplateCols.tools));

		model = new SafeExamBrowserTemplateDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "templates", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(true);
		tableEl.setSortEnabled(true);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(SEBTemplateCols.name.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setEmptyTableMessageKey("seb.template.table.empty");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createTemplateButton) {
			doEdit(ureq, null);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent se && "edit-template".equals(se.getCommand())) {
				SafeExamBrowserTemplateRow row = model.getObject(se.getIndex());
				doEdit(ureq, row.getTemplate());
			}
		} else if (source instanceof FormLink formLink && "tools".equals(formLink.getCmd())) {
			doOpenTools(ureq, formLink);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			if (event == Event.DONE_EVENT) {
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	private void reloadModel() {
		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		List<SafeExamBrowserTemplate> templates = assessmentModeManager.getSafeExamBrowserTemplates(params);
		Map<Long, Long> usageCounts = assessmentModeManager.getSafeExamBrowserTemplateUsageCounts();

		List<SafeExamBrowserTemplateRow> rows = new ArrayList<>(templates.size());
		for (SafeExamBrowserTemplate template : templates) {
			SafeExamBrowserConfiguration config = template.getSafeExamBrowserConfiguration();
			Long usage = usageCounts.getOrDefault(template.getKey(), 0L);
			SafeExamBrowserTemplateRow row = new SafeExamBrowserTemplateRow(template, config, usage);
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsButton(toolsLink);
			rows.add(row);
		}

		model.setObjects(rows);
		tableEl.reset();
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), (SafeExamBrowserTemplateRow) link.getUserObject());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doEdit(UserRequest ureq, SafeExamBrowserTemplate template) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new SafeExamBrowserTemplateEditController(ureq, getWindowControl(), template);
		listenTo(editCtrl);

		String title = template != null ? translate("edit.seb.template") : translate("add.seb.template");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private void doSetDefault(SafeExamBrowserTemplate template) {
		SafeExamBrowserTemplateSearchParams params = new SafeExamBrowserTemplateSearchParams();
		params.setDefault(Boolean.TRUE);
		List<SafeExamBrowserTemplate> allTemplates = assessmentModeManager.getSafeExamBrowserTemplates(params);
		for (SafeExamBrowserTemplate t : allTemplates) {
			t.setDefault(false);
			assessmentModeManager.updateSafeExamBrowserTemplate(t);
		}
		template.setDefault(true);
		assessmentModeManager.updateSafeExamBrowserTemplate(template);
		reloadModel();
	}

	private void doToggleActive(SafeExamBrowserTemplate template) {
		template.setActive(!template.isActive());
		assessmentModeManager.updateSafeExamBrowserTemplate(template);
		reloadModel();
	}

	private void doDelete(SafeExamBrowserTemplate template) {
		assessmentModeManager.deleteSafeExamBrowserTemplate(template);
		reloadModel();
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link editLink;
		private Link setDefaultLink;
		private Link toggleActiveLink;
		private Link deleteLink;
		private final SafeExamBrowserTemplateRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, SafeExamBrowserTemplateRow row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(4);

			editLink = addLink("edit", "o_icon_edit", links);
			if (!row.isDefault() && row.isActive()) {
				setDefaultLink = addLink("seb.template.set.default", "o_icon_star", links);
			}
			if (row.isActive() && !row.isDefault()) {
				toggleActiveLink = addLink("seb.template.deactivate", "o_icon_deactivate", links);
			} else if (!row.isActive()) {
				toggleActiveLink = addLink("seb.template.activate", "o_icon_activate", links);
			}
			if (row.getUsage() == 0 && !row.isDefault()) {
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}
			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (editLink == source) {
				close();
				doEdit(ureq, row.getTemplate());
			} else if (setDefaultLink == source) {
				close();
				doSetDefault(row.getTemplate());
			} else if (toggleActiveLink == source) {
				close();
				doToggleActive(row.getTemplate());
			} else if (deleteLink == source) {
				close();
				doDelete(row.getTemplate());
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
