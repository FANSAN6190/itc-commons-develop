async function getCsrfToken() {
  try {
    const response = await fetch('/libs/granite/csrf/token.json', {
      credentials: 'same-origin'
    });
    if (!response.ok) {
      console.error("Failed to fetch CSRF token:", response.status);
      return null;
    }
    const json = await response.json();
    return json.token;
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

document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("groupForm");
  const category = document.getElementById("categorySelect");
  const brand = document.getElementById("brandSelect");
  const subBrand = document.getElementById("subBrandSelect");
  const campaignName = document.getElementById("campaignName");
  const campaignDescription = document.getElementById("campaignDescription");
  const group = document.getElementById("groupDisplay");

  const resourcePath = document.querySelector(".group-form-container").dataset.resourcepath;

  const loaderOverlay = document.querySelector(".loader-overlay");
  const successOverlay = document.querySelector(".success-overlay");
    const errorOverlay = document.querySelector(".error-overlay");
  const formContainer = document.querySelector(".group-form-container");

  brand.disabled = true;
  subBrand.disabled = true;

  function resetAndPopulate(selectElement, map) {
    selectElement.innerHTML = '<option value="">-- Select --</option>';
    if (map && Object.keys(map).length > 0) {
      for (const displayName in map) {
        const value = map[displayName];
        const option = document.createElement("option");
        option.value = value;
        option.text = displayName;
        option.dataset.display = displayName;
        selectElement.appendChild(option);
      }
      selectElement.disabled = false;
    } else {
      selectElement.disabled = true;
    }
  }

  let dynamicCategoryBrandSubBrandMap = {};

  fetch("/bin/populateDropdownMapping")
    .then(res => res.json())
    .then(data => {
      dynamicCategoryBrandSubBrandMap = data;

      const categoryMap = {};
      for (const displayName in dynamicCategoryBrandSubBrandMap) {
        categoryMap[displayName] = dynamicCategoryBrandSubBrandMap[displayName].value;
      }
      resetAndPopulate(category, categoryMap);
    })
    .catch(err => {
      console.error("Error fetching category-brand-subBrand mapping:", err);
    });

  resetAndPopulate(category, categoryMap);

  category.addEventListener("change", function () {
    const selectedCategoryKey = category.value;
    const selectedCategoryDisplay = getDisplayFromKey(dynamicCategoryBrandSubBrandMap, selectedCategoryKey);
    const categoryObj = dynamicCategoryBrandSubBrandMap[selectedCategoryDisplay];
    const brandsObj = categoryObj?.brand || {};
    const brandMap = {};


    for (const brandDisplay in brandsObj) {
      brandMap[brandDisplay] = brandsObj[brandDisplay].value;
    }

    resetAndPopulate(brand, brandMap);
    resetAndPopulate(subBrand, {});
    group.value = "";
  });

  brand.addEventListener("change", function () {
    const selectedCategoryKey = category.value;
    const selectedBrandKey = brand.value;
    const selectedCategoryDisplay = getDisplayFromKey(dynamicCategoryBrandSubBrandMap, selectedCategoryKey);
    const categoryObj = dynamicCategoryBrandSubBrandMap[selectedCategoryDisplay];


    const brandsObj = categoryObj?.brand || {};
    const selectedBrandDisplay = getDisplayFromKey(brandsObj, selectedBrandKey);

    const subBrandsObj = brandsObj[selectedBrandDisplay]?.subBrand || {};
    const subBrandMap = {};
    for (const subBrandDisplay in subBrandsObj) {
      subBrandMap[subBrandDisplay] = subBrandsObj[subBrandDisplay];
    }

    resetAndPopulate(subBrand, subBrandMap);

    if (selectedCategoryDisplay && selectedBrandDisplay) {
      group.value = `${selectedCategoryKey}-${selectedBrandKey}-agency-group`;
    } else {
      group.value = "";
    }
  });

form.addEventListener("submit", async function (e) {
  e.preventDefault();

  successOverlay.classList.remove("show");
  errorOverlay.classList.remove("show");

  loaderOverlay.classList.add("active");
  formContainer.classList.add("blur");

  const selectedCategoryKey = category.value;
  const selectedBrandKey = brand.value;
  const selectedSubBrandKey = subBrand.value;
  const selectedCategoryDisplay = category.options[category.selectedIndex]?.dataset.display;
  const selectedBrandDisplay = brand.options[brand.selectedIndex]?.dataset.display;
  const selectedSubBrandDisplay = subBrand.options[subBrand.selectedIndex]?.dataset.display;

  const formData = {
    category: selectedCategoryKey,
    categoryDisplay: selectedCategoryDisplay,
    brand: selectedBrandKey,
    brandDisplay: selectedBrandDisplay,
    subBrand: selectedSubBrandKey,
    subBrandDisplay: selectedSubBrandDisplay,
    campaignName: campaignName.value,
    campaignDescription: campaignDescription.value,
    group: group.value
  };

  try {
    const csrfToken = await getCsrfToken();
    const selectedCategoryKey = category.value;
    const selectedBrandKey = brand.value;
    const selectedSubBrandKey = subBrand.value;
    const selectedCategoryDisplay = category.options[category.selectedIndex]?.dataset.display;
    const selectedBrandDisplay = brand.options[brand.selectedIndex]?.dataset.display;
    const selectedSubBrandDisplay = subBrand.options[subBrand.selectedIndex]?.dataset.display;
    const formData = {
      category: selectedCategoryKey,
      categoryDisplay: selectedCategoryDisplay,
      brand: selectedBrandKey,
      brandDisplay: selectedBrandDisplay,
      subBrand: selectedSubBrandKey,
      subBrandDisplay: selectedSubBrandDisplay,
      campaignName: campaignName.value,
      campaignDescription: campaignDescription.value,
      group: group.value
    };

    const res = await fetch("/bin/groupdatasource", {
      method: "POST",
      credentials: 'same-origin',
      headers: {
        "Content-Type": "application/json",
        "CSRF-Token": csrfToken
      },
      body: JSON.stringify(formData)
    });
    
    const response = await res.json();

    if (!res.ok || response.error) {
      loaderOverlay.classList.remove("active");
      formContainer.classList.remove("blur");

      errorOverlay.querySelector(".error-message").textContent =
        response.error || "There was an error submitting the form.";
      errorOverlay.classList.add("show");

      setTimeout(() => {
        errorOverlay.classList.remove("show");
      }, 2500);

      return;
    }

    loaderOverlay.classList.remove("active");
    successOverlay.classList.add("show");

    setTimeout(() => {
      successOverlay.classList.remove("show");
      formContainer.classList.remove("blur");
    }, 2000);

    form.reset();
    brand.disabled = true;
    subBrand.disabled = true;
    group.value = "";

  } catch (error) {
    console.error("Submission error:", error);

    loaderOverlay.classList.remove("active");
    formContainer.classList.remove("blur");

    errorOverlay.querySelector(".error-message").textContent =
      "There was an error submitting the form.";
    errorOverlay.classList.add("show");

    setTimeout(() => {
      errorOverlay.classList.remove("show");
    }, 2500);
  }
});
});

