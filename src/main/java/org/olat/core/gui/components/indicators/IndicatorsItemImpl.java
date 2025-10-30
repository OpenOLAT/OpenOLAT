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
package org.olat.core.gui.components.indicators;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: Oct 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IndicatorsItemImpl extends FormItemImpl implements IndicatorsItem {

	private final IndicatorsComponent component;

	protected IndicatorsItemImpl(String name) {
		super(name, false);
		
		component = new IndicatorsComponent(this);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
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
	public void setKeyIndicator(Component keyIndicator) {
		component.setKeyIndicator(keyIndicator);
	}

	@Override
	public void setKeyIndicator(FormItem keyIndicator) {
		setKeyIndicator(keyIndicator.getComponent());
	}

	@Override
	public void setFocusIndicators(List<Component> focusIndicators) {
		component.setFocusIndicators(focusIndicators);
	}

	@Override
	public void setFocusIndicatorsItems(List<FormItem> focusIndicators) {
		List<Component> components = focusIndicators.stream().map(FormItem::getComponent).collect(Collectors.toList());
		setFocusIndicators(components);
	}

}
