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
package org.olat.core.commons.modules.bc.meta.tagged;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.util.vfs.VFSItem;

/**
 * Compare the title, the filename and the last modification date
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TitleComparator implements Comparator<VFSItem> {
	private final Collator collator;

	public TitleComparator(Collator collator) {
		this.collator = collator;
	}

	public TitleComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	public int compare(VFSItem i1, VFSItem i2) {
		int result = 0;
		if(i1 instanceof MetaTagged && i2 instanceof MetaTagged) {
			MetaInfo m1 = ((MetaTagged)i1).getMetaInfo();
			MetaInfo m2 = ((MetaTagged)i2).getMetaInfo();
			if(m1 != null && m2 != null) {
				String t1 = m1.getTitle();
				String t2 = m2.getTitle();
				if(t1 != null && t2 != null) {
					result = collator.compare(t1, t2);
				}
			}
		}
		
		if(result == 0) {
			String n1 = i1.getName();
			String n2 = i2.getName();
			if(n1 != null && n2 != null) {
				result = collator.compare(n1, n2);
			}
		}
		
		if(result == 0) {
			long l1 = i1.getLastModified();
			long l2 = i2.getLastModified();
			result = l1<l2 ? -1 : (l1==l2 ? 0 : 1);
		}
		return result;
	}
}
