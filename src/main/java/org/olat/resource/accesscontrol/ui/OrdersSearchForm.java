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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.StateEntry;
import org.olat.core.id.context.StateMapped;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * The mask to search for orders
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersSearchForm extends FormBasicController {

	private DateChooser toEl;
	private DateChooser fromEl;
	private TextElement refNoEl;
	
	public OrdersSearchForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	
	public OrdersSearchForm(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		refNoEl = uifactory.addTextElement("order.nr", "order.nr", 32, "", formLayout);
		fromEl = uifactory.addDateChooser("order.from", null, formLayout);
		toEl = uifactory.addDateChooser("order.to", null, formLayout);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("searchLayout", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("search", "search", buttonCont);
	}

	public StateEntry getStateEntry() {
		StateMapped entry = new StateMapped();
		Long refNo = getRefNo();
		if(refNo != null) {
			entry.getDelegate().put("refNo", refNo.toString());
		}
		String fromStr = fromEl.getValue();
		if(StringHelper.containsNonWhitespace(fromStr)) {
			entry.getDelegate().put("from", fromStr);
		}
		String toStr = toEl.getValue();
		if(StringHelper.containsNonWhitespace(toStr)) {
			entry.getDelegate().put("to", toStr);
		}
		return entry;
	}
	
	public boolean setStateEntry(StateEntry state) {
		boolean changed = false;
		
		if(state instanceof StateMapped) {
			StateMapped map = (StateMapped)state;
			String refNo = map.getDelegate().get("refNo");
			if(StringHelper.containsNonWhitespace(refNo)) {
				refNoEl.setValue(refNo);
				changed = true;
			}
			
			String fromStr = map.getDelegate().get("from");
			if(StringHelper.containsNonWhitespace(fromStr)) {
				fromEl.setValue(fromStr);
				changed = true;
			}
			
			String toStr = map.getDelegate().get("to");
			if(StringHelper.containsNonWhitespace(toStr)) {
				toEl.setValue(toStr);
				changed = true;
			}
		}
		
		return changed;
	}
	
	public Long getRefNo() {
		String no = refNoEl.getValue();
		if(StringHelper.containsNonWhitespace(no)) {
			try {
				return Long.parseLong(no);
			} catch (NumberFormatException e) {
				//
			}
		}
		return null;
	}

	public Date getTo() {
		return toEl.getDate();
	}

	public Date getFrom() {
		return fromEl.getDate();
	}
	
	public void setFrom(Date from) {
		fromEl.setDate(from);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
