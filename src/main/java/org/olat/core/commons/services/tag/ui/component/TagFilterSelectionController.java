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
import java.util.Collection;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.ChangeValueEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
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
public class TagFilterSelectionController extends FormBasicController {
	
	private static final String CMD_TOGGLE = "toggle";

	private final Comparator<TagItem> comparator;
	private FormLink clearButton;
	private FormLink updateButton;
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;
	private FormLink resetQuickSearchButton;
	private FormLayoutContainer tagsCont;
	
	private final FlexiTableTagFilter filter;
	private final Set<Long> selectedKeys;
	private List<TagItem> tagItems;
	private int counter;


	public TagFilterSelectionController(UserRequest ureq, WindowControl wControl, FlexiTableTagFilter filter,
			Collection<String> preselectedKeys) {
		super(ureq, wControl, "tag_filter", Util.createPackageTranslator(TagUIFactory.class, ureq.getLocale()));
		this.filter = filter;
		setTranslator(Util.createPackageTranslator(FlexiTableElementImpl.class, getLocale(), getTranslator()));
		this.selectedKeys = preselectedKeys != null
				? preselectedKeys.stream().map(Long::valueOf).collect(Collectors.toSet())
				: new HashSet<>(2);
		this.comparator = createComparator();
		
		initForm(ureq);
	}
	
	private Comparator<TagItem> createComparator() {
		Collator collator = Collator.getInstance(getLocale());
		return Comparator
				.comparing(
						TagItem::getCount,
						Comparator.reverseOrder())
				.thenComparing(
						(i1, i2) -> collator.compare(i1.getDisplayValue(), i2.getDisplayValue())
				);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setDomReplacementWrapperRequired(false);
		
		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("btn o_reset_filter_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		
		((FormLayoutContainer)formLayout).contextPut("numOfItems", Long.valueOf(filter.getAllTags().size()));
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
		
		tagsCont = FormLayoutContainer.createCustomFormLayout("tags", getTranslator(), velocity_root + "/tag_selection_tags.html");
		tagsCont.setRootForm(mainForm);
		formLayout.add("tags", tagsCont);
		
		initTagLinks();
	}

	private void initTagLinks() {
		tagItems = filter.getAllTags().stream().map(this::createTagItem).collect(Collectors.toList());
		tagItems.sort(comparator);
		tagsCont.contextPut("tags", tagItems);
	}

	private TagItem createTagItem(TagInfo tagInfo) {
		TagItem tagItem = new TagItem();
		tagItem.setKey(tagInfo.getKey());
		tagItem.setDisplayValue(tagInfo.getDisplayName());
		tagItem.setCount(tagInfo.getCount() != null? tagInfo.getCount().longValue(): 0l);
		tagItem.setSelected(selectedKeys.contains(tagInfo.getKey()));
		
		FormLink link = uifactory.addFormLink("tag_" + counter++, CMD_TOGGLE, getDisplayTag(tagItem), null, tagsCont,  Link.NONTRANSLATED);
		link.setElementCssClass(getTagLinkCss(selectedKeys.contains(tagInfo.getKey())));
		link.setDomReplacementWrapperRequired(false);
		tagItem.setLink(link);
		link.setUserObject(tagItem);
		return tagItem;
	}
	
	
	private String getDisplayTag(TagItem tagItem) {
		return translate("tag.count", tagItem.getDisplayValue(), String.valueOf(tagItem.getCount()));
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
		if (updateButton == source) {
			doSelect(ureq);
		} else if(clearButton == source) {
			doClear(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch();
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch();
		} else if (source instanceof FormLink link) {
			if (CMD_TOGGLE.equals(link.getCmd())) {
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
		List<String> keys = selectedKeys != null
				? selectedKeys.stream().map(l -> l.toString()).toList()
				: null;
		fireEvent(ureq, new ChangeValueEvent(filter, keys));
	}
	
	private void doClear(UserRequest ureq) {
		selectedKeys.clear();
		tagItems.forEach(item -> item.getLink().setVisible(true));
		quickSearchEl.setValue("");
		fireEvent(ureq, new ChangeValueEvent(filter, null));
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
		} else {
			tagItems.forEach(item -> item.getLink().setVisible(true));
		}
		
		tagsCont.setDirty(true);
	}

	private void doResetQuickSearch() {
		quickSearchEl.setValue("");
		doQuickSearch();
	}
	
}
