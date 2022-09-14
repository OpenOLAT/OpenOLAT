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
package org.olat.modules.zoom.ui;

import static java.util.stream.Collectors.toList;
import static org.olat.core.gui.components.link.LinkFactory.createLink;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.*;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomModule;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.manager.ZoomProfileDAO;
import org.olat.modules.zoom.ui.ZoomProfilesTableModel.ZoomProfileCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
public class ZoomConfigurationController extends FormBasicController {

    private static final String CMD_TOOLS = "tools";

    private final SelectionValues moduleEnabledKV = new SelectionValues();
    private final SelectionValues enableForKV = new SelectionValues();
    private final SelectionValues enableCalendarEntriesKV = new SelectionValues();

    private MultipleSelectionElement moduleEnabledEl;
	private MultipleSelectionElement enableForEl;
    private MultipleSelectionElement enableCalendarEntriesEl;

    private FlexiTableElement profilesTableEl;
    private ZoomProfilesTableModel profilesTableModel;
	private FormLink addProfileButton;

    private CloseableCalloutWindowController calloutCtrl;
    private ToolsController toolsCtrl;

    private CloseableModalController modalCtrl;
    private ZoomProfileEditController editZoomProfileCtrl;
    private ConfirmDeleteProfileController confirmDeleteProfileCtrl;
    private ProfileInUseController profileInUseCtrl;
    private ShowZoomApplicationsController showApplicationsCtrl;

    @Autowired
    private ZoomModule zoomModule;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomConfigurationController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        moduleEnabledKV.add(SelectionValues.entry("on", translate("enabled")));
        enableForKV.add(SelectionValues.entry(ZoomManager.ApplicationType.courseElement.name(), translate("zoom.module.enable.for.courseElement")));
        enableForKV.add(SelectionValues.entry(ZoomManager.ApplicationType.courseTool.name(), translate("zoom.module.enable.for.courseTool")));
        enableForKV.add(SelectionValues.entry(ZoomManager.ApplicationType.groupTool.name(), translate("zoom.module.enable.for.groupTool")));
        enableCalendarEntriesKV.add(SelectionValues.entry("on", translate("enabled")));
        initForm(ureq);
        updateUI();
        loadModel();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("zoom.title");
        setFormInfo("zoom.info");
        setFormContextHelp("manual_admin/administration/Zoom/");

