/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.component;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * 
 * Initial date: 4 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DurationComponent extends FormBaseComponentImpl {
	
	private DurationComponentRenderer RENDERER = new DurationComponentRenderer();

	private String addOn;
	private DurationFormItem formItem;
	
	public DurationComponent(DurationFormItem formItem, String name) {
		super(name);
		setTranslator(formItem.getTranslator());
		this.formItem = formItem;
	}

	@Override
	public DurationFormItem getFormItem() {
		return formItem;
	}

	public String getAddOn() {
		return addOn;
	}

	public void setAddOn(String addOn) {
		this.addOn = addOn;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// Nothing to dispatch here
	}

}
