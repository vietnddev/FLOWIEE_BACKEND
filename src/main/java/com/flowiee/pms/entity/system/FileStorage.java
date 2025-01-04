package com.flowiee.pms.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.entity.BaseEntity;

import com.flowiee.pms.entity.product.*;
import com.flowiee.pms.entity.sales.Order;
import com.flowiee.pms.entity.sales.TicketExport;
import com.flowiee.pms.entity.sales.TicketImport;
import com.flowiee.pms.utils.CommonUtils;
import com.flowiee.pms.utils.FileUtils;
import lombok.*;

import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

@Builder
@Entity
@Table(name = "file_storage")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileStorage extends BaseEntity implements Serializable {
    @Serial
	static final long serialVersionUID = 1L;

    public static final String QRCODE = "Q";
    public static final String BARCODE = "B";
    public static final String DOCUMENT = "D";
    public static final String IMAGE = "I";

	@Column(name = "customize_name")
    String customizeName;

    @Column(name = "saved_name", nullable = false)
    String storageName;

    @Column(name = "original_name", nullable = false)
    String originalName;

    @Column(name = "note")
    String note;

    @Column(name = "extension", length = 10)
    String extension;

    @Column(name = "content_type", length = 100)
    String contentType;

    @Column(name = "file_size")
    long fileSize;

    @Column(name = "content")
    byte[] content;

    @Column(name = "content_base_64")
    String contentBase64;

    @Column(name = "directory_path", length = 500)
    String directoryPath;

    @Column(name = "sort")
    Integer sort;

    @Column(name = "status", nullable = false)
    boolean status;

    @Column(name = "module", nullable = false)
    String module;

    @Column(name = "file_type")
    String fileType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    ProductDetail productDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    Material material;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_by", nullable = false)
    Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_import_id")
    TicketImport ticketImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_export_id")
    TicketExport ticketExport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_combo_id")
    ProductCombo productCombo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_damaged_id")
    ProductDamaged productDamaged;

    @Column(name = "is_active", nullable = false)
    boolean isActive;

    @Transient
    String src;

    @JsonIgnore
    @Transient
    MultipartFile fileAttach;

    public FileStorage(MultipartFile file, String pModule, Long productId) {
        try {
            this.module = pModule;
            this.extension = FileUtils.getFileExtension(file.getOriginalFilename());
            this.originalName = file.getOriginalFilename();
            this.customizeName = file.getOriginalFilename();
            this.storageName = FileUtils.genRandomFileName() + "." + this.extension;
            this.fileSize = file.getSize();
            this.contentType = file.getContentType();
            this.directoryPath = CommonUtils.getPathDirectory(pModule).substring(CommonUtils.getPathDirectory(pModule).indexOf("uploads"));
            this.account = new Account(CommonUtils.getUserPrincipal().getId());
            if (productId != null) {
                this.product = new Product(productId);
            }
            this.isActive = false;
            this.fileAttach = file;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public String toString() {
		return "FileStorage [id=" + super.id + ", customizeName=" + customizeName + ", storageName=" + storageName
				+ ", originalName=" + originalName + ", ghiChu=" + note + ", extension=" + extension + ", contentType="
				+ contentType + ", fileSize=" + fileSize + ", content=" + Arrays.toString(content)
				+ ", directoryPath=" + directoryPath + ", sort=" + sort + ", status=" + status + ", module=" + module
				+ ", product=" + product + ", productVariant=" + productDetail + ", account=" + account + ", isActive=" + isActive + "]";
	}        
}