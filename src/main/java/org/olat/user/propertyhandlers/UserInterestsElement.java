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
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;

/**
 * <h3>Description:</h3>
 * 
 * The form element for user interests.
 * 
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com, srosse
 */
public class UserInterestsElement extends FormItemImpl implements FormItemCollection, ControllerEventListener {
	
	private String selectedInterestsIDs;
	
	private FormLink editLink;
	private final UserInterestsComponent component;

	private CloseableModalController modalController;
	private UserInterestsController userInterestsController;
	
	/**
	 * Creates this form element.
	 * 
	 * @param name Name of this form element.
	 * @param selectedInterestsIDs The IDs of the interests the user has chosen, separated by colons.
	 * @param availableUserInterests The available user interests, as configured in the system.
	 */
	public UserInterestsElement(String name, String selectedInterestsIDs, Locale locale) {
		super(name);
		setTranslator(Util.createPackageTranslator(UserInterestsPropertyHandler.PACKAGE_UINTERESTS, locale, null));
		component = new UserInterestsComponent(this);
		setSelectedInterestsIDs(selectedInterestsIDs);
	}
	
	protected FormLink getEditLink() {
		return editLink;
	}
	
	protected List<String> getUserInterests() {
		if(getSelectedInterestsIDs() == null || getSelectedInterestsIDs().isEmpty()) {
			return new ArrayList<>();
		}
		
		String[] userInterestsIDs = getSelectedInterestsIDs().split(":");
		List<String> sortedUserInterestsIDs = new Vector<>();
		for (String id : userInterestsIDs) {
			if (!id.equals("")) {
				sortedUserInterestsIDs.add(id);
			}
		}
		
		Collections.sort(sortedUserInterestsIDs, new Comparator<String> () {
			public int compare(String id0, String id1) {
				String[] levels0 = id0.split("\\.");
				String[] levels1 = id1.split("\\.");
				Integer level00 = Integer.parseInt(levels0[0]);
				Integer level01 = Integer.parseInt(levels0[1]);
				Integer level10 = Integer.parseInt(levels1[0]);
				Integer level11 = Integer.parseInt(levels1[1]);
				
				if (level00.equals(level10)) {
					return level01.compareTo(level11);
				} else {
					return level00.compareTo(level10);
				}
			}
		});
		
		List<String> sortedUserInterests = new ArrayList<>();
		for (String id : sortedUserInterestsIDs) {
			sortedUserInterests.add(getTranslator().translate(UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX + id));
		}
		
		return sortedUserInterests;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> items = new ArrayList<>(1);
		if(editLink != null) {
			items.add(editLink);
		}
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		return null;
	}

	@Override
	public void setRootForm(Form rootForm) {
		String dispatchId = component.getDispatchID();
		editLink = new FormLinkImpl(dispatchId + "_searchButton", "editInterests", "edit", Link.BUTTON);
		editLink.setTranslator(getTranslator());
		editLink.setIconLeftCSS("o_icon o_icon_edit");
		super.setRootForm(rootForm);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(editLink != null && editLink.getFormDispatchId().equals(dispatchuri)) {
			doEdit(ureq);
		}
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(modalController == source) {
			userInterestsController = null;
			modalController = null;
		} else if(userInterestsController == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setSelectedInterestsIDs(userInterestsController.getSelectedInterests());
				getComponent().setDirty(true);
			}
			modalController.deactivate();
			userInterestsController = null;
			modalController = null;
		}
	}

	private void doEdit(UserRequest ureq) {
		ChiefController chief = Windows.getWindows(ureq).getChiefController(ureq);
		WindowControl wControl = chief.getWindowControl();
		if (wControl != null) {
			List<UserInterestsCategory> availableUserInterests = UserInterestsPropertyHandler.loadAvailableUserInterests();
			userInterestsController = new UserInterestsController(ureq, wControl, getSelectedInterestsIDs(), availableUserInterests);
			userInterestsController.addControllerListener(this);
			String title = getTranslator().translate("userinterests.title");
			modalController = new CloseableModalController(wControl, "close", userInterestsController.getInitialComponent(), true, title);
			modalController.activate();
		}
		
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void reset() {
		// Do nothing.	
	}

	@Override
	protected void rootFormAvailable() {
		if(editLink != null && editLink.getRootForm() != getRootForm()) {
			editLink.setRootForm(getRootForm());
		}
	}

	/**
	 * Returns the IDs of the selected user interests.
	 * 
	 * @return The IDs of the selected user interests.
	 */
	public String getSelectedInterestsIDs() {
		return selectedInterestsIDs;
	}

	/**
	 * Sets the IDs of the selected user interests.
	 * 
	 * @param selectedInterestsIDs The IDs of the selected user interests.
	 */
	public void setSelectedInterestsIDs(String selectedInterestsIDs) {
		this.selectedInterestsIDs = selectedInterestsIDs;
	}
}
