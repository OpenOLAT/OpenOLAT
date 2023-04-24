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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.model.TagRefImpl;
import org.olat.core.commons.services.tag.ui.component.TagSelectionController.TagSelectionEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
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
public class TagSelectionImpl extends FormItemImpl implements TagSelection, FormItemCollection, ControllerEventListener {
	
	private Collator collator;
	private final TagSelectionComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>(1);
	private final FormLink button;
	
	private CloseableCalloutWindowController calloutCtrl;
	private TagSelectionController selectionCtrl;
	
	private final List<? extends TagInfo> allTags;
	private final Set<Long> initialSelectedKeys;
	private Set<Long> selectedKeys;
	private Set<String> newTags = new HashSet<>(1);

	public TagSelectionImpl(WindowControl wControl, String name, List<? extends TagInfo> allTags) {
		super(name);
		this.wControl = wControl;
		this.allTags = allTags;
		this.initialSelectedKeys = allTags.stream().filter(TagInfo::isSelected).map(TagInfo::getKey).collect(Collectors.toSet());
		this.selectedKeys = new HashSet<>();
		this.selectedKeys.addAll(initialSelectedKeys);
		this.component = new TagSelectionComponent(name, this);
		
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_tl";
		button = new FormLinkImpl(id, id, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_tag_selection_button");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(id, button);
		rootFormAvailable(button);
	}

	@Override
	public void setSelectedTags(Collection<? extends TagRef> tags) {
		selectedKeys = tags == null 
				? selectedKeys = new HashSet<>(3)
				: tags.stream().map(TagRef::getKey).collect(Collectors.toSet());
		updateButtonUI();
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

	public FormLink getButton() {
		return button;
	}
	
	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(button);
		collator = Collator.getInstance(getTranslator().getLocale());
		updateButtonUI();
	}
	
	private final void rootFormAvailable(FormItem item) {
		if (item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}

	@Override
	public void reset() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(button != null && button.getFormDispatchId().equals(dispatchuri)) {
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
				updateButtonUI();
				getFormItemComponent().setDirty(true);
				Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
				wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
			}
		} else if (calloutCtrl == source) {
			cleanUp();
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
	
	private void updateButtonUI() {
		List<String> displayNames = getDisplayNames();
		displayNames.sort((t1, t2) -> collator.compare(t1, t2));
		
		String linkTitle = displayNames.stream()
				.map(this::toLabel)
				.collect(Collectors.joining());
		if (!StringHelper.containsNonWhitespace(linkTitle)) {
			linkTitle = "&nbsp;";
		}
		linkTitle = "<span class=\"o_tag_selection_button_tags o_tag_selection_tags\">" + linkTitle + "</span>";
		button.setI18nKey(linkTitle);
	}
	
	private String toLabel(String displayName) {
		return "<span class=\"o_tag o_selection_tag o_tag_in_button\">" + displayName + "</span>";
	}
	
	private void doOpenSelection(UserRequest ureq) {
		selectionCtrl = new TagSelectionController(ureq, wControl, allTags, initialSelectedKeys, selectedKeys, newTags);
		selectionCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, selectionCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false, CalloutOrientation.bottom, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}

}
