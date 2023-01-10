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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;

/**
 * Initial date: 2023-01-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditCategoriesController extends FormBasicController {

	private final String MOVE_UP_CMD = "moveUp";
	private final String MOVE_DOWN_CMD = "moveDown";
	private final String ADD_CMD = "add";
	private final String DELETE_CMD = "delete";

	private final VideoSegments videoSegments;
	private List<Category> categories;

	public class Category {
		int id;
		int sortOrder;
		String color;
		FormLink moveUpLink;
		FormLink moveDownLink;
		TextElement labelEl;
		TextElement titleEl;
		FormLink addButton;
		FormLink deleteButton;

		public Category(FormItemContainer formLayout, UserRequest ureq, int id, VideoSegmentCategory category) {
			this.id = id;
			this.sortOrder = id;
			this.color = category == null ? "o_video_marker_gray" : category.getColor();
			moveUpLink = uifactory.addFormLink(MOVE_UP_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveUpLink.setIconRightCSS("o_icon o_icon_move_up o_icon-lg");

			moveDownLink = uifactory.addFormLink(MOVE_DOWN_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveDownLink.setIconRightCSS("o_icon o_icon_move_down o_icon-lg");

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

			FormSubmit saveButton = uifactory.addFormSubmitButton("save", formLayout);
			FormCancel cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}

		public FormLink getMoveUpLink() {
			return moveUpLink;
		}

		public FormLink getMoveDownLink() {
			return moveDownLink;
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

		public String getColor() {
			return color;
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
		}
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

	}
}
