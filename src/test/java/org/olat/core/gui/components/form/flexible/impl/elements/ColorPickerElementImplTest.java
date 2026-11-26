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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.WindowControl;

/**
 * Checks the requests of the colour picker ({@link ColorPickerElementImpl}):
 * a pick with the values of the form, a pick of the selected colour (close),
 * the event ONCHANGE after a pick, a disabled picker, the dispose and the
 * target of the label.
 *
 * Initial date: 26 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorPickerElementImplTest {

	private static final List<Color> COLORS = List.of(
			new Color("red", "Red", "o_swatch_red"),
			new Color("blue", "Blue", "o_swatch_blue"));

	private ColorPickerElementImpl pickerEl;
	private Form form;
	private UserRequest ureq;

	@Before
	public void setUp() {
		pickerEl = new ColorPickerElementImpl(mock(WindowControl.class), "color", COLORS);
		form = mock(Form.class);
		pickerEl.setRootForm(form);
		ureq = mock(UserRequest.class);
	}

	@Test
	public void windowControlIsMandatory() {
		assertThatThrownBy(() -> new ColorPickerElementImpl(null, "color", COLORS))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	public void labelPointsToTheButton() {
		assertThat(pickerEl.getForId()).isEqualTo(pickerEl.getFormDispatchId() + "_B");
		assertThat(pickerEl.getPopupId()).isEqualTo(pickerEl.getFormDispatchId() + "_P");
		assertThat(pickerEl.isCalloutOpen()).isFalse();
	}

	@Test
	public void pickWithFormValues() {
		when(form.getRequestParameter(pickerEl.getFormDispatchId())).thenReturn("red");
		when(form.getRequestParameter(pickerEl.getFormDispatchId() + "_picked")).thenReturn("true");
		pickerEl.evalFormRequest(ureq);

		assertThat(pickerEl.getColor().id()).isEqualTo("red");
		assertThat(pickerEl.isColorPicked()).isTrue();
	}

	@Test
	public void valueWithoutPick() {
		pickerEl.setColor("blue");
		when(form.getRequestParameter(pickerEl.getFormDispatchId())).thenReturn("blue");
		when(form.getRequestParameter(pickerEl.getFormDispatchId() + "_picked")).thenReturn("");
		pickerEl.evalFormRequest(ureq);

		assertThat(pickerEl.getColor().id()).isEqualTo("blue");
		assertThat(pickerEl.isColorPicked()).isFalse();
	}

	@Test
	public void pickNoColor() {
		pickerEl.setColor("blue");
		when(form.getRequestParameter(pickerEl.getFormDispatchId())).thenReturn(ColorPickerRenderer.UNSELECT_KEY);
		when(form.getRequestParameter(pickerEl.getFormDispatchId() + "_picked")).thenReturn("true");
		pickerEl.evalFormRequest(ureq);

		assertThat(pickerEl.getColor()).isNull();
		assertThat(pickerEl.isColorPicked()).isTrue();
	}

	@Test
	public void disabledIgnoresPick() {
		pickerEl.setColor("blue");
		pickerEl.setEnabled(false);
		when(form.getRequestParameter(pickerEl.getFormDispatchId())).thenReturn("red");
		when(form.getRequestParameter(pickerEl.getFormDispatchId() + "_picked")).thenReturn("true");
		pickerEl.evalFormRequest(ureq);
		pickerEl.addActionListener(FormEvent.ONCHANGE);
		pickerEl.doDispatchFormRequest(ureq);

		assertThat(pickerEl.getColor().id()).isEqualTo("blue");
		assertThat(pickerEl.isColorPicked()).isFalse();
		verify(form, never()).fireFormEvent(any(), any());
	}

	@Test
	public void pickFiresOnChange() {
		pickerEl.addActionListener(FormEvent.ONCHANGE);
		when(form.getRequestParameter(ColorPickerElementImpl.CMD_PARAM)).thenReturn(ColorPickerElementImpl.CMD_PICK);
		pickerEl.doDispatchFormRequest(ureq);

		verify(form).fireFormEvent(eq(ureq), argThat(event -> event.getFormItemSource() == pickerEl
				&& event.wasTriggerdBy(FormEvent.ONCHANGE)));
	}

	@Test
	public void pickWithoutOnChange() {
		when(form.getRequestParameter(ColorPickerElementImpl.CMD_PARAM)).thenReturn(ColorPickerElementImpl.CMD_PICK);
		pickerEl.doDispatchFormRequest(ureq);

		verify(form, never()).fireFormEvent(any(), any());
	}

	@Test
	public void closeWithoutChange() {
		// a pick of the selected colour: the values of the form, the command close
		pickerEl.setColor("blue");
		pickerEl.addActionListener(FormEvent.ONCHANGE);
		when(form.getRequestParameter(pickerEl.getFormDispatchId())).thenReturn("blue");
		when(form.getRequestParameter(pickerEl.getFormDispatchId() + "_picked")).thenReturn("true");
		when(form.getRequestParameter(ColorPickerElementImpl.CMD_PARAM)).thenReturn(ColorPickerElementImpl.CMD_CLOSE);
		pickerEl.evalFormRequest(ureq);
		pickerEl.doDispatchFormRequest(ureq);

		assertThat(pickerEl.getColor().id()).isEqualTo("blue");
		// confirms a preselected colour
		assertThat(pickerEl.isColorPicked()).isTrue();
		verify(form, never()).fireFormEvent(any(), any());
	}

	@Test
	public void disposeWithoutCallout() {
		pickerEl.dispose();
		assertThat(pickerEl.isCalloutOpen()).isFalse();
	}

	@Test
	public void accessibleNamesWithoutTranslator() {
		assertThat(pickerEl.getButtonAriaLabel()).isNull();
		assertThat(pickerEl.getPopupAriaLabel()).isNull();
	}

	@Test
	public void alreadyFired() {
		pickerEl.addActionListener(FormEvent.ONCHANGE);
		when(form.hasAlreadyFired()).thenReturn(true);
		pickerEl.doDispatchFormRequest(ureq);

		verify(form, never()).fireFormEvent(any(), any());
	}
}
