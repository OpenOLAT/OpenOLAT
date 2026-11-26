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

import java.util.List;
import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * The colour picker: a button in the form with the selected colour and a
 * hidden field with the value. A click on the button opens the colours (list
 * or grid, see {@link ColorPickerElement.Presentation}) in a callout at window
 * level, so a dialog or a scroll container does not clip them. The open and
 * the pick submit the values of the form: the callout redraws the form below
 * it, the values the user typed stay. A pick sets the hidden field in the
 * browser and sends a request to the element, which closes the callout and
 * fires the event ONCHANGE if the element listens to it. A pick of the
 * selected colour, Escape and a click outside close the callout without a
 * change. The callout returns the focus to the button.
 * 
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorPickerElementImpl extends FormItemImpl implements ColorPickerElement, ControllerEventListener, Disposable {

	/** The suffix of the name of the hidden field that is "true" after the user picked a colour */
	static final String PICKED_SUFFIX = "_picked";
	/** The suffix of the id of the button which opens the callout */
	static final String BUTTON_SUFFIX = "_B";
	/** The suffix of the id of the colours in the callout */
	static final String POPUP_SUFFIX = "_P";
	/** The request parameter with the command of a request to the element */
	static final String CMD_PARAM = "cp_cmd";
	static final String CMD_OPEN = "open";
	static final String CMD_PICK = "pick";
	/** Closes the callout without a change, e.g. a pick of the selected colour */
	static final String CMD_CLOSE = "close";

	private final WindowControl wControl;
	private final ColorPickerComponent component;
	private CloseableCalloutWindowController calloutCtrl;
	private Color color;
	private final List<Color> colors;
	private String nonSelectedText;
	private String resetButtonId;
	private boolean allowUnselect;
	private boolean colorPicked;
	private Presentation presentation = Presentation.LIST;
	private Translator pickerTranslator;

	/**
	 * @param wControl The window control of the controller of the form, it
	 * 		opens the callout with the colours
	 * @param name The name of the element
	 * @param colors The colours
	 */
	public ColorPickerElementImpl(WindowControl wControl, String name, List<Color> colors) {
		super(name);
		this.wControl = Objects.requireNonNull(wControl, "The colour picker needs the window control of its controller");

		String id = getFormItemId() == null ? null : getFormItemId() + "_COLOR_PICKER";
		component = new ColorPickerComponent(id, this);
		this.colors = colors;
	}

	public String getNonSelectedText() {
		return nonSelectedText;
	}

	@Override
	public void setNonSelectedText(String nonSelectedText) {
		this.nonSelectedText = nonSelectedText;
	}

	public boolean isAllowUnselect() {
		return allowUnselect;
	}

	@Override
	public void setAllowUnselect(boolean allowUnselect) {
		this.allowUnselect = allowUnselect;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setResetButtonId(String resetButtonId) {
		this.resetButtonId = resetButtonId;
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}

	public String getResetButtonId() {
		return resetButtonId;
	}

	/**
	 * The label points to the button, not to the hidden field of the value.
	 */
	@Override
	public String getForId() {
		return getButtonId();
	}

	/**
	 * @return The DOM id of the button which opens the callout
	 */
	String getButtonId() {
		return getFormDispatchId() + BUTTON_SUFFIX;
	}

	/**
	 * @return The DOM id of the colours in the callout
	 */
	String getPopupId() {
		return getFormDispatchId() + POPUP_SUFFIX;
	}

	/**
	 * @return true while the callout with the colours is open
	 */
	boolean isCalloutOpen() {
		return calloutCtrl != null;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		if (isEnabled()) {
			if ("true".equals(form.getRequestParameter(getPickedFieldName()))) {
				colorPicked = true;
			}
			String colorId = form.getRequestParameter(DISPPREFIX + component.getDispatchID());
			if (colorId != null) {
				setColor(colorId);
			}
		}
	}

	/**
	 * A request to the element opens the callout (command {@link #CMD_OPEN}),
	 * closes it without a change ({@link #CMD_CLOSE}) or is a pick:
	 * {@link #evalFormRequest(UserRequest)} has set the colour from the hidden
	 * field, the pick closes the callout and fires ONCHANGE if the element
	 * listens to it. All requests submit the values of the form.
	 */
	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		if (form.hasAlreadyFired() || !isEnabled()) {
			return;
		}

		String cmd = form.getRequestParameter(CMD_PARAM);
		if (CMD_OPEN.equals(cmd)) {
			doOpenCallout(ureq);
		} else if (CMD_CLOSE.equals(cmd)) {
			doCloseCallout();
		} else {
			doCloseCallout();
			if (getAction() == FormEvent.ONCHANGE) {
				form.fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (calloutCtrl == source) {
			// Escape or a click outside: the callout closed itself
			cleanUpCallout();
		}
	}

	private void doOpenCallout(UserRequest ureq) {
		if (calloutCtrl != null) {
			doCloseCallout();
		}

		ColorPickerPopupComponent popup = new ColorPickerPopupComponent(this);
		// top modal layer: the push and the pop do not redraw a dialog below the
		// callout and its form (no lost input, no "dirty" mark from the redraw)
		CalloutSettings settings = new CalloutSettings(false, CalloutSettings.CalloutOrientation.bottomOrTop, false, null, true);
		// the list like a dropdown menu: as wide as the button; the grid as wide as its
		// columns, at the left edge of the button (the right edge if there is no room)
		settings.setAlignment(presentation == Presentation.GRID
				? CalloutSettings.CalloutAlignment.edge : CalloutSettings.CalloutAlignment.targetWidth);
		String label = getPopupAriaLabel();
		if (StringHelper.containsNonWhitespace(label)) {
			settings.setAriaLabel(label);
		}
		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, popup, getButtonId(), "", true, "", settings);
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();

		// the callout sets aria-expanded of the button (aria-haspopup dialog)
		wControl.getWindowBackOffice().sendCommandTo(FunctionCommand.setAttribute(getButtonId(), "aria-controls", getPopupId()));
	}

	private void doCloseCallout() {
		if (calloutCtrl != null) {
			// returns the focus to the button
			calloutCtrl.deactivate();
			cleanUpCallout();
		}
	}

	/**
	 * The owning controller disposes the form: the open callout goes with it.
	 * The callout removes itself from the GUI stack only if it is the top layer.
	 */
	@Override
	public void dispose() {
		if (calloutCtrl != null) {
			calloutCtrl.removeControllerListener(this);
			calloutCtrl.dispose();
			calloutCtrl = null;
		}
	}

	private void cleanUpCallout() {
		if (calloutCtrl != null) {
			calloutCtrl.removeControllerListener(this);
			calloutCtrl.dispose();
			calloutCtrl = null;
			wControl.getWindowBackOffice().sendCommandTo(FunctionCommand.setAttribute(getButtonId(), "aria-controls", null));
		}
	}

	@Override
	public void reset() {
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
	}

	private Translator getPickerTranslator() {
		if (pickerTranslator == null && getTranslator() != null) {
			pickerTranslator = Util.createPackageTranslator(ColorPickerElementImpl.class, getTranslator().getLocale());
		}
		return pickerTranslator;
	}

	/**
	 * @return The accessible name of the button of a picker without label,
	 * 		"Color" and the current colour, e.g. "Color: Green". Null if the
	 * 		picker has a label: the label and the button name it.
	 */
	String getButtonAriaLabel() {
		Translator translator = getPickerTranslator();
		if (hasLabel() || translator == null) {
			return null;
		}
		String current = color == null ? nonSelectedText : color.translatedName();
		if (StringHelper.containsNonWhitespace(current)) {
			return translator.translate("color.picker.value", current);
		}
		return translator.translate("color.picker");
	}

	/**
	 * @return The accessible name of the callout and of the grid: the label,
	 * 		else "Color"
	 */
	String getPopupAriaLabel() {
		String label = hasLabel() ? getLabelText() : null;
		if (StringHelper.containsNonWhitespace(label)) {
			return label;
		}
		Translator translator = getPickerTranslator();
		return translator == null ? null : translator.translate("color.picker");
	}

	public List<Color> getColors() {
		return colors;
	}

	@Override
	public void setColor(String colorId) {
		this.color = colors.stream().filter(c -> c.id().equals(colorId)).findFirst().orElse(null);
		component.setDirty(true);
	}

	@Override
	public boolean isColorPicked() {
		return colorPicked;
	}

	@Override
	public void resetColorPicked() {
		colorPicked = false;
		component.setDirty(true);
	}

	/**
	 * @return The name of the hidden field that the picker sets to "true"
	 * 		when the user picks a colour
	 */
	String getPickedFieldName() {
		return getFormDispatchId() + PICKED_SUFFIX;
	}

	@Override
	public Presentation getPresentation() {
		return presentation;
	}

	@Override
	public void setPresentation(Presentation presentation) {
		this.presentation = presentation == null ? Presentation.LIST : presentation;
		component.setDirty(true);
	}
}
