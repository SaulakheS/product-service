package com.zestindia.productservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Product creation payload")
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    @Schema(description = "Name of the product", example = "Dell XPS 15 Laptop")
    private String productName;

    public ProductCreateRequest() {
    }

    public ProductCreateRequest(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
