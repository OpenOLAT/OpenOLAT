package org.olat.ims.qti21.manager;

import java.io.File;
import java.util.List;

import org.olat.core.configuration.PreWarm;
import org.olat.core.logging.OLog;
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
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentTestPreWarm.class);

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
				qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot);
			} catch (RuntimeException e) {
				log.error("Loading the AssessmentTest of repository entry: " + entry.getKey() + " (" + entry.getDisplayname() + ")", e);
			}
		}
		log.info(entries.size() + " AssessmentTest preloaded in (ms): " + CodeHelper.nanoToMilliTime(start));
	}
}
