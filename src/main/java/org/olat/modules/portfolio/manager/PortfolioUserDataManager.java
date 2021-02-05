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
package org.olat.modules.portfolio.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.ui.export.ExportBinderAsCPResource;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.resource.OLATResource;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioUserDataManager implements DeletableGroupData, UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private PortfolioService portfolioService;
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(group);
		Property mapKeyProperty = npm.findProperty(null, null, CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_PORTFOLIO);
		if (mapKeyProperty != null) {
			Long mapKey = mapKeyProperty.getLongValue();
			String version = mapKeyProperty.getStringValue();
			if("2".equals(version)) {
				Binder binder = binderDao.loadByKey(mapKey);
				if(binder != null) {
					portfolioService.deleteBinder(binder);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<Binder> ownedBinders = binderDao.getAllBindersAsOwner(identity);
		for(Binder ownedBinder:ownedBinders) {
			OLATResource resource = ((BinderImpl)ownedBinder).getOlatResource();
			if(resource != null) {
				continue;// this is a template
			}

			List<Identity> owners = binderDao.getMembers(ownedBinder, PortfolioRoles.owner.name());
			if(owners.size() == 1 && owners.get(0).getKey().equals(identity.getKey())) {
				try {
					portfolioService.deleteBinder(ownedBinder);
					dbInstance.commit();
				} catch (Exception e) {
					log.error("Cannot delete binder: {}", ownedBinder.getKey());
					dbInstance.rollbackAndCloseSession();
				}
			}
		}

		List<Media> medias = mediaDao.load(identity);
		for(Media media:medias) {
			if(mediaDao.isUsed(media)) {
				log.info(Tracing.M_AUDIT, "Cannot delete media because used: {}", media.getKey());
			} else {
				portfolioService.deleteMedia(media);
			}
		}
	}

	@Override
	public String getExporterID() {
		return "binders";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File portfolioArchive = new File(archiveDirectory, "Portfolios");
		portfolioArchive.mkdir();
		exportBinders(identity, portfolioArchive, locale);
		exportMedias(identity, manifest, portfolioArchive, locale);
	}
	
	private void exportMedias(Identity identity, ManifestBuilder manifest, File portfolioArchive, Locale locale) {
		File mediasArchive = new File(portfolioArchive, "Media");
		mediasArchive.mkdir();
		
		List<Media> medias = mediaDao.load(identity);
		dbInstance.commitAndCloseSession();
		for(Media media:medias) {
			MediaHandler handler = portfolioService.getMediaHandler(media.getType());
			handler.export(media, manifest, mediasArchive, locale);
		}
	}
	
	private void exportBinders(Identity identity, File portfolioArchive, Locale locale) {
		File bindersArchive = new File(portfolioArchive, "Binders");
		bindersArchive.mkdir();
		List<Binder> binders = binderDao.getOwnedBinders(identity);
		for(Binder binder:binders) {
			exportBinder(binder, identity, bindersArchive, locale);
		}
	}
	
	private void exportBinder(Binder binder, Identity identity, File portfolioArchive, Locale locale) {
		String secureLabel = StringHelper.transformDisplayNameToFileSystemName(binder.getTitle());
		File binderFile = new File(portfolioArchive, secureLabel + ".zip");
		try(OutputStream out = new FileOutputStream(binderFile)) {
			UserRequest ureq = new SyntheticUserRequest(identity, locale);
			new ExportBinderAsCPResource(binder, ureq, locale).export(binder, out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		if(binderFile.length() > 0) {
			File binderDir = new File(portfolioArchive, secureLabel);
			binderDir.mkdirs();
			ZipUtil.unzip(binderFile, binderDir);
		}
		try {
			Files.deleteIfExists(binderFile.toPath());
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
