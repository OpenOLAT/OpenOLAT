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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.manager.ZoomProfileDAO;
import org.olat.modules.zoom.ui.ZoomProfilesTableModel.ZoomProfileCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Master view controller for the "Zoom LTI Pro configurations" tab in the Zoom
 * administration. Displays the list of Zoom LTI Pro profiles and handles profile
 * CRUD operations. Fires a {@link ShowApplicationsEvent} when the user requests
 * to see the applications of a profile, so the parent can open the detail view.
 *
 * Initial date: 2026-03-26<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ZoomProfileListController extends FormBasicController {

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
    private ZoomManager zoomManager;

    public ZoomProfileListController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        initForm(ureq);
        loadModel();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        formLayout.setElementCssClass("o_sel_zoom_admin_lti_pro_configurations");

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
        columnsModel.addFlexiColumnModel(new ActionsColumnModel(ZoomProfileCols.tools));

        profilesTableModel = new ZoomProfilesTableModel(columnsModel, getLocale());
        profilesTableEl = uifactory.addTableElement(getWindowControl(), "profiles", profilesTableModel, 10, false, getTranslator(), formLayout);

        addProfileButton = uifactory.addFormLink("zoom.add.configuration", formLayout, Link.BUTTON);
        addProfileButton.setElementCssClass("o_sel_zoom_admin_add_profile");
    }

    @Override
    protected void formOK(UserRequest ureq) {
        //
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
        if (source == addProfileButton) {
            doAdd(ureq);
        } else if (source instanceof FormLink link) {
            if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof ZoomProfileRow row) {
                doOpenTools(ureq, link, row);
            }
        } else if (source == profilesTableEl) {
            if (event instanceof SelectionEvent selectionEvent) {
                if ("edit".equals(selectionEvent.getCommand())) {
                    doEdit(ureq, profilesTableModel.getObject(selectionEvent.getIndex()));
                } else if ("showApplications".equals(selectionEvent.getCommand())) {
                    fireEvent(ureq, new ShowApplicationsEvent(profilesTableModel.getObject(selectionEvent.getIndex())));
                }
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    public void loadModel() {
        List<ZoomProfileRow> profileRows = zoomManager.getProfilesWithConfigCount().stream()
                .map(this::mapZoomProfileToRow).collect(toList());
        profilesTableModel.setObjects(profileRows);
        profilesTableEl.reset(true, true, true);
    }

    private ZoomProfileRow mapZoomProfileToRow(ZoomProfileDAO.ZoomProfileWithConfigCount zoomProfileWithConfigCount) {
        ZoomProfileRow row = new ZoomProfileRow(zoomProfileWithConfigCount.getZoomProfile(), zoomProfileWithConfigCount.getConfigCount());
        addToolLink(row);
        return row;
    }

    private void addToolLink(ZoomProfileRow row) {
        FormLink toolLink = ActionsColumnModel.createLink(uifactory, getTranslator());
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

        modalCtrl = new CloseableModalController(getWindowControl(), translate("close"), editZoomProfileCtrl.getInitialComponent(), true, translate("zoom.profile.add"));
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
        modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
                editZoomProfileCtrl.getInitialComponent(), true, title);
        modalCtrl.activate();
        listenTo(modalCtrl);
    }

    private void doCopy(ZoomProfileRow row) {
        zoomManager.copyProfile(row.getZoomProfile(), getTranslator().translate("copy.suffix"));
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
        modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
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
        modalCtrl = new CloseableModalController(getWindowControl(), translate("close"),
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

    public static class ShowApplicationsEvent extends Event {
        private final ZoomProfileRow row;

        public ShowApplicationsEvent(ZoomProfileRow row) {
            super("showApplications");
            this.row = row;
        }

        public ZoomProfileRow getRow() {
            return row;
        }
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