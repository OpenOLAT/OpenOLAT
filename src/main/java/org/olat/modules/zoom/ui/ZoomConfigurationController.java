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

import java.util.List;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tab page controller for the "Configuration" tab in the Zoom administration.
 * Contains the top-level module settings for the Zoom integration.
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 *
 */
public class ZoomConfigurationController extends FormBasicController implements Activateable2 {

    private final SelectionValues moduleEnabledKV = new SelectionValues();
    private final SelectionValues enableForKV = new SelectionValues();
    private final SelectionValues enableCalendarEntriesKV = new SelectionValues();

    private MultipleSelectionElement moduleEnabledEl;
	private MultipleSelectionElement enableForEl;
    private MultipleSelectionElement enableCalendarEntriesEl;

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
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
    	formLayout.setElementCssClass("o_sel_zoom_admin_configuration");
        setFormTitle("zoom.configuration.integration.title");
        setFormInfo("zoom.info");
        setFormContextHelp("manual_admin/administration/Zoom/");

        moduleEnabledEl = uifactory.addCheckboxesHorizontal("zoom.module.enabled", formLayout, moduleEnabledKV.keys(), moduleEnabledKV.values());
        moduleEnabledEl.setElementCssClass("o_sel_zoom_admin_enable");
        moduleEnabledEl.select(moduleEnabledKV.keys()[0], zoomModule.isEnabled());
        moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);

        enableForEl = uifactory.addCheckboxesVertical("zoom.module.enable.for", formLayout, enableForKV.keys(), enableForKV.values(), 1);
        enableForEl.setElementCssClass("o_sel_zoom_admin_enabled_for");
        enableForEl.select(enableForKV.keys()[0], zoomModule.isEnabledForCourseElement());
        enableForEl.select(enableForKV.keys()[1], zoomModule.isEnabledForCourseTool());
        enableForEl.select(enableForKV.keys()[2], zoomModule.isEnabledForGroupTool());
        enableForEl.addActionListener(FormEvent.ONCHANGE);

        enableCalendarEntriesEl = uifactory.addCheckboxesHorizontal("zoom.module.zoomCanSetCalendarEntries", formLayout, enableCalendarEntriesKV.keys(), enableCalendarEntriesKV.values());
        enableCalendarEntriesEl.select(enableCalendarEntriesKV.keys()[0], zoomModule.isCalendarEntriesEnabled());
        enableCalendarEntriesEl.addActionListener(FormEvent.ONCHANGE);

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
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
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == moduleEnabledEl) {
            updateUI();
        }
        super.formInnerEvent(ureq, source, event);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        //
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
    }
}