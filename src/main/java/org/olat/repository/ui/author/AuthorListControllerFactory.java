package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;

/**
 * @author Martin Schraner
 */
public interface AuthorListControllerFactory {

	AuthorListController create(UserRequest ureq,
								WindowControl wControl,
								String i18nName,
								SearchAuthorRepositoryEntryViewParams searchParams,
								boolean withSearch,
								boolean withClosedFilter);

}
