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
package org.olat.modules.video.ui.editor;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 2023-03-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditPositionSizeController extends FormBasicController {
	private TextElement topEl;
	private TextElement leftEl;
	private TextElement widthEl;
	private TextElement heightEl;

	protected EditPositionSizeController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "edit_position_size");

		initForm(ureq);
	}

	public double getTop() {
		return parsePercentDouble(topEl);
	}

	private double parsePercentDouble(TextElement textElement) {
		double percentValue = Double.parseDouble(textElement.getValue());
		return percentValue / 100.0;
	}

	public void setTop(double top) {
		topEl.setValue(formatPercentDouble(top));
	}

	private String formatPercentDouble(Double value) {
		if (value == null || Double.isNaN(value)) {
			return "";
		}
		String stringValue = String.format(Locale.US, "%.5f", value * 100.0);
		if (stringValue.endsWith(".00000")) {
			stringValue = stringValue.substring(0, stringValue.length() - 6);
		}
		return stringValue;
	}

	public double getLeft() {
		return parsePercentDouble(leftEl);
	}

	public void setLeft(double left) {
		leftEl.setValue(formatPercentDouble(left));
	}

	public double getWidth() {
		return parsePercentDouble(widthEl);
	}

	public void setWidth(double width) {
		widthEl.setValue(formatPercentDouble(width));
	}

	public double getHeight() {
		return parsePercentDouble((heightEl));
	}

	public void setHeight(double height) {
		heightEl.setValue(formatPercentDouble(height));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		topEl = uifactory.addTextElement("top", "form.annotation.positionSize.top", -1, "", formLayout);
		leftEl = uifactory.addTextElement("left", "form.annotation.positionSize.left", -1, "", formLayout);
		widthEl = uifactory.addTextElement("width", "form.annotation.positionSize.width", -1, "", formLayout);
		heightEl = uifactory.addTextElement("height", "form.annotation.positionSize.height", -1, "", formLayout);

		uifactory.addFormSubmitButton("ok", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validatePercentInputField(topEl);
		allOk &= validatePercentInputField(leftEl);
		allOk &= validatePercentInputField(widthEl);
		allOk &= validatePercentInputField(heightEl);

		return allOk;
	}

	private boolean validatePercentInputField(TextElement textElement) {
		boolean allOk = true;
		textElement.clearError();
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			try {
				double value = parsePercentDouble(textElement);
				if (value < 0 || value > 1) {
					textElement.setErrorKey("form.error.nopercent");
					allOk = false;
				}
			} catch (Exception e) {
				textElement.setErrorKey("form.error.nopercent");
				allOk = false;
			}
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
