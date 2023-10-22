package com.shopify.monitor.shopifymonitor.persistance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifyStoreInventory {
    private List<Product> products;
}
