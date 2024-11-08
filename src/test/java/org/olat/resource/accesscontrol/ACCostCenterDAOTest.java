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
package org.olat.resource.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.resource.accesscontrol.manager.ACCostCenterDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ACCostCenterDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACCostCenterDAO sut;

	@Test
	public void shouldCreate() {
		CostCenter costCenter = sut.create();
		dbInstance.commitAndCloseSession();
		
		assertThat(costCenter.getCreationDate()).isNotNull();
		assertThat(costCenter.getLastModified()).isNotNull();
		assertThat(costCenter.isEnabled()).isTrue();
	}
	
	@Test
	public void shouldDeleteByKey() {
		CostCenter costCenter1 = sut.create();
		CostCenter costCenter2 = sut.create();
		CostCenter costCenter3 = sut.create();
		dbInstance.commitAndCloseSession();
		
		CostCenterSearchParams searchParams = new CostCenterSearchParams();
		List<CostCenter> costCenteres = sut.loadCostCenters(searchParams);
		assertThat(costCenteres).contains(costCenter1, costCenter2, costCenter3);
		
		sut.delete(costCenter1);
		dbInstance.commitAndCloseSession();
		
		costCenteres = sut.loadCostCenters(searchParams);
		assertThat(costCenteres)
				.contains(costCenter2, costCenter3)
				.doesNotContain(costCenter1);
	}
	
	@Test
	public void shouldFiler_organisations() {
		CostCenter costCenter1 = sut.create();
		CostCenter costCenter2 = sut.create();
		sut.create();
		dbInstance.commitAndCloseSession();
		
		CostCenterSearchParams searchParams = new CostCenterSearchParams();
		searchParams.setCostCenters(List.of(costCenter1, costCenter2));
		List<CostCenter> costCenteres = sut.loadCostCenters(searchParams);
		
		assertThat(costCenteres).containsExactlyInAnyOrder(costCenter1, costCenter2);
	}
	
	@Test
	public void shouldFiler_enabled() {
		CostCenter costCenter1 = sut.create();
		CostCenter costCenter2 = sut.create();
		costCenter2.setEnabled(false);
		costCenter2 = sut.update(costCenter2);
		dbInstance.commitAndCloseSession();
		
		CostCenterSearchParams searchParams = new CostCenterSearchParams();
		searchParams.setCostCenters(List.of(costCenter1, costCenter2));
		assertThat(sut.loadCostCenters(searchParams)).containsExactlyInAnyOrder(costCenter1, costCenter2);
		
		searchParams.setEnabled(Boolean.TRUE);
		assertThat(sut.loadCostCenters(searchParams)).containsExactlyInAnyOrder(costCenter1);
		
		searchParams.setEnabled(Boolean.FALSE);
		assertThat(sut.loadCostCenters(searchParams)).containsExactlyInAnyOrder(costCenter2);
	}
	
}
