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
package org.olat.restapi.system.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "hibernateStatisticsVO")
public class HibernateStatisticsVO {
	
	@XmlAttribute(name="openSessionsCount", required=true)
	private long openSessionsCount;
	
	@XmlAttribute(name="transactionsCount", required=true)
	private long transactionsCount;
	@XmlAttribute(name="successfulTransactionCount", required=true)
	private long successfulTransactionCount;
	@XmlAttribute(name="failedTransactionsCount", required=true)
	private long failedTransactionsCount;
	
	@XmlAttribute(name="optimisticFailureCount", required=true)
	private long optimisticFailureCount;
	
	@XmlAttribute(name="queryExecutionMaxTime", required=true)
	private long queryExecutionMaxTime;
	@XmlAttribute(name="queryExecutionMaxTimeQueryString", required=true)
	private String queryExecutionMaxTimeQueryString;
	@XmlAttribute(name="queryExecutionCount", required=true)
	private long queryExecutionCount;
	

	public long getOpenSessionsCount() {
		return openSessionsCount;
	}

	public void setOpenSessionsCount(long openSessionsCount) {
		this.openSessionsCount = openSessionsCount;
	}

	public long getTransactionsCount() {
		return transactionsCount;
	}

	public void setTransactionsCount(long transactionsCount) {
		this.transactionsCount = transactionsCount;
	}

	public long getSuccessfulTransactionCount() {
		return successfulTransactionCount;
	}

	public void setSuccessfulTransactionCount(long successfulTransactionCount) {
		this.successfulTransactionCount = successfulTransactionCount;
	}

	public long getFailedTransactionsCount() {
		return failedTransactionsCount;
	}
	
	public void setFailedTransactionsCount(long failedTransactionsCount) {
		this.failedTransactionsCount = failedTransactionsCount;
	}
	
	public long getOptimisticFailureCount() {
		return optimisticFailureCount;
	}

	public void setOptimisticFailureCount(long optimisticFailureCount) {
		this.optimisticFailureCount = optimisticFailureCount;
	}
	
	public long getQueryExecutionCount() {
		return queryExecutionCount;
	}

	public void setQueryExecutionCount(long queryExecutionCount) {
		this.queryExecutionCount = queryExecutionCount;
	}

	public long getQueryExecutionMaxTime() {
		return queryExecutionMaxTime;
	}
	
	public void setQueryExecutionMaxTime(long queryExecutionMaxTime) {
		this.queryExecutionMaxTime = queryExecutionMaxTime;
	}

	public String getQueryExecutionMaxTimeQueryString() {
		return queryExecutionMaxTimeQueryString;
	}

	public void setQueryExecutionMaxTimeQueryString(String queryExecutionMaxTimeQueryString) {
		this.queryExecutionMaxTimeQueryString = queryExecutionMaxTimeQueryString;
	}
}
