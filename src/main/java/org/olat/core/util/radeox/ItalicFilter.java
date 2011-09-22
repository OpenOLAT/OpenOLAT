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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.radeox;

import org.radeox.filter.regex.RegexReplaceFilter;

/**
 * Description:<br>
 * Italic filter that is added for compatibillity with the wikimedia synatx form
 * the WikiMarkupComponent. Filters " ''word-xy''  " and replaces it with <i>word-xy<i>
 * <P>
 * Initial Date: Jul 24, 2006 <br>
 * 
 * @author guido
 */
public class ItalicFilter extends RegexReplaceFilter {

	/**
	 * 
	 */
	public ItalicFilter() {
		super("(^|>|[\\p{Punct}\\p{Space}]+)'{2}(.*?)'{2}([\\p{Punct}\\p{Space}]+|<|$)", "$1<i>$2</i>$3");
	}

}
