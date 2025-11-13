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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.render.Renderer;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagSelectionController extends FormBasicController {
	
	private static final String CMD_CREATE = "create";

	private final Comparator<TagItem> comparator;
	private TextElement searchTermEl;
	private FormLink searchResetLink;
	private FormLink createLink;
	private FormLayoutContainer tagsCont;
	private FormLink applyButton;
	
	private final List<? extends TagInfo> allTags;
	private final Set<Long> currentSelectionKeys;
	private final Set<String> initialNewTags;
	private List<TagItem> tagItems;
	private int counter;

	public TagSelectionController(UserRequest ureq, WindowControl wControl, List<? extends TagInfo> allTags,
			Set<Long> currentSelectionKeys, Set<String> initialNewTags) {
		super(ureq, wControl, "tag_selection", Util.createPackageTranslator(TagUIFactory.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(ObjectSelectionController.class, ureq.getLocale(), getTranslator()));
		this.allTags = allTags;
		this.currentSelectionKeys = currentSelectionKeys;
		this.initialNewTags = initialNewTags;
		this.comparator = createComparator();
		
		initForm(ureq);
		doResetSearch();
	}
	
	private Comparator<TagItem> createComparator() {
		Collator collator = Collator.getInstance(getLocale());
		return Comparator
				.comparing(
						TagItem::isSelected,
						Comparator.reverseOrder())
				.thenComparing(Comparator.comparing(
						TagItem::getCount,
						Comparator.reverseOrder())
				.thenComparing((i1, i2) -> collator.compare(i1.getDisplayValue(), i2.getDisplayValue()))
				);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchTermEl = uifactory.addTextElement("search.term", null, 100, "", formLayout);
		searchTermEl.setDomReplacementWrapperRequired(false);
		searchTermEl.setElementCssClass("o_search_term");
		searchTermEl.setAriaLabel(translate("search.term.aria"));
		searchTermEl.setAriaRole(TextElement.ARIA_ROLE_SEARCHBOX);
		searchTermEl.setAutocomplete("off");
		searchTermEl.addActionListener(FormEvent.ONKEYUP);
		
		searchResetLink = uifactory.addFormLink("search.reset", "", null, formLayout, Link.BUTTON_SMALL | Link.NONTRANSLATED);
		searchResetLink.setDomReplacementWrapperRequired(true);
		searchResetLink.setElementCssClass("o_reset_search");
		searchResetLink.setTitle(translate("search.reset"));
		searchResetLink.setIconLeftCSS("o_icon o_icon_remove_filters");
		
		tagsCont = FormLayoutContainer.createCustomFormLayout("tags", getTranslator(), velocity_root + "/tag_selection_tags.html");
		tagsCont.setRootForm(mainForm);
		formLayout.add("tags", tagsCont);
		searchTermEl.setAriaControls(Renderer.getComponentPrefix(tagsCont.getComponent()));
		
		createLink = uifactory.addFormLink("create", CMD_CREATE, "", null, tagsCont,  Link.NONTRANSLATED);
		createLink.setDomReplacementWrapperRequired(true);
		createLink.setVisible(false);
		
		applyButton = uifactory.addFormLink("apply", formLayout, Link.BUTTON);
		applyButton.setElementCssClass("o_selection_apply o_button_primary_light");
		
		initTagLinks();
	}
	
	private void initTagLinks() {
		tagItems = new ArrayList<>(allTags.size() + initialNewTags.size());
		
		for (TagInfo tagInfo : allTags) {
			TagItem tagItem = createTagItem(tagInfo);
			tagItems.add(tagItem);
		}
		for (String newTag : initialNewTags) {
			TagItem tagItem = createNewTagItem(newTag);
			tagItems.add(tagItem);
		}
		
		tagItems.sort(comparator);
		tagsCont.contextPut("tags", tagItems);
	}
	
	private TagItem createTagItem(TagInfo tagInfo) {
		TagItem tagItem = new TagItem();
		tagItem.setKey(tagInfo.getKey());
		tagItem.setDisplayValue(tagInfo.getDisplayName());
		tagItem.setCount(tagInfo.getCount() != null? tagInfo.getCount().longValue(): 0l);
		tagItem.setSelected(currentSelectionKeys.contains(tagInfo.getKey()));
		
		createToggle(tagItem);
		
		return tagItem;
	}
	
	private TagItem createNewTagItem(String tag) {
		TagItem tagItem = new TagItem();
		tagItem.setDisplayValue(tag);
		tagItem.setCount(1l);
		tagItem.setSelected(true);
		
		createToggle(tagItem);
		
		return tagItem;
	}

	private void createToggle(TagItem tagItem) {
		FormToggle toggle = uifactory.addToggleButton("tag_" + counter++, null, null, null, tagsCont);
		toggle.setPresentation(Presentation.CHECK_CUSTOM);
		tagItem.setToggle(toggle);
		toggle.setUserObject(tagItem);
		updateToggleUI(toggle, tagItem);
	}
	
	private void updateToggleUI(FormToggle toggle, TagItem tagItem) {
		toggle.toggle(tagItem.isSelected());
		
		String displayTag = getDisplayTag(tagItem);
		toggle.setToggleOnText(displayTag);
		toggle.setToggleOffText(displayTag);
		toggle.setElementCssClass(getTagLinkCss(tagItem.isSelected()));
	}
	
	private String getDisplayTag(TagItem tagItem) {
		return translate("tag.num.count", StringHelper.escapeHtml(tagItem.getDisplayValue()), String.valueOf(tagItem.getCount()));
	}
	
	private String getTagLinkCss(boolean selected) {
		return selected? "o_tag o_selection_tag o_tag_clickable o_tag_selected": "o_tag o_selection_tag o_tag_clickable";
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == searchResetLink || source == createLink) {
 			super.propagateDirtinessToContainer(source, fe);
 		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (applyButton == source) {
			doApplySelection(ureq);
		} else if (searchTermEl == source) {
			doSearch();
		} else if (searchResetLink == source) {
			doResetSearch();
		} else if (source instanceof FormToggle toggle) {
			doToggleTag(toggle);
		} else if (source instanceof FormLink link) {
			if (CMD_CREATE.equals(link.getCmd())) {
				doCreateTag();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (createLink.isVisible()) {
			// Create a new tag on enter...
			doCreateTag();
		} else {
			//.. or close this selection view.
			doApplySelection(ureq);
		}
	}
	
	private void doApplySelection(UserRequest ureq) {
		Set<Long> selectionKeys = tagItems.stream()
				.filter(item -> item.getKey() != null && item.isSelected())
				.map(TagItem::getKey)
				.collect(Collectors.toSet());
		Set<String> newTags = tagItems.stream()
				.filter(item -> item.getKey() == null && item.isSelected())
				.map(TagItem::getDisplayValue)
				.collect(Collectors.toSet());
		fireEvent(ureq, new TagSelectionEvent(selectionKeys, newTags));
	}
	
	private void doCreateTag() {
		TagItem tagItem = createNewTagItem(searchTermEl.getValue());
		tagItems.add(tagItem);
		tagItems.sort(comparator);
		createLink.setVisible(false);
		doResetSearch();
	}
	
	private void doToggleTag(FormToggle toggle) {
		if (toggle.getUserObject() instanceof TagItem tagItem) {
			boolean selected = !tagItem.isSelected();
			tagItem.setSelected(selected);
			if (selected) {
				tagItem.setCount(tagItem.getCount() + 1);
			} else {
				tagItem.setCount(tagItem.getCount() - 1);
			}
			updateToggleUI(toggle, tagItem);
			
			Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), toggle.getForId());
			mainForm.getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
		}
	}

	private void doSearch() {
		String searchText = searchTermEl.getValue().toLowerCase();
		searchTermEl.getComponent().setDirty(false);
		
		if (StringHelper.containsNonWhitespace(searchText)) {
			tagItems.forEach(item -> item.getToggle().setVisible(item.getDisplayValue().toLowerCase().contains(searchText)));
			if (isTagExists(searchText)) {
				createLink.setVisible(false);
			} else {
				createLink.setI18nKey(translate("create.new.tag", StringHelper.escapeHtml(searchTermEl.getValue())));
				createLink.setVisible(true);
			}
		} else {
			tagItems.forEach(item -> item.getToggle().setVisible(true));
			createLink.setVisible(false);
		}
		
		tagsCont.setDirty(true);
		
		searchResetLink.setVisible(StringHelper.containsNonWhitespace(searchText));
	}
	
	private boolean isTagExists(String searchText) {
		return tagItems.stream().anyMatch(item -> searchText.equals(item.getDisplayValue().toLowerCase()));
	}

	private void doResetSearch() {
		searchTermEl.setValue("");
		doSearch();
		
		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), searchTermEl.getForId());
		mainForm.getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}

	public static final class TagSelectionEvent extends Event {
		
		private static final long serialVersionUID = -3344663878305209936L;
		
		private final Set<Long> keys;
		private final Set<String> newTags;
		
		public TagSelectionEvent(Set<Long> keys, Set<String> newTags) {
			super("tag-selection");
			this.keys = keys;
			this.newTags = newTags;
		}
		
		public Set<Long> getKeys() {
			return keys;
		}

		public Set<String> getNewTags() {
			return newTags;
		}
	}
	
}
