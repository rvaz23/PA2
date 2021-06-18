package com.example.csd.CoinBase;

import auxiliary.utils;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Entity
public class CoinBase implements Serializable {

    private int id;
    private int amount;
    private String username;
    private int blockId;
    private byte[] operationHash;

public CoinBase(){

}

    public CoinBase(int id, String name, int amount){
        this.amount=amount;
        this.username=name;
        this.id=id;
        this.blockId=-1;
    }
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public byte[] getOperationHash() {
        return operationHash;
    }

    public void setOperationHash(byte[] operationHash) {
        this.operationHash = operationHash;
    }

    public byte[] CoinBaseBlock() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] id = utils.intToByteArray(this.id);
        byte[] toUs =username.getBytes(StandardCharsets.ISO_8859_1);
        byte[] toLength= utils.intToByteArray(toUs.length);
        byte[] am = utils.intToByteArray(this.amount);
        os.write(id);
        os.write(toLength);
        os.write(toUs);
        os.write(am);
        return os.toByteArray();
    }

    public static CoinBase recoverBaseCoinRaw(byte[] coinRaw){
        byte[] aux = new byte[4];
        System.arraycopy(coinRaw,0,aux,0,4);
        int id = utils.byteArrayToint(aux);
        System.arraycopy(coinRaw,4,aux,0,4);
        int nCarachters= utils.byteArrayToint(aux);
        byte[] username= new byte[nCarachters];
        System.arraycopy(coinRaw,8,username,0,nCarachters);
        String name = new String(username,StandardCharsets.ISO_8859_1);
        System.arraycopy(coinRaw,8+nCarachters,aux,0,4);
        int amount = utils.byteArrayToint(aux);
        System.out.println(id+name+amount);
        return new CoinBase(id,name,amount);
    }
    

}
