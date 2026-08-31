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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Implements {@link ComponentCollection} so the rendering engine can track
 * the search input, search button and reset button as independently
 * replaceable components instead of treating this composite as one opaque
 * leaf that gets fully re-rendered on every keystroke round trip.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public class SearchElementComponent extends FormBaseComponentImpl implements ComponentCollection {

	private static final ComponentRenderer RENDERER = new SearchElementRenderer();

	private final SearchElementImpl searchElement;

	public SearchElementComponent(SearchElementImpl searchElement, String id) {
		super(id, "search");
		this.searchElement = searchElement;
	}

	@Override
	public SearchElement getFormItem() {
		return searchElement;
	}

	@Override
	public Component getComponent(String name) {
		FormItem item = searchElement.getFormComponent(name);
		return item == null ? null : item.getComponent();
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> components = new ArrayList<>();
		for (FormItem item : searchElement.getFormItems()) {
			components.add(item.getComponent());
		}
		return components;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// Nothing to dispatch here, SearchElementImpl.evalFormRequest() handles it
	}

}
