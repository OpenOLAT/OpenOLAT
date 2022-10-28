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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.MultiSelectionController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.MultiSelectionController.KeysSelectedEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiSelectionFilterElementImpl  extends FormItemImpl implements MultiSelectionFilterElement, FormItemCollection, ControllerEventListener {

	private final MultiSelectionFilterComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>(1);
	private final FormLink button;
	
	private CloseableCalloutWindowController calloutCtrl;
	private MultiSelectionController selectionCtrl;
	
	private final SelectionValues availableValues;
	private Set<String> selectedKeys = new HashSet<>(3);
	
	public MultiSelectionFilterElementImpl(WindowControl wControl, String name, SelectionValues availableValues) {
		super(name);
		this.wControl = wControl;
		this.availableValues = availableValues;
		this.component = new MultiSelectionFilterComponent(name, this);
		
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_msf";
		button = new FormLinkImpl(id, id, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_msf_button");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(id, button);
		rootFormAvailable(button);
	}

	@Override
	public void select(String key, boolean select) {
		if (select) {
			if (availableValues.containsKey(key)) {
				selectedKeys.add(key);
			}
		} else {
			selectedKeys.remove(key);
		}
		updateButtonUI();
	}
	
	@Override
	public Set<String> getKeys() {
		return Set.of(availableValues.keys());
	}

	@Override
	public Set<String> getSelectedKeys() {
		return selectedKeys;
	}

	public FormLink getButton() {
		return button;
	}
	
	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(button);
	}
	
	private final void rootFormAvailable(FormItem item) {
		if (item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}

	@Override
	public void reset() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if (button != null && button.getFormDispatchId().equals(dispatchuri)) {
			doOpenSelection(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (selectionCtrl == source) {
			if (event instanceof KeysSelectedEvent) {
				KeysSelectedEvent se = (KeysSelectedEvent)event;
				selectedKeys = se.getSelectedKeys();
				if (selectedKeys == null) {
					selectedKeys = new HashSet<>(3);
				}
				calloutCtrl.deactivate();
				cleanUp();
				updateButtonUI();
				getFormItemComponent().setDirty(true);
				Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
				wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
				if (FormEvent.ONCHANGE == getAction()) {
					getRootForm().fireFormEvent(ureq, new FormEvent("selected", this, FormEvent.ONCHANGE));
				}
			} else if(event == Event.CANCELLED_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(calloutCtrl == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		calloutCtrl = cleanUp(calloutCtrl);
		selectionCtrl = cleanUp(selectionCtrl);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	private void updateButtonUI() {
		StringBuilder sb = new StringBuilder();
		boolean append = false;
		for (SelectionValue selectionValue : availableValues.keyValues()) {
			if (selectedKeys.contains(selectionValue.getKey())) {
				if (append) {
					sb.append(", ");
				}
				String value = StringHelper.containsNonWhitespace(selectionValue.getDescription())
						? selectionValue.getDescription()
						: selectionValue.getValue();
				sb.append(value);
				append = true;
			}
		}
		button.setI18nKey(sb.toString());
	}
	
	private void doOpenSelection(UserRequest ureq) {
		selectionCtrl = new MultiSelectionController(ureq, wControl, availableValues, selectedKeys);
		selectionCtrl.addControllerListener(this);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, selectionCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false, CalloutOrientation.bottom, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}
	
}
