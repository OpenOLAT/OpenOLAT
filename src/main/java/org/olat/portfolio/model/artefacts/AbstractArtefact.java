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
package org.olat.portfolio.model.artefacts;

import java.io.Serializable;
import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Description:<br>
 * used for common stuff of all types of artefact
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class AbstractArtefact extends PersistentObject implements Serializable, OLATResourceable {

	private static final long serialVersionUID = -1966363957300570702L;


	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableId()
	 */
	@Override
	public Long getResourceableId() {
		return getKey();
	}



	private String title;
	private String description;
	private int signature;
	private String businessPath;
	private String fulltextContent;
	private String reflexion;
	private String source;
	private Date collectionDate;
	private VFSContainer fileSourceContainer; 
	
	private Identity author;
	

	/**
	 * @return Returns the title.
	 * @uml.property name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter of the property <tt>title</tt>
	 * 
	 * @param title The title to set.
	 * @uml.property name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the description.
	 * @uml.property name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter of the property <tt>description</tt>
	 * 
	 * @param description The description to set.
	 * @uml.property name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the signature int identifier.
	 * for the meaning of the levels, see setSignature().
	 * @uml.property name="signature"
	 */
	public int getSignature() {
		return signature;
	}

	/**
	 * Setter of the property <tt>signature</tt>
	 * there are several levels of the authenticity of an artefact.
	 * 0=no guarantee - 100 really sure, with spaces between for improvements.
	 * 
	 * 90 - OLAT system generated source (certificate)
	 * 80 - OLAT author generated source (test-results)
	 * 70 - users personal content (forum post)
	 * 50 - user contributed to the content (wiki page)
	 * 30 - file upload from an user
	 * 20 - text upload by user
	 * 
	 * @param signature The signature to set.
	 * @uml.property name="signature"
	 */
	public void setSignature(int signature) {
		this.signature = signature;
	}

	/**
	 * @return Returns the businessPath.
	 * @uml.property name="businessPath"
	 */
	public String getBusinessPath() {
		return businessPath;
	}

	/**
	 * Setter of the property <tt>businessPath</tt>
	 * 
	 * @param businessPath The businessPath to set.
	 * @uml.property name="businessPath"
	 */
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	/**
	 * @return Returns the author.
	 * @uml.property name="author"
	 */
	public Identity getAuthor() {
		return author;
	}

	/**
	 * Setter of the property <tt>author</tt>
	 * 
	 * @param author The author to set.
	 * @uml.property name="author"
	 */
	public void setAuthor(Identity author) {
		this.author = author;
	}


	/**
	 * attention!: this should not be read directly, use manager to get fulltext
	 * if fulltext is larger than db-size its persisted on fs, therefore you need the manager to read it!
	 * @return Returns the fulltextContent.
	 */
	public String getFulltextContent() {
		return fulltextContent;
	}

	/**
	 * Setter of the property <tt>fulltextContent</tt>
	 * 
	 * @param fulltextContent The fulltextContent to set.
	 * @uml.property name="fulltextContent"
	 */
	public void setFulltextContent(String fulltextContent) {
		this.fulltextContent = fulltextContent;
	}

	/**
	 * @return Returns the reflexion.
	 * @uml.property name="reflexion"
	 */
	public String getReflexion() {
		return reflexion;
	}

	/**
	 * Setter of the property <tt>reflexion</tt>
	 * 
	 * @param reflexion The reflexion to set.
	 * @uml.property name="reflexion"
	 */
	public void setReflexion(String reflexion) {
		this.reflexion = reflexion;
	}

	/**
	 * despite the businesspath, save some additional information about the
	 * artefact source
	 * 
	 * @uml.property name="source"
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Setter of the property <tt>source</tt>
	 * 
	 * @param source The source to set.
	 * @uml.property name="source"
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return Returns the collectionDate.
	 * @uml.property name="collectionDate"
	 */
	public Date getCollectionDate() {
		return collectionDate;
	}

	/**
	 * Setter of the property <tt>collectionDate</tt>
	 * @param collectionDate The collectionDate to set.
	 * @uml.property name="collectionDate"
	 */
	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}

	/**
		 */
	public abstract String getIcon();

	/**
	 * set a file source container to transport this info during collection wizzard
	 * this is not persisted on artefact
	 * if set, files from within this container will be copied to artefacts folder on save.
	 * @param fileSourceContainer The fileSourceContainer to set.
	 */
	public void setFileSourceContainer(VFSContainer fileSourceContainer) {
		this.fileSourceContainer = fileSourceContainer;
	}

	/**
	 * get source container for this artefact containing files to copy to artefacts folder.
	 * this is not a persisted value from db, can just be used while artefact is living.
	 * the container of an persisted artefact can be found by EPFrontendManager.getArtefactContainer(artefact).
	 * @return Returns the fileSourceContainer.
	 */
	public VFSContainer getFileSourceContainer() {
		return fileSourceContainer;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	@Override
	public String toString() {
		return this.getResourceableTypeName() + " : " + this.getTitle() + " : " + this.getKey();
	}

	@Override
	public String getResourceableTypeName() {
		return null;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 4415237 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AbstractArtefact) {
			AbstractArtefact a = (AbstractArtefact)obj;
			return getKey() != null && getKey().equals(a.getKey());
		}
		return false;
	}
}