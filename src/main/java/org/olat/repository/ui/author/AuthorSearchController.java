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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorSearchController extends FormBasicController implements ExtendedFlexiTableSearchController {

	private static final String[] keys = new String[]{ "my" };
	
	private TextElement id; // only for admins
	private TextElement displayName;
	private TextElement author;
	private TextElement description;
	private SingleSelection types;
	private MultipleSelectionElement ownedResourcesOnlyEl;
	private FormLink searchButton;
	
	private String[] typeKeys;
	private String[] limitTypes;
	private boolean cancelAllowed;
	private boolean enabled = true;
	
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AuthorSearchController(UserRequest ureq, WindowControl wControl, boolean cancelAllowed) {
		super(ureq, wControl, "search");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.cancelAllowed = cancelAllowed;
		initForm(ureq);
	}

	public AuthorSearchController(UserRequest ureq, WindowControl wControl, boolean cancelAllowed, Form form) {
		super(ureq, wControl, LAYOUT_CUSTOM, "search", form);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.cancelAllowed = cancelAllowed;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", leftContainer);
		displayName.setElementCssClass("o_sel_repo_search_displayname");
		displayName.setFocus(true);

		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", leftContainer);
		description.setElementCssClass("o_sel_repo_search_description");

		List<String> typeList = getResources();
		typeKeys = typeList.toArray(new String[typeList.size()]);
		String[] typeValues = getTranslatedResources(typeList);
		types = uifactory.addDropdownSingleselect("cif.type", "cif.type", leftContainer, typeKeys, typeValues, null);

		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);
		
		author = uifactory.addTextElement("cif_author", "cif.author", 255, "", rightContainer);
		author.setElementCssClass("o_sel_repo_search_author");
		
		id = uifactory.addTextElement("cif_id", "cif.id", 128, "", rightContainer);
		id.setElementCssClass("o_sel_repo_search_id");
		
		ownedResourcesOnlyEl = uifactory.addCheckboxesHorizontal("cif_my", "cif.owned.resources.only", rightContainer, keys, new String[]{ "" });
		ownedResourcesOnlyEl.select(keys[0], true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		searchButton.setCustomEnabledLinkCSS("btn btn-primary");
		if(cancelAllowed) {
			uifactory.addFormCancelButton("quick.search", buttonLayout, ureq, getWindowControl());
		}
	}
	
	public void update(SearchEvent se) {
		displayName.setValue(se.getDisplayname());
		id.setValue(se.getId());
		author.setValue(se.getAuthor());
		ownedResourcesOnlyEl.select(keys[0], se.isOwnedResourcesOnly());
		description.setValue(se.getDescription());
		
		String type = se.getType();
		if(StringHelper.containsNonWhitespace(type)) {
			for(String typeKey:typeKeys) {
				if(type.equals(typeKey)) {
					types.select(typeKey, true);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public List<String> getConditionalQueries() {
		return Collections.emptyList();
	}

	/**
	 * @return Return value of ID field.
	 */
	public String getId() {
		return id.getValue();
	}

	/**
	 * @return Display name filed value.
	 */
	public String getDisplayName() {
		return displayName.getValue();
	}

	/**
	 * @return Author field value.
	 */
	public String getAuthor() {
		return author.getValue();
	}

	/**
	 * @return Descritpion field value.
	 */
	public String getDescription() {
		return description.getValue();
	}

	/**
	 * @return Limiting type selections.
	 */
	public String getRestrictedType() {
		if(types.isOneSelected()) {
			return types.getSelectedKey();
		} else if (limitTypes != null && limitTypes.length > 0) {
			return limitTypes[0];
		}
		return null;
	}
	
	public boolean isOwnedResourcesOnly() {
		return ownedResourcesOnlyEl.isAtLeastSelected(1);
	}
	
	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if(!enabled) return true;
		
		if (displayName.isEmpty() && author.isEmpty() && description.isEmpty() && (id != null && id.isEmpty()))	{
			showWarning("cif.error.allempty");
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enabled) {
			fireSearchEvent(ureq);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled) {
			if (source == searchButton) {
				fireSearchEvent(ureq);
			}
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setId(getId());
		e.setAuthor(getAuthor());
		e.setDisplayname(getDisplayName());
		e.setDescription(getDescription());
		e.setType(getRestrictedType());
		e.setOwnedResourcesOnly(isOwnedResourcesOnly());
		fireEvent(ureq, e);
	}

	private String[] getTranslatedResources(List<String> resources) {
		List<String> l = new ArrayList<String>();
		for(String key: resources){
			if(StringHelper.containsNonWhitespace(key)) {
				l.add(translate(key));
			} else {
				l.add("");
			}
		}
		return l.toArray(new String[0]);
	}
	
	private List<String> getResources() {
		List<String> resources = new ArrayList<String>();
		resources.add("");
		for(OrderedRepositoryHandler handler:repositoryHandlerFactory.getOrderRepositoryHandlers()) {
			resources.add(handler.getHandler().getSupportedType());
		}
		return resources;
	}
}