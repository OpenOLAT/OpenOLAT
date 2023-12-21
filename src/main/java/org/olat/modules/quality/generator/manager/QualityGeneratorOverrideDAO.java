/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.quality.generator.GeneratorOverrideSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.model.QualityGeneratorOverrideImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityGeneratorOverrideDAO {
	
	@Autowired
	private DB dbInstance;

	public QualityGeneratorOverride create(String identifier, QualityGenerator generator, Long generatorProviderKey) {
		QualityGeneratorOverrideImpl override = new QualityGeneratorOverrideImpl();
		override.setCreationDate(new Date());
		override.setLastModified(override.getCreationDate());
		override.setIdentifier(identifier);
		override.setGenerator(generator);
		override.setGeneratorProviderKey(generatorProviderKey);
		dbInstance.getCurrentEntityManager().persist(override);
		return override;
	}

	public QualityGeneratorOverride save(QualityGeneratorOverride override) {
		if (override instanceof QualityGeneratorOverrideImpl impl) {
			impl.setLastModified(new Date());
		}
		return dbInstance.getCurrentEntityManager().merge(override);
	}

	public void delete(String identifier) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualitygeneratoroverride as override");
		sb.append(" where override.identifier = :identifier");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("identifier", identifier)
				.executeUpdate();
	}

	public QualityGeneratorOverride load(String identifier) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select override");
		sb.append("  from qualitygeneratoroverride as override");
		sb.append(" where override.identifier = :identifier");
		
		List<QualityGeneratorOverride> resultList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGeneratorOverride.class)
				.setParameter("identifier", identifier)
				.getResultList();
		
		return resultList != null && !resultList.isEmpty()? resultList.get(0): null;
	}

	public List<QualityGeneratorOverride> load(GeneratorOverrideSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select override");
		sb.append("  from qualitygeneratoroverride as override");
		if (searchParams.getGeneratorKeys() != null && !searchParams.getGeneratorKeys().isEmpty()) {
			sb.and().append("override.generator.key in :generatorKeys");
		}
		if (searchParams.getDataCollectionCreated() != null) {
			sb.and().append("override.dataCollection is ").append("not ", searchParams.getDataCollectionCreated()).append("null");
		}
		
		TypedQuery<QualityGeneratorOverride> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGeneratorOverride.class);
		if (searchParams.getGeneratorKeys() != null && !searchParams.getGeneratorKeys().isEmpty()) {
			query.setParameter("generatorKeys", searchParams.getGeneratorKeys());
		}
		
		return query.getResultList();
	}

}
