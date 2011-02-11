/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.search.service;

import java.util.Date;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.fo.Message;

/**
 * Search internal class to build resourceUrl in indexer.
 * @author Christian Guretzki
 */
public class SearchResourceContext {

	
	/** Workaround for forum message. Forum-Message is currently no OLATResourcable. */
	public static final String MESSAGE_RESOURCE_TYPE = "Message";
	
	private static final String FILEPATH_PREFIX = "[path=";
	private static final String ENDTAG = "]";


	// Parameter to pass from parent to child
	private Date lastModified;
	private Date createdDate;
	private String documentType = null;
	private String title = null;
	private String description = null;
	private String parentContextType = null;
	private String parentContextName = null;
	private String documentContext = null;

	private BusinessControl myBusinessControl;
	private BusinessControl parentBusinessControl;
	
  private String filePath = null;


	/**
	 * Constructor for root-object without any parent.
	 */
	public SearchResourceContext( ) {
		parentBusinessControl = null;
	}

	/**
	 * Constructor for child-object with a parent.
	 */
	public SearchResourceContext(SearchResourceContext parentResourceContext) {
		lastModified = parentResourceContext.getLastModified();
		createdDate  = parentResourceContext.getCreatedDate();
		documentType = parentResourceContext.getDocumentType();
		parentBusinessControl = parentResourceContext.getBusinessControl();
		filePath = parentResourceContext.getFilePath();
		parentContextType = parentResourceContext.parentContextType;
		parentContextName = parentResourceContext.getParentContextName();
	}

	
	public String getFilePath() {
		return filePath;
	}

	protected BusinessControl getBusinessControl() {
		return myBusinessControl;
	}

	/**
	 * @return Returns the resourcePath.
	 */
	public String getResourceUrl() {
		String resourceUrl = BusinessControlFactory.getInstance().getAsString(myBusinessControl);
		if (filePath != null) {
			// It is a file resource => Append file path
	    StringBuilder buf = new StringBuilder(resourceUrl);
			buf.append(FILEPATH_PREFIX);
			buf.append(filePath);
			buf.append(ENDTAG);
			resourceUrl = buf.toString();
		}
		return resourceUrl;
	}

	/**
	 * 
	 * @param olatResource
	 */
	public void setBusinessControlFor(OLATResourceable olatResource) {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(olatResource);
		myBusinessControl = BusinessControlFactory.getInstance().createBusinessControl(ce,parentBusinessControl);
	}

	
	/**
	 * Workaround for forum message. Forum-Message is currently no OLATResourcable.<br>
	 * ResourceUrl-Format for Forum-Message :<br>
	 * forum:<FORUM-ID>:message:<MESSAGE-ID> 
	 * @param message
	 */
	public void setBusinessControlFor(Message message) {
		setBusinessControlFor(OresHelper.createOLATResourceableInstance(Message.class,message.getKey()));
	}

	/**
	 * Set BusinessControl for certain CourseNode.
	 * @param courseNode
	 */
	public void setBusinessControlFor(CourseNode courseNode) {
		if (Tracing.isDebugEnabled(SearchResourceContext.class)) Tracing.logDebug("Course-node-ID=" + courseNode.getIdent(), SearchResourceContext.class);
		setBusinessControlFor(OresHelper.createOLATResourceableInstance(CourseNode.class,new Long(courseNode.getIdent())));
  }

	/**
	 * Pass lastModified parameter from parent to child.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Pass createdDate parameter from parent to child.
	 */
	public void setCreatedDate(Date creationDate) {
		this.createdDate = creationDate;
	}

	/**
	 * Pass lastModified parameter from parent to child.
	 * @return Returns the creationDate.
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Pass lastModified parameter from parent to child.
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Pass filePath parameter from parent to child.
	 */
	public void setFilePath(String myFilePath) {
		this.filePath = myFilePath;
	}

	/**
	 * Pass documentType parameter from parent to child.
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * Pass documentType parameter from parent to child.
	 * @return
	 */
	public String getDocumentType() {
		return documentType;
	}

	public String getDocumentContext() {
		return documentContext;
	}

	public void setDocumentContext(String documentContext) {
		this.documentContext = documentContext;
	}

	/**
	 * Pass title parameter from parent to child.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Pass description parameter from parent to child.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Pass title parameter from parent to child.
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Pass description parameter from parent to child.
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}

	public void setParentContextType(String parentContextType) {
		this.parentContextType = parentContextType;
	}

	public void setParentContextName(String parentContextName) {
		this.parentContextName = parentContextName;
	}

	public String getParentContextType() {
		return parentContextType;
	}

	public String getParentContextName() {
		return parentContextName;
	}
	
}
