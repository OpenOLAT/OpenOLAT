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
import org.olat.modules.zoom.ui.ZoomProfilesTableModel.ZoomProfileCols;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.olat.core.gui.components.link.LinkFactory.createLink;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomConfigurationController extends FormBasicController {

    private static final String CMD_TOOLS = "tools";

	private static final String[] ENABLE_FOR_KEYS = { "courseElement", "courseTool", "groupTool" };

    private static final String[] enabledKeys = new String[]{"on"};
    private final String[] enabledValues;

    private MultipleSelectionElement moduleEnabledEl;
	private MultipleSelectionElement enableForEl;

    private FlexiTableElement profilesTableEl;
    private ZoomProfilesTableModel profilesTableModel;
	private FormLink addProfileButton;

    private CloseableCalloutWindowController calloutCtrl;
    private ToolsController toolsCtrl;

    private CloseableModalController modalCtrl;
    private ZoomProfileEditController editZoomProfileCtrl;
    private ConfirmDeleteProfileController confirmDeleteProfileCtrl;
    private ProfileInUseController profileInUseCtrl;

    @Autowired
    private ZoomModule zoomModule;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomConfigurationController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        enabledValues = new String[]{translate("enabled")};
        initForm(ureq);
        updateUI();
        loadModel();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("zoom.title");
        setFormInfo("zoom.info");

        moduleEnabledEl = uifactory.addCheckboxesHorizontal("zoom.module.enabled", formLayout, enabledKeys, enabledValues);
        moduleEnabledEl.select(enabledKeys[0], zoomModule.isEnabled());
        moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);
        
        String[] enableForValues = new String[] {
                translate("zoom.module.enable.for.courseElement"),
                translate("zoom.module.enable.for.courseTool"),
            	translate("zoom.module.enable.for.groupTool")
        };
        enableForEl = uifactory.addCheckboxesVertical("zoom.module.enable.for", formLayout, ENABLE_FOR_KEYS, enableForValues, 1);
        enableForEl.select(ENABLE_FOR_KEYS[0], zoomModule.isEnabledForCourseElement());
        enableForEl.select(ENABLE_FOR_KEYS[1], zoomModule.isEnabledForCourseTool());
        enableForEl.select(ENABLE_FOR_KEYS[2], zoomModule.isEnabledForGroupTool());

        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.name));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.status));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.mailDomain));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.studentsCanHost));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ZoomProfileCols.clientId));
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
        boolean enabled = moduleEnabledEl.isSelected(0);
        zoomModule.setEnabled(enabled);

        if (enabled) {
            zoomModule.setEnabledForCourseElement(enableForEl.isSelected(0));
            zoomModule.setEnabledForCourseTool(enableForEl.isSelected(1));
            zoomModule.setEnabledForGroupTool(enableForEl.isSelected(2));
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

        calloutCtrl = null;
        toolsCtrl = null;
        modalCtrl = null;
        editZoomProfileCtrl = null;
        confirmDeleteProfileCtrl = null;
        profileInUseCtrl = null;
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
                }
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    private void updateUI() {
        boolean enabled = moduleEnabledEl.isAtLeastSelected(1);
        enableForEl.setVisible(enabled);
        profilesTableEl.setVisible(enabled);
        addProfileButton.setVisible(enabled);
    }
    
    private void loadModel() {
        List<ZoomProfileRow> profileRows = zoomManager.getProfiles().stream().map(this::mapZoomProfileToRow).collect(toList());
        profilesTableModel.setObjects(profileRows);
        profilesTableEl.reset(true, true, true);
    }

    private ZoomProfileRow mapZoomProfileToRow(ZoomProfile zoomProfile) {
        ZoomProfileRow row = new ZoomProfileRow(zoomProfile);
        addToolLink(row, zoomProfile);
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

    private void doCopy(UserRequest ureq, ZoomProfileRow row) {
        ZoomProfile zoomProfile = row.getZoomProfile();
        zoomManager.copyProfile(zoomProfile);
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

    private void doDeactivate(UserRequest ureq, ZoomProfileRow row) {
        ZoomProfile zoomProfile = row.getZoomProfile();
        zoomProfile.setStatus(ZoomProfile.ZoomProfileStatus.inactive);
        zoomManager.updateProfile(zoomProfile);
        loadModel();
    }

    private void doActivate(UserRequest ureq, ZoomProfileRow row) {
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
                doCopy(ureq, row);
            } else if (deleteLink == source) {
                if (zoomManager.isInUse(row.getZoomProfile())) {
                    doWarnProfileInUse(ureq, row);
                } else {
                    doConfirmDelete(ureq, row);
                }
            } else if (deactivateLink == source) {
                doDeactivate(ureq, row);
            } else if (activateLink == source) {
                doActivate(ureq, row);
            }
        }
    }
}
