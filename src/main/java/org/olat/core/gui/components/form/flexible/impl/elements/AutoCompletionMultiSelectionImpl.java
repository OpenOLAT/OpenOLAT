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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.AutoCompletionController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.AutoCompletionController.AutoCompletionSelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;

/**
 * 
 * Initial date: 5 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AutoCompletionMultiSelectionImpl extends FormItemImpl implements AutoCompletionMultiSelection, FormItemCollection, ControllerEventListener {
	
	private final WindowControl wControl;
	private final AutoCompletionMultiSelectionComponent component;
	private final Map<String, FormItem> components = new HashMap<>(1);
	private final FormLink button;
	
	private CloseableCalloutWindowController autoCompletionCallout;
	private AutoCompletionController autoCompletionrCtrl;
	
	private final AutoCompletionSource source;
	private Comparator<SelectionValue> selectionComporator;
	private String searchPlaceholder;
	private SelectionValues selection;

	public AutoCompletionMultiSelectionImpl(WindowControl wControl, String name, AutoCompletionSource source) {
		super(name);
		this.wControl = wControl;
		this.source = source;
		this.component = new AutoCompletionMultiSelectionComponent(name, this);
		this.selectionComporator = SelectionValues.VALUE_ASC;
		
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_acms";
		button = new FormLinkImpl(id, id, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_acms_button");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(id, button);
		rootFormAvailable(button);
	}
	
	@Override
	public void setSelectionComporator(Comparator<SelectionValue> selectionComporator) {
		this.selectionComporator = selectionComporator;
	}
	
	@Override
	public void setSearchPlaceholder(String placeholder) {
		this.searchPlaceholder = placeholder;
	}
	
	@Override
	public SelectionValues getSelection() {
		if (selection == null) {
			selection = new SelectionValues();
		}
		return selection;
	}
	
	@Override
	public int getSelectedKeysSize() {
		return getSelection().size();
	}

	@Override
	public Collection<String> getSelectedKeys() {
		return getSelection().keyValues().stream().map(SelectionValue::getKey).collect(Collectors.toSet());
	}
	
	@Override
	public void setSelectedKeys(Collection<String> keys) {
		if (keys != null && !keys.isEmpty()) {
			selection = source.getSelectionValues(keys);
		} else {
			selection = null;
		}
		updateButtonUI();
	}
	
	private void updateButtonUI() {
		if (selectionComporator != null) {
			getSelection().sort(selectionComporator);
		}
		String linkTitle = getSelection().keyValues().stream()
				.map(SelectionValue::getValue)
				.distinct()
				.collect(Collectors.joining(", "));
		button.setI18nKey(linkTitle);
	}

	@Override
	public void reset() {
		//
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
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	public FormLink getButton() {
		return button;
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(button != null && button.getFormDispatchId().equals(dispatchuri)) {
			doOpen(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(autoCompletionrCtrl == source) {
			if(event instanceof AutoCompletionSelectionEvent) {
				AutoCompletionSelectionEvent se = (AutoCompletionSelectionEvent)event;
				selection = se.getSelection();
				autoCompletionCallout.deactivate();
				cleanUp();
				updateButtonUI();
				getRootForm().fireFormEvent(ureq, new FlexiAutoCompletionSelectionEvent(this, selection));
			} else if(event == Event.CANCELLED_EVENT) {
				autoCompletionCallout.deactivate();
				cleanUp();
			}
		} else if(autoCompletionCallout == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		autoCompletionCallout = cleanUp(autoCompletionCallout);
		autoCompletionrCtrl = cleanUp(autoCompletionrCtrl);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if(ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	private void doOpen(UserRequest ureq) {
		autoCompletionrCtrl = new AutoCompletionController(ureq, wControl, searchPlaceholder, source, getSelection());
		autoCompletionrCtrl.addControllerListener(this);

		autoCompletionCallout = new CloseableCalloutWindowController(ureq, wControl, autoCompletionrCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false));
		autoCompletionCallout.addControllerListener(this);
		autoCompletionCallout.activate();
	}

}
