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
package org.olat.core.commons.modules.bc.meta.tagged;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaTitleComparator;

/**
 * Compares the title of two meta tagged objects.
 * 
 * <P>
 * Initial Date: Jul 9, 2009 <br>
 * 
 * @author gwassmann
 */
public class TitleComparator implements Comparator<MetaTagged> {
	private final Collator collator;

	public TitleComparator(Collator collator) {
		this.collator = collator;
	}

	public TitleComparator(Locale locale) {
		this.collator = Collator.getInstance(locale);
	}

	public int compare(MetaTagged one, MetaTagged two) {
		MetaInfo that = one.getMetaInfo();
		MetaInfo other = two.getMetaInfo();
		// Delegate!
		MetaTitleComparator comparator = new MetaTitleComparator(collator);
		return comparator.compare(that, other);
	}
}
