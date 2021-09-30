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
package org.olat.course.learningpath.obligation;

import java.io.Serializable;

import org.olat.group.BusinessGroupRef;
import org.olat.group.model.BusinessGroupRefImpl;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupExceptionalObligation extends AbstractExceptionalObligation implements Serializable {

	private static final long serialVersionUID = 6434962185712444003L;
	
	private Long businessGroupKey;
	private transient BusinessGroupRef businessGroupRef;
	
	public BusinessGroupRef getBusinessGroupRef() {
		if (businessGroupKey != null && businessGroupRef == null) {
			businessGroupRef = new BusinessGroupRefImpl(businessGroupKey);
		}
		return businessGroupRef;
	}
	
	public void setBusinessGroupRef(BusinessGroupRef businessGroupRef) {
		this.businessGroupRef = businessGroupRef;
		if (businessGroupRef != null) {
			this.businessGroupKey = businessGroupRef.getKey();
		} else {
			this.businessGroupKey = null;
		}
	}
}
