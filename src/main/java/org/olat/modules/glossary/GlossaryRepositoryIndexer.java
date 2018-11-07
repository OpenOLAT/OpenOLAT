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

package org.olat.modules.glossary;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index a repository entry of type glossary.
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class GlossaryRepositoryIndexer extends DefaultIndexer {

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttypes and
	// lucene has problems with '_'
	public static final String TYPE = "type.repository.entry.glossary";

	public static final String ORES_TYPE_GLOSSARY = GlossaryResource.TYPE_NAME;


	@Override
	public String getSupportedTypeName() {
		return ORES_TYPE_GLOSSARY;
	}

	@Override
	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,
			InterruptedException {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		if(isLogDebugEnabled()) logDebug("Analyse Glosary RepositoryEntry...");
		try {
			resourceContext.setDocumentType(TYPE);
			resourceContext.setTitle(repositoryEntry.getDisplayname());
			resourceContext.setDescription(repositoryEntry.getDescription());
			Document document = CoreSpringFactory.getImpl(GlossaryManager.class).getIndexerDocument(repositoryEntry, resourceContext);
			if (document != null) {
				indexWriter.addDocument(document);
			}
		} catch (NullPointerException nex) {
			logWarn("NullPointerException in GlossaryRepositoryIndexer.doIndex.", nex);
		}
	}
}
