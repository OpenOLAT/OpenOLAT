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
package org.olat.modules.video.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.model.VideoSegmentCategoryImpl;

/**
 * Initial date: 2023-01-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditCategoriesController extends FormBasicController {

	private final String MOVE_UP_CMD = "moveUp";
	private final String MOVE_DOWN_CMD = "moveDown";
	private final String COLOR_CMD = "color";
	private final String ADD_CMD = "add";
	private final String DELETE_CMD = "delete";

	private final VideoSegments videoSegments;
	private List<Category> categories;
	private CloseableCalloutWindowController ccwc;
	private SelectColorController selectColorController;

	public class Category {
		private int id;
		private String longId;
		private int sortOrder;
		private String color;
		private FormLink moveUpLink;
		private FormLink moveDownLink;
		private FormLink colorLink;
		private TextElement labelEl;
		private TextElement titleEl;
		private FormLink addButton;
		private FormLink deleteButton;

		public Category(FormItemContainer formLayout, UserRequest ureq, int id, VideoSegmentCategory category) {
			this.id = id;
			this.longId = category == null ? UUID.randomUUID().toString() : category.getId();
			this.sortOrder = id;
			this.color = category == null ? "o_video_marker_gray" : category.getColor();
			moveUpLink = uifactory.addFormLink(MOVE_UP_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveUpLink.setIconRightCSS("o_icon o_icon_move_up o_icon-lg");

			moveDownLink = uifactory.addFormLink(MOVE_DOWN_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveDownLink.setIconRightCSS("o_icon o_icon_move_down o_icon-lg");

			colorLink = uifactory.addFormLink(COLOR_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			colorLink.setIconRightCSS("o_color_button_placeholder o_video_colored_area " + color);

			String labelValue = category == null ? "" : category.getLabel();
			labelEl = uifactory.addTextElement("label_" + id, "", 2, labelValue, formLayout);
			labelEl.setMandatory(true);

			String titleValue = category == null ? "" : category.getTitle();
			titleEl = uifactory.addTextElement("title_" + id, "", 80, titleValue,
					formLayout);
			titleEl.setMandatory(true);

			addButton = uifactory.addFormLink(ADD_CMD + "_" + id, "", "", formLayout,
					Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			addButton.setIconRightCSS("o_icon o_icon_add");

			deleteButton = uifactory.addFormLink(DELETE_CMD + "_" + id, "", "", formLayout,
					Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			deleteButton.setIconRightCSS("o_icon o_icon_delete_item");

			uifactory.addFormSubmitButton("save", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}

		public FormLink getMoveUpLink() {
			return moveUpLink;
		}

		public FormLink getMoveDownLink() {
			return moveDownLink;
		}

		public FormLink getColorLink() {
			return colorLink;
		}

		public TextElement getLabel() {
			return labelEl;
		}

		public TextElement getTitle() {
			return titleEl;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public int getId() {
			return id;
		}

		public int getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		public void setColor(String color) {
			this.color = color;
			colorLink.setIconRightCSS("o_color_button_placeholder o_video_colored_area " + color);
		}

		public String getColor() {
			return color;
		}

		public VideoSegmentCategory asVideoSegmentCategory() {
			VideoSegmentCategoryImpl videoSegmentCategory = new VideoSegmentCategoryImpl();
			videoSegmentCategory.setId(longId);
			videoSegmentCategory.setLabel(labelEl.getValue());
			videoSegmentCategory.setTitle(titleEl.getValue());
			videoSegmentCategory.setColor(color);
			videoSegmentCategory.setSortOrder(sortOrder);
			return videoSegmentCategory;
		}
	}

	public EditCategoriesController(UserRequest ureq, WindowControl wControl, VideoSegments videoSegments) {
		super(ureq, wControl, "video_edit_categories");
		this.videoSegments = videoSegments;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		categories = new ArrayList<>();
		for (int i = 0; i < videoSegments.getCategories().size(); i++) {
			VideoSegmentCategory category = videoSegments.getCategories().get(i);
			categories.add(new Category(formLayout, ureq, i, category));
		}
		initCategorySortOrders();
		flc.contextPut("categories", categories);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (selectColorController == source) {
			if (event instanceof ColorSelectedEvent colorSelectedEvent) {
				Category category = categories.get((int) colorSelectedEvent.getUserObject());
				category.setColor(colorSelectedEvent.getColor());
				flc.contextPut("categories", categories);
			}
			ccwc.deactivate();
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(selectColorController);
		ccwc = null;
		selectColorController = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source.getName().contains("_")){
			String parts[] = source.getName().split("_");
			String cmd = parts[0];
			int id = Integer.parseInt(parts[1]);
			doHandleCommand(cmd, id, ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doHandleCommand(String cmd, int id, UserRequest ureq) {
		if (ADD_CMD.equals(cmd)) {
			categories.add(0, new Category(flc, ureq, getNewCategoryIndex(), null));
			initCategorySortOrders();
			flc.contextPut("categories", categories);
		} else if (DELETE_CMD.equals(cmd)) {
			categories.stream().filter((c) -> c.getId() == id).findFirst().ifPresent((c) -> categories.remove(c));
			initCategorySortOrders();
			flc.contextPut("categories", categories);
		} else if (MOVE_UP_CMD.equals(cmd)) {
			int index = getCategoryIndex(id);
			if (index >= 1) {
				Category category = categories.get(index);
				categories.remove(index);
				categories.add(index - 1, category);
				initCategorySortOrders();
				flc.contextPut("categories", categories);
			}
		} else if (MOVE_DOWN_CMD.equals(cmd)) {
			int index = getCategoryIndex(id);
			if (index != -1 && index <= (categories.size() - 2)) {
				Category category = categories.get(index);
				categories.remove(index);
				categories.add(index + 1, category);
				initCategorySortOrders();
				flc.contextPut("categories", categories);
			}
		} else if (COLOR_CMD.equals(cmd)) {
			int index = getCategoryIndex(id);
			Category category = categories.get(index);
			doSelectColor(ureq, category.getColorLink(), index);
		}
	}

	private void doSelectColor(UserRequest ureq, FormLink anchorButton, int index) {
		if (guardModalController(selectColorController)) {
			return;
		}

		selectColorController = new SelectColorController(ureq, getWindowControl(), index);
		listenTo(selectColorController);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				selectColorController.getInitialComponent(), anchorButton.getFormDispatchId(), "", true,
				"", new CalloutSettings(false));
		listenTo(ccwc);
		ccwc.activate();
	}

	private int getNewCategoryIndex() {
		int index = 0;
		for (Category category : categories) {
			index = Math.max(index, category.getId());
		}
		return index + 1;
	}

	private int getCategoryIndex(int id) {
		for (int i = 0; i < categories.size(); i++) {
			if (categories.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	private void initCategorySortOrders() {
		int sortOrder = 0;
		for (Category category : categories) {
			category.setSortOrder(sortOrder++);
		}

		for (int i = 0; i < categories.size(); i++) {
			Category category = categories.get(i);
			category.setSortOrder(i);
			category.getMoveUpLink().setVisible(i > 0);
			category.getMoveDownLink().setVisible(i < (categories.size() - 1));
		}
	}
	@Override
	protected void formOK(UserRequest ureq) {
		videoSegments.getCategories().clear();
		for (Category category: categories) {
			videoSegments.getCategories().add(category.asVideoSegmentCategory());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
