package com.flowiee.pms.entity.sales;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.entity.BaseEntity;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.entity.product.ProductReview;
import com.flowiee.pms.model.dto.CustomerDTO;

import com.flowiee.pms.utils.constants.ContactType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Entity
@Table(name = "customer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer extends BaseEntity implements Serializable {
	@Serial
	static final long serialVersionUID = 1L;

	@Column(name = "customer_name", length = 100, nullable = false)
	String customerName;

	@JsonFormat(pattern = "dd/MM/yyyy")
	@Column(name = "birthday")
	LocalDate dateOfBirth;

	@Column(name = "sex", nullable = false)
	boolean sex;

	@Column(name = "marital_status")
	String maritalStatus;

	@Column(name = "referral_source")
	String referralSource;

	@Column(name = "black_list")
	Boolean isBlackList;

	@Column(name = "black_list_reason")
	String blackListReason;

	@Column(name = "bonus_points")
	Integer bonusPoints;

	@Column(name = "has_outstanding_balance")
	Boolean hasOutstandingBalance;

	@Column(name = "outstanding_balance_amount")
	BigDecimal outstandingBalanceAmount;

	@Column(name = "is_vip")
	Boolean isVIP;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_customer_id")
	Category groupCustomer;

	@JsonIgnore
	@JsonIgnoreProperties("customer")
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	List<Order> listOrder;

	@JsonIgnore
	@JsonIgnoreProperties("customer")
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	List<ProductReview> listProductReviews;

	@JsonIgnore
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	List<CustomerContact> listCustomerContact;

	@JsonIgnore
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	List<LoyaltyTransaction> loyaltyTransactionList;

	@JsonIgnore
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	List<GiftRedemption> giftRedemptionList;

	@JsonIgnore
	@OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
	List<CustomerDebt> customerDebtList;

	public Customer(long id) {
		super.id = id;
	}

	public Customer(long id, String customerName) {
		super.id = id;
		this.customerName = customerName;
	}

	public static Customer fromCustomerDTO(CustomerDTO dto) {
		Customer customer = Customer.builder()
			.customerName(dto.getCustomerName())
			.dateOfBirth(dto.getDateOfBirth())
			.sex(dto.isSex())
			.build();
		customer.setId(dto.getId());
		return customer;
	}

	public CustomerContact getContact(ContactType contactType) {
		if (getListCustomerContact() != null) {
			for (CustomerContact contact : getListCustomerContact()) {
				if (contactType.name().equals(contact.getCode()))
					return contact;
			}
			return null;
		}
		return null;
	}

	public String getContactPhone() {
		CustomerContact contact = getContact(ContactType.P);
		return contact != null ? contact.getValue() : null;
	}

	public String getContactEmail() {
		CustomerContact contact = getContact(ContactType.E);
		return contact != null ? contact.getValue() : null;
	}

	public String getContactAddress() {
		CustomerContact contact = getContact(ContactType.A);
		return contact != null ? contact.getValue() : null;
	}

	@Override
	public String toString() {
		return "Customer [id=" + super.id + ", customerName=" + customerName + ", birthday=" + dateOfBirth + ", sex=" + sex  + "]";
	}
}