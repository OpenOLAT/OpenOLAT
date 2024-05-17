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
package org.olat.core.gui.components.widget;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 13 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class Widget extends AbstractComponent {
	
	private String title;
	private String subTitle;
	private String iconCss;

	protected Widget(String name) {
		super(name);
	}
	
	protected Widget(String name, Translator translator) {
		super(name, translator);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return getWidgetRenderer();
	}

	protected abstract WidgetRenderer getWidgetRenderer();

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		setDirty(true);
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
		setDirty(true);
	}

	public String getIconCss() {
		return iconCss;
	}

	public void setIconCss(String iconCss) {
		this.iconCss = iconCss;
		setDirty(true);
	}

}
