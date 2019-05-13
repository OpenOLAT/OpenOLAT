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

package org.olat.fileresource.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;

/**
 * Description:<br>
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WikiResource extends FileResource {
	private static final Logger log = Tracing.createLoggerFor(WikiResource.class);

	public static final String TYPE_NAME = "FileResource.WIKI";
	public static final String INDEX_FILENAME = WikiManager.generatePageId(WikiPage.WIKI_INDEX_PAGE) + "." + WikiManager.WIKI_FILE_SUFFIX;
	public static final String INDEX_PROPNAME = WikiManager.generatePageId(WikiPage.WIKI_INDEX_PAGE) + "." + WikiManager.WIKI_PROPERTIES_SUFFIX;	
	public static final String INDEX_ALT_FILENAME = INDEX_FILENAME.replace("=", "_");
	public static final String INDEX_ALT_PROPNAME = INDEX_PROPNAME.replace("=", "_");
	
	public WikiResource() {
		super(TYPE_NAME);
	}

	public static ResourceEvaluation validate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			IndexFileFilter visitor = new IndexFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			eval.setValid(visitor.isValid());
			PathUtils.closeSubsequentFS(fPath);
		} catch (IOException | IllegalArgumentException e) {
			log.error("", e);
		}
		return eval;
	}
	
	private static class IndexFileFilter extends SimpleFileVisitor<Path> {
		private boolean indexFile;
		private boolean indexProperties;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(INDEX_FILENAME.equals(filename) || INDEX_ALT_FILENAME.equals(filename)) {
				indexFile = true;
			} else if(INDEX_PROPNAME.equals(filename) || INDEX_ALT_PROPNAME.equals(filename)) {
				indexProperties = true;
			}
			return (indexProperties && indexFile) ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean isValid() {
			return indexFile && indexProperties;
		}
	}
}
