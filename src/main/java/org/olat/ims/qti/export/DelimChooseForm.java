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
* <p>
*/ 

package org.olat.ims.qti.export;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Initial Date: June 07, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class DelimChooseForm extends FormBasicController {
	private TextElement separatedByEl;
	private TextElement embeddedByEl;
	private TextElement escapedByEl;
	private TextElement carriageReturnEl;
	private TextElement fileNameSuffixEl;
	private SelectionElement taglessMattext;
	String sep, emb, esc, car, suf;
	public DelimChooseForm(UserRequest ureq, WindowControl wControl, String sep, String emb, String esc, String car, String suf) {
		super(ureq, wControl);
		this.sep = sep;
		this.emb = emb;
		this.esc = esc;
		this.car = car;
		this.suf = suf;
		initForm(ureq);
	}

	public String getSeparatedBy(){
		return separatedByEl.getValue();
	}
	
	public String getEmbeddedBy(){
		return embeddedByEl.getValue();
	}
	
	public String getEscapedBy(){
		return escapedByEl.getValue();
	}
	
	public String getCarriageReturn(){
		return carriageReturnEl.getValue();
	}
	
	public String getFileNameSuffix(){
		return fileNameSuffixEl.getValue();
	}

	public boolean isTagless(){
		return taglessMattext.isSelected(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		separatedByEl = uifactory.addTextElement("separatedby", "form.separatedby", 50, sep, formLayout);
		separatedByEl.setDisplaySize(5);
		separatedByEl.setExampleKey("form.separatedby.ex", null);
		
		embeddedByEl = uifactory.addTextElement("embeddedby", "form.embeddedby", 1, emb, formLayout);
		embeddedByEl.setDisplaySize(2);
		embeddedByEl.setExampleKey("form.embeddedby.ex", null);
		
		escapedByEl = uifactory.addTextElement("escapedby", "form.escapedby", 50, esc, formLayout);
		escapedByEl.setDisplaySize(5);
		escapedByEl.setExampleKey("form.escapedby.ex", null);
		
		carriageReturnEl = uifactory.addTextElement("carriagereturn", "form.carriagereturn", 50, car, formLayout);
		carriageReturnEl.setDisplaySize(5);
		carriageReturnEl.setExampleKey("form.carriagereturn.ex", null); 
		
		uifactory.addSpacerElement("spacer1", formLayout, false);
	
		fileNameSuffixEl = uifactory.addTextElement("filenamesuffix", "form.filenamesuffix", 50, suf, formLayout);
		fileNameSuffixEl.setDisplaySize(5);
		fileNameSuffixEl.setExampleKey("form.filenamesuffix.ex", null); 
		
		taglessMattext = uifactory.addCheckboxesVertical("taglessmattext", "form.taglessmattext", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		
		uifactory.addFormSubmitButton("form.finished", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

}
