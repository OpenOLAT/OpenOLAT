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
package org.olat.core.commons.services.export.manager;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.ExportMetadataToCurriculumElement;
import org.olat.core.commons.services.export.model.ExportMetadataToCurriculumElementImpl;
import org.olat.modules.curriculum.CurriculumElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ExportMetadataToCurriculumElementDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ExportMetadataToCurriculumElement createMetadataToCurriculumElement(ExportMetadata metadata, CurriculumElement curriculumElement) {
		ExportMetadataToCurriculumElementImpl metadataToElement = new ExportMetadataToCurriculumElementImpl();
		metadataToElement.setCreationDate(new Date());
		metadataToElement.setMetadata(metadata);
		metadataToElement.setCurriculumElement(curriculumElement);
		dbInstance.getCurrentEntityManager().persist(metadataToElement);
		return metadataToElement;
	}
	
	public int deleteRelations(ExportMetadata metadata) {
		if (metadata == null || metadata.getKey() == null) return 0;
		
		String query = "delete from exportmetadatatocurriculumelement rel where rel.metadata.key=:metadataKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("metadataKey", metadata.getKey())
				.executeUpdate();
	}
}
