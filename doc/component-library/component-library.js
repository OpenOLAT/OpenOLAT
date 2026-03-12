/**
 * OpenOlat Component Library - Theme switcher and utilities
 */

// Built-in themes path (always available)
var builtinThemesPath = '../../src/main/webapp/static/themes/';
// External client themes path (../../themes when served from project root,
// or symlinked into the project)
var externalThemesPath = '../../themes/';

// Active base path for the currently selected theme
var activeThemesPath = builtinThemesPath;

function switchTheme(theme) {
  var link = document.getElementById('o_theme_css');
  if (!link) return;

  var customPathEl = document.querySelector('.cl-custom-path');

  if (theme === '__custom__') {
    // Show custom path input
    if (customPathEl) customPathEl.classList.add('visible');
    // Apply saved custom path if available
    try {
      var savedPath = localStorage.getItem('cl-custom-css');
      if (savedPath) {
        link.href = savedPath;
      }
    } catch(e) {}
    try { localStorage.setItem('cl-theme', '__custom__'); } catch(e) {}
    return;
  }

  // Hide custom path input for normal themes
  if (customPathEl) customPathEl.classList.remove('visible');

  // Determine which path to use: check if this theme exists in the external
  // themes dir, otherwise use built-in
  var sel = document.getElementById('themeSwitch');
  var opt = sel && sel.querySelector('option[value="' + theme + '"]');
  var path = (opt && opt.dataset.path) || builtinThemesPath;

  link.href = path + theme + '/theme.css';
  try { localStorage.setItem('cl-theme', theme); } catch(e) {}
}

function applyCustomCss() {
  var input = document.querySelector('.cl-custom-path input');
  if (!input) return;
  var cssPath = input.value.trim();
  if (!cssPath) return;
  var link = document.getElementById('o_theme_css');
  if (link) link.href = cssPath;
  try { localStorage.setItem('cl-custom-css', cssPath); } catch(e) {}
}

// Discover themes dynamically, with static HTML options as fallback
(function() {
  var sel = document.getElementById('themeSwitch');
  if (!sel) return;

  function restoreSavedTheme() {
    try {
      var saved = localStorage.getItem('cl-theme');
      if (saved === '__custom__') {
        sel.value = '__custom__';
        switchTheme('__custom__');
        // Restore custom path into input
        var input = document.querySelector('.cl-custom-path input');
        var savedPath = localStorage.getItem('cl-custom-css');
        if (input && savedPath) input.value = savedPath;
      } else if (saved && sel.querySelector('option[value="' + saved + '"]')) {
        sel.value = saved;
        switchTheme(saved);
      }
    } catch(e) {}
  }

  // Append "custom" option to dropdown and create input element
  function appendCustomOption() {
    if (sel.querySelector('option[value="__custom__"]')) return;
    var opt = document.createElement('option');
    opt.value = '__custom__';
    opt.textContent = '— custom CSS path —';
    sel.appendChild(opt);

    // Create the custom path input if not already in DOM
    if (!document.querySelector('.cl-custom-path')) {
      var container = document.createElement('div');
      container.className = 'cl-custom-path';
      container.innerHTML = '<input type="text" placeholder="path/to/theme.css">' +
        '<button onclick="applyCustomCss()">Apply</button>';
      // Insert after the theme toggle label
      var controls = sel.closest('.cl-theme-toggle') || sel.closest('.meta');
      if (controls && controls.parentNode) {
        controls.parentNode.insertBefore(container, controls.nextSibling);
      }
      // Allow Enter key to apply
      container.querySelector('input').addEventListener('keydown', function(e) {
        if (e.key === 'Enter') applyCustomCss();
      });
    }
  }

  // Parse a directory listing HTML into an array of subdirectory names
  function parseDirectoryListing(html) {
    var matches = html.match(/href="([^"]+?)\/"/g);
    if (!matches) return [];
    var names = [];
    matches.forEach(function(m) {
      var name = m.replace(/href="/, '').replace(/"/, '').replace(/\/$/, '');
      if (name.indexOf('.') === 0 || name.indexOf('/') !== -1) return;
      names.push(name);
    });
    return names;
  }

  // Verify which directories have a theme.css, return promise of {name, path} array
  function verifyThemes(names, basePath) {
    return Promise.all(names.map(function(name) {
      return fetch(basePath + name + '/theme.css', { method: 'HEAD' })
        .then(function(r) { return r.ok ? { name: name, path: basePath } : null; })
        .catch(function() { return null; });
    })).then(function(results) {
      return results.filter(function(r) { return r !== null; });
    });
  }

  // Build dropdown from verified themes
  function populateDropdown(themes) {
    sel.innerHTML = '';
    themes.forEach(function(t) {
      var opt = document.createElement('option');
      opt.value = t.name;
      opt.textContent = t.name;
      opt.dataset.path = t.path;
      sel.appendChild(opt);
    });
    appendCustomOption();
    restoreSavedTheme();
  }

  // Try to fetch a directory listing, returns promise of names array or empty
  function fetchThemeNames(basePath) {
    return fetch(basePath)
      .then(function(r) {
        if (!r.ok) throw new Error('not ok');
        return r.text();
      })
      .then(function(html) {
        return parseDirectoryListing(html);
      })
      .catch(function() {
        return [];
      });
  }

  // Main: discover from both built-in and external paths
  try {
    Promise.all([
      fetchThemeNames(builtinThemesPath),
      fetchThemeNames(externalThemesPath)
    ]).then(function(results) {
      var builtinNames = results[0];
      var externalNames = results[1];

      if (builtinNames.length === 0 && externalNames.length === 0) {
        appendCustomOption();
        restoreSavedTheme();
        return;
      }

      return Promise.all([
        verifyThemes(builtinNames, builtinThemesPath),
        verifyThemes(externalNames, externalThemesPath)
      ]).then(function(verified) {
        var builtinThemes = verified[0];
        var externalThemes = verified[1];

        // Merge: external themes override built-in if same name
        var map = {};
        builtinThemes.forEach(function(t) { map[t.name] = t; });
        externalThemes.forEach(function(t) { map[t.name] = t; });

        var all = Object.keys(map).sort().map(function(k) { return map[k]; });

        if (all.length > 0) {
          populateDropdown(all);
        } else {
          appendCustomOption();
          restoreSavedTheme();
        }
      });
    }).catch(function() {
      appendCustomOption();
      restoreSavedTheme();
    });
  } catch(e) {
    appendCustomOption();
    restoreSavedTheme();
  }
})();

// Toggle code visibility
document.addEventListener('click', function(e) {
  if (e.target.classList.contains('cl-code-toggle')) {
    var codeBlock = e.target.closest('.cl-code');
    if (codeBlock) {
      codeBlock.classList.toggle('open');
      e.target.textContent = codeBlock.classList.contains('open') ? 'Hide Code' : 'Show Code';
    }
  }
});

// Highlight active nav link in side-nav
(function() {
  var currentPage = window.location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.side-nav a').forEach(function(a) {
    var href = a.getAttribute('href');
    if (href === currentPage) {
      a.classList.add('active');
    } else {
      a.classList.remove('active');
    }
  });
})();
