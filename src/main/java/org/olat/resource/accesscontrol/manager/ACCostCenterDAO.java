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
package org.olat.resource.accesscontrol.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.resource.accesscontrol.CostCenter;
import org.olat.resource.accesscontrol.CostCenterSearchParams;
import org.olat.resource.accesscontrol.model.CostCenterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ACCostCenterDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CostCenter create() {
		CostCenterImpl costCenter = new CostCenterImpl();
		costCenter.setCreationDate(new Date());
		costCenter.setLastModified(costCenter.getCreationDate());
		costCenter.setEnabled(true);
		dbInstance.getCurrentEntityManager().persist(costCenter);
		return costCenter;
	}
	
	public CostCenter update(CostCenter costCenter) {
		if (costCenter instanceof CostCenterImpl impl) {
			impl.setLastModified(new Date());
			return dbInstance.getCurrentEntityManager().merge(costCenter);
		}
		return costCenter;
	}
	
	public void delete(CostCenter costCenter) {
		String query = "delete from accostcenter costcenter where costcenter.key = :costCenterKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("costCenterKey", costCenter.getKey())
				.executeUpdate();
	}
	
	public List<CostCenter> loadCostCenters(CostCenterSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select costcenter");
		sb.append("  from accostcenter costcenter");
		if (searchParams.getCostCenterKeys() != null && !searchParams.getCostCenterKeys().isEmpty()) {
			sb.and().append("costcenter.key in :costCenterKeys");
		}
		if (searchParams.getEnabled() != null) {
			sb.and().append("costcenter.enabled = ").append(searchParams.getEnabled());
		}
		
		TypedQuery<CostCenter> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CostCenter.class);
		
		if (searchParams.getCostCenterKeys() != null && !searchParams.getCostCenterKeys().isEmpty()) {
			query.setParameter("costCenterKeys", searchParams.getCostCenterKeys());
		}
		
		return query.getResultList();
	}

}
