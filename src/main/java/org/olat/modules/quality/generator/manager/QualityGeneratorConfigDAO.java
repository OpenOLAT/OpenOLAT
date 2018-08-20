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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfig;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.model.QualityGeneratorConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityGeneratorConfigDAO {
	
	@Autowired
	private DB dbInstance;

	QualityGeneratorConfig create(QualityGenerator generator, String identifier, String value) {
		QualityGeneratorConfigImpl config = new QualityGeneratorConfigImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setIdentifier(identifier);
		config.setValue(value);
		config.setGenerator(generator);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}

	List<QualityGeneratorConfig> loadByGenerator(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select config");
		sb.append("  from qualitygeneratorconfig as config");
		sb.append(" where config.generator.key = :generatorKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGeneratorConfig.class)
				.setParameter("generatorKey", generatorRef.getKey())
				.getResultList();
	}

	QualityGeneratorConfig save(QualityGeneratorConfig config) {
		config.setLastModified(new Date());
		config = dbInstance.getCurrentEntityManager().merge(config);
		return config;
	}

	void delete(QualityGeneratorConfig config) {
		if (config == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete");
		sb.append("  from qualitygeneratorconfig as config");
		sb.append(" where config.key = :configKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("configKey", config.getKey())
				.executeUpdate();
	}

	void deleteAll(QualityGeneratorRef generatorRef) {
		if (generatorRef == null || generatorRef.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualitygeneratorconfig as config");
		sb.append(" where config.generator.key = :generatorKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("generatorKey", generatorRef.getKey())
				.executeUpdate();
	}

}
