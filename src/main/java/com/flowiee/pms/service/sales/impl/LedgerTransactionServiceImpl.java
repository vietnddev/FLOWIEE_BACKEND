package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.sales.LedgerTransaction;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.repository.sales.LedgerTransactionRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.LedgerTransactionService;
import com.flowiee.pms.utils.constants.LedgerTranStatus;
import com.flowiee.pms.utils.constants.MasterObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LedgerTransactionServiceImpl extends BaseService implements LedgerTransactionService {
    @Autowired
    LedgerTransactionRepository mvLedgerTransactionRepository;

    @Override
    public List<LedgerTransaction> findAll() {
        return this.findAll(-1, -1, null, null).getContent();
    }

    @Override
    public Page<LedgerTransaction> findAll(int pageSize, int pageNum, LocalDate fromDate, LocalDate toDate) {
        Pageable pageable = Pageable.unpaged();
        if (pageSize >= 0 && pageNum >= 0) {
            pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        }
        Page<LedgerTransaction> ledgerTransactions = mvLedgerTransactionRepository.findAll(getTranType(), null, null, pageable);
        for (LedgerTransaction trans : ledgerTransactions) {
            for (LedgerTranStatus transStatus : LedgerTranStatus.values()) {
                if (transStatus.name().equals(trans.getStatus())) {
                    trans.setStatus(transStatus.getDescription());
                }
            }
            if (trans.getGroupObject() != null) {
                trans.setGroupObjectName(trans.getGroupObject().getName());
            }
            if (trans.getTranContent() != null) {
                trans.setTranContentName(trans.getTranContent().getName());
            }
        }
        return ledgerTransactions;
    }

    @Override
    public Optional<LedgerTransaction> findById(Integer tranId) {
        return mvLedgerTransactionRepository.findById(tranId);
    }

    @Override
    public LedgerTransaction save(LedgerTransaction transaction) {
        Integer lastIndex = mvLedgerTransactionRepository.findLastIndex(getTranType());
        if (ObjectUtils.isEmpty(lastIndex)) {
            lastIndex = 1;
        } else {
            lastIndex += 1;
        }
        if (ObjectUtils.isEmpty(transaction.getTranCode())) {
            transaction.setTranCode(getNextTranCode(lastIndex));
        }
        transaction.setTranType(getTranType());
        transaction.setTranIndex(lastIndex);
        transaction.setStatus(LedgerTranStatus.COMPLETED.name());
        LedgerTransaction transactionSaved = mvLedgerTransactionRepository.save(transaction);

        String logTitle = "Thêm mới phiếu thu";
        ACTION logFunc = ACTION.SLS_RCT_C;
        if (transactionSaved.getTranType().equals("PC")) {
            logTitle = "Thêm mới phiếu chi";
            logFunc = ACTION.SLS_PMT_C;
        }
        systemLogService.writeLogCreate(MODULE.PRODUCT, logFunc, MasterObject.LedgerTransaction, logTitle, transactionSaved.getGroupObject().getName());

        return transactionSaved;
    }

    @Override
    public LedgerTransaction update(LedgerTransaction transaction, Integer tranId) {
        transaction.setId(tranId);
        transaction.setTranType(getTranType());
        LedgerTransaction transactionUpdated = mvLedgerTransactionRepository.save(transaction);

        String logTitle = "Cập nhật phiếu thu";
        ACTION logFunc = ACTION.SLS_RCT_U;
        if (transactionUpdated.getTranType().equals("PC")) {
            logTitle = "Cập nhật phiếu chi";
            logFunc = ACTION.SLS_PMT_U;
        }
        systemLogService.writeLogCreate(MODULE.PRODUCT, logFunc, MasterObject.LedgerTransaction, logTitle, transactionUpdated.getGroupObject().getName());

        return transactionUpdated;
    }

    @Override
    public String delete(Integer tranId) {
        throw new AppException("Method dose not support!");
    }

    protected String getTranType() {
        return null;
    }

    public String getNextTranCode(int lastIndex) {
        return getTranType() + String.format("%05d", lastIndex);
    }
}