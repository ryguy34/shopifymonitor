package com.shopify.monitor.shopifymonitor.service;

import com.shopify.monitor.shopifymonitor.api.vo.ProductVO;
import com.shopify.monitor.shopifymonitor.api.vo.VariantVO;
import com.shopify.monitor.shopifymonitor.mappings.ShopifyProductMapper;
import com.shopify.monitor.shopifymonitor.mappings.ShopifyVariantMapper;
import com.shopify.monitor.shopifymonitor.persistance.model.Product;
import com.shopify.monitor.shopifymonitor.persistance.model.Variant;
import com.shopify.monitor.shopifymonitor.persistance.repository.ProductRepository;
import com.shopify.monitor.shopifymonitor.persistance.repository.VariantRepository;
import com.shopify.monitor.shopifymonitor.utility.ShopifyUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UpdateVariants {

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopifyUtility shopifyUtility;

    @Autowired
    private ShopifyProductMapper shopifyProductMapper;

    @Autowired
    private ShopifyVariantMapper shopifyVariantMapper;

    @Async
    public CompletableFuture<Void> updateVariants(ProductVO p, String siteName) {
        // TODO: clean up method
        String productId = p.getId();

        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            // is the product completely new
            log.info("*** New product found: {} ***", p.getTitle());
            Product mappedProduct = shopifyProductMapper.mapProduct(p, siteName);
            productRepository.save(mappedProduct);

            List<Variant> newProductVariants = shopifyVariantMapper.map(p.getVariants());
            newProductVariants = shopifyUtility.cleanVariantData(newProductVariants);
            variantRepository.saveAll(newProductVariants);

            // TODO: send discord notification
        } else {
            List<VariantVO> currentStoreVariants = p.getVariants();
            List<Variant> savedProductVariants = variantRepository.findAllByProductId(productId);

            for (final VariantVO currentStoreVariant : currentStoreVariants) {
                for (Variant savedVariant : savedProductVariants) {
                    if (currentStoreVariant.getId().equals(savedVariant.getId())) {
                        if (Boolean.TRUE.equals(currentStoreVariant.getAvailable()) && Boolean.FALSE.equals(savedVariant.getAvailable())) {
                            // restocked
                            log.info("*** Restocked item -> title: {} productId: {} ***", savedVariant.getTitle(), productId);
                            savedVariant.setAvailable(true);
                            savedVariant.setUpdatedAt(currentStoreVariant.getUpdatedAt());
                            savedVariant = shopifyUtility.cleanVariantData(savedVariant);
                            variantRepository.save(savedVariant);

                            // TODO: send discord notification
                        } else if (Boolean.FALSE.equals(currentStoreVariant.getAvailable()) && Boolean.TRUE.equals(savedVariant.getAvailable())) {
                            // out of stock
                            log.info("*** OOS -> title: {} productId: {} ***", savedVariant.getTitle(), productId);
                            savedVariant.setAvailable(false);
                            savedVariant.setUpdatedAt(currentStoreVariant.getUpdatedAt());
                            variantRepository.save(savedVariant);
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
