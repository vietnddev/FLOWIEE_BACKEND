package com.flowiee.pms.entity.product;

import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@Table(name = "image_crawled")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImageCrawled {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "path")
    private String path;

    @Column(name = "main_image")
    private boolean mainImage;
}