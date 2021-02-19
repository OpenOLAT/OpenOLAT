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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxListComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * This class wraps the TextBoxListElementComponent<br />
 * it delegates most of the methods to the TextBoxListElementComponent
 * 
 * 
 * <P>
 * Initial Date:  27.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListElementImpl extends AbstractTextElement implements TextBoxListElement {

	/**
	 * the wrapped textBoxListElementComponent
	 */
	private final TextBoxListElementComponent component;

	public TextBoxListElementImpl(String name, String inputHint, List<TextBoxItem> initialItems, Translator translator) {
		super(name,true);// we wan't to be an inline-editing element!
		component = new TextBoxListElementComponent(this, name, inputHint, initialItems, translator);
		setInlineEditingComponent(component);
		setTranslator(translator);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(!isEnabled()) return;
		
		String inputId = TextBoxListComponent.INPUT_SUFFIX + getFormDispatchId();
		String cmd = ureq.getParameter(inputId);
		if(StringHelper.containsNonWhitespace(cmd)) {
			component.dispatchRequest(ureq);
		} else {
			//this one handle multipart/form too
			String submitValue = getRootForm().getRequestParameter(inputId);
			if(StringHelper.containsNonWhitespace(submitValue)) {
				component.setCmd(ureq, submitValue);
			} else if(submitValue != null) {
				component.clearItems();
			}
		}
	}

	@Override
	public void setTranslator(Translator translator) {
		// wrap package translator with fallback form translator
		Translator elmTranslator = Util.createPackageTranslator(TextBoxListElementImpl.class,
				translator.getLocale(), translator);
		super.setTranslator(elmTranslator);
	}
	
	@Override
	public String getValue() {
		return StringUtils.join(component.getCurrentItemValues(),", ");
	}
	
	@Override
	public List<TextBoxItem> getValueItems() {
		return component.getCurrentItems();
	}

	@Override
	public List<String> getValueList() {
		return component.getCurrentItemValues();
	}

	@Override
	public void setAutoCompleteContent(Set<String> autoCompletionValues) {
		component.setAutoCompleteContent(autoCompletionValues);		
	}
	
	@Override
	public void setAutoCompleteContent(List<TextBoxItem> tagM) {
		component.setAutoCompleteContent(tagM);		
	}
	
	/**
	 * @return Returns the allowDuplicates.
	 */
	@Override
	public boolean isAllowDuplicates() {
		return component.isAllowDuplicates();
	}

	/**
	 * @param allowDuplicates
	 *            if set to false (default) duplicates will be filtered
	 *            automatically
	 */
	@Override
	public void setAllowDuplicates(boolean allowDuplicates) {
		component.setAllowDuplicates(allowDuplicates);
	}

	@Override
	public void setMapperProvider(ResultMapProvider provider, UserRequest ureq) {
		component.setMapperProvider(provider, ureq);
	}

	@Override
	public boolean isAllowNewValues() {
		return component.isAllowNewValues();
	}

	@Override
	public void setAllowNewValues(boolean allowNewValues) {
		component.setAllowNewValues(allowNewValues);
	}

	@Override
	public void setMaxResults(int maxResults) {
		component.setMaxResults(maxResults);
	}
	
	@Override
	public void setIcon(String icon) {
		component.setIcon(icon);		
	}
}
