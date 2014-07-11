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
*/

package org.olat.admin.properties;

import java.util.Iterator;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.properties.PropertyManager;

/**
*  Description:<br>
*
*
* @author Alexander Schneider
*/
public class AdvancedPropertySearchForm extends FormBasicController {
	private FormLink userChooser;
	private TextElement userName;
	private SingleSelection resourceTypeName;
	private TextElement resourceTypeId;
	private TextElement category;
	private TextElement propertyName;
	private FormLink searchButton;
	
	private String[] theKeys;
	private String[] theValues;
	private FormLayoutContainer horizontalLayout;
	
	private UserSearchController usc;
	private Identity identity = null;
	
	private CloseableModalController cmc;

	/**
	 * @param name
	 */
	public AdvancedPropertySearchForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		PropertyManager pm = PropertyManager.getInstance();
		List<String> resourceTypeNames = pm.getAllResourceTypeNames();
		int size = resourceTypeNames.size();
		theKeys = new String[size+1];
		theValues = new String[size+1];
		theKeys[0] = "0";
		theValues[0] = "";
		int i = 1;
		for (Iterator<String> iter = resourceTypeNames.iterator(); iter.hasNext(); i++) {
			theKeys[i] = Integer.toString(i);
			theValues[i] = iter.next();
		}
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		int c = 0;
		
		if (userName.getValue().length()>0) {
			c++;
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			identity = secMgr.findIdentityByName(userName.getValue());
	 		if (identity == null){
	 					userName.setErrorKey ("error.search.form.nousername", null);
	 					return false;
	 		}
		}
		
		if (resourceTypeName.getSelected()>0) c++;
		if (resourceTypeId.getValue().length()>0) c++;
		if (category.getValue().length()>0) c++;
		if (propertyName.getValue().length()>0) c++;
		
		if (c==0) {
			showInfo("error.search.form.notempty");
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("header.advancedsearchform");
		//setFormDescription("xx");
		
		horizontalLayout = FormLayoutContainer.createHorizontalFormLayout("userChooser", getTranslator());
		formLayout.add(horizontalLayout);
		
		horizontalLayout.setLabel("searchform.username", null);
		userName = uifactory.addTextElement("userName", null, 60, "", horizontalLayout);	
		userChooser = uifactory.addFormLink("choose", horizontalLayout);
		
		resourceTypeName = uifactory.addDropdownSingleselect("resourceTypeName", "searchform.resoursetypename", formLayout, theKeys, theValues, null);
		resourceTypeId = uifactory.addTextElement("resourceTypeId", "searchform.resourcetypeid", 60, "", formLayout);
		category = uifactory.addTextElement("category", "searchform.category", 60, "", formLayout);
		propertyName = uifactory.addTextElement("propertyName", "searchform.propertyname", 60, "", formLayout);

		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.addActionListener(FormEvent.ONCLICK);
		
		resourceTypeId.setRegexMatchCheck("\\d*", "error.search.form.onlynumbers");
	}
	
	@Override
	protected void  formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == userChooser) {
			usc = new UserSearchController(ureq, getWindowControl(), false);
			listenTo(usc);

			cmc = new CloseableModalController(
					getWindowControl(),
					translate("close"),
					usc.getInitialComponent()
			);
			
			listenTo(cmc);
			cmc.activate();
		} else if (source == searchButton) {
			source.getRootForm().submit(ureq);			
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == usc && event.getCommand().equals("IdentityFound")){
			SingleIdentityChosenEvent uce = (SingleIdentityChosenEvent) event;
			identity = uce.getChosenIdentity();
			userName.setValue(identity.getName());
			cmc.deactivate();
		} 
	}
	
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub	
	}

	public String getPropertyName() {
		return propertyName.getValue();
	}

	public String getCategory() {
		return category.getValue();
	}

	public String getResourceTypeId() {
		return resourceTypeId.getValue();
	}

	public String getResourceTypeName() {
		return theValues[resourceTypeName.getSelected()];
	}

	public String getUserName() {
		return userName.getValue();
	}
	
	protected Identity getIdentity() {
		return identity;
	}
}
