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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.ui.VideoSettingsController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SelectColorController extends FormBasicController {
	private static final String COLOR_CMD = "color";
	private final SelectionValues colorsKV;
	private final Object userObject;
	private List<Color> colors;

	@Autowired
	private VideoModule videoModule;

	public class Color {
		int id;
		String color;
		FormLink colorLink;

		public Color(FormItemContainer formLayout, UserRequest ureq, int id, String color) {
			this.id = id;
			this.color = color;

			colorLink = uifactory.addFormLink(COLOR_CMD + "_" + id, "", "", formLayout,
					Link.LINK | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
			colorLink.setElementCssClass("o_video_color_link o_video_colored_area " + color);
		}

		public int getId() {
			return id;
		}

		public String getColor() {
			return color;
		}

		public FormLink getColorLink() {
			return colorLink;
		}
	}
	protected SelectColorController(UserRequest ureq, WindowControl wControl, Object userObject) {
		super(ureq, wControl, "video_select_color");
		this.userObject = userObject;
		colorsKV = new SelectionValues();
		Translator videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		for (String color : videoModule.getMarkerStyles()) {
			colorsKV.add(SelectionValues.entry(color, videoTranslator.translate("video.marker.style.".concat(color))));
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		colors = new ArrayList<>();
		for (int i = 0; i < colorsKV.size(); i++) {
			Color color = new Color(formLayout, ureq, i, colorsKV.keys()[i]);
			colors.add(color);
		}
		flc.contextPut("colors", colors);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		for (Color color : colors) {
			if (color.getColorLink() == source) {
				fireEvent(ureq, new ColorSelectedEvent(color.color, userObject));
				break;
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}
}
