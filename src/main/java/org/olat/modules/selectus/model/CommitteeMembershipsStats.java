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
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 22.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMembershipsStats {
	
	private final int numAsSecretary;
	private final int numAsHead;
	private final int numAsExOfficio;
	
	public CommitteeMembershipsStats(int numAsSecretary, int numAsHead, int numAsExOfficio) {
		this.numAsHead = numAsHead;
		this.numAsSecretary = numAsSecretary;
		this.numAsExOfficio = numAsExOfficio;
	}
	
	public static CommitteeMembershipsStats empty() {
		return new CommitteeMembershipsStats(0, 0, 0);
	}
	
	public int getNumAsSecretary() {
		return numAsSecretary;
	}
	
	public int getNumAsHead() {
		return numAsHead;
	}
	
	public int getNumAsExOfficio() {
		return numAsExOfficio;
	}
}
