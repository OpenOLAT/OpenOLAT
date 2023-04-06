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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
	private static final String CMD_TOGGLE = "toggle";

	private final Comparator<TagItem> comparator;
	private FormLink selectButton;
	private FormLink quickSearchButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;
	private FormLayoutContainer tagsCont;
	private FormLink createLink;
	
	private final List<? extends TagInfo> allTags;
	private final Set<Long> initialSelectedKeys;
	private final Set<Long> selectedKeys;
	private final Set<String> initialNewTags;
	private List<TagItem> tagItems;
	private int counter;


	public TagSelectionController(UserRequest ureq, WindowControl wControl, List<? extends TagInfo> allTags,
			Set<Long> initialSelectedKeys, Set<Long> currentSelectionKeys, Set<String> initialNewTags) {
		super(ureq, wControl, "tag_selection", Util.createPackageTranslator(TagUIFactory.class, ureq.getLocale()));
		this.allTags = allTags;
		this.initialSelectedKeys = initialSelectedKeys;
		this.initialNewTags = initialNewTags;
		this.selectedKeys = new HashSet<>(currentSelectionKeys);
		this.comparator = createComparator();
		
		initForm(ureq);
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
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);
		
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		quickSearchEl.setFocus(true);
		
		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON_SMALL);
		
		tagsCont = FormLayoutContainer.createCustomFormLayout("tags", getTranslator(), velocity_root + "/tag_selection_tags.html");
		tagsCont.setRootForm(mainForm);
		formLayout.add("tags", tagsCont);
		
		createLink = uifactory.addFormLink("create", CMD_CREATE, "", null, tagsCont,  Link.NONTRANSLATED);
		createLink.setDomReplacementWrapperRequired(false);
		createLink.setVisible(false);
		
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
		tagItem.setCount(tagInfo.getCount());
		tagItem.setSelected(selectedKeys.contains(tagInfo.getKey()));
		
		FormLink link = uifactory.addFormLink("tag_" + counter++, CMD_TOGGLE, getDisplayTag(tagItem), null, tagsCont,  Link.NONTRANSLATED);
		link.setElementCssClass(getTagLinkCss(selectedKeys.contains(tagInfo.getKey())));
		link.setDomReplacementWrapperRequired(false);
		tagItem.setLink(link);
		link.setUserObject(tagItem);
		return tagItem;
	}
	
	private TagItem createNewTagItem(String tag) {
		TagItem tagItem = new TagItem();
		tagItem.setDisplayValue(tag);
		tagItem.setCount(Long.valueOf(1));
		tagItem.setSelected(true);
		
		FormLink link = uifactory.addFormLink("tag_" + counter++, CMD_TOGGLE, getDisplayTag(tagItem), null, tagsCont,  Link.NONTRANSLATED);
		link.setElementCssClass(getTagLinkCss(true));
		link.setDomReplacementWrapperRequired(false);
		tagItem.setLink(link);
		link.setUserObject(tagItem);
		return tagItem;
	}
	
	private String getDisplayTag(TagItem tagItem) {
		long effCount = tagItem.getCount().longValue();
		
		Long key = tagItem.getKey();
		if (key != null) {
			if (selectedKeys.contains(key) && !initialSelectedKeys.contains(key)) {
				effCount++;
			} else if (!selectedKeys.contains(key) && initialSelectedKeys.contains(key)) {
				effCount--;
			}
		}
		
		return translate("tag.count", tagItem.getDisplayValue(), String.valueOf(effCount));
	}
	
	private String getTagLinkCss(boolean selected) {
		return selected? "o_tag o_selection_tag o_tag_clickable o_tag_selected": "o_tag o_selection_tag o_tag_clickable";
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source != quickSearchEl && source != quickSearchButton && source != resetQuickSearchButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (selectButton == source) {
			doSelect(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch();
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch();
		} else if (source instanceof FormLink link) {
			if (CMD_CREATE.equals(link.getCmd())) {
				doCreateTag();
			} else if (CMD_TOGGLE.equals(link.getCmd())) {
				doToggleTag(link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSelect(ureq);
	}
	
	private void doSelect(UserRequest ureq) {
		Set<String> newTags = tagItems.stream()
				.filter(item -> item.getKey() == null)
				.map(TagItem::getDisplayValue)
				.collect(Collectors.toSet());
		fireEvent(ureq, new TagSelectionEvent(selectedKeys, newTags));
	}
	
	private void doCreateTag() {
		TagItem tagItem = createNewTagItem(quickSearchEl.getValue());
		tagItems.add(tagItem);
		tagItems.sort(comparator);
	}
	
	private void doToggleTag(FormLink link) {
		if (link.getUserObject() instanceof TagItem tagItem) {
			boolean selected = !tagItem.isSelected();
			if (tagItem.getKey() != null) {
				tagItem.setSelected(selected);
				if (selected) {
					selectedKeys.add(tagItem.getKey());
				} else {
					selectedKeys.remove(tagItem.getKey());
				}
				link.setI18nKey(getDisplayTag(tagItem));
				link.setElementCssClass(getTagLinkCss(selected));
			} else {
				tagItems.removeIf(item -> item.getKey() == null && item.getLink() == link);
				tagsCont.setDirty(true);
			}
		}
	}

	private void doQuickSearch() {
		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);
		
		if (StringHelper.containsNonWhitespace(searchText)) {
			tagItems.forEach(item -> item.getLink().setVisible(item.getDisplayValue().toLowerCase().contains(searchText)));
			if (isTagExists(searchText)) {
				createLink.setVisible(false);
			} else {
				createLink.setI18nKey(translate("create.new.tag", quickSearchEl.getValue()));
				createLink.setVisible(true);
			}
		} else {
			tagItems.forEach(item -> item.getLink().setVisible(true));
			createLink.setVisible(false);
		}
		
		tagsCont.setDirty(true);
	}
	
	private boolean isTagExists(String searchText) {
		return tagItems.stream().anyMatch(item -> searchText.equals(item.getDisplayValue().toLowerCase()));
	}

	private void doResetQuickSearch() {
		quickSearchEl.setValue("");
		doQuickSearch();
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
