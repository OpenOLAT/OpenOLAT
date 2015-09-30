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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
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
	private static final String[] keys_displayType = new String[] { "system", "peekview", "file", "delegate" };

	// read current configuration
	private String displayConfig = null;
	private int columnsConfig = 1;
	private SingleSelection displayTypeRadios;
	private MultipleSelectionElement displayTwoColumns;
	private MultipleSelectionElement selectedPeekviewChildren;
	private String[] selectedPeekviewChildKeys;
	private String[] selectedPeekviewChildValues;
	private String[] selectedPeekviewChildCssClasses;
	private String selectedPeekviewChildNodesConfig = null;

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
		this.displayConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
				STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
		this.columnsConfig = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
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
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// No explicit submit button. Form is submitted every time when a radio or
		// checkbox is clicked (OLAT-5610)
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.fieldset.view");
		setFormContextHelp("Knowledge Transfer#_struktur");
		FormUIFactory formFact = FormUIFactory.getInstance();
		// Display type
		String[] values_displayType = new String[] { translate("form.system"), translate("form.peekview"), translate("form.self"),
				translate("form.delegate") };
		displayTypeRadios = formFact.addRadiosVertical("selforsystemoverview", formLayout, keys_displayType, values_displayType);
		displayTypeRadios.addActionListener(FormEvent.ONCLICK);
		if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
			displayTypeRadios.select("file", true);
		} else if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW)) {
			displayTypeRadios.select("peekview", true);
		} else if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE)) {
			displayTypeRadios.select("delegate", true);
		} else {
			displayTypeRadios.select("system", true);
		}
		// Peekview details configuration - allow only MAX_PEEKVIEW_CHILD_NODES
		// peekviews to be selected
		if (selectedPeekviewChildKeys.length > 0) {
			SpacerElement spacerChild = formFact.addSpacerElement("spacerChild", formLayout, true);
			selectedPeekviewChildren = formFact.addCheckboxesVertical("selectedPeekviewChildren", formLayout, selectedPeekviewChildKeys,
					selectedPeekviewChildValues, selectedPeekviewChildCssClasses, 1);
			selectedPeekviewChildren.setLabel("selectedPeekviewChildren",
					new String[] { STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES + "" });
			// visibility rules for peekview children selection
			RulesFactory.createHideRule(displayTypeRadios, "file", selectedPeekviewChildren, formLayout);
			RulesFactory.createHideRule(displayTypeRadios, "system", selectedPeekviewChildren, formLayout);
			RulesFactory.createShowRule(displayTypeRadios, "peekview", selectedPeekviewChildren, formLayout);
			RulesFactory.createHideRule(displayTypeRadios, "delegate", selectedPeekviewChildren, formLayout);
			RulesFactory.createHideRule(displayTypeRadios, "file", spacerChild, formLayout);
			RulesFactory.createHideRule(displayTypeRadios, "system", spacerChild, formLayout);
			RulesFactory.createShowRule(displayTypeRadios, "peekview", spacerChild, formLayout);
			RulesFactory.createHideRule(displayTypeRadios, "delegate", spacerChild, formLayout);
			// Pre-select the first MAX_PEEKVIEW_CHILD_NODES child nodes if none is
			// selected to reflect meaningfull default configuration
			preselectConfiguredOrMaxChildNodes();
			// Add as listener for any changes
			selectedPeekviewChildren.addActionListener(FormEvent.ONCLICK);
		}
		//
		// Number of rows (only available in system or peekview type)
		SpacerElement spacerCols = formFact.addSpacerElement("spacerCols", formLayout, true);
		displayTwoColumns = formFact
				.addCheckboxesHorizontal("displayTwoColumns", formLayout, new String[] { "on" }, new String[] { "" });
		displayTwoColumns.setLabel("displayTwoColumns", null);
		displayTwoColumns.addActionListener(FormEvent.ONCLICK);
		if (columnsConfig == 2) {
			displayTwoColumns.selectAll();
		}
		if (displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE) || displayConfig.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE)) {
			displayTwoColumns.setVisible(false);
		}
		// 
		// Visibility rules for display columns switch
		RulesFactory.createHideRule(displayTypeRadios, "file", displayTwoColumns, formLayout);
		RulesFactory.createShowRule(displayTypeRadios, "peekview", displayTwoColumns, formLayout);
		RulesFactory.createShowRule(displayTypeRadios, "system", displayTwoColumns, formLayout);
		RulesFactory.createHideRule(displayTypeRadios, "delegate", displayTwoColumns, formLayout);
		RulesFactory.createHideRule(displayTypeRadios, "file", spacerCols, formLayout);
		RulesFactory.createShowRule(displayTypeRadios, "peekview", spacerCols, formLayout);
		RulesFactory.createShowRule(displayTypeRadios, "system", spacerCols, formLayout);
		RulesFactory.createHideRule(displayTypeRadios, "delegate", spacerCols, formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == selectedPeekviewChildren) {
			if (selectedPeekviewChildren.getSelectedKeys().size() == 0) {
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
		} else {
			// Fix problem of not-preselected items (OLAT-5610). The initial status
			// from the initForm method gets lost by the re-evaluation of the
			// selection element. Seems to be a flexi form bug, could not find other
			// solution as this workaround.
			preselectConfiguredOrMaxChildNodes();
		}
		// Submit form on each click on any radio or checkbox (OLAT-5610)
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
			if (selectedPeekviewChildren.getSelectedKeys().size() == 0) {
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
				if (selectedPeekviewChildren == null || selectedPeekviewChildren.getSelectedKeys().size() == 0) {
					moduleConfig.remove(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES);
				} else {
					selectedPeekviewChildNodesConfig = "";
					for (String childKey : selectedPeekviewChildren.getSelectedKeys()) {
						if (selectedPeekviewChildNodesConfig.length() != 0) {
							// separate node id's with commas
							selectedPeekviewChildNodesConfig += ",";
						}
						selectedPeekviewChildNodesConfig += childKey;
					}
					moduleConfig.set(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, selectedPeekviewChildNodesConfig);
				}
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
			// in both cases, also set the columns configuration
			int cols = (displayTwoColumns.isSelected(0) ? 2 : 1);
			moduleConfig.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, Integer.valueOf(cols));
			// Let other config values from old config setup remain in config, maybe
			// used when user switches back to other config (OLAT-5610)
		}
	}

}
