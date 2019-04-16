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
package org.olat.core.commons.services.doceditor.onlyoffice.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 15 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ApiConfigBuilder {

	private static final OLog log = Tracing.createLoggerFor(ApiConfigBuilder.class);
	
	private final VFSMetadata vfsMetadata;
	private final Identity identity;
	private boolean edit;
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private OnlyOfficeService onlyOfficeService;
	@Autowired
	private UserManager userManager;
	
	private ApiConfigBuilder(VFSMetadata vfsMetadata, Identity identity) {
		this.vfsMetadata = vfsMetadata;
		this.identity = identity;
		CoreSpringFactory.autowireObject(this);
	}
	
	public static ApiConfigBuilder builder(VFSMetadata vfsMetadata, Identity identity) {
		return new ApiConfigBuilder(vfsMetadata, identity);
	}
	
	/**
	 * Default: true
	 *
	 * @param edit
	 * @return
	 */
	public ApiConfigBuilder withEdit(boolean edit) {
		this.edit = edit;
		return this;
	}
	
	public ApiConfig build() {
		String fileName = vfsMetadata.getFilename();

		ApiConfig apiConfig = new ApiConfig();
		apiConfig.setWidth("100%");
		apiConfig.setHeight("100%");
		apiConfig.setType("desktop");
		String suffix = FileUtils.getFileSuffix(fileName);
		String documentType = onlyOfficeService.getEditorDocumentType(suffix);
		apiConfig.setDocumentType(documentType);
		
		Document document = new Document();
		document.setFileType(suffix);
		String key = onlyOfficeService.getDocumentKey(vfsMetadata);
		document.setKey(key);
		document.setTitle(fileName);
		document.setUrl(getContentUrl());
		apiConfig.setDocument(document);
		
		Info info = new Info();
		String author = vfsMetadata.getAuthor() != null
				? userManager.getUserDisplayName(vfsMetadata.getAuthor())
				: null;
		info.setAuthor(author);
		info.setCreated(null); // not in metadata
		info.setFolder(null); // is often a hidden folder, so we do not want to show
		document.setInfo(info);
		
		Permissions permissions = new Permissions();
		permissions.setEdit(edit);
		// Default is true. We do not want to restrict
		permissions.setComment(null);
		permissions.setDownload(null);
		permissions.setFollForms(null);
		permissions.setPrint(null);
		permissions.setReview(null);
		document.setPermissions(permissions);
		
		EditorConfig editorConfig = new EditorConfig();
		editorConfig.setCallbackUrl(getCallbackUrl());
		String mode = edit? "edit": "view";
		editorConfig.setMode(mode);
		editorConfig.setLang(identity.getUser().getPreferences().getLanguage());
		apiConfig.setEditor(editorConfig);
		
		User user = new User();
		String name = userManager.getUserDisplayName(identity);
		user.setName(name);
		user.setId(identity.getKey().toString());
		editorConfig.setUser(user);
		
		return apiConfig;
	}

	public String buildJson() {
		try {
			ApiConfig apiConfig = build();
			return mapper.writeValueAsString(apiConfig);
		} catch (JsonProcessingException e) {
			log.error("", e);
		}
		return null;
	}

	private String getContentUrl() {
		StringBuilder fileUrl = getFileUrl();
		fileUrl.append("contents");
		return fileUrl.toString();
	}

	private String getCallbackUrl() {
		StringBuilder fileUrl = getFileUrl();
		fileUrl.append("callback");
		return fileUrl.toString();
	}

	private StringBuilder getFileUrl() {
		StringBuilder fileUrl = new StringBuilder();
		fileUrl.append(Settings.getServerContextPathURI());
		fileUrl.append(RestSecurityHelper.SUB_CONTEXT);
		fileUrl.append("/onlyoffice/files/");
		fileUrl.append(vfsMetadata.getUuid());
		fileUrl.append("/");
		return fileUrl;
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class ApiConfig {
		
		private String token;
		private String type;
		private String documentType;
		private String width;
		private String height;
		private Document document;
		private EditorConfig editorConfig;
		// not implemented yet
		// private final Events events;
		
		private ApiConfig() {
			//
		}
		
		public String getToken() {
			return token;
		}

		private void setToken(String token) {
			this.token = token;
		}

		public String getType() {
			return type;
		}

		private void setType(String type) {
			this.type = type;
		}

		public String getDocumentType() {
			return documentType;
		}

		private void setDocumentType(String documentType) {
			this.documentType = documentType;
		}

		public String getWidth() {
			return width;
		}

		private void setWidth(String width) {
			this.width = width;
		}

		public String getHeight() {
			return height;
		}

		private void setHeight(String height) {
			this.height = height;
		}

		public Document getDocument() {
			return document;
		}

		private void setDocument(Document document) {
			this.document = document;
		}

		public EditorConfig getEditorConfig() {
			return editorConfig;
		}

		private void setEditor(EditorConfig editorConfig) {
			this.editorConfig = editorConfig;
		}
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class Document {
		
		private String fileType;
		private String key;
		private String title;
		private String url;
		private Info info;
		private Permissions permissions;

		public String getFileType() {
			return fileType;
		}

		private void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String getKey() {
			return key;
		}

		private void setKey(String key) {
			this.key = key;
		}

		public String getTitle() {
			return title;
		}

		private void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		private void setUrl(String url) {
			this.url = url;
		}

		public Info getInfo() {
			return info;
		}

		private void setInfo(Info info) {
			this.info = info;
		}

		public Permissions getPermissions() {
			return permissions;
		}

		private void setPermissions(Permissions permissions) {
			this.permissions = permissions;
		}
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class Info {
		
		private String author;
		private String created;
		private String folder;
		// not implemented yed
		// private Object sharingSettings;

		public String getAuthor() {
			return author;
		}

		private void setAuthor(String author) {
			this.author = author;
		}

		public String getCreated() {
			return created;
		}

		private void setCreated(String created) {
			this.created = created;
		}

		public String getFolder() {
			return folder;
		}

		private void setFolder(String folder) {
			this.folder = folder;
		}
		
	}	

	@JsonInclude(Include.NON_NULL)
	public static class Permissions {
		
		private Boolean comment;
		private Boolean download;
		private Boolean edit;
		private Boolean print;
		private Boolean follForms;
		private Boolean review;
		
		public Boolean getComment() {
			return comment;
		}

		private void setComment(Boolean comment) {
			this.comment = comment;
		}

		public Boolean getDownload() {
			return download;
		}

		private void setDownload(Boolean download) {
			this.download = download;
		}

		public Boolean getEdit() {
			return edit;
		}
		
		private void setEdit(Boolean edit) {
			this.edit = edit;
		}

		public Boolean getPrint() {
			return print;
		}

		private void setPrint(Boolean print) {
			this.print = print;
		}

		public Boolean getFollForms() {
			return follForms;
		}

		private void setFollForms(Boolean follForms) {
			this.follForms = follForms;
		}

		public Boolean getReview() {
			return review;
		}

		private void setReview(Boolean review) {
			this.review = review;
		}
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class EditorConfig {
		
		private String callbackUrl;
		private String lang;
		private String mode;
		private User user;
		// not implemented yet
		// private Customization customization;
		// private Object recent;
		// private String createUrl;
		// private Embedded embedded;
		
		public String getCallbackUrl() {
			return callbackUrl;
		}

		private void setCallbackUrl(String callbackUrl) {
			this.callbackUrl = callbackUrl;
		}

		public String getLang() {
			return lang;
		}

		private void setLang(String lang) {
			this.lang = lang;
		}

		public String getMode() {
			return mode;
		}

		private void setMode(String mode) {
			this.mode = mode;
		}

		public User getUser() {
			return user;
		}

		private void setUser(User user) {
			this.user = user;
		}
		
	}
	
	@JsonInclude(Include.NON_NULL)
	public static class User {
		
		private String id;
		private String name;
		
		public String getId() {
			return id;
		}

		private void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		private void setName(String name) {
			this.name = name;
		}
		
	}
}
