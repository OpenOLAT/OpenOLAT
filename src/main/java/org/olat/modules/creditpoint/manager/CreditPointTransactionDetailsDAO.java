/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;
import org.olat.modules.creditpoint.model.CreditPointTransactionDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointTransactionDetailsDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointTransactionDetails createTransactionDetails(BigDecimal amount,
			CreditPointTransaction source, CreditPointTransaction target) {
		CreditPointTransactionDetailsImpl details = new CreditPointTransactionDetailsImpl();
		details.setCreationDate(new Date());
		details.setAmount(amount);
		details.setSource(source);
		details.setTarget(target);
		dbInstance.getCurrentEntityManager().persist(details);
		return details;
	}
	
	public List<CreditPointTransactionDetails> loadTransactionDetails(CreditPointTransaction source) {
		String query = """
				select details from creditpointtransactiondetails as details
				inner join fetch details.source as source
				inner join fetch details.target as target
				where source.key=:sourceKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointTransactionDetails.class)
				.setParameter("sourceKey", source.getKey())
				.getResultList();
	}

}
