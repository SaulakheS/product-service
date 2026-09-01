package com.zestindia.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product details response payload")
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Integer id;

    @Schema(description = "Product Name", example = "Dell XPS 15 Laptop")
    private String productName;

    @Schema(description = "Username who created the product", example = "admin")
    private String createdBy;

    @Schema(description = "Timestamp when the product was created")
    private LocalDateTime createdOn;

    @Schema(description = "Username who last modified the product", example = "admin")
    private String modifiedBy;

    @Schema(description = "Timestamp when the product was last modified")
    private LocalDateTime modifiedOn;

    @Schema(description = "List of items associated with this product")
    private List<ItemResponse> items;

    public ProductResponse() {
    }

    public ProductResponse(Integer id, String productName, String createdBy, LocalDateTime createdOn, String modifiedBy, LocalDateTime modifiedOn, List<ItemResponse> items) {
        this.id = id;
        this.productName = productName;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.modifiedBy = modifiedBy;
        this.modifiedOn = modifiedOn;
        this.items = items;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public List<ItemResponse> getItems() {
        return items;
    }

    public void setItems(List<ItemResponse> items) {
        this.items = items;
    }
}
