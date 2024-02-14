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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.StaticListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 14 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class StaticListComponent extends FormBaseComponentImpl {
	
	static final String CMD_SHOW_ALL = "static.list.show.all";
	private static final ComponentRenderer RENDERER = new StaticListRenderer();

	private final StaticListElement element;
	private List<String> values;
	private int initialNumValues = 5;
	private String showAllI18nKey;
	private boolean showAllVisible = true;

	public StaticListComponent(String name) {
		this(name, null);
	}
	
	public StaticListComponent(String name, StaticListElement element) {
		super(name);
		this.element = element;
	}

	@Override
	public FormItem getFormItem() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if (CMD_SHOW_ALL.equalsIgnoreCase(cmd)) {
			showAllVisible = false;
			setDirty(true);
		}
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public int getInitialNumValues() {
		return initialNumValues;
	}

	public void setInitialNumValues(int initialNumValues) {
		this.initialNumValues = initialNumValues;
	}

	public String getShowAllI18nKey() {
		return showAllI18nKey;
	}

	public void setShowAllI18nKey(String showAllI18nKey) {
		this.showAllI18nKey = showAllI18nKey;
	}

	public boolean isShowAllVisible() {
		return showAllVisible;
	}

	public void setShowAllVisible(boolean showAllVisible) {
		this.showAllVisible = showAllVisible;
	}

}
