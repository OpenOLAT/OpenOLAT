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
import org.olat.core.gui.components.Component;

/**
 * 
 * Initial date: 13 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ComponentWidget extends AdditionalWidget {
	
	private static final WidgetRenderer RENDERER = new ComponentWidgetRenderer();
	
	private Component content;
	private String mainCss;

	protected ComponentWidget(String name) {
		super(name);
	}

	@Override
	protected WidgetRenderer getWidgetRenderer() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public Component getContent() {
		return content;
	}

	public void setContent(Component content) {
		this.content = content;
	}

	public String getMainCss() {
		return mainCss;
	}

	public void setMainCss(String mainCss) {
		this.mainCss = mainCss;
	}

}
