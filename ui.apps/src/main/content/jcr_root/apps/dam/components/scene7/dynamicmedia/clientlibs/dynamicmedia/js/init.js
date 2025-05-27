/*
 * ADOBE CONFIDENTIAL
 *
 * Copyright 2016 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

(function() {
    'use strict';


    var COMP_SELECTOR = '.s7dm-dynamic-media';
    var VIEWER_JS = {
        'BasicZoomViewer': 'html5/js/BasicZoomViewer.js',
        'FlyoutViewer': 'html5/js/FlyoutViewer.js',
        'MixedMediaViewer': 'html5/js/MixedMediaViewer.js',
        'SpinViewer': 'html5/js/SpinViewer.js',
        'VideoViewer': 'html5/js/VideoViewer.js',
        'ZoomViewer': 'html5/js/ZoomViewer.js',
        'ZoomVerticalViewer': 'html5/js/ZoomVerticalViewer.js',
        'Responsive': 'libs/responsive_image.js'
    };
    var responsiveViewerCSS = 's7responsiveViewer'; //defined in responsive.css



    $(document).ready(function(){
        //do nothing if there is no component due to no asset yet or DM disabled
        if ($(COMP_SELECTOR).length > 0) {
            var viewers = new S7dmUtils.Viewer();
            var viewerRootPath = $(COMP_SELECTOR).data('viewer-path');
            var pageLocale = $(COMP_SELECTOR).data('page-locale');
            var s7i18nPath = '/libs/dam/components/scene7/common/clientlibs/i18n.js';
            if (typeof Granite != 'undefined' && typeof Granite.I18n != 'undefined') {
                Granite.I18n.setLocale(pageLocale);
                s7i18nPath = Granite.HTTP.externalize(s7i18nPath);
            }
            $('head').append('<script type="text/javascript" src="' + s7i18nPath + '"></script>');
            viewers.load(VIEWER_JS, viewerRootPath)
                .ready({
                    success: initViewers,
                    fail: failViewerLoader
                });
        }
    });

    /**
     * Viewer intialization for interactive media component
     */
    function initViewers(){
        var components = $(COMP_SELECTOR);
        components.each(function(){
            setupViewer($(this));

        });
    }


    /**
     * Setup viewer based on component
     * @param component a div component contains viewer config
     */
    function setupViewer(component){
        var compId = component.attr('id');
        var viewer = S7dmUtils[compId],
            assetType = component.data('asset-type'),
            mode = component.data('mode'),
            stageSize = component.data('stagesize');

        if (typeof viewer != 'undefined' && viewer != null) {
            viewer.dispose();
            S7dmUtils[compId] = null;
            component.removeClass(responsiveViewerCSS);
        }
        component.html('');
        var viewerType = component.data('viewer-type');

        if (viewerType) {

            var viewerParams = buildViewerParams(component);
            var viewerSettings = { 'containerId' : component.attr('id'),
                'params': viewerParams };
            if (!(viewerType == 'FlyoutViewer' && stageSize)) {
                component.addClass(responsiveViewerCSS);
            }
            viewer = new s7viewers[viewerType](viewerSettings);
            if (typeof Granite != 'undefined' && typeof Granite.I18n != 'undefined') {
                viewer.setLocalizedTexts(getLocalizedText()); //passing localized text to the viewer
            }
            S7dmUtils[compId] = viewer;
            viewer.init();
        }
        else if(assetType === 'image') {
            if (component.data('breakpoints') || mode == 'smartcrop') {
                component[0].removeAttribute('data-stagesize');
                buildResponsiveImage(component);
            }
            else {
                component[0].removeAttribute('data-enablehd');
                buildImage(component);
            }
        }

    }

    /**
     * Build Image and anchor when there is no viewer constructor defined for IMAGE ONLY
     * @param component a div component contains viewer config
     */
    function buildImage(component) {
        var stageSize = component.data('stagesize'),
            linkURL = component.data('linkurl'),
            linkTarget = component.data('linktarget'),
            title = component.data('title'),
            altText = component.data('alt'),
            imagePreset = component.data('imagepreset'),
            imageServer = component.data('imageserver'),
            modifiers = component.data('urlmodifiers');

        var imgSetup = {
            'src': imageServer + component.data('asset-path')
        }
        if (imagePreset) {
            imgSetup['src'] += '?$' + imagePreset + '$';
        }
        if (modifiers) {
            imgSetup['src'] += (imagePreset ? '&' : '?') + modifiers
        }

        if (stageSize) {
            var stageSizeParts = stageSize.split(',');
            imgSetup['width'] = stageSizeParts[0];
            if (stageSizeParts.length > 1) {
                imgSetup['height'] = stageSizeParts[1];
            }
        }

        var imgComp = $('<img>', imgSetup);
        //if there is link, wrap it around image
        if (linkURL) {
            var anchorSetup = {
                'href': linkURL,
                'target': linkTarget
            };
            if (altText) {
                anchorSetup['alt'] = altText;
                anchorSetup['title'] = altText;  // default to alt text
                imgComp.attr('alt', altText);
            }
            if (title) {
                anchorSetup['title'] = title;
            }
            var anchor = $('<a>', anchorSetup);
            anchor.append(imgComp);
            component.append(anchor);
        }
        else {
            //Add alt text if it's set
            if (altText) {
                imgComp.attr('alt', altText);
            }
            component.append(imgComp);
        }

    }

    /**
     * Build responsive image
     * @param component container
     */
    function buildResponsiveImage(component) {
        var imagePreset = component.data('imagepreset'),
            imageServer = component.data('imageserver'),
            linkURL = component.data('linkurl'),
            linkTarget = component.data('linktarget'),
            title = component.data('title'),
            altText = component.data('alt'),
            customClass = component.data('custom-class');
        var modifiers = component.data('urlmodifiers');
        var respCon = $('<div>', { 'class': 's7responsiveContainer' });
        var respImgId = component.attr('id') + '_resp';
        var respImgSetup = {
            'id': respImgId,
            'src': "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7",
            'data-src': imageServer + component.data('asset-path'),
            'class': 'fluidimage '+(typeof customClass != 'undefined' ? customClass : ''),
            'data-mode': component.data('mode'),
            'data-breakpoints': component.data('breakpoints'),
            'data-enablehd': component.data('enablehd') == "never" ? "never" : "always",
            'data-aspectratio' : component.data('aspectratio')
        }
        if (imagePreset) {
            respImgSetup['data-src'] += '?$' + imagePreset + '$';
        }
        if (modifiers) {
            respImgSetup['data-src'] += (imagePreset ? '&' : '?') + modifiers
        }
        var respImg = $('<img>', respImgSetup);
        //if there is link, wrap it around image
        if (linkURL) {
            var anchorSetup = {
                'href': linkURL,
                'target': linkTarget
            };
            if (altText) {
                anchorSetup['alt'] = altText;
                anchorSetup['title'] = altText;  // default to alt text
                respImg.attr('alt', altText);
            }
            if (title) {
                anchorSetup['title'] = title;
            }
            var anchor = $('<a>', anchorSetup);
            anchor.append(respImg);
            respCon.append(anchor);
        } else {
            if (altText) {
                respImg.attr('alt', altText);
            }
            respCon.append(respImg);
        }
        component.append(respCon);
        s7responsiveImage(document.getElementById(respImgId));
    }

    /**
     * @param component a div element with viewer setup
     * @return viewer parameters in JSON
     */
    function buildViewerParams(component) {
        var assetPath = component.data('asset-path'),
            assetType = component.data('asset-type'),
            viewerType = component.data('viewer-type'),
            imageserver = component.data('imageserver'),
            videoserver = component.data('videoserver'),
            contentserver = component.data('contenturl'),
            config = component.data('config'),
            wcmdisabled = component.data('wcmdisabled'),
            stageSize = component.data('stagesize'),
            viewerModifiers = component.data('viewermodifiers'),
            isdms7 = component.data('dms7');

        var viewerParams = {
            'asset': assetPath,
            'serverurl': imageserver,
            'contenturl': contentserver
        };

        //Add extra viewer modifiers if any
        if (viewerModifiers) {
            $.extend(viewerParams, getViewerModifiers(viewerModifiers));
        }

        var isAuthor = !wcmdisabled && wcmdisabled != '';

        //Add cache=off when WCM is not disabled (author side)
        if (isAuthor) {
            viewerParams['asset'] += '?cache=off';
        }
        if (stageSize) {
            viewerParams['stagesize'] = stageSize;
        }
        if (viewerType == 'FlyoutViewer') {
            viewerParams['imagereload'] = '1,breakpoint,100;320;480';
        }
        if (assetPath.indexOf('/') != 0) {
            viewerParams['aemmode'] = '0';
        }

        //For video, we add videoserverurl
        if (viewerType.indexOf('Video') >= 0 || viewerType.indexOf('Mixed') >= 0) {
            viewerParams['videoserverurl'] = videoserver;
            // force native if
            // Asset is non-AVS video (apply to both DMS7 and DM Hybrid for playing progressively) OR
            // (Asset is DMS7 video regardless of AVS or non-AVS AND the page is on author side)
            if (assetType == 'video' || ((typeof isdms7 != 'undefined') && isAuthor)) {
                viewerParams['playback'] = 'native';
                viewerParams['progressivebitrate'] = '20000';
            }
        }
        //check for viewer preset setup
        if (typeof config != 'undefined' && config.length > 0) {
            viewerParams['config'] = getViewerPresetPath(config);
        }
        return viewerParams;
    }

    /**
     * Parse viewer preset path for viewer-usable format
     * @param config viewer preset path stored in the component node
     * @return viewer-usable format viewer preset path
     */
    function getViewerPresetPath(config) {
        var configParts = config.split('|');
        return configParts[0];
    }

    /**
     * Parse viewer preset path for isCustom Preset flag
     * @param config viewer preset string
     * @return flag for custom preset
     */
    function isCustomPreset(config) {
        var configParts = config.split('|');
        return configParts[2];
    }

    /**
     * Fail viewer loader handler
     */
    function failViewerLoader(){
        console.log('S7Viewer is missing');
    }

    /**
     * Parse free-form viewer modifiers
     * @param viewerModifiers string for free-form viewer modifiers
     * @return JSON representing viewer modifiers
     */
    function getViewerModifiers(viewerModifiers) {
        var modifiers = {};
        if (viewerModifiers) {
            var items = viewerModifiers.split('&');
            for (var i = 0; i < items.length; i++) {
                //only when there is key=val pair
                if (items[i].indexOf('=') > 0) {
                    var key = items[i].substring(0, items[i].indexOf('='));
                    var val = items[i].substring(items[i].indexOf('=') + 1);
                    modifiers[key] = val;
                }
            }
        }
        return modifiers;
    }

    /**
     * Get localized text for viewer from s7sdk_i18n.localizedText
     * @return JSON for localized text
     */
    function getLocalizedText() {
        var localizedText = {};
        localizedText[Granite.I18n.getLocale()] = formatLocalizedText(s7sdk_i18n.localizedText);
        localizedText['defaultLocale'] = Granite.I18n.getLocale();
        return localizedText;
    }

    /**
     * Format localized text into a correct viewer format
     * @param localizedText localized text parsed from s7sdk
     * @return formatted localized text with correct key for viewer
     */
    function formatLocalizedText(localizedText) {
        var formatTexts = {};
        for (var compKey in localizedText) {
            var compObj = localizedText[compKey];
            for (var symbolKey in compObj) {
                var modKey = compKey + '.' + symbolKey;
                var symbolVal = compObj[symbolKey];
                formatTexts[modKey] = symbolVal;
            }
        }
        return formatTexts;
    }

}).call(this);
