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
package org.olat.modules.quality.analysis.manager;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.model.QualityDataCollectionRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.repository.model.RepositoryEntryRefImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 01.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisPresentationXStream {
	
	private static final Logger log = Tracing.createLoggerFor(AnalysisPresentationXStream.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				MultiGroupBy.class, GroupBy.class, AnalysisSearchParameter.class, QualityDataCollectionRefImpl.class,
				RepositoryEntryRefImpl.class, IdentityRefImpl.class, OrganisationRefImpl.class, CurriculumRefImpl.class,
				CurriculumElementRefImpl.class, CurriculumElementTypeRefImpl.class, TaxonomyLevelRefImpl.class };
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("multiGroupBy", MultiGroupBy.class);
		xstream.alias("groupBy", GroupBy.class);
		xstream.alias("AnalysisSearchParameter", AnalysisSearchParameter.class);
		xstream.alias("QualityDataCollectionRef", QualityDataCollectionRefImpl.class);
		xstream.alias("RepositoryEntryRef", RepositoryEntryRefImpl.class);
		xstream.alias("IdentityRef", IdentityRefImpl.class);
		xstream.alias("OrganisationRef", OrganisationRefImpl.class);
		xstream.alias("CurriculumRef", CurriculumRefImpl.class);
		xstream.alias("CurriculumElementRef", CurriculumElementRefImpl.class);
		xstream.alias("CurriculumElementTypeRef", CurriculumElementTypeRefImpl.class);
		xstream.alias("TaxonomyLevelRef", TaxonomyLevelRefImpl.class);
	}
	
	static String toXml(Object obj) {
		if (obj == null) return null;
		
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = xstream.fromXML(xml);
				return (U)obj;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}

}
