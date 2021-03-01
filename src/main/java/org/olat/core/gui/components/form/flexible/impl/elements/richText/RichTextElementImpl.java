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

package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.AbstractTextElement;
import org.olat.core.gui.control.Disposable;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
	
	private static final Logger log = Tracing.createLoggerFor(RichTextElementImpl.class);
	private final RichTextElementComponent component;
	private final RichTextConfiguration configuration;
	private TextMode renderingMode;
	
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
	public RichTextElementImpl(String name, String predefinedValue, int rows, int cols, Locale locale) {
		this(name, rows, cols, locale);
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
	protected RichTextElementImpl(String name, int rows, int cols, Locale locale) {
		super(name);
		// initialize the component
		component = new RichTextElementComponent(this, rows, cols);
		component.setTranslator(Util.createPackageTranslator(RichTextElementImpl.class, locale));
		// configure tiny (must be after component initialization)
		// init editor on our form element
		configuration = new RichTextConfiguration(getFormDispatchId(), locale);
	}
	
	@Override
	public String getForId() {
		return isEnabled() ? super.getForId() : null;
	}
	
	/**
	 * The returned value is XSS save and does not contain executable JavaScript
	 * code. Further all value filter of the configuration are applied. If you want
	 * to get the raw user data, use the getRawValue() method.
	 */
	@Override
	public String getValue() {
		String val = getRawValue();
		Filter xssFilter = FilterFactory.getXSSFilter();
		val = xssFilter.filter(val);
		for (Filter filter : configuration.getValueFilters()) {
			val = filter.filter(val);
		}
		return val;
	}
	
	@Override
	public String getValue(Filter filter) {
		String val = getRawValue();
		return filter.filter(val);
	}
	
	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}
	
	protected void setRenderingMode(TextMode mode) {
		renderingMode = mode;
	}

	/**
	 * @return The list of text modes available based on the configuration and
	 * 		the content of the editor.
	 */
	public TextModeState getAvailableTextModes() {
		List<TextMode> textModes = configuration.getTextModes();
		if(textModes.size() == 1) {
			return new TextModeState(TextMode.formatted, textModes);
		}
		
		TextMode minimalMode = TextMode.guess(getRawValue());
		for(Iterator<TextMode> it=textModes.iterator(); it.hasNext(); ) {
			if(it.next().ordinal() < minimalMode.ordinal()) {
				it.remove();
			}
		}
		
		if(minimalMode.ordinal() < textModes.get(0).ordinal()) {
			minimalMode = textModes.get(0);
		}
		
		TextMode currentMode = minimalMode;
		if(component.getCurrentTextMode() != null
				&& component.getCurrentTextMode().ordinal() > currentMode.ordinal()) {
			currentMode = component.getCurrentTextMode();
		} else if(component.getCurrentTextMode() == null) {
			component.setCurrentTextMode(currentMode); 
		}
		return new TextModeState(currentMode, textModes);
	}
	
	protected String getRawValue(TextMode mode) {
		String raw = getRawValue();
		if(mode == TextMode.oneLine) {
			raw = TextMode.toOneLine(raw);
		} else if(mode == TextMode.multiLine) {
			raw = TextMode.toMultiLine(raw);
		}
		return raw;
	}

	/**
	 * This apply a filter to remove some buggy conditional comment
	 * of Word
	 * 
	 * @see org.olat.core.gui.components.form.flexible.elements.RichTextElement#getRawValue()
	 */
	@Override
	public String getRawValue() {
		if(value != null) {
			value = value.replace("<!--[endif] -->", "<![endif]-->");
		}
		return value;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String paramId = component.getFormDispatchId();
		String cmd = getRootForm().getRequestParameter("cmd");
		String submitValue = getRootForm().getRequestParameter(paramId);
		String sizeParamId = "rtinye_".concat(paramId);
		String size = getRootForm().getRequestParameter(sizeParamId);
		String browser = getRootForm().getRequestParameter("browser");
		
		if(StringHelper.containsNonWhitespace(submitValue)) {
			if(renderingMode == TextMode.oneLine) {
				submitValue = TextMode.fromOneLine(submitValue); 
			} else if(renderingMode == TextMode.multiLine) {
				submitValue = TextMode.fromMultiLine(submitValue);
			}
		}
		
		if(StringHelper.containsNonWhitespace(size)) {
			setCurrentHeight(size);
		}
		
		String dispatchUri = getRootForm().getRequestParameter("dispatchuri");
		if("saveinlinedtiny".equals(cmd)) {
			if(submitValue != null) {
				setValue(submitValue);
				getRootForm().fireFormEvent(ureq, new FormEvent(cmd, this, FormEvent.ONCLICK));
			}
		} else if (submitValue != null) {
			setValue(submitValue);
			// don't re-render component, value in GUI already correct
			component.setDirty(false);
		}  else if(cmd != null && !cmd.equals("multiline") && !cmd.equals("formatted")) {
			getRootForm().fireFormEvent(ureq, new FormEvent(cmd, this, FormEvent.ONCLICK));
			component.setDirty(false);
		}

		if(paramId.equals(dispatchUri)) {
			if(TextMode.formatted.name().equals(cmd)) {
				component.setCurrentTextMode(TextMode.formatted);
			} else if(TextMode.multiLine.name().equals(cmd)) {
				component.setCurrentTextMode(TextMode.multiLine);
			} else if(TextMode.oneLine.name().equals(cmd)) {
				component.setCurrentTextMode(TextMode.oneLine);
			} else if(StringHelper.containsNonWhitespace(browser)) {
				component.dispatchRequest(ureq);
				component.setDirty(false);
			}
		}
	}

	@Override
	public RichTextConfiguration getEditorConfiguration() {
		return configuration;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void dispose() {
		// cleanup stuff in the configuration (base url maper)
		if (configuration != null) {
			configuration.dispose();
		}
	}
	
	private void setCurrentHeight(String size) {
		try {
			int height = Double.valueOf(size).intValue();
			component.setCurrentHeight(height);
		} catch (NumberFormatException e) {
			//can happen, don't make a drama of it
		}
	}

	@Override
	public void setNewOriginalValue(String value) {
		if (value == null) value = "";
		original = value;
		originalInitialised = true;
		//the check is made on the raw values instead of the getValue()
		if (getRawValue() != null && !getRawValue().equals(value)) {
			getComponent().setDirty(true);
		}
	}

	/**
	 * DO NOT USE THE ONCHANGE EVENT with TEXTAREAS!
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#addActionListener(org.olat.core.gui.control.Controller, int)
	 */
	@Override
	public void addActionListener(int actionType) {
		super.addActionListener(actionType);
		if (action == FormEvent.ONCHANGE && Settings.isDebuging()) {
			log.warn("Do not use the onChange event in Textfields / TextAreas as this has often unwanted side effects. " +
					"As the onchange event is only tiggered when you click outside a field or navigate with the tab to the next element " +
					"it will suppress the first attempt to the submit click as by clicking " +
					"the submit button first the onchange event will be triggered and you have to click twice to submit the data. ");
		}
	}
	
	public static class TextModeState {
		
		private final TextMode currentMode;
		private final List<TextMode> availableTextModes;
		
		public TextModeState(TextMode currentMode, List<TextMode> availableTextModes) {
			this.currentMode = currentMode;
			this.availableTextModes = availableTextModes;
		}

		public TextMode getCurrentMode() {
			return currentMode;
		}

		public List<TextMode> getAvailableTextModes() {
			return availableTextModes;
		}
	}
}