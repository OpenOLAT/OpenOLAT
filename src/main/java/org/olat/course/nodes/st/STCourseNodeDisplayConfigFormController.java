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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.ArrayHelper;
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

	private static final String DISPLAY_OVERVIEW = "system";
	
	private SingleSelection displayTypeEl;
	private SingleSelection childrenFilterEl;
	private MultipleSelectionElement childrenEl;
	private SingleSelection peekviewFilterEl;
	private MultipleSelectionElement displayTwoColumns;

	private String[] childrenKeys;
	private String[] childrenValues;
	private String[] childrenCssClasses;

	private final ModuleConfiguration config;

	STCourseNodeDisplayConfigFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, CourseEditorTreeNode node) {
		super(ureq, wControl);
		this.config = config;
		
		int childCount = node.getChildCount();
		childrenKeys = new String[childCount];
		childrenValues = new String[childCount];
		childrenCssClasses = new String[childCount];
		for (int i = 0; i < childCount; i++) {
			CourseEditorTreeNode child = node.getCourseEditorTreeNodeChildAt(i);
			childrenKeys[i] = child.getIdent();
			childrenValues[i] = child.getTitle() + " (" + child.getIdent() + ")";
			childrenCssClasses[i] = "o_icon " + child.getIconCssClass();
		}
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.fieldset.view");
		setFormContextHelp("Knowledge Transfer#_struktur");
		formLayout.setElementCssClass("o_sel_st_overview_settings");
	
		// Display type
		SelectionValues typesKV = new SelectionValues();
		typesKV.add(entry(DISPLAY_OVERVIEW, translate("form.system")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW, translate("form.peekview")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE, translate("form.self")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE, translate("form.delegate")));
		displayTypeEl = uifactory.addRadiosVertical("selforsystemoverview", formLayout, typesKV.keys(), typesKV.values());
		displayTypeEl.addActionListener(FormEvent.ONCLICK);
		displayTypeEl.setElementCssClass("o_sel_st_display_type");
		String displayConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
		if (displayTypeEl.containsKey(displayConfig)) {
			displayTypeEl.select(displayConfig, true);
		} else {
			displayTypeEl.select(DISPLAY_OVERVIEW, true);
		}
		
		// Children filter
		SelectionValues childrenFilterKV = new SelectionValues();
		childrenFilterKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL, translate("form.children.filter.all")));
		childrenFilterKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_CHILDREN_SELECTION, translate("form.children.filter.selection")));
		childrenFilterEl = uifactory.addRadiosHorizontal("form.children.filter", formLayout, childrenFilterKV.keys(), childrenFilterKV.values());
		childrenFilterEl.addActionListener(FormEvent.ONCLICK);
		String cildrenFilter = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_FILTER, STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL);
		if (childrenFilterEl.containsKey(cildrenFilter)) {
			childrenFilterEl.select(cildrenFilter, true);
		} else {
			childrenFilterEl.select(STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL, true);
		}
		
		// Selected children
		childrenEl = uifactory.addCheckboxesVertical("form.children.selection", formLayout,
				childrenKeys, childrenValues, childrenCssClasses, 1);
		childrenEl.addActionListener(FormEvent.ONCLICK);
		
		// Peekview filter
		SelectionValues peekviewFilterKV = new SelectionValues();
		peekviewFilterKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL, translate("form.peekview.filter.all")));
		peekviewFilterKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_STRUCTURES, translate("form.peekview.filter.structures")));
		peekviewFilterEl = uifactory.addRadiosHorizontal("form.peekview.filter", formLayout, peekviewFilterKV.keys(), peekviewFilterKV.values());
		peekviewFilterEl.addActionListener(FormEvent.ONCLICK);
		String peekviewFilter = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_FILTER, STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL);
		if (peekviewFilterEl.containsKey(peekviewFilter)) {
			peekviewFilterEl.select(peekviewFilter, true);
		} else {
			peekviewFilterEl.select(STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL, true);
		}
	
		// Number of rows
		displayTwoColumns = uifactory.addCheckboxesHorizontal("displayTwoColumns", formLayout, new String[] { "on" }, new String[] { "" });
		displayTwoColumns.setLabel("displayTwoColumns", null);
		displayTwoColumns.addActionListener(FormEvent.ONCLICK);

	}
	
	private void updateUI() {
		String selectedKey = displayTypeEl.getSelectedKey();
		peekviewFilterEl.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey));
		childrenFilterEl.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey) || DISPLAY_OVERVIEW.equals(selectedKey));
		
		String childrenIdentsConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_IDENTS, "");
		String[] childrenIdents = childrenIdentsConfig == null ? ArrayHelper.emptyStrings() : childrenIdentsConfig.split(",");
		for (String childrenIdent : childrenIdents) {
			for (String childrenKey : childrenKeys) {
				if (childrenIdent.equals(childrenKey)) {
					childrenEl.select(childrenKey, true);
				}
			}
		}
		childrenEl.setVisible(childrenFilterEl.isVisible() && childrenFilterEl.isOneSelected() 
				&& childrenFilterEl.getSelectedKey().equals(STCourseNodeEditController.CONFIG_VALUE_CHILDREN_SELECTION));
		
		int columnsConfig = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
		if (columnsConfig == 2) {
			displayTwoColumns.select("on", true);
		}
		displayTwoColumns.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey) || DISPLAY_OVERVIEW.equals(selectedKey));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if(displayTypeEl == source) {
			updateUI();
		} else if (source == childrenFilterEl) {
			updateUI();
		} else if (source == childrenEl) {
			childrenEl.clearError();
			if (childrenEl.getSelectedKeys().isEmpty()) {
				// There must be at least one selected child
				childrenEl.setErrorKey("form.peekview.error.mandatory.child", null);
				return; // abort
			}
		}
		// Submit form on each click on any radio or checkbox
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public void updateModuleConfiguration(ModuleConfiguration moduleConfig) {
		String displayType = displayTypeEl.getSelectedKey();
		if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(displayType)) {
			moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE);
		} else {
			if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(displayType)) {
				moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
			} else if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE.equals(displayType)) {
				moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE);
			} else {
				// the old auto generated TOC view without peekview
				moduleConfig
						.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
				// Let other config values from old config setup remain in config, maybe
				// used when user switches back to other config (OLAT-5610)
			}
		}
		
		if (childrenFilterEl.isVisible()) {
			String childrenFilter = childrenFilterEl.isOneSelected()
					? childrenFilterEl.getSelectedKey()
					: STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL;
			moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_FILTER, childrenFilter);
		}
		
		if (childrenEl.isVisible()) {
			if (childrenEl.isAtLeastSelected(1)) {
				String selectedChildren = childrenEl.getSelectedKeys().stream().collect(Collectors.joining(","));
				moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_IDENTS, selectedChildren);
			} else {
				moduleConfig.remove(STCourseNodeEditController.CONFIG_KEY_CHILDREN_IDENTS);
			}
		}
		
		if (peekviewFilterEl.isVisible()) {
			String peekviewFilter = peekviewFilterEl.isOneSelected()
					? peekviewFilterEl.getSelectedKey()
							: STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL;
			moduleConfig.setStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_FILTER, peekviewFilter);
		}
		
		if (displayTwoColumns.isVisible()) {
			int columnsConfig = displayTwoColumns.isAtLeastSelected(1) ? 2 : 1;
			moduleConfig.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, columnsConfig);
		}
	}
}
