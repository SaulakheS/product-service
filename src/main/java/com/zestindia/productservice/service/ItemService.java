package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.ItemUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;

public interface ItemService {

    ItemResponse getItemById(Integer id);

    ItemResponse updateItem(Integer id, ItemUpdateRequest request);

    ApiResponse<String> deleteItem(Integer id);
}
