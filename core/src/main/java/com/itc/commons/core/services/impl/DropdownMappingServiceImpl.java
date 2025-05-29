package com.itc.commons.core.services.impl;

import com.google.gson.JsonObject;
import com.itc.commons.core.services.DropdownMappingService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;

import static com.itc.commons.core.utils.ItcCommonsConstants.*;

@Component(service = DropdownMappingService.class, immediate = true)
public class DropdownMappingServiceImpl implements DropdownMappingService {


  /**
   *
   * @param resolver Passes authenticated request resolver
   * @return JsonObject mapping of cat-brand-subBrand
   * @throws RepositoryException if unable to read jcr
   */
  @Override
  public JsonObject getDropdownMapping(ResourceResolver resolver) throws RepositoryException {

    JsonObject result = new JsonObject();
    Resource root = resolver.getResource(ROOT_PATH);

    if (root != null) {
      for (Resource category : root.getChildren()) {
        String categoryTitle = category.getValueMap().get(PROP_JCR_TITLE, null);
        String categoryValue = category.getValueMap().get(PROP_VALUE, category.getName());

        if (categoryTitle == null) continue;

        JsonObject categoryObj = new JsonObject();
        categoryObj.addProperty(PROP_VALUE, categoryValue);

        JsonObject brandsObj = new JsonObject();

        Resource brandFolder = category.getChild(BRAND);
        if (brandFolder != null) {
          for (Resource brand : brandFolder.getChildren()) {
            String brandTitle = brand.getValueMap().get(PROP_JCR_TITLE, null);
            String brandValue = brand.getValueMap().get(PROP_VALUE, brand.getName());


            if (brandTitle == null) continue;

            JsonObject brandObj = new JsonObject();
            brandObj.addProperty(PROP_VALUE, brandValue);

            JsonObject subBrandsObj = new JsonObject();
            Resource subBrandFolder = brand.getChild(SUB_BRAND);
            if (subBrandFolder != null) {
              for (Resource subBrand : subBrandFolder.getChildren()) {
                String subBrandTitle = subBrand.getValueMap().get(PROP_JCR_TITLE, null);
                String subBrandValue = subBrand.getValueMap().get(PROP_VALUE, subBrand.getName());

                if (subBrandTitle == null) continue;

                subBrandsObj.addProperty(subBrandTitle, subBrandValue);
              }
            }
            brandObj.add(SUB_BRAND, subBrandsObj);
            brandsObj.add(brandTitle, brandObj);
          }
        }

        categoryObj.add(BRAND, brandsObj);
        result.add(categoryTitle, categoryObj);
      }
    }
    return result;
  }
}
