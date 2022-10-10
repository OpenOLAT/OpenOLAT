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
package org.olat.course.nodes.pf.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.modules.ModuleConfiguration;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class PFFolderTemplateController extends FormBasicController {

    private static final String TEMPLATE_CREATE_SUBFOLDER = "table.elementCreateSubFolder";
    private static final String TEMPLATE_TOOLS_LINK = "tools";
    private final List<PFFolderTemplateRow> initBoxes = new ArrayList<>(3);
    private final Map<String, PFFolderTemplateRow> keyToRows = new HashMap<>();
    private final PFCourseNode pfNode;
    private List<String> elements = new ArrayList<>();
    private String folderToDelete;
    private PFFolderTemplateTreeTableModel tableDataModel;
    private FlexiTableElement tableEl;
    private PFCreateFolderTemplateController createFolderTemplateCtrl;
    private CloseableCalloutWindowController toolsCalloutCtrl;
    private DialogBoxController deleteDialogCtrl;
    private CloseableModalController cmc;
    private ToolsController toolsCtrl;


    public PFFolderTemplateController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        this.pfNode = pfNode;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("form.template");
        setFormContextHelp("manual_user/course_elements/Communication_and_Collaboration/#participant_folder");
        setFormInfo("template.info");

        FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(false);
        tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PFFolderTemplateCols.folderName, treeNodeRenderer));
        tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PFFolderTemplateCols.numOfChildren));
        tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PFFolderTemplateCols.createSubFolder));
        tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PFFolderTemplateCols.toolsLink));

        tableDataModel = new PFFolderTemplateTreeTableModel(tableColumnModel);

        tableEl = uifactory.addTableElement(getWindowControl(), "FolderTemplateTable", tableDataModel, getTranslator(), formLayout);

        readTemplateStructure(ureq, true, true);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source instanceof FormLink) {
            FormLink link = (FormLink) source;
            String cmd = link.getCmd();
            if (TEMPLATE_TOOLS_LINK.equals(cmd)) {
                doOpenTools(ureq, link);
            }
            if (TEMPLATE_CREATE_SUBFOLDER.equals(cmd)) {
                doCreateSubFolder(ureq, link.getComponent().getComponentName().replaceAll(".+?_", ""));
            }
        }

        super.formInnerEvent(ureq, source, event);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == createFolderTemplateCtrl) {
            if (event == Event.DONE_EVENT) {
                String newFolderName = createFolderTemplateCtrl.getSubFolderNameEl().getValue();
                String path = createFolderTemplateCtrl.getFolderElement();
                saveTemplateStructure(ureq, path, newFolderName);
                readTemplateStructure(ureq, false, true);
            }
            deactivateCmc();
            cleanUp();
        } else if (source == deleteDialogCtrl) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                doDelete();
                fireEvent(ureq, Event.CHANGED_EVENT);
                readTemplateStructure(ureq, false, true);
            }
            cleanUp();
        }
    }

    private void deactivateCmc() {
        if (cmc != null) {
            cmc.deactivate();
        }
    }

    private void cleanUp() {
        removeAsListenerAndDispose(createFolderTemplateCtrl);
        removeAsListenerAndDispose(deleteDialogCtrl);
        removeAsListenerAndDispose(toolsCalloutCtrl);
        removeAsListenerAndDispose(toolsCtrl);
        removeAsListenerAndDispose(cmc);
        createFolderTemplateCtrl = null;
        deleteDialogCtrl = null;
        toolsCalloutCtrl = null;
        toolsCtrl = null;
        cmc = null;
    }

    private PFFolderTemplateRow forgeRow(String folderName, String folderPath) {
        FormLink createSubFolderLink = uifactory.addFormLink("subFolderLink_" + folderPath, TEMPLATE_CREATE_SUBFOLDER, TEMPLATE_CREATE_SUBFOLDER, null, null, Link.LINK);
        FormLink toolsLink = uifactory.addFormLink("tools_" + folderPath, TEMPLATE_TOOLS_LINK, "", null, null, Link.NONTRANSLATED);
        toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
        PFFolderTemplateRow row = new PFFolderTemplateRow(folderName, toolsLink, createSubFolderLink, getTranslator());
        createSubFolderLink.setUserObject(row);
        toolsLink.setUserObject(row);
        return row;
    }

    private void readTemplateStructure(UserRequest ureq, boolean resetPage, boolean resetInternal) {
        elements.clear();
        initBoxes.clear();

        ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();

        if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null) {
            elements = new ArrayList<>(Arrays.asList(moduleConfiguration
                    .get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString()
                    .split(",")));
            elements.removeIf(String::isBlank);
        } else {
            moduleConfiguration.setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, "");
            // Persists config
            fireEvent(ureq, Event.CHANGED_EVENT);
        }


        elements.add(PFManager.FILENAME_RETURNBOX);
        elements.add(PFManager.FILENAME_DROPBOX);

        for (String element : elements) {
            String normalizedElement =
                    element
                            .replaceAll(".+?/", "")
                            .replace("/", "")
                            .replace(PFManager.FILENAME_RETURNBOX, translate(PFCourseNode.FOLDER_RETURN_BOX))
                            .replace(PFManager.FILENAME_DROPBOX, translate(PFCourseNode.FOLDER_DROP_BOX));
            PFFolderTemplateRow row = forgeRow(normalizedElement, element);

            initBoxes.add(row);

            if (element.contains("/")) {
                row.setPath(element.substring(0, element.lastIndexOf("/")));
                keyToRows.put(element, row);
            } else {
                row.setPath(element);
                keyToRows.put(element, row);
            }
        }

        for (PFFolderTemplateRow row : initBoxes) {
            if (keyToRows.get(row.getPath()) != null) {
                if (!row.getFolderName().equals(keyToRows.get(row.getPath()).getFolderName())) {
                    row.setParent(keyToRows.get(row.getPath()));
                    keyToRows.get(row.getPath()).setNumOfChildren(keyToRows.get(row.getPath()).getNumberOfChildren() + 1);
                }
            }
        }

        initBoxes.sort(new FolderTemplateTreeNodeComparator());

        tableDataModel.setObjects(initBoxes);
        tableEl.reset(resetPage, resetInternal, true);
    }

    private void saveTemplateStructure(UserRequest ureq, String path, String newFolderName) {
        ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();

        String newPath = path + "/" + newFolderName;
        String updatedModuleConfig = "";

        // add new folder to config with respective path
        if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null) {
            if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString().equals("")) {
                updatedModuleConfig = moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString().concat(newPath);
            } else {
                updatedModuleConfig = moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString().concat("," + newPath);
            }
        }

        moduleConfiguration.setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, updatedModuleConfig);

        // Persists config
        fireEvent(ureq, Event.CHANGED_EVENT);
    }

    private void doCreateSubFolder(UserRequest ureq, String folderPath) {
        createFolderTemplateCtrl = new PFCreateFolderTemplateController(ureq, getWindowControl(), folderPath);
        listenTo(createFolderTemplateCtrl);

        cmc = new CloseableModalController(getWindowControl(), "close", createFolderTemplateCtrl.getInitialComponent(),
                true, translate(TEMPLATE_CREATE_SUBFOLDER));
        listenTo(cmc);
        cmc.activate();
    }

    private void doOpenTools(UserRequest ureq, FormLink link) {
        toolsCtrl = new ToolsController(ureq, getWindowControl(), link.getName().replaceAll(".+?_", ""));
        listenTo(toolsCtrl);

        toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
                toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
        listenTo(toolsCalloutCtrl);
        toolsCalloutCtrl.activate();
    }

    private void doConfirmDelete(UserRequest ureq, String folder) {
        folderToDelete = folder;
        folder = folder
                .replaceAll(PFManager.FILENAME_RETURNBOX, translate(PFCourseNode.FOLDER_RETURN_BOX))
                .replaceAll(PFManager.FILENAME_DROPBOX, translate(PFCourseNode.FOLDER_DROP_BOX));

        List<String> buttons = new ArrayList<>();
        buttons.add(translate("delete"));
        buttons.add(translate("cancel"));

        deleteDialogCtrl = activateGenericDialog(ureq, translate("table.elementDeleteFolder"), translate("confirmation.delete.element.title", folder), buttons , deleteDialogCtrl);
    }

    private void doDelete() {
        ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();
        List<String> folderElements = new ArrayList<>();

        if (!moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).equals("")) {
            folderElements = new ArrayList<>(Arrays.asList(moduleConfiguration
                    .get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString()
                    .split(",")));
        }
        if (!folderElements.isEmpty()) {
            folderElements.removeIf(el -> el.contains(folderToDelete));
        }

        String updatedElements = folderElements.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        moduleConfiguration.setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, updatedElements);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // No need
    }

    private static class FolderTemplateTreeNodeComparator extends FlexiTreeNodeComparator {

        @Override
        protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
            PFFolderTemplateRow r1 = (PFFolderTemplateRow) o1;
            PFFolderTemplateRow r2 = (PFFolderTemplateRow) o2;

            int c;
            if (r1 == null || r2 == null) {
                c = compareNullObjects(r1, r2);
            } else {
                String c1 = r1.getFolderName();
                String c2 = r2.getFolderName();
                if (c1 == null || c2 == null) {
                    c = -compareNullObjects(c1, c2);
                } else {
                    c = c1.compareTo(c2);
                }
            }
            return c;
        }
    }

    private class ToolsController extends BasicController {

        private final VelocityContainer mainVC;
        private final Link deleteLink;
        private final String folderToDelete;

        public ToolsController(UserRequest ureq, WindowControl wControl, String folderToDelete) {
            super(ureq, wControl);
            this.folderToDelete = folderToDelete;

            mainVC = createVelocityContainer(TEMPLATE_TOOLS_LINK);

            List<String> links = new ArrayList<>(2);

            deleteLink = addLink("delete", "o_icon_delete_item", links);
            mainVC.contextPut("links", links);

            putInitialPanel(mainVC);
        }

        private Link addLink(String name, String iconCss, List<String> links) {
            Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
            mainVC.put(name, link);
            links.add(name);
            link.setIconLeftCSS("o_icon " + iconCss);
            return link;
        }

        @Override
        protected void event(UserRequest ureq, Component source, Event event) {
            if (deleteLink == source) {
                close();
                doConfirmDelete(ureq, folderToDelete);
            }
        }

        private void close() {
            toolsCalloutCtrl.deactivate();
            cleanUp();
        }
    }


}
