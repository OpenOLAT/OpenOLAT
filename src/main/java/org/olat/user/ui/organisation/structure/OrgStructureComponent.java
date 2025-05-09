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
package org.olat.user.ui.organisation.structure;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Initial date: Mai 07, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrgStructureComponent extends FormBaseComponentImpl {
	private static final ComponentRenderer RENDERER = new OrgStructureRenderer();
	private final OrgStructureElementImpl item;

	public OrgStructureComponent(String name, OrgStructureElementImpl item) {
		super(name);
		this.item = item;
	}

	@Override
	public OrgStructureElementImpl getFormItem() {
		return item;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// handled in the FormItemImpl
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
