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
package org.olat.repository.model;

/**
 * 
 * Initial date: 8 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AutomaticLifecycleInfos {
	
	private int closed;
	private int totalToClose;
	private int deleted;
	private int totalToDelete;
	private int definitivelyDeleted;
	private int totalToDefinitivelyDelete;
	
	public AutomaticLifecycleInfos() {
		this(0, 0, 0, 0, 0, 0);
	}
	
	public AutomaticLifecycleInfos(int closed, int totalToClose,
			int deleted, int totalToDelete, int definitivelyDeleted, int totalToDefinitivelyDelete ) {
		this.closed = closed;
		this.totalToClose = totalToClose;
		this.deleted = deleted;
		this.totalToDelete = totalToDelete;
		this.definitivelyDeleted = definitivelyDeleted;
		this.totalToDefinitivelyDelete = totalToDefinitivelyDelete;
	}
	
	public int getClosed() {
		return closed;
	}
	
	public void setClosed(int closed) {
		this.closed = closed;
	}
	
	public int getTotalToClose() {
		return totalToClose;
	}
	
	public void setTotalToClose(int totalToClose) {
		this.totalToClose = totalToClose;
	}
	
	public int getDeleted() {
		return deleted;
	}
	
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}
	
	public int getTotalToDelete() {
		return totalToDelete;
	}
	
	public void setTotalToDelete(int totalToDelete) {
		this.totalToDelete = totalToDelete;
	}
	
	public int getDefinitivelyDeleted() {
		return definitivelyDeleted;
	}
	
	public void setDefinitivelyDeleted(int definitivelyDeleted) {
		this.definitivelyDeleted = definitivelyDeleted;
	}
	
	public int getTotalToDefinitivelyDelete() {
		return totalToDefinitivelyDelete;
	}
	
	public void setTotalToDefinitivelyDelete(int totalToDefinitivelyDelete) {
		this.totalToDefinitivelyDelete = totalToDefinitivelyDelete;
	}
}
