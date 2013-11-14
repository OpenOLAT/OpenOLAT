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

/**
 * Compares the title of two meta tagged objects.
 * 
 * <P>
 * Initial Date: Jul 9, 2009 <br>
 * 
 * @author gwassmann
 */
public class MetaTaggedComparator implements Comparator<MetaTagged> {
	private final Comparator<MetaInfo> comparator;

	public MetaTaggedComparator(Collator collator) {
		comparator = new MetaTitleComparator(collator);
	}

	public MetaTaggedComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	public int compare(MetaTagged i1, MetaTagged i2) {
		MetaInfo m1 = i1.getMetaInfo();
		MetaInfo m2 = i2.getMetaInfo();
		return comparator.compare(m1, m2);

	}
}
