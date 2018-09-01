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

package org.olat.core.util.filter;

import org.olat.core.util.filter.impl.AddBaseURLToMediaRelativeURLFilter;
import org.olat.core.util.filter.impl.ConditionalHTMLCommentsFilter;
import org.olat.core.util.filter.impl.NekoHTMLFilter;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.filter.impl.SimpleHTMLTagsFilter;
import org.olat.core.util.filter.impl.SmileysCssToDataUriFilter;
import org.olat.core.util.filter.impl.XMLValidCharacterFilter;
import org.olat.core.util.filter.impl.XMLValidEntityFilter;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter.Variant;

/**
 * Description:<br>
 * Use this factory to create filter instances. The factory makes sure that you
 * get a statefull filter that can be used only once or one that does not have
 * any inner state depending on the implementation.
 * 
 * <P>
 * Initial Date: 16.07.2009 <br>
 * 
 * @author gnaegi
 */
public class FilterFactory {
	// the html tag filter is static, not stateful
	private static final Filter htmlTagsFilter = new SimpleHTMLTagsFilter();
	private static final Filter htmlTagsAndDesescapingFilter = new NekoHTMLFilter();
	private static final Filter conditionalCommentsFilter = new ConditionalHTMLCommentsFilter();
	private static final Filter xmlValidCharacterFilter = new XMLValidCharacterFilter();
	private static final Filter smileysCssToDataUriFilter = new SmileysCssToDataUriFilter();
	private static final Filter xmlValidEntityFilter = new XMLValidEntityFilter();

	/**
	 * Get an instance of the HTML tag filter
	 * 
	 * @return
	 */
	public static Filter getHtmlTagsFilter() {
		return htmlTagsFilter;
	}
	
	public static Filter getHtmlTagAndDescapingFilter() {
		return htmlTagsAndDesescapingFilter;
	}
	

	/**
	 * Get an instance of the conditional IE comments filter
	 * @return
	 */
	public static Filter getConditionalHtmlCommentsFilter() {
		return conditionalCommentsFilter;
	}
	
	/**
	 * The filter remove characters which are not valid in a
	 * XML 1.0 document.
	 * 
	 * @return A filter implementation
	 */
	public static Filter getXMLValidCharacterFilter() {
		return xmlValidCharacterFilter;
	}
	
	/**
	 * The filter remove entities which are not valid in a
	 * XML 1.0 document like &amp;#25;.
	 * 
	 * @return A filter implementation
	 */
	public static Filter getXMLValidEntityFilter() {
		return xmlValidEntityFilter;
	}
	
	/**
	 * Get a cross site scripting filter instance
	 * @param set the maximum length allowed by the xss filter, -1 take the default value from the policy file
	 * @return
	 */
	public static Filter getXSSFilter(int maxLength) {
		// currently the XSS filter is statefull
		return new OWASPAntiSamyXSSFilter(maxLength, false);
	}
	
	/**
	 * 
	 * @param maxLength
	 * @return
	 */
	public static Filter getXSSFilterForTextField(int maxLength) {
		// currently the XSS filter is statefull
		return new OWASPAntiSamyXSSFilter(maxLength, false, Variant.tinyMce, false);
	}
	
	public static Filter getXSSFilterForWiki(int maxLength) {
		// currently the XSS filter is statefull
		return new OWASPAntiSamyXSSFilter(maxLength, false, Variant.wiki, false);
	}

	/**
	 * Get a filter to add a mapper base url to relative media links in HTML
	 * fragments
	 * 
	 * @param mapperBaseURL
	 * @return
	 */
	public static Filter getBaseURLToMediaRelativeURLFilter(String mapperBaseURL) {
		return new AddBaseURLToMediaRelativeURLFilter(mapperBaseURL);
	}

	/**
	 * Get a filter that replaces TinyMCE image tags with data uri images, e.g. for HTML emails
	 * @return
	 */
	public static Filter getSmileysCssToDataUriFilter() {
		return smileysCssToDataUriFilter;
	}
}
