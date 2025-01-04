package com.flowiee.pms.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.entity.BaseEntity;

import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.system.FileStorage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
@Entity
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends BaseEntity implements Serializable {
    @Serial
	static final long serialVersionUID = 1L;

    @Column(name = "PID", nullable = false)
    String PID;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    Category productType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    Category brand;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    Category unit;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column(name = "origin_country", length = 20)
    String originCountry;

    @Column(name = "release_date")
    LocalDate releaseDate;

    @Column(name = "gender", length = 1)
    String gender;

    @Column(name = "storage_instructions")
    String storageInstructions;

    @Column(name = "uv_protection")
    String uvProtection;

    @Column(name = "is_machine_washable")
    Boolean isMachineWashable;

    @Column(name = "is_sale_off")
    Boolean isSaleOff;

    @Column(name = "is_hot_trend")
    Boolean isHotTrend;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    ProductDescription productDescription;

    @Column(name = "notes")
    String internalNotes;

    @Column(name = "status", nullable = false, length = 10)
    String status;

    @JsonIgnore
    @JsonIgnoreProperties("product")
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<ProductDetail> listVariants;

    @JsonIgnore
    @JsonIgnoreProperties("product")
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<FileStorage> listImages;

    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<ProductHistory> listProductHistories;

    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<ProductReview> listProductPreviews;

    @JsonIgnore
    @OneToMany(mappedBy = "productBase", fetch = FetchType.LAZY)
    List<ProductPrice> listProductBasePrice;

    @JsonIgnore
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<ProductDamaged> productDamagedList;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductRelated> ProductRelatedList;

    public Product(long id) {
        super.id = id;
    }

    public Product(long id, String name) {
        super.id = id;
        this.productName = name;
    }

    public ProductPrice getPrice(Long pPriceId) {
        if (getListProductBasePrice() != null) {
            for (ProductPrice price : getListProductBasePrice()) {
                Long lvPriceId = price.getId();
                if (lvPriceId.equals(pPriceId))
                    return price;
            }
            return null;
        }
        return null;
    }

    public ProductPrice getPrice() {
        if (getListProductBasePrice() != null) {
            for (ProductPrice price : getListProductBasePrice()) {
                if (ProductPrice.STATE_ACTIVE.equals(price.getState()))
                    return price;
            }
            return null;
        }
        return null;
    }

    public FileStorage getImage(Long pImageId) {
        if (getListImages() != null) {
            return getListImages().stream()
                    .filter(image -> image.getId().equals(pImageId))
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    public FileStorage getImage() {
        if (getListImages() != null) {
            for (FileStorage image : getListImages()) {
                if (image.isActive())
                    return image;
            }
            return null;
        }
        return null;
    }

    public boolean isClothes() {
        return com.flowiee.pms.utils.constants.PID.CLOTHES.getId().equals(PID);
    }

    public boolean isFruit() {
        return com.flowiee.pms.utils.constants.PID.FRUIT.getId().equals(PID);
    }

    public boolean isSouvenir() {
        return com.flowiee.pms.utils.constants.PID.SOUVENIR.getId().equals(PID);
    }

	@Override
	public String toString() {
		return "Product [id=" + super.id + ", productType=" + productType + ", brand=" + brand + ", productName=" + productName + ", unit=" + unit + ", status=" + status + "]";
	}
}