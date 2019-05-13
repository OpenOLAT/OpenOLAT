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
package org.olat.search.service.indexer.identity;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.IdentityDocument;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes the users profile
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class ProfileIndexer extends AbstractHierarchicalIndexer {

	private static final Logger log = Tracing.createLoggerFor(ProfileIndexer.class);

	/**
	 * @see org.olat.search.service.indexer.Indexer#getSupportedTypeName()
	 */
	@Override
	public String getSupportedTypeName() {
		return Identity.class.getSimpleName();
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,
			InterruptedException {

		try {
			Identity identity = (Identity) parentObject;
			// no need to change the resource context, the profile is activated in the user homepage anyway
			Document document = IdentityDocument.createDocument(parentResourceContext, identity);
			indexWriter.addDocument(document);
		} catch (Exception ex) {
			log.warn("Exception while indexing profile for identity::" + parentObject.toString() + ". Skipping this user, try next one.", ex);
		}
		if (log.isDebugEnabled()) log.debug("ProfileIndexer finished for user::" + parentObject.toString());
	}
}
