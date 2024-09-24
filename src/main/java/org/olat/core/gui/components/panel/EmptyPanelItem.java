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
package org.olat.core.gui.components.panel;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 24 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyPanelItem extends FormItemImpl {

	private final EmptyPanel panel;
	
	public EmptyPanelItem(String name) {
		super(name);
		panel = new EmptyPanel(name, this);
	}
	
	public String getTitle() {
		return panel.getTitle();
	}

	public void setTitle(String title) {
		panel.setTitle(title);
	}

	public String getInformations() {
		return panel.getInformations();
	}

	public void setInformations(String informations) {
		panel.setInformations(informations);
	}
	
	public String getIconCssClass() {
		return panel.getIconCssClass();
	}

	public void setIconCssClass(String iconCssClass) {
		panel.setIconCssClass(iconCssClass);
	}
	
	@Override
	public void setElementCssClass(String elementCssClass) {
		panel.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	@Override
	protected EmptyPanel getFormItemComponent() {
		return panel;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected void rootFormAvailable() {
		//
	}
}
