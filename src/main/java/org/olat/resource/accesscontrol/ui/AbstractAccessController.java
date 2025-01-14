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
package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractAccessController extends BasicController {
	
	private final VelocityContainer mainVC;

	private Controller detailsCtrl;
	
	private final OfferAccess link;
	
	@Autowired
	private AccessControlModule acModule;

	protected AbstractAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AbstractAccessController.class, getLocale(), getTranslator()));
		this.link = link;
		this.velocity_root = Util.getPackageVelocityRoot(AbstractAccessController.class);
		
		mainVC = createVelocityContainer("access_method");
		putInitialPanel(mainVC);
	}

	protected void init(UserRequest ureq) {
		Price price = link.getOffer().getPrice();
		if (price != null && !price.isEmpty()) {
			String priceStr = "<span class=\"o_ac_method_price_ammount\">" + PriceFormat.fullFormat(price) + "</span>";
			if(acModule.isVatEnabled()) {
				BigDecimal vat = acModule.getVat();
				String vatStr = vat == null ? "" : vat.setScale(3, RoundingMode.HALF_EVEN).toPlainString();
				priceStr = translate("access.info.price.vat", new String[]{priceStr, vatStr});
			} else {
				priceStr = translate("access.info.price.noVat", new String[]{priceStr});
			}
			mainVC.contextPut("price", priceStr);
		}
		
		String description = link.getOffer().getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			if(!StringHelper.isHtml(description)) {
				description = Formatter.escWithBR(description).toString();
			}
			description = StringHelper.xssScan(description);
			mainVC.contextPut("description", description);
		}
		
		detailsCtrl = createDetailsController(ureq, getWindowControl(), link);
		listenTo(detailsCtrl);
		mainVC.put("details", detailsCtrl.getInitialComponent());
	}
	
	protected abstract Controller createDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess link);

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}

}
