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
package org.olat.shibboleth.handler;

import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.stereotype.Component;

/**
 *
 * Initial date: 05.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class SpringShibbolethAttributeHandlerFactory implements ShibbolethAttributeHandlerFactory {

	private static final Logger log = Tracing.createLoggerFor(SpringShibbolethAttributeHandlerFactory.class);

	private static final String DEFAULT_HANDLER_NAME = "DoNothing";

	@Override
	public ShibbolethAttributeHandler getHandler(String handlerName) {
		ShibbolethAttributeHandler shibbolethAttributeHandler;
		try {
			shibbolethAttributeHandler = getHandlerByName(handlerName);
		} catch (Exception e) {
			if (StringHelper.containsNonWhitespace(handlerName)) {
				log.warn("ShibbolethAttributeHandler '" + handlerName
						+ "' does not exist. Using the Default ShibbolethAttributeHandler '" + DEFAULT_HANDLER_NAME
						+ "'.");
			}
			shibbolethAttributeHandler = getDefaultHandler();
		}
		return shibbolethAttributeHandler;
	}

	private ShibbolethAttributeHandler getHandlerByName(String handlerName) {
		return (ShibbolethAttributeHandler) CoreSpringFactory.getBean(handlerName);
	}

	private ShibbolethAttributeHandler getDefaultHandler() {
		return (ShibbolethAttributeHandler) CoreSpringFactory.getBean(DEFAULT_HANDLER_NAME);
	}

}