        moduleEnabledEl = uifactory.addCheckboxesHorizontal("zoom.module.enabled", formLayout, moduleEnabledKV.keys(), moduleEnabledKV.values());
        moduleEnabledEl.select(moduleEnabledKV.keys()[0], zoomModule.isEnabled());
        moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);

        enableForEl = uifactory.addCheckboxesVertical("zoom.module.enable.for", formLayout, enableForKV.keys(), enableForKV.values(), 1);
        enableForEl.select(enableForKV.keys()[0], zoomModule.isEnabledForCourseElement());
        enableForEl.select(enableForKV.keys()[1], zoomModule.isEnabledForCourseTool());
        enableForEl.select(enableForKV.keys()[2], zoomModule.isEnabledForGroupTool());
        enableForEl.addActionListener(FormEvent.ONCHANGE);

        enableCalendarEntriesEl = uifactory.addCheckboxesHorizontal("zoom.module.zoomCanSetCalendarEntries", formLayout, enableCalendarEntriesKV.keys(), enableCalendarEntriesKV.values());
        enableCalendarEntriesEl.select(enableCalendarEntriesKV.keys()[0], zoomModule.isCalendarEntriesEnabled());
        enableCalendarEntriesEl.addActionListener(FormEvent.ONCHANGE);

        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.name));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.status));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.mailDomain));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.clientId));
        FlexiCellRenderer renderer = new StaticFlexiCellRenderer("showApplications", new TextFlexiCellRenderer());
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
                ZoomProfileCols.applications.i18nHeaderKey(),
                ZoomProfileCols.applications.ordinal(),
                "showApplications",
                true,
                ZoomProfileCols.applications.name(),
                renderer
        ));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));

        StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ZoomProfileCols.tools.i18nHeaderKey(), ZoomProfileCols.tools.ordinal());
        toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
        columnsModel.addFlexiColumnModel(toolsColumn);

        profilesTableModel = new ZoomProfilesTableModel(columnsModel, getLocale());
        profilesTableEl = uifactory.addTableElement(getWindowControl(), "profiles", profilesTableModel, 10, false, getTranslator(), formLayout);

		addProfileButton = uifactory.addFormLink("zoom.add.configuration", formLayout, Link.BUTTON);

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean valid = super.validateFormLogic(ureq);

        boolean enabled = moduleEnabledEl.isSelected(0);
        profilesTableEl.clearError();
        if (enabled) {
            if (profilesTableModel.getRowCount() == 0) {
                profilesTableEl.setErrorKey("zoom.profiles.mandatory", null);
                valid = false;
            }
        }

        return valid;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        final boolean enabled = moduleEnabledEl.isSelected(0);
        zoomModule.setEnabled(enabled);

        if (enabled) {
            zoomModule.setEnabledForCourseElement(enableForEl.isSelected(0));
            zoomModule.setEnabledForCourseTool(enableForEl.isSelected(1));
            zoomModule.setEnabledForGroupTool(enableForEl.isSelected(2));

            final boolean enableCalendarEntries = enableCalendarEntriesEl.isSelected(0);
            zoomModule.setCalendarEntriesEnabled(enableCalendarEntries);
        }
        CollaborationToolsFactory.getInstance().initAvailableTools();
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (toolsCtrl == source) {
            if (calloutCtrl != null) {
                calloutCtrl.deactivate();
            }
            cleanUp();
        } else if (editZoomProfileCtrl == source) {
            if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
                loadModel();
            }
            modalCtrl.deactivate();
            cleanUp();
        } else if (profileInUseCtrl == source) {
            modalCtrl.deactivate();
            cleanUp();
        } else if (confirmDeleteProfileCtrl == source) {
            if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
                loadModel();
            }
            modalCtrl.deactivate();
            cleanUp();
        } else if (modalCtrl == source) {
            cleanUp();
        } else if (showApplicationsCtrl == source) {
            if (event instanceof ShowZoomApplicationsController.OpenBusinessPathEvent) {
                modalCtrl.deactivate();
                cleanUp();
                String businessPath = ((ShowZoomApplicationsController.OpenBusinessPathEvent) event).getBusinessPath();

                NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());

                reloadModuleState();
                updateUI();
            }
        }
        super.event(ureq, source, event);
    }

    private void cleanUp() {
        removeAsListenerAndDispose(calloutCtrl);
        removeAsListenerAndDispose(toolsCtrl);
        removeAsListenerAndDispose(modalCtrl);
        removeAsListenerAndDispose(editZoomProfileCtrl);
        removeAsListenerAndDispose(confirmDeleteProfileCtrl);
        removeAsListenerAndDispose(profileInUseCtrl);
        removeAsListenerAndDispose(showApplicationsCtrl);

        calloutCtrl = null;
        toolsCtrl = null;
        modalCtrl = null;
        editZoomProfileCtrl = null;
        confirmDeleteProfileCtrl = null;
        profileInUseCtrl = null;
        showApplicationsCtrl = null;
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == moduleEnabledEl) {
            updateUI();
        } else if (source == addProfileButton) {
        	doAdd(ureq);
        } else if (source instanceof FormLink) {
            FormLink link = (FormLink)source;
            if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof ZoomProfileRow) {
                doOpenTools(ureq, link, (ZoomProfileRow)link.getUserObject());
            }
        } else if (source == profilesTableEl) {
            if (event instanceof SelectionEvent) {
                SelectionEvent selectionEvent = (SelectionEvent)event;
                if ("edit".equals(selectionEvent.getCommand())) {
                    doEdit(ureq, profilesTableModel.getObject(selectionEvent.getIndex()));
                } else if ("showApplications".equals(selectionEvent.getCommand())) {
                    doShowApplications(ureq, profilesTableModel.getObject(selectionEvent.getIndex()));
                }
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    private void reloadModuleState() {
        moduleEnabledEl.select(moduleEnabledKV.keys()[0], zoomModule.isEnabled());
    }

    private void updateUI() {
        boolean enabled = moduleEnabledEl.isAtLeastSelected(1);
        enableForEl.setVisible(enabled);
        enableForEl.select(enableForKV.keys()[0], zoomModule.isEnabledForCourseElement());
        enableForEl.select(enableForKV.keys()[1], zoomModule.isEnabledForCourseTool());
        enableForEl.select(enableForKV.keys()[2], zoomModule.isEnabledForGroupTool());

        enableCalendarEntriesEl.setVisible(enabled);
        enableCalendarEntriesEl.select(enableCalendarEntriesKV.keys()[0], zoomModule.isCalendarEntriesEnabled());
        enableCalendarEntriesEl.setHelpTextKey("zoom.module.zoomCanSetCalendarEntries.help", null);

        profilesTableEl.setVisible(enabled);
        addProfileButton.setVisible(enabled);
    }
    
    private void loadModel() {
        List<ZoomProfileRow> profileRows = zoomManager.getProfilesWithConfigCount().stream().map(this::mapZoomProfileToRow).collect(toList());
        profilesTableModel.setObjects(profileRows);
        profilesTableEl.reset(true, true, true);
    }

    private ZoomProfileRow mapZoomProfileToRow(ZoomProfileDAO.ZoomProfileWithConfigCount zoomProfileWithConfigCount) {
        ZoomProfileRow row = new ZoomProfileRow(zoomProfileWithConfigCount.getZoomProfile(), zoomProfileWithConfigCount.getConfigCount());
        addToolLink(row, zoomProfileWithConfigCount.getZoomProfile());
        return row;
    }

    private void addToolLink(ZoomProfileRow row, ZoomProfile zoomProfile) {
        String toolId = "tool_" + zoomProfile.getKey();
        FormLink toolLink = (FormLink)flc.getFormComponent(toolId);
        if (toolLink == null) {
            toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", profilesTableEl, Link.LINK | Link.NONTRANSLATED);
            toolLink.setTranslator(getTranslator());
            toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
            toolLink.setTitle(translate("table.header.actions"));
        }
        toolLink.setUserObject(row);
        row.setToolLink(toolLink);
    }

    private void doOpenTools(UserRequest ureq, FormLink link, ZoomProfileRow row) {
        toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
        listenTo(toolsCtrl);

        calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
        listenTo(calloutCtrl);
        calloutCtrl.activate();
    }

    private void doAdd(UserRequest ureq) {
        if (guardModalController(editZoomProfileCtrl)) {
            return;
        }

        editZoomProfileCtrl = new ZoomProfileEditController(ureq, getWindowControl());
        listenTo(editZoomProfileCtrl);

        modalCtrl = new CloseableModalController(getWindowControl(), "close", editZoomProfileCtrl.getInitialComponent(), true, translate("zoom.profile.add"));
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doEdit(UserRequest ureq, ZoomProfileRow row) {
        if (guardModalController(editZoomProfileCtrl)) {
            return;
        }

        ZoomProfile zoomProfile = row.getZoomProfile();

        editZoomProfileCtrl = new ZoomProfileEditController(ureq, getWindowControl(), zoomProfile);
        listenTo(editZoomProfileCtrl);

        String title = translate("zoom.profile.edit", zoomProfile.getName());
        modalCtrl = new CloseableModalController(getWindowControl(), "close",
                editZoomProfileCtrl.getInitialComponent(), true, title);
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doShowApplications(UserRequest ureq, ZoomProfileRow row) {
        if (guardModalController(showApplicationsCtrl)) {
            return;
        }

        ZoomProfile zoomProfile = row.getZoomProfile();

        showApplicationsCtrl = new ShowZoomApplicationsController(ureq, getWindowControl(), zoomProfile);
        listenTo(showApplicationsCtrl);

        String title = translate("zoom.profile.applications", zoomProfile.getName());
        modalCtrl = new CloseableModalController(getWindowControl(), "close",
                showApplicationsCtrl.getInitialComponent(), true, title);
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doCopy(ZoomProfileRow row) {
        ZoomProfile zoomProfile = row.getZoomProfile();
        zoomManager.copyProfile(zoomProfile, getTranslator().translate("copy.suffix"));
        loadModel();
    }

    private void doWarnProfileInUse(UserRequest ureq, ZoomProfileRow row) {
        if (guardModalController(profileInUseCtrl)) {
            return;
        }

        ZoomProfile zoomProfile = row.getZoomProfile();

        profileInUseCtrl = new ProfileInUseController(ureq, getWindowControl(), zoomProfile);
        listenTo(profileInUseCtrl);

        String title = translate("zoom.profile.in.use.title", zoomProfile.getName());
        modalCtrl = new CloseableModalController(getWindowControl(), "close",
                profileInUseCtrl.getInitialComponent(), true, title);
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doConfirmDelete(UserRequest ureq, ZoomProfileRow row) {
        if (guardModalController(confirmDeleteProfileCtrl)) return;

        ZoomProfile zoomProfile = row.getZoomProfile();

        confirmDeleteProfileCtrl = new ConfirmDeleteProfileController(ureq, getWindowControl(), zoomProfile);
        listenTo(confirmDeleteProfileCtrl);

        String title = translate("confirm.delete.profile.title", zoomProfile.getName());
        modalCtrl = new CloseableModalController(getWindowControl(), "close",
                confirmDeleteProfileCtrl.getInitialComponent(), true, title);
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doDeactivate(ZoomProfileRow row) {
        ZoomProfile zoomProfile = row.getZoomProfile();
        zoomProfile.setStatus(ZoomProfile.ZoomProfileStatus.inactive);
        zoomManager.updateProfile(zoomProfile);
        loadModel();
    }

    private void doActivate(ZoomProfileRow row) {
        ZoomProfile zoomProfile = row.getZoomProfile();
        zoomProfile.setStatus(ZoomProfile.ZoomProfileStatus.active);
        zoomManager.updateProfile(zoomProfile);
        loadModel();
    }

    private class ToolsController extends BasicController {
        private final VelocityContainer mainVC;
        private final Link editLink;
        private final Link copyLink;
        private final Link deleteLink;
        private Link deactivateLink;
        private Link activateLink;

        private final ZoomProfileRow row;

        public ToolsController(UserRequest ureq, WindowControl wControl, ZoomProfileRow row) {
            super(ureq, wControl);
            this.row = row;

            mainVC = createVelocityContainer("tools");

            editLink = createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
            editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
            mainVC.put("tool.edit", editLink);

            copyLink = createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
            copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
            mainVC.put("tool.copy", copyLink);

            deleteLink = createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
            deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
            mainVC.put("tool.delete", deleteLink);

            if (row.getStatus() == ZoomProfile.ZoomProfileStatus.active) {
                deactivateLink = createLink("deactivate", "deactivate", getTranslator(), mainVC, this, Link.LINK);
                deactivateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_deactivate");
                mainVC.put("tool.deactivate", deactivateLink);
            }

            if (row.getStatus() == ZoomProfile.ZoomProfileStatus.inactive) {
                activateLink = createLink("activate", "activate", getTranslator(), mainVC, this, Link.LINK);
                activateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_activate");
                mainVC.put("tool.activate", activateLink);
            }

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
                if (zoomManager.isInUse(row.getZoomProfile())) {
                    doWarnProfileInUse(ureq, row);
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
}
