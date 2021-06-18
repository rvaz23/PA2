package com.example.csd.Transfer;

import auxiliary.utils;
import com.example.csd.Account.Account;
import com.example.csd.UTXO;

import javax.persistence.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

@Entity
public class TokenTransfer  implements Serializable {



    private Account from;


    private Account to;

    private Timestamp time;

    private byte[] signature;

    private byte[] operationHash;

    private boolean isEncrypted;
    private Integer id;
    private byte[] amount;

    private int blockId;

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public TokenTransfer(){
    }

    public TokenTransfer(int id,boolean isEncrypted,byte[] amount,Account from,Account to,Timestamp time,byte[] signature,byte[] operationHash){
        super();
        this.id=id;
        this.time=time;
        this.amount=amount;
        this.isEncrypted=isEncrypted;
        this.from=from;
        this.to=to;
        this.signature=signature;
        this.operationHash=operationHash;
        this.blockId=-1;
    }

    public TokenTransfer(int id,boolean isEncrypted,byte[] amount,Account from,Account to,byte[] signature){
        super();
        this.id=id;
        this.amount=amount;
        this.isEncrypted=isEncrypted;
        this.from=from;
        this.to=to;
        this.signature=signature;
        this.blockId=-1;
    }




    public byte[] getOperationHash() {
        return operationHash;
    }

    public void setOperationHash(byte[] operationHash) {
        this.operationHash = operationHash;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }


    public void setFrom(Account from) {
        this.from = from;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public void setAmount(byte[] amount) {
        this.amount = amount;
    }

    public byte[] getAmount() {
        return amount;
    }

    @Id
    public Integer getId() {
        return id;
    }

    public Timestamp getTime() {
        return time;
    }



    @ManyToOne
    @JoinColumn(name = "fromId")
    public Account getFrom() {
        return from;
    }

    public void setTo(Account to) {
        this.to = to;
    }

    @ManyToOne
    @JoinColumn(name = "toId")
    public Account getTo() {
        return to;
    }

    public byte[] TransferBlock() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] id = utils.intToByteArray(this.id);
        byte[] fromUs =from.getUsername().getBytes(StandardCharsets.UTF_8);
        byte[] fromLength= utils.intToByteArray(fromUs.length);
        byte[] toUs =to.getUsername().getBytes(StandardCharsets.UTF_8);
        byte[] toLength= utils.intToByteArray(toUs.length);
        byte isEncrypted=this.isEncrypted ? (byte) 1:0;
        byte[] time = utils.floatToByteArray(getTime().getTime());
        byte[] sign = signature;
        byte[] am =amount;
        os.write(id);
        os.write(sign);
        os.write(fromLength);
        os.write(fromUs);
        os.write(toLength);
        os.write(toUs);
        os.write(isEncrypted);
        os.write(am);
        byte[] send = os.toByteArray();
        return send;
    }
}
