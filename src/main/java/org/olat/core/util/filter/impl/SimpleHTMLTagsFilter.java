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

import java.util.regex.Pattern;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;

/**
 * Description:<br>
 * The html tags filter takes a string and filters all HTML tags. The filter
 * does not remove the code within the tags, only the tag itself. Example:
 * '&lt;font color="red"&gt;hello&lt;/font&gt;world' will become 'hello world'
 * <p>
 * The filter might not be perfect, its a simple version. All tag attributes
 * will be removed as well.
 * <p>
 * Use the SimpleHTMLTagsFilterTest to add new testcases that must work with 
 * this filter.
 * 
 * <P>
 * Initial Date: 15.07.2009 <br>
 * 
 * @author gnaegi
 */
public class SimpleHTMLTagsFilter implements Filter {
	private static final OLog log = Tracing.createLoggerFor(SimpleHTMLTagsFilter.class);
	// match <p> <p/> <br> <br/>
	private static final Pattern brAndPTagsPattern = Pattern.compile("<((br)|p|(BR)|P)( )*(/)?>");
	// match </h1>..
	private static final Pattern titleTagsPattern = Pattern.compile("</[hH][123456]>");
	// match everything <....> 
	private static final Pattern stripHTMLTagsPattern = Pattern.compile("<(!|/)?\\w+((\\s+[\\w-]+(\\s*(=\\s*)?(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
	// match entities
 	private static final Pattern htmlSpacePattern = Pattern.compile("&nbsp;");
	
	/**
	 * @see org.olat.core.util.filter.Filter#filter(java.lang.String)
	 */
	public String filter(String original) {
		try {
			if (original == null) return null;
			//some strange chars let to infinite loop in the regexp and need to be replaced
			String  modified = original.replaceAll("\u00a0", " ");
			modified = brAndPTagsPattern.matcher(modified).replaceAll(" ");
			modified = titleTagsPattern.matcher(modified).replaceAll(" ");
			if (log.isDebug()) log.debug("trying to remove all html tags from: "+modified); 
			modified = stripHTMLTagsPattern.matcher(modified).replaceAll("");	
			modified = htmlSpacePattern.matcher(modified).replaceAll(" ");	
			return modified;			
		} catch (Throwable e) {
			log.error("Could not filter HTML tags. Using unfiltered string! Original string was::" + original, e);
			return original;
		}
	}
}
