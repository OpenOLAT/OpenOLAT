/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.report;

import java.io.OutputStream;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReportGeneratorResource extends DefaultMediaResource {

	private static final Logger log = Tracing.createLoggerFor(ReportGeneratorResource.class);
	
	private final Identity doer;
	private final Translator translator;
	private final List<Position> positions;
	private final ReportGenerator generator;
	
	public ReportGeneratorResource(ReportGenerator generator, List<Position> positions,
			Identity doer, Translator translator) {
		this.doer = doer;
		this.positions = positions;
		this.translator = translator;
		this.generator = generator;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	@Override
	public final void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		String filename = generator.getFilename(positions, doer, translator);
		String urlEncodedFilename = StringHelper.urlEncodeUTF8(filename) + ".xlsx";
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedFilename);			
		hres.setHeader("Content-Description", urlEncodedFilename);
		
		try(OutputStream out = hres.getOutputStream()) {
			generator.generateReport(positions, doer, translator, out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
