package com.example.be.util;

import com.example.be.dto.*;
import com.example.be.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    // --- Brand ---
    public BrandDTO toBrandDTO(Brand brand) {
        if (brand == null)
            return null;
        BrandDTO dto = new BrandDTO();
        BeanUtils.copyProperties(brand, dto);
        return dto;
    }

    public Brand toBrandEntity(BrandDTO dto) {
        if (dto == null)
            return null;
        Brand brand = new Brand();
        BeanUtils.copyProperties(dto, brand);
        return brand;
    }

    // --- Category ---
    public CategoryDTO toCategoryDTO(Category category) {
        if (category == null)
            return null;
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    public Category toCategoryEntity(CategoryDTO dto) {
        if (dto == null)
            return null;
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        return category;
    }

    // --- Color ---
    public ColorDTO toColorDTO(Color color) {
        if (color == null)
            return null;
        ColorDTO dto = new ColorDTO();
        BeanUtils.copyProperties(color, dto);
        return dto;
    }

    public Color toColorEntity(ColorDTO dto) {
        if (dto == null)
            return null;
        Color color = new Color();
        BeanUtils.copyProperties(dto, color);
        return color;
    }

    // --- Size ---
    public SizeDTO toSizeDTO(Size size) {
        if (size == null)
            return null;
        SizeDTO dto = new SizeDTO();
        BeanUtils.copyProperties(size, dto);
        return dto;
    }

    public Size toSizeEntity(SizeDTO dto) {
        if (dto == null)
            return null;
        Size size = new Size();
        BeanUtils.copyProperties(dto, size);
        return size;
    }

    // --- User ---
    public UserDTO toUserDTO(User user) {
        if (user == null)
            return null;
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // Password intentionally skipped
        return dto;
    }

    // Reverse mapping for User (usually handled with specific registration DTOs,
    // but basic supported here)
    public User toUserEntity(UserDTO dto) {
        if (dto == null)
            return null;
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return user;
    }

    // --- Coupon ---
    public CouponDTO toCouponDTO(Coupon coupon) {
        if (coupon == null)
            return null;
        CouponDTO dto = new CouponDTO();
        BeanUtils.copyProperties(coupon, dto);
        return dto;
    }

    public Coupon toCouponEntity(CouponDTO dto) {
        if (dto == null)
            return null;
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        return coupon;
    }

    // --- Discount ---
    public DiscountDTO toDiscountDTO(Discount discount) {
        if (discount == null)
            return null;
        DiscountDTO dto = new DiscountDTO();
        BeanUtils.copyProperties(discount, dto);
        if (discount.getProduct() != null) {
            dto.setProductId(discount.getProduct().getId());
            dto.setProductName(discount.getProduct().getName());
        }
        return dto;
    }

    // --- Product Image ---
    public ProductImageDTO toProductImageDTO(ProductImage image) {
        if (image == null)
            return null;
        ProductImageDTO dto = new ProductImageDTO();
        BeanUtils.copyProperties(image, dto);
        if (image.getProduct() != null) {
            dto.setProductId(image.getProduct().getId());
        }
        return dto;
    }

    public ProductImage toProductImageEntity(ProductImageDTO dto) {
        if (dto == null)
            return null;
        ProductImage image = new ProductImage();
        BeanUtils.copyProperties(dto, image);
        // Relationship to be set by service/controller
        return image;
    }

    // --- Product Variant ---
    public ProductVariantDTO toProductVariantDTO(ProductVariant variant) {
        if (variant == null)
            return null;
        ProductVariantDTO dto = new ProductVariantDTO();
        BeanUtils.copyProperties(variant, dto);
        // costPrice copied by BeanUtils
        if (variant.getProduct() != null) {
            dto.setProductId(variant.getProduct().getId());
        }
        if (variant.getSize() != null) {
            dto.setSizeId(variant.getSize().getId());
            dto.setSizeValue(variant.getSize().getSizeValue());
        }
        if (variant.getColor() != null) {
            dto.setColorId(variant.getColor().getId());
            dto.setColorName(variant.getColor().getColorName());
        }
        return dto;
    }

    public ProductVariant toProductVariantEntity(ProductVariantDTO dto) {
        if (dto == null)
            return null;
        ProductVariant variant = new ProductVariant();
        BeanUtils.copyProperties(dto, variant);
        // Relationships to be set by service/controller
        return variant;
    }

    // --- Product ---
    public ProductDTO toProductDTO(Product product) {
        if (product == null)
            return null;
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }
        // Images and variants can be populated separately if not eagerly fetched
        return dto;
    }

    public Product toProductEntity(ProductDTO dto) {
        if (dto == null)
            return null;
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        // Relationships to be set later
        return product;
    }

    // --- Order Item ---
    public OrderItemDTO toOrderItemDTO(OrderItem item) {
        if (item == null)
            return null;
        OrderItemDTO dto = new OrderItemDTO();
        BeanUtils.copyProperties(item, dto);
        if (item.getProductVariant() != null) {
            dto.setProductVariantId(item.getProductVariant().getId());
            if (item.getProductVariant().getProduct() != null) {
                dto.setProductName(item.getProductVariant().getProduct().getName());
            }
            dto.setSku(item.getProductVariant().getSku());
        }
        return dto;
    }

    // --- Order ---
    public OrderDTO toOrderDTO(Order order) {
        if (order == null)
            return null;
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        }
        if (order.getCoupon() != null) {
            dto.setCouponId(order.getCoupon().getId());
            dto.setCouponCode(order.getCoupon().getCode());
            dto.setCouponCode(order.getCoupon().getCode());
        }
        dto.setPointsUsed(order.getPointsUsed());
        dto.setPointsDiscount(order.getPointsDiscount());
        // Items populated separately
        return dto;
    }

    // --- Supplier ---
    public SupplierDTO toSupplierDTO(Supplier supplier) {
        if (supplier == null)
            return null;
        SupplierDTO dto = new SupplierDTO();
        BeanUtils.copyProperties(supplier, dto);
        return dto;
    }

    public Supplier toSupplierEntity(SupplierDTO dto) {
        if (dto == null)
            return null;
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        return supplier;
    }

    // --- Goods Receipt ---
    public GoodsReceiptDTO toGoodsReceiptDTO(GoodsReceipt receipt) {
        if (receipt == null)
            return null;
        GoodsReceiptDTO dto = new GoodsReceiptDTO();
        BeanUtils.copyProperties(receipt, dto);
        if (receipt.getSupplier() != null) {
            dto.setSupplierId(receipt.getSupplier().getId());
            dto.setSupplierName(receipt.getSupplier().getName());
        }
        if (receipt.getUser() != null) {
            dto.setUserId(receipt.getUser().getId());
            dto.setUsername(receipt.getUser().getUsername());
        }
        if (receipt.getDetails() != null) {
            dto.setDetails(receipt.getDetails().stream()
                    .map(this::toGoodsReceiptDetailDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public GoodsReceiptDetailDTO toGoodsReceiptDetailDTO(GoodsReceiptDetail detail) {
        if (detail == null)
            return null;
        GoodsReceiptDetailDTO dto = new GoodsReceiptDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        if (detail.getProductVariant() != null) {
            dto.setProductVariantId(detail.getProductVariant().getId());
            dto.setSku(detail.getProductVariant().getSku());
            // Safe access to product name
            if (detail.getProductVariant().getProduct() != null) {
                dto.setProductVariantName(detail.getProductVariant().getProduct().getName()
                        + " (" + detail.getProductVariant().getSku() + ")");
            }
        }
        return dto;
    }

    public GoodsReceipt toGoodsReceiptEntity(GoodsReceiptDTO dto) {
        if (dto == null)
            return null;
        GoodsReceipt receipt = new GoodsReceipt();
        BeanUtils.copyProperties(dto, receipt);
        return receipt;
    }

    // --- Order Status History ---
    public OrderStatusHistoryDTO toOrderStatusHistoryDTO(OrderStatusHistory history) {
        if (history == null)
            return null;
        OrderStatusHistoryDTO dto = new OrderStatusHistoryDTO();
        BeanUtils.copyProperties(history, dto);
        return dto;
    }

    // --- Return Request ---
    public ReturnRequestDTO toReturnRequestDTO(ReturnRequest request) {
        if (request == null)
            return null;
        ReturnRequestDTO dto = new ReturnRequestDTO();
        BeanUtils.copyProperties(request, dto);
        if (request.getOrder() != null) {
            dto.setOrderId(request.getOrder().getId());
        }
        return dto;
    }
}
