const utils = {

  /**
   * Get the rendition path for the given device width based on whether the image
   * has to be shown on large view or not.
   * @param {string} deviceWidth device width, based on which rendition will be selected
   * @returns rendition's relative path
   */
  getDeviceRendition: function (deviceWidth) {
    let imageRenditionKey;
    if (deviceWidth >= GLOBAL_VAR.BREAK_POINTS.LARGE) {
      imageRenditionKey = 'LARGE';
    } else if (deviceWidth >= GLOBAL_VAR.BREAK_POINTS.MEDIUM) {
      imageRenditionKey = 'MEDIUM';
    } else {
      imageRenditionKey = 'SMALL';
    }

    const renditionName = GLOBAL_VAR.DEVICE_RENDITION_MAP.IMAGES[imageRenditionKey];
    const renditionRelativePath = "/jcr:content/renditions/" + renditionName;
    return renditionRelativePath;
  }
};