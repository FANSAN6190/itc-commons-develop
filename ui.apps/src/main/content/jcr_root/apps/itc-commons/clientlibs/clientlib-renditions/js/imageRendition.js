const renditions = {
  /**
   *
   */
  apply: () => {
    const windowWidth = screen.width;
    const imageRenditionPath = utils.getDeviceRendition(windowWidth);

    //set image src with renditions based on device
    document.querySelectorAll("img[data-device-src]").forEach((item, index) => {
      let src = item.getAttribute('data-device-src');
      const extension = src.substring(src.lastIndexOf("."));
      if (extension === ".gif" || extension === ".svg") {
        src += "/jcr:content/renditions/original";
      } else {
        src += imageRenditionPath;
      }
      item.src = src;
    });

    //set background images with renditions based on device
    document.querySelectorAll("[data-device-bg-src]").forEach((item, index) => {
      let bgURL = item.getAttribute('data-device-bg-src');
      const extension = bgURL.substring(bgURL.lastIndexOf("."));
      if (extension === ".gif" || extension === ".svg") {
        bgURL += "/jcr:content/renditions/original";
      } else {
        bgURL += imageRenditionPath;
      }
      item.style.backgroundImage = `url("${bgURL}")`;
    });

    //Set background images without renditions. Can be used for DM static URLs
    document.querySelectorAll("[data-bg-src]").forEach((item, index) => {
      let bgURL = item.getAttribute('data-bg-src');
      item.style.backgroundImage = `url("${bgURL}")`;
    });

    //Set background images with different images in desktop and mobile view
    document.querySelectorAll("[data-bg-desktop-src]").forEach((item, index) => {
      const desktopURL = item.getAttribute('data-bg-desktop-src');
      const mobileURL = item.getAttribute('data-bg-mobile-src');
      let bgURL;
      const extension = desktopURL.substring(desktopURL.lastIndexOf("."));
      if (windowWidth >= GLOBAL_VAR.BREAK_POINTS.SMALL) {
        bgURL = desktopURL;
      } else {
        bgURL = mobileURL;
      }
      if (extension === ".gif" || extension === ".svg") {
        bgURL += "/jcr:content/renditions/original";
      } else {
        bgURL += imageRenditionPath;
      }
      item.style.backgroundImage = `url("${bgURL}")`;
    });

    //set DM background image with different images for mobile and desktop
    document.querySelectorAll("[data-desktop-dm-bg-src]").forEach((item, index) => {
      const desktopURL = item.getAttribute('data-desktop-dm-bg-src');
      const mobileURL = item.getAttribute('data-mobile-dm-bg-src');
      let bgURL;
      if (windowWidth >= GLOBAL_VAR.BREAK_POINTS.SMALL) {
        bgURL = desktopURL;
      } else {
        bgURL = mobileURL;
      }
      item.style.backgroundImage = `url("${bgURL}")`;
    });
  },
};

document.addEventListener("DOMContentLoaded", () => {
  renditions.apply();
});
