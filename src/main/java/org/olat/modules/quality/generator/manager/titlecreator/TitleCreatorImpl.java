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
package org.olat.modules.quality.generator.manager.titlecreator;

import static java.util.Arrays.asList;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.TitleCreatorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TitleCreatorImpl implements TitleCreator {
	
	private static final Logger log = Tracing.createLoggerFor(TitleCreatorImpl.class);

	private static final String PREFIX = "$";

	private static final TitleCreatorHandler NOT_FOUND_HANDLER = new NotFoundHandler();
	
	private VelocityEngine velocityEngine;
	
	@Autowired
	private List<TitleCreatorHandler> handlers;

	@PostConstruct
	public void afterPropertiesSet() {
		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
	}
	
	@Override
	public List<String> getIdentifiers(Collection<Class<?>> classes) {
		Set<Class<?>> interfaces = new HashSet<>();
		for (Class<?> c: classes) {
			interfaces.addAll(asList(c.getInterfaces()));
			interfaces.add(c);
		}
		
		List<String> allIdentifiers = new ArrayList<>();
		for (Class<?> clazz: interfaces) {
			TitleCreatorHandler handler = getHandler(clazz);
			List<String> identifiers = handler.getIdentifiers();
			for (String identifier: identifiers) {
				allIdentifiers.add(addPrefix(identifier));
			}
		}

		return allIdentifiers;
	}
	
	private String addPrefix(String idenitfierString) {
		return new StringBuilder(PREFIX).append(idenitfierString).toString();
	}

	@Override
	public String merge(String template, Collection<?> objects) {
		VelocityContext context = new VelocityContext();
		for (Object object: objects) {
			TitleCreatorHandler handler = getHandler(object.getClass());
			handler.mergeContext(context, object);
		}
		return merge(template, context);
	}

	private String merge(String template, VelocityContext context) {
		boolean result = false;
		String merged = "";
		try(Reader in = new StringReader(template);
			StringWriter out = new StringWriter()) {
			result = velocityEngine.evaluate(context, out, "mailTemplate", in);
			out.flush();
			merged = out.toString();
		} catch (Exception e) {
			result = false;
			log.error("", e);
		}
		return result ? merged : null;
	}
	
	private TitleCreatorHandler getHandler(Class<?> clazz) {
		for (TitleCreatorHandler handler: handlers) {
			if (handler.canHandle(clazz)) {
				return handler;
			}
		}
		return NOT_FOUND_HANDLER;
	}
	
}
