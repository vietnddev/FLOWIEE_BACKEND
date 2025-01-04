package com.flowiee.pms.repository.sales;

import com.flowiee.pms.entity.sales.Order;

import com.flowiee.pms.utils.constants.CategoryType;
import com.flowiee.pms.utils.constants.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "select distinct o from Order o " +
            "left join o.customer c " +
            "left join o.kenhBanHang sc on sc.type = 'SALES_CHANNEL' " +
            "left join o.paymentMethod pm on pm.type = 'PAYMENT_METHOD' " +
            "left join FileStorage f on f.order.id = o.id " +
            "left join Account a on a.id = o.createdBy " +
            "where (:txtSearch is null or (o.receiverName like %:txtSearch%)) " +
            "and (:orderId is null or o.id=:orderId) " +
            "and (:paymentMethodId is null or o.paymentMethod.id=:paymentMethodId) " +
            "and (:orderStatus is null or o.orderStatus = :orderStatus) " + // So sánh trực tiếp với enum
            "and (:salesChannelId is null or o.kenhBanHang.id=:salesChannelId) " +
            "and (:sellerId is null or o.nhanVienBanHang.id=:sellerId) " +
            "and (:customerId is null or o.customer.id=:customerId) " +
            "and (:branchId is null or a.branch.id = :branchId) " +
            "and (:groupCustomerId is null or 1=1) " +
            "and ((trunc(o.orderTime) >= trunc(:orderTimeFrom)) and (trunc(o.orderTime) <= trunc(:orderTimeTo)))")
    Page<Order> findAll(@Param("txtSearch") String txtSearch,
                        @Param("orderId") Long orderId,
                        @Param("paymentMethodId") Long paymentMethodId,
                        @Param("orderStatus") OrderStatus orderStatus,
                        @Param("salesChannelId") Long salesChannelId,
                        @Param("sellerId") Long sellerId,
                        @Param("customerId") Long customerId,
                        @Param("branchId") Long branchId,
                        @Param("groupCustomerId") Long groupCustomerId,
                        @Param("orderTimeFrom") LocalDateTime orderTimeFrom,
                        @Param("orderTimeTo") LocalDateTime orderTimeTo,
                        Pageable pageable);

    @Query(value = "select sum((select sum(d.price * d.quantity) from order_detail d where d.order_id = o.id) - o.amount_discount) from orders o where trunc(o.order_time) = trunc(current_date)", nativeQuery = true)
    Double findRevenueToday();

//    @Query("select nvl(sum(o.totalAmountDiscount), 0) from Order o where extract(month from o.thoiGianDatHang) = extract(month from current_date)")
//    Double findRevenueThisMonth();

    @Query("from Order o where trunc(o.orderTime) = trunc(current_date)")
    List<Order> findOrdersToday();

    @Modifying
    @Query("update Order set paymentTime=:paymentTime, paymentMethod.id=:paymentMethod, paymentAmount=:paymentAmount, paymentNote=:paymentNote, paymentStatus = true where id=:orderId")
    void updatePaymentStatus(@Param("orderId") Long orderId, @Param("paymentTime") LocalDateTime paymentTime, @Param("paymentMethod") Long paymentMethod, @Param("paymentAmount") BigDecimal paymentAmount, @Param("paymentNote") String paymentNote);

//    @Query("select " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 1 then o.totalAmountDiscount else 0 end) as JAN, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 2 then o.totalAmountDiscount else 0 end) as FEB, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 3 then o.totalAmountDiscount else 0 end) as MAR, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 4 then o.totalAmountDiscount else 0 end) as APRIL, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 5 then o.totalAmountDiscount else 0 end) as MAY, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 6 then o.totalAmountDiscount else 0 end) as JUN, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 7 then o.totalAmountDiscount else 0 end) as JUL, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 8 then o.totalAmountDiscount else 0 end) as AUG, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 9 then o.totalAmountDiscount else 0 end) as SEP, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 10 then o.totalAmountDiscount else 0 end) as OCT, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 11 then o.totalAmountDiscount else 0 end) as NOV, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 12 then o.totalAmountDiscount else 0 end) as DEC " +
//           "from Order o " +
//           "where extract(year from o.thoiGianDatHang) = extract(year from current_date)")
//    List<Object[]> findRevenueEachMonthOfYear();

    @Modifying
    @Query("update Order set ticketExport.id=:ticketExportId where id=:orderId")
    void updateTicketExportInfo(@Param("orderId") Long orderId, @Param("ticketExportId") Long ticketExportId);

    @Query("from Order where ticketExport.id=:ticketExportId")
    Order findByTicketExport(@Param("ticketExportId") Long ticketExportId);

    @Query("select " +
           "    extract(year from o.orderTime) AS year, " +
           "    extract(month from o.orderTime) AS month, " +
           "    count(o.id) AS order_count, " +
           "    nvl(sum(od.price - o.amountDiscount), 0) AS avgValue " +
           "from Order o " +
           "left join OrderDetail od on od.order.id = o.id " +
           "where 1=1 " +
           "    and o.customer.id=:customerId " +
           "    and extract(year from o.orderTime) = :year " +
           "    and (:month is null or (extract(month from o.orderTime) = :month)) " +
           "group by extract(year from o.orderTime), extract(month from o.orderTime) " +
           "order by extract(year from o.orderTime), extract(month from o.orderTime)")
    List<Object[]> findPurchaseHistory(@Param("customerId") Long customerId, @Param("year") Integer year, @Param("month") Integer month);

    @Query("from Order where code = :orderCode")
    Order findByOrderCode(@Param("orderCode") String orderCode);

    @Query("from Order where cancellationReason = :cancellationReason")
    List<Order> findByCancellationReason(Long cancellationReason);

    @Query("from Order where orderStatus in (:orderStatus)")
    List<Order> findByOrderStatus(List<OrderStatus> orderStatus);

    @Query("from Order where customer.id = :customerId")
    List<Order> findByCustomer(@Param("customerId") Long customerId);
}