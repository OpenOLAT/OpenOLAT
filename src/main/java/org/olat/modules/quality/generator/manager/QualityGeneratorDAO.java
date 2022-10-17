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
package org.olat.modules.quality.generator.manager;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.generator.model.QualityGeneratorImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityGeneratorDAO {
	
	@Autowired
	private DB dbInstance;

	QualityGenerator create(String providerType) {
		QualityGeneratorImpl generator = new QualityGeneratorImpl();
		generator.setCreationDate(new Date());
		generator.setLastModified(generator.getCreationDate());
		generator.setType(providerType);
		generator.setEnabled(Boolean.FALSE);
		dbInstance.getCurrentEntityManager().persist(generator);
		return generator;
	}
	
	QualityGenerator save(QualityGenerator generator) {
		generator.setLastModified(new Date());
		generator = dbInstance.getCurrentEntityManager().merge(generator);
		return generator;
	}

	QualityGenerator loadByKey(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select generator");
		sb.append("  from qualitygenerator as generator");
		sb.append("       left join fetch generator.formEntry");
		sb.append(" where generator.key = :generatorKey");
		
		List<QualityGenerator> generators = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGenerator.class)
				.setParameter("generatorKey", generatorRef.getKey())
				.getResultList();
		return generators.isEmpty() ? null : generators.get(0);
	}

	List<QualityGenerator> loadEnabledGenerators() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select generator");
		sb.append("  from qualitygenerator as generator");
		sb.append(" where generator.enabled = true");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGenerator.class)
				.getResultList();
	}
	
	boolean isFormEntryInUse(RepositoryEntryRef formEntry) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select generator.key");
		sb.append("  from qualitygenerator as generator");
		sb.append(" where generator.formEntry.key=:formEntryKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFirstResult(0)
				.setMaxResults(1)
				.setParameter("formEntryKey", formEntry.getKey())
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}

	void delete(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualitygenerator as generator");
		sb.append(" where generator.key = :generatorKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("generatorKey", generatorRef.getKey())
				.executeUpdate();
	}

	List<QualityGeneratorView> load(QualityGeneratorSearchParams searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.generator.model.QualityGeneratorViewImpl(");
		sb.append("       generator.key");
		sb.append("     , generator.creationDate");
		sb.append("     , generator.type");
		sb.append("     , generator.title");
		sb.append("     , generator.enabled");
		sb.append("     , (");
		sb.append("           select count(*)");
		sb.append("             from qualitydatacollection collection");
		sb.append("            where collection.generator.key = generator.key");
		sb.append("       )");
		sb.append("     )");
		sb.append("  from qualitygenerator as generator");
		sb.append(" where 1=1");
		appendWhereClause(sb, searchParams);
		
		TypedQuery<QualityGeneratorView> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), QualityGeneratorView.class);
		appendParameter(query, searchParams);

		return query.getResultList();
	}

	private void appendWhereClause(StringBuilder sb, QualityGeneratorSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getGeneratorRefs() != null && !searchParams.getGeneratorRefs().isEmpty()) {
				sb.append(" and generator.key in :generatorKeys");
			}
			if (searchParams.getOrganisationRefs() != null) {
				sb.append(" and generator.key in (");
				sb.append("     select generatorToOrganisation.generator.key");
				sb.append("       from qualitygeneratortoorganisation as generatorToOrganisation");
				sb.append("      where generatorToOrganisation.organisation.key in :organisationKeys");
				sb.append(" )");
			}
			if (StringHelper.containsNonWhitespace(searchParams.getProviderType())) {
				sb.append(" and generator.type = :providerType");
			}
		}
	}

	private void appendParameter(TypedQuery<QualityGeneratorView> query,
			QualityGeneratorSearchParams searchParams) {
		if (searchParams != null) {
			if (searchParams.getGeneratorRefs() != null && !searchParams.getGeneratorRefs().isEmpty()) {
				List<Long> generatorKeys = searchParams.getGeneratorRefs().stream().map(QualityGeneratorRef::getKey).collect(toList());
				query.setParameter("generatorKeys", generatorKeys);
			}
			if (searchParams.getOrganisationRefs() != null) {
				List<Long> organiationKeys = searchParams.getOrganisationRefs().stream().map(OrganisationRef::getKey).collect(toList());
				organiationKeys = !organiationKeys.isEmpty()? organiationKeys: Collections.singletonList(-1l);
				query.setParameter("organisationKeys", organiationKeys);
			}
			if (StringHelper.containsNonWhitespace(searchParams.getProviderType())) {
				query.setParameter("providerType", searchParams.getProviderType());
			}
		}
	}

}
