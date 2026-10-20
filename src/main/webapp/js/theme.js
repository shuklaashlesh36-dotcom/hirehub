/**
 * HireHub Theme Manager
 * Supports Light Mode and Dark Mode with instant toggle, keyboard accessibility,
 * persistence via localStorage, and automatic OS preference detection.
 */
(function () {
    'use strict';

    var STORAGE_KEY = 'hirehub-theme';

    /**
     * Determines the user's preferred theme.
     * Hierarchy: 1) Stored preference, 2) OS preference, 3) Default 'dark'
     */
    function getInitialTheme() {
        try {
            var stored = localStorage.getItem(STORAGE_KEY);
            if (stored === 'light' || stored === 'dark') {
                return stored;
            }
            if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
                return 'light';
            }
        } catch (e) {
            // Storage access blocked or restricted
        }
        return 'dark';
    }

    /**
     * Immediately applies the theme to root element to prevent FOUT (Flash of Unstyled Theme).
     */
    var initialTheme = getInitialTheme();
    document.documentElement.setAttribute('data-theme', initialTheme);

    /**
     * Updates all theme toggle buttons across the page with proper icon, accessible label, and title.
     * When Light Mode is active: displays Sun icon (☀️).
     * When Dark Mode is active: displays Moon icon (🌙).
     */
    function updateToggleButtons(theme) {
        var buttons = document.querySelectorAll('.theme-toggle');
        buttons.forEach(function (btn) {
            var icon = btn.querySelector('.theme-toggle-icon');
            var text = btn.querySelector('.theme-toggle-text');
            var isLight = (theme === 'light');

            if (icon) {
                icon.innerHTML = isLight ? '&#9728;&#65039;' : '&#127769;';
            }

            if (text) {
                text.textContent = isLight ? 'Light' : 'Dark';
            }

            var nextMode = isLight ? 'Dark Mode' : 'Light Mode';
            var currentMode = isLight ? 'Light Mode' : 'Dark Mode';

            btn.setAttribute('aria-label', 'Current theme: ' + currentMode + '. Click to switch to ' + nextMode);
            btn.setAttribute('title', 'Current: ' + currentMode + '. Toggle to ' + nextMode);
            btn.setAttribute('aria-pressed', isLight ? 'true' : 'false');
        });
    }

    /**
     * Toggles between light and dark themes, persists choice, and updates UI.
     */
    function toggleTheme() {
        var current = document.documentElement.getAttribute('data-theme') || 'dark';
        var next = (current === 'light') ? 'dark' : 'light';

        document.documentElement.setAttribute('data-theme', next);

        try {
            localStorage.setItem(STORAGE_KEY, next);
        } catch (e) {
            // Silently fallback if localStorage is unavailable
        }

        updateToggleButtons(next);
    }

    // Expose toggle function globally
    window.HireHubTheme = {
        toggle: toggleTheme,
        getTheme: function () {
            return document.documentElement.getAttribute('data-theme') || 'dark';
        },
        setTheme: function (theme) {
            if (theme === 'light' || theme === 'dark') {
                document.documentElement.setAttribute('data-theme', theme);
                try {
                    localStorage.setItem(STORAGE_KEY, theme);
                } catch (e) {}
                updateToggleButtons(theme);
            }
        }
    };

    /**
     * Wire up toggle buttons and OS preference listener once DOM is ready.
     */
    function init() {
        var current = document.documentElement.getAttribute('data-theme') || 'dark';
        updateToggleButtons(current);

        document.querySelectorAll('.theme-toggle').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                toggleTheme();
            });

            btn.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    toggleTheme();
                }
            });
        });

        // Listen for OS scheme changes when user hasn't explicitly saved a choice
        if (window.matchMedia) {
            var mediaQuery = window.matchMedia('(prefers-color-scheme: light)');
            var handleMediaChange = function (e) {
                try {
                    if (!localStorage.getItem(STORAGE_KEY)) {
                        var newTheme = e.matches ? 'light' : 'dark';
                        document.documentElement.setAttribute('data-theme', newTheme);
                        updateToggleButtons(newTheme);
                    }
                } catch (err) {}
            };

            if (mediaQuery.addEventListener) {
                mediaQuery.addEventListener('change', handleMediaChange);
            } else if (mediaQuery.addListener) {
                mediaQuery.addListener(handleMediaChange);
            }
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
