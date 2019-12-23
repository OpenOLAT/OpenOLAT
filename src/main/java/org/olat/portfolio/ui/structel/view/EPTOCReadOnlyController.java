/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.portfolio.ui.structel.view;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.structel.EPStructureEvent;

/**
 * Description:<br>
 * presents a static TOC with links to elements
 * 
 * <P>
 * Initial Date: 25.10.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTOCReadOnlyController extends BasicController {

	private static final String CONST_FOR_VC_STYLE_STRUCT = "struct"; // used to
																		// style
																		// in
																		// velocity
	private static final String CONST_FOR_VC_STYLE_PAGE = "page"; // used to
																	// style in
																	// velocity
	private static final String LINK_CMD_OPEN_ARTEFACT = "oArtefact";
	private static final String LINK_CMD_OPEN_STRUCT = "oStruct";
	private static final String LINK_CMD_OPEN_COMMENTS = "oComments";
	private VelocityContainer vC;
	private EPFrontendManager ePFMgr;
	private List<UserCommentsCount> commentCounts;
	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private PortfolioStructure map;
	private EPSecurityCallback secCallback;
	private Link artOnOffLink;

	private boolean displayArtefactsInTOC = false;

	public EPTOCReadOnlyController(UserRequest ureq, WindowControl wControl, PortfolioStructure map, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.map = map;
		this.secCallback = secCallback;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");

		commentCounts = CoreSpringFactory.getImpl(UserCommentsDAO.class).countCommentsWithSubPath(map.getOlatResource(), null);
		 

		vC = createVelocityContainer("toc");
		// have a toggle to show with/without artefacts
		artOnOffLink = LinkFactory.createButtonSmall("artOnOffLink", vC, this);
		artOnOffLink.setCustomDisplayText(translate("artOnOffLink." + !displayArtefactsInTOC));
		putInitialPanel(vC);
		refreshTOC(ureq);
	}

	public void refreshTOC(UserRequest ureq) {
		// do recursively
		int level = 0;
		List<TOCElement> tocList = new ArrayList<>();
		buildTOCModel(map, tocList, level);

		vC.contextPut("tocList", tocList);

		if (secCallback.canCommentAndRate()) {
			removeAsListenerAndDispose(commentsAndRatingCtr);
			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback callback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
			commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), map.getOlatResource(), null, callback, null, true, true, true);
			listenTo(commentsAndRatingCtr);
			vC.put("commentCtrl", commentsAndRatingCtr.getInitialComponent());
		}

	}

	/**
	 * builds the tocList recursively containing artefacts, pages and
	 * struct-Elements
	 * 
	 * @param pStruct
	 * @param tocList
	 *            list with TOCElement's to use in velocity
	 * @param level
	 * @param withArtefacts
	 *            set false, to skip artefacts
	 */
	private void buildTOCModel(PortfolioStructure pStruct, List<TOCElement> tocList, int level) {
		level++;

		if (displayArtefactsInTOC) {
			List<AbstractArtefact> artList = ePFMgr.getArtefacts(pStruct);
			if (artList != null && artList.size() != 0) {
				for (AbstractArtefact artefact : artList) {
					String key = String.valueOf(artefact.getKey());
					String title = StringHelper.escapeHtml(artefact.getTitle());

					Link iconLink = LinkFactory.createCustomLink("arte_" + key, LINK_CMD_OPEN_ARTEFACT, "", Link.NONTRANSLATED, vC, this);
					iconLink.setIconRightCSS("o_icon o_icon_start");
					iconLink.setUserObject(pStruct);

					Link titleLink = LinkFactory.createCustomLink("arte_t_" + key, LINK_CMD_OPEN_ARTEFACT, title, Link.NONTRANSLATED, vC, this);
					titleLink.setUserObject(pStruct);

					TOCElement actualTOCEl = new TOCElement(level, "artefact", titleLink, iconLink, null, null);
					tocList.add(actualTOCEl);
				}
			}
		}

		List<PortfolioStructure> childs = ePFMgr.loadStructureChildren(pStruct);
		if (childs != null && childs.size() != 0) {
			for (PortfolioStructure portfolioStructure : childs) {
				String type = "";
				if (portfolioStructure instanceof EPPage) {
					type = CONST_FOR_VC_STYLE_PAGE;
				} else {
					// a structure element
					type = CONST_FOR_VC_STYLE_STRUCT;
				}

				String key = String.valueOf(portfolioStructure.getKey());
				String title = StringHelper.escapeHtml(portfolioStructure.getTitle());

				Link iconLink = LinkFactory.createCustomLink("portstruct" + key, LINK_CMD_OPEN_STRUCT, "", Link.NONTRANSLATED, vC, this);
				iconLink.setIconRightCSS("o_icon o_icon_start");
				iconLink.setUserObject(portfolioStructure);

				Link titleLink = LinkFactory.createCustomLink("portstruct_t_" + key, LINK_CMD_OPEN_STRUCT, title, Link.NONTRANSLATED, vC, this);
				titleLink.setUserObject(portfolioStructure);

				Link commentLink = null;
				if (portfolioStructure instanceof EPPage && secCallback.canCommentAndRate()) {
					UserCommentsCount comments = getUserCommentsCount(portfolioStructure);
					String count = comments == null ? "0" : comments.getCount().toString();
					String label = translate("commentLink", new String[] { count });
					commentLink = LinkFactory.createCustomLink("commentLink" + key, LINK_CMD_OPEN_COMMENTS, label, Link.NONTRANSLATED, vC, this);
					commentLink.setIconLeftCSS("o_icon o_icon_comments");
					commentLink.setUserObject(portfolioStructure);
				}

				// prefetch children to keep reference on them
				List<TOCElement> tocChildList = new ArrayList<>();
				buildTOCModel(portfolioStructure, tocChildList, level);
				TOCElement actualTOCEl = new TOCElement(level, type, titleLink, iconLink, commentLink, tocChildList);
				tocList.add(actualTOCEl);

				if (tocChildList.size() != 0) {
					tocList.addAll(tocChildList);
				}
			}
		}
	}

	protected UserCommentsCount getUserCommentsCount(PortfolioStructure portfolioStructure) {
		if (commentCounts == null || commentCounts.isEmpty())
			return null;

		String keyStr = portfolioStructure.getKey().toString();
		for (UserCommentsCount commentCount : commentCounts) {
			if (keyStr.equals(commentCount.getSubPath())) {
				return commentCount;
			}
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == artOnOffLink) {
			displayArtefactsInTOC = !displayArtefactsInTOC;
			artOnOffLink.setCustomDisplayText(translate("artOnOffLink." + !displayArtefactsInTOC));
			refreshTOC(ureq);
		} else if (source instanceof Link) {
			// could be a TOC-Link
			Link link = (Link) source;
			String cmd = link.getCommand();
			PortfolioStructure parentStruct = (PortfolioStructure) link.getUserObject();
			if (cmd.equals(LINK_CMD_OPEN_STRUCT)) {
				fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT, parentStruct));
			} else if (cmd.equals(LINK_CMD_OPEN_ARTEFACT)) {
				// open the parent structure
				fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT, parentStruct));
			} else if (cmd.equals(LINK_CMD_OPEN_COMMENTS)) {
				fireEvent(ureq, new EPStructureEvent(EPStructureEvent.SELECT_WITH_COMMENTS, parentStruct));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}
}
