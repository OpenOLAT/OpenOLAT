package org.olat.catalog.ui;

import org.olat.catalog.CatalogEntry;
import org.olat.core.gui.control.Controller;

/**
 * 
 * Initial date: 22.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CatalogMoveController extends Controller {
	
	public CatalogEntry getMovedCatalogEntry();

}
