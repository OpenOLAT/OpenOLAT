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
package org.olat.core.gui.components.date;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.Util;

/**
 * Initial date: 2026-06-16<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RelativeDateElementImpl extends FormItemImpl
		implements RelativeDateElement, FormItemCollection, ControllerEventListener {

	static final String LINK_COMP_NAME = "rightAddOn";

	private final RelativeDateComponent component;
	private final WindowControl wControl;

	private FormLink link;
	private RelativeDateContext context;
	private DisplayFormatter displayFormatter;
	private RelativeDateSelection value;
	private String displayText;
	private String iconLeftCss;
	private String ariaLabel;

	private RelativeDatePickerController pickerCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	public RelativeDateElementImpl(WindowControl wControl, String name, RelativeDateContext context) {
		super(name);
		this.wControl = wControl;
		this.context = context;
		this.component = new RelativeDateComponent(this);
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
	public Iterable<FormItem> getFormItems() {
		return List.of(link);
	}

	@Override
	public FormItem getFormComponent(String name) {
		if (link != null && link.getName().equals(name)) {
			return link;
		}
		return null;
	}

	@Override
	protected void rootFormAvailable() {
		if (link == null) {
			link = new FormLinkImpl(LINK_COMP_NAME, LINK_COMP_NAME, "set.rule", Link.BUTTON);
			link.setIconLeftCSS("o_icon o_icon_calendar");
			link.setElementCssClass("input-group-addon");
			link.setTranslator(Util.createPackageTranslator(RelativeDateElementImpl.class, getTranslator().getLocale()));
			link.setRootForm(getRootForm());
		} else if (link.getRootForm() != getRootForm()) {
			link.setTranslator(Util.createPackageTranslator(RelativeDateElementImpl.class, getTranslator().getLocale()));
			link.setRootForm(getRootForm());
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String dispatchUri = getRootForm().getRequestParameter("dispatchuri");
		if (link != null && link.getFormDispatchId().equals(dispatchUri)) {
			doOpenPicker(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (pickerCtrl == source) {
			if (event instanceof RelativeDateAppliedEvent ae) {
				setValue(ae.getSelection());
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
			} else if (event instanceof RelativeDateRemovedEvent) {
				setValue(null);
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
			}
			cleanUp();
		} else if (calloutCtrl == source) {
			cleanUp();
		}
	}

	private void doOpenPicker(UserRequest ureq) {
		if (pickerCtrl != null) {
			return;
		}
		pickerCtrl = new RelativeDatePickerController(ureq, wControl, context, value);
		pickerCtrl.addControllerListener(this);
		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl,
				pickerCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "",
				new CalloutSettings(true, CalloutSettings.CalloutOrientation.bottomOrTop, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}

	private void cleanUp() {
		if (calloutCtrl != null) {
			calloutCtrl.deactivate();
			calloutCtrl.removeControllerListener(this);
			calloutCtrl.dispose();
			calloutCtrl = null;
		}
		if (pickerCtrl != null) {
			pickerCtrl.removeControllerListener(this);
			pickerCtrl.dispose();
			pickerCtrl = null;
		}
	}

	@Override
	public void setContext(RelativeDateContext context) {
		this.context = context;
	}

	@Override
	public void setDisplayFormatter(DisplayFormatter formatter) {
		this.displayFormatter = formatter;
	}

	@Override
	public RelativeDateSelection getValue() {
		return value;
	}

	@Override
	public void setValue(RelativeDateSelection value) {
		this.value = value;
		updateDisplay();
		component.setDirty(true);
	}

	@Override
	public void refreshDisplay() {
		updateDisplay();
		component.setDirty(true);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (link != null) {
			link.setEnabled(isEnabled);
		}
		super.setEnabled(isEnabled);
	}

	String getDisplayText() {
		return displayText;
	}

	String getIconLeftCss() {
		return iconLeftCss;
	}

	String getAriaLabel() {
		return ariaLabel;
	}

	@Override
	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
	}

	private void updateDisplay() {
		if (value == null) {
			displayText = null;
			iconLeftCss = null;
			return;
		}
		RelativeDateDisplayValue dv = null;
		if (displayFormatter != null) {
			dv = displayFormatter.format(value);
		} else if (context != null) {
			String composedRef;
			if (!value.isOffsetEnabled()) {
				composedRef = "SAME_DAY_" + value.getRefKey();
			} else {
				composedRef = value.getDirection().name() + "_" + value.getRefKey();
			}
			dv = context.getDisplayValue(composedRef, value.getUnitKey(), value.getValue());
		}
		displayText = dv != null ? dv.text() : null;
		iconLeftCss = dv != null ? dv.iconLeftCss() : null;
	}

}
