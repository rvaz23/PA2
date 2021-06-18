package com.example.csd.CoinBase;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CoinBaseRepository extends CrudRepository<CoinBase,Integer>{

    @Query("select max(id) from CoinBase")
    public Integer findLast();

    @Modifying (clearAutomatically = true)
    @Transactional
    @Query("update CoinBase u set u.blockId=:blockId where u.id=:Id")
    public void addConfirmation(@Param("Id") int Id,@Param("blockId") int blockId);

}
