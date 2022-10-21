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
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.modules.ModuleConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class PFCreateFolderTemplateController extends FormBasicController {

    private final String translatedFolderElement;
    private final String folderElement;
    private final PFCourseNode pfNode;
    private TextElement subFolderNameEl;
    private StaticTextElement folderEl;


    public PFCreateFolderTemplateController(UserRequest ureq, WindowControl wControl, String folderElement, PFCourseNode pfNode) {
        super(ureq, wControl, LAYOUT_DEFAULT);
        this.folderElement = folderElement;
        this.translatedFolderElement =
                folderElement
                        .replaceAll(PFManager.FILENAME_RETURNBOX, translate(PFCourseNode.FOLDER_RETURN_BOX))
                        .replaceAll(PFManager.FILENAME_DROPBOX, translate(PFCourseNode.FOLDER_DROP_BOX));
        this.pfNode = pfNode;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout,
                            Controller listener,
                            UserRequest ureq) {
        folderEl = uifactory.addStaticTextElement("level.folder", translatedFolderElement + "/", formLayout);
        folderEl.setEnabled(false);

        subFolderNameEl = uifactory.addTextElement(folderElement, "level.subFolderName", 255, "", formLayout);
        subFolderNameEl.setMandatory(true);

        FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttons);
        uifactory.addFormSubmitButton("level.save", buttons);
        uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();
        List<String> elements = new ArrayList<>();

        if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null) {
            elements = new ArrayList<>(Arrays.asList(moduleConfiguration
                    .get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString()
                    .split(",")));
            elements.removeIf(String::isBlank);
        }


        boolean isInputValid = super.validateFormLogic(ureq);
        String name = subFolderNameEl.getValue();

        if (name == null || name.trim().equals("")) {
            subFolderNameEl.setErrorKey("error.sf.empty", new String[0]);
            isInputValid = false;
        } else if (!validateFolderName(subFolderNameEl.getValue())) {
            subFolderNameEl.setErrorKey("error.sf.invalid", null);
            isInputValid = false;
        } else if (elements.stream().anyMatch(l -> l.equals(folderElement + "/" + subFolderNameEl.getValue()))) {
            subFolderNameEl.setErrorKey("error.sf.exists", new String[]{subFolderNameEl.getValue()});
            isInputValid = false;
        } else if (subFolderNameEl.getValue().equals(translate(PFCourseNode.FOLDER_DROP_BOX))
                || subFolderNameEl.getValue().equals(translate(PFCourseNode.FOLDER_RETURN_BOX))) {
            subFolderNameEl.setErrorKey("error.sf.root", new String[]{subFolderNameEl.getValue()});
            isInputValid = false;
        }

        return isInputValid;
    }

    private boolean validateFolderName(String name) {
        return FileUtils.validateFilename(name);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (validateFormLogic(ureq)) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    public String getFolderElement() {
        return folderElement;
    }

    public TextElement getSubFolderNameEl() {
        return subFolderNameEl;
    }
}
