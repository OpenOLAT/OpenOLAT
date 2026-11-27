
const OOEdusharing = {
		
	start: function() {
		if (typeof o_info !== "undefined" && o_info.edusharing_enabled) {
			window.addEventListener("message", OOH5pResizer.resize);
			OOEdusharing.render();
			jQuery(document).on("oo.dom.replacement.after", OOEdusharing.render);
			OOEdusharing.enableMetadataToggler();
		}
	},
		
	replaceWithSpinner: function(node, width, height) {
		let spinnerHtml = "<div class='BGlossarIgnore' style='";
		if (width > 0) {
			spinnerHtml += "width:" + width + "px;";
		}
		if (height > 0) {
			spinnerHtml += "height:" + height + "px;";
		}
		spinnerHtml += "'>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner1'></div></div>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner2'></div></div>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner3'></div></div>";
		spinnerHtml += "</div>";
		
		const spinner = jQuery(spinnerHtml);
		node.before(spinner);
		node.remove();
		return spinner;
	},

	replaceWithRendered: function(node, identifier, version, width, height, esClass, showLicense, showInfos, isIFrame) {
		let url = o_info.uriprefix.replace("auth", "edusharing") + "render?identifier=" + identifier;
		if (version >= 0) {
			url = url + "&version=" + version;
		}
		if (width > 0) {
			url = url + "&width=" + width;
		}
		if (height) {
			url = url + "&height=" + height;
		}
		
		let containerHtml = "<div class='o_edusharing_container BGlossarIgnore";
		if (typeof esClass != 'undefined') {
			containerHtml += " " + esClass;
		}
		if (isIFrame) {
			containerHtml += " o_in_iframe";
		}
		if ('hide' === showLicense) {
			containerHtml += " o_hide_license";
		}
		if ('hide' === showInfos) {
			containerHtml += " o_hide_infos";
		}
		containerHtml += "'>";
		containerHtml += "</div>";
		
		const container = jQuery(containerHtml);
		
		jQuery.ajax({
			type: "GET",
			url: url,
			dataType : 'html',
			success : function(data){
				const esNode = container.append(data);
				OOEdusharing.replaceMaterialIcons(esNode);
				node.replaceWith(esNode);
				OOH5pResizer.notifyReady(esNode.get(0));
				o_adjustContentHeightForAbsoluteElement('.o_edusharing_container .edusharing_metadata_wrapper');
			},
			error : function(xhr) {
				node.replaceWith(jQuery("<div class='o_warning'></div>").text(xhr.responseText || "edu-sharing not available"));
			}
		})
	},
		
	replaceMaterialIcons: function(esNode) {
		const icons = { info_outline: "o_icon_info", person: "o_icon_user", settings: "o_icon_settings", done: "o_icon_check" };
		esNode.find("i.material-icons").each(function() {
			const icon = jQuery(this);
			icon.removeClass("material-icons").addClass("o_icon " + (icons[icon.text().trim()] || "o_icon_info")).text("");
		});
	},
	
	replace: function(node, isIFrame) {
		const identifier = node.data("es_identifier");
		const version = node.data("es_version");
		const width = node.attr("width");
		const height = node.attr("height");
		const esClass = node.attr('class');
		const showLicense = node.data("es_show_license");
		const showInfos = node.data("es_show_infos");

		if (o_info.edusharing_content_origin && !isIFrame && (o_info.edusharing_iframe_all || node.data("es_display") === "iframe")) {
			OOEdusharing.replaceWithFrame(node, identifier, version, width, height, esClass, showLicense, showInfos);
			return;
		}
		const spinner = OOEdusharing.replaceWithSpinner(node, width, height);
		OOEdusharing.replaceWithRendered(spinner, identifier, version, width, height, esClass, showLicense, showInfos, isIFrame);
	},
	
	/**
	 * Course element "edu-sharing" and display mode ORIGINAL_IFRAME: the unchanged
	 * edu-sharing content runs in a sandboxed iFrame on the content domain.
	 */
	replaceWithFrame: function(node, identifier, version, width, height, esClass, showLicense, showInfos) {
		let url = o_info.uriprefix.replace("auth", "edusharing") + "frame?identifier=" + encodeURIComponent(identifier);
		if (version >= 0) {
			url += "&version=" + encodeURIComponent(version);
		}
		if (width > 0) {
			url += "&width=" + encodeURIComponent(width);
		}
		if (height) {
			url += "&height=" + encodeURIComponent(height);
		}
		if ("hide" === showLicense) {
			url += "&license=hide";
		}
		if ("hide" === showInfos) {
			url += "&infos=hide";
		}
		const iframe = jQuery("<iframe class='o_edusharing_frame' title='edu-sharing' allow='fullscreen'></iframe>")
			.attr("sandbox", "allow-scripts allow-same-origin allow-popups allow-popups-to-escape-sandbox allow-forms allow-presentation")
			.attr("src", url);
		iframe.css("width", width > 0? width + "px": "100%");
		const container = jQuery("<div class='o_edusharing_container o_edusharing_frame_container BGlossarIgnore'></div>")
			.addClass(esClass || "")
			.append(iframe);
		node.replaceWith(container);
		iframe.iFrameResize({ checkOrigin: [o_info.edusharing_content_origin], warningTimeout: 0 });
	},
	
	/**
	 * Replace the edu-sharing nodes with the real resources from the edu-sharing rendering service.
	 */
	render: function() {
		const esNodes = jQuery("[data-es_identifier]");
		esNodes.addClass("BGlossarIgnore");
		if (esNodes.length > 0) {
			esNodes.each(function() {
				const node = jQuery( this );
				OOEdusharing.replace(node, false);
			});
		}
		// Handle inside internal iFrames as well
		const iFrames = jQuery(".o_iframe_rel");
		if (iFrames.length > 0) {
			iFrames.each(function() {
				const iFrame = jQuery( this );
				iFrame.on('load', function(){
					iFrame.contents().on('click', OOEdusharing.toggleMetadata);
					const iFrameEsNodes = iFrame.contents().find("[data-es_identifier]");
					if (iFrameEsNodes.length > 0) {
						iFrameEsNodes.each(function() {
							const iFrameEsNode = jQuery( this );
							OOEdusharing.replace(iFrameEsNode, true);
						});
					}
				});
			});
		}
	},
	
	/**
	 * Toggle edu-sharing metadata.
	 * see https://github.com/edu-sharing/plugin-moodle/blob/master/filter/edusharing/amd/src/edu.js
	 */
	toggleMetadata: function (e) {
		if (jQuery(e.target).closest(".edusharing_metadata").length) {
			//clicked inside ".edusharing_metadata" - do nothing
		} else if (jQuery(e.target).closest(".edusharing_metadata_toggle_button").length) {
			jQuery(".edusharing_metadata").hide();
			const toggle_button = jQuery(e.target);
			const metadata = toggle_button.parent().find(".edusharing_metadata");
			if (metadata.hasClass('open')) {
				metadata.toggleClass('open');
				metadata.hide();
			} else {
				jQuery(".edusharing_metadata").removeClass('open');
				metadata.toggleClass('open');
				metadata.show();
			}
		} else {
			jQuery(".edusharing_metadata").hide();
			jQuery(".edusharing_metadata").removeClass('open');
		}
	},
	enableMetadataToggler: function() {
		jQuery(document).click(OOEdusharing.toggleMetadata);
		jQuery(document).on("click", ".o_edusharing_container .edusharing_rendering_content_video_options", OOEdusharing.toggleVideoOptions);
		jQuery(document).on("click keydown", ".o_edusharing_container .edusharing_rendering_content_video_options_resolutions li[data-url]", OOEdusharing.selectVideoResolution);
	},
	
	toggleVideoOptions: function(e) {
		const button = jQuery(e.currentTarget);
		const open = button.attr("aria-expanded") !== "true";
		button.attr("aria-expanded", open);
		button.siblings(".edusharing_rendering_content_video_options_content").toggle(open);
	},
	
	selectVideoResolution: function(e) {
		if (e.type === "keydown" && e.key !== "Enter" && e.key !== " ") {
			return;
		}
		e.preventDefault();
		const option = jQuery(e.currentTarget);
		const video = option.closest(".edusharing_rendering_content_video_wrapper").find("video").get(0);
		if (!video) {
			return;
		}
		const time = video.currentTime;
		const playing = !video.paused;
		video.src = option.data("url");
		video.currentTime = time;
		if (playing) {
			video.play();
		}
		option.siblings().attr("aria-checked", "false").find(".o_icon_check").remove();
		option.attr("aria-checked", "true").prepend("<i class='o_icon o_icon_check' aria-hidden='true'></i>");
		option.closest(".edusharing_rendering_content_video_options_content").hide()
			.siblings(".edusharing_rendering_content_video_options").attr("aria-expanded", "false");
	}
}

jQuery( document ).ready(function() {
	OOEdusharing.start();
});