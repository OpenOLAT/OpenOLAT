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
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Initial date: 22 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveComponent extends AbstractComponent {
	
	private static final ComponentRenderer RENDERER = new MathLiveComponentRenderer();
	
	private String value;
	private final MathLiveElementImpl formItem;
	private MathLiveVirtualKeyboardMode virtualKeyboardMode;
	private DomWrapperElement domWrapperElement = DomWrapperElement.span;
	
	public MathLiveComponent(String name) {
		super(name);
		this.formItem = null;
		setDomReplacementWrapperRequired(false);
	}
	
	public MathLiveComponent(String name, MathLiveElementImpl formItem) {
		super(name);
		this.formItem = formItem;
		setDomReplacementWrapperRequired(false);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public MathLiveVirtualKeyboardMode getVirtualKeyboardMode() {
		return virtualKeyboardMode;
	}

	public void setVirtualKeyboardMode(MathLiveVirtualKeyboardMode virtualKeyboardMode) {
		this.virtualKeyboardMode = virtualKeyboardMode;
	}
	
	/**
	 * The DOM element type that is used to wrap this StaticTextElementComponent
	 * DomWrapperElement.p is the default setting
	 * @return
	 */
	public DomWrapperElement getDomWrapperElement() {
		return domWrapperElement;
	}
	
	/**
	 * @param domWrapperElement The DOM wrapper element. 
	 */
	public void setDomWrapperElement(DomWrapperElement domWrapperElement) {
		this.domWrapperElement = domWrapperElement;
	}

	public MathLiveElement getFormItem() {
		return formItem;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		if(StringHelper.containsNonWhitespace(WebappHelper.getMathLiveCdn())) {
			jsa.addRequiredStaticJsFile(WebappHelper.getMathLiveCdn());
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
