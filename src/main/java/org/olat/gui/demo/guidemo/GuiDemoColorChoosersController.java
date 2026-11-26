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

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorSet;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.commons.services.color.Palette;
import org.olat.core.commons.services.color.Swatch;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.gui.demo.guidemo.GuiDemoColorsController.Chooser;
import org.olat.gui.demo.guidemo.GuiDemoColorsController.HueRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The tab "Choosers" of the colors demo. It renders a real color picker for
 * each of the three chooser sets S, M and L, built as in the places that use
 * the set, next to the palette cells the set uses. The sets S and M use the
 * list, the set L the grid presentation. The report chart series are shown
 * without picker.
 *
 * Initial date: 1 Nov 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoColorChoosersController extends FormBasicController {

	private final List<HueRow> rows;
	private final List<Chooser> choosers;

	@Autowired
	private ColorService colorService;

	public GuiDemoColorChoosersController(UserRequest ureq, WindowControl wControl, List<HueRow> rows, List<Chooser> choosers) {
		super(ureq, wControl, "guidemo-colors-choosers");
		this.rows = rows;
		this.choosers = choosers;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("rows", rows);
		flc.contextPut("steps", Palette.STEPS);
		flc.contextPut("choosers", choosers);

		addPicker("set_s", ColorUIFactory.createColors(ColorSet.S, getLocale()));
		addPicker("set_m", ColorUIFactory.createColors(ColorSet.M, getLocale()));
		// The set L as grid: one column per colour, one row per variant, and the option "no color"
		ColorPickerElement setLEl = addPicker("set_l", createSetLColors());
		setLEl.setPresentation(ColorPickerElement.Presentation.GRID);
		setLEl.setAllowUnselect(true);
		setLEl.setNonSelectedText("No color");
	}

	/**
	 * @return The colours of the set L: per colour of the set the 5 variants,
	 * 		light to dark, labelled with the label of the colour and the variant,
	 * 		e.g. "Blue soft", ..., "Blue dark"; the theme colours in their group
	 */
	private List<ColorPickerElement.Color> createSetLColors() {
		List<ColorPickerElement.Color> colors = new ArrayList<>();
		// one entry per colour of the set, with the label and the group of the colour
		for (ColorPickerElement.Color color : ColorUIFactory.createColors(ColorSet.L, getLocale())) {
			for (String variant : ColorSet.L.getVariants()) {
				String key = new Swatch(color.id(), variant).key();
				String cssClass = colorService.toSwatchCssClass(key);
				if (cssClass != null) {
					colors.add(new ColorPickerElement.Color(key, color.translatedName() + " " + variant, cssClass, color.group()));
				}
			}
		}
		return colors;
	}

	private ColorPickerElement addPicker(String chooserId, List<ColorPickerElement.Color> colors) {
		ColorPickerElement pickerEl = uifactory.addColorPickerElement("picker_" + chooserId, null, flc, getWindowControl(), colors);
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
