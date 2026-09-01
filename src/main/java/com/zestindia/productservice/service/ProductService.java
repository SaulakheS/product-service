package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.ItemCreateRequest;
import com.zestindia.productservice.dto.request.ProductCreateRequest;
import com.zestindia.productservice.dto.request.ProductUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.dto.response.PagedResponse;
import com.zestindia.productservice.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, String search);

    ProductResponse getProductById(Integer id);

    ProductResponse updateProduct(Integer id, ProductUpdateRequest request);

    ApiResponse<String> deleteProduct(Integer id);

    List<ItemResponse> getItemsByProductId(Integer productId);

    ItemResponse addItemToProduct(Integer productId, ItemCreateRequest request);
}
