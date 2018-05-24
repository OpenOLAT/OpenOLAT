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
package org.olat.user.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.UserDataExport;
import org.olat.user.UserDataExportService;
import org.olat.user.UserDataExportable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDataExportServiceImpl implements UserDataExportService {
	
	private static final OLog log = Tracing.createLoggerFor(UserDataExportServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserDataExportDAO userDataExportDao;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	@Override
	public List<String> getExporterIds() {
		Map<String,UserDataExportable> exporters = CoreSpringFactory.getBeansOfType(UserDataExportable.class);
		return exporters.values().stream()
				.map(UserDataExportable::getExporterID)
				.collect(Collectors.toList());
	}

	@Override
	public void requestExportData(Identity identity, Collection<String> exportIds) {
		List<UserDataExport.ExportStatus> runningStatus = new ArrayList<>();
		runningStatus.add(UserDataExport.ExportStatus.requested);
		runningStatus.add(UserDataExport.ExportStatus.processing);
		List<UserDataExport> currentExport = userDataExportDao.getUserDataExport(identity, runningStatus);
		if(currentExport.isEmpty()) {
			UserDataExport dataExport = userDataExportDao.createExport(identity, exportIds, UserDataExport.ExportStatus.requested);
			dbInstance.commit();
			taskExecutorManager.execute(new UserDataExportTask(dataExport.getKey()));
		}
	}

	@Override
	public void exportData(Long requestKey) {
		UserDataExport dataExport = userDataExportDao.loadByKey(requestKey);
		if(dataExport == null) return;
		
		if(dataExport.getStatus() != UserDataExport.ExportStatus.requested) {
			return;
		}
		
		// start processing
		dataExport.setStatus(UserDataExport.ExportStatus.processing);
		dataExport = userDataExportDao.update(dataExport);
		dbInstance.commit();
		
		// make sure we doesn't double down the export
		File archiveDirectory = getArchiveDirectory(dataExport);
		File archive = new File(archiveDirectory, "data.zip");
		if(archive.exists()) {
			dataExport.setStatus(UserDataExport.ExportStatus.ready);
			userDataExportDao.update(dataExport);
			return; // already done
		}
		
		// export datas
		Identity identity = dataExport.getIdentity();
		Preferences preferences = identity.getUser().getPreferences();
		Locale locale = i18nManager.getLocaleOrDefault(preferences.getLanguage());

		ManifestBuilder manifest = ManifestBuilder.createBuilder();
		Set<String> exportIds = dataExport.getExportIds();
		Map<String,UserDataExportable> exporters = CoreSpringFactory.getBeansOfType(UserDataExportable.class);
		for(UserDataExportable exporter:exporters.values()) {
			if(exportIds.contains(exporter.getExporterID())) {
				try {
					exporter.export(identity, manifest, archiveDirectory, locale);
					dbInstance.commitAndCloseSession();
				} catch (Exception e) {
					log.error("Cannot export " + exporter.getClass().getSimpleName() + " for identity=" + identity.getKey(), e);
					dbInstance.rollbackAndCloseSession();
				}
			}
		}
		
		// save manifest
		manifest.write(new File(archiveDirectory, "imsmanifest.xml"));
		
		// make zip
		String[] files = archiveDirectory.list();
		Set<String> filenames = Arrays.stream(files).collect(Collectors.toSet());
		ZipUtil.zip(filenames, archiveDirectory, archive);
		
		UserDataExport reloadedDataExport = userDataExportDao.loadByKey(requestKey);
		reloadedDataExport.setStatus(UserDataExport.ExportStatus.ready);
		userDataExportDao.update(reloadedDataExport);
		dbInstance.commitAndCloseSession();
	}
	
	@Override
	public UserDataExport getCurrentData(IdentityRef identity) {
		return userDataExportDao.getLastUserDataExport(identity);
	}

	@Override
	public MediaResource getDownload(IdentityRef identity) {
		UserDataExport dataExport = getCurrentData(identity);
		if(dataExport.getStatus() == UserDataExport.ExportStatus.ready) {
			File archiveDirectory = getArchiveDirectory(dataExport);
			File archive = new File(archiveDirectory, "data.zip");
			if(archive.exists()) {
				return new ArchiveMediaResource(archive);
			}
		}
		return new NotFoundMediaResource();
	}
	
	private File getArchiveDirectory(UserDataExport dataExport) {
		File archivesDirectory = new File(WebappHelper.getUserDataRoot(), "export_usersdata");
		File archiveDirectory = new File(archivesDirectory, dataExport.getDirectory());
		if(!archiveDirectory.exists()) {
			archiveDirectory.mkdirs();
		}
		return archiveDirectory;
	}
	
	private static class ArchiveMediaResource extends FileMediaResource {
		
		public ArchiveMediaResource(File file) {
			super(file, true);
		}

		@Override
		public String getContentType() {
			return "application/zip";
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			// encode filename in ISO8859-1; does not really help but prevents from filename not being displayed at all
			// if it contains non-US-ASCII characters which are not allowed in header fields.
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''Archive.zip");
			hres.setHeader("Content-Description", "Archive.zip");
		}
	}
}
