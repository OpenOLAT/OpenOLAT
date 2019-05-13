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
package org.olat.ims.qti21.manager;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.PreWarm;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentTestPreWarm implements PreWarm {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentTestPreWarm.class);

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	
	@Override
	public void run() {
		long start = System.nanoTime();
		FileResourceManager frm = FileResourceManager.getInstance();
		List<RepositoryEntry> entries = repositoryEntryDao
				.getLastUsedRepositoryEntries(ImsQTI21Resource.TYPE_NAME, 0, 20);
		for(RepositoryEntry entry:entries) {
			try {
				File fUnzippedDirRoot = frm.unzipFileResource(entry.getOlatResource());
				qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
			} catch (RuntimeException e) {
				log.error("Loading the AssessmentTest of repository entry: " + entry.getKey() + " (" + entry.getDisplayname() + ")", e);
			}
		}
		log.info(entries.size() + " AssessmentTest preloaded in (ms): " + CodeHelper.nanoToMilliTime(start));
	}
}
