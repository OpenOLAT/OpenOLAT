/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.report;

import java.io.OutputStream;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 8 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReportGenerator {
	
	public String getFilename(List<Position> positions, Identity identity, Translator translator);

	public void generateReport(List<Position> positions, Identity identity, Translator translator, OutputStream out);

}
