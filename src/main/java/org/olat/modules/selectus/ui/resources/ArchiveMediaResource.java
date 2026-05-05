/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.resources;

import static org.olat.modules.selectus.ui.RecruitingHelper.getPositionDerivedFilename;
import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldOutputStream;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.FOPTableExport;
import org.olat.modules.selectus.ui.components.ExportTableDataModel;

/**
 * 
 * Initial date: 20.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ArchiveMediaResource.class);

	private final Identity downloader;
	private final Position position;
	private final Locale locale;
	private final Translator translator;
	private final ExportTableDataModel<?> rejectionDataModel;
	private final ExportTableDataModel<?> applicationPdfModel;
	private final ExportTableDataModel<?> applicationExcelModel;
	
	public ArchiveMediaResource(Identity downloader, Position position,
			ExportTableDataModel<?> rejectionDataModel, ExportTableDataModel<?> applicationPdfModel,
			ExportTableDataModel<?> applicationExcelModel, Translator translator, Locale locale) {
		this.downloader = downloader;
		this.position = position;
		this.applicationPdfModel = applicationPdfModel;
		this.rejectionDataModel = rejectionDataModel;
		this.applicationExcelModel = applicationExcelModel;
		this.locale = locale;
		this.translator = translator;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String optionalFilename = getPositionDerivedFilename(position, locale);
		String derivedFilename = normalizeFilename(optionalFilename);
		
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + derivedFilename + "_archive.zip");			
		hres.setHeader("Content-Description", derivedFilename + "_archive.zip");

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			//rejection log
			zout.putNextEntry(new ZipEntry(derivedFilename + "_mailcenterlog.pdf"));
			zout.write(new FOPTableExport().exportAsByteArray(downloader, position, rejectionDataModel, "rejection_log.xslt", locale));
			zout.closeEntry();
			
			//application list pdf
			zout.putNextEntry(new ZipEntry(derivedFilename + "_apps.pdf"));
			zout.write(new FOPTableExport().exportAsByteArray(downloader, position, applicationPdfModel, "applications_staff.xslt", locale));
			zout.closeEntry();

			//application list Excel
			zout.putNextEntry(new ZipEntry(derivedFilename + "_applications.xls"));
			outputApplicationListExcel(zout);
			zout.closeEntry();
			
			zout.flush();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void outputApplicationListExcel(ZipOutputStream zout) {
		try(OutputStream sout = new ShieldOutputStream(zout)) {
			ExcelFlexiTableResource resource = new ExcelFlexiTableResource("Applications", applicationExcelModel, translator);
			resource.generate(sout);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}