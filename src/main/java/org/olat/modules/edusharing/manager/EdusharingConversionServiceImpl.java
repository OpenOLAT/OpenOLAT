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
package org.olat.modules.edusharing.manager;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.EdusharingConversionService;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.NodeIdentifier;
import org.olat.modules.edusharing.model.NodeIdentifierImpl;
import org.olat.modules.edusharing.model.SearchResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 12 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingConversionServiceImpl implements EdusharingConversionService {
	
	private final ObjectMapper mapper = new ObjectMapper();

	private String objectToJson(Object o) throws EdusharingException {
		try {
			return mapper.writeValueAsString(o);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public String toEdusharingCourseId(OLATResourceable ores) {
		// Valid id has to fulfill the regex: ^[a-z0-9-]+$
		return ores.getResourceableTypeName() + ores.getResourceableId();
	}
	
	@Override
	public SearchResult toSearchResult(UserRequest ureq) {
		return SearchResultConverter.fromUserRequest(ureq);
	}

	@Override
	public String toJson(SearchResult searchResult) throws EdusharingException {
		return objectToJson(searchResult);
	}

	@Override
	public NodeIdentifier toNodeIdentifier(String objectUrl) {
		if (StringHelper.containsNonWhitespace(objectUrl)) {
			String[] split = objectUrl.split("/");
			if (split.length == 4) {
				return new NodeIdentifierImpl(split[2], split[3]);
			}
		}
		return new NodeIdentifierImpl(null, null);
	}
}
