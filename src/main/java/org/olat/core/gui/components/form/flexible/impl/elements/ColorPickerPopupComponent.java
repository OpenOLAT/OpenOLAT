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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * The colours of a colour picker in the callout: the list or the grid. The
 * component has no request of its own: a pick sends a request to the form
 * of the picker, see {@link ColorPickerPopupRenderer}.
 *
 * Initial date: 26 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorPickerPopupComponent extends AbstractComponent {

	private static final ComponentRenderer RENDERER = new ColorPickerPopupRenderer();

	private final ColorPickerElementImpl colorPickerEl;

	public ColorPickerPopupComponent(ColorPickerElementImpl colorPickerEl) {
		super(colorPickerEl.getName() + "_popup");
		this.colorPickerEl = colorPickerEl;
	}

	public ColorPickerElementImpl getColorPickerElement() {
		return colorPickerEl;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
