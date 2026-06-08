
const OOEdusharing = {
		
	start: function() {
		if (o_info.edusharing_enabled) {
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

	replaceGoTo: function(html, identifier) {
		const url = o_info.uriprefix.replace("auth", "edusharing") + "goto?identifier=" + identifier;
		html = html.replace("{{{LMS_INLINE_HELPER_SCRIPT}}}", url)
		return html;
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
				const goToData = OOEdusharing.replaceGoTo(data, identifier);
				const esNode = container.append(goToData);
				node.replaceWith(esNode);
				o_adjustContentHeightForAbsoluteElement('.o_edusharing_container .edusharing_metadata_wrapper');
			},
			error : function(xhr) {
				if (xhr.responseText) {
					node.replaceWith("<div class='o_warning'>" + xhr.responseText + "</div>");
				} else {
					node.replaceWith("<div class='o_warning'>edu-sharing not available</div>");
				}
			}
		})
	},
		
	replace: function(node, isIFrame) {
		const identifier = node.data("es_identifier");
		const version = node.data("es_version");
		const width = node.attr("width");
		const height = node.attr("height");
		const esClass = node.attr('class');
		const showLicense = node.data("es_show_license");
		const showInfos = node.data("es_show_infos");

		const spinner = OOEdusharing.replaceWithSpinner(node, width, height);
		OOEdusharing.replaceWithRendered(spinner, identifier, version, width, height, esClass, showLicense, showInfos, isIFrame);
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
	}
}

jQuery( document ).ready(function() {
	OOEdusharing.start();
});