package com.flowiee.pms.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.base.BaseEntity;

import com.flowiee.pms.entity.sales.TicketImport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "material_temp")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaterialTemp extends BaseEntity implements Serializable {
    @Serial
	private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_import_id", nullable = false)
    private TicketImport ticketImport;

	@Column(name = "material_id", nullable = false)
	private Integer materialId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "note")
    private String note;
}