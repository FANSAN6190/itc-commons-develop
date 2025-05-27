package com.itc.commons.core.services;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

public interface S3FileUploadService {

    String updateFileInS3(JsonObject jsonObject, String brandName, String fileName, List<String> fieldLabels) throws IOException;

}
