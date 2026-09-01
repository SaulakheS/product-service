package com.zestindia.productservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item details response")
public class ItemResponse {

    @Schema(description = "Item ID", example = "1")
    private Integer id;

    @Schema(description = "Associated Product ID", example = "10")
    private Integer productId;

    @Schema(description = "Quantity of item", example = "50")
    private Integer quantity;

    public ItemResponse() {
    }

    public ItemResponse(Integer id, Integer productId, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
