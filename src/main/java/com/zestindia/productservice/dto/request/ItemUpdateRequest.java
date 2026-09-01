package com.zestindia.productservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Item update payload")
public class ItemUpdateRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(description = "Updated quantity of the item", example = "100")
    private Integer quantity;

    public ItemUpdateRequest() {
    }

    public ItemUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
