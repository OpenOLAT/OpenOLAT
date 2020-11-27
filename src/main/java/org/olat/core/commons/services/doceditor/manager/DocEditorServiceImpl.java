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
package org.olat.core.commons.services.doceditor.manager;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.commons.editor.fileeditor.FileEditor;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessRef;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorContextEntryControllerCreator;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.UserInfo;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocEditorServiceImpl implements DocEditorService, UserDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(DocEditorServiceImpl.class);
	
	private static final String CONTEXT_ENTRY_KEY = "Document";
	private static final String PROPERTY_CATEGOTY = "document.editor";
	private static final String PROPERTY_PREF_EDITOR = "pref.editor";

	@Autowired
	private List<DocEditor> editors;
	@Autowired
	private AccessDAO accessDao;
	@Autowired
	private UserInfoDAO userInfoDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private PropertyManager propertyManager;
	
	@PostConstruct
	private void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(CONTEXT_ENTRY_KEY,
				new DocEditorContextEntryControllerCreator(this));
	}
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, String suffix, Mode mode, boolean metadataAvailable,
			boolean collaborativeOnly) {
		if (mode == null) return false;
		
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(collaborativeFilter(collaborativeOnly))
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, metadataAvailable))
				.findFirst()
				.isPresent();
	}

	private Predicate<? super DocEditor> collaborativeFilter(boolean collaborativeOnly) {
		return docEditor -> (collaborativeOnly? docEditor.isCollaborative(): true);
	}

	@Override
	public List<DocEditor> getEditors(Identity identity, Roles roles, String suffix, Mode mode, boolean metadataAvailable) {
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, metadataAvailable))
				.collect(Collectors.toList());
	}

	@Override
	public List<DocEditor> getExternalEditors(Identity identity, Roles roles) {
		return editors.stream()
				.filter(editor -> !FileEditor.TYPE.equals(editor.getType()))
				.filter(DocEditor::isEnable)
				.filter(DocEditor::isEditEnabled)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<DocEditor> getEditor(String editorType) {
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.getType().equals(editorType))
				.findFirst();
	}
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, Mode mode, boolean metadataAvailable) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, metadataAvailable))
				.filter(editor -> !editor.isLockedForMe(vfsLeaf, identity, mode))
				.findFirst()
				.isPresent();
	}
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, VFSMetadata metadata, Mode mode) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, metadata != null))
				.filter(editor -> !editor.isLockedForMe(vfsLeaf, metadata, identity, mode))
				.findFirst()
				.isPresent();
	}

	@Override
	public boolean isEditorEnabled(Access access) {
		String app = access.getEditorType();
		for (DocEditor docEditor : editors) {
			if (app.equals(docEditor.getType()) && docEditor.isEnable()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getPreferredEditorType(Identity identity) {
		Property property = getPreferredEditorProperty(identity);
		return property != null? property.getStringValue(): null;
	}

	@Override
	public void setPreferredEditorType(Identity identity, String type) {
		Property property = getPreferredEditorProperty(identity);
		if (property == null) {
			property = propertyManager.createUserPropertyInstance(identity, PROPERTY_CATEGOTY, PROPERTY_PREF_EDITOR,
					null, null, type, null);
		} else {
			property.setStringValue(type);
		}
		propertyManager.saveProperty(property);
	}

	private Property getPreferredEditorProperty(Identity identity) {
		return propertyManager.findUserProperty(identity, PROPERTY_CATEGOTY, PROPERTY_PREF_EDITOR);
	}

	@Override
	public Access createAccess(Identity identity, Roles roles, DocEditorConfigs configs) {
		Date expiresAt = Date.from(Instant.now().plus(Duration.ofHours(10)));
		DocEditor editor = getPreferredEditor(identity, roles, configs);
		String editorType = editor != null? editor.getType(): "no-editor-found";
		return accessDao.createAccess(configs.getVfsLeaf().getMetaInfo(), identity, editorType, configs.getMode(),
				configs.isVersionControlled(), configs.isDownloadEnabled(), expiresAt);
	}
 
	private DocEditor getPreferredEditor(Identity identity, Roles roles, DocEditorConfigs configs) {
		// HTML is always edited in the internal Editor because of the special html code (TyneMCE, edu-sharing, ...)
		String filename = configs.getVfsLeaf().getName();
		String suffix = FileUtils.getFileSuffix(filename);
		if (FileEditor.HTML_EDITOR_SUFFIX.contains(suffix)) {
			return editors.stream().filter(editor -> FileEditor.TYPE.equals(editor.getType())).findFirst().get();
		}
		
		List<DocEditor> leafEditors = getEditors(identity, roles, suffix, configs.getMode(), configs.isMetaAvailable());
		if (leafEditors.isEmpty()) {
			log.warn("No document editor present for {}.", filename);
			return null;
		}
		
		boolean canSetPreferredEditor = getExternalEditors(identity, roles).size() >= 2;
		if (canSetPreferredEditor) {
			String preferredEditorType = getPreferredEditorType(identity);
			if (preferredEditorType != null) {
				for (DocEditor docEditor : leafEditors) {
					if (preferredEditorType.equals(docEditor.getType())) {
						return docEditor;
					}
				}
			}
		}
	
		return leafEditors.stream()
				.sorted((e1, e2) -> Integer.compare(e1.getPriority(), e2.getPriority()))
				.findFirst().get();
	}
	
	@Override
	public Access updatetExpiresAt(Access access, Date expiresAt) {
		return accessDao.updateExpiresAt(access, expiresAt);
	}

	@Override
	public Access updateMode(Access access, Mode mode) {
		return accessDao.updateMode(access, mode);
	}

	@Override
	public void deleteAccess(Access access) {
		if (access != null) {
			accessDao.delete(access);
		}
	}

	@Override
	public Access getAccess(AccessRef accessRef) {
		Access access = accessDao.loadAccess(accessRef);
		if (expired(access)) {
			accessDao.delete(accessRef);
			access = null;
		}
		return access;
	}
	
	private boolean expired(Access access) {
		return access != null && access.getExpiresAt() != null && access.getExpiresAt().before(new Date());
	}
	
	@Override
	public VFSLeaf getVfsLeaf(Access access) {
		VFSItem item = vfsRepositoryService.getItemFor(access.getMetadata());
		if (item instanceof VFSLeaf) {
			return (VFSLeaf) item;
		}
		return null;
	}

	@Override
	public List<Access> getAccesses(Mode mode) {
		return accessDao.getAccesses(mode);
	}

	@Override
	public Long getAccessCount(String editorType, VFSMetadata metadata, Identity identity) {
		return accessDao.getAccessCount(editorType, metadata, identity);
	}

	@Override
	public Long getAccessCount(String editorType, Mode mode) {
		return accessDao.getAccessCount(editorType, mode);
	}

	@Override
	public String getDocumentUrl(Access access) {
		String url = null;
		if (access != null) {
			Optional<DocEditor> editor = getEditor(access.getEditorType());
			if (editor.isPresent()) {
				DocEditor docEditor = editor.get();
				if (docEditor.hasDocumentBaseUrl()) {
					String documenBasetUrl = docEditor.getDocumentBaseUrl();
					url = documenBasetUrl + getDocumentPath(access);
				} else {
					url = createDocumentUrl(access);
				}
			} else {
				url = createDocumentUrl(access);
			}
		}
		return url;
	}
	// TODO DIspatcher
	private String createDocumentUrl(Access access) {
		StringBuilder sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI());
		appendDocumentPath(sb, access);
		return sb.toString();
	}
	
	private String getDocumentPath(Access access) {
		StringBuilder sb = new StringBuilder();
		appendDocumentPath(sb, access);
		return sb.toString();
	}

	private static final void appendDocumentPath(StringBuilder sb, Access access) {
		sb.append("/auth/")
			.append(CONTEXT_ENTRY_KEY)
			.append("/")
			.append(access.getKey());
	}

	@Override
	public String prepareDocumentUrl(UserSession userSession, DocEditorConfigs configs) {
		Access access = createAccess(userSession.getIdentity(), userSession.getRoles(), configs);
		String configKey = getConfigKey(access);
		userSession.putEntryInNonClearedStore(configKey, configs);
		return getDocumentUrl(access);
	}
	
	@Override
	public String getConfigKey(Access access) {
		return "access-key-" + access.getKey();
	}

	@Override
	public UserInfo createOrUpdateUserInfo(Identity identity, String info) {
		UserInfo userInfo = userInfoDao.load(identity);
		if (userInfo == null) {
			userInfo = userInfoDao.create(identity, info);
		} else {
			userInfo.setInfo(info);
			userInfo = userInfoDao.save(userInfo);
		}
		return userInfo;
	}

	@Override
	public void deleteUserInfo(Identity identity) {
		userInfoDao.delete(identity);
	}

	@Override
	public UserInfo getUserInfo(Identity identity) {
		return userInfoDao.load(identity);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		userInfoDao.delete(identity);
		accessDao.deleteByIdentity(identity);
	}

}
