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
package org.olat.modules.mediasite.manager;

import java.util.Arrays;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.modules.mediasite.MediaSiteManager;
import org.springframework.stereotype.Service;

/**
 * Initial date: 25.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class MediaSiteManagerImpl implements MediaSiteManager {

	@Override
	public String parseAlias(String identifier) {
		if (!StringHelper.containsNonWhitespace(identifier)) {
			return null;
		}
		
		identifier = identifier.replace(" ", "");
		
		try {
			List<String> elements = Arrays.asList(identifier.split("/"));
			
			if (elements.size() == 1) {
				return elements.get(0);
			}
			
			if (elements.contains("presentations")) {
				return elements.get(elements.indexOf("presentations") + 1);
			} else if (elements.contains("Play")) {
				return elements.get(elements.indexOf("Play") + 1);
			} else if (elements.contains("channels")) {
				return elements.get(elements.indexOf("channels") + 1);
			} 
			
			String mediaId = elements.stream().filter(el -> el.startsWith("Launch")).findFirst().get();
			return mediaId.replace("Launch?mediasiteId=", "");
			
		} catch (Exception e) {}
		
		return null;
	}
	
}
