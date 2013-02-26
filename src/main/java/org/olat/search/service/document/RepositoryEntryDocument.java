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

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class RepositoryEntryDocument extends OlatDocument {

	private static final long serialVersionUID = -1738740951095875294L;
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryDocument.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.repository.entry.";
	
	public RepositoryEntryDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, RepositoryEntry repositoryEntry) {
		RepositoryEntryDocument repositoryEntryDocument = new RepositoryEntryDocument();	

		// Set all know attributes
		repositoryEntryDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		repositoryEntryDocument.setLastChange(repositoryEntry.getLastModified());
		repositoryEntryDocument.setCreatedDate(repositoryEntry.getCreationDate());
		repositoryEntryDocument.setDocumentType(TYPE + repositoryEntry.getOlatResource().getResourceableTypeName());
		repositoryEntryDocument.setCssIcon(getIconCss(repositoryEntryDocument.getDocumentType()));
		repositoryEntryDocument.setTitle(repositoryEntry.getDisplayname());
		String desc = repositoryEntry.getDescription();
		// strip all html tags
		Filter htmlTagsFilter = FilterFactory.getHtmlTagsFilter();
		desc = htmlTagsFilter.filter(desc);
		repositoryEntryDocument.setDescription(desc);
		repositoryEntryDocument.setAuthor(repositoryEntry.getInitialAuthor());

		if (log.isDebug()) log.debug(repositoryEntryDocument.toString());
		return repositoryEntryDocument.getLuceneDocument();
	}
	
	private static String getIconCss(String docType) {
		String icon;
		if (docType.equals("type.repository.entry.CourseModule"))
			icon = "o_course_icon";
		else if (docType.equals("type.repository.entry.wiki") || docType.equals("type.repository.entry.FileResource.WIKI"))
			icon = "o_wiki_icon";
		else if (docType.equals("type.repository.entry.imscp") || docType.equals("type.repository.entry.FileResource.IMSCP"))
			icon = "o_cp_icon";
		else if (docType.equals("type.repository.entry.sharedfolder") || docType.equals("type.repository.entry.FileResource.SHAREDFOLDER"))
			icon = "o_FileResource-SHAREDFOLDER_icon";
		else if (docType.equals("type.repository.entry.glossary") || docType.equals("type.repository.entry.FileResource.GLOSSARY"))
			icon = "o_FileResource-GLOSSARY_icon";
		else if (docType.equals("type.repository.entry.FileResource.SURVEY"))
			icon = "o_iqsurv_icon";
		else if (docType.equals("type.repository.entry.FileResource.SCORMCP"))
			icon = "o_FileResource-SCORMCP_icon";
		else if (docType.equals("type.repository.entry.FileResource.XLS"))
			icon = "b_filetype_xls";
		else if (docType.equals("type.repository.entry.FileResource.DOC"))
			icon = "b_filetype_doc";
		else if (docType.equals("type.repository.entry.FileResource.FILE"))
			icon = "b_filetype_file";
		else if (docType.equals("type.repository.entry.FileResource.PDF"))
			icon = "b_filetype_pdf";
		else if (docType.equals("type.repository.entry.FileResource.PPT"))
			icon = "b_filetype_ppt";
		else if (docType.equals("type.repository.entry.FileResource.PODCAST"))
			icon = "o_podcast_icon";
		else if (docType.equals("type.repository.entry.FileResource.BLOG"))
			icon = "o_blog_icon";
		else if (docType.equals("type.repository.entry.FileResource.TEST"))
			icon = "o_iqtest_icon";
		else if (docType.equals("type.repository.entry.FileResource.SURVEY"))
			icon = "o_iqsurv_icon";
		else if (docType.equals("type.repository.entry.EPStructuredMapTemplate") || docType.equals("type.repository.entry.ep"))
			icon = "o_ep_icon";
		else {
			icon = "o_sp_icon";
		}
		return icon;
	}

}
