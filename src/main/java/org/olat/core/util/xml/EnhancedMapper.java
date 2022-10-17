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
package org.olat.core.util.xml;

import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.collection.spi.PersistentList;
import org.hibernate.collection.spi.PersistentMap;

import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Remap some specific class as hibernate class to maintain compatibility
 * with other olat system and/or old versions
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EnhancedMapper extends MapperWrapper {
	
	public EnhancedMapper(MapperWrapper mapper) {
		super(mapper);
	}

	@Override
	public Class<?> realClass(String elementName) {
		if("org.hibernate.collection.PersistentBag".equals(elementName)) {
			return PersistentBag.class;
		} else if("org.hibernate.collection.PersistentList".equals(elementName)) {
			return PersistentList.class;
		} else if("org.hibernate.collection.PersistentMap".equals(elementName)) {
			return PersistentMap.class;
		} else {
			return super.realClass(elementName);
		}
	}
}
