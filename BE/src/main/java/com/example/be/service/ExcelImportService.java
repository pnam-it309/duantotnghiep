package com.example.be.service;

import com.example.be.entity.*;
import com.example.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final DiscountRepository discountRepository;

    // In a real app, you might inject data types for password encoding
    // private final PasswordEncoder passwordEncoder;

    public void importBrands(MultipartFile file) throws IOException {
        List<Brand> brands = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header
                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    brands.add(Brand.builder().name(nameCell.getStringCellValue()).build());
                }
            }
        }
        brandRepository.saveAll(brands);
    }

    public void importCategories(MultipartFile file) throws IOException {
        List<Category> categories = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                Cell nameCell = row.getCell(0);
                if (nameCell != null) {
                    categories.add(Category.builder().name(nameCell.getStringCellValue()).build());
                }
            }
        }
        categoryRepository.saveAll(categories);
    }

    public void importColors(MultipartFile file) throws IOException {
        List<com.example.be.entity.Color> colors = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                String name = getStringValue(row.getCell(0));
                String hex = getStringValue(row.getCell(1));
                if (name != null) {
                    colors.add(com.example.be.entity.Color.builder().colorName(name).hexCode(hex).build());
                }
            }
        }
        colorRepository.saveAll(colors);
    }

    public void importSizes(MultipartFile file) throws IOException {
        List<Size> sizes = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                String val = getStringValue(row.getCell(0));
                if (val != null) {
                    sizes.add(Size.builder().sizeValue(val).build());
                }
            }
        }
        sizeRepository.saveAll(sizes);
    }

    public void importUsers(MultipartFile file) throws IOException {
        List<User> users = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header
                // Username, Password, Email
                String username = getStringValue(row.getCell(0));
                String password = getStringValue(row.getCell(1));
                String email = getStringValue(row.getCell(2));

                if (username != null) {
                    users.add(User.builder()
                            .username(username)
                            .password(password) // Plain text for now, should encode in real app
                            .email(email)
                            .build());
                }
            }
        }
        userRepository.saveAll(users);
    }

    public void importCoupons(MultipartFile file) throws IOException {
        List<Coupon> coupons = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                // Code, DiscountAmount, MinOrder, Expiry
                String code = getStringValue(row.getCell(0));
                BigDecimal amount = getBigDecimalValue(row.getCell(1));
                Integer minOrder = getIntegerValue(row.getCell(2));
                LocalDateTime expiry = getDateValue(row.getCell(3));

                if (code != null) {
                    coupons.add(Coupon.builder()
                            .code(code)
                            .discountAmount(amount)
                            .minOrderValue(minOrder)
                            .expiryDate(expiry)
                            .build());
                }
            }
        }
        couponRepository.saveAll(coupons);
    }

    public void importProducts(MultipartFile file) throws IOException {
        List<Product> products = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                // Name, Slug, Desc, CategoryID, BrandID, Active
                String name = getStringValue(row.getCell(0));
                String slug = getStringValue(row.getCell(1));
                String desc = getStringValue(row.getCell(2));
                Long catId = getLongValue(row.getCell(3));
                Long brandId = getLongValue(row.getCell(4));
                Boolean active = getBooleanValue(row.getCell(5));

                if (name != null) {
                    Category cat = catId != null ? categoryRepository.findById(catId).orElse(null) : null;
                    Brand brand = brandId != null ? brandRepository.findById(brandId).orElse(null) : null;

                    products.add(Product.builder()
                            .name(name)
                            .slug(slug)
                            .description(desc)
                            .category(cat)
                            .brand(brand)
                            .active(active != null ? active : true)
                            .build());
                }
            }
        }
        productRepository.saveAll(products);
    }

    public void importProductVariants(MultipartFile file) throws IOException {
        List<ProductVariant> variants = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                // ProductID, SizeID, ColorID, Price, Stock, SKU
                Long prodId = getLongValue(row.getCell(0));
                Long sizeId = getLongValue(row.getCell(1));
                Long colorId = getLongValue(row.getCell(2));
                BigDecimal price = getBigDecimalValue(row.getCell(3));
                Integer stock = getIntegerValue(row.getCell(4));
                String sku = getStringValue(row.getCell(5));

                if (prodId != null && stock != null) {
                    Product p = productRepository.findById(prodId).orElse(null);
                    Size s = sizeId != null ? sizeRepository.findById(sizeId).orElse(null) : null;
                    com.example.be.entity.Color c = colorId != null ? colorRepository.findById(colorId).orElse(null)
                            : null;

                    if (p != null) {
                        variants.add(ProductVariant.builder()
                                .product(p)
                                .size(s)
                                .color(c)
                                .price(price)
                                .stockQuantity(stock)
                                .sku(sku)
                                .build());
                    }
                }
            }
        }
        productVariantRepository.saveAll(variants);
    }

    public void importDiscounts(MultipartFile file) throws IOException {
        List<Discount> discounts = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                // ProductID, Percent, Start, End, Active
                Long prodId = getLongValue(row.getCell(0));
                BigDecimal percent = getBigDecimalValue(row.getCell(1));
                LocalDateTime start = getDateValue(row.getCell(2));
                LocalDateTime end = getDateValue(row.getCell(3));
                Boolean active = getBooleanValue(row.getCell(4));

                if (prodId != null) {
                    Product p = productRepository.findById(prodId).orElse(null);
                    if (p != null) {
                        discounts.add(Discount.builder()
                                .product(p)
                                .discountPercent(percent)
                                .startDate(start)
                                .endDate(end)
                                .active(active)
                                .build());
                    }
                }
            }
        }
        discountRepository.saveAll(discounts);
    }

    // Helper methods
    private String getStringValue(Cell cell) {
        if (cell == null)
            return null;
        CellType type = cell.getCellType();
        if (type == CellType.STRING)
            return cell.getStringCellValue();
        if (type == CellType.NUMERIC)
            return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private Long getLongValue(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC)
            return (long) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Long.parseLong(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getIntegerValue(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC)
            return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC)
            return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Boolean getBooleanValue(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.BOOLEAN)
            return cell.getBooleanCellValue();
        if (cell.getCellType() == CellType.STRING)
            return Boolean.parseBoolean(cell.getStringCellValue());
        return null;
    }

    private LocalDateTime getDateValue(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        // Handle string dates if needed, e.g. ISO 8601
        return null;
    }
}
