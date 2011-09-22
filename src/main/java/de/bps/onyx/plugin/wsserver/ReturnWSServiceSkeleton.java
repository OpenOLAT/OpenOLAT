/**
 * ReturnWSServiceSkeleton.java
 *
 * This file was auto-generated from WSDL by the Apache Axis2 version: 1.5 Built
 * on : Apr 30, 2009 (06:07:24 EDT)
 */
package de.bps.onyx.plugin.wsserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.OnyxResultManager;

/**
 * ReturnWSServiceSkeleton java skeleton for the axisService
 */
public class ReturnWSServiceSkeleton {

	/**
	 * Auto generated method signature
	 *
	 * @param saveResultLocal
	 */
	
	private final static OLog LOGGER = Tracing.createLoggerFor(ReturnWSServiceSkeleton.class);

	public de.bps.onyx.plugin.wsserver.SaveResultLocalResponse saveResultLocal(
			de.bps.onyx.plugin.wsserver.SaveResultLocal saveResultLocal) {

		QTIResultSet qtiResultSet = OnyxResultManager.getResultSet(Long.parseLong(saveResultLocal.getUniqueId()));
		String resultfile = saveResultLocal.getResultLocalFile();
		if (resultfile == null) {
			LOGGER.error("unauthorized request: saveResultLocal.getUniqueId()=" + saveResultLocal.getUniqueId()
							+ " saveResultLocal.getResultLocalFile()=" + saveResultLocal.getResultLocalFile());
			throw new java.lang.UnsupportedOperationException("unauthorized request, this event will be logged");
		} else {
			OnyxResultManager.persistOnyxResults(qtiResultSet, resultfile);
		}

		return new de.bps.onyx.plugin.wsserver.SaveResultLocalResponse();
	}

	/**
	 * Auto generated method signature
	 *
	 * @param saveResult
	 */

	public de.bps.onyx.plugin.wsserver.SaveResultResponse saveResult(de.bps.onyx.plugin.wsserver.SaveResult saveResult) {
		File temp = null;
				try {
					// file.createtempfile() is not correctly interpreted as an archive even if the isArchive() method says so
					temp = new File(System.getProperty("java.io.tmpdir"),
							java.io.File.separatorChar + (saveResult).hashCode() + "_"
							+ new Date().getTime() + ".zip");
					FileUtils.copy(saveResult.getResultFile().getInputStream(), new FileOutputStream(temp));
		
					QTIResultSet qtiResultSet = OnyxResultManager.getResultSet(Long.parseLong(saveResult.getUniqueId()));
		
					if (temp == null || temp.getAbsolutePath() == null) {
						LOGGER.error("unauthorized request: saveResultLocal.getUniqueId()=" + saveResult.getUniqueId()
								+ " saveResultLocal.getResultLocalFile()=" + temp);
						throw new java.lang.UnsupportedOperationException("unauthorized request, this event will be logged");
					} else {
						OnyxResultManager.persistOnyxResults(qtiResultSet, temp.getAbsolutePath());
						//temporary-file will be deleted in the OnyxResultManager
					}
		
				} catch (FileNotFoundException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
		
				return new de.bps.onyx.plugin.wsserver.SaveResultResponse();

	}

}
