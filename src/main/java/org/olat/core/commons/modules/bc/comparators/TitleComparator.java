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
import org.olat.core.util.StringHelper;
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
	
	@Override
	public int compare(VFSItem i1, VFSItem i2) {
		String n1 = getName(i1);
		String n2 = getName(i2);
		if(n1 == null) return -1;
		if(n2 == null) return 1;
		
		int result =  collator.compare(n1, n2);
		if(result == 0) {
			long l1 = i1.getLastModified();
			long l2 = i2.getLastModified();
			result = Long.compare(l1, l2);
		}
		return result;
	}
	
	private String getName(VFSItem item) {
		if(item == null) return null;
		
		String name = null;
		if(item instanceof MetaTagged) {
			MetaInfo m = ((MetaTagged)item).getMetaInfo();
			if(m != null) {
				name = m.getTitle();
			}
		}
		
		if(!StringHelper.containsNonWhitespace(name)) {
			name = item.getName();
		}
		return name;
	}
}
