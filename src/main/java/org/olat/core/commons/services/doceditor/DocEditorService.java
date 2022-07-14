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
package org.olat.core.commons.services.doceditor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DocEditorService {
	
	/**
	 * Check if file with a specific suffix is supported by any enabled editor.
	 * @param identity
	 * @param roles
	 * @param suffix
	 * @param mode
	 * @param metadataAvailable
	 * @param collaborativeOnly 
	 * @return
	 */
	public boolean hasEditor(Identity identity, Roles roles, String suffix, Mode mode, boolean metadataAvailable, boolean collaborativeOnly);
	
	/**
	 * Get all enabled editors which support a file with a specific suffix.
	 * @param identity 
	 * @param roles 
	 * @param suffix
	 * @param mode
	 * @param metadataAvailable
	 *
	 * @return
	 */
	public List<DocEditor> getEditors(Identity identity, Roles roles, String suffix, Mode mode, boolean metadataAvailable);
	
	public List<DocEditor> getExternalEditors(Identity identity, Roles roles);

	/**
	 * Get the editor of a specific type.
	 * 
	 * @param editorType
	 * @return
	 */
	public Optional<DocEditor> getEditor(String editorType);
	
	/**
	 * Checks whether a vfsLeaf can be opened in any editor by a user and in a
	 * specific mode. This method checks not only if a file format is supported but
	 * also e.g. if the vfsLeaf is not locked by an other editor or user.
	 * 
	 * @param identity
	 * @param roles 
	 * @param vfsLeaf
	 * @param mode
	 * @param metadataAvailable 
	 * @return
	 */
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, Mode mode, boolean metadataAvailable);
	
	/**
	 * This is an optimized version of the method for list where the metadata is
	 * loaded for a whole directory.
	 * 
	 * @param identity The user identity
	 * @param roles The roles of the user
	 * @param vfsLeaf The file
	 * @param metadata The metadata
	 * @param mode 
	 * @return
	 */
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, VFSMetadata metadata, Mode mode);

	/**
	 * Checks whether the editor of the access is enabled or not. Roles are ignored.
	 *
	 * @param access
	 * @return
	 */
	public boolean isEditorEnabled(Access access);
	
	public String getPreferredEditorType(Identity identity);
	
	public void setPreferredEditorType(Identity identity, String type);

	public Access createAccess(Identity identity, Roles roles, DocEditorConfigs configs);
	
	public Access updateMode(Access access, Mode mode);

	public Access updatetExpiresAt(Access access, Date expiresAt);
	
	public Access updateEditStart(Access access);

	public void deleteAccess(Access access);

	public Access getAccess(AccessRef accessRef);

	public List<Access> getAccesses(AccessSearchParams params);

	public Long getAccessCount(String editorType, VFSMetadata metadata, Identity identity);

	public Long getAccessCount(String editorType, Mode mode);
	
	/**
	 * 
	 *
	 * @param access
	 * @return
	 */
	public VFSLeaf getVfsLeaf(Access access);

	/**
	 * Get the URL pointing to the document of the access. Before open the document you have to 
	 * {@link #prepareDocumentUrl(UserSession, DocEditorConfigs)}.
	 *
	 * @param access
	 * @return
	 */
	public String getDocumentUrl(Access access);

	/**
	 * Prepares to open a document, e.g. created the access and puts the necessary values to the UserSession.
	 *
	 * @param userSession
	 * @param configs
	 * @return the URL pointing to the document
	 */
	public String prepareDocumentUrl(UserSession userSession, DocEditorConfigs configs);
	
	public String getConfigKey(Access access);
	
	public UserInfo createOrUpdateUserInfo(Identity identity, String info);
	
	public void deleteUserInfo(Identity identity);
	
	public UserInfo getUserInfo(Identity identity);


	/**
	 * Get a CSS class for the specified mode
	 * @param mode
	 * @param fileName
	 * @return The CSS class
	 */
	public String getModeIcon(Mode mode, String fileName);

	/**
	 * Get the label for a button representing the current mode and file type
	 * @param mode
	 * @param fileName
	 * @param translator A translator that can translate from the DocEditorController namespace
	 * @return 
	 */
	public String getModeButtonLabel(Mode mode, String fileName, Translator trans);

}
