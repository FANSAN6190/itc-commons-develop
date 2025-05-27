async function getCsrfToken() {
    const response = await fetch('/libs/granite/csrf/token.json', {
        credentials: 'same-origin'
    });
    const json = await response.json();
    return json.token;
}

document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("groupForm");
  const category = document.getElementById("categorySelect");
  const brand = document.getElementById("brandSelect");
  const subBrand = document.getElementById("subBrandSelect");
  const campaignName = document.getElementById("campaignName");
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

  const categoryMap = {};
  for (const displayName in window.categoryBrandSubBrandMap) {
    categoryMap[displayName] = window.categoryBrandSubBrandMap[displayName].value;
  }
  resetAndPopulate(category, categoryMap);

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
    let selectedCategoryDisplay = "";
    for (const display in window.categoryBrandSubBrandMap) {
      if (window.categoryBrandSubBrandMap[display].value === selectedCategoryKey) {
        selectedCategoryDisplay = display;
        break;
      }
    }

    const categoryObj = window.categoryBrandSubBrandMap[selectedCategoryDisplay];
    const brandsObj = categoryObj?.brands || {};
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
    let selectedCategoryDisplay = "";
    for (const display in window.categoryBrandSubBrandMap) {
      if (window.categoryBrandSubBrandMap[display].value === selectedCategoryKey) {
        selectedCategoryDisplay = display;
        break;
      }
    }

    const categoryObj = window.categoryBrandSubBrandMap[selectedCategoryDisplay];
    const brandsObj = categoryObj?.brands || {};
    let selectedBrandDisplay = "";

    for (const display in brandsObj) {
      if (brandsObj[display].value === selectedBrandKey) {
        selectedBrandDisplay = display;
        break;
      }
    }

    const subBrandsObj = brandsObj[selectedBrandDisplay]?.subBrands || {};
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
      group: group.value
    };

    const csrfToken = await getCsrfToken();

      const res = await fetch("/bin/groupdatasource", {
        method: "POST",
        credentials: 'same-origin',
        headers: {
          "Content-Type": "application/json",
          'CSRF-Token': csrfToken
        },
        body: JSON.stringify(formData)
      })

      .then(res => {
        if (!res.ok) throw new Error("Network response was not ok");
        return res.json();
      })

      .then(response => {
        alert("Form submitted successfully!");
        console.log("Success:", response);
        form.reset();
        brand.disabled = true;
        subBrand.disabled = true;
      })

      .catch(error => {
        console.error("Submission error:", error);
        alert("There was an error submitting the form.");
      })
      .finally(() => {
      	loader.classList.add("hidden");
       });
  });
});
