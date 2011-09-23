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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course.statistic.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;

/**
 * ICourseLogExporter used for the case where a separate DB
 * should be used to retrieve the o_loggingtable.
 * <p>
 * This would be a non-standard situation specifically for a mysql/stored procedure setup
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
public class UZHStoredProcedureLogExporter implements ICourseLogExporter {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(UZHStoredProcedureLogExporter.class);
	
	private JdbcTemplate jdbcTemplate_;
	
	private String header_ = "header not configured";
	
	public UZHStoredProcedureLogExporter() {
		// this empty constructor is ok - instantiated via spring
	}
	
	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate_ = jdbcTemplate;
	}
	
	/** set via spring **/
	public void setHeader(String header) {
		header_ = header;
	}

	/**
	 * @TODO: charSet is currently ignored!!!!!
	 * @see org.olat.course.statistic.export.ICourseLogExporter#exportCourseLog(java.io.File, java.lang.String, java.lang.Long, java.util.Date, java.util.Date, boolean)
	 */
	public void exportCourseLog(final File outFile, String charSet, final Long resourceableId, final Date begin, final Date end, final boolean resourceAdminAction, final boolean anonymize) {
		final long startTime = System.currentTimeMillis();
		log_.info("exportCourseLog: BEGIN outFile="+outFile+", charSet="+charSet+", resourceableId="+resourceableId+", begin="+begin+", end="+end+", resourceAdminAction="+resourceAdminAction+", anonymize="+anonymize);
		try {
			if (!outFile.exists()) {
				if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
					throw new IllegalArgumentException("Cannot create parent of OutFile "+outFile.getAbsolutePath());
				}
				if (!outFile.createNewFile()) {
					throw new IllegalArgumentException("Cannot create outFile "+outFile.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Cannot create outFile "+outFile.getAbsolutePath());
		}
		if (!outFile.delete()) {
			throw new IllegalStateException("Could not delete temporary outfile "+outFile.getAbsolutePath());
		}
		
		// try to make sure the database can write into this directory
		if (!outFile.getParentFile().setWritable(true, false)) {
			log_.warn("exportCourseLog: COULD NOT SET DIR TO WRITEABLE: "+outFile.getParent());
		}
		
		BufferedInputStream fis = null;
		BufferedOutputStream fos = null;
		try{
			List<SqlParameter> emptyOutParams = new LinkedList<SqlParameter>();
			
			// we ignore the result of the stored procedure
			jdbcTemplate_.call(new CallableStatementCreator() {
	
				@Override
				public CallableStatement createCallableStatement(Connection con) throws SQLException {
					CallableStatement cs = con.prepareCall("call o_logging_export(?,?,?,?,?,?)");
					cs.setString(1, outFile.getAbsolutePath());
					cs.setBoolean(2, resourceAdminAction);
					cs.setString(3, Long.toString(resourceableId));
					cs.setBoolean(4, anonymize);
					cs.setTimestamp(5, begin==null ? null : new Timestamp(begin.getTime()));
					cs.setTimestamp(6, end==null ? null : new Timestamp(end.getTime()));
					UZHStoredProcedureLogExporter.log_.info("exportCourseLog: executing stored procedure right about now");
					return cs;
				}
				
			}, emptyOutParams);
			
			log_.info("exportCourseLog: adding header... mysql outfile="+outFile.getAbsolutePath());
			fis = new BufferedInputStream(new FileInputStream(outFile));
			File tmpOutFile = new File(outFile.getParent(), "tmp_"+outFile.getName());
			log_.info("exportCourseLog: tmpoutfile="+tmpOutFile.getAbsolutePath());
			
			fos = FileUtils.getBos(tmpOutFile);
			
			fos.write((header_+System.getProperty("line.separator")).getBytes(StringHelper.check4xMacRoman(charSet)));
			
			FileUtils.cpio(fis, fos, "exportCourseLogUZH");
			
			fos.flush();
			fos.close();
			fis.close();

			outFile.delete();
			tmpOutFile.renameTo(outFile);
			
			log_.info("exportCourseLog: resulting outfile="+outFile.getAbsolutePath());
		} catch(RuntimeException e) {
			String details = "outFile="+outFile.getAbsolutePath()+", resAdminAction="+resourceAdminAction+", resourceableId="+resourceableId+", anonymize="+anonymize+", begin="+(begin==null ? null : begin.getTime())+", end="+(end==null ? null : end.getTime());
			log_.error("exportCourseLog: runtime exception. Details: "+details,e);
		} catch(Error er) {
			String details = "outFile="+outFile.getAbsolutePath()+", resAdminAction="+resourceAdminAction+", resourceableId="+resourceableId+", anonymize="+anonymize+", begin="+(begin==null ? null : begin.getTime())+", end="+(end==null ? null : end.getTime());
			log_.error("exportCourseLog: error. Details: "+details,er);
		} catch (FileNotFoundException e) {
			String details = "outFile="+outFile.getAbsolutePath()+", resAdminAction="+resourceAdminAction+", resourceableId="+resourceableId+", anonymize="+anonymize+", begin="+(begin==null ? null : begin.getTime())+", end="+(end==null ? null : end.getTime());
			log_.error("exportCourseLog: FileNotFoundException. Details: "+details,e);
		} catch (IOException e) {
			String details = "outFile="+outFile.getAbsolutePath()+", resAdminAction="+resourceAdminAction+", resourceableId="+resourceableId+", anonymize="+anonymize+", begin="+(begin==null ? null : begin.getTime())+", end="+(end==null ? null : end.getTime());
			log_.error("exportCourseLog: IOException. Details: "+details, e);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("exportCourseLog: END DURATION="+diff);
		}
	}
	
}
