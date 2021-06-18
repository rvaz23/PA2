package bftsmart.demo.csdcoin;

import bftsmart.tom.ServiceProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



@Scope("singleton")
@Service("client")
public class CsdClient {

    private static CsdClient instance;
    ServiceProxy serviceProxy;

    public CsdClient(int clientId) {
        super();
        serviceProxy = new ServiceProxy(clientId);
    }

    public static CsdClient getInstance(int id) {
        if (instance==null){
            instance = new CsdClient(id);
        }
        return instance;
    }

    public static CsdClient getInstance() {
        return instance;
    }

    public byte[] operationHash(Message msg){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<byte[]> result = new ArrayList<byte[]>();
                for (String k :responseMap.keySet()){
                    return responseMap.get(k);
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }


    public List<byte[]> addUser(String username,String pwd,byte[] publicKey){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.addUser(username,pwd,publicKey);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            //byte[] reply =serviceProxy.invokeUnorderedHashed(byteOut.toByteArray());
            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<byte[]> result = new ArrayList<byte[]>();
                for (String k :responseMap.keySet()){
                    result.add(responseMap.get(k));
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public List<byte[]> deposit(String username,float amount,byte[] signature){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {


            Message msg = Message.deposit(username,amount,signature);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<byte[]> result = new ArrayList<byte[]>();
                for (String k :responseMap.keySet()){
                    result.add(responseMap.get(k));
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public List<byte[]> transfer(String from,String to,float amount,byte[] signature){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.transfer(from,to,amount,signature);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<byte[]> result = new ArrayList<byte[]>();
                for (String k :responseMap.keySet()){
                    result.add(responseMap.get(k));
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }
    public List<String> userBalance(String username){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.balance(username);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<String> result = new ArrayList<String>();
                for (String k :responseMap.keySet()){
                    result.add(new String(responseMap.get(k),StandardCharsets.UTF_8) );
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public byte[] mineBlock(byte[] minedBlock,String username){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.mine(minedBlock,username);
            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply == null)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                System.out.println(response);
                for (String k :responseMap.keySet()){
                    System.out.println(responseMap.get(k));
                    return responseMap.get(k);
                }
                return null;

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public byte[] lastBlock(){
        System.out.println("Fez pedido Cliente 1");
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.LastBlock();
            System.out.println("Fez pedido Cliente 2");
            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply == null)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                System.out.println(response);
                for (String k :responseMap.keySet()){
                    System.out.println(responseMap.get(k));
                    return responseMap.get(k);
                }
                return null;

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public byte[] pending(int numberTransactions){

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            Message msg = Message.pending(numberTransactions);
            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply == null)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                System.out.println(response);
                for (String k :responseMap.keySet()){
                    System.out.println(responseMap.get(k));
                    return responseMap.get(k);
                }
                return null;

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }

    public List<String> userLedger(String username,long start,long end){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.userLedger(username,start,end);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<String> result = new ArrayList<String>();
                for (String k :responseMap.keySet()){
                    result.add(new String(responseMap.get(k),StandardCharsets.UTF_8) );
                }
                return result;

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }




    public List<String> globalLedger(Long start,Long end){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.globalLedger(start,end);

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<String> result = new ArrayList<String>();
                for (String k :responseMap.keySet()){
                    result.add(new String(responseMap.get(k),StandardCharsets.UTF_8) );
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }


    public List<String> listUsers(){
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            Message msg = Message.listUsers();

            objOut.writeObject(msg);
            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                Message response= (Message)objIn.readObject();
                HashMap<String,byte[]> responseMap = response.getValues();
                List<String> result = new ArrayList<String>();
                for (String k :responseMap.keySet()){
                    result.add(new String(responseMap.get(k),StandardCharsets.UTF_8) );
                }
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return null;
    }


    public void close() {
        serviceProxy.close();
    }




}
