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

package org.olat.core.commons.services.search;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.olat.core.util.StringHelper;



/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class OlatDocument extends AbstractOlatDocument {

	private static final long serialVersionUID = 2632864475115088251L;
	private String content = "";
	
	public OlatDocument() {
		super();
	}
	
	public OlatDocument(Document document) {
		super(document);
		content = document.get(CONTENT_FIELD_NAME);
	}





	/**
	 * @return Returns the content.
	 */
	public String getContent() {
		if (content == null) {
			return ""; // Do not return null
		}
		return content;
	}


	/**
	 * @param content The content to set.
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * Generate a lucene document from the data stored in this document
	 * @return
	 */
	public Document getLuceneDocument() {
		Document document = new Document();
		document.add( createField(TITLE_FIELD_NAME,getTitle(), Field.Index.ANALYZED,4) );
		document.add( createField(DESCRIPTION_FIELD_NAME,getDescription(), Field.Index.ANALYZED,2) );
		document.add( createField(CONTENT_FIELD_NAME,getContent(), Field.Index.ANALYZED, 0.5f ) );
		document.add(new Field(RESOURCEURL_FIELD_NAME,getResourceUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED) );//SET to ANALYZED
		document.add(new Field(DOCUMENTTYPE_FIELD_NAME,getDocumentType(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
		if(getCssIcon() != null)
			document.add(new Field(CSS_ICON,getCssIcon(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
		document.add(new Field(FILETYPE_FIELD_NAME,getFileType(), Field.Store.YES, Field.Index.NOT_ANALYZED) );
		document.add( createField(AUTHOR_FIELD_NAME,getAuthor(), Field.Index.ANALYZED, 2) );
	    try {
	    	if(getCreatedDate() != null) {
	    		document.add(new Field(CREATED_FIELD_NAME,DateTools.dateToString(getCreatedDate(), DateTools.Resolution.DAY), Field.Store.YES, Field.Index.ANALYZED) );
	    	}
	    }catch (Exception ex) {
	    	// No createdDate set => does not add field
	    }
	    try {
	    	if(getLastChange() != null) {
			  document.add(new Field(CHANGED_FIELD_NAME,DateTools.dateToString(getLastChange(), DateTools.Resolution.DAY), Field.Store.YES, Field.Index.ANALYZED) );
	    	}
	    }catch (Exception ex) {
	    	// No changedDate set => does not add field
	    }
	    try {
	    	if(getTimestamp() != null) {
			  document.add(new Field(TIME_STAMP_NAME,DateTools.dateToString(getTimestamp(), DateTools.Resolution.MILLISECOND), Field.Store.YES, Field.Index.NO) );
	    	}
	    }catch (Exception ex) {
	    	// No changedDate set => does not add field
	    }
	    // Add various metadata
	    if (metadata != null) {
	    	for (Entry<String, List<String>> metaDataEntry : metadata.entrySet()) {
	    		String key = metaDataEntry.getKey();
					List<String> values = metaDataEntry.getValue();
					for (String value : values) {
						//FIXME:FG: tokenized or not? which priority
						document.add( createField(key, value, Field.Index.ANALYZED, 2) );
					}
				}
	    }
	    document.add(new Field(PARENT_CONTEXT_TYPE_FIELD_NAME,getParentContextType(), Field.Store.YES, Field.Index.ANALYZED) );
	    document.add(new Field(PARENT_CONTEXT_NAME_FIELD_NAME,getParentContextName(), Field.Store.YES, Field.Index.ANALYZED) );
	    if(StringHelper.containsNonWhitespace(getReservedTo())) {
	    	for(StringTokenizer tokenizer = new StringTokenizer(getReservedTo(), " "); tokenizer.hasMoreTokens(); ) {
	    		String reserved = tokenizer.nextToken();
	    		document.add(new Field(RESERVED_TO, reserved, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    	}
		} else {
			document.add(new Field(RESERVED_TO, "public", Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	    return document;
	}


	private Field createField(String fieldName, String content, Field.Index fieldIndex, float wight) {
		Field field = new Field(fieldName,content, Field.Store.YES, fieldIndex);
		field.setBoost(wight);
		return field;
	}
}
