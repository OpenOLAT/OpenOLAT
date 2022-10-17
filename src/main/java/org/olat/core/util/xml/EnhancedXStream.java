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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.collection.spi.PersistentList;
import org.hibernate.collection.spi.PersistentMap;
import org.hibernate.collection.spi.PersistentSet;
import org.hibernate.collection.spi.PersistentSortedMap;
import org.hibernate.collection.spi.PersistentSortedSet;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This implementation of XStream automatically convert hibernate list, set and
 * map to standard java collections and convert by import / deserialization the
 * old hibernate 3 collection packages to the new one.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class EnhancedXStream extends XStream {

	EnhancedXStream(boolean export) {
		super();

		if (export) {
			addDefaultImplementation(PersistentList.class, List.class);
			addDefaultImplementation(PersistentBag.class, List.class);
			addDefaultImplementation(PersistentMap.class, Map.class);
			addDefaultImplementation(PersistentSortedMap.class, Map.class);
			addDefaultImplementation(PersistentSet.class, Set.class);
			addDefaultImplementation(PersistentSortedSet.class, Set.class);
			addDefaultImplementation(ArrayList.class, List.class);

			registerConverter(new CollectionConverter(getMapper()) {
				@Override
				public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
					return PersistentList.class == type || PersistentBag.class == type;
				}
			});

			registerConverter(new MapConverter(getMapper()) {
				@Override
				public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
					return PersistentMap.class == type;
				}
			});
		}
	}

	@Override
	protected MapperWrapper wrapMapper(MapperWrapper next) {
		return new EnhancedMapper(next);
	}

}
