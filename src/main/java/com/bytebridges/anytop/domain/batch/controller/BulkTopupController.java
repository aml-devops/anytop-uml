package com.bytebridges.anytop.domain.batch.controller;

import com.bytebridges.anytop.common.ServiceResponse;
import com.bytebridges.anytop.domain.batch.ExcelTopupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Bulk Topup Management")
@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
public class BulkTopupController {

    private final ExcelTopupService excelTopupService;

    @Operation(
        summary = "Upload Topup Excel",
        description = "Uploads Excel sheet and saves topup transactions into database"
    )
    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ServiceResponse<?> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        try {

            int total = excelTopupService.processExcel(file);

            return ServiceResponse.success(
                    "Excel uploaded successfully. Total records: " + total);

        } catch (Exception e) {

            return ServiceResponse.error(e.getMessage());
        }
    }
}
