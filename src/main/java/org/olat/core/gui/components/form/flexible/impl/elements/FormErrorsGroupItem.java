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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 21 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormErrorsGroupItem extends FormItemImpl {
	
	private int errorHash = 0;
	private int warningHash = 0;
	
	private final FormErrorsGroup component;
	private final FormLayoutContainer container;
	
	public FormErrorsGroupItem(String name, FormLayoutContainer container) {
		super(name);
		this.container = container;
		component = new FormErrorsGroup(name, this);
	}

	@Override
	public boolean isValidationDeferred() {
		return true;
	}

	@Override
	public String getErrorText() {
		if(!hasError()) return "";
		
		return container.getFormComponents().values().stream()
			.filter(item -> item != this && item.isVisible() && item.isEnabled() && item.hasError())
			.map(FormItem::getErrorText)
			.filter(StringHelper::containsNonWhitespace)
			.collect(Collectors.joining("<br>"));
	}
	
	@Override
	public String getWarningText() {
		if(!hasWarning()) return "";
		
		return container.getFormComponents().values().stream()
			.filter(item -> item != this && item.isVisible() && item.isEnabled() && item.hasWarning())
			.map(FormItem::getWarningText)
			.filter(StringHelper::containsNonWhitespace)
			.collect(Collectors.joining("<br>"));
	}

	@Override
	public boolean validate() {
		List<String> errorIds = new ArrayList<>();
		List<String> warningIds = new ArrayList<>();
		for(FormItem item: container.getFormComponents().values()) {
			if(item != this && item.isVisible() && item.isEnabled()) {
				if(item.hasError()) {
					errorIds.add(item.getFormDispatchId());
				} else if(item.hasWarning()) {
					warningIds.add(item.getFormDispatchId());
				}
			}
		}
		
		boolean itemError = !errorIds.isEmpty();
		int newErrorHash = hashCode(errorIds);
		if(itemError != hasError) {
			hasError = itemError;
			setComponentDirty();
		}
		
		boolean itemWarning = !warningIds.isEmpty();
		int newWarningHash = hashCode(warningIds);
		if(itemWarning != hasWarning) {
			hasWarning = itemWarning;
			setComponentDirty();
		}
	
		if(errorHash != newErrorHash || warningHash != newWarningHash) {
			errorHash = newErrorHash;
			warningHash = newWarningHash;
			setComponentDirty();
		}
		return itemError;
	}
	
	private int hashCode(List<String> errors) {
		int c = 0;
		for(String error:errors) {
			c += error.hashCode();
		}
		return c;
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
