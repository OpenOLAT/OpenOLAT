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
import org.olat.core.gui.components.form.flexible.elements.IconSelectorElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * Initial date: 2024-02-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IconSelectorElementImpl extends FormItemImpl implements IconSelectorElement {
	private final IconSelectorComponent component;
	private Icon icon;
	private final List<Icon> icons;
	private boolean dropUp;

	public IconSelectorElementImpl(String name, List<Icon> icons) {
		super(name);

		String id = getFormItemId() == null ? null : getFormItemId() + "_ICON_SELECTOR";
		component = new IconSelectorComponent(id, this);
		this.icons = icons;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		if (isEnabled()) {
			String dispatchuri = form.getRequestParameter("dispatchuri");
			if (dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())) {
				String iconId = form.getRequestParameter("iconId");
				if (iconId != null) {
					setIcon(iconId);
					return;
				}
			}
			String iconId = form.getRequestParameter(DISPPREFIX + component.getDispatchID());
			if (iconId != null) {
				setIcon(iconId);
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

	public List<Icon> getIcons() {
		return icons;
	}

	@Override
	public void setIcon(String iconId) {
		this.icon = icons.stream().filter(i -> i.id().equals(iconId)).findFirst().orElse(null);
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
