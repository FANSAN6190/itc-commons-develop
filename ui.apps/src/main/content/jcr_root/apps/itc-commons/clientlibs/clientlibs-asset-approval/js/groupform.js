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
  const group = document.getElementById("groupSelect");
  const resourcePath = document.querySelector(".group-form-container").dataset.resourcepath;

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

  fetch(resourcePath)
    .then(res => res.json())
    .then(data => {
      data.forEach(groupItem => {
        const option = document.createElement("option");
        option.value = groupItem.value;
        option.text = groupItem.text;
        group.appendChild(option);
      });
    })
    .catch(err => {
      console.error("Error loading groups:", err);
    });

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
  });

  form.addEventListener("submit", async function (e) {
    e.preventDefault();
    const loader = document.querySelector(".loader");
    loader.classList.remove("hidden");

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

    const csrfToken = await getCsrfToken();

    try {
      const res = await fetch("/bin/groupdatasource", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
          "Content-Type": "application/json",
          'CSRF-Token': csrfToken
        },
        body: JSON.stringify(formData)
      });

      if (!res.ok) throw new Error("Network response was not ok");

      const response = await res.json();
      alert("Form submitted successfully!");
      console.log("Success:", response);
      form.reset();
      brand.disabled = true;
      subBrand.disabled = true;

    } catch (error) {
      console.error("Submission error:", error);
      alert("There was an error submitting the form.");
    } finally {
      loader.classList.add("hidden");
    }
  });
});

