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
package org.olat.repository.manager;

import org.apache.lucene.document.Document;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("repositoryEntryDocumentFactory")
public class RepositoryEntryDocumentFactory {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	public String getResourceUrl(Long itemKey) {
		return "[RepositoryEntry:" + itemKey + "]";
	}

	public Document createDocument(SearchResourceContext searchResourceContext, Long repoEntryKey) {
		RepositoryEntry re = repositoryService.loadByKey(repoEntryKey);
		if(re != null) {
			return createDocument(searchResourceContext, re);
		}
		return null;
	}

	public Document createDocument(SearchResourceContext searchResourceContext, RepositoryEntry re) {
		OlatDocument oDocument = new OlatDocument();
		oDocument.setId(re.getKey());
		oDocument.setCreatedDate(re.getCreationDate());
		oDocument.setLastChange(re.getLastModified());
		oDocument.setTitle(re.getDisplayname());
		
		StringBuilder sb = new StringBuilder();
		String desc = re.getDescription();
		if(desc != null) {
			sb.append(desc).append(" ");
		}
		String objectives = re.getObjectives();
		if(objectives != null) {
			sb.append(objectives).append(" ");
		}
		String requirements = re.getRequirements();
		if(requirements != null) {
			sb.append(requirements);
		}
		oDocument.setDescription(sb.toString());
		oDocument.setResourceUrl(getResourceUrl(re.getKey()));
		
		String docType = RepositoryEntryDocument.TYPE + re.getOlatResource().getResourceableTypeName();
		oDocument.setDocumentType(docType);
		oDocument.setCssIcon(getIconCss(docType));
		oDocument.setParentContextType(searchResourceContext.getParentContextType());
		oDocument.setParentContextName(searchResourceContext.getParentContextName());
		oDocument.setAuthor(re.getAuthors());
		oDocument.setLocation(re.getLocation());
		
		if (licenseModule.isEnabled(licenseHandler)) {
			ResourceLicense license = licenseService.loadLicense(re.getOlatResource());
			if (license != null && license.getLicenseType() != null) {
				oDocument.setLicenseTypeKey(String.valueOf(license.getLicenseType().getKey()));
			}
		}
		
		//add specific fields
		Document document = oDocument.getLuceneDocument();
		return document;
	}
	
	public String getIconCss(String docType) {
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
			icon = "o_filetype_xls";
		else if (docType.equals("type.repository.entry.FileResource.DOC"))
			icon = "o_filetype_doc";
		else if (docType.equals("type.repository.entry.FileResource.FILE"))
			icon = "o_filetype_file";
		else if (docType.equals("type.repository.entry.FileResource.PDF"))
			icon = "o_filetype_pdf";
		else if (docType.equals("type.repository.entry.FileResource.PPT"))
			icon = "o_filetype_ppt";
		else if (docType.equals("type.repository.entry.FileResource.PODCAST"))
			icon = "o_podcast_icon";
		else if (docType.equals("type.repository.entry.FileResource.BLOG"))
			icon = "o_blog_icon";
		else if (docType.equals("type.repository.entry.FileResource.TEST"))
			icon = "o_iqtest_icon";
		else if (docType.equals("type.repository.entry.FileResource.SURVEY"))
			icon = "o_iqsurv_icon";
		else if(docType.equals("type.repository.entry.FileResource.IMSQTI21"))
			icon = "o_qtiassessment_icon";
		else if (docType.equals("type.repository.entry.ep"))
			icon = "o_ep_icon";
		else {
			icon = "o_sp_icon";
		}
		return icon;
	}
}
