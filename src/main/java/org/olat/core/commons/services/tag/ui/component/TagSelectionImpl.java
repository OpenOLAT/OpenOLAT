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
package org.olat.core.commons.services.tag.ui.component;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.model.TagRefImpl;
import org.olat.core.commons.services.tag.ui.component.TagSelectionController.TagSelectionEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.SelectionDisplayComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagSelectionImpl extends FormItemImpl implements TagSelection, ControllerEventListener {
	
	private Collator collator;
	private final SelectionDisplayComponent component;
	private final WindowControl wControl;
	
	private CloseableCalloutWindowController calloutCtrl;
	private TagSelectionController selectionCtrl;
	
	private final List<? extends TagInfo> allTags;
	private Set<Long> selectedKeys;
	private Set<String> newTags = new HashSet<>(1);
	private boolean dirtyCheck = true;

	public TagSelectionImpl(WindowControl wControl, String name, List<? extends TagInfo> allTags) {
		super(name);
		this.wControl = wControl;
		this.allTags = allTags;
		this.selectedKeys = new HashSet<>(allTags.stream().filter(TagInfo::isSelected).map(TagInfo::getKey).collect(Collectors.toSet()));
		this.component = new SelectionDisplayComponent(this);
	}

	@Override
	public void setSelectedTags(Collection<? extends TagRef> tags) {
		selectedKeys = tags == null 
				? selectedKeys = new HashSet<>(3)
				: tags.stream().map(TagRef::getKey).collect(Collectors.toSet());
		updateDisplayUI();
	}

	@Override
	public Set<TagRef> getSelectedTags() {
		return selectedKeys.stream()
				.map(TagRefImpl::new)
				.collect(Collectors.toSet());
	}
	
	@Override
	public Set<String> getNewDisplayNames() {
		return newTags;
	}
	
	@Override
	public List<String> getDisplayNames() {
		List<String> tags = new ArrayList<>(selectedKeys.size() + newTags.size());
		
		for (TagInfo tagInfo : allTags) {
			if (selectedKeys.contains(tagInfo.getKey())) {
				tags.add(tagInfo.getDisplayName());
			}
		}
		for (String newTag : newTags) {
			tags.add(newTag);
		}
		
		return tags;
	}
	
	@Override
	public void setDirtyCheck(boolean dirtyCheck) {
		this.dirtyCheck = dirtyCheck;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		collator = Collator.getInstance(getTranslator().getLocale());
		updateDisplayUI();
	}

	@Override
	public void reset() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if (getFormDispatchId().equals(dispatchuri)) {
			doOpenSelection(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(selectionCtrl == source) {
			if (event instanceof TagSelectionEvent) {
				TagSelectionEvent se = (TagSelectionEvent)event;
				selectedKeys = se.getKeys();
				newTags = se.getNewTags();
				calloutCtrl.deactivate();
				updateDisplayUI();
				component.setAriaExpanded(false);
				if (dirtyCheck) {
					Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
					wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
				}
				if (getAction() == FormEvent.ONCHANGE)
					getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
			}
		} else if (calloutCtrl == source) {
			cleanUp();
			component.setAriaExpanded(false);
		}
	}

	private void cleanUp() {
		calloutCtrl = cleanUp(calloutCtrl);
		selectionCtrl = cleanUp(selectionCtrl);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	private void updateDisplayUI() {
		List<String> displayNames = getDisplayNames();
		displayNames.sort((t1, t2) -> collator.compare(t1, t2));
		
		String value = displayNames.stream()
				.map(this::toLabel)
				.collect(Collectors.joining());
		if (!StringHelper.containsNonWhitespace(value)) {
			value = "&nbsp;";
		}
		value = "<span class=\"o_tag_selection_button_tags o_tag_selection_tags\">" + value + "</span>";
		component.setValue(value);
	}
	
	private String toLabel(String displayName) {
		return "<span class=\"o_tag o_selection_tag\">" + displayName + "</span>";
	}
	
	private void doOpenSelection(UserRequest ureq) {
		selectionCtrl = new TagSelectionController(ureq, wControl, allTags, selectedKeys, newTags);
		selectionCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, selectionCtrl.getInitialComponent(),
				getFormDispatchId(), "", true, "", new CalloutSettings(false, CalloutOrientation.bottom, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
		
		component.setAriaExpanded(true);
	}

}
