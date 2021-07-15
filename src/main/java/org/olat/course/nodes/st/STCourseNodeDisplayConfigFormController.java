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
package org.olat.course.nodes.st;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;

/**
 * <h3>Description:</h3> The STCourseNodeDisplayConfigFormController displays
 * the layout configuration form for an ST node. It lets the user decide if he
 * wants to display a custom file layout, a system generated TOC layout or a
 * system generated peekview layout. In both system generated layouts he can
 * further define the number of columns (1 or 2).
 * <p>
 * When the peek view configuration is used, the children that should be
 * displayed in the peekview must be selected manually. For performance reasons
 * only 10 direct children can be selected by default. This behavior can be
 * overridden by using the spring setter method setMaxPeekviewChildNodes() on
 * the org.olat.course.nodes.st.STCourseNodeConfiguration bean
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>Event.DONE_EVENT when the form is submitted</li>
 * </ul>
 * <p>
 * Initial Date: 15.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class STCourseNodeDisplayConfigFormController extends FormBasicController {

	private static final String DISPLAY_SYSTEM = "system";
	
	private SpacerElement spacerCols;
	private SingleSelection displayTypeRadios;
	private MultipleSelectionElement displayTwoColumns;
	private MultipleSelectionElement selectedPeekviewChildren;

	private String[] selectedPeekviewChildKeys;
	private String[] selectedPeekviewChildValues;
	private String[] selectedPeekviewChildCssClasses;
	
	// read current configuration
	private int columnsConfig = 1;
	private String displayConfig;
	private String selectedPeekviewChildNodesConfig;

	/**
	 * Constructor for the config form
	 * 
	 * @param ureq
	 * @param wControl
	 * @param config the module configuration
	 * @param node the course editor node
	 */
	STCourseNodeDisplayConfigFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, CourseEditorTreeNode node) {
		super(ureq, wControl);
		// Read current configuration
		displayConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
				STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
		columnsConfig = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
		// Build data model for the selected child peekview checkboxes
		int childCount = node.getChildCount();
		selectedPeekviewChildKeys = new String[childCount];
		selectedPeekviewChildValues = new String[childCount];
		selectedPeekviewChildCssClasses = new String[childCount];
		for (int i = 0; i < childCount; i++) {
			CourseEditorTreeNode child = node.getCourseEditorTreeNodeChildAt(i);
			selectedPeekviewChildKeys[i] = child.getIdent();
			selectedPeekviewChildValues[i] = child.getTitle() + " (" + child.getIdent() + ")";
			selectedPeekviewChildCssClasses[i] = "o_icon " + child.getIconCssClass();
		}
		selectedPeekviewChildNodesConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, "");
		// initialize the form now
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// No explicit submit button. Form is submitted every time when a radio or
		// checkbox is clicked (OLAT-5610)
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.fieldset.view");
		setFormContextHelp("Knowledge Transfer#_struktur");
		formLayout.setElementCssClass("o_sel_st_overview_settings");
	
		// Display type
		SelectionValues typesKV = new SelectionValues();
		typesKV.add(entry(DISPLAY_SYSTEM, translate("form.system")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW, translate("form.peekview")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_STRUCTURES, translate("form.structures")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE, translate("form.self")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE, translate("form.delegate")));
		displayTypeRadios = uifactory.addRadiosVertical("selforsystemoverview", formLayout, typesKV.keys(), typesKV.values());
		displayTypeRadios.addActionListener(FormEvent.ONCLICK);
		displayTypeRadios.setElementCssClass("o_sel_st_display_type");
		if (displayTypeRadios.containsKey(displayConfig)) {
			displayTypeRadios.select(displayConfig, true);
		} else {
			displayTypeRadios.select(DISPLAY_SYSTEM, true);
		}
		// Peekview details configuration - allow only MAX_PEEKVIEW_CHILD_NODES
		// peekviews to be selected
		if (selectedPeekviewChildKeys.length > 0) {
			selectedPeekviewChildren = uifactory.addCheckboxesVertical("selectedPeekviewChildren", formLayout,
					selectedPeekviewChildKeys, selectedPeekviewChildValues, selectedPeekviewChildCssClasses, 1);
			selectedPeekviewChildren.setLabel("selectedPeekviewChildren",
					new String[] { STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES + "" });
			
			// Pre-select the first MAX_PEEKVIEW_CHILD_NODES child nodes if none is
			// selected to reflect meaningfull default configuration
			preselectConfiguredOrMaxChildNodes();
			// Add as listener for any changes
			selectedPeekviewChildren.addActionListener(FormEvent.ONCLICK);
		}
	
		// Number of rows (only available in system or peekview type)
		spacerCols = uifactory.addSpacerElement("spacerCols", formLayout, true);
		displayTwoColumns = uifactory.addCheckboxesHorizontal("displayTwoColumns", formLayout, new String[] { "on" }, new String[] { "" });
		displayTwoColumns.setLabel("displayTwoColumns", null);
		displayTwoColumns.addActionListener(FormEvent.ONCLICK);
	}
	
	private void updateUI() {
		String selectedKey = displayTypeRadios.getSelectedKey();
		displayTwoColumns.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey) 
				|| STCourseNodeEditController.CONFIG_VALUE_DISPLAY_STRUCTURES.equals(selectedKey) 
				|| DISPLAY_SYSTEM.equals(selectedKey));
		spacerCols.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey) || DISPLAY_SYSTEM.equals(selectedKey));
		if(selectedPeekviewChildren != null) {
			selectedPeekviewChildren.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey));
		}
		if (columnsConfig == 2) {
			displayTwoColumns.select("on", true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if(displayTypeRadios == source) {
			updateUI();
			preselectConfiguredOrMaxChildNodes();
		} else if (source == selectedPeekviewChildren) {
			if (selectedPeekviewChildren.getSelectedKeys().isEmpty()) {
				// There must be at least one selected child
				selectedPeekviewChildren.setErrorKey("form.peekview.error.mandatory.child", null);
				return; // abort
			}
			// Clean potential previous error and continue with rules to
			// enable/disable the checkboxes to ensure that users can't select more
			// than the allowed number of child nodes
			selectedPeekviewChildren.clearError();
			if (selectedPeekviewChildren.getSelectedKeys().size() >= STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
				// Max reached, disabled all not already enabled checkboxes.
				for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
					if (!selectedPeekviewChildren.isSelected(i)) {
						selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
					}
				}
				showInfo("form.peekview.max.reached", STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES + "");
			} else {
				// Enable all checkboxes
				selectedPeekviewChildren.setEnabled(true);
			}
		}
		// Submit form on each click on any radio or checkbox
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * Helper to select the configured child nodes or the maximum of child nodes
	 * when no child is selected at all
	 */
	private void preselectConfiguredOrMaxChildNodes() {
		// Pre-select configured keys. Discard configured selections that are not
		// found (e.g. deleted or moved nodes)
		//
		// SelectedPeekviewChildren can be NULL in case this structure element has
		// no child elements at all, e.g. during development of the course.
		if (selectedPeekviewChildren != null) {
			String[] preSelectedChildNodes = (selectedPeekviewChildNodesConfig == null ? new String[0] : selectedPeekviewChildNodesConfig
					.split(","));
			for (String preSelectedNode : preSelectedChildNodes) {
				for (String selectableNode : selectedPeekviewChildKeys) {
					if (preSelectedNode.equals(selectableNode)) {
						selectedPeekviewChildren.select(selectableNode, true);
						break;
					}
				}
			}
			// Allow only MAX_PEEKVIEW_CHILD_NODES child nodes to be enabled
			if (selectedPeekviewChildren.getSelectedKeys().size() >= STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
				for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
					if (!selectedPeekviewChildren.isSelected(i)) {
						selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
					}
				}
			}
			// Pre-select the first MAX_PEEKVIEW_CHILD_NODES child nodes if none is
			// selected to reflect meaningfull default configuration.
			//
			if (selectedPeekviewChildren.getSelectedKeys().isEmpty()) {
				for (int i = 0; i < selectedPeekviewChildKeys.length; i++) {
					if (i < STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
						selectedPeekviewChildren.select(selectedPeekviewChildKeys[i], true);
					} else {
						selectedPeekviewChildren.setEnabled(selectedPeekviewChildKeys[i], false);
					}
				}
			}
			// remove errors from previous invalid form selection
			selectedPeekviewChildren.clearError();
		}
	}
	
	/**
	 * Update the given module config object from the data in the form
	 * 
	 * @param moduleConfig
	 */
	public void updateModuleConfiguration(ModuleConfiguration moduleConfig) {
		String displayType = displayTypeRadios.getSelectedKey();
		if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(displayType)) {
			// manual file view selected, remove columns config
			moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE);
			// Let other config values from old config setup remain in config, maybe
			// used when user switches back to other config (OLAT-5610)
		} else {
			// auto generated view selected, set TOC or peekview
			if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(displayType)) {
				moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
						STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
				// update selected peekview children
				if (selectedPeekviewChildren == null || selectedPeekviewChildren.getSelectedKeys().isEmpty()) {
					moduleConfig.remove(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES);
				} else {
					selectedPeekviewChildNodesConfig = selectedPeekviewChildren.getSelectedKeys().stream().collect(Collectors.joining(","));
					moduleConfig.set(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, selectedPeekviewChildNodesConfig);
				}
			} else if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_STRUCTURES.equals(displayType)) {
					moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
						STCourseNodeEditController.CONFIG_VALUE_DISPLAY_STRUCTURES);
			} else if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE.equals(displayType)) {
					moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
						STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE);
			} else {
				// the old auto generated TOC view without peekview
				moduleConfig
						.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
				// Let other config values from old config setup remain in config, maybe
				// used when user switches back to other config (OLAT-5610)
			}
		}

		// in both cases, also set the columns configuration
		columnsConfig = displayTwoColumns.isAtLeastSelected(1) ? 2 : 1;
		moduleConfig.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, columnsConfig);
	}
}
