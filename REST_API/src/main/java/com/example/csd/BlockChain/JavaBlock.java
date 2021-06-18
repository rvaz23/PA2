package com.example.csd.BlockChain;

import auxiliary.utils;
import com.example.csd.Account.Account;
import com.example.csd.Account.AccountRepository;
import com.example.csd.CoinBase.CoinBase;
import com.example.csd.Transfer.TokenTransfer;
import com.example.csd.Transfer.TokenTransferSimple;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaBlock {

    public int id;
    public int nonce;
    public byte[] dataHash;
    public byte[] previousHash;
    public byte[] finalHash;
    public List<CoinBase> coinBase;
    public List<TokenTransferSimple> transactions;
    public byte[] rawBlock;

    public JavaBlock(byte [] blockRaw){
        rawBlock=blockRaw;
        previousHash=getPreviousHash(blockRaw);
        dataHash= getTransactionHash(blockRaw);
        finalHash=getBlockHash(blockRaw);
        getCoinBases(blockRaw);
        //this.acounts=accounts;
    }

    private byte[]getPreviousHash(byte[] block){
        byte[] previousHash= new byte[32];
        System.arraycopy(block,40,previousHash,0,32);
        return previousHash;
    }

    private byte[] getTransactionHash(byte[] block){
        byte[] hash= new byte[32];
        System.arraycopy(block,8,hash,0,32);
        return hash;
    }
    private byte[]getBlockHash(byte[] block){
        byte[] blockHash= new byte[32];
        System.arraycopy(block,72,blockHash,0,32);
        return blockHash;
    }

    private void getCoinBases(byte[] block){
        List<CoinBase> lCb = new ArrayList<CoinBase>();
        byte[] numberCoinBase = new byte[4];
        System.arraycopy(block,104,numberCoinBase,0,4);
        int nCoinBases = utils.byteArrayToint(numberCoinBase);

        int bytesConsumed=108;
        for(int i=0;i<nCoinBases;i++){
            byte[] aux = new byte[4];
            System.arraycopy(block,bytesConsumed,aux,0,4);
            int id = utils.byteArrayToint(aux);
            System.arraycopy(block,bytesConsumed+4,aux,0,4);
            int nCarachters= utils.byteArrayToint(aux);
            byte[] username= new byte[nCarachters];
            System.arraycopy(block,bytesConsumed+8,username,0,nCarachters);
            String name = new String(username, StandardCharsets.UTF_8);
            System.arraycopy(block,bytesConsumed+8+nCarachters,aux,0,4);
            int amount = utils.byteArrayToint(aux);
            System.out.println(id+name+amount);
            bytesConsumed+=(12+nCarachters);
            CoinBase cb = new CoinBase(id,name,amount);
            lCb.add(cb);
        }
        this.coinBase= lCb;

        List<TokenTransferSimple> lTt = new ArrayList<TokenTransferSimple>();
        byte[] numberTransactions = new byte[4];
        System.arraycopy(block,bytesConsumed,numberTransactions,0,4);
        int nTransactions = utils.byteArrayToint(numberTransactions);
        bytesConsumed+=4;
        for(int i=0;i<nTransactions;i++){
            byte[] aux = new byte[4];
            System.arraycopy(block,bytesConsumed,aux,0,4);
            int id = utils.byteArrayToint(aux);

            byte[] signature = new byte[128];
            System.arraycopy(block,bytesConsumed+4,signature,0,128);

            System.arraycopy(block,bytesConsumed+4+128,aux,0,4);
            int nCarachtersFrom= utils.byteArrayToint(aux);
            byte[] usernameFrom= new byte[nCarachtersFrom];
            System.arraycopy(block,bytesConsumed+8+128,usernameFrom,0,nCarachtersFrom);
            String nameFrom = new String(usernameFrom,StandardCharsets.UTF_8);

            System.arraycopy(block,bytesConsumed+8+nCarachtersFrom+128,aux,0,4);
            int nCarachtersTo= utils.byteArrayToint(aux);
            byte[] usernameTo= new byte[nCarachtersTo];
            System.arraycopy(block,bytesConsumed+12+128+nCarachtersFrom,usernameTo,0,nCarachtersTo);
            String nameTo = new String(usernameTo,StandardCharsets.UTF_8);

            boolean isEncrypted=true;
            if(block[bytesConsumed+12+128+nCarachtersFrom+nCarachtersTo]==0){
                isEncrypted=false;
            }

            System.arraycopy(block,bytesConsumed+13+128+nCarachtersFrom+nCarachtersTo,aux,0,4);
            int amount = utils.byteArrayToint(aux);



            System.out.println(id+nameFrom+nameTo+amount);
            TokenTransferSimple tT = new TokenTransferSimple(id,isEncrypted,utils.intToByteArray(amount),nameFrom, nameTo,signature);
            lTt.add(tT);
            bytesConsumed+=(17+nCarachtersFrom+nCarachtersTo+128);
        }
        this.transactions = lTt;
    }


}
