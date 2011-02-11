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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.modules.glossary;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index a repository entry of type glossary.
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class GlossaryRepositoryIndexer implements Indexer {

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttypes and
	// lucene has problems with '_'
	public static String TYPE = "type.repository.entry.glossary";

	public static String ORES_TYPE_GLOSSARY = GlossaryResource.TYPE_NAME;

	public GlossaryRepositoryIndexer() {
	// Repository types

	}

	/**
	 * 
	 */
	public String getSupportedTypeName() {
		return ORES_TYPE_GLOSSARY;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,
			InterruptedException {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		Tracing.logDebug("Analyse Glosary RepositoryEntry...", GlossaryRepositoryIndexer.class);
		try {
			resourceContext.setDocumentType(TYPE);
			Document document = GlossaryManager.getInstance().getIndexerDocument(repositoryEntry, resourceContext);
			if (document != null) {
				indexWriter.addDocument(document);
			}
		} catch (NullPointerException nex) {
			Tracing.logWarn("NullPointerException in GlossaryRepositoryIndexer.doIndex.", nex, GlossaryRepositoryIndexer.class);
		}
	}

	/**
	 * Bean setter method used by spring.
	 * 
	 * @param indexerList
	 */
	public void setIndexerList(List indexerList) {}

	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}

}
