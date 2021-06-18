package com.example.csd.Transfer;


import auxiliary.utils;
import bftsmart.demo.csdcoin.CsdClient;
import bftsmart.demo.csdcoin.Message;
import com.example.csd.Account.Account;
import com.example.csd.Account.AccountRepository;
import com.example.csd.BlockChain.Block;
import com.example.csd.BlockChain.ChainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Controller // This means that this class is a Controller
@RequestMapping(path="/transfer") // This means URL's start with /demo (after Application path)
public class TransferController {

    @Autowired
    TransferRepository transfer;//

    @Autowired
    AccountRepository accounts;

    @Autowired
    ChainRepository chain;

    @Autowired
    private CsdClient client;

    @PostMapping(path = "/csdcoin")
    public @ResponseBody
    List<String> transfer(@RequestParam String from,@RequestParam String to,
             @RequestParam float amount,@RequestBody byte[] signature) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
       /* Account fromAc = accounts.findDistinctByUsername(from);
        Account toAc = accounts.findDistinctByUsername(to);
        if(verifyTransfer(from,to,amount,signature)){
            Timestamp time = new Timestamp(System.currentTimeMillis());
            TokenTransfer tr = new TokenTransfer(amount,fromAc,toAc,time,signature);
            transfer.save(tr);
            return "Sucesses";
        }
        return "Failed to transfer";
    */

        List<byte[]> results = client.transfer(from,to,amount,signature);
        int operationid = Integer.parseInt(new String(results.get(0),StandardCharsets.UTF_8));
        byte[] hash = results.get(1);
        byte[] replicasHash =client.operationHash(Message.getOperationHash(operationid));
        //byte[] replicasHash =response.getValues().get(0);
        System.out.println(replicasHash);
        List<String> clientResponse = new ArrayList<String>();
        if(MessageDigest.isEqual(hash,replicasHash)){
            clientResponse.add(new String(results.get(0),StandardCharsets.UTF_8));
            //clientResponse.add(new String(hash,StandardCharsets.UTF_8));
            clientResponse.add(new String(results.get(2),StandardCharsets.UTF_8));
            return clientResponse;
        }else{
            clientResponse.add("Operation Hash verification Failed");
            return clientResponse;
        }
    }

    @GetMapping(path = "/ledger")
    public @ResponseBody Iterable<String> getLedger(@RequestParam(required = false) String username,
                                                    @RequestParam(required = false) Long start,
                                                    @RequestParam(required = false) Long end) {
        Iterable<String> prelist;
        if(username==null){
            if(start!=null && end !=null){
                return client.globalLedger(start,end);
            }else{
                return client.globalLedger(start,end);
            }
        }else {
            return client.userLedger(username,start,end);
        }
    }

    @GetMapping(path = "/pending")
    public @ResponseBody byte[] getPending(@RequestParam(required = true) int nTransactions) {
    byte[] result = client.pending(nTransactions);

        return result;
    }

    @GetMapping(path = "/LastBlock")
    public @ResponseBody byte[] getPending() {
        byte[] result = client.lastBlock();
        return result;
    }

    @PostMapping(path = "/mine")
    public @ResponseBody
    String transfer(@RequestParam String username,@RequestBody byte[] block) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        byte[] message = client.mineBlock(block);

    return new String(message,StandardCharsets.UTF_8);
    }




}


