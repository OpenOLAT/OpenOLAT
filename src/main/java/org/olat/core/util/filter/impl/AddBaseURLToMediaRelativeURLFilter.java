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
package org.olat.core.util.filter.impl;

import org.olat.core.util.filter.Filter;

/**
 * Description:<br>
 * This filter searches in the given HTML string for relative media urls that
 * point to media files and adds the given mapper base path to this URL to
 * deliver the media files with absolute URLs.
 * <p>
 * Example: <br />
 * &lt;img src="media/myimage.jpg" /&gt;
 * <p>
 * This will be converted to: <br />
 * &lt;img src="http://your.olat.com/olat/m/12345/media/myimage.jpg" /&gt;
 * <p>
 * The resulting string can be embedded into any HTML page and the media files
 * will be loaded from the mapper base path.
 * 
 * <P>
 * Initial Date: 16.07.2009 <br>
 * 
 * @author gnaegi
 */
public class AddBaseURLToMediaRelativeURLFilter implements Filter {
	private static final String MEDIA_REL_PATH_DETECTOR = "\"media/";
	private static final String REPLACEMENT_POSTFIX = "/media/";
	private static final String REPLACEMENT_PREFIX = "\"";
	//
	private final String mapperBaseURL;

	/**
	 * Constructor
	 * 
	 * @param mapperBaseURL The mapper base url that is used in this filter as a
	 *          prefix for relative media URL
	 */
	public AddBaseURLToMediaRelativeURLFilter(String mapperBaseURL) {
		if (mapperBaseURL.endsWith("/")) {
			// remove trailing slash, will be added later
			this.mapperBaseURL = mapperBaseURL.substring(0, mapperBaseURL.length() - 1);
		} else {
			this.mapperBaseURL = mapperBaseURL;
		}
	}

	/**
	 * @see org.olat.core.util.filter.Filter#filter(java.lang.String)
	 */
	public String filter(String original) {
		if (original == null) return null;
		String withPath = original.replace(MEDIA_REL_PATH_DETECTOR, REPLACEMENT_PREFIX + mapperBaseURL + REPLACEMENT_POSTFIX);
		return withPath;
	}

}
