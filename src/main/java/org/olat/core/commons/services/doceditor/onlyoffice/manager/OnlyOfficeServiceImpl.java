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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.onlyoffice.ApiConfig;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeEditor;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.onlyoffice.model.ApiConfigImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.DocumentImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.EditorConfigImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.EmbeddedImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.InfoImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.PermissionsImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.UserImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.lock.LockResult;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeServiceImpl implements OnlyOfficeService {

	private static final Logger log = Tracing.createLoggerFor(OnlyOfficeServiceImpl.class);
	
	private static final DateFormat LAST_MODIFIED = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private static ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeSecurityService onlyOfficeSecurityService;
	@Autowired
	private DocEditorService documentEditorServie;
	@Autowired
	private DocEditorIdentityService identityService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager lockManager;

	@Override
	public boolean fileExists(String fileId) {
		return vfsRepositoryService.getItemFor(fileId) != null? true: false;
	}

	@Override
	public File getFile(String fileId) {
		VFSLeaf vfsLeaf = getVfsLeaf(fileId);
		if (vfsLeaf != null) {
			String uri = vfsLeaf.getMetaInfo().getUri();
			try {
				return Paths.get(new URL(uri).toURI()).toFile();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}

	@Override
	public VFSLeaf getVfsLeaf(String fileId) {
		VFSItem item = vfsRepositoryService.getItemFor(fileId);
		if (item instanceof VFSLeaf) {
			return (VFSLeaf) item;
		}
		return null;
	}

	@Override
	public ApiConfig getApiConfig(VFSMetadata vfsMetadata, Identity identity, Mode mode, boolean isDownloadEnabled, boolean versionControlled, String downloadUrl) {
		String fileName = vfsMetadata.getFilename();

		ApiConfigImpl apiConfig = new ApiConfigImpl();
		apiConfig.setWidth("100%");
		apiConfig.setHeight("100%");
		String type = "desktop";
		if (Mode.EMBEDDED.equals(mode)) {
			type = "embedded";
		}
		apiConfig.setType(type);
		String suffix = FileUtils.getFileSuffix(fileName);
		String documentType = getEditorDocumentType(suffix);
		apiConfig.setDocumentType(documentType);
		
		DocumentImpl document = new DocumentImpl();
		document.setFileType(suffix);
		String key = getDocumentKey(vfsMetadata);
		document.setKey(key);
		document.setTitle(fileName);
		document.setUrl(getContentUrl(vfsMetadata));
		apiConfig.setDocument(document);
		
		InfoImpl info = new InfoImpl();
		String author = vfsMetadata.getAuthor() != null
				? identityService.getUserDisplayName(vfsMetadata.getAuthor())
				: null;
		info.setAuthor(author);
		info.setCreated(null); // not in metadata
		info.setFolder(null); // is often a hidden folder, so we do not want to show
		document.setInfo(info);
		
		PermissionsImpl permissions = new PermissionsImpl();
		boolean edit = Mode.EDIT.equals(mode);
		permissions.setEdit(edit);
		permissions.setComment(true);
		permissions.setDownload(isDownloadEnabled);
		permissions.setFillForms(true);
		permissions.setPrint(true);
		permissions.setReview(true);
		document.setPermissions(permissions);
		
		EditorConfigImpl editorConfig = new EditorConfigImpl();
		String callbackUrl = getCallbackUrl(vfsMetadata, versionControlled);
		editorConfig.setCallbackUrl(callbackUrl);
		String modeConfig = edit? "edit": "view";
		editorConfig.setMode(modeConfig);
		editorConfig.setLang(identity.getUser().getPreferences().getLanguage());
		
		if (Mode.EMBEDDED.equals(mode)) {
			EmbeddedImpl embedded = new EmbeddedImpl();
			embedded.setSaveUrl(downloadUrl);
			editorConfig.setEmbedded(embedded);
		}
		apiConfig.setEditor(editorConfig);
		
		UserImpl user = new UserImpl();
		String name = identityService.getUserDisplayName(identity);
		user.setName(name);
		user.setId(identityService.getGlobalIdentityId(identity));
		editorConfig.setUser(user);
		
		String token = onlyOfficeSecurityService.getApiConfigToken(document, editorConfig);
		apiConfig.setToken(token);
		
		return apiConfig;
	}
	
	private String getContentUrl(VFSMetadata vfsMetadata) {
		StringBuilder fileUrl = getFileUrl(vfsMetadata);
		fileUrl.append("contents");
		return fileUrl.toString();
	}

	private String getCallbackUrl(VFSMetadata vfsMetadata, boolean versionControlled) {
		StringBuilder fileUrl = getFileUrl(vfsMetadata);
		fileUrl.append("callback");
		if (versionControlled) {
			fileUrl.append("?versionControlled=true");
		}
		return fileUrl.toString();
	}

	private StringBuilder getFileUrl(VFSMetadata vfsMetadata) {
		StringBuilder fileUrl = new StringBuilder();
		fileUrl.append(Settings.getServerContextPathURI());
		fileUrl.append(RestSecurityHelper.SUB_CONTEXT);
		fileUrl.append("/onlyoffice/files/");
		fileUrl.append(vfsMetadata.getUuid());
		fileUrl.append("/");
		return fileUrl;
	}

	private String getEditorDocumentType(String suffix) {
		return Formats.getEditorType(suffix);
	}

	private String getDocumentKey(VFSMetadata metadata) {
		String lastModified = LAST_MODIFIED.format(metadata.getFileLastModified());
		return metadata.getUuid() + "-" + lastModified;
	}
	
	@Override
	public String toJson(ApiConfig apiConfig) {
		try {
			return mapper.writeValueAsString(apiConfig);
		} catch (JsonProcessingException e) {
			log.error("", e);
		}
		return null;
	}

	@Override
	public boolean canUpdateContent(VFSLeaf vfsLeaf, Identity identity, String documentKey) {
		String currentDocumentKey = getDocumentKey(vfsLeaf.getMetaInfo());
		log.debug("ONLYOFFICE currentDokumentKey: {}", currentDocumentKey);
		log.debug("ONLYOFFICE documentKey:        {}", documentKey);
		return currentDocumentKey.equals(documentKey) && !isLockedForMe(vfsLeaf, identity);
	}

	@Override
	public boolean updateContent(VFSLeaf vfsLeaf, Identity identity, String url, boolean versionControlled) {
		log.debug("Update content from ONLYOFICE: " + url);
		boolean updated = false;
		
		String token = onlyOfficeSecurityService.getFileDonwloadToken();
		String autorization = "Bearer " + token;
		HttpGet request = new HttpGet(url);
		request.addHeader("Authorization", autorization);
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				InputStream content = httpResponse.getEntity().getContent();
				if (versionControlled && vfsLeaf.canVersion() == VFSConstants.YES) {
					updated = vfsRepositoryService.addVersion(vfsLeaf, identity, "OnlyOffice", content);
				} else {
					updated = VFSManager.copyContent(content, vfsLeaf, identity);
				}
			} else {
				log.warn("Update content from ONLYOFICE failed. URL: " + url);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		if (updated) {
			log.debug("File updated. File name: " + vfsLeaf.getName());
			refreshLock(vfsLeaf);
			vfsRepositoryService.resetThumbnails(vfsLeaf);
		}
		
		return updated;
	}
	
	private void refreshLock(VFSLeaf vfsLeaf) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null) {
			long inADay = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
			lock.setExpiresAt(inADay);
		}
	}

	@Override
	public boolean isEditLicenseAvailable() {
		Integer licenseEdit = onlyOfficeModule.getLicenseEdit();
		if (licenseEdit == null) return true;
		if (licenseEdit.intValue() <= 0) return false;
		
		Long accessCount = documentEditorServie.getAccessCount(OnlyOfficeEditor.TYPE, Mode.EDIT);
		return accessCount < licenseEdit.intValue();
	}

	@Override
	public Long getEditLicensesInUse() {
		return documentEditorServie.getAccessCount(OnlyOfficeEditor.TYPE, Mode.EDIT);
	}
	
	@Override
	public boolean isLockNeeded(Mode mode) {
		return Mode.EDIT.equals(mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, identity, VFSLockApplicationType.collaboration, OnlyOfficeEditor.TYPE);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity) {
		return lockManager.isLockedForMe(vfsLeaf, metadata, identity, VFSLockApplicationType.collaboration, OnlyOfficeEditor.TYPE);
	}

	@Override
	public LockResult lock(VFSLeaf vfsLeaf, Identity identity) {
		LockResult lock = lockManager.lock(vfsLeaf, identity, VFSLockApplicationType.collaboration, OnlyOfficeEditor.TYPE);
		log.debug("Locked file. File name: " + vfsLeaf.getName() + ", Identity: " + identity);
		return lock;
	}

	@Override
	public void unlock(VFSLeaf vfsLeaf) {
		LockInfo lock = lockManager.getLock(vfsLeaf);
		if (lock != null && OnlyOfficeEditor.TYPE.equals(lock.getAppName())) {
			lock.getTokens().clear();
			lockManager.unlock(vfsLeaf, VFSLockApplicationType.collaboration);
			log.debug("Unlocked file. File name: " + vfsLeaf.getName());
		}
	}

	@Override
	public boolean isSupportedFormat(String suffix, Mode mode) {
		return Formats.isSupportedFormat(suffix, mode);
	}

	@Override
	public Identity getIdentity(String identityId) {
		return identityService.getIdentity(identityId);
	}
	
}
