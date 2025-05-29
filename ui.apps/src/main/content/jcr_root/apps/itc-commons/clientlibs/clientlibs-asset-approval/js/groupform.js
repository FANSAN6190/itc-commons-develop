async function getCsrfToken() {
  try {
    const response = await fetch('/libs/granite/csrf/token.json', {
      credentials: 'same-origin'
    });
    if (!response.ok) {
      console.error("Failed to fetch CSRF token:", response.status);
      return null;
    }
    const { token } = await response.json();
    return token;
  } catch (err) {
    console.error("Error fetching CSRF token:", err);
    return null;
  }
}

function getDisplayFromKey(map, selectedKey) {
  for (const display in map) {
    if (map[display].value === selectedKey) return display;
  }
  return "";
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("groupForm");
  const category = document.getElementById("categorySelect");
  const brand = document.getElementById("brandSelect");
  const subBrand = document.getElementById("subBrandSelect");
  const campaignName = document.getElementById("campaignName");
  const campaignDescription = document.getElementById("campaignDescription");
  const groupDisplay = document.getElementById("groupDisplay");
  const resourcePath = document.querySelector(".group-form-container").dataset.resourcepath;
  const loaderOverlay = document.querySelector(".loader-overlay");
  const successOverlay = document.querySelector(".success-overlay");
  const errorOverlay = document.querySelector(".error-overlay");
  const formContainer = document.querySelector(".group-form-container");

  let mapping = {};

  brand.disabled    = true;
  subBrand.disabled = true;

  function resetAndPopulate(selectEl, map) {
    selectEl.innerHTML = '<option value="">-- Select --</option>';
    if (map && Object.keys(map).length) {
      for (const display in map) {
        const opt = document.createElement("option");
        opt.value = map[display];
        opt.text = display;
        opt.dataset.display = display;
        selectEl.appendChild(opt);
      }
      selectEl.disabled = false;
    } else {
      selectEl.disabled = true;
    }
  }

  fetch("/bin/populateDropdownMapping")
    .then(r => r.json())
    .then(data => {
      mapping = data;
      const categoryMap = {};
      for (const display in mapping) {
        categoryMap[display] = mapping[display].value;
      }
      resetAndPopulate(category, categoryMap);
    })
    .catch(err => console.error("Error fetching mapping:", err));

  category.addEventListener("change", () => {
    const catKey = category.value;
    const catDisplay = getDisplayFromKey(mapping, catKey);
    const catObj = mapping[catDisplay] || {};
    const brandsObj = catObj.brand || {};
    const brandMap = {};
    for (const bDisp in brandsObj) {
      brandMap[bDisp] = brandsObj[bDisp].value;
    }
    resetAndPopulate(brand, brandMap);
    resetAndPopulate(subBrand, {});
    groupDisplay.value = "";
  });

  brand.addEventListener("change", () => {
    const catDisplay = getDisplayFromKey(mapping, category.value);
    const catObj = mapping[catDisplay] || {};
    const brandsObj = catObj.brand || {};
    const bDisplay = getDisplayFromKey(brandsObj, brand.value);
    const subObj = brandsObj[bDisplay]?.subBrand || {};
    const subMap = {};
    for (const sbDisp in subObj) {
      subMap[sbDisp] = subObj[sbDisp];
    }
    resetAndPopulate(subBrand, subMap);

    if (category.value && brand.value) {
      groupDisplay.value = `${category.value}-${brand.value}-agency-group`;
    } else {
      groupDisplay.value = "";
    }
  });

  form.addEventListener("submit", async e => {
    e.preventDefault();
    loaderOverlay.classList.add("active");
    formContainer.classList.add("blur");
    successOverlay.classList.remove("show");
    errorOverlay.classList.remove("show");

    const formData = {
      category: category.value,
      categoryDisplay: category.options[category.selectedIndex]?.dataset.display,
      brand: brand.value,
      brandDisplay: brand.options[brand.selectedIndex]?.dataset.display,
      subBrand: subBrand.value,
      subBrandDisplay: subBrand.options[subBrand.selectedIndex]?.dataset.display,
      campaignName: campaignName.value,
      campaignDescription: campaignDescription.value,
      group: groupDisplay.value
    };

    try {
      const csrf = await getCsrfToken();
      const resp = await fetch("/bin/groupdatasource", {
        method: "POST",
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json",
          "CSRF-Token": csrf
        },
        body: JSON.stringify(formData)
      });
      const payload = await resp.json();
      if (!resp.ok || payload.error) {
        throw new Error(payload.error || "Submission failed");
      }
      successOverlay.classList.add("show");
      setTimeout(() => successOverlay.classList.remove("show"), 2000);
      form.reset();
      brand.disabled = subBrand.disabled = true;
      groupDisplay.value = "";
    } catch (err) {
      console.error("Submission error:", err);
      errorOverlay.querySelector(".error-message").textContent = err.message;
      errorOverlay.classList.add("show");
      setTimeout(() => errorOverlay.classList.remove("show"), 2500);
    } finally {
      loaderOverlay.classList.remove("active");
      formContainer.classList.remove("blur");
    }
  });
});
