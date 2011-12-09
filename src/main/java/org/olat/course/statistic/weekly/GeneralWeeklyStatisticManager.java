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

package org.olat.course.statistic.weekly;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.logging.OLog;
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
public class GeneralWeeklyStatisticManager implements IStatisticManager {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(GeneralWeeklyStatisticManager.class);
	
	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey) {
		DBQuery dbQuery = DBFactory.getInstance().createQuery("select businessPath,week,value from org.olat.course.statistic.weekly.WeeklyStat sv "
				+ "where sv.resId=:resId");
		dbQuery.setLong("resId", courseRepositoryEntryKey);

		return new StatisticResult(course, dbQuery.list());
	}
	
	@Override
	public ColumnDescriptor createColumnDescriptor(UserRequest ureq, int column, String headerId) {
		if (column==0) {
			throw new IllegalStateException("column must never be 0 here");
		}
		
		TotalAwareColumnDescriptor cd = new TotalAwareColumnDescriptor(headerId, column, 
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
		
		SimpleDateFormat sdf = new SimpleDateFormat(); 
		StringBuffer dateClause = new StringBuffer();
		//concat(year(creationdate),'-',week(creationdate)) week
		if (fromDate!=null) {
			dateClause.append(" and (week=concat(:yearFromDate,'-',:weekFromDate) or week>=concat(:yearFromDate,'-',:weekFromDate)) ");
		}
		if (toDate!=null) {
			dateClause.append(" and (week=concat(:yearToDate,'-',:weekToDate) or week<=concat(:yearToDate,'-',:weekToDate)) ");
		}
		DBQuery dbQuery = DBFactory.getInstance().createQuery("select businessPath,week,value from org.olat.course.statistic.weekly.WeeklyStat sv "
				+ "where sv.resId=:resId "
				+ dateClause);
		dbQuery.setLong("resId", courseRepositoryEntryKey);
		if (fromDate!=null) {
			dbQuery.setString("yearFromDate", getYear(sdf, fromDate));
			dbQuery.setString("weekFromDate", getWeek(sdf, fromDate, MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault())));
		}
		if (toDate!=null) {
			dbQuery.setString("yearToDate", getYear(sdf, toDate));
			dbQuery.setString("weekToDate", getWeek(sdf, toDate, MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault())));
		}
		// note: we use the default locale here since the WeeklyStatisticUpdater uses the default as well
		//       there is no such concept as 'weekly stats table per user locale'
		//       instead, the weekly stats table is generated with the system default locale
		// dbQuery.setInteger("weekMode", MysqlWeekHelper.getMysqlWeekMode(Locale.getDefault()));
		
		StatisticResult statisticResult = new StatisticResult(course, dbQuery.list());
		fillGapsInColumnHeaders(statisticResult);
		return statisticResult;
	}

	private String getWeek(SimpleDateFormat sdf, Date date, int mysqlWeekMode) {
		sdf.applyPattern("w");
		return sdf.format(date);
	}

	private String getYear(SimpleDateFormat sdf, Date date) {
		sdf.applyPattern("yyyy");
		return sdf.format(date);
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
			String firstWeek = columnHeaders.get(0);
			String previousWeek = firstWeek;
			for (int i = 1; i < columnHeaders.size(); i++) {
				String currWeek = columnHeaders.get(i);
				
				if (!isNextWeek(previousWeek, currWeek)) {
					String additionalWeek = nextWeek(previousWeek);
					if (columnHeaders.contains(additionalWeek)) {
						// oups, then we have a bug in our algorithm or what?
						log_.warn("fillGapsInColumnHeaders: throwing a ParseException, can't add "+additionalWeek+" to "+columnHeaders);
						throw new ParseException("Can't add "+additionalWeek+" to the list of weeks - it is already there", 0);
					}
					columnHeaders.add(i, additionalWeek);
					previousWeek = additionalWeek;
				} else {
					previousWeek = currWeek;
				}
			}
			statisticResult.setColumnHeaders(columnHeaders);
		} catch(ParseException e) {
			log_.warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ",e);
		}
		
	}

	private String nextWeek(String week) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-w");
		Date d = sdf.parse(week);
		d = new Date(d.getTime() + 7*24*60*60*1000);
		String result = sdf.format(d);
		
		// bug with SimpleDateFormat:
		//   Mon Dec 29 00:00:00 CET 2008
		// returns
		//   2008-01
		// which should probably rather be
		//   2009-01 or 2008-53
		
		if (result.compareTo(week)<0) {
			// then we might have hit the bug mentioned above
			// calculate manually
			try{
				StringTokenizer st = new StringTokenizer(week, "-");
				Integer year = Integer.parseInt(st.nextToken());
				Integer w = Integer.parseInt(st.nextToken());
				if (result.equals(year+"-1")) {
					// then it looks like we need to switch to the next year already
					return (year+1)+"-"+1;
				}
				if (w==51) {
					return year+"-"+52;
				} else if (w==52) {
					return year+"-"+53;
				} else if (w>=53) {
					return (year+1)+"-"+0;
				}
			} catch(NumberFormatException nfe) {
				log_.warn("nextWeek: Got a NumberFormatException: "+nfe, nfe);
				throw new ParseException("Got a NumberFormatException, rethrowing", 0);
			}
		} else if (result.equals(week)) {
			// daylight saving
			d = new Date(d.getTime() + 1*60*60*1000);
			result = sdf.format(d);
		}
		
		return result;
	}

	private boolean isNextWeek(String week, String nextWeek) throws ParseException {
		return nextWeek(week).equals(nextWeek);
	}
}
