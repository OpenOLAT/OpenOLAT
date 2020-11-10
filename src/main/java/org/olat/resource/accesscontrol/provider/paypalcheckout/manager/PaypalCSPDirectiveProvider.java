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
package org.olat.resource.accesscontrol.provider.paypalcheckout.manager;

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.csp.CSPDirectiveProvider;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PaypalCSPDirectiveProvider implements CSPDirectiveProvider {
	
	private static final List<String> sandboxedPaypal = List.of("https://www.paypal.com", "https://www.sandbox.paypal.com");
	private static final List<String> productionPaypal = List.of("https://www.paypal.com");

	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private PaypalCheckoutModule paypalCheckoutModule;

	@Override
	public Collection<String> getScriptSrcUrls() {
		return urls();
	}

	@Override
	public Collection<String> getImgSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getFontSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getConnectSrcUrls() {
		return urls();
	}

	@Override
	public Collection<String> getFrameSrcUrls() {
		return urls();
	}

	@Override
	public Collection<String> getMediaSrcUrls() {
		return null;
	}
	
	private Collection<String> urls() {
		if(acModule.isPaypalCheckoutEnabled()) {
			return paypalCheckoutModule.isSandbox() ? sandboxedPaypal : productionPaypal;
		}
		return null;
	}
}
