package org.olat.course.statistic.export;

import java.io.File;
import java.util.Date;

/**
 * An ICourseLogExporter is capable of exporting the course log file.
 * <p>
 * The idea is that the export method can be called concurrently by 
 * multiple threads and that the implementor of this class takes care
 * of any synchronization issue.
 * <p>
 * There is a default implementation of the ICourseLogExporter which
 * works with the default database - this default implementation is used
 * if no other courseLogExporter is defined via spring.
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
public interface ICourseLogExporter {

	/**
	 * Export the course log with the given resourceableId, starting from the
	 * given begin date until the given end date.
	 * <p>
	 * Further you can specify whether you want resourceAdminActions to be
	 * exported (or the opposite).
	 * @param outFile the file (to be overwritten if already exists) where the output should be stored
	 * @param resourceableId
	 * @param begin
	 * @param end
	 * @param resourceAdminAction
	 * @return
	 */
	public void exportCourseLog(File outFile, String charset, Long resourceableId, Date begin, Date end, boolean resourceAdminAction, boolean anonymize);
	
}
