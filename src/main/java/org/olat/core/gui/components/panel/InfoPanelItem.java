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
 * Initial date: 21 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfoPanelItem extends FormItemImpl {
	
	private final InfoPanel infoPanel;
	
	public InfoPanelItem(String name) {
		super(name);
		infoPanel = new InfoPanel(name);
	}
	
	public String getTitle() {
		return infoPanel.getTitle();
	}

	public void setTitle(String title) {
		infoPanel.setTitle(title);
	}

	public String getInformations() {
		return infoPanel.getInformations();
	}

	public void setInformations(String informations) {
		infoPanel.setInformations(informations);
	}

	public void setPersistedStatusId(UserRequest ureq, String id) {
		infoPanel.setPersistedStatusId(ureq, id);
	}
	
	@Override
	public void setElementCssClass(String elementCssClass) {
		infoPanel.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	@Override
	protected InfoPanel getFormItemComponent() {
		return infoPanel;
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
