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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormLayouter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.ValidationStatus;

/**
 * 
 * Initial date: 06.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SyntheticFormItem<T> implements FormItem {
	
	private final String name;
	private final String itemId;
	private final String dispatchId;
	private final FlexiTableElementImpl table;
	
	public SyntheticFormItem(String name, String itemId, String dispatchId, FlexiTableElementImpl table) {
		this.name = name;
		this.itemId = itemId;
		this.dispatchId = dispatchId;
		this.table = table;
	}
	

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFormItemId() {
		return itemId;
	}

	@Override
	public String getFormDispatchId() {
		return dispatchId;
	}
	
	@Override
	public Form getRootForm() {
		return table.getRootForm();
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	public boolean hasFocus() {
		return false;
	}
	
	@Override
	public void setFocus(boolean hasFocus) {
		//
	}


	@Override
	public String getElementCssClass() {
		return null;
	}

	@Override
	public void setElementCssClass(String cssClass) {
		//
	}

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public Component getLabelC() {
		return null;
	}

	@Override
	public String getLabelText() {
		return null;
	}

	@Override
	public void setLabel(String labelkey, String[] params) {
		//
	}

	@Override
	public void setLabel(String labelkey, String[] params, boolean translate) {
		//
	}

	@Override
	public FormItem setLabelComponent(FormItem labelComponent, FormItemContainer formLayout) {
		return null;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean isMandatory) {
		//
	}

	@Override
	public Component getErrorC() {
		return null;
	}
	
	@Override
	public void setErrorKey(String errorKey, String[] params) {
		//
	}

	@Override
	public void setErrorComponent(FormItem errorFormItem, FormLayouter container) {
		//
	}

	@Override
	public Component getExampleC() {
		return null;
	}

	@Override
	public String getExampleText() {
		return null;
	}

	@Override
	public void setExampleKey(String exampleKey, String[] params) {
		//
	}

	@Override
	public Translator getTranslator() {
		return null;
	}
	
	@Override
	public void setTranslator(Translator translator) {
		//
	}

	@Override
	public boolean isVisible() {
		return false;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		//
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		//
	}

	@Override
	public boolean hasError() {
		return false;
	}

	@Override
	public boolean hasLabel() {
		return false;
	}

	@Override
	public boolean hasExample() {
		return false;
	}

	@Override
	public void setRootForm(Form rootForm) {
		//
	}

	@Override
	public void showLabel(boolean show) {
		//
	}

	@Override
	public void showError(boolean show) {
		//
	}

	@Override
	public void clearError() {
		//
	}

	@Override
	public void showExample(boolean show) {
		//
	}

	@Override
	public void addActionListener(Controller listener, int events) {
		//
	}

	@Override
	public int getAction() {
		return 0;
	}

	@Override
	public void setUserObject(Object userObject) {
		//
	}

	@Override
	public Object getUserObject() {
		return null;
	}
}