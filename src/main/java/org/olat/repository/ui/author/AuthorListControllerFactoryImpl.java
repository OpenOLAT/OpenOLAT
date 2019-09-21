package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class AuthorListControllerFactoryImpl implements AuthorListControllerFactory {

	@Override
	public AuthorListController create(UserRequest ureq,
									   WindowControl wControl,
									   String i18nName,
									   SearchAuthorRepositoryEntryViewParams searchParams,
									   boolean withSearch,
									   boolean withClosedFilter) {

		return new AuthorListController(
				ureq,
				wControl,
				i18nName,
				searchParams,
				withSearch,
				withClosedFilter);
	}
}
