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
package org.olat.modules.jupyterhub.ui;

import static org.olat.core.gui.components.link.LinkFactory.createLink;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.ims.lti13.LTI13Module;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterHubModule;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.modules.jupyterhub.manager.JupyterHubDAO;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubAdminController extends FormBasicController implements Activateable2 {

	private static final String CMD_TOOLS = "tools";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_SHOW_APPLICATIONS = "showApplications";

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private JupyterHubModule jupyterHubModule;
	@Autowired
	private JupyterManager jupyterManager;

	private final boolean ltiEnabled;
	private final SelectionValues enabledKV;
	private MultipleSelectionElement enabledEl;
	private FormLink addJupyterHubButton;
	private JupyterHubsTableModel hubsTableModel;
	private FlexiTableElement hubsTable;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private EditJupyterHubController editJupyterHubController;
	private CloseableModalController modalCtrl;
	private JupyterHubInUseController jupyterHubInUseCtrl;
	private ConfirmDeleteHubController confirmDeleteHubCtrl;
	private ShowJupyterHubApplicationsController showApplicationsCtrl;

	public JupyterHubAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		enabledKV = new SelectionValues();
		enabledKV.add(SelectionValues.entry("on", translate("on")));
		ltiEnabled = lti13Module.isEnabled();
		initForm(ureq);
		if(ltiEnabled) {
			loadModel();
			updateUi();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("jupyterHub.title");
		setFormInfoHelp("manual_user/learningresources/Course_Element_JupyterHub/");
		setFormContextHelp("manual_user/learningresources/Course_Element_JupyterHub/");
		formLayout.setElementCssClass("o_sel_jupyterhub_admin_configuration");

		if (!ltiEnabled) {
			setFormWarning("jupyterHub.warning.no.lti");
			return;
		}

		enabledEl = uifactory.addCheckboxesHorizontal("jupyterHub.courseElement", formLayout, enabledKV.keys(), enabledKV.values());
		enabledEl.setElementCssClass("o_sel_jupyterhub_admin_enable");
		enabledEl.select(enabledKV.keys()[0], jupyterHubModule.isEnabled() && jupyterHubModule.isEnabledForCourseElement());
		enabledEl.addActionListener(FormEvent.ONCHANGE);

		initTable(formLayout);

		addJupyterHubButton = uifactory.addFormLink("jupyterHub.add", formLayout, Link.BUTTON);
		addJupyterHubButton.setElementCssClass("o_sel_jupyterhub_add");
	}

	private void initTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JupyterHubsTableModel.JupyterHubCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JupyterHubsTableModel.JupyterHubCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JupyterHubsTableModel.JupyterHubCols.clientId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JupyterHubsTableModel.JupyterHubCols.ram));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(JupyterHubsTableModel.JupyterHubCols.cpu));

		FlexiCellRenderer renderer = new StaticFlexiCellRenderer(CMD_SHOW_APPLICATIONS, new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
				JupyterHubsTableModel.JupyterHubCols.applications.i18nHeaderKey(),
				JupyterHubsTableModel.JupyterHubCols.applications.ordinal(),
				CMD_SHOW_APPLICATIONS,
				true,
				JupyterHubsTableModel.JupyterHubCols.applications.name(),
				renderer
		));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), CMD_EDIT));

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				JupyterHubsTableModel.JupyterHubCols.tools.i18nHeaderKey(),
				JupyterHubsTableModel.JupyterHubCols.tools.ordinal()
		);
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsColumn);

		hubsTableModel = new JupyterHubsTableModel(columnsModel, getLocale());

		hubsTable = uifactory.addTableElement(getWindowControl(), "hubs", hubsTableModel, 10,
				false, getTranslator(), formLayout);
		hubsTable.setLabel("jupyterHub.configurations", null);
	}

	private void loadModel() {	
		List<JupyterHubRow> rows = jupyterManager.getJupyterHubsWithApplicationCounts().stream().map(this::mapHubToHubRow).toList();
		hubsTableModel.setObjects(rows);
		hubsTable.reset(true, true, true);
	}

	private JupyterHubRow mapHubToHubRow(JupyterHubDAO.JupyterHubWithApplicationCount jupyterHubWithApplicationCount) {
		JupyterHubRow row = new JupyterHubRow(jupyterHubWithApplicationCount.getJupyterHub(), jupyterHubWithApplicationCount.getApplicationCount());
		addToolLink(row, jupyterHubWithApplicationCount.getJupyterHub());
		return row;
	}

	private void addToolLink(JupyterHubRow row, JupyterHub jupyterHub) {
		String toolId = "tool_" + jupyterHub.getKey();
		FormLink toolLink = (FormLink) flc.getFormComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", hubsTable,
					Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(row);
		row.setToolLink(toolLink);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(modalCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(jupyterHubInUseCtrl);
		removeAsListenerAndDispose(confirmDeleteHubCtrl);
		removeAsListenerAndDispose(editJupyterHubController);
		removeAsListenerAndDispose(showApplicationsCtrl);

		calloutCtrl = null;
		modalCtrl = null;
		toolsCtrl = null;
		jupyterHubInUseCtrl = null;
		confirmDeleteHubCtrl = null;
		editJupyterHubController = null;
		showApplicationsCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (toolsCtrl == source) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if (editJupyterHubController == source) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
			modalCtrl.deactivate();
			cleanUp();
		} else if (modalCtrl == source) {
			cleanUp();
			updateUi();
		} else if (jupyterHubInUseCtrl == source) {
			modalCtrl.deactivate();
			cleanUp();
		} else if (confirmDeleteHubCtrl == source) {
			if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
			modalCtrl.deactivate();
			cleanUp();
		} else if (showApplicationsCtrl == source) {
			if (event instanceof ShowJupyterHubApplicationsController.OpenBusinessPathEvent openBusinessPathEvent) {
				modalCtrl.deactivate();
				cleanUp();
				String businessPath = openBusinessPathEvent.getBusinessPath();
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
			updateUi();
		}
		super.event(ureq, source, event);
	}

	private void updateUi() {
		enabledEl.select(enabledKV.keys()[0], jupyterHubModule.isEnabled() && jupyterHubModule.isEnabledForCourseElement());

		if (enabledEl.isAtLeastSelected(1)) {
			hubsTable.setVisible(true);
			addJupyterHubButton.setVisible(true);
		} else {
			hubsTable.setVisible(false);
			addJupyterHubButton.setVisible(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enabledEl == source) {
			loadModel();
			doSetEnabled();
		} else if (addJupyterHubButton == source) {
			doAdd(ureq);
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof JupyterHubRow row) {
				doOpenTools(ureq, link, row);
			}
		} else if (hubsTable == source) {
			if (event instanceof SelectionEvent selectionEvent) {
				if (CMD_EDIT.equals(selectionEvent.getCommand())) {
					doEdit(ureq, hubsTableModel.getObject(selectionEvent.getIndex()));
				} else if (CMD_SHOW_APPLICATIONS.equals(selectionEvent.getCommand())) {
					doShowApplications(ureq, hubsTableModel.getObject(selectionEvent.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetEnabled() {
		if (enabledEl.isAtLeastSelected(1)) {
			jupyterHubModule.setEnabled(true);
			jupyterHubModule.setEnabledForCourseElement(true);
		} else {
			jupyterHubModule.setEnabled(false);
			jupyterHubModule.setEnabledForCourseElement(false);
		}
		updateUi();
	}

	private void doAdd(UserRequest ureq) {
		if (guardModalController(editJupyterHubController)) {
			return;
		}

		editJupyterHubController = new EditJupyterHubController(ureq, getWindowControl());
		listenTo(editJupyterHubController);

		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
				editJupyterHubController.getInitialComponent(), true, translate("jupyterHub.add"));
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doOpenTools(UserRequest ureq, FormLink link, JupyterHubRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doEdit(UserRequest ureq, JupyterHubRow row) {
		if (guardModalController(editJupyterHubController)) {
			return;
		}

		JupyterHub jupyterHub = row.getHub();

		editJupyterHubController = new EditJupyterHubController(ureq, getWindowControl(), jupyterHub);
		listenTo(editJupyterHubController);

		String title = translate("jupyterHub.edit", jupyterHub.getName());
		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
				editJupyterHubController.getInitialComponent(), true, title);
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doCopy(JupyterHubRow row) {
		JupyterHub jupyterHub = row.getHub();
		jupyterManager.copyProfile(jupyterHub, getTranslator().translate("jupyterHub.copy.suffix"));
		loadModel();
	}

	private void doWarnInUse(UserRequest ureq, JupyterHubRow row) {
		if (guardModalController(jupyterHubInUseCtrl)) {
			return;
		}

		JupyterHub jupyterHub = row.getHub();
		jupyterHubInUseCtrl = new JupyterHubInUseController(ureq, getWindowControl(), jupyterHub);
		listenTo(jupyterHubInUseCtrl);

		String title = translate("jupyterHub.warning.inUse.title", jupyterHub.getName());
		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"), jupyterHubInUseCtrl.getInitialComponent(), true, title);
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doConfirmDelete(UserRequest ureq, JupyterHubRow row) {
		if (guardModalController(confirmDeleteHubCtrl)) {
			return;
		}

		JupyterHub jupyterHub = row.getHub();

		confirmDeleteHubCtrl = new ConfirmDeleteHubController(ureq, getWindowControl(), jupyterHub);
		listenTo(confirmDeleteHubCtrl);

		String title = translate("jupyterHub.confirm.delete.title", jupyterHub.getName());
		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
				confirmDeleteHubCtrl.getInitialComponent(), true, title);
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	private void doActivate(JupyterHubRow row) {
		JupyterHub hub = row.getHub();
		hub.setStatus(JupyterHub.JupyterHubStatus.active);
		jupyterManager.updateJupyterHub(hub);
		loadModel();
	}

	private void doDeactivate(JupyterHubRow row) {
		JupyterHub hub = row.getHub();
		hub.setStatus(JupyterHub.JupyterHubStatus.inactive);
		jupyterManager.updateJupyterHub(hub);
		loadModel();
	}

	private void doShowApplications(UserRequest ureq, JupyterHubRow row) {
		if (guardModalController(showApplicationsCtrl)) {
			return;
		}

		JupyterHub jupyterHub = row.getHub();

		showApplicationsCtrl = new ShowJupyterHubApplicationsController(ureq, getWindowControl(), jupyterHub);
		listenTo(showApplicationsCtrl);

		String title = translate("jupyterHub.applications", jupyterHub.getName());
		modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
				showApplicationsCtrl.getInitialComponent(), true, title);
		modalCtrl.activate();
		listenTo(modalCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		updateUi();
	}

	private class ToolsController extends BasicController {
		private final Link editLink, copyLink, deleteLink;
		private Link deactivateLink, activateLink;
		private final JupyterHubRow row;

		ToolsController(UserRequest ureq, WindowControl wControl, JupyterHubRow row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tools");

			editLink = createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("tool.edit", editLink);

			copyLink = createLink("tools.copy", "copy", getTranslator(), mainVC, this, Link.LINK);
			copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			mainVC.put("tool.copy", copyLink);

			if (row.getStatus() == JupyterHub.JupyterHubStatus.active) {
				deactivateLink = createLink("tools.deactivate", "deactivate", getTranslator(), mainVC, this, Link.LINK);
				deactivateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_deactivate");
				mainVC.put("tool.deactivate", deactivateLink);
			}

			if (row.getStatus() == JupyterHub.JupyterHubStatus.inactive) {
				activateLink = createLink("tools.activate", "activate", getTranslator(), mainVC, this, Link.LINK);
				activateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_activate");
				mainVC.put("tool.activate", activateLink);
			}

			deleteLink = createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("tool.delete", deleteLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (editLink == source) {
				doEdit(ureq, row);
			} else if (copyLink == source) {
				doCopy(row);
			} else if (deleteLink == source) {
				if (jupyterManager.isInUse(row.getHub())) {
					doWarnInUse(ureq, row);
				} else {
					doConfirmDelete(ureq, row);
				}
			} else if (deactivateLink == source) {
				doDeactivate(row);
			} else if (activateLink == source) {
				doActivate(row);
			}
		}
	}

	private static class JupyterHubInUseController extends FormBasicController {
		final private JupyterHub jupyterHub;

		protected JupyterHubInUseController(UserRequest ureq, WindowControl wControl, JupyterHub jupyterHub) {
			super(ureq, wControl, "jupyter_hub_in_use");
			this.jupyterHub = jupyterHub;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("msg", translate("jupyterHub.warning.inUse", jupyterHub.getName()));
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
