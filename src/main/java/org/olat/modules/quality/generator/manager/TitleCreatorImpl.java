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
package org.olat.modules.quality.generator.manager;

import static org.olat.core.util.StringHelper.blankIfNull;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.quality.generator.TitleCreator;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TitleCreatorImpl implements TitleCreator {
	
	private static final OLog log = Tracing.createLoggerFor(TitleCreatorImpl.class);

	private static final String PREFIX = "$";
	private static final String SEPARATOR = ", ";
	static final String CURRICULUM_ELEMENT_DISPLAY_NAME = "curEleDisplayName";
	static final String CURRICULUM_ELEMENT_IDENTIFIER = "curEleIdentifier";
	static final String CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME = "curEleTypeDisplayName";
	static final String CURRICULUM_DISPLAY_NAME = "curDisplayName";
	
	private VelocityEngine velocityEngine;
	
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
	public String getMergeCurriculumElementIdentifiers() {
		return new StringBuilder(256)
				.append(PREFIX).append(CURRICULUM_ELEMENT_DISPLAY_NAME).append(SEPARATOR)
				.append(PREFIX).append(CURRICULUM_ELEMENT_IDENTIFIER).append(SEPARATOR)
				.append(PREFIX).append(CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME).append(SEPARATOR)
				.append(PREFIX).append(CURRICULUM_DISPLAY_NAME)
				.toString();
	}

	@Override
	public String mergeCurriculumElement(String template, CurriculumElement element) {
		VelocityContext context = getContext(element);
		return merge(template, context);
	}
	
	private VelocityContext getContext(CurriculumElement element) {
		VelocityContext context = new VelocityContext();
		context.put(CURRICULUM_ELEMENT_DISPLAY_NAME, blankIfNull(element.getDisplayName()));
		context.put(CURRICULUM_ELEMENT_IDENTIFIER, blankIfNull(element.getIdentifier()));
		
		Curriculum curriculum = element.getCurriculum();
		if (curriculum != null) {
			context.put(CURRICULUM_DISPLAY_NAME, blankIfNull(curriculum.getDisplayName()));
		}
		
		CurriculumElementType type = element.getType();
		if (type != null) {
			context.put(CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME, blankIfNull(type.getDisplayName()));
		}
		
		return context;
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
	
}
