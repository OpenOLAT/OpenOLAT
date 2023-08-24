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
package org.olat.core.gui.components.emptystate;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 14 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyStateItem extends FormItemImpl {
	
	private final EmptyState component;
	
	public EmptyStateItem(String name) {
		super(name);
		component = new EmptyState(name);
	}
	
	public String getIconCss() {
		return component.getIconCss();
	}

	public void setIconCss(String iconCss) {
		component.setIconCss(iconCss);
	}

	public String getIndicatorIconCss() {
		return component.getIndicatorIconCss();
	}

	public void setIndicatorIconCss(String indicatorIconCss) {
		component.setIndicatorIconCss(indicatorIconCss);
	}

	public String getMessageI18nKey() {
		return component.getMessageI18nKey();
	}

	public void setMessageI18nKey(String messageI18nKey) {
		component.setMessageI18nKey(messageI18nKey);
	}
	
	@Override
	protected Component getFormItemComponent() {
		return component;
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
