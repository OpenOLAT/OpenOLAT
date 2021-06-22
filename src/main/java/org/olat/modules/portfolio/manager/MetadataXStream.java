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
package org.olat.modules.portfolio.manager;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.portfolio.Citation;
import org.olat.modules.portfolio.CitationSourceType;
import org.olat.modules.portfolio.model.CitationXml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * For XStream
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataXStream {

	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				Citation.class, CitationSourceType.class, CitationXml.class
			};
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("citation", org.olat.modules.portfolio.model.CitationXml.class);
		xstream.aliasType("citation", org.olat.modules.portfolio.model.CitationXml.class);
	}
	
	public static final XStream get() {
		return xstream;
	}
}
