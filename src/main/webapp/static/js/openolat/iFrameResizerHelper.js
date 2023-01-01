
var debugIFRH = false;

function registerIFrame(iFrameId) {
	// Activate the iFrameResizer script for the iFrame.
	jQuery("#" + iFrameId).iFrameResize({
		checkOrigin: false,
		warningTimeout: 0,
		scrolling: true,
		initCallback: function(iframe) {
			if (debugIFRH) console.log("iFrame %s registered.", iFrameId);
			iframe.contentDocument.body.style["overflow-y"] = "hidden";
			iframe.contentDocument.body.style["overflow-x"] = "auto";
		},
		resizedCallback: function(iframe) {
			// nothing to do
		}
	});
}

function observeIFrameForDeregistration(iFrameId) {
	// iFrameResizer prints ugly warnings when a registered iFrame is not present any more.
	// So use the close() method to deregister the iFrame from iFrameResizer, when it was
	// removed by the OpenOLAT framework.
	// The MutationObserver was these day the best approach to check whether the iFrame was
	// removed.
	new MutationObserver(function(mutations, observer) {
		if (debugIFRH) console.log("Observer for iFrame %s observed some mutations.", iFrameId);
		mutations.forEach(function(mutation) {
			var removedNodes = mutation.removedNodes;
			if (removedNodes !== null) {
				jQuery( removedNodes ).each(function() {
					var oldIFrame = jQuery( this ).find("#" + iFrameId);
					if (oldIFrame.length === 1) {
						oldIFrame[0].iFrameResizer.close();
						if (debugIFRH) console.log("iFrame %s deregistered.", iFrameId);
						observer.disconnect();
						if (debugIFRH) console.log("Observer for iFrame %s disconnected.", iFrameId);
						
						// Check if a new iFrame with the same ID was added.
						// The iFrameResizer.close() closed this iFrame as well.
						// So reregister the iFrame in iFrameResizer.
						var newiFrame = jQuery("#" + iFrameId);
						if (newiFrame.length === 1) {
							if (debugIFRH) console.log("iFrame %s still on page.", iFrameId);
							registerIFrame(iFrameId);
							if (debugIFRH) console.log("iFrame %s reregistered.", iFrameId);
						}
					}
				});
			}
		});
	 }).observe(
		document.querySelector("body"),
		{childList: true, subtree: true}
	);
	if (debugIFRH) console.log("Observer for iFrame %s started.", iFrameId);
}

function doRegisterIFrameAndObserve(iFrameId) {
	registerIFrame(iFrameId);
	observeIFrameForDeregistration(iFrameId);
}

function registerIFrameAndObserve(iFrameId, debug) {
	if (debug === true) {
		debugIFRH = true;
	} else {
		debugIFRH = false;
	}
	doRegisterIFrameAndObserve(iFrameId);
}

