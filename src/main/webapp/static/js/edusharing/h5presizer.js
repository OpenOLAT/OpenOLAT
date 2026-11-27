const OOH5pResizer = {
	
	/**
	 * H5P content in an edu-sharing iframe reports its height with the H5P message
	 * protocol (hello, prepareResize, resize). Only iframes inside an edu-sharing
	 * container are resized. After a resize of the window, the content is asked
	 * to report its height again.
	 */
	resize: function(e) {
		const data = e.data;
		if (!data || data.context !== "h5p") {
			return;
		}
		const iframe = Array.from(document.querySelectorAll(".o_edusharing_container iframe"))
			.find(frame => frame.contentWindow === e.source);
		if (!iframe) {
			return;
		}
		const respond = action => e.source.postMessage({ context: "h5p", action: action }, e.origin);
		if (data.action === "hello") {
			iframe.style.width = "100%";
			if (!iframe.oH5pWindowResize) {
				iframe.oH5pWindowResize = function() {
					if (iframe.isConnected && iframe.contentWindow) {
						respond("resize");
					} else {
						window.removeEventListener("resize", iframe.oH5pWindowResize);
					}
				};
				window.addEventListener("resize", iframe.oH5pWindowResize);
			}
			respond("hello");
		} else if (data.action === "prepareResize") {
			if (iframe.offsetHeight !== data.clientHeight || data.scrollHeight !== data.clientHeight) {
				iframe.style.height = data.clientHeight + "px";
				respond("resizePrepared");
			}
		} else if (data.action === "resize") {
			iframe.style.height = data.scrollHeight + "px";
		}
	},
	
	/**
	 * H5P content which is loaded before the listener starts the protocol after a ready message.
	 */
	notifyReady: function(container) {
		container.querySelectorAll("iframe").forEach(function(iframe) {
			if (iframe.src.indexOf("h5p") > -1) {
				const ready = function() {
					iframe.contentWindow.postMessage({ context: "h5p", action: "ready" }, new URL(iframe.src).origin);
				};
				iframe.addEventListener("load", ready);
				if (iframe.contentWindow) {
					ready();
				}
			}
		});
	}
};

// o_info is missing in an iFrame without content domain, h5presizer.js is only loaded there if edu-sharing is enabled
if (typeof o_info === "undefined") {
	window.addEventListener("message", OOH5pResizer.resize);
}
