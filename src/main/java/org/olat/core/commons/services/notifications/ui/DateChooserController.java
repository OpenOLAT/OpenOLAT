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
package org.olat.core.commons.services.notifications.ui;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;


/**
 * Description:<br>
 * This controller provides a simple date chooser
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CHANGED_EVENT whenever the date has been changed</li>
 * </ul>
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DateChooserController extends FormBasicController {
	private DateChooser dateChooser;
	private Date initDate;
	private FormLink link;
	
	private SingleSelection typeSelection;
	private SelectionValues typeKeys;

	public DateChooserController(UserRequest ureq, WindowControl wControl, Date initDate) {
		super(ureq, wControl);
		this.initDate = initDate;
		
		typeKeys = new SelectionValues();
		typeKeys.add(SelectionValues.entry("all", translate("news.type.all")));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dateChooser = uifactory.addDateChooser("news.since", null, formLayout);

		dateChooser.setDate(initDate);
		dateChooser.addActionListener(FormEvent.ONCHANGE);
		
		typeSelection = uifactory.addDropdownSingleselect("news.type", "news.type", formLayout, typeKeys.keys(), typeKeys.values(), null);
		typeSelection.addActionListener(FormEvent.ONCHANGE);
		typeSelection.select("all", true);
		typeSelection.setVisible(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do here
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		if (dateChooser.hasError() || dateChooser.getDate() == null) {
			dateChooser.setErrorKey("error.date", new String[0]);
			return false;
		}
		return isInputValid;			
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == link && !dateChooser.hasError()) {
			flc.getRootForm().submit(ureq);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == dateChooser && !dateChooser.hasError()) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == typeSelection) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	/**
	 * Get the date that has been chosen.
	 *  
	 * @return The selected date
	 */
	public Date getChoosenDate() {
		return dateChooser.getDate();
	}
	
	public void setDate(Date date) {
		dateChooser.setDate(date);
	}
	
	public String getType() {
		if(typeSelection.isSelected(0) || typeSelection.getKeys().length <= 1 || !typeSelection.isOneSelected()) {
			return null;
		}
		return typeSelection.getSelectedKey();
	}
	
	public void setType(String type) {
		if(StringHelper.containsNonWhitespace(type)) {
			for(String typeKey:typeKeys.keys()) {
				if(type.equals(typeKey)) {
					typeSelection.select(type, true);
					break;
				}
			}
		} else {
			typeSelection.select("all", true);
		}
	}
	
	public void setSubscribers(List<Subscriber> subscribers) {
		String selectedKey = typeSelection.isOneSelected() ? typeSelection.getSelectedKey() : null;
		
		Set<String> types = new HashSet<>();
		for(Subscriber subscriber:subscribers) {
			String type = subscriber.getPublisher().getType();
			types.add(type);
		}
		
		typeKeys = new SelectionValues();
		typeKeys.add(SelectionValues.entry("all", translate("news.type.all")));

		for(String type:types) {
			String typeName = NewControllerFactory.translateResourceableTypeName(type, getLocale());
			typeKeys.add(SelectionValues.entry(type, typeName));
		}
		
		typeSelection.setKeysAndValues(typeKeys.keys(), typeKeys.values(), null);
		
		if(selectedKey != null) {
			//select the current key but check if it still exists
			for(String typeKey:typeKeys.keys()) {
				if(typeKey.equals(selectedKey)) {
					typeSelection.select(selectedKey, true);
					break;
				}
			}
		}
		
		if(!typeSelection.isOneSelected()) {
			typeSelection.select("all", true);
		}
		if(typeKeys.size() == 1) {
			typeSelection.setVisible(false);
		}
	}
}
