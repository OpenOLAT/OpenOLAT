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
package org.olat.fileresource.types;

import java.io.File;

/**
 * A podcast learning resource
 * 
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class PodcastFileResource extends FeedFileResource {
	public static final String TYPE_NAME = "FileResource.PODCAST";

	/**
	 * Default Constructor
	 */
	public PodcastFileResource() {
		super(TYPE_NAME);
	}

	/**
	 * Constructor used for import (FileResourceManager)
	 * 
	 * @param root The root folder of the resource
	 * @param resource The uploaded folder containing the resource
	 */
	public PodcastFileResource(File root, File resourceFolder) {
		super(root, resourceFolder, TYPE_NAME);
	}

	/**
	 * Validates the uploaded resource directory
	 * 
	 * @param directory
	 * @return True if it is falid
	 */
	public static boolean validate(File directory) {
		return FeedFileResource.validate(directory, TYPE_NAME);
	}
	
	public static ResourceEvaluation evaluate(File file, String filename) {
		return evaluate(file, filename, TYPE_NAME);
	}
}
