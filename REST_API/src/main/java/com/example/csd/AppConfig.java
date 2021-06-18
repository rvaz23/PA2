package com.example.csd;


import bftsmart.demo.csdcoin.CsdClient;

import bftsmart.demo.csdcoin.CsdServer;
import com.example.csd.Account.AccountRepository;
import com.example.csd.BlockChain.ChainRepository;
import com.example.csd.CoinBase.CoinBaseRepository;
import com.example.csd.Transfer.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {


    @Value("${id}")
    private int id;

    @Autowired
    AccountRepository acc_repo;

    @Autowired
    TransferRepository transf_repo;

    @Autowired
    CoinBaseRepository coin_repo;

    @Autowired
    ChainRepository chain_repo;


        @Bean
        public CsdServer getServer(){
            if(acc_repo==null){
                System.out.println("é nulo oa KKKKKKKKKKKKKKKKKKKK");
            }else{
                System.out.println("édiferente de nulo");
            }
            //CoinBaseRepository coin_repo = null;
            return new CsdServer(id,acc_repo,transf_repo,coin_repo,chain_repo);
        }

    @Bean
    public CsdClient getClient(){
        return new CsdClient(id);
    }
}
