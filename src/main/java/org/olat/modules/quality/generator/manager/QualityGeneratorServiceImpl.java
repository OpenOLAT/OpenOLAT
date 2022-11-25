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

import static org.olat.modules.quality.QualityReportAccessReference.of;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityGeneratorProviderReferenceable;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityGeneratorToOrganisation;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityGeneratorServiceImpl implements QualityGeneratorService {

	private static final Logger log = Tracing.createLoggerFor(QualityGeneratorServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityGeneratorDAO generatorDao;
	@Autowired
	private QualityGeneratorToOrganisationDAO generatorToOrganisationDao;
	@Autowired
	private QualityGeneratorProviderFactory providerFactory;
	@Autowired
	private QualityService qualityService;

	@Override
	public QualityGenerator createGenerator(String providerType, Collection<Organisation> organisations) {
		QualityGenerator generator = generatorDao.create(providerType);
		for (Organisation organisation: organisations) {
			generatorToOrganisationDao.createRelation(generator, organisation);
		}
		return generator;
	}

	@Override
	public QualityGenerator updateGenerator(QualityGenerator generator) {
		return generatorDao.save(generator);
	}

	@Override
	public QualityGenerator loadGenerator(QualityGeneratorRef generatorRef) {
		return generatorDao.loadByKey(generatorRef);
	}

	@Override
	public List<QualityGeneratorView> loadGenerators(QualityGeneratorSearchParams searchParams) {
		return generatorDao.load(searchParams);
	}

	@Override
	public long getNumberOfDataCollections(QualityGenerator generator) {
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setGeneratorRefs(Collections.singletonList(generator));
		List<QualityGeneratorView> generators = generatorDao.load(searchParams);
		return !generators.isEmpty()? generators.get(0).getNumberDataCollections(): 0l;
	}

	@Override
	public void deleteGenerator(QualityGeneratorRef generatorRef) {
		qualityService.deleteReportAccess(of(generatorRef));
		generatorToOrganisationDao.deleteRelations(generatorRef);
		generatorDao.delete(generatorRef);
	}

	@Override
	public void updateGeneratorOrganisations(QualityGenerator generator, List<Organisation> organisations) {
		Set<QualityGeneratorToOrganisation> currentRelations = new HashSet<>(
				generatorToOrganisationDao.loadByGeneratorKey(generator));
		List<QualityGeneratorToOrganisation> copyRelations = new ArrayList<>(currentRelations);
		List<Organisation> currentOrganisationsByRelations = new ArrayList<>();
		for (QualityGeneratorToOrganisation relation : copyRelations) {
			if (!organisations.contains(relation.getOrganisation())) {
				generatorToOrganisationDao.delete(relation);
				currentRelations.remove(relation);
			} else {
				currentOrganisationsByRelations.add(relation.getOrganisation());
			}
		}

		for (Organisation organisation : organisations) {
			if (!currentOrganisationsByRelations.contains(organisation)) {
				QualityGeneratorToOrganisation newRelation = generatorToOrganisationDao.createRelation(generator,
						organisation);
				currentRelations.add(newRelation);
			}
		}
	}

	@Override
	public List<Organisation> loadGeneratorOrganisations(QualityGeneratorRef generatorRef) {
		return generatorToOrganisationDao.loadOrganisationsByGeneratorKey(generatorRef);
	}

	@Override
	public QualityGeneratorConfigs loadGeneratorConfigs(QualityGenerator generator) {
		return new QualityGeneratorConfigsImpl(generator);
	}

	@Override
	public String getProviderDisplayName(QualityGeneratorProviderReferenceable providerRef, Locale locale) {
		QualityGeneratorProvider provider = providerFactory.getProvider(providerRef.getType());
		return provider.getDisplayname(locale);
	}

	@Override
	public ProviderConfigController getConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGenerator generator) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
		return provider.getConfigController(ureq, wControl, mainForm, configs);
	}

	@Override
	public String getGeneratorEnableInfo(QualityGenerator generator, Date fromDate, Date toDate, Locale locale) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
		return provider.getEnableInfo(generator, configs, fromDate, toDate, locale);
	}

	@Override
	public boolean hasWhiteListController(QualityGenerator generator) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		return provider.hasWhiteListController();
	}

	@Override
	public Controller getWhiteListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
		return provider.getWhiteListController(ureq, wControl, secCallback, stackPanel, generator, configs);
	}

	@Override
	public boolean hasBlackListController(QualityGenerator generator) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		return provider.hasBlackListController();
	}

	@Override
	public Controller getBlackListController(UserRequest ureq, WindowControl wControl,
			GeneratorSecurityCallback secCallback, TooledStackedPanel stackPanel, QualityGenerator generator) {
		QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
		QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
		return provider.getBlackListController(ureq, wControl, secCallback, stackPanel, generator, configs);
	}

	@Override
	public void generateDataCollections() {
		List<QualityGenerator> generators = generatorDao.loadEnabledGenerators();
		for (QualityGenerator generator: generators) {
			generateDataCollection(generator);
		}
	}

	private void generateDataCollection(QualityGenerator generator) {
		try {
			tryToGenerateDataCollection(generator);
		} catch (Exception e) {
			log.error("Error while generating data collection from generator " + generator.toString(), e);
		}
	}

	private void tryToGenerateDataCollection(QualityGenerator generator) {
		if (generator.isEnabled()) {
			if (generator.getFormEntry() == null) {
				log.warn("Evaluation form of quality data generator is not available: " + generator);
			} else if (providerFactory.isAvailable(generator)) {
				QualityGeneratorProvider provider = providerFactory.getProvider(generator.getType());
				QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
				Date now = new Date();
				List<QualityDataCollection> dataCollections = provider.generate(generator, configs, generator.getLastRun(), now);
				copyReportAccess(generator, dataCollections);
				QualityGenerator reloadedGenerator = generatorDao.loadByKey(generator);
				reloadedGenerator.setLastRun(now);
				generatorDao.save(reloadedGenerator);
				dbInstance.commit();
				if (!dataCollections.isEmpty()) {
					log.info(dataCollections.size() + " data collections created by generator " + generator.toString());
				}
			} else {
				log.warn("Provider not found for quality data generator: " + generator.getType());
			}
		}
	}

	private void copyReportAccess(QualityGenerator generator, List<QualityDataCollection> dataCollections) {
		if (dataCollections == null || dataCollections.isEmpty()) return;
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(generator));
		List<QualityReportAccess> reportAccesses = qualityService.loadReportAccesses(searchParams);
		for (QualityDataCollection dataCollection : dataCollections) {
			for (QualityReportAccess reportAccess : reportAccesses) {
				qualityService.copyReportAccess(of(dataCollection), reportAccess);
			}
		}
	}

}
