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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.user.UserDataExport;
import org.olat.user.UserDataExportService;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.ui.data.UserDataController;
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
	
	private static final Logger log = Tracing.createLoggerFor(UserDataExportServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailService;
	@Autowired
	private UserManager userManager;
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
	public void requestExportData(Identity identity, Collection<String> exportIds, Identity actingIdentity) {
		List<UserDataExport.ExportStatus> runningStatus = new ArrayList<>();
		runningStatus.add(UserDataExport.ExportStatus.requested);
		runningStatus.add(UserDataExport.ExportStatus.processing);
		List<UserDataExport> currentExport = userDataExportDao.getUserDataExport(identity, runningStatus);
		if(currentExport.isEmpty()) {
			UserDataExport dataExport = userDataExportDao.createExport(identity, exportIds,
					UserDataExport.ExportStatus.requested, actingIdentity);
			dbInstance.commit();
			taskExecutorManager.execute(new UserDataExportTask(dataExport.getKey()));
			
			List<UserDataExport> previousExports = userDataExportDao.getUserDataExports(identity);
			for(UserDataExport previousExport:previousExports) {
				if(!previousExport.equals(dataExport)) {
					dbInstance.commitAndCloseSession();
					deleteExport(previousExport);
				}
			}
		}
	}
	
	@Override
	public void deleteByDate(Date date) {
		List<UserDataExport> dataExports = userDataExportDao.getUserDataExportBefore(date);
		for(UserDataExport dataExport:dataExports) {
			deleteExport(dataExport);
		}
	}
	
	private void deleteExport(UserDataExport dataExport) {
		log.info(Tracing.M_AUDIT, "Delete user data export: {}", dataExport.getIdentity().getKey());
		dbInstance.commitAndCloseSession();
		File archiveDirectory = getArchiveDirectory(dataExport);
		FileUtils.deleteDirsAndFiles(archiveDirectory, true, true);
		userDataExportDao.delete(dataExport);
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
		File[] files = archiveDirectory.listFiles(SystemFileFilter.DIRECTORY_FILES);
		Set<String> filenames = Arrays.stream(files).map(File::getName).collect(Collectors.toSet());
		ZipUtil.zip(filenames, archiveDirectory, archive, false);
		// delete the temporary files
		for(File file:files) {
			FileUtils.deleteDirsAndFiles(file, true, true);
		}

		// export ready
		UserDataExport reloadedDataExport = userDataExportDao.loadByKey(requestKey);
		reloadedDataExport.setStatus(UserDataExport.ExportStatus.ready);
		userDataExportDao.update(reloadedDataExport);
		dbInstance.commitAndCloseSession();
		
		// send the mail
		if(reloadedDataExport.getRequestBy() != null) {
			sendEmail(dataExport);
		}
	}
	
	private void sendEmail(UserDataExport dataExport) {
		Identity to = dataExport.getRequestBy();
		MailerResult result = new MailerResult();
		Locale locale = i18nManager.getLocaleOrDefault(to.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(UserDataController.class, locale);
		
		String fullName = userManager.getUserDisplayName(dataExport.getIdentity());
		String url = getDownloadURL(dataExport.getIdentity());
		String[] args = new String[] {
				fullName, // 0
				url// 1
		};
		String subject = translator.translate("export.user.data.ready.subject", args);
		String text = translator.translate("export.user.data.ready.text", args);
		
		MailTemplate template = new MailTemplate(subject, text, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
				vContext.put("fullName", fullName);
				vContext.put("fullname", fullName);
				vContext.put("url", url);
			}
		};
		MailBundle bundle = mailService.makeMailBundle(new MailContextImpl(), dataExport.getRequestBy(), template, null, null, result);
		if(bundle != null) {
			mailService.sendMessage(bundle);
		}
	}
	
	@Override
	public UserDataExport getCurrentData(IdentityRef identity) {
		return userDataExportDao.getLastUserDataExport(identity);
	}
	
	@Override
	public String getDownloadURL(Identity identity) {
		String businessPath = "[Identity:" + identity.getKey() + "][mysettings:0][Data:0]";
		return BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
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
