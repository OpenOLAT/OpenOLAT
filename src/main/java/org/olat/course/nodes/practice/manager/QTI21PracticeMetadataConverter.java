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
package org.olat.course.nodes.practice.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.pool.QTI21MetadataConverter;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21PracticeMetadataConverter extends QTI21MetadataConverter {
	
	private final List<QItemType> itemTypes;
	private final Map<String,TaxonomyLevel> taxonomyLevelsMap;
	private final List<QEducationalContext> educationalContexts;
	
	public QTI21PracticeMetadataConverter(List<TaxonomyLevel> taxonomyLevels, QItemTypeDAO itemTypeDao, QEducationalContextDAO educationalContextDao) {
		super(null, null, null);
		if(taxonomyLevels == null) {
			taxonomyLevelsMap = Map.of();
		} else {
			taxonomyLevelsMap = taxonomyLevels.stream()
					.collect(Collectors.toMap(level -> level.getIdentifier().toLowerCase(), level -> level, (u, v) -> u));
		}		
		itemTypes = itemTypeDao.getItemTypes();
		educationalContexts = educationalContextDao.getEducationalContexts();
	}
	
	@Override
	public void createLicense(QuestionItemImpl poolItem, String licenseText, String licensor) {
		//
	}

	@Override
	public QEducationalContext toEducationalContext(String txt) {
		if(StringHelper.containsNonWhitespace(txt)) {
			for(QEducationalContext educationalContext:educationalContexts) {
				if(txt.equalsIgnoreCase(educationalContext.getLevel())) {
					return educationalContext;
				}
			}
		}
		return null;
	}

	@Override
	public QItemType toType(String txt) {
		if(StringHelper.containsNonWhitespace(txt)) {
			for(QItemType itemType:itemTypes) {
				if(txt.equalsIgnoreCase(itemType.getType())) {
					return itemType;
				}
			}
		}
		return null;
	}

	@Override
	public TaxonomyLevel toTaxonomy(List<String> cleanedPath) {
		TaxonomyLevel lowerLevel = null;
		if(!cleanedPath.isEmpty()) {
			String lastLevel = cleanedPath.get(cleanedPath.size() - 1).toLowerCase();
			return taxonomyLevelsMap.get(lastLevel);
		}
		return lowerLevel;
	}
}
