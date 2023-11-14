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
package org.olat.core.commons.services.tag.manager;

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TagServiceImpl implements TagService {
	
	@Autowired
	private TagDAO tagDao;

	@Override
	public Tag getOrCreateTag(String displayName) {
		Tag tag = tagDao.loadTag(displayName);
		if (tag == null) {
			tag = tagDao.createTag(displayName);
		}
		return tag;
	}

	@Override
	public List<Tag> getOrCreateTags(Collection<String> displayNames) {
		List<Tag> tags = tagDao.loadTags(displayNames);
		List<String> existingDisplayNames = tags.stream().map(Tag::getDisplayName).toList();
		
		for (String displayName : displayNames) {
			if (!existingDisplayNames.contains(displayName)) {
				Tag tag = getOrCreateTag(displayName);
				tags.add(tag);
			}
		}
		
		return tags;
	}

}
