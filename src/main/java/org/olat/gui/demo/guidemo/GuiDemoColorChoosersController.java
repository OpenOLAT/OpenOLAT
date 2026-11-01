/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.commons.calendar.ui.CalendarColors;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.commons.services.color.Palette;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.gui.demo.guidemo.GuiDemoColorsController.Chooser;
import org.olat.gui.demo.guidemo.GuiDemoColorsController.HueRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The tab "Choosers" of the colors demo. It renders the real color picker of
 * every chooser, built with the same colors as in its original place, next to
 * the palette cells the chooser uses.
 *
 * Initial date: 1 Nov 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoColorChoosersController extends FormBasicController {

	private static final ColorCategorySearchParams SEARCH_PARAMS_SELECTION = ColorCategorySearchParams.builder()
			.addColorTypes()
			.withEnabled(Boolean.TRUE)
			.build();

	private final List<HueRow> rows;
	private final List<Chooser> choosers;

	@Autowired
	private ColorService colorService;
	@Autowired
	private CourseStyleService courseStyleService;

	public GuiDemoColorChoosersController(UserRequest ureq, WindowControl wControl, List<HueRow> rows, List<Chooser> choosers) {
		super(ureq, wControl, "guidemo-colors-choosers");
		setTranslator(Util.createPackageTranslator(CourseStyleUIFactory.class, getLocale(), getTranslator()));
		this.rows = rows;
		this.choosers = choosers;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("rows", rows);
		flc.contextPut("steps", Palette.STEPS);
		flc.contextPut("choosers", choosers);

		// Course editor, NodeLayoutController
		List<ColorPickerElement.Color> categoryColors = new ArrayList<>();
		for (ColorCategory colorCategory : courseStyleService.getColorCategories(SEARCH_PARAMS_SELECTION)) {
			categoryColors.add(CourseStyleUIFactory.createColor(colorCategory, getTranslator()));
		}
		ColorPickerElement categoryEl = addPicker("category", categoryColors);
		categoryEl.setElementCssClass("o_colcal_ele");

		// Calendar, CalendarEntryForm and CalendarPersonalConfigurationController
		addPicker("calendar", ColorUIFactory.createColors(CalendarColors.getColorsList(), getLocale(), "o_cal_"));
		// Page editor, room management, video editor
		addPicker("colorlist", ColorUIFactory.createColors(colorService.getColors(), getLocale()));
		// Open Badges, CreateBadge02CustomizationStep
		addPicker("badges", ColorUIFactory.createColors(colorService.getColorsForBadges(), getLocale()));
	}

	private ColorPickerElement addPicker(String chooserId, List<ColorPickerElement.Color> colors) {
		ColorPickerElement pickerEl = uifactory.addColorPickerElement("picker_" + chooserId, null, flc, colors);
		if (!colors.isEmpty()) {
			pickerEl.setColor(colors.get(0).id());
		}
		for (Chooser chooser : choosers) {
			if (chooser.getId().equals(chooserId)) {
				chooser.setPicker(pickerEl.getName());
			}
		}
		return pickerEl;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// the pickers are for trying only
	}
}
