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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.course.statistic.daily;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.manager.BasicManager;
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
public class DailyStatisticManager extends BasicManager implements IStatisticManager {

	/** the SimpleDateFormat with which the column headers will be created formatted by the database, 
	 * so change this in coordination with any db changes if you really need to 
	 **/
	private final SimpleDateFormat columnHeaderFormat_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey) {
		DBQuery dbQuery = DBFactory.getInstance().createQuery("select businessPath,day,value from org.olat.course.statistic.daily.DailyStat sv "
				+ "where sv.resId=:resId");
		dbQuery.setLong("resId", courseRepositoryEntryKey);

		return new StatisticResult(course, dbQuery.list());
	}
	
	@Override
	public ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId) {
		if (column==0) {
			throw new IllegalStateException("column must never be 0 here");
		}
		
		String header = headerId;
		try{
			Date d = columnHeaderFormat_.parse(headerId);

			Calendar c = Calendar.getInstance(ureq.getLocale());
			c.setTime(d);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, ureq.getLocale());
			header = df.format(c.getTime());
		} catch(ParseException pe) {
			getLogger().warn("createColumnDescriptor: ParseException while parsing "+headerId+".", pe);
		}
		TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(header, column, 
				StatisticDisplayController.CLICK_TOTAL_ACTION+column, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT);	
		cd.setTranslateHeaderKey(false);
		return cd;
	}

	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate) {
		if (fromDate==null && toDate==null) {
			// no restrictions, return the defaults
			StatisticResult statisticResult = generateStatisticResult(ureq, course, courseRepositoryEntryKey);
			fillGapsInColumnHeaders(statisticResult);
			return statisticResult;
		}
		
		StringBuffer dateClause = new StringBuffer();
		if (fromDate!=null) {
			dateClause.append(" and (day>=:fromDate) ");
		}
		if (toDate!=null) {
			dateClause.append(" and (day<=:toDate) ");
		}
		DBQuery dbQuery = DBFactory.getInstance().createQuery("select businessPath,day,value from org.olat.course.statistic.daily.DailyStat sv "
				+ "where sv.resId=:resId "
				+ dateClause);
		dbQuery.setLong("resId", courseRepositoryEntryKey);
		if (fromDate!=null) {
			dbQuery.setDate("fromDate", fromDate);
		}
		if (toDate!=null) {
			dbQuery.setDate("toDate", toDate);
		}
		
		StatisticResult statisticResult = new StatisticResult(course, dbQuery.list());
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
			Date fromDate = columnHeaderFormat_.parse(firstDate);
			Date previousDate = new Date(fromDate.getTime()); // copy fromDate
			final long DAY_DIFF = 24*60*60*1000;
			for (int i = 1; i < columnHeaders.size(); i++) {
				String aDate = columnHeaders.get(i);
				Date currDate = columnHeaderFormat_.parse(aDate);
				long diff = currDate.getTime() - previousDate.getTime();
				// note that we should have full days - we have the HH:MM:SS set to 00:00:00 - hence the
				// difference should always be a full day
				if (diff>DAY_DIFF) {
					// then we should add a few days in here
					Date additionalDate = new Date(previousDate.getTime() + DAY_DIFF);
					String additionalDateStr = columnHeaderFormat_.format(additionalDate);
					columnHeaders.add(i, additionalDateStr);
					previousDate = additionalDate;
				} else {
					previousDate = currDate;
				}
			}
			
			statisticResult.setColumnHeaders(columnHeaders);
		} catch(ParseException e) {
			getLogger().warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ",e);
		}
	}

}
