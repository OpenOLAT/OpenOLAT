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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.model.VideoSegmentCategoryImpl;

import org.jcodec.common.UsedViaReflection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditCategoriesController extends FormBasicController {
	private final static long MAX_NB_CATEGORIES = 10;

	private final String MOVE_UP_CMD = "moveUp";
	private final String MOVE_DOWN_CMD = "moveDown";
	private final String ADD_CMD = "add";
	private final String DELETE_CMD = "delete";

	private final VideoSegments videoSegments;
	private final boolean restrictedEdit;
	private List<Category> categories;
	@Autowired
	private ColorService colorService;
	private final List<String> colors;

	public class Category {
		private final int id;
		private final String longId;
		private int sortOrder;
		private String color;
		private final FormLink moveUpLink;
		private final FormLink moveDownLink;
		private final ColorPickerElement colorPickerElement;
		private final TextElement labelEl;
		private final TextElement titleEl;
		private final FormLink addButton;
		private final FormLink deleteButton;

		public Category(FormItemContainer formLayout, int id, VideoSegmentCategory category, List<String> colorNames) {
			this.id = id;
			this.longId = category == null ? UUID.randomUUID().toString() : category.getId();
			this.sortOrder = id;
			this.color = category == null ? colorNames.get(0) : category.getColor();
			moveUpLink = uifactory.addFormLink(MOVE_UP_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveUpLink.setIconRightCSS("o_icon o_icon_move_up o_icon-lg");

			moveDownLink = uifactory.addFormLink(MOVE_DOWN_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			moveDownLink.setIconRightCSS("o_icon o_icon_move_down o_icon-lg");

			colorPickerElement = uifactory.addColorPickerElement("color_" + id, "",
					formLayout, ColorUIFactory.createColors(colorNames, getLocale()));
			colorPickerElement.setColor(color);
			colorPickerElement.setEnabled(!restrictedEdit);

			String labelValue = category == null ? "" : category.getLabel();
			labelEl = uifactory.addTextElement("label_" + id, "", 2, labelValue, formLayout);
			labelEl.setDisplaySize(2);
			labelEl.setMandatory(true);

			String titleValue = category == null ? "" : category.getTitle();
			titleEl = uifactory.addTextElement("title_" + id, "", 25, titleValue,
					formLayout);
			titleEl.setMandatory(true);

			addButton = uifactory.addFormLink(ADD_CMD + "_" + id, "", "", formLayout,
					Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			addButton.setIconRightCSS("o_icon o_icon_add");

			deleteButton = uifactory.addFormLink(DELETE_CMD + "_" + id, "", "", formLayout,
					Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			deleteButton.setIconRightCSS("o_icon o_icon_delete_item");
		}

		public FormLink getMoveUpLink() {
			return moveUpLink;
		}

		public FormLink getMoveDownLink() {
			return moveDownLink;
		}

		@UsedViaReflection
		public ColorPickerElement getColorPickerElement() {
			return colorPickerElement;
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

		public String getLongId() {
			return longId;
		}

		public int getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		public void setColor(String color) {
			this.color = color;
			colorPickerElement.setColor(color);
		}

		public String getColor() {
			return color;
		}

		public VideoSegmentCategory asVideoSegmentCategory() {
			VideoSegmentCategoryImpl videoSegmentCategory = new VideoSegmentCategoryImpl();
			videoSegmentCategory.setId(longId);
			videoSegmentCategory.setLabel(labelEl.getValue());
			videoSegmentCategory.setTitle(titleEl.getValue());
			videoSegmentCategory.setColor(colorPickerElement.getColor().id());
			videoSegmentCategory.setSortOrder(sortOrder);
			return videoSegmentCategory;
		}
	}

	public EditCategoriesController(UserRequest ureq, WindowControl wControl, VideoSegments videoSegments, boolean restrictedEdit) {
		super(ureq, wControl, "video_edit_categories");
		this.videoSegments = videoSegments;
		this.restrictedEdit = restrictedEdit;
		this.colors = colorService.getColors();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		categories = new ArrayList<>();
		for (int i = 0; i < videoSegments.getCategories().size(); i++) {
			VideoSegmentCategory category = videoSegments.getCategories().get(i);
			categories.add(new Category(formLayout, i, category, colors));
		}
		initUi();
		flc.contextPut("categories", categories);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source.getName().contains("_")){
			String[] parts = source.getName().split("_");
			String cmd = parts[0];
			int id = Integer.parseInt(parts[1]);
			doHandleCommand(cmd, id);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doHandleCommand(String cmd, int id) {
		if (ADD_CMD.equals(cmd)) {
			Category category = new Category(flc, getNewCategoryIndex(), null, colors);

			Set<String> usedColors = categories.stream().map(Category::getColor).collect(Collectors.toSet());
			Optional<String> unusedColor = colors.stream().filter(c -> !usedColors.contains(c)).findFirst();
			unusedColor.ifPresentOrElse(category::setColor, () -> category.setColor(colors.get(0)));

			category.getLabel().setValue(translate("form.segment.category.label.new"));
			category.getTitle().setValue(translate("form.segment.category.title.new"));
			int index = Integer.min(id + 1, categories.size());
			categories.add(index, category);
			initUi();
			flc.contextPut("categories", categories);
		} else if (DELETE_CMD.equals(cmd)) {
			categories.stream().filter((c) -> c.getId() == id).findFirst().ifPresent((c) -> categories.remove(c));
			if (categories.isEmpty()) {
				Category category = new Category(flc, getNewCategoryIndex(), null, colors);
				category.setColor(colors.get(0));
				category.getLabel().setValue(translate("form.segment.category.label.new"));
				category.getTitle().setValue(translate("form.segment.category.title.new"));
				categories.add(0, category);
			}
			initUi();
			flc.contextPut("categories", categories);
		} else if (MOVE_UP_CMD.equals(cmd)) {
			int index = getCategoryIndex(id);
			if (index >= 1) {
				Category category = categories.get(index);
				categories.remove(index);
				categories.add(index - 1, category);
				initUi();
				flc.contextPut("categories", categories);
			}
		} else if (MOVE_DOWN_CMD.equals(cmd)) {
			int index = getCategoryIndex(id);
			if (index != -1 && index <= (categories.size() - 2)) {
				Category category = categories.get(index);
				categories.remove(index);
				categories.add(index + 1, category);
				initUi();
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

	private void initUi() {
		initWarning();
		initButtonStates();
		initCategorySortOrders();
	}

	private void initWarning() {
		flc.contextPut("limitReached", categories.size() >= MAX_NB_CATEGORIES);
	}

	private void initButtonStates() {
		Set<String> usedIds = videoSegments.getSegments().stream().map(VideoSegment::getCategoryId).collect(Collectors.toSet());
		boolean canAddCategory = categories.size() < MAX_NB_CATEGORIES;
		for (Category category : categories) {
			category.getDeleteButton().setEnabled(!usedIds.contains(category.getLongId()) && !restrictedEdit);
			category.getAddButton().setEnabled(canAddCategory && !restrictedEdit);
		}
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
			category.getMoveUpLink().setEnabled(!restrictedEdit);
			category.getMoveDownLink().setEnabled(!restrictedEdit);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (!validateNonWhitespace()) {
			return false;
		}

		if (!validateUniqueness()) {
			return false;
		}

		return allOk;
	}

	private boolean validateNonWhitespace() {
		for (Category category: categories) {
			category.labelEl.clearError();
			if (!StringHelper.containsNonWhitespace(category.labelEl.getValue())) {
				category.labelEl.setErrorKey("form.legende.mandatory");
				return false;
			}
			category.titleEl.clearError();
			if (!StringHelper.containsNonWhitespace(category.titleEl.getValue())) {
				category.titleEl.setErrorKey("form.legende.mandatory");
				return false;
			}
		}
		return true;
	}

	private boolean validateUniqueness() {
		for (Category category: categories) {
			category.labelEl.clearError();
			String label = category.labelEl.getValue();
			category.titleEl.clearError();
			String title = category.titleEl.getValue();
			for (Category otherCategory: categories) {
				if (otherCategory.id == category.id) {
					continue;
				}
				String otherLabel = otherCategory.getLabel().getValue();
				if (otherLabel.equalsIgnoreCase(label)) {
					category.labelEl.setErrorKey("form.segment.category.label.error");
					return false;
				}

				String otherTitle = otherCategory.getTitle().getValue();
				if (otherTitle.equalsIgnoreCase(title)) {
					category.titleEl.setErrorKey("form.segment.category.title.error");
					return false;
				}
			}
		}
		return true;
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
