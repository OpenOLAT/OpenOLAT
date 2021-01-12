/*
 * Credit goes to https://github.com/MatheusAvellar/textarea-line-numbers
 * Parts of the script has been modified, renamed, dropped
 * New parts have been added
 */

var O_TEXTAREA = {
    eventList: {},
    errorList: {},
    update_line_numbers: function(textarea, line_numbers_container) {
        // Let's check if there are more or less lines than before
        var line_count = textarea.value.split("\n").length;
        // Check whether there are more lines than the default height shows
        if(textarea.rows > line_count) {
            line_count = textarea.rows;
        }
        var child_count = line_numbers_container.children.length;
        var difference = line_count - child_count;
        // If there is any positive difference, we need to add more line numbers
        if(difference > 0) {
            // Create a fragment to work with so we only have to update DOM once
            var frag = document.createDocumentFragment();
            // For each new line we need to add,
            while(difference > 0) {
                // Create a <div> as a wrapper for eventual error icon and the line number
                var line_number_wrapper = document.createElement("div");
                line_number_wrapper.className = "o_textarea_line_numbers_row";

                var line_number = document.createElement("span");
                line_number.className = "text-muted";
                line_number.textContent = 1 + child_count++;

                line_number_wrapper.appendChild(line_number);

                frag.appendChild(line_number_wrapper);
                difference--;
            }
            // Append fragment (with <span> children) to our wrapper element
            line_numbers_container.appendChild(frag);
        }
        // If there are any errors, show them
        if(O_TEXTAREA.errorList[textarea.id] != null && O_TEXTAREA.errorList[textarea.id].length > 0) {
            O_TEXTAREA.show_errors(textarea.id);
        }
        // If, however, there's negative difference, we need to remove line numbers
        while(difference < 0) {
            // Simple stuff, remove last child and update difference
            line_numbers_container.removeChild(line_numbers_container.lastChild);
            difference++;
        }
    },
    set_errors: function (id, error_rows) {
        O_TEXTAREA.errorList[id] = error_rows;
        O_TEXTAREA.show_errors(id);
    },
    show_errors: function(id) {
        // Get errors for specific textarea
        var error_rows = O_TEXTAREA.errorList[id];

        if (id == null || error_rows == null || error_rows.length === 0) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] Error arguments are not sufficient");
            }
            return;
            return;
        }
        // Get reference to desired <textarea>
        var textarea = document.getElementById(id);
        // If getting reference to element fails, warn and leave
        if(textarea == null) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] Couldn't find textarea of id '"+id+"'");
            }
            return;
        }
        // Get parent (the container wrapper) of the textarea
        var parent = textarea.parentElement;
        if(parent == null) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] Couldn't find the parent of the textarea with id '"+id+"'");
            }
            return;
        }
        // Get the children and select the line numbers wrapper
        var wrapper;
        for(var i = 0; i < parent.childNodes.length; i++) {
            if(parent.childNodes[i].className == "o_textarea_line_numbers_wrapper") {
                wrapper = parent.childNodes[i];
                break;
            }
        }
        // Check whether we got the wrapper
        if(wrapper == null) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] Couldn't find the line numbers wrapper from textarea of id '"+id+"'");
            }
            return;
        }
        // Check every row
        for(var i = 0; i < wrapper.childNodes.length; i++) {
            var row = wrapper.childNodes[i];
            // Add the error, if we have the right line
            if (error_rows.includes(i) && row.childNodes.length < 2) {
                // Create the icon
                error_icon = document.createElement("i");
                error_icon.className = "o_icon o_icon_fw o_icon_warn";
                // Add it at the first position in our row
                row.insertBefore(error_icon, row.firstChild);
            }
            // Remove the error, if there is one
            else if (!error_rows.includes(i) && row.childNodes.length > 1) {
                row.removeChild(row.childNodes[0]);
            }
        }
    },
    append_line_numbers: function(id) {
        // Get reference to desired <textarea>
        var textarea = document.getElementById(id);
        // If getting reference to element fails, warn and leave
        if(textarea == null) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] Couldn't find textarea of id '"+id+"'");
            }
            return;
        }
        // If <textarea> already has TLN active, warn and leave
        if(textarea.className.indexOf("o_textarea_line_numbers_input") != -1) {
            if (window.console) {
                console.warn("[O_TEXTAREA.js] textarea of id '"+id+"' is already numbered");
            }
            return;
        }
        // Otherwise, we're safe to add the class name and clear inline styles
        textarea.classList.add("o_textarea_line_numbers_input");
        textarea.style = {};

        // Create line numbers wrapper, insert it before <textarea>
        var el = document.createElement("div");
        el.className = "o_textarea_line_numbers_wrapper";
        el.setAttribute("aria-hidden", true);
        textarea.parentNode.insertBefore(el, textarea);
        // Call update to actually insert line numbers to the wrapper
        O_TEXTAREA.update_line_numbers(textarea, el);
        // Initialize event listeners list for this element ID, so we can remove
        // them later if needed
        O_TEXTAREA.eventList[id] = [];

        // varant list of input event names so we can iterate
        var __change_evts = [
            "propertychange", "input", "keydown", "keyup"
        ];
        // Default handler for input events
        var __change_hdlr = function(ta, el) {
            return function(e) {
                // If pressed key is Left Arrow (when cursor is on the first character),
                // or if it's Enter/Home, then we set horizontal scroll to 0
                // Check for .keyCode, .which, .code and .key, because the web is a mess
                // [Ref] stackoverflow.com/a/4471635/4824627
                if((+ta.scrollLeft==10 && (e.keyCode==37||e.which==37
                    ||e.code=="ArrowLeft"||e.key=="ArrowLeft"))
                    || e.keyCode==36||e.which==36||e.code=="Home"||e.key=="Home"
                    || e.keyCode==13||e.which==13||e.code=="Enter"||e.key=="Enter"
                    || e.code=="NumpadEnter")
                    ta.scrollLeft = 0;
                // Whether we scrolled or not, let's check for any line count updates
                O_TEXTAREA.update_line_numbers(ta, el);
            }
        }(textarea, el);

        // Finally, iterate through those event names, and add listeners to
        // <textarea> and to events list
        for(var i = __change_evts.length - 1; i >= 0; i--) {
            textarea.addEventListener(__change_evts[i], __change_hdlr);
            O_TEXTAREA.eventList[id].push({
                evt: __change_evts[i],
                hdlr: __change_hdlr
            });
        }

        // varant list of scroll event names so we can iterate
        var __scroll_evts = [ "change", "mousewheel", "scroll" ];
        // Default handler for scroll events (pretty self explanatory)
        var __scroll_hdlr = function(ta, el) {
            return function() {  el.scrollTop = ta.scrollTop;  }
        }(textarea, el);
        // Just like before, iterate and add listeners to <textarea> and to list
        for(var i = __scroll_evts.length - 1; i >= 0; i--) {
            textarea.addEventListener(__scroll_evts[i], __scroll_hdlr);
            O_TEXTAREA.eventList[id].push({
                evt: __scroll_evts[i],
                hdlr: __scroll_hdlr
            });
        }
    }
}
