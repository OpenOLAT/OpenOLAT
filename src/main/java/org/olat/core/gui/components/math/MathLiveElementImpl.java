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
package org.olat.core.gui.components.math;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 22 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveElementImpl extends FormItemImpl implements MathLiveElement {
	
	private boolean sendOnBlur;
	private final MathLiveComponent component;
	
	public MathLiveElementImpl(String name) {
		super(name);
		component = new MathLiveComponent(name, this);
	}

	@Override
	public boolean isSendOnBlur() {
		return sendOnBlur;
	}

	@Override
	public void setSendOnBlur(boolean sendOnBlur) {
		this.sendOnBlur = sendOnBlur;
	}

	@Override
	public MathLiveVirtualKeyboardMode getVirtualKeyboardMode() {
		return component.getVirtualKeyboardMode();
	}

	@Override
	public void setVirtualKeyboardMode(MathLiveVirtualKeyboardMode virtualKeyboardMode) {
		component.setVirtualKeyboardMode(virtualKeyboardMode);
	}

	@Override
	public String getValue() {
		return component.getValue();
	}

	@Override
	public void setValue(String value) {
		component.setValue(value);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String cmd = getRootForm().getRequestParameter("cmd");
		String paramId = getFormDispatchId();
		String paramValue = getRootForm().getRequestParameter(paramId);
		if (paramValue != null) {
			setValue(paramValue);
			// mark associated component dirty, that it gets rerendered
			component.setDirty(true);
			if("saveinlinedmathlive".equals(cmd)) {
				getRootForm().fireFormEvent(ureq, new FormEvent(cmd, this, FormEvent.ONCLICK));
			}
		}
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}


}
