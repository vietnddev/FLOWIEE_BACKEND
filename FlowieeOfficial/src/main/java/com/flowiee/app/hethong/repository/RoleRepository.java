package com.flowiee.app.hethong.repository;

import com.flowiee.app.hethong.entity.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface RoleRepository extends JpaRepository<AccountRole, Integer> {
    List<AccountRole> findByModule(String module);

    List<AccountRole> findByAccountId(Integer accountId);

    @Query("select id from AccountRole where accountId=:accountId")
    List<Integer> findIdsByAccountId(@Param("accountId") Integer accountId);

    @Query("from AccountRole where accountId=:accountId and module=:module and action=:action")
    AccountRole isAuthorized(int accountId, String module, String action);

    @Query("from AccountRole where action=:action and accountId=:accountId")
    AccountRole findByActionAndAccountId(String action, int accountId);

    void deleteByAccountId(Integer accountId);
}