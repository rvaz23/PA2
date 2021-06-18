package com.example.csd.Transfer;

import com.example.csd.Account.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TransferRepository extends CrudRepository<TokenTransfer,Integer> {

    //List<TokenTransfer>  findAllByFromEqualsOrToEqualsOrderByTime(Account account);

    @Query("select  u from TokenTransfer u where u.from.username=:username or u.to.username=:username")//inner join Account where Account.username = :username")
    public Iterable<TokenTransfer> findByUsername(@Param("username") String  username);

    @Query("select u from TokenTransfer u where u.time>=:start and u.time<=:end")
    public Iterable<TokenTransfer> findAllByTimestamp(@Param("start")Timestamp stime, @Param("end") Timestamp etime);

    @Query("select  u from TokenTransfer u where (u.from.username=:username or u.to.username=:username) and u.time>=:start and u.time<=:end")
    public Iterable<TokenTransfer> findAllByUserandTimestamp(@Param("username")String username ,@Param("start")Timestamp stime, @Param("end") Timestamp etime);

    @Query("select  u from TokenTransfer u where u.blockId=-1 order by u.time ASC ")//inner join Account where Account.username = :username")
    public Iterable<TokenTransfer> findNotConfirmed();

    @Query("select max(id) from TokenTransfer")
    public Integer findLast();

}
