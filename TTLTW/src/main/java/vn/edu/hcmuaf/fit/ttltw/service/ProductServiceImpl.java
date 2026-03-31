package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.ProductDao;
import vn.edu.hcmuaf.fit.ttltw.dao.ProductDaoImpl;
import vn.edu.hcmuaf.fit.ttltw.dao.ProductUserDao;
import vn.edu.hcmuaf.fit.ttltw.dao.ProductUserDaoImpl;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static vn.edu.hcmuaf.fit.ttltw.config.DBConnect.getJdbi;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao = new ProductDaoImpl();

    private final ProductUserDao productUseDao = new ProductUserDaoImpl();
    @Override
    public List<Map<String, Object>> getForAdmin(
            String keyword,
            Integer status,
            Integer categoryId,
            int page,
            int limit) {

        int offset = (page - 1) * limit;

        List<Integer> vcIds = productDao.findVariantIdsForAdmin(
                keyword, status, categoryId, offset, limit);

        if (vcIds.isEmpty())
            return List.of();

        return productDao.findForAdminByVariantIds(vcIds);

    }

    @Override
    public int countForAdmin(String keyword, Integer status, Integer categoryId) {
        return productDao.countForAdmin(keyword, status, categoryId);
    }

    @Override
    public boolean toggleStatus(int productId) {
        return productDao.toggleStatus(productId);
    }

    @Override
    public void addProduct(Product product,
            String[] techNames, String[] techValues, String[] techPriorities,
            String[] variantNames, String[] basePrices,
            String[] quantities, String[] variantQuantities,
            String[] skus, String[] colorVariantIndexes,
            String[] colorIds, String[] customColors, String[] colorPrices, Map<String, List<Image>> colorImagesMap)
            throws Exception {

        // bắt lỗi dữ liệu đầu vào
        if (variantNames == null || variantNames.length == 0) {
            throw new Exception("Sản phẩm phải có ít nhất 1 phiên bản");
        }

        for (String v : variantNames) {
            if (v == null || v.trim().isEmpty()) {
                throw new Exception("Tên phiên bản không được để trống");
            }
        }

        getJdbi().useTransaction(handle -> {

            Brand brand = product.getBrand();

            if (brand != null && brand.getId() == null) {

                String brandName = brand.getName().trim();

                //  kiểm tra trùng
                Brand existed = productDao.findBrandByName(handle, brandName);

                if (existed != null) {
                    brand.setId(existed.getId());
                    System.out.println("USE EXIST BRAND: " + brandName);
                } else {
                    int brandId = productDao.insertBrand(handle, brand);
                    brand.setId(brandId);
                    System.out.println("INSERT NEW BRAND: " + brandName);
                }
            }
            int productId = productDao.insertProduct(handle, product);

            if (techNames != null) {
                for (int i = 0; i < techNames.length; i++) {
                    if (techNames[i] == null || techNames[i].isBlank())
                        continue;
                    TechSpecs t = new TechSpecs();
                    t.setName(techNames[i]);
                    t.setValue(techValues[i]);
                    t.setPriority(parseInt(techPriorities[i]));
                    productDao.insertTechSpec(handle, productId, t);
                }
            }

            if (variantNames != null) {
                for (int i = 0; i < variantNames.length; i++) {

                    ProductVariant v = new ProductVariant();
                    v.setName(variantNames[i]);
                    v.setBasePrice(parseDbl(basePrices != null ? basePrices[i] : "0"));
                    v.setStatus(1);
                    v.setCreatedAt(LocalDateTime.now());

                    int variantId = productDao.insertVariant(handle, productId, v);

                    // Lấy categoryId từ object product truyền vào
                    int categoryId = product.getCategory().getId();

                    if (categoryId == 1) {
                        // --- TRƯỜNG HỢP ĐIỆN THOẠI ---
                        if (colorIds != null && colorVariantIndexes != null) {
                            for (int j = 0; j < colorIds.length; j++) {
                                int belongsToVariant = parseInt(colorVariantIndexes[j]);

                                if (belongsToVariant == i) {
                                    VariantColor vc = new VariantColor();
                                    if ("custom".equals(colorIds[j])) {
                                        Color c = new Color();
                                        c.setName(customColors != null ? customColors[j] : "Màu mới");
                                        vc.setColor(c);
                                    } else {
                                        vc.setColor(new Color(parseInt(colorIds[j])));
                                    }
                                    vc.setPrice(parseDbl(colorPrices != null ? colorPrices[j] : "0"));
                                    vc.setQuantity(parseInt(quantities != null ? quantities[j] : "0"));
                                    vc.setCreatedAt(LocalDateTime.now());
                                    if (skus != null && j < skus.length) {
                                        String sku = skus[j];
                                        if (sku == null || sku.trim().isEmpty()) {
                                            vc.setSku(null); // cho phép SKU null nếu trống
                                        } else {
                                            vc.setSku(sku.trim());
                                        }
                                    }
                                    int variantColorId = productDao.insertVariantColor(handle, variantId, vc);
                                    String key = i + "_" + j;

                                    if (colorImagesMap != null && colorImagesMap.containsKey(key)) {

                                        boolean isFirst = true;

                                        for (Image img : colorImagesMap.get(key)) {

                                            img.setMain(isFirst);
                                            isFirst = false;

                                            System.out.println(
                                                    "Insert image vcId=" + variantColorId +
                                                            " key=" + key +
                                                            " path=" + img.getImgPath());

                                            productDao.insertVariantColorImage(handle, variantColorId, img);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- TRƯỜNG HỢP LINH KIỆN ---
                        double basePrice = parseDbl(basePrices != null ? basePrices[i] : "0");
                        VariantColor vc = new VariantColor();
                        vc.setColor(new Color(1)); // Màu mặc định
                        vc.setPrice(basePrice);
                        vc.setQuantity(parseInt(variantQuantities != null ? variantQuantities[i] : "0"));
                        vc.setCreatedAt(LocalDateTime.now());
                        productDao.insertVariantColor(handle, variantId, vc);

                    }
                }
            }

        });
    }

    private int parseInt(String s) {
        if (s == null || s.trim().isEmpty())
            return 0;
        return Integer.parseInt(s.trim());
    }

    private double parseDbl(String s) {
        if (s == null || s.trim().isEmpty())
            return 0.0;
        return Double.parseDouble(s.trim());
    }

    @Override
    public Map<String, Object> getProductForEditByVariantColorId(int vcId) {
        Map<String, Object> product = productDao.findProductByVariantColorId(vcId);
        int productId = (int) product.get("product_id");

        Map<String, Object> detail = (productDao).findVariantColorDetailForEdit(vcId);

        product.putAll(detail);

        product.put("techs", productDao.findTechByProductId(productId));

        List<Map<String, Object>> variants = productDao.findVariantsByProductId(productId);
        for (Map<String, Object> v : variants) {
            v.put("colors", productDao.findColorsByVariantId((int) v.get("variant_id")));
        }
        product.put("variants", variants);

        return product;
    }

    @Override
    public List<Map<String, Object>> getProductsForList() {
        return ((ProductDaoImpl) productDao).getProductsForListDisplay();
    }

    @Override
    public List<Map<String, Object>> getProductsByCategory(int categoryId) {
        return ((ProductDaoImpl) productDao).getProductsByCategory(categoryId);
    }

    @Override
    public void updateProduct(Product product, String[] techNames, String[] techValues, String[] techPriorities,
            String[] vNames, String[] bPrices, String[] vIds, String[] cIds,
            String[] skus, String[] qtys, String[] cPrices) throws Exception {

        getJdbi().useTransaction(handle -> {
            productDao.updateProductBasic(handle, product);

            productDao.deleteTechSpecsByProductId(handle, product.getId());
            if (techNames != null) {
                for (int i = 0; i < techNames.length; i++) {
                    // Kiểm tra nếu tên thông số trống thì bỏ qua không lưu
                    if (techNames[i] == null || techNames[i].isBlank())
                        continue;
                    TechSpecs t = new TechSpecs();
                    t.setName(techNames[i]);
                    t.setValue(techValues[i]);
                    t.setPriority(Integer.parseInt(techPriorities[i]));
                    productDao.insertTechSpec(handle, product.getId(), t);
                }
            }

            int categoryId = product.getCategory().getId();

            if (categoryId == 1) {
                int vId = Integer.parseInt(vIds[0]);
                int vcId = Integer.parseInt(cIds[0]);
                double baseP = Double.parseDouble(bPrices[0]);
                double colorP = Double.parseDouble(cPrices[0]);
                int quantity = Integer.parseInt(qtys[0]);

                productDao.updateVariant(handle, vId, vNames[0], baseP);
                productDao.updateVariantColor(handle, vcId, quantity, skus[0], colorP);
            } else {
                if (vIds != null) {
                    for (int i = 0; i < vIds.length; i++) {
                        int vId = Integer.parseInt(vIds[i]);
                        int vcId = Integer.parseInt(cIds[i]);
                        double price = Double.parseDouble(bPrices[i]);
                        int quantity = Integer.parseInt(qtys[i]);

                        productDao.updateVariant(handle, vId, "Default", price);
                        String sku = (skus != null && skus.length > i) ? skus[i] : null;
                        productDao.updateVariantColor(handle, vcId, quantity, sku, price);
                        System.out.println("CATEGORY = " + categoryId);
                        System.out.println("vIds=" + Arrays.toString(vIds));
                        System.out.println("bPrices=" + Arrays.toString(bPrices));
                        System.out.println("qtys=" + Arrays.toString(qtys));

                    }
                }
            }
        });
    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryWithFilters(int categoryId, Double priceMin, Double priceMax,
            List<String> memory,
            List<String> colors, Integer year, String brandName, List<String> types,
            String condition, String sortBy) {
        return ((ProductDaoImpl) productDao).getProductsByCategoryWithFilters(
                categoryId, priceMin, priceMax, memory, colors, year, brandName, types, condition, sortBy);
    }

    @Override
    public List<Map<String, Object>> getAccessories() {
        return ((ProductDaoImpl) productDao).getAccessories();
    }

    @Override
    public List<Map<String, Object>> getAccessoriesWithFilters(Double priceMin, Double priceMax, String brandName,
            List<String> types, String condition, String sortBy) {
        return ((ProductDaoImpl) productDao).getAccessoriesWithFilters(
                priceMin, priceMax, brandName, types, condition, sortBy);
    }



    @Override
    public List<Map<String, Object>> getRelatedProducts(
            int brandId,
            int excludeProductId, int limit) {

        List<Map<String, Object>> products = productDao.findRelatedBySameBrand(brandId, excludeProductId, limit);

        if (products.size() < limit) {
            int remain = limit - products.size();

            List<Integer> existedIds = products.stream()
                    .map(p -> (Integer) p.get("id"))
                    .toList();

            List<Map<String, Object>> fallback = productDao.findFallbackRelatedProducts(
                    excludeProductId,
                    existedIds,
                    remain);

            products.addAll(fallback);
        }

        return products;
    }

    @Override
    public List<String> getSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return productUseDao.getSuggestions(keyword);

    }

    @Override
    public List<Map<String, Object>> getProductsByCategoryPaginated(
            int categoryId, Double priceMin, Double priceMax, List<String> memory, List<String> colors,
            Integer year, String brandName, String sortBy, int page, int pageSize, String search) {
        // Gọi DAO method (đã có LIMIT + OFFSET)
        return productDao.getProductsByCategoryPaginated(
                categoryId, priceMin, priceMax,
                memory, colors, year, brandName,
                sortBy, page, pageSize, search);
    }

    @Override
    public int countProductsByCategory(int categoryId, Double priceMin, Double priceMax, List<String> memory,
            List<String> colors, Integer year, String brandName, String search) {
        // Gọi DAO method
        return productDao.countProductsByCategory(
                categoryId, priceMin, priceMax,
                memory, colors, year, brandName, search);
    }

    @Override
    public List<Map<String, Object>> getAccessoryCategories() {
        return productDao.getAccessoryCategories();
    }
    public List<Brand> getAll() {
        return productDao.getAll();
    }

}
