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
package org.olat.course.certificate.ui;

import java.text.Collator;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 23 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementTreeComparator extends FlexiTreeNodeComparator {
	
	private final Collator collator;
	private final Translator taxonomyTranslator;
	
	public CertificateAndEfficiencyStatementTreeComparator(Locale locale) {
		collator = Collator.getInstance(locale);
		taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
	}

	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		CertificateAndEfficiencyStatementRow r1 = (CertificateAndEfficiencyStatementRow)o1;
		CertificateAndEfficiencyStatementRow r2 = (CertificateAndEfficiencyStatementRow)o2;
		
		int c = 0;
		if(r1.isTaxonomy() && r2.isTaxonomy()) {
			TaxonomyLevel t1 = r1.getTaxonomyLevel();
			TaxonomyLevel t2 = r2.getTaxonomyLevel();
			c = compareStrings(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, t1), TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, t2));
			if(c == 0) {
				c = compareLongs(t1.getKey(), t2.getKey());
			}
		} else if(r1.isTaxonomy()) {
			c = -1;
		} else if(r2.isTaxonomy()) {
			c = 1;
		} else {
			c = compareCurriculumElements(r1.getCurriculumElement(), r2.getCurriculumElement());
			if(c == 0) {
				c = compareStrings(r1.getDisplayName(), r2.getDisplayName());
			}
		}
		return c;
	}
	
	private int compareCurriculumElements(CurriculumElement c1, CurriculumElement c2) {
		int c = 0;
		if(c1 == null || c2 == null) {
			c = compareNullObjects(c1, c2);
		} else {
			Integer p1 = c1.getPos();
			Integer p2 = c2.getPos();
			c = compareIntegers(p1, p2);
		}
		return c;
	}
	
	private int compareStrings(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return compareNullObjects(s1, s2);
		}
		return collator.compare(s1, s2);
	}
	
	private int compareIntegers(Integer l1, Integer l2) {
		if (l1 == null || l2 == null) {
			return compareNullObjects(l1, l2);
		}
		return l1.compareTo(l2);
	}
	
	private int compareLongs(Long l1, Long l2) {
		if (l1 == null || l2 == null) {
			return compareNullObjects(l1, l2);
		}
		return l1.compareTo(l2);
	}
}
