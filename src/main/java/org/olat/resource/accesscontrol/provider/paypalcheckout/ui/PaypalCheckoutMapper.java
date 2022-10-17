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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalCheckoutMapper.class);
	
	private final String businessPath;
	private final PaypalCheckoutManager paypalManager;
	
	public PaypalCheckoutMapper(String businessPath, PaypalCheckoutManager paypalManager) {
		this.businessPath = businessPath;
		this.paypalManager = paypalManager;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		List<ContextEntry> entries = null;
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		try {
			entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
			if(relPath.indexOf(';') >= 0) {
				relPath = relPath.substring(0, relPath.indexOf(';'));
			}
			if(relPath.indexOf('?') >= 0) {
				relPath = relPath.substring(0, relPath.indexOf('?'));
			}
			String uuid = relPath.substring(1, relPath.length() - 5);
			paypalManager.updateTransaction(uuid);
			log.info("Returned uuid:{}", uuid);
			usess.putEntryInNonClearedStore("paypal-uuid", uuid);
		} catch (Exception e) {
			log.error("", e);
			usess.putEntryInNonClearedStore("paypal-mapper-error", Boolean.TRUE);
		}
		
		String resourceUrl = BusinessControlFactory.getInstance().getBusinessPathAsURIFromCEList(entries);
		return new RedirectMediaResource(Settings.getServerContextPathURI() + "/auth/" + resourceUrl);
	}
}