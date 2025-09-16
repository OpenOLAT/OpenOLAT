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

package org.olat.resource.accesscontrol.provider.free.ui;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;

/**
 * 
 * Description:<br>
 * Configuration for a free acces
 * 
 * <P>
 * Initial Date:  31 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FreeAccessConfigurationController extends AbstractConfigurationMethodController {
	
	private MultipleSelectionElement autoEl;
	
	private String[] autoKeys = new String[]{ "x" };
	
	public FreeAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean confirmationByManagerSupported, CatalogInfo catalogInfo, boolean edit) {
		super(ureq, wControl, link, offerOrganisationsSupported, offerOrganisations, confirmationByManagerSupported, catalogInfo, edit);
		initForm(ureq);
		updateAutoBookingUI();
	}

	@Override
	protected void initCustomMembershipElements(FormItemContainer formLayout) {
		formLayout.setElementCssClass("o_sel_accesscontrol_free_form");
		
		if (catalogInfo.isAutoBookingSupported()) {
			String[] autoValues = new String[]{ translate("auto.booking.option") };
			autoEl = uifactory.addCheckboxesHorizontal("auto.booking", "auto.booking", formLayout, autoKeys, autoValues);
			autoEl.setHelpText(translate("auto.booking.help"));
			autoEl.setElementCssClass("o_sel_accesscontrol_auto_booking");
			autoEl.addActionListener(FormEvent.ONCHANGE);
			if(link.getOffer() != null && link.getOffer().getKey() != null) {
				autoEl.select(autoKeys[0], link.getOffer().isAutoBooking());
			} else {
				autoEl.select(autoKeys[0], true);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == confirmationByManagerEl) {
			updateAutoBookingUI();
		} else if (source == autoEl) {
			updateAutoBookingUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateAutoBookingUI() {
		if (autoEl == null) {
			return;
		}
		
		boolean confirmationByManager = confirmationByManagerEl != null
				&& confirmationByManagerEl.isOneSelected()
				&& confirmationByManagerEl.isKeySelected(CONFIRMATION_BY_MANAGER_YES);
		autoEl.setVisible(!confirmationByManager);
		
		boolean autoBooking = autoEl.isVisible() && autoEl.isAtLeastSelected(1);
		customSpacerEl.setVisible(!autoBooking);
		descEl.setVisible(!autoBooking);
	}

	@Override
	protected void updateCustomChanges() {
		link.getOffer().setAutoBooking(autoEl != null && autoEl.isVisible() && autoEl.isAtLeastSelected(1));
	}
	
}
