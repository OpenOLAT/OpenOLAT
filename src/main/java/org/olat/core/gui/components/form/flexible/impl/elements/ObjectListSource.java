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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.gui.control.creator.ControllerCreator;

/**
 * 
 * Initial date: Sep 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ObjectListSource implements ObjectSelectionSource {
	
	private final ObjectDisplayValues defaultDisplayValue;
	private final List<? extends ObjectOption> options;
	private final String optionsLabel;
	
	public ObjectListSource(List<ObjectOption> options) {
		this(null, options, null);
	}
	
	public ObjectListSource(ObjectDisplayValues defaultDisplayValue, List<? extends ObjectOption> options, String optionsLabel) {
		this.defaultDisplayValue = defaultDisplayValue != null? defaultDisplayValue: ObjectDisplayValues.NONE;
		this.options = options;
		this.optionsLabel = optionsLabel;
	}
	
	@Override
	public ObjectDisplayValues getDefaultDisplayValue() {
		return defaultDisplayValue;
	}

	@Override
	public ObjectDisplayValues getDisplayValue(Collection<String> keys) {
		String title = options.stream()
				.filter(option -> keys.contains(option.getKey()))
				.map(ObjectOption::getTitle)
				.collect(Collectors.joining(", "));
		
		return new ObjectDisplayValues(title, title);
	}

	@Override
	public Collection<String> getDefaultSelectedKeys() {
		return List.of();
	}

	@Override
	public String getOptionsLabel(Locale locale) {
		return optionsLabel;
	}

	@Override
	public List<? extends ObjectOption> getOptions() {
		return options;
	}

	@Override
	public boolean isBrowserAvailable() {
		return false;
	}

	@Override
	public ControllerCreator getBrowserCreator(boolean multiSelection) {
		return null;
	}

}
