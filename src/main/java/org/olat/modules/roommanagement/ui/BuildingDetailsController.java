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
package org.olat.modules.roommanagement.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;

/**
 * Initial date: 2 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingDetailsController extends FormBasicController {

	private FormLink editLink;
	private final Building building;

	public BuildingDetailsController(UserRequest ureq, WindowControl wControl, Building building, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "building_detail_panel", rootForm);
		this.building = building;
		initForm(ureq);
	}

	public Building getBuilding() {
		return building;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(building.getColorCss())) {
			formLayout.contextPut("colorCss", building.getColorCss());
		}
		if (StringHelper.containsNonWhitespace(building.getExternalRef())) {
			formLayout.contextPut("reference", building.getExternalRef());
		}
		if (StringHelper.containsNonWhitespace(building.getDescription())) {
			formLayout.contextPut("description", building.getDescription());
		}
		formLayout.contextPut("statusName", building.getStatus().name());
		formLayout.contextPut("statusLabel", translate("building.status." + building.getStatus().name()));

		editLink = uifactory.addFormLink("building.detail.edit", formLayout, Link.BUTTON);
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
