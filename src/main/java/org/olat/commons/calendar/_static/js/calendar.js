function o_adjustCalendarHeight(gridId, dayheight) {
	// adjust the given iframe to use as much height as possible
	// (fg)
	var calgrid = $(gridId);
	if (calgrid) {
		
		// check for calendar config, subtract if available
		var calConfigHeight = 0;
		// check if config is within calendar container. If yes, it is rendered below
		// the calendar and thus the cal config real eastate must be subtracted as well
		// to calculate the remaining space for the calender itself.
		if (o_containsElement("o_cal_config_below_cal_container", "o_cal_config", 5)) {
			calConfigHeight = Element.getHeight("o_cal_config");		
		}
		
		// height of all day events..
		var allDayHeight = 0;
		var allDayElem = $("o_cal_wv_daylong");
		if (allDayElem != null && allDayElem != 'undefined') {
			allDayHeight = Element.getHeight("o_cal_wv_daylong");
		}
		// height of header
		var headerHeight = 0;
		var headerElem = $("o_cal_wv_header");
		if (headerElem != null && headerElem != 'undefined') {
			headerHeight = Element.getHeight("o_cal_wv_header");
		}
		
		// now calculate remaining space for calendar
		// use 3/4 of the browser viewport as displayable size and subtract other calendar stuff
		var contentHeight = b_viewportHeight() / 4 * 3 - allDayHeight - headerHeight - calConfigHeight;
		if (contentHeight < dayheight) {
			if (contentHeight < 200) {
				contentHeight=200; // never smaller than 200px, makes no sense 
			}
			calgrid.setStyle({height: contentHeight + 'px'});
		} else {
			calgrid.setStyle({height: dayheight + 'px'});
		}
	} else {
		B_AjaxLogger.logDebug("No calendar grid found","calendar.js::o_adjustCalendarHeight()");
	}
}

function o_positionScrollbar(id, pos) {
	// set the scrollbar of the given DOM element to the given position
	// (fg)
	var scroll = document.getElementById(id);
	if (scroll) {
		// used by safari to initialize the scrollbar positioning
		var initialized = scroll.scrollTop; 
		// not position scrollbar
		scroll.scrollTop = pos;
	}
}

function o_containsElement(parentElementID, containedElementID, nestingLevels) {
	// checks if the given contained DOM element is a child or grand-child of 
	// the given parent DOM element. The sarch will be limited by the nesting
	// levels.
	// Returns true if element is a child element within the nesting level, 
	// false otherwhise.
	// (fg)
		
	var parentElem = document.getElementById(parentElementID);
	var containedElem = document.getElementById(containedElementID);
	// exit when the given elements do not exist at all!
	if (!parentElem || !containedElem) return false;
	
	var visitingParent = containedElem.parentNode;
	var i = 0;
	while (i<nestingLevels) {
		if (visitingParent == null) {
			return false;
		} if (visitingParent.id == parentElem.id) {
			return true;
		}
		i++;
		visitingParent = visitingParent.parentNode;
	}
	return false;
}

function o_init_event_tooltips() {
	// init the Ext.tooltip for each event
	// (gw)
	// 1) for day events
	$$(".o_cal_wv_devent").each(o_init_event_tooltip);
	// 2) for normal events
	$$(".o_cal_wv_event").each(o_init_event_tooltip);
}

function o_mark_event_box_overflow() {
	// check if the event boxes can display everything, if not add a ... at the bottom right corner
	$$(".o_cal_wv_event").each(function(item) {
		if (item.offsetHeight + 5 < item.scrollHeight) {
			item.insert("<div class='o_cal_wv_event_overflow' style='top: " + (item.offsetHeight-15) + "px;'>...</div>");
		}
	});
	$$(".o_cal_wv_devent_content").each(function(item) {
		if (item.offsetHeight + 5 < item.scrollHeight) {
			item.insert("<div class='o_cal_wv_event_overflow' style='top: " + (item.offsetHeight-5) + "px;'>...</div>");
		}
	});
}

function o_init_event_tooltip(item) {
	var tooltip = item.select(".o_cal_wv_event_tooltip").first();
	var title = tooltip.select(".o_cal_time").first();
	var content = tooltip.select(".o_cal_wv_event_tooltip_content").first();
	var renderScope = "o_cal_wv_grid";
	if (Ext.get(tooltip).hasClass("o_cal_allday")) {
		renderScope = "o_cal_wv";
	}
	var tt = new Ext.ToolTip({
        target: item,
        anchor: 'left',
        anchorToTarget : true,
        draggable: true,
        autoShow  : true,
        autoHide: false,
        showDelay: 300,
        closable: true,
        title: title.innerHTML,
        contentEl: content,
        renderTo: Ext.get(renderScope)
	});
	// Add reference to tooltip to array and add listener so we can 
	// auto close tips when a new tip is opened
	o_cal_tt.push(tt);
	tt.addListener('beforeshow', function(curItem){
		o_cal_tt.each(function(tip){
			if (tip !=  curItem) tip.hide();
		});
	});
}
// Array to keep references to current calendar tooltips
var o_cal_tt = new Array();
