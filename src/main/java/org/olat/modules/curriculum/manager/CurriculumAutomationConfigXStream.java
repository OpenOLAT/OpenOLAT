/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationConfigXStream {

	private static final Logger log = Tracing.createLoggerFor(CurriculumAutomationConfigXStream.class);

	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
			CurriculumAutomationConfig.class,
			CurriculumAutomationRule.class,
			AutomationContext.class,
			AutomationType.class,
			AutomationDependingOn.class,
			AutomationUnit.class,
			OffsetDirection.class,
			ArrayList.class,
			HashSet.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("automationConfig", CurriculumAutomationConfig.class);
		xstream.alias("rule", CurriculumAutomationRule.class);
	}

	private CurriculumAutomationConfigXStream() {
	}

	public static String toXML(CurriculumAutomationConfig config) {
		if (config == null) {
			return null;
		}
		return xstream.toXML(config);
	}

	public static CurriculumAutomationConfig fromXML(String xml) {
		if (xml == null || xml.isBlank()) {
			return null;
		}
		try {
			return (CurriculumAutomationConfig) xstream.fromXML(xml);
		} catch (Exception e) {
			log.warn("Cannot deserialize CurriculumAutomationConfig from XML.", e);
			return null;
		}
	}
}
