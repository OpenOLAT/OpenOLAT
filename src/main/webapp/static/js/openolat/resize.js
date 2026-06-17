/**
 * Method to make the center content larger if it contains an element that does not have enough 
 */
function o_adjustContentHeightForAbsoluteElement(itemDomSelector) {
	try {
		let itemsDom = jQuery(itemDomSelector);
		if(itemsDom.length == 0) {
			// Element not found in DOM
			return;
		}
		itemsDom = jQuery(itemsDom[0]);
		let mainDom = itemsDom.closest('#o_main_center_content_inner');
		if(mainDom == null) {
			// Not within center column, nothing to adjust
			return;
		}
		// Current available height
		mainDom = jQuery(mainDom);
		let mainOffsetTop = 0;
		const mainOffset = mainDom.offset();
		if(mainOffset) {
			mainOffsetTop = mainOffset.top;
		}
		const mainHeight = mainDom.outerHeight(true);
		const availableHeight = mainOffsetTop + mainHeight;

		// Calculate minimum required height based on the position of the previous DOM element
		// (e.g. the pull-down button). Absolute positioned element have not offset
		const prevDom = itemsDom.prev();
		if (prevDom.length == 0) {
			// No previous element, don't know what to do
			return;
		}
		const prevOffset = prevDom.offset();
		const prevHeight = prevDom.outerHeight(true);
		const itemsHeight = itemsDom.outerHeight(true);
		const requiredHeight = prevOffset.top + prevHeight + itemsHeight;
		// Check if entire element fits into main element, if not enlarge
		const missingHeight = (requiredHeight - availableHeight);
		if (missingHeight > 0) {
			const newHeight = (mainHeight + missingHeight) + 'px';
			mainDom.css('min-height', newHeight);
		}			
	} catch (e) {
		if(window.console)	console.log(e);
	}
}