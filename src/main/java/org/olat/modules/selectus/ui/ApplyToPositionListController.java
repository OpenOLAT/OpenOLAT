/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;

/**
 * 
 * Initial date: 18.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplyToPositionListController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	@Autowired
	private RecruitingModule recruitingModule;

	public ApplyToPositionListController(UserRequest ureq, WindowControl wControl, List<Position> openPositions) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("apply_positions");
		
		if(recruitingModule.getPositionLocales().length > 1) {
			List<Link> languageLinks = new ArrayList<>();
			for(Locale locale:recruitingModule.getPositionLocales()) {
				String id = "apply." + locale.getLanguage();
				Link localeLink = LinkFactory.createCustomLink(id, "language", id, Link.BUTTON, mainVC, this);
				if(locale.equals(getLocale())) {
					localeLink.setCustomEnabledLinkCSS("active btn btn-default");
				} else {
					localeLink.setCustomEnabledLinkCSS("btn btn-default");
				}
				localeLink.setUserObject(locale);
				languageLinks.add(localeLink);
			}
			mainVC.contextPut("languageLinks", languageLinks);
		}

		loadModel(openPositions);
		putInitialPanel(mainVC);
	}
	
	private void loadModel(List<Position> openPositions) {
		List<PositionWrapper> wrappers = new ArrayList<>();
		for(Position position:openPositions) {
			Link applyLink = LinkFactory.createCustomLink("apply" + wrappers.size(), "apply", "apply", Link.BUTTON, mainVC, this);
			applyLink.setCustomEnabledLinkCSS("btn btn-primary");
			applyLink.setUserObject(position);
			PositionWrapper wrapper = new PositionWrapper(applyLink);
			
			String title = PositionMLHelper.getPositionMLTitle(position, getLocale());
			wrapper.setTitle(title);
			
			String descr = PositionMLHelper.getPositionMLDescription(position, getLocale());
			wrapper.setDescription(descr);
			
			if(recruitingModule.isPositionDepartmentEnabled()) {
				String department = PositionMLHelper.getPositionMLDepartment(position, getLocale());
				wrapper.setDepartement(department);
			}
			if(recruitingModule.isPositionHomepageEnabled()) {
				String homepage = position.getHomepage();
				if(StringHelper.containsNonWhitespace(homepage)) {
					try {
						new URL(homepage).getHost();
						wrapper.setHomepageUrl(homepage);
					} catch(Exception ex) {
						wrapper.setHomepage(homepage);
					}
				}	
			}
			wrappers.add(wrapper);
		}
		mainVC.contextPut("positions", wrappers);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("apply".equals(link.getCommand())) {
				Position position = (Position)link.getUserObject();
				fireEvent(ureq, new SelectPositionEvent(position));
			} else if("language".equals(link.getCommand())){
				Locale newLocale = (Locale)link.getUserObject();
				fireEvent(ureq, new LanguageChangedEvent(newLocale, ureq));
			}
		}
	}
	
	public static class PositionWrapper {
		
		private String title;
		private String description;
		
		private String departement;
		private String homepage;
		private String homepageUrl;
		
		private final Link applyLink;
		
		public PositionWrapper(Link applyLink) {
			this.applyLink = applyLink;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getDepartement() {
			return departement;
		}

		public void setDepartement(String departement) {
			this.departement = departement;
		}
		
		public String getHomepage() {
			return homepage;
		}

		public void setHomepage(String homepage) {
			this.homepage = homepage;
		}
		
		public String getHomepageUrl() {
			return homepageUrl;
		}

		public void setHomepageUrl(String homepageUrl) {
			this.homepageUrl = homepageUrl;
		}

		public String getApplyLinkName() {
			return applyLink.getComponentName();
		}
	}
}
