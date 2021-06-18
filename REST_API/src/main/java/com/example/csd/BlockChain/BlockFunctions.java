package com.example.csd.BlockChain;

import auxiliary.utils;
import com.example.csd.CoinBase.CoinBase;
import com.example.csd.Transfer.TokenTransfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BlockFunctions {

    public static byte[] buildPendingTransactions(int nTransactions, List<TokenTransfer> transactions, List<CoinBase> rewards) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if(rewards.size()>nTransactions){
            os.write(utils.intToByteArray(nTransactions));
            for(int i=0;i<nTransactions;i++){
                byte[] raw =rewards.get(i).CoinBaseBlock();
                os.write(raw);
                //SCoinBase.recoverBaseCoinRaw(raw);
            }
            os.write(utils.intToByteArray(0));
        }else{
            os.write(utils.intToByteArray(rewards.size()));
            System.out.println("CoinBase size= "+rewards.size());
            for(CoinBase coin : rewards){
                os.write(coin.CoinBaseBlock());
            }
            int remaining = nTransactions-rewards.size();
            if(transactions.size()>remaining){
                os.write(utils.intToByteArray(remaining));
                for(int i =0;i<remaining;i++){
                    os.write(transactions.get(i).TransferBlock());
                }
            }else{
                os.write(utils.intToByteArray(transactions.size()));
                for(TokenTransfer transfer : transactions){
                    os.write(transfer.TransferBlock());
                }
            }
        }
        return os.toByteArray();
    }

}
