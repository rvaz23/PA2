package com.example.csd.BlockChain;


import com.example.csd.Account.Account;
import com.example.csd.Transfer.TokenTransfer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface ChainRepository extends CrudRepository<Block,Integer>  {

    @Query("select max(id) from Block")
    public Integer findLast();

    Block findDistinctById(Integer integer);
}
