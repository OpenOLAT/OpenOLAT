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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;

/**
 * Description:<br>
 * The conditional HTML comments filter takes a string and filters all IE specific conditional comments away. Example:
 * '&lt;!--[if gte mso 9]&gt; ... some IE stuff ... &lt;!--[endif]--&gt; will be removed
 * 
 * <P>
 * Initial Date: 15.07.2009 <br>
 * 
 * @author gnaegi
 */
public class ConditionalHTMLCommentsFilter implements Filter {
	private static final Logger log = Tracing.createLoggerFor(ConditionalHTMLCommentsFilter.class);
	private static final Pattern conditionalCommentPattern = Pattern.compile("<!--\\[if.*?\\[endif\\]-->");
	
	/**
	 * @see org.olat.core.util.filter.Filter#filter(java.lang.String)
	 */
	public String filter(String original) {
		try {
			if (original == null) return null;
			//some strange chars let to infinite loop in the regexp and need to be replaced
			String  modified = original.replaceAll("\u00a0", " ");
			modified = conditionalCommentPattern.matcher(original).replaceAll("");
			return modified;			
		} catch (Throwable e) {
			log.error("Could not filter conditional comments. Using unfiltered string! Original string was::" + original, e);
			return original;
		}
	}
}
