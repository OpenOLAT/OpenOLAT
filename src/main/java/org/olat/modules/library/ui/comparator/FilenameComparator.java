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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.library.ui.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FilenameComparator implements Comparator<VFSItem> {
	
	private final Collator collator;
	
	public FilenameComparator(Collator collator) {
		this.collator = collator;
	}

	public FilenameComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	@Override
	public int compare(VFSItem o1, VFSItem o2) {
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		
		String n1 = o1.getName();
		String n2 = o2.getName();
		if(n1 == null) return -1;
		if(n2 == null) return 1;
		return collator.compare(n1, n2);
	}
}
