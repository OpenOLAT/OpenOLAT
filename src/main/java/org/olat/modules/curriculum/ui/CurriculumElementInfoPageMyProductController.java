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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.repository.ui.list.LeavingEvent;

/**
 *
 * Initial date: 3 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoPageMyProductController extends BasicController {
	
	private static final String CMD_CANCEL = "cancel";
	
	private Link cancelLink;
	private CancelBookingController cancelBookingCtrl;
	
	public CurriculumElementInfoPageMyProductController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config, CurriculumElement element) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumElementInfoPageMyProductController.class, ureq.getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale())));
		
		if (!config.isCancelAvailable()) {
			putInitialPanel(new Panel("empty"));
			return;
		}
		
		FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", null);
		factSheet.setTitle(translate("details.my.educational.product"));
		putInitialPanel(factSheet);
		
		String cancelText = config.isCancelWithFee() ? translate("leave.cancel.fee") : translate("leave.cancel");
		cancelLink = LinkFactory.createCustomLink(CMD_CANCEL, CMD_CANCEL, cancelText, Link.BUTTON | Link.NONTRANSLATED, null, this);
		cancelLink.setElementCssClass("o_cancel btn-danger " + FactSheet.CSS_FOOTER_LINK_FULL_WIDTH);
		cancelLink.setIconLeftCSS("o_icon o_icon_sign_out");
		cancelLink.setGhost(true);
		cancelLink.setEnabled(config.isCancelEnabled());
		factSheet.setFooterLinks(List.of(cancelLink));
		
		cancelBookingCtrl = new CancelBookingController(ureq, wControl, element, config.getBookedIdentity(), false);
		listenTo(cancelBookingCtrl);
	}
	
	public boolean hasContent() {
		return cancelLink != null;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelLink) {
			cancelBookingCtrl.doConfirmCancel(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (cancelBookingCtrl == source) {
			if (event instanceof LeavingEvent) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
}
