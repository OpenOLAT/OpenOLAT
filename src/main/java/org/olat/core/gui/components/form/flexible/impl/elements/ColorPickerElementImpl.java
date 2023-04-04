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
import java.util.Locale;

import org.olat.core.commons.services.color.ColorServiceImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.util.Util;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorPickerElementImpl extends FormItemImpl implements ColorPickerElement {
	private final ColorPickerComponent component;
	private Color color;
	private final List<Color> colors;
	private boolean ajaxOnlyMode = false;
	private String nonSelectedText;
	private String cssPrefix;

	public ColorPickerElementImpl(String name, List<String> colors, Locale locale) {
		super(name);

		String id = getFormItemId() == null ? null : getFormItemId() + "_COLOR_PICKER";
		component = new ColorPickerComponent(id, this);
		setTranslator(Util.createPackageTranslator(ColorServiceImpl.class, locale));
		this.colors = colors.stream().map(this::getColorFromColorId).toList();
	}

	@Override
	public boolean isAjaxOnlyMode() {
		return ajaxOnlyMode;
	}

	@Override
	public void setAjaxOnlyMode(boolean ajaxOnlyMode) {
		this.ajaxOnlyMode = ajaxOnlyMode;
	}

	public String getNonSelectedText() {
		return nonSelectedText;
	}

	@Override
	public void setNonSelectedText(String nonSelectedText) {
		this.nonSelectedText = nonSelectedText;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setCssPrefix(String cssPrefix) {
		this.cssPrefix = cssPrefix;
	}

	public String getCssPrefix() {
		if (cssPrefix == null) {
			return null;
		}
		if (cssPrefix.endsWith("_")) {
			return cssPrefix;
		}
		return cssPrefix.concat("_");
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		if (isEnabled()) {
			if (isAjaxOnlyMode()) {
				String dispatchuri = form.getRequestParameter("dispatchuri");
				if (dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())) {
					String colorId = form.getRequestParameter("colorId");
					if (colorId != null) {
						setColor(colorId);
					}
				}
			} else {
				String colorId = form.getRequestParameter("o_cp" + component.getDispatchID());
				if (colorId != null) {
					setColor(colorId);
				}
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

	private Color getColorFromColorId(String colorId) {
		String colorText = getTranslator().translate("color.".concat(colorId));
		return new Color(colorId, colorText);
	}

	@Override
	public void setColor(String colorId) {
		this.color = colors.stream().filter(c -> c.getId().equals(colorId)).findFirst().orElse(null);
		component.setDirty(true);
	}
}
