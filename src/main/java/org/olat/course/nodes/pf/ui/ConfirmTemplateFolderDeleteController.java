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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.modules.ModuleConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class ConfirmTemplateFolderDeleteController extends FormBasicController {

    private final String translatedFolderElement;
    private final String folderToDelete;
    private final PFCourseNode pfNode;

    public ConfirmTemplateFolderDeleteController(UserRequest ureq, WindowControl wControl, String folderToDelete, PFCourseNode pfNode) {
        super(ureq, wControl, LAYOUT_HORIZONTAL);
        this.pfNode = pfNode;
        this.folderToDelete = folderToDelete;
        this.translatedFolderElement =
                folderToDelete
                        .replaceAll(PFManager.FILENAME_RETURNBOX, translate(PFCourseNode.FOLDER_RETURN_BOX))
                        .replaceAll(PFManager.FILENAME_DROPBOX, translate(PFCourseNode.FOLDER_DROP_BOX));

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        if (formLayout instanceof FormLayoutContainer) {
            ((FormLayoutContainer) formLayout).contextPut("element", folderToDelete);
        }
        uifactory.addStaticTextElement("", translate("confirmation.delete.element.title", translatedFolderElement), formLayout);

        FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttons);
        uifactory.addFormSubmitButton("delete", buttons);
        uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
    }

    @Override
    protected void formOK(UserRequest ureq) {
        doDelete();
        fireEvent(ureq, Event.DONE_EVENT);
    }

    private void doDelete() {
        ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();
        List<String> elements = new ArrayList<>();

        if (!moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).equals("")) {
            elements = new ArrayList<>(Arrays.asList(moduleConfiguration
                    .get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString()
                    .split(",")));
        }
        if (!elements.isEmpty()) {
            elements.removeIf(el -> el.contains(folderToDelete));
        }

        String updatedElements = elements.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        moduleConfiguration.setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, updatedElements);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

}
