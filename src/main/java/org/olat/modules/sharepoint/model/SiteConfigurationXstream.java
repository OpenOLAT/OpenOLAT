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
package org.olat.modules.sharepoint.model;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteConfigurationXstream {
	
	private static final Logger log = Tracing.createLoggerFor(SiteConfigurationXstream.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				SitesAndDrivesConfiguration.class, SiteAndDriveConfiguration.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.aliasType("siteAndDrive", SitesAndDrivesConfiguration.class);
	}
	
	public static final SitesAndDrivesConfiguration fromXML(String configuration) {
		try {
			if(StringHelper.containsNonWhitespace(configuration)) {
				return (SitesAndDrivesConfiguration)xstream.fromXML(configuration);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return new SitesAndDrivesConfiguration();
	}
	
	public static final String toXML(SitesAndDrivesConfiguration configuration) {
		try {
			return xstream.toXML(configuration);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
