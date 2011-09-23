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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.AbstractTextElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;

/**
 * 
 * Description:<br>
 * This class implements a rich text form element based on the TinyMCE
 * javascript library.
 * 
 * <P>
 * Initial Date: 21.04.2009 <br>
 * 
 * @author gnaegi
 */
public class RichTextElementImpl extends AbstractTextElement implements
		RichTextElement, Disposable {
	
	OLog log = Tracing.createLoggerFor(this.getClass());

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.AbstractTextElement#getValue()
	 * The returned value is XSS save and
	 * does not contain executable JavaScript code. If you want to get the raw
	 * user data use the getRawValue() method.
	 */
	@Override
	public String getValue() {
		Filter xssFilter = FilterFactory.getXSSFilter(value.length() + 1);
		return super.getValue(xssFilter);
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.components.form.flexible.elements.RichTextElement#getRawValue()
	 */
	public String getRawValue(){
		return this.value;
	}
	
	

	@Override
	public void setExtDelay(boolean extDelay) {
		component.setExtDelay(extDelay);
	}



	protected RichTextElementComponent component;
	private RichTextConfiguration configuration;
	private WindowBackOffice windowBackOffice;
	
	/**
	 * Constructor for specialized TextElements, i.e. IntegerElementImpl.
	 * 
	 * @param name
	 * @param predefinedValue
	 *            Initial value
	 * @param rows
	 *            the number of lines or -1 to use default value (resizeable)
	 * @param cols
	 *            the number of characters per line or -1 to use 100% of the
	 *            available space
	 * @param form The dispatch ID of the root form that deals with the submit button
	 * @param windowBackOffice The window back office used to properly cleanup code in browser window
	 */
	public RichTextElementImpl(String name, String predefinedValue, int rows,
			int cols, Form form, WindowBackOffice windowBackOffice) {
		this(name, rows, cols, form, windowBackOffice);
		setValue(predefinedValue);
	}

	/**
	 * Constructor for specialized TextElements, i.e. IntegerElementImpl.
	 * 
	 * @param name
	 * @param rows
	 *            the number of lines or -1 to use default value (resizeable)
	 * @param cols
	 *            the number of characters per line or -1 to use 100% of the
	 *            available space
	 * @param form The dispatch ID of the root form that deals with the submit button
	 * @param windowBackOffice The window back office used to properly cleanup code in browser window
	 */
	protected RichTextElementImpl(String name, int rows, int cols, Form rootForm, WindowBackOffice windowBackOffice) {
		super(name);
		this.windowBackOffice = windowBackOffice;
		// initialize the component
		component = new RichTextElementComponent(this, rows, cols);
		// configure tiny (must be after component initialization)
		// init editor on our form element
		if (GUIInterna.isLoadPerformanceMode()) {
			configuration = new RichTextConfiguration(
				DISPPREFIX+rootForm.getReplayableDispatchID(getComponent()),
				rootForm.getDispatchFieldId()
			);
		} else {
			configuration = new RichTextConfiguration(
				getFormDispatchId(),
				rootForm.getDispatchFieldId()
			);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String paramId = String.valueOf(component.getFormDispatchId());
		String submitValue = getRootForm().getRequestParameter(paramId);
		if (submitValue != null) {
			setValue(submitValue);
			// don't re-render component, value in GUI already correct
			component.setDirty(false);
		} 
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.RichTextElement#getRichTextConfiguration()
	 */
	public RichTextConfiguration getEditorConfiguration() {
		return configuration;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#getFormItemComponent()
	 */
	protected Component getFormItemComponent() {
		return component;
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	public void dispose() {
		// cleanup stuff in the configuration (base url maper)
		if (configuration != null) {
			configuration.dispose();
			configuration = null;
		}
		// remove tiny editor instance from browser
		if (windowBackOffice != null) {
			JSCommand jsCommand = new JSCommand("try{BTinyHelper.removeEditorInstance('" + component.getFormDispatchId() + "');}catch(e){}");
			windowBackOffice.sendCommandTo(jsCommand);
			windowBackOffice = null; // do this only once
		}
	}

	/**
	 * DO NOT USE THE ONCHANGE EVENT with TEXTAREAS!
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#addActionListener(org.olat.core.gui.control.Controller, int)
	 */
	@Override
	public void addActionListener(Controller listener, int action) {
		super.addActionListener(listener, action);
		if (action == FormEvent.ONCHANGE) {
			log.warn("Do not use the onChange event in Textfields / TextAreas as this has often unwanted side effects. " +
					"As the onchange event is only tiggered when you click outside a field or navigate with the tab to the next element " +
					"it will suppress the first attempt to the submit click as by clicking " +
					"the submit button first the onchange event will be triggered and you have to click twice to submit the data. ");
		}
	}
	
	

}
