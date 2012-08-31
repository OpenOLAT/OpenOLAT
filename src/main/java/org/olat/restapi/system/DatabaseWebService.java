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
package org.olat.restapi.system;

import java.util.Collection;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.stat.Statistics;
import org.olat.admin.jmx.JMXManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.restapi.system.vo.DatabaseConnectionVO;
import org.olat.restapi.system.vo.DatabaseVO;
import org.olat.restapi.system.vo.HibernateStatisticsVO;

import com.mchange.v2.c3p0.PooledDataSource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DatabaseWebService {
	
	private static final OLog log = Tracing.createLoggerFor(DatabaseWebService.class);
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getDatabaseStatistics() {
		DatabaseConnectionVO connections = getConnectionInfos();
		HibernateStatisticsVO hibernateStats = getHibernateStatistics();
		
		DatabaseVO vo = new DatabaseVO();
		vo.setConnectionInfos(connections);
		vo.setHibernateStatistics(hibernateStats);
		return Response.ok(vo).build();
	}
	
	private HibernateStatisticsVO getHibernateStatistics() {
		
		Statistics statistics = DBFactory.getInstance().getStatistics();
		if(!statistics.isStatisticsEnabled()) {
			return null;
		}
		
		HibernateStatisticsVO stats = new HibernateStatisticsVO();
		stats.setOpenSessionsCount(statistics.getSessionOpenCount());
		stats.setTransactionsCount(statistics.getTransactionCount());
		stats.setSuccessfulTransactionCount(statistics.getSuccessfulTransactionCount());
		stats.setFailedTransactionsCount(statistics.getTransactionCount() - statistics.getSuccessfulTransactionCount());
		stats.setOptimisticFailureCount(statistics.getOptimisticFailureCount());
		stats.setQueryExecutionCount(statistics.getQueryExecutionCount());
		stats.setQueryExecutionMaxTime(statistics.getQueryExecutionMaxTime());
		stats.setQueryExecutionMaxTimeQueryString(statistics.getQueryExecutionMaxTimeQueryString());
		return stats;
	}
	
	private DatabaseConnectionVO getConnectionInfos() {
		DatabaseConnectionVO vo = new DatabaseConnectionVO();
		try {
			int activeConnectionCount = 0;
			int currentConnectionCount = 0;

			JMXManager jmxManager = CoreSpringFactory.getImpl(JMXManager.class);
			MBeanServer mBeanServer = jmxManager.getMBeanServer();
			ObjectName c3p0ObjectName = new ObjectName("com.mchange.v2.c3p0:type=C3P0Registry");

			Object attributes = mBeanServer.getAttribute(c3p0ObjectName, "AllPooledDataSources");
			if(attributes instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<Object> attributeCollection = (Collection<Object>)attributes;
				for(Object attribute : attributeCollection) {
					if(attribute instanceof PooledDataSource) {
						PooledDataSource dataSource = (PooledDataSource)attribute;
						activeConnectionCount += dataSource.getNumBusyConnectionsAllUsers();
						currentConnectionCount += dataSource.getNumConnectionsAllUsers();
					}
				}
			}

			vo.setActiveConnectionCount(activeConnectionCount);
			vo.setCurrentConnectionCount(currentConnectionCount);
			
		} catch (Exception e) {
			log.error("", e);
		}
		return vo;
	}
}
