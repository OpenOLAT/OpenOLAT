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
import java.util.StringTokenizer;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;
import org.olat.course.statistic.TotalAwareColumnDescriptor;

/**
 * Implementation of the IStatisticManager for 'weekly' statistic.
 * <p>
 * Note that this class uses SimpleDateFormat's way of calculating the
 * week number. This might (!) differ from what the IStatisticUpdater for weekly
 * calculates via the Database!
 * So if you find such a difference you might have to patch this class accordingly!
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class WeeklyStatisticManager implements IStatisticManager {

	/** the logging object used in this class **/
	private static final Logger log = Tracing.createLoggerFor(WeeklyStatisticManager.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-ww");
	
	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey) {
		String q = "select businessPath,week,value from weeklystat sv where sv.resId=:resId";
		List<Object[]> raw = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(q, Object[].class)
				.setParameter("resId", courseRepositoryEntryKey)
				.getResultList();
		return new StatisticResult(course, raw);
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

	@Override
	public StatisticResult generateStatisticResult(UserRequest ureq, ICourse course, long courseRepositoryEntryKey, Date fromDate, Date toDate) {
		if (fromDate==null && toDate==null) {
			// no restrictions, return the defaults
			StatisticResult statisticResult = generateStatisticResult(ureq, course, courseRepositoryEntryKey);
			fillGapsInColumnHeaders(statisticResult);
			return statisticResult;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select businessPath, week, value from org.olat.course.statistic.weekly.WeeklyStat sv where sv.resId=:resId");
		//concat(year(creationdate),'-',week(creationdate)) week
		if (fromDate != null) {
			sb.append(" and (week=:fromDate or week>=:fromDate) ");
		}
		if (toDate!=null) {
			sb.append(" and (week=:toDate or week<=:toDate) ");
		}
		TypedQuery<Object[]> dbQuery = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resId", courseRepositoryEntryKey);
		StringBuilder infoMsg = new StringBuilder(256);
		if (fromDate!=null) {
			String fromDateStr = getYear(fromDate) + "-" + getWeek(fromDate);
			infoMsg.append("from date: "+fromDateStr);
			dbQuery.setParameter("fromDate", fromDateStr);
		}
		if (toDate!=null) {
			String toDateStr = getYear(toDate) + "-" + getWeek(toDate);
			if (infoMsg!=null) {
				infoMsg.append(", ");
			}
			infoMsg.append("to date: "+toDateStr);
			dbQuery.setParameter("toDate", toDateStr);
		}
		
		log.info("generateStatisticResult: Searching with params "+infoMsg.toString());
		
		StatisticResult statisticResult = new StatisticResult(course, dbQuery.getResultList());
		fillGapsInColumnHeaders(statisticResult);
		return statisticResult;
	}

	private String getWeek(Date date) {
		SimpleDateFormat df = new SimpleDateFormat(); 
		df.applyPattern("ww");
		return df.format(date);
	}

	private String getYear(Date date) {
		SimpleDateFormat df = new SimpleDateFormat(); 
		df.applyPattern("yyyy");
		return df.format(date);
	}

	/** fill any gaps in the column headers between the first and the last days **/
	private void fillGapsInColumnHeaders(StatisticResult statisticResult) {
		if (statisticResult==null) {
			throw new IllegalArgumentException("statisticResult must not be null");
		}
		List<String> columnHeaders = statisticResult.getColumnHeaders();
		List<String> resultingColumnHeaders = fillGapsInColumnHeaders(columnHeaders);
		if (resultingColumnHeaders!=null) {
			statisticResult.setColumnHeaders(resultingColumnHeaders);
		}
	}
	
	List<String> fillGapsInColumnHeaders(List<String> columnHeaders) {
		if (columnHeaders==null) {
			// nothing to be done
			return null;
		}
		if (columnHeaders.size()<=1) {
			// if the resulting set one or less, don't bother
			return null;
		}
		try{
			String firstWeek = columnHeaders.get(0);
			String previousWeek = firstWeek;
			log.debug("fillGapsInColumnHeaders: starting...");
			log.debug("fillGapsInColumnHeaders: columnHeaders.size()="+columnHeaders.size());
			log.debug("fillGapsInColumnHeaders: columnHeaders="+columnHeaders);
			if (columnHeaders.size()>1) {
				Date previousWeekDate = sdf.parse(previousWeek);
				String lastWeek = columnHeaders.get(columnHeaders.size()-1);
				Date lastWeekDate = sdf.parse(lastWeek);
				if (previousWeekDate==null || lastWeekDate==null) {
					log.warn("fillGapsInColumnHeaders: can't get date from weeks: "+previousWeek+"/"+lastWeek);
					return null;
				}
				if (previousWeekDate.compareTo(lastWeekDate)>=1) {
					// that means that we got wrong input params!
					log.warn("fillGapsInColumnHeaders: got a wrongly ordered input, skipping sorting. columnHeaders: "+columnHeaders);
					return null;
				}
			}
			for (int i = 1; i < columnHeaders.size(); i++) {
				if (i>255) {
					// that's probably a bug in the loop - although it is unlikely to occur again (OLAT-5161)
					// we do an emergency stop here
					log.warn("fillGapsInColumnHeaders: stopped at i="+i+", skipped sorting. columnHeaders grew to: "+columnHeaders);
					return null;
				}
				String currWeek = columnHeaders.get(i);
				log.debug("fillGapsInColumnHeaders: columnHeaders["+i+"]: "+currWeek);
				
				if (!isNextWeek(previousWeek, currWeek)) {
					log.debug("fillGapsInColumnHeaders: isNextweek("+previousWeek+","+currWeek+"): false");
					String additionalWeek = nextWeek(previousWeek);
					if (columnHeaders.contains(additionalWeek)) {
						// oups, then we have a bug in our algorithm or what?
						log.warn("fillGapsInColumnHeaders: throwing a ParseException, can't add "+additionalWeek+" to "+columnHeaders);
						throw new ParseException("Can't add "+additionalWeek+" to the list of weeks - it is already there", 0);
					}
					if (sdf.parse(additionalWeek).compareTo(sdf.parse(currWeek))>0) {
						// then we're overshooting
						continue;
					}
					columnHeaders.add(i, additionalWeek);
					previousWeek = additionalWeek;
				} else {
					log.debug("fillGapsInColumnHeaders: isNextweek("+previousWeek+","+currWeek+"): true");
					previousWeek = currWeek;
				}
			}
			log.debug("fillGapsInColumnHeaders: columnHeaders.size()="+columnHeaders.size());
			log.debug("fillGapsInColumnHeaders: columnHeaders="+columnHeaders);
			log.debug("fillGapsInColumnHeaders: done.");
			return columnHeaders;
		} catch(ParseException e) {
			log.warn("fillGapsInColumnHeaders: Got a ParseException while trying to fill gaps. Giving up. ",e);
			return null;
		}
	}

	private String nextWeek(String week) throws ParseException {
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
				log.warn("nextWeek: Got a NumberFormatException: "+nfe, nfe);
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
