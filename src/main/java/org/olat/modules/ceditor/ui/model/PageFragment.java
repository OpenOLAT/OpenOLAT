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
package org.olat.modules.ceditor.ui.model;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.ui.ValidationMessage;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageFragment {
	
	private final String type;
	private final String componentName;
	private final PageElement element;
	private final PageRunElement runElement;
	
	public PageFragment(String type, String componentName, PageRunElement runElement, PageElement element) {
		this.type = type;
		this.element = element;
		this.componentName = componentName;
		this.runElement = runElement;
	}
	
	public String getCssClass() {
		return "o_ed_".concat(type);
	}
	
	public boolean isContainer() {
		return element instanceof ContainerElement;
	}
	
	public String getElementId() {
		return element.getId();
	}
	
	public String getComponentName() {
		return componentName;
	}
	
	public Component getComponent() {
		return runElement.getComponent();
	}
	
	public PageElement getPageElement() {
		return element;
	}
	
	public PageRunElement getPageRunElement() {
		return runElement;
	}
	
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return runElement.validate(ureq, messages);
	}
}
