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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.math.MathLiveElement;
import org.olat.core.gui.components.math.MathLiveVirtualKeyboardMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.MathElement;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 01.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveEditorController extends FormBasicController implements PageElementEditorController {

	private static final String START_MATH_TAG= "<math>";
	private static final String END_MATH_TAG= "</math>";
	
	private MathLiveElement mathItem;
	
	private MathElement mathPart;
	private final PageElementStore<MathElement> store;
	
	public MathLiveEditorController(UserRequest ureq, WindowControl wControl, MathElement mathPart, PageElementStore<MathElement> store) {
		super(ureq, wControl, "math_editor");
		this.mathPart = mathPart;
		this.store = store;
		
		initForm(ureq);

		setBlockLayoutClass(mathPart.getMathSettings());
	}

	private void setBlockLayoutClass(MathSettings mathSettings) {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(mathSettings, false));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String cmpId = "math-" + CodeHelper.getRAMUniqueID() + "h";
		String content = mathPart.getContent();
		if(content != null && content.startsWith(START_MATH_TAG) && content.endsWith(END_MATH_TAG)) {
			content = content.substring(START_MATH_TAG.length());
			content = content.substring(0, content.length() - END_MATH_TAG.length());
		}
		mathItem = uifactory.addMathLiveElement(cmpId, null, content, formLayout);
		mathItem.setVirtualKeyboardMode(MathLiveVirtualKeyboardMode.onfocus);
		mathItem.setSendOnBlur(true);
		
		((FormLayoutContainer)formLayout).contextPut("mathCmpId", cmpId);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof MathLiveInspectorController && event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement().equals(mathPart) && changePartEvent.getElement() instanceof MathElement mathElement) {
				mathPart = mathElement;
				setBlockLayoutClass(mathPart.getMathSettings());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(mathItem == source) {
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doSave(UserRequest ureq) {
		String content = mathItem.getValue();
		mathPart.setContent(content);
		mathPart = store.savePageElement(mathPart);
		fireEvent(ureq, new ChangePartEvent(mathPart));
	}
}
