package com.example.csd.Account;

import bftsmart.demo.csdcoin.CsdClient;
import bftsmart.demo.csdcoin.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller // This means that this class is a Controller
@RequestMapping(path="/users") // This means URL's start with /demo (after Application path)
public class AccountController {

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private AccountRepository userRepository;

    @Autowired
    private CsdClient client;

    //private CsdClient client = CsdClient.getInstance();
    @PostMapping(path="/new") // Map ONLY POST Requests
    public @ResponseBody
    List<String> addNewUser (@RequestParam String username
            , @RequestParam String pwd, @RequestBody byte[] publickey) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        //Account acc = new Account(username,pwd,publickey);
        //userRepository.save(acc);
        List<byte[]> results = client.addUser(username,pwd,publickey);
        int operationid = Integer.parseInt(new String(results.get(0),StandardCharsets.UTF_8));
        System.out.println(results.get(0));
        System.out.println("Entrou");
        byte[] hash = results.get(1);
        //verify hash consensus with replicas again
        byte[] replicasHash =client.operationHash(Message.getOperationHash(operationid));
        //byte[] replicasHash =response.getValues().get(0);
        List<String> clientResponse = new ArrayList<String>();
        if(MessageDigest.isEqual(hash,replicasHash)){
            clientResponse.add(new String(results.get(0),StandardCharsets.UTF_8));
            //clientResponse.add(new String(hash,StandardCharsets.ISO_8859_1));
            clientResponse.add(new String(results.get(2),StandardCharsets.UTF_8));
        }else{
             String resp= new String(results.get(2),StandardCharsets.UTF_8);
            if(resp.contains("Logged In")){
                clientResponse.add("Logged In");
            }else{
                results = new ArrayList<byte[]>();
                results.add("Operation Hash verification Failed".getBytes(StandardCharsets.UTF_8));
                clientResponse.add("Operation  Failed");
            }
        }
        return clientResponse;
        /*
        System.out.println(publickey);
        if (userRepository.findDistinctByUsername(username)==null){
            Account n = new Account(username,pwd,publickey);
            userRepository.save(n);
            return "Saved";
        }else{
            return "Failed";
        }*/

    }


    @GetMapping(path="/hash")
    public @ResponseBody
    String getOperationHash(@RequestParam int id){
        byte[] replicasHash =client.operationHash(Message.getOperationHash(id));
        return new String(replicasHash,StandardCharsets.ISO_8859_1);
    }


    @PostMapping(path="/deposit") // Map ONLY POST Requests
    public @ResponseBody
    List<String> deposit(@RequestParam String username
            , @RequestParam int amount, @RequestBody byte[] signature) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request
        /*Account acc = userRepository.findDistinctByUsername(username);
        if (verifyDeposit(username,amount,signature)){
            acc.deposit(amount);
            userRepository.save(acc);
            return "Sucess";
        }
        return "failed";
  */
        List<byte[]> results = client.deposit(username,(float)amount,signature);
        //int operationid = Integer.parseInt(new String(results.get(0),StandardCharsets.UTF_8));
        //byte[] hash = results.get(1);
        //byte[] replicasHash =client.operationHash(Message.getOperationHash(operationid));
        //byte[] replicasHash =response.getValues().get(0);
       // System.out.println(replicasHash);
        List<String> clientResponse = new ArrayList<String>();
        //if(MessageDigest.isEqual(hash,replicasHash)){
           // clientResponse.add(new String(Integer.toString(operationid)));
            //clientResponse.add(new String(hash,StandardCharsets.ISO_8859_1));
            clientResponse.add(new String(results.get(0),StandardCharsets.UTF_8));
            clientResponse.add(new String(results.get(1),StandardCharsets.UTF_8));
            return clientResponse;
        /*}else{
            clientResponse.add("Operation Hash verification Failed");
            return clientResponse;
        }*/
    }

    @GetMapping(path="/balance")
    public @ResponseBody
    List<String> balance(@RequestParam String username) {
        /*Account acc = userRepository.findDistinctByUsername(username);
        if (acc!=null){
            return Float.toString(acc.getAmount());
        }
        return "failed";
   */
        return client.userBalance(username);
    }




    @GetMapping(path="/all")
    public @ResponseBody Iterable<Account> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }
}