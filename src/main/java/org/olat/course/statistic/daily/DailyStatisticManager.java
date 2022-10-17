/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.statistic.daily;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;
import org.olat.course.statistic.TotalAwareColumnDescriptor;

/**
 * Implementation of the IStatisticManager for 'weekly' statistic - 
 * specific for Mysql since it uses the mysql specific week(date,mode) function
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class DailyStatisticManager implements IStatisticManager {
	
	private static final Logger log = Tracing.createLoggerFor(DailyStatisticManager.class);

	/** the SimpleDateFormat with which the column headers will be created formatted by the database, 
	 * so change this in coordination with any db changes if you really need to 
	 **/
	private final SimpleDateFormat columnHeaderFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey) {
		String q = "select businessPath,day,value from org.olat.course.statistic.daily.DailyStat sv where sv.resId=:resId";
		List<Object[]> raw = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(q, Object[].class)
				.setParameter("resId", courseRepositoryEntryKey).getResultList();
		return new StatisticResult(course, raw);
	}
	
	@Override
	public ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId) {
		if (column==0) {
			throw new IllegalStateException("column must never be 0 here");
		}
		
		String header = headerId;
		try{
			Date d = columnHeaderFormat.parse(headerId);

			Calendar c = Calendar.getInstance(ureq.getLocale());
			c.setTime(d);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, ureq.getLocale());
			header = df.format(c.getTime());
		} catch(ParseException pe) {
			log.warn("createColumnDescriptor: ParseException while parsing "+headerId+".", pe);
		}
		TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(header, column, 
				StatisticDisplayController.CLICK_TOTAL_ACTION+column, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT);
		cd.setTranslateHeaderKey(false);
		return cd;
	}

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate) {
		if (fromDate==null && toDate==null) {
			// no restrictions, return the defaults
			StatisticResult statisticResult = generateStatisticResult(ureq, course, courseRepositoryEntryKey);
			fillGapsInColumnHeaders(statisticResult);
			return statisticResult;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select businessPath,day,value from org.olat.course.statistic.daily.DailyStat sv where sv.resId=:resId");
		if (fromDate != null) {
			sb.append(" and (day>=:fromDate) ");
		}
		if (toDate != null) {
			sb.append(" and (day<=:toDate) ");
		}
		
		TypedQuery<Object[]> dbQuery = DBFactory.getInstance()
				.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseRepositoryEntryKey);
		if (fromDate != null) {
			dbQuery.setParameter("fromDate", fromDate, TemporalType.TIMESTAMP);
		}
		if (toDate != null) {
			dbQuery.setParameter("toDate", toDate, TemporalType.TIMESTAMP);
		}
		
		StatisticResult statisticResult = new StatisticResult(course, dbQuery.getResultList());
		fillGapsInColumnHeaders(statisticResult);
		return statisticResult;
	}

	/** fill any gaps in the column headers between the first and the last days **/
	private void fillGapsInColumnHeaders(StatisticResult statisticResult) {
		if (statisticResult==null) {
			throw new IllegalArgumentException("statisticResult must not be null");
		}
		List<String> columnHeaders = statisticResult.getColumnHeaders();
		if (columnHeaders.size()<=1) {
			// if the resulting set one or less, don't bother
			return;
		}
		try{
			String firstDate = columnHeaders.get(0);
			Date fromDate = columnHeaderFormat.parse(firstDate);
			Date previousDate = new Date(fromDate.getTime()); // copy fromDate
			final long DAY_DIFF = 24l * 60l * 60l * 1000l;
			for (int i = 1; i < columnHeaders.size(); i++) {
				String aDate = columnHeaders.get(i);
				Date currDate = columnHeaderFormat.parse(aDate);
				long diff = currDate.getTime() - previousDate.getTime();
				// note that we should have full days - we have the HH:MM:SS set to 00:00:00 - hence the
				// difference should always be a full day
				if (diff>DAY_DIFF) {
					// then we should add a few days in here
					Date additionalDate = new Date(previousDate.getTime() + DAY_DIFF);
					String additionalDateStr = columnHeaderFormat.format(additionalDate);
					columnHeaders.add(i, additionalDateStr);
					previousDate = additionalDate;
				} else {
					previousDate = currDate;
				}
			}
			
			statisticResult.setColumnHeaders(columnHeaders);
		} catch(ParseException e) {
			log.warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ",e);
		}
	}
}
