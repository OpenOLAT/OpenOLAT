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
package org.olat.resource.accesscontrol.provider.paypal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 *
 * @author srosse@ stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalIPNFilter implements Filter {
	
	private static final Logger log = Tracing.createLoggerFor(PaypalIPNFilter.class);

	@Override
	public void init(FilterConfig config) {
		//
	}
	
	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
		try{
			PaypalManager paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);
			verify(request, paypalManager);
			DBFactory.getInstance().commitAndCloseSession();
		} catch(Throwable t) {
			//we must log all errors, it's critical for this part of the payment
			log.error("Paypal IPN unexpected error", t);
		} finally {
			DBFactory.getInstance().closeSession();
		}
	}

	@SuppressWarnings("deprecation")
	private void verify(ServletRequest request, PaypalManager paypalManager) {
		try {
			//code from the Paypal example
			// read post from PayPal system and add 'cmd'	
			StringBuilder sb = new StringBuilder();
			sb.append("cmd=_notify-validate");
			Map<String,String> values = new HashMap<>();
			for(Enumeration<String> en = request.getParameterNames(); en.hasMoreElements(); ){
				String paramName = en.nextElement();
				String paramValue = request.getParameter(paramName);
				sb.append("&").append(paramName).append("=").append(URLEncoder.encode(paramValue));
				values.put(paramName, paramValue);
			}

			// post back to PayPal system to validate
			// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
			// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
			// and configured for older versions.
			URL u = new URL(paypalManager.getIpnVerificationUrl());
			URLConnection uc = u.openConnection();
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			PrintWriter pw = new PrintWriter(uc.getOutputStream());
			pw.println(sb.toString());
			pw.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String res = in.readLine();
			in.close();

			// assign posted variables to local variables
			if(res.equals("VERIFIED")) {
				// check that paymentStatus=Completed
				// check that txnId has not been previously processed
				// check that receiverEmail is your Primary PayPal email
				// check that paymentAmount/paymentCurrency are correct
				// process payment
				paypalManager.updateTransactionByNotification(values, true);
			} else if(res.equals("INVALID")) {
				// log for investigation
				paypalManager.updateTransactionByNotification(values, false);
			} else {
				log.error("Paypal IPN error: " + res + " with values: " + values);
			}
		} catch (Exception e) {
			log.error("Paypal IPN unexpected error", e);
		}
	}
}
