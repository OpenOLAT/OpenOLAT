/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;

/**
 * 
 * Description:<br>
 * The mapper (not cacheable) for the redirect from Paypal
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalMapper.class);
	
	private final String businessPath;
	private final PaypalManager paypalManager;
	
	public PaypalMapper(String businessPath, PaypalManager paypalManager) {
		this.businessPath = businessPath;
		this.paypalManager = paypalManager;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		List<ContextEntry> entries = null;
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		try {
			entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
			if(relPath.indexOf(';') > 0) {
				relPath = relPath.substring(0, relPath.indexOf(';'));
			}
			if(relPath.indexOf('?') > 0) {
				relPath = relPath.substring(0, relPath.indexOf('?'));
			}
			String uuid = relPath.substring(1, relPath.length() - 5);
			paypalManager.updateTransaction(uuid);
			usess.putEntryInNonClearedStore("paypal-uuid", uuid);
		} catch (Exception e) {
			log.error("", e);
			usess.putEntryInNonClearedStore("paypal-mapper-error", Boolean.TRUE);
		}
		
		String resourceUrl = BusinessControlFactory.getInstance().getBusinessPathAsURIFromCEList(entries);
		MediaResource redirect = new RedirectMediaResource(Settings.getServerContextPathURI() + "/auth/" + resourceUrl);
		return redirect;
	}
}