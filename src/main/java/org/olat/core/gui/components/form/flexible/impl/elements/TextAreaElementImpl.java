/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * TextArea-Element for FlexiForm
 * <P>
 * Initial Date: 31.01.2008 <br>
 * 
 * @author rhaag
 */
public abstract class TextAreaElementImpl extends AbstractTextElement implements TextAreaElement {

	protected final TextAreaElementComponent component;

	/**
	 * Constructor for specialized TextElements, i.e. IntegerElementImpl.
	 * @param name
	 * @param predefinedValue Initial value
	 * @param rows the number of lines or -1 to use default value
	 * @param cols the number of characters per line or -1 to use 100% of the
	 *          available space
	 * @param isAutoHeightEnabled true: element expands to fit content height,
	 *          (max 100 lines); false: specified rows used
	 * @param fixedFontWidth
	 * @param originalLineBreaks Try to maintain the original line breaks and prevent the browser to add its own
	 */
	public TextAreaElementImpl(String name, String predefinedValue, int rows, int cols, boolean isAutoHeightEnabled, boolean fixedFontWidth, boolean originalLineBreaks) {
		this(name, rows, cols, isAutoHeightEnabled, fixedFontWidth, originalLineBreaks);
		setValue(predefinedValue);
	}

	/**
	 * Constructor for specialized TextElements, i.e. IntegerElementImpl.
	 * @param name
	 * @param rows the number of lines or -1 to use default value
	 * @param cols the number of characters per line or -1 to use 100% of the
	 *          available space
	 * @param isAutoHeightEnabled true: element expands to fit content height,
	 *          (max 100 lines); false: specified rows used
	 * @param fixedFontWidth
	 * @param originalLineBreaks Try to maintain the original line breaks and prevent the browser to add its own
	 */
	protected TextAreaElementImpl(String name, int rows, int cols, boolean isAutoHeightEnabled, boolean fixedFontWidth, boolean originalLineBreaks) {
		super(name);
		component = new TextAreaElementComponent(this, rows, cols, isAutoHeightEnabled, fixedFontWidth, originalLineBreaks);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String paramId = String.valueOf(component.getFormDispatchId());
		String val = getRootForm().getRequestParameter(paramId);
		if (val != null) {
			setValue(val);
			// mark associated component dirty, that it gets rerendered
			component.setDirty(true);
		}

	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public int getRows() {
		return component.getRows();
	}

	@Override
	public void setRows(int rows) {
		component.setRows(rows);
	}
	
	@Override
	public boolean isLineNumbersEnabled() {
		return component.isLineNumbersEnabled();
	}
	
	@Override
	public void setLineNumbersEnbaled(boolean lineNumbersEnabled) {
		component.setLineNumbersEnabled(lineNumbersEnabled);		
	}
	
	@Override
	public boolean isStripedBackgroundEnabled() {
		return component.isStripedBackgroundEnabled();
	}
	
	@Override
	public void setStripedBackgroundEnabled(boolean stripedBackgroundEnabled) {
		component.setStripedBackgroundEnabled(stripedBackgroundEnabled);
	}
	
	@Override
	public boolean isOriginalLineBreaks() {
		return component.isOriginalLineBreaks();
	}
	
	@Override
	public void setOriginalLineBreaks(boolean originalLineBreaks) {
		component.setOriginalLineBreaks(originalLineBreaks);
	}
	
	@Override
	public boolean isFixedFontWidth() {
		return component.isFixedFontWidth();
	}
	
	@Override
	public void setFixedFontWidth(boolean fixedFontWidth) {
		component.setFixedFontWidth(fixedFontWidth);
	}
	
	@Override
	public void setErrors(List<Integer> rows) {
		component.setErrors(rows);
	}
	
	@Override
	public List<Integer> getErrors() {
		return component.getErrors();
	}
	
	@Override
	public String getErrorsAsString() {
		return component.getErrorsAsString();
	}

	@Override
	public void setTranslator(Translator translator) {
		// wrap package translator with fallback form translator
		// hint: do not take this.getClass() but the real class! for package translator creation
		Translator elmTranslator = Util.createPackageTranslator(TextAreaElementImpl.class, translator.getLocale(), translator);
		super.setTranslator(elmTranslator);
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}
}