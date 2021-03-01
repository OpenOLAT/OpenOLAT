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
package org.olat.course.noderight.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.quality.ui.wizard.UserOverviewContext;

/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddIdentitiesContext implements UserOverviewContext {
	
	private final NodeRightWrapper wrapper;
	private Date start;
	private Date end;
	private List<Identity> identities;

	public AddIdentitiesContext(NodeRightWrapper wrapper) {
		this.wrapper = wrapper;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public NodeRightWrapper getWrapper() {
		return wrapper;
	}

	@Override
	public List<Identity> getIdentities() {
		if (identities == null) {
			identities = new ArrayList<>(0);
		}
		return identities;
	}

	@Override
	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}

}
