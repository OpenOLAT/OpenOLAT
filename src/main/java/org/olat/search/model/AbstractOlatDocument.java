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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.model;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.olat.core.util.StringHelper;


/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public abstract class AbstractOlatDocument implements Serializable {

	private static final long serialVersionUID = 3477625468662703214L;

	// Field names
	public static final String DB_ID_NAME = "key";
	
	public static final String TITLE_FIELD_NAME = "title";

	public static final String DESCRIPTION_FIELD_NAME = "description";

	public static final String CONTENT_FIELD_NAME = "content";

	public static final String DOCUMENTTYPE_FIELD_NAME = "documenttype";

	public static final String FILETYPE_FIELD_NAME = "filetype";

	public static final String RESOURCEURL_FIELD_NAME = "resourceurl";
	
	public static final String RESOURCEURL_MD5_FIELD_NAME = "resourceurlmd";

	public static final String AUTHOR_FIELD_NAME = "author";
	
	public static final String LOCATION_FIELD_NAME = "location";

	public static final String CREATED_FIELD_NAME = "created";

	public static final String CHANGED_FIELD_NAME = "changed";
	
	public static final String PUBLICATION_DATE_FIELD_NAME = "pubdate";

	public static final String TIME_STAMP_NAME = "timestamp";

	public static final String PARENT_CONTEXT_TYPE_FIELD_NAME = "parentcontexttype";

	public static final String PARENT_CONTEXT_NAME_FIELD_NAME = "parentcontextname";
	
	public static final String CSS_ICON = "cssicon";
	
	public static final String RESERVED_TO = "reservedto";

	public static final String LICENSE_TYPE_FIELD_NAME = "licensetype";
	
	public static final Set<String> getFields() {
		Set<String> fields = new HashSet<>();
		fields.add(DB_ID_NAME);
		fields.add(TITLE_FIELD_NAME);
		fields.add(DESCRIPTION_FIELD_NAME);
		fields.add(CONTENT_FIELD_NAME);
		fields.add(DOCUMENTTYPE_FIELD_NAME);
		fields.add(FILETYPE_FIELD_NAME);
		fields.add(RESOURCEURL_FIELD_NAME);
		fields.add(AUTHOR_FIELD_NAME);
		fields.add(LOCATION_FIELD_NAME);
		fields.add(CREATED_FIELD_NAME);
		fields.add(CHANGED_FIELD_NAME);
		fields.add(PUBLICATION_DATE_FIELD_NAME);
		fields.add(TIME_STAMP_NAME);
		fields.add(PARENT_CONTEXT_TYPE_FIELD_NAME);
		fields.add(PARENT_CONTEXT_NAME_FIELD_NAME);
		fields.add(CSS_ICON);
		fields.add(RESERVED_TO);
		fields.add(LICENSE_TYPE_FIELD_NAME);
		return fields;
	}

	
	// Lucene Attributes
	private Long id;
	private String title = "";
	protected String description = "";
	/** E.g. 'Group','ForumMessage'. */
	private String documentType = "";
	private String fileType = "";
	/** JumpInUrl to E.g. 'Group:123456:Forum:342556:Message:223344'. */ 
	private String resourceUrl = "";
	private String author = "";
	private String location = "";
	private Date createdDate;
	private Date lastChange;
	private Date publicationDate;
	private Date timestamp;
	/** Various metadata, most likely dublin core **/
	protected Map<String, List<String>> metadata;
	/* e.g. Course */
	private String parentContextType = "";
	/* e.g. Course-name */
	private String parentContextName = "";
	private String cssIcon;
	private String reservedTo;
	private String licenseTypeKey = "";
	
	public AbstractOlatDocument() {
		timestamp = new Date();
	}

	public AbstractOlatDocument(Document document) {
		String idStr = document.get(DB_ID_NAME);
		if(StringHelper.containsNonWhitespace(idStr)) {
			id = Long.parseLong(idStr);
		}
		title = document.get(TITLE_FIELD_NAME);
		description = document.get(DESCRIPTION_FIELD_NAME);
		documentType = document.get(DOCUMENTTYPE_FIELD_NAME);
		fileType = document.get(FILETYPE_FIELD_NAME);
		resourceUrl = document.get(RESOURCEURL_FIELD_NAME);
		author = document.get(AUTHOR_FIELD_NAME);
		location = document.get(LOCATION_FIELD_NAME);
		reservedTo = document.get(RESERVED_TO);
		createdDate = toDate(document, CREATED_FIELD_NAME);
		lastChange = toDate(document, CHANGED_FIELD_NAME);
		publicationDate = toDate(document, PUBLICATION_DATE_FIELD_NAME);
		timestamp = toDate(document, TIME_STAMP_NAME);
		parentContextType = document.get(PARENT_CONTEXT_TYPE_FIELD_NAME);
		parentContextName = document.get(PARENT_CONTEXT_NAME_FIELD_NAME);
		cssIcon = document.get(CSS_ICON);
		licenseTypeKey = document.get(LICENSE_TYPE_FIELD_NAME);
	}
	
	private Date toDate(Document document, String fieldName) {
		try {
			String f = document.get(fieldName);
			if(StringHelper.containsNonWhitespace(f)) {
				return DateTools.stringToDate(f);
			}
		} catch (ParseException e) {
			//can happen
		}
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		if (author == null) {
			return ""; // Do not return null
		}
		return author;
	}

	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getLocation() {
		if (location == null) {
			return ""; // Do not return null
		}
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		if (description == null) {
			return ""; // Do not return null
		}
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the documentType.
	 */
	public String getDocumentType() {
		if (documentType == null) {
			return ""; // Do not return null
		}
		return documentType;
	}

	/**
	 * @param documentType The documentType to set.
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * @return Returns the fileType.
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType The fileType to set.
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return Returns the lastChange.
	 */
	public Date getLastChange() {
		return lastChange;
	}

	/**
	 * @param lastChange The lastChange to set.
	 */
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	/**
	 * @return Returns the resourceUrl.
	 */
	public String getResourceUrl() {
		if (resourceUrl == null) {
			return ""; // Do not return null
		}
		return resourceUrl;
	}

	/**
	 * @param resourceUrl The resourceUrl to set.
	 */
	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		if (title == null) {
			return ""; // Do not return null
		}
		return title;
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * The list of identities who can see the document. It's an optimized
	 * check access for private documents.
	 * @return Return a list of identity keys separated by spaces
	 */
	public String getReservedTo() {
		return reservedTo;
	}

	public void setReservedTo(String reservedTo) {
		this.reservedTo = reservedTo;
	}

	public String getLicenseTypeKey() {
		if (licenseTypeKey == null) {
			return ""; // Do not return null
		}
		return licenseTypeKey;
	}

	public void setLicenseTypeKey(String licenseTypeKey) {
		this.licenseTypeKey = licenseTypeKey;
	}

	/**
	 * Add generic metadata. It is strongly recommended not to use anything else
	 * than the doublin core metadata namespace here. See {@link http
	 * ://en.wikipedia.org/wiki/Dublin_Core} for more information.
	 * <p>
	 * A metadata element consists of a key-value pair. It is possible to have
	 * more than one value for a key. In this case use the method multiple times
	 * with the same key.
	 * <p>
	 * Example:<br>
	 * DC.subject		OLAT - the best Open Source LMS<br>
	 * DC.creator		Florian GnÔøΩgi
	 * 
	 * @param key The metadata key
	 * @param value The metadata value
	 */
	public synchronized void addMetadata(String key, String value) {
		if (key == null || ! StringHelper.containsNonWhitespace(value)) return;
		// initialize metadata map if never done before
		if (metadata == null) metadata = new HashMap<>();
		// get list of already added values for this key
		List<String> values = metadata.get(key);
		if (values == null) {
			// this meta key has never been added so far
			values = new ArrayList<>(1);
			metadata.put(key, values);
		}
		values.add(value);
	}

	/**
	 * Get the list of metadata values for the given key. This might return NULL
	 * if no such metadata is linked to this document.
	 * 
	 * @param key The metadata key, e.g. DC.subject
	 * @return The list of values or NULL if not found
	 */
	public List<String> getMetadataValues(String key) {
		List<String> values = null;
		if (metadata != null) {
				values = metadata.get(key);
		}
		return values;
	}

	public String getParentContextType() {
		if (parentContextType == null) {
			return ""; // Do not return null
		}
		return parentContextType;
	}

	public void setParentContextType(String parentContextType) {
		this.parentContextType = parentContextType;
	}

	public String getParentContextName() {
		if (parentContextName == null) {
			return ""; // Do not return null
		}
		return parentContextName;
	}

	public void setParentContextName(String parentContextName) {
		this.parentContextName = parentContextName;
	}

	public String getCssIcon() {
		return cssIcon;
	}

	public void setCssIcon(String cssIcon) {
		this.cssIcon = cssIcon;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return Returns the createdDate.
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate The createdDate to set.
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getDocumentType())
		   .append("|")
		   .append(getTitle())
		   .append("|");
		if (getDescription() != null) buf.append(getDescription());
		buf.append("|").append(getResourceUrl());
		return buf.toString();
	}
}
