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

package org.olat.search.service.document;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroup;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class GroupDocument extends OlatDocument {

	private static final long serialVersionUID = 7177532567808727224L;
	private static final Logger log = Tracing.createLoggerFor(GroupDocument.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.group";
	
	public GroupDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, BusinessGroup businessGroup) {
		GroupDocument groupDocument = new GroupDocument();	

		// Set all know attributes
		groupDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		groupDocument.setLastChange(businessGroup.getLastModified());
		groupDocument.setDocumentType(TYPE);
		groupDocument.setCssIcon(CSSHelper.CSS_CLASS_GROUP);
		groupDocument.setTitle(businessGroup.getName());
		groupDocument.setDescription(FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription()));

		if (log.isDebugEnabled()) log.debug(groupDocument.toString());
		return groupDocument.getLuceneDocument();
	}

}
