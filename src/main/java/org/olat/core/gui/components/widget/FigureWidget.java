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

/**
 * 
 * Initial date: 13 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FigureWidget extends AdditionalWidget {
	
	private static final WidgetRenderer RENDERER = new FigureWidgetRenderer();
	
	private String value;
	private String valueCssClass;
	private String desc;

	protected FigureWidget(String name) {
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		setDirty(true);
	}

	public String getValueCssClass() {
		return valueCssClass;
	}

	public void setValueCssClass(String valueCssClass) {
		this.valueCssClass = valueCssClass;
		setDirty(true);
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
		setDirty(true);
	}

}
