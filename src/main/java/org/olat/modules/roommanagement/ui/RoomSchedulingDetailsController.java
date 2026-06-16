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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;

/**
 * Initial date: 15 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingDetailsController extends FormBasicController {

	private final RoomSchedulingRow row;

	public RoomSchedulingDetailsController(UserRequest ureq, WindowControl wControl, RoomSchedulingRow row, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "room_scheduling_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		this.row = row;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!(formLayout instanceof FormLayoutContainer layoutCont)) return;

		LectureBlock lb = row.getBooking().getLectureBlock();
		if (lb == null) return;

		layoutCont.contextPut("title", lb.getTitle());
		layoutCont.contextPut("externalRef", lb.getExternalRef());
		String statusBadge = LectureBlockStatusCellRenderer.getStatusLabelSolidWithIcon(lb, false, getTranslator());
		layoutCont.contextPut("lectureBlockStatusBadge", statusBadge);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read-only
	}
}
