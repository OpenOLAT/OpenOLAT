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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.StudentsTableDataModel.Columns;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 May 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public abstract class AbstactCoachListController extends FormBasicController implements Activateable2 {

    protected List<UserPropertyHandler> userPropertyHandlers;

    protected TooledStackedPanel stackPanel;
    protected RoleSecurityCallback securityCallback;

    protected FlexiTableElement tableEl;
    protected StudentsTableDataModel model;

    protected UserOverviewController userCtrl;

    private boolean hasChanged;


    @Autowired
    protected UserManager userManager;
    @Autowired
    protected BaseSecurity securityManager;
    @Autowired
    protected BaseSecurityModule securityModule;

    public AbstactCoachListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
        super(ureq, wControl, LAYOUT_BAREBONE);
        setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

        boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
        userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

        this.stackPanel = stackPanel;
        this.stackPanel.setInvisibleCrumb(0);
        stackPanel.addListener(this);
    }

//    public ListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RelationRole relationRole) {
//        this.relationRole = identityRelationshipService.getRole(relationRole.getKey());
//        this.securityCallback = UserRelationSecurityCallbackFactory.create(this.relationRole.getRights());
//        boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
//        userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
//
//        initForm(ureq);
//        loadModel();
//    }
//
//    public ListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Organisation organisation) {
//        this.organisation = organisation;
//        this.securityCallback = UserRelationSecurityCallbackFactory.create(this.relationRole.getRights());
//        boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
//        userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
//    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        //add the table
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

        int colIndex = UserListController.USER_PROPS_OFFSET;
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer, userPropertyHandler);
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
                    true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
        }

        if (securityCallback.canViewCoursesAndCurriculum()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countCourse));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.initialLaunch, new LightIconRenderer()));
        }

        if (securityCallback.canViewCourseProgressAndStatus()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.completion, new LearningProgressCompletionCellRenderer()));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.countPassed, new ProgressOfCellRenderer()));
        }

        model = new StudentsTableDataModel(columnsModel);
        tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
        tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
        tableEl.setAndLoadPersistedPreferences(ureq, "UserRelationsListController");

        UserSession usess = ureq.getUserSession();
        boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(usess.getRoles());
        if (autoCompleteAllowed) {
            tableEl.setSearchEnabled(new StudentListProvider(model, userManager), usess);
        }
    }

    @Override
    protected void doDispose() {
        stackPanel.removeListener(this);
        super.doDispose();
    }

    @Override
    protected void formOK(UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (tableEl == source) {
            if (event instanceof SelectionEvent) {
                SelectionEvent se = (SelectionEvent) event;
                String cmd = se.getCommand();
                StudentStatEntry selectedRow = model.getObject(se.getIndex());
                if ("select".equals(cmd)) {
                    selectStudent(ureq, selectedRow);
                }
            } else if (event instanceof FlexiTableSearchEvent) {
                FlexiTableSearchEvent ftse = (FlexiTableSearchEvent) event;
                String searchString = ftse.getSearch();
                model.search(searchString);
                tableEl.reset();
                tableEl.reloadData();
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    protected abstract void loadModel();

    private void reloadModel() {
        if (hasChanged) {
            loadModel();
            hasChanged = false;
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == userCtrl) {
            if (event == Event.CHANGED_EVENT) {
                hasChanged = true;
            } else if ("next.student".equals(event.getCommand())) {
                nextStudent(ureq);
            } else if ("previous.student".equals(event.getCommand())) {
                previousStudent(ureq);
            }
        }
        super.event(ureq, source, event);
    }

    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        if (stackPanel == source) {
            if (event instanceof PopEvent) {
                PopEvent pe = (PopEvent) event;
                if (pe.getController() == this.userCtrl && hasChanged) {
                    reloadModel();
                }
            }
        }
        super.event(ureq, source, event);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if (entries == null || entries.isEmpty()) return;

        ContextEntry ce = entries.get(0);
        OLATResourceable ores = ce.getOLATResourceable();
        if ("Identity".equals(ores.getResourceableTypeName())) {
            Long identityKey = ores.getResourceableId();
            for (StudentStatEntry entry : model.getObjects()) {
                if (identityKey.equals(entry.getIdentityKey())) {
                    Activateable2 selectedCtrl = selectStudent(ureq, entry);
                    selectedCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
                    break;
                }
            }
        }
    }

    protected void previousStudent(UserRequest ureq) {
        StudentStatEntry currentEntry = userCtrl.getEntry();
        int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
        if (previousIndex < 0 || previousIndex >= model.getRowCount()) {
            previousIndex = model.getRowCount() - 1;
        }
        StudentStatEntry previousEntry = model.getObject(previousIndex);
        selectStudent(ureq, previousEntry);
    }

    protected void nextStudent(UserRequest ureq) {
        StudentStatEntry currentEntry = userCtrl.getEntry();
        int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
        if (nextIndex < 0 || nextIndex >= model.getRowCount()) {
            nextIndex = 0;
        }
        StudentStatEntry nextEntry = model.getObject(nextIndex);
        selectStudent(ureq, nextEntry);
    }

    protected abstract UserOverviewController selectStudent(UserRequest ureq, StudentStatEntry studentStat);
}
