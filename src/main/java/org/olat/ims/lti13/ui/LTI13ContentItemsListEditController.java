/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemTypesEnum;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.ui.events.LTI13ContentItemAddEvent;
import org.olat.ims.lti13.ui.events.LTI13ContentItemRemoveEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LTI13ContentItemsListEditController extends FormBasicController {
	
	private int count = 0;
	private List<ContentItemRow> contentItemRows;
	
	private CloseableModalController cmc;
	private ConfirmRemoveContentItemController confirmRemoveCtrl;

	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13ContentItemsListEditController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "contentitems_edit", mainForm);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}
	
	public boolean isEmpty() {
		return contentItemRows == null || contentItemRows.isEmpty();
	}
	
	public List<Long> getOrderedItemsKey() {
		if(contentItemRows == null) return List.of();
		return contentItemRows.stream()
				.map(ContentItemRow::getKey)
				.toList();
	}
	
	public void loadItems(List<LTI13ContentItem> contentItems) {
		List<ContentItemRow> rows = new ArrayList<>();
		for(LTI13ContentItem contentItem:contentItems) {
			rows.add(forgeRow(contentItem, flc));
		}
		contentItemRows = rows;
		recalculateUpDownLinks();
		flc.contextPut("rows", rows);
	}
	
	private ContentItemRow forgeRow(LTI13ContentItem contentItem, FormLayoutContainer formLayout) {
		String num = Integer.toString(++count);
		String id = "title_".concat(num);
		String title = contentItem.getTitle();
		TextElement titleEl = uifactory.addTextElement(id, id, "item.title", 255, title, formLayout);
		
		String text = contentItem.getText();
		RichTextElement textEl = uifactory.addRichTextElementForStringDataMinimalistic("text_".concat(num), "item.text", text, 4, 60, formLayout);
		textEl.getEditorConfiguration().setStatusBar(false);
		textEl.setEnabled(contentItem.getType() != LTI13ContentItemTypesEnum.html);
		
		String removeId = "rm-".concat(num);
		FormLink removeLink = uifactory.addFormLink(removeId, "rm", "", null, formLayout, Link.NONTRANSLATED);
		removeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
		formLayout.add(removeLink);
		formLayout.add(removeId, removeLink);
		
		String addId = "add-".concat(num);
		FormLink addLink = uifactory.addFormLink(addId, "add", "", null, formLayout, Link.NONTRANSLATED);
		addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		formLayout.add(addLink);
		formLayout.add(addId, addLink);
		
		String upId = "up-".concat(num);
		FormLink upLink = uifactory.addFormLink(upId, "up", "", null, formLayout, Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		formLayout.add(upLink);
		formLayout.add(upId, upLink);
		
		String downId = "down-".concat(num);
		FormLink downLink = uifactory.addFormLink(downId, "down", "", null, formLayout, Link.NONTRANSLATED);
		downLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		formLayout.add(downLink);
		formLayout.add(downId, downLink);
		
		ContentItemRow row = new ContentItemRow(contentItem, titleEl, textEl, addLink, removeLink, upLink, downLink);
		upLink.setUserObject(row);
		addLink.setUserObject(row);
		downLink.setUserObject(row);
		removeLink.setUserObject(row);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new LTI13ContentItemRemoveEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveCtrl);
		removeAsListenerAndDispose(cmc);
		confirmRemoveCtrl = null;
		cmc = null;
	}

	@Override
	public void formOK(UserRequest ureq) {
		commitConfig();
	}
	
	public List<Long> commitConfig() {
		if(contentItemRows == null || contentItemRows.isEmpty()) {
			return new ArrayList<>(1);
		}
		
		List<Long> keys = new ArrayList<>();
		for(ContentItemRow contentItemRow:contentItemRows) {
			LTI13ContentItem item = contentItemRow.getContentItem();
			item.setTitle(contentItemRow.getTitleEl().getValue());
			item.setText(contentItemRow.getTextEl().getValue());
			item = lti13Service.updateContentItem(item);
			contentItemRow.setContentItem(item);
			keys.add(item.getKey());
		}
		return keys;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink button) {
			String cmd = button.getCmd();
			if("rm".equals(cmd) && button.getUserObject() instanceof ContentItemRow row) {
				doConfirmRemoveItem(ureq, row);
			} else if("add".equals(cmd)) {
				int index = contentItemRows.indexOf(button.getUserObject()) + 1;
				fireEvent(ureq, new LTI13ContentItemAddEvent(index));
			} else if("up".equals(cmd) && button.getUserObject() instanceof ContentItemRow row) {
				doMoveUp(row);
			} else if("down".equals(cmd) && button.getUserObject() instanceof ContentItemRow row) {
				doMoveDown(row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doMoveUp(ContentItemRow itemRow) {
		int index = contentItemRows.indexOf(itemRow) - 1;
		if(index >= 0 && index < contentItemRows.size()) {
			contentItemRows.remove(itemRow);
			contentItemRows.add(index, itemRow);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doMoveDown(ContentItemRow itemRow) {
		int index = contentItemRows.indexOf(itemRow) + 1;
		if(index > 0 && index < contentItemRows.size()) {
			contentItemRows.remove(itemRow);
			contentItemRows.add(index, itemRow);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void recalculateUpDownLinks() {
		int numOfItems = contentItemRows.size();
		for(int i=0; i<numOfItems; i++) {
			ContentItemRow choiceWrapper = contentItemRows.get(i);
			choiceWrapper.getUp().setEnabled(i != 0);
			choiceWrapper.getDown().setEnabled(i < (numOfItems - 1));
		}
	}
	
	private void doConfirmRemoveItem(UserRequest ureq, ContentItemRow row) {
		LTI13ContentItem item = row.getContentItem();
		confirmRemoveCtrl = new ConfirmRemoveContentItemController(ureq, getWindowControl(), item);
		listenTo(confirmRemoveCtrl);
		
		String title = item.getTitle();
		String modalTitle;
		if(StringHelper.containsNonWhitespace(title)) {
			modalTitle = translate("remove.content.item.title", title);
		} else {
			modalTitle = translate("remove.content.item.title.wo");
		}
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRemoveCtrl.getInitialComponent(), true, modalTitle);
		cmc.activate();
		listenTo(cmc);	
	}

	public static class ContentItemRow {
		
		private LTI13ContentItem contentItem;
		private final TextElement titleEl;
		private final TextElement textEl;
		private final FormLink addLink;
		private final FormLink removeLink;
		private final FormLink upLink;
		private final FormLink downLink;
		
		public ContentItemRow(LTI13ContentItem contentItem, TextElement titleEl, TextElement textEl,
				FormLink addLink, FormLink removeLink, FormLink upLink, FormLink downLink) {
			this.contentItem = contentItem;
			this.titleEl = titleEl;
			this.textEl = textEl;
			this.addLink = addLink;
			this.removeLink = removeLink;
			this.upLink = upLink;
			this.downLink = downLink;
		}

		public LTI13ContentItem getContentItem() {
			return contentItem;
		}

		public void setContentItem(LTI13ContentItem contentItem) {
			this.contentItem = contentItem;
		}

		public TextElement getTitleEl() {
			return titleEl;
		}

		public TextElement getTextEl() {
			return textEl;
		}
		
		public Long getKey() {
			return contentItem.getKey();
		}
		
		public String presentation() {
			return contentItem.getPresentation() == null ? null : contentItem.getPresentation().name();
		}

		public String type() {
			return contentItem.getType().name();
		}
		
		public String url() {
			return contentItem.getUrl();
		}
		
		public boolean hasThumbnail() {
			return StringHelper.containsNonWhitespace(contentItem.getThumbnailUrl());
		}
		
		public String thumbnailUrl() {
			return contentItem.getThumbnailUrl();
		}
		
		public Long thumbnailWidth() {
			return contentItem.getThumbnailWidth();
		}
		
		public Long thumbnailHeight() {
			return contentItem.getThumbnailHeight();
		}
		
		public String titleComponentName() {
			return titleEl.getComponent().getComponentName();
		}
		
		public String textComponentName() {
			return textEl.getComponent().getComponentName();
		}
		
		public String addComponentName() {
			return addLink.getComponent().getComponentName();
		}
		
		public String removeComponentName() {
			return removeLink.getComponent().getComponentName();
		}
		
		public String upComponentName() {
			return upLink.getComponent().getComponentName();
		}
		
		public String downComponentName() {
			return downLink.getComponent().getComponentName();
		}
		
		public FormLink getUp() {
			return upLink;
		}
		
		public FormLink getDown() {
			return downLink;
		}
	}
}
