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
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * 
 * Initial date: 24 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyPanel extends AbstractComponent implements FormBaseComponent {
	
	private static final EmptyPanelRenderer RENDERER = new EmptyPanelRenderer();

	private String title;
	private String informations;
	private String iconCssClass;
	
	private final EmptyPanelItem emptyPanelItem;
	
	public EmptyPanel(String name, EmptyPanelItem emptyPanelItem) {
		super(name);
		this.emptyPanelItem = emptyPanelItem;
	}
	
	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}

	@Override
	public FormItem getFormItem() {
		return emptyPanelItem;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInformations() {
		return informations;
	}

	public void setInformations(String informations) {
		this.informations = informations;
		setDirty(true);
	}
	
	public String getIconCssClass() {
		return iconCssClass;
	}

	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
		setDirty(true);
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
