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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorPickerElementImpl extends FormItemImpl implements ColorPickerElement {
	private final ColorPickerComponent component;
	private Color color;
	private final List<Color> colors;
	private String nonSelectedText;
	private String resetButtonId;
	private boolean dropUp;
	private boolean allowUnselect;

	public ColorPickerElementImpl(String name, List<Color> colors) {
		super(name);

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

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		if (isEnabled()) {
			String dispatchuri = form.getRequestParameter("dispatchuri");
			if (dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())) {
				String colorId = form.getRequestParameter("colorId");
				if (colorId != null) {
					setColor(colorId);
					return;
				}
			}
			String colorId = form.getRequestParameter(DISPPREFIX + component.getDispatchID());
			if (colorId != null) {
				setColor(colorId);
			}
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

	public List<Color> getColors() {
		return colors;
	}

	@Override
	public void setColor(String colorId) {
		this.color = colors.stream().filter(c -> c.id().equals(colorId)).findFirst().orElse(null);
		component.setDirty(true);
	}

	@Override
	public boolean isDropUp() {
		return dropUp;
	}

	@Override
	public void setDropUp(boolean dropUp) {
		this.dropUp = dropUp;
	}
}
