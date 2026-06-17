/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReflectionStaticElementImpl extends FormItemImpl implements ReflectionStaticElement {
	
	private static final Logger log = Tracing.createLoggerFor(ReflectionStaticElementImpl.class);
	
	private String textAddOn;
	private final Component cmp;
	private List<TextElement> textElements;
	private boolean formatTextElements = false;
	private ReflectionType reflectionType = ReflectionType.concat;
	
	public ReflectionStaticElementImpl(String name) {
		super(name);
		cmp = new ReflectionStaticComponent(name, this);
	}

	@Override
	public List<TextElement> getTextElements() {
		return textElements;
	}

	@Override
	public void setTextElements(List<TextElement> textElements) {
		this.textElements = textElements;
	}

	@Override
	public boolean isFormatTextElements() {
		return formatTextElements;
	}

	@Override
	public void setFormatTextElements(boolean formatTextElements) {
		this.formatTextElements = formatTextElements;
	}

	@Override
	public String getReflectedValue() {
		if(textElements == null || textElements.isEmpty()) return null;
		
		String val;
		if(reflectionType == ReflectionType.sumInteger) {
			int valInt = 0;
			for(TextElement textElement:textElements) {
				String textVal = textElement.getValue();
				if(StringHelper.containsNonWhitespace(textVal) && StringHelper.isLong(textVal)) {
					try {
						valInt += Integer.parseInt(textElement.getValue());
					} catch (NumberFormatException e) {
						log.warn("", e);
					}
				}
			}
			val = Integer.toString(valInt);
		} else {
			StringBuilder sb = new StringBuilder();
			for(TextElement textElement:textElements) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(textElement.getValue());
			}
			val = sb.toString();
		}
		return val;
	}

	@Override
	public ReflectionType getReflectionType() {
		return reflectionType;
	}

	@Override
	public void setReflectionType(ReflectionType type) {
		reflectionType = type;
	}

	@Override
	public String getTextAddOn() {
		return textAddOn;
	}

	@Override
	public void setTextAddOn(String textAddOn) {
		this.textAddOn = textAddOn;
	}

	@Override
	protected Component getFormItemComponent() {
		return cmp;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(textElements == null || textElements.isEmpty()) return;
		for(TextElement textElement:textElements) {
			textElement.evalFormRequest(ureq);
		}
	}

	@Override
	public void reset() {
		//
	}
}
