package com.shopify.monitor.shopifymonitor.service;

import com.shopify.monitor.shopifymonitor.api.vo.ShopifyStoreInventoryVO;
import com.shopify.monitor.shopifymonitor.feignclient.ShopifyProductsFeignClient;
import com.shopify.monitor.shopifymonitor.persistance.model.ShopifyStoreInventory;
import com.shopify.monitor.shopifymonitor.persistance.repository.ProductRepository;
import com.shopify.monitor.shopifymonitor.persistance.repository.VariantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class RetrieveProducts {

    @Autowired
    private ShopifyProductsFeignClient productsFeignClient;

    @Autowired
    private DiscordService discordService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantRepository variantRepository;

    public ShopifyStoreInventoryVO retrieveProducts(String siteUrl) {
        ResponseEntity<ShopifyStoreInventoryVO> storeInventory = null;
        ResponseEntity<ShopifyStoreInventoryVO> additionalPages = null;
        int i = 0;

//        do {
//            i++;
//            if (i == 1) {   // first page
//                storeInventory = productsFeignClient.getProducts(250, i);
//                if (storeInventory.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
//                    discordService.sendPasswordPageNotification();
//                }
//            } else {
//                additionalPages = productsFeignClient.getProducts(250, i);
//                Objects.requireNonNull(storeInventory.getBody()).getProducts()
//                        .addAll(Objects.requireNonNull(additionalPages.getBody()).getProducts());
//            }
//        } while (i == 1 || !additionalPages.getBody().getProducts().isEmpty());
        storeInventory = productsFeignClient.getProducts(1, 1);
        storeInventory.getBody().setStoreName(siteUrl);
//        productRepository.saveAll(storeInventory.getBody().getProducts());
//        for (final Product p: storeInventory.getBody().getProducts()) {
//            variantRepository.saveAll(p.getVariants());
//        }


        log.debug("Store Inventory: {}", storeInventory);
        log.info("Products size: {}", storeInventory.getBody().getProducts().size());

        return storeInventory.getBody();
    }
}
