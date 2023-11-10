package com.flowiee.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.app.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table (name = "account_role")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccountRole extends BaseEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "module", nullable = false)
	@NotNull
	private String module;

	@Column(name = "action", nullable = false)
	@NotNull
	private String action;

	@Column(name = "account_id", nullable = false)
	@NotNull
	private Integer accountId;
}