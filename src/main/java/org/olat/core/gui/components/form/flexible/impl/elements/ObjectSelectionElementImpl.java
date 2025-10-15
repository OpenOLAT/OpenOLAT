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
package org.olat.core.gui.components.form.flexible.impl.elements;



import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.expand.ExpandButton;
import org.olat.core.gui.components.expand.ExpandButtonFactory;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Sep 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ObjectSelectionElementImpl extends FormItemImpl implements ObjectSelectionElement, ControllerEventListener {
	
	private Translator elementTranslator;
	private final ExpandButton component;
	private final WindowControl wControl;
	
	private CloseableCalloutWindowController calloutCtrl;
	private ObjectSelectionController selectionCtrl;
	private CloseableModalController cmc;
	private Controller browseCtrl;
	
	private final boolean multiSelection;
	private String noSelectionText;
	private ObjectSelectionSource source;
	private Set<String> selectedKeys;

	public ObjectSelectionElementImpl(WindowControl wControl, String name, boolean multiSelection, ObjectSelectionSource source) {
		super(name);
		this.wControl = wControl;
		this.multiSelection = multiSelection;
		this.source = source;
		component = ExpandButtonFactory.createSelectionDisplay(this);
		component.setAriaHasPopup(ExpandButton.ARIA_HASPOPUP_DIALOG);
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
		elementTranslator = Util.createPackageTranslator(ObjectSelectionElementImpl.class, getTranslator().getLocale());
		
		initDefaults();
	}
	
	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		if (getRootForm().hasAlreadyFired()) {
			return;
		}
		
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if (getFormDispatchId().equals(dispatchuri)) {
			doOpenSelection(ureq);
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (selectionCtrl == source) {
			if(event == ObjectSelectionController.OPEN_BROWSER_EVENT) {
				calloutCtrl.deactivate();
				doOpenBrowser(ureq);
			} else if (event instanceof ObjectSelectionController.SelectionEvent selectionEvent) {
				if (!selectedKeys.equals(selectionEvent.getSelectedKeys())) {
					selectedKeys = new HashSet<>(selectionEvent.getSelectedKeys());
					updateDisplayUI();
					
					Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
					wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
					
					if (getAction() == FormEvent.ONCHANGE) {
						getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
					}
				}
				calloutCtrl.deactivate();
				cleanUpSelection();
			}
		} else if (calloutCtrl == source) {
			cleanUpSelection();
		} else if (source == browseCtrl) {
			if (event instanceof ObjectSelectionBrowserEvent browserEvent) {
				doSelectedInBrowser(browserEvent.getKeys());
			}
			cmc.deactivate();
			cleanUpBrowse();
			calloutCtrl.activate();
		} else if (cmc == source) {
			cleanUpBrowse();
			calloutCtrl.activate();
		}
	}

	private void cleanUpSelection() {
		selectionCtrl = cleanUp(selectionCtrl);
		calloutCtrl = cleanUp(calloutCtrl);
		cleanUpBrowse();
		
		component.setExpanded(false);
		component.setAriaControls(null);
		
		Command focusCommand = FormJSHelper.getFormFocusCommand(getRootForm().getFormName(), getFormDispatchId());
		getRootForm().getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}
	
	private void cleanUpBrowse() {
		browseCtrl = cleanUp(browseCtrl);
		cmc = cleanUp(cmc);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}

	@Override
	public void setNoSelectionText(String noSelectionText) {
		this.noSelectionText = noSelectionText;
		updateDisplayUI();
	}
	
	@Override
	public String getSelectedKey() {
		return selectedKeys.size() == 1? List.copyOf(selectedKeys).get(0): null;
	}

	@Override
	public Set<String> getSelectedKeys() {
		return Set.copyOf(selectedKeys);
	}
	
	@Override
	public void select(String key) {
		if (!multiSelection) {
			selectedKeys.clear();
		}
		
		if (source.getOptions().stream().anyMatch(option -> key.equals(option.getKey()))) {
			selectedKeys.add(key);
		}
		
		updateDisplayUI();
	}
	
	@Override
	public void unselectAll() {
		selectedKeys.clear();
		updateDisplayUI();
	}
	
	@Override
	public void setSource(ObjectSelectionSource source) {
		this.source = source;
		initDefaults();
	}
	
	private void initDefaults() {
		if (source == null) {
			return;
		}
		
		// Performance optimization: Options loading may be heavy weight.
		// Therefore, they are initially loaded only when the dialog is opened.
		// The initial selection is displayed on the button.
		updateDisplayUI(source.getDefaultDisplayValue());
		selectedKeys = new HashSet<>(source.getDefaultSelectedKeys());
	}
	
	private void updateDisplayUI() {
		updateDisplayUI(source.getDisplayValue(selectedKeys));
	}
	
	private void updateDisplayUI(ObjectDisplayValues displayValue) {
		component.setEscapeMode(displayValue.titleEscapeMode());
		
		if (!isEnabled()) {
			if (StringHelper.containsNonWhitespace(displayValue.title())) {
				component.setText(displayValue.title());
			} else {
				component.setText(null);
			}
			return;
		}
		
		String labelText = displayValue.ariaTitleLabel();
		if (!StringHelper.containsNonWhitespace(labelText)) {
			labelText = getLabelText();
		}
		
		if (StringHelper.containsNonWhitespace(displayValue.title())) {
			component.setText(displayValue.title());
			
			if (StringHelper.containsNonWhitespace(displayValue.ariaTitle())) {
				component.setAriaLabel(elementTranslator.translate("select.aria.value", labelText, displayValue.ariaTitle()));
			}
		} else {
			String text = StringHelper.containsNonWhitespace(noSelectionText)
					? StringHelper.escapeHtml(noSelectionText)
					: elementTranslator.translate("select.please");
			component.setText(text);
			
			if (StringHelper.containsNonWhitespace(labelText)) {
				component.setAriaLabel(elementTranslator.translate("select.please.aria", labelText));
			} else {
				component.setAriaLabel(elementTranslator.translate("select.please"));
			}
		}
	}
	
	private void doOpenSelection(UserRequest ureq) {
		selectionCtrl = new ObjectSelectionController(ureq, wControl, multiSelection, source, selectedKeys);
		selectionCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, selectionCtrl.getInitialComponent(),
				getFormDispatchId(), "", true, "",
				new CalloutSettings(false, CalloutSettings.CalloutOrientation.bottomOrTop, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
		
		component.setExpanded(true);
		// Dialog-Element of calloutCtrl would be better, but how to get the id?
		component.setAriaControls(Renderer.getComponentPrefix(selectionCtrl.getInitialComponent()));
	}
	

	
	private void doOpenBrowser(UserRequest ureq) {
		browseCtrl = source.getBrowserCreator(multiSelection).createController(ureq, wControl);
		browseCtrl.addControllerListener(this);
		
		String optionsLabel = source.getOptionsLabel(getTranslator().getLocale());
		optionsLabel = StringHelper.containsNonWhitespace(optionsLabel)? optionsLabel: getTranslator().translate("options");
		cmc = new CloseableModalController(wControl, component.getTranslator().translate("close"), browseCtrl.getInitialComponent(), true, optionsLabel);
		cmc.activate();
		cmc.addControllerListener(this);
	}
	
	private void doSelectedInBrowser(Collection<String> keys) {
		selectionCtrl.addSelection(keys);
	}

}
