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
package org.olat.search.service.indexer.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.dom4j.Element;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.ims.resources.IMSLoader;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.IMSMetadataDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Description:<br>
 * Index the SCORM package
 * 
 * <P>
 * Initial Date:  11 déc. 2009 <br>
 * @author srosse
 */
public class ScormRepositoryIndexer extends FolderIndexer {

	private static final Logger log = Tracing.createLoggerFor(ScormRepositoryIndexer.class);
	
	public static Set<String> stopWords = new HashSet<>();
	static {
		stopWords.add("LOMv1.0");
		stopWords.add("yes");
		stopWords.add("NA");
	}
	public static final List<String> forbiddenExtensions = new ArrayList<>();
	static {
		forbiddenExtensions.add("LOMv1.0");
		forbiddenExtensions.add(".xsd");
		forbiddenExtensions.add(".js");
	}
	public static final Set<String> forbiddenFiles = new HashSet<>();
	static {
		forbiddenFiles.add("imsmanifest.xml");
	}
	
	
	public static final String TYPE = "type.repository.entry.scorm";
	public static final String ORES_TYPE_SCORM = ScormCPFileResource.TYPE_NAME;
	
	public ScormRepositoryIndexer() {
		// Repository types
	}
	
	/**
	 * 
	 */
	public String getSupportedTypeName() {	
		return ORES_TYPE_SCORM; 
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter)
	throws IOException,InterruptedException  {
		if (log.isDebugEnabled()) log.debug("Index Scorm package...");
		
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		OLATResource ores = repositoryEntry.getOlatResource();
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);
		resourceContext.setDocumentType(TYPE);
		SearchResourceContext scormContext = new SearchResourceContext(resourceContext);
		doIndex(scormContext, indexWriter, cpRoot);
	}
		
	protected void doIndex(SearchResourceContext resourceContext, OlatFullIndexer indexWriter, File cpRoot)
	throws IOException,InterruptedException  {
		VFSContainer container = new LocalFolderImpl(cpRoot);
		VFSLeaf fManifest = (VFSLeaf)container.resolve("imsmanifest.xml");
		if(fManifest != null) {
			Element rootElement =  IMSLoader.loadIMSDocument(fManifest).getRootElement();
			Document manfiestDoc = createManifestDocument(fManifest, rootElement, resourceContext);
			indexWriter.addDocument(manfiestDoc);
			
			ScormFileAccess accessRule = new ScormFileAccess();
			doIndexVFSContainer(resourceContext, container, indexWriter, "", accessRule);
		}
	}
	
	private Document createManifestDocument(VFSLeaf fManifest, Element rootElement, SearchResourceContext resourceContext) {
		IMSMetadataDocument document = new IMSMetadataDocument();
		document.setResourceUrl(resourceContext.getResourceUrl());
		if (log.isDebugEnabled()) log.debug("MM: URL=" + document.getResourceUrl());
		document.setLastChange(new Date(fManifest.getLastModified()));
		document.setDocumentType(resourceContext.getDocumentType());
		if (StringHelper.containsNonWhitespace(resourceContext.getTitle())) {
			document.setTitle(resourceContext.getTitle());
		} else {
			document.setTitle(fManifest.getName());
		}
		document.setParentContextType(resourceContext.getParentContextType());
		document.setParentContextName(resourceContext.getParentContextName());
	
		
		
		StringBuilder sb = new StringBuilder();
		collectLangString(sb, rootElement);
		document.setContent(sb.toString());
		return document.getLuceneDocument();
	}
	
	private void collectLangString(StringBuilder sb, Element element) {
		if("langstring".equals(element.getName())) {
			String content = element.getText();
			if(!stopWords.contains(content)) {
				sb.append(content).append(' ');
			}	
		}
		@SuppressWarnings("rawtypes")
		List children = element.elements();
		for(int i=0; i<children.size(); i++) {
			Element child = (Element)children.get(i);
			collectLangString(sb, child);
		}
	}
	
	public class ScormFileAccess implements FolderIndexerAccess {
		
		@Override
		public boolean allowed(VFSItem item) {
			String name = item.getName();
			if(forbiddenFiles.contains(name)) {
				return false;
			}
			
			for(String forbiddenExtension:forbiddenExtensions) {
				if(name.endsWith(forbiddenExtension)) {
					return false;
				}
			}
			return true;
		}
	}
}
