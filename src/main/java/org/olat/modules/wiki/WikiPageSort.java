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

package org.olat.modules.wiki;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.util.vfs.VFSItem;

/**
 * Description:<br>
 * Class to sort wiki pages either by alphabetical order or by modification time order
 * <P>
 * Initial Date: Jun 8, 2006 <br>
 * 
 * @author guido
 */
public class WikiPageSort {
	/**
	 * use this comparator if you like a list of sorted pages by pageName
	 */
	protected static final Comparator<WikiPage> PAGENAME_ORDER = new Comparator<WikiPage>() {
		@Override
		public int compare(WikiPage p1, WikiPage p2) {
			return p1.getPageName().compareTo(p2.getPageName());
		}

	};
	/**
	 * use this comparator if you like a list of sorted pages by mod time
	 */
	protected static final Comparator<WikiPage> MODTIME_ORDER = new Comparator<WikiPage>() {
		@Override
		public int compare(WikiPage p1, WikiPage p2) {
			// the "-" in from of the Long return negative int and therefore realizes an descending order
			return - Long.valueOf(p1.getModificationTime()).compareTo(Long.valueOf(p2.getModificationTime()));
		}
	};
	
	public static class WikiPageNameComparator implements Comparator<String> {
		
		private final Collator collator;
		
		public WikiPageNameComparator(Locale locale) {
			 collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(String n1, String n2) {
			if(n1 == null) {
				return n2 == null ? 0 : 1;
			} else if(n2 == null) {
				return -1;
			}
			return collator.compare(n1, n2);
		}
	}
	
	public static class WikiFileComparator implements Comparator<VFSItem> {
		
		private final Collator collator;
		
		public WikiFileComparator(Locale locale) {
			 collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(VFSItem v1, VFSItem v2) {
			if(v1 == null) {
				return v2 == null ? 0 : 1;
			} else if(v2 == null) {
				return -1;
			}
			
			String n1 = v1.getName();
			String n2 = v2.getName();
			
			if(n1 == null) {
				return n2 == null ? 0 : 1;
			} else if(n2 == null) {
				return -1;
			}
			return collator.compare(n1, n2);
		}
	};
}
