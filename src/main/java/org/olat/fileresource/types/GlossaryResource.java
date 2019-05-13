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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;

/**
 * Description:<br>
 * Resource description for a glossary learning resource
 * <P>
 * Initial Date: Dec 04 2006 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class GlossaryResource extends FileResource {
	
	private static final Logger log = Tracing.createLoggerFor(GlossaryResource.class);

	// type identifyer
	public static final String TYPE_NAME = "FileResource.GLOSSARY";
	// file name of glossary xml file
	public static final String GLOSSARY_DEFAULT_FILEREF = "glossary.xml";
	private static final String GLOSSARY_OLD_FILEREF = "glossary.textmarker.xml";
	// file name filter that looks for glossary xml files
	
	public static final FilenameFilter GLOSSARY_FILENAME_FILTER = new FilenameFilter() {
		public boolean accept(File arg0, String arg1) {
			if (arg1.equals(GLOSSARY_DEFAULT_FILEREF)||arg1.equals(GLOSSARY_OLD_FILEREF)) return true;
			else return false;
		}
	};

	public GlossaryResource() {
		super(TYPE_NAME);
	}

	/**
	 * @param dir containing the glossary. the dir must already be unzipped
	 * @return true if of type GlossaryResource, false otherwhise
	 */
	public static boolean validate(File dir) {
		if (dir != null) {
			if (!dir.isDirectory()) {
				// cant handle anything else than a directory.
				// file must be unzipped by another process.
				return false;
			}
			// directory must contain the glossary xml file
			File[] glossaryFiles = dir.listFiles(GLOSSARY_FILENAME_FILTER);
			if (glossaryFiles != null && glossaryFiles.length == 1) { return true; }
		}
		return false;
	}
	
	public static ResourceEvaluation evaluate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			GlossaryFileFilter visitor = new GlossaryFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			if(visitor.hasFile()) {
				XMLScanner scanner = new XMLScanner();
				scanner.scan(visitor.glossaryFile);
				eval.setValid(scanner.hasGlossaryListMarkup());
			} else {
				eval.setValid(false);
			}
			PathUtils.closeSubsequentFS(fPath);
		} catch (IOException | IllegalArgumentException e) {
			log.error("", e);
		}
		return eval;
	}
	
	private static class GlossaryFileFilter extends SimpleFileVisitor<Path> {
		private Path glossaryFile;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(GLOSSARY_DEFAULT_FILEREF.equals(filename) || GLOSSARY_OLD_FILEREF.equals(filename)) {
				glossaryFile = file;
			}
			return glossaryFile != null ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean hasFile() {
			return glossaryFile != null;
		}
	}
}
