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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.addremove.AddRemoveComponent;
import org.olat.core.gui.components.addremove.AddRemoveEvent;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.AddRemoveElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.util.CodeHelper;

/**
 * Initial date: Nov 4, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class AddRemoveElementImpl extends FormItemImpl implements AddRemoveElement {

	public static final String COMMAND_ADD = "add";
	public static final String COMMAND_REMOVE = "remove";
	public static final String COMMAND_RESET = "reset";
	
	public static enum AddRemoveMode { THREE_STATE, TWO_STATE };
	
	private final AddRemoveComponent addRemoveComponent;
	private final FormLink addLink;
	private final FormLink removeLink;
	
	private AddRemoveMode addRemoveMode = AddRemoveMode.THREE_STATE;
	
	private boolean showText;
	private boolean addSelected;
	private boolean removeSelected;
	
	private String addActiveCssClass = "o_addremove_add_active";
	private String addTitle;
	private String addText = "";
	private String addIcon = "o_icon o_icon-fw o_icon_addremove_add";
	
	private String removeActiveCssClass = "o_addremove_remove_active";
	private String removeTitle;
	private String removeText = "";
	private String removeIcon = "o_icon o_icon-fw o_icon_addremove_remove";
	
	
	public AddRemoveElementImpl(String name, int presentation) {
		super(name);
		
		long idPrefix = CodeHelper.getRAMUniqueID();
		
		addRemoveComponent = new AddRemoveComponent(this, idPrefix + "_" + name + "_component");
		
		addLink = new FormLinkImpl(idPrefix + "_" + name +"_add", COMMAND_ADD, "", presentation | Link.NONTRANSLATED);
		removeLink = new FormLinkImpl(idPrefix + "_" + name +"_remove", COMMAND_REMOVE, "", presentation | Link.NONTRANSLATED);
	}
	
	@Override
	public FormLink getAddLink() {
		return addLink;
	}

	@Override
	public FormLink getRemoveLink() {
		return removeLink;
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(getRootForm().hasAlreadyFired()){
			//dispatchFormRequest did fire already
			//in this case we do not try to fire the general events
			return;
		}
		
		String dispatchuri = getRootForm().getRequestParameter("dispatchuri");
		dispatchuri.replace(FormBaseComponentIdProvider.DISPPREFIX, "");
		
		if(addLink != null && addLink.getFormDispatchId().equals(dispatchuri)) {
			if (!isAddSelected()) {
				selectAdd();
				getRootForm().fireFormEvent(ureq, new FormEvent(AddRemoveEvent.ADD_EVENT, this, FormEvent.ONCHANGE));
			} else if (addRemoveMode == AddRemoveMode.THREE_STATE) {
				reset();
				getRootForm().fireFormEvent(ureq, new FormEvent(AddRemoveEvent.RESET_EVENT, this, FormEvent.ONCHANGE));
			}
		} else if (removeLink != null && removeLink.getFormDispatchId().equals(dispatchuri)) {
			if (!isRemoveSelected()) {
				selectRemove();
				getRootForm().fireFormEvent(ureq, new FormEvent(AddRemoveEvent.REMOVE_EVENT, this, FormEvent.ONCHANGE));
			} else if (addRemoveMode == AddRemoveMode.THREE_STATE) {
				reset();
				getRootForm().fireFormEvent(ureq, new FormEvent(AddRemoveEvent.RESET_EVENT, this, FormEvent.ONCHANGE));
			}
		}
	}
	
	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		// TODO Auto-generated method stub
		super.doDispatchFormRequest(ureq);
	}
	
	@Override
	public boolean isAddSelected() {
		return addSelected;
	}

	@Override
	public boolean isRemoveSelected() {
		return removeSelected;
	}

	@Override
	public boolean isNoneSelected() {
		return !addSelected && !removeSelected;
	}
	
	@Override
	public Boolean getSelection() {
		if (isAddSelected()) {
			return true;
		} else if (isRemoveSelected()) {
			return false;
		} else {
			return null;
		}
	}
	
	@Override
	public void setSelection(Boolean selection) {
		if (selection == null) {
			reset();
		} else if (selection) {
			selectAdd();
		} else {
			selectRemove();
		}
	}

	@Override
	public void selectAdd() {
		addSelected = true;
		removeSelected = false;
		
		addLink.setElementCssClass(addActiveCssClass);
		removeLink.setElementCssClass(null);
	}

	@Override
	public void selectRemove() {
		addSelected = false;
		removeSelected = true;
		
		addLink.setElementCssClass(null);
		removeLink.setElementCssClass(removeActiveCssClass);
	}

	@Override
	public void reset() {
		addSelected = false;
		removeSelected = false;
		
		
		addLink.setElementCssClass(null);
		removeLink.setElementCssClass(null);
	}
	
	@Override
	public void setShowText(boolean showText) {
		this.showText = showText;	
	}

	@Override
	public boolean isTextShown() {
		return this.showText;
	}

	@Override
	public void setAddActiveCssClass(String cssClass) {
		this.addActiveCssClass = cssClass;
	}

	@Override
	public String getAddActiveCssClass() {
		return this.addActiveCssClass;
	}

	@Override
	public void setRemoveActiveCssClass(String cssClass) {
		this.removeActiveCssClass = cssClass;
	}

	@Override
	public String getRemoveActiveCssClass() {
		return this.removeActiveCssClass;
	}

	@Override
	public void setAddText(String text) {
		this.addText = text;
	}

	@Override
	public String getAddText() {
		return this.addText;
	}

	@Override
	public void setAddTitle(String title) {
		this.addTitle = title;
	}

	@Override
	public String getAddTitle() {
		return this.addTitle;
	}

	@Override
	public void setRemoveText(String text) {
		this.removeText = text;		
	}

	@Override
	public String getRemoveText() {
		return this.removeText;
	}

	@Override
	public void setRemoveTitle(String title) {
		this.removeTitle = title;
	}

	@Override
	public String getRemoveTitle() {
		return this.removeTitle;
	}
	
	@Override
	public void setAddIcon(String icon) {
		this.addIcon = "o_icon o_icon-fw " + icon;
	}
	
	@Override
	public String getAddIcon() {
		return addIcon;
	}
	
	@Override
	public void setRemoveIcon(String icon) {
		this.removeIcon = "o_icon o_icon-fw " + icon;
	}
	
	@Override
	public String getRemoveIcon() {
		return removeIcon;
	}

	@Override
	protected Component getFormItemComponent() {
		return addRemoveComponent;
	}
	
	@Override
	public AddRemoveMode getDisplayMode() {
		return addRemoveMode;
	}
	
	@Override
	public void setAddRemoveMode(AddRemoveMode addRemoveMode) {
		this.addRemoveMode = addRemoveMode;
		
		// Reset the state, because it cannot be identified, whether to keep the "add" or "remove" state in the checkbox
		reset();
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		addLink.setEnabled(isEnabled);
		removeLink.setEnabled(isEnabled);
	}
	
	@Override
	public void setAddEnabled(boolean addEnabled) {
		addLink.setEnabled(addEnabled);
	}
	
	@Override
	public boolean isAddEnabled() {
		return addLink.isEnabled();
	}
	
	@Override
	public void setRemoveEnabled(boolean removeEnabled) {
		removeLink.setEnabled(removeEnabled);
	}
	
	@Override
	public boolean isRemoveEnabled() {
		return removeLink.isEnabled();
	}

	@Override
	protected void rootFormAvailable() {
		// If enabled or not must be set now in case it was set during construction time
		addRemoveComponent.setEnabled(isEnabled());
		addRemoveComponent.setTranslator(getTranslator()); // TODO Alex Ask Stephane: Necessary? 
		
		if (addLink.getRootForm() != getRootForm()) {
			addLink.setRootForm(getRootForm());
		}
		if (removeLink.getRootForm() != getRootForm()) {
			removeLink.setRootForm(getRootForm());
		}
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> itemList = new ArrayList<>();
		itemList.add(addLink);
		itemList.add(removeLink);
		return itemList;
	}

	@Override
	public FormItem getFormComponent(String name) {
		for(FormItem item : getFormItems()) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		
		return null;
	}
}
