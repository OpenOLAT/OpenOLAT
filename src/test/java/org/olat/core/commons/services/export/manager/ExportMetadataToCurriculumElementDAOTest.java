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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.ExportMetadataToCurriculum;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ExportMetadataToCurriculumElementDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportMetadataDAO exportMetadataDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ExportMetadataToCurriculumDAO exportMetadataToCurriculumDao;
	
	@Test
	public void createRelation() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Curriculum curriculum = curriculumService.createCurriculum("MD-1", "METADATA 1", null, false, null);
		
		String title = "Some meta export relations";
		ExportMetadata metadata = exportMetadataDao.createMetadata(title, null, null, ArchiveType.CURRICULUM, null, false,
				null, null, null, actor, null);
		dbInstance.commit();
		
		ExportMetadataToCurriculum rel = exportMetadataToCurriculumDao.createMetadataToCurriculum(metadata, curriculum);
		metadata.getCurriculums().add(rel);
		metadata = exportMetadataDao.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		ExportMetadata reloadedMetadata = exportMetadataDao.getMetadataByKey(metadata.getKey());
		Assert.assertNotNull(reloadedMetadata);
		Assertions.assertThat(reloadedMetadata.getCurriculums())
			.hasSize(1)
			.containsExactly(rel)
			.map(ExportMetadataToCurriculum::getCurriculum)
			.containsExactly(curriculum);
	}
	
}
