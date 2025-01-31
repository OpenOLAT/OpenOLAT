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
package org.olat.modules.curriculum.ui;

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;

/**
 * 
 * Initial date: Jan 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementCatalogStatusEvaluator implements CatalogStatusEvaluator {

	private final CurriculumElementStatus status;

	private CurriculumElementCatalogStatusEvaluator(CurriculumElementStatus status) {
		this.status = status;
	}

	@Override
	public boolean isVisibleStatusNoPeriod() {
		return Arrays.asList(ACService.CESTATUS_ACTIVE_METHOD).contains(status);
	}

	@Override
	public boolean isVisibleStatusPeriod() {
		return Arrays.asList(ACService.CESTATUS_ACTIVE_METHOD_PERIOD).contains(status);
	}
	
	public static final CatalogStatusEvaluator create(CurriculumElementStatus status) {
		return create(CoreSpringFactory.getImpl(CatalogV2Module.class), status);
	}
	
	public static final CatalogStatusEvaluator create(CatalogV2Module catalogV2Module, CurriculumElementStatus status) {
		if (catalogV2Module.isEnabled()) {
			return new CurriculumElementCatalogStatusEvaluator(status);
		}
		return CatalogInfo.TRUE_STATUS_EVALUATOR;
	}

}
