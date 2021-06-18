package com.example.csd.BlockChain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;

@Entity
public class Block implements Serializable {


    private int id;
    @Lob
    @Column(name = "block",columnDefinition = "MEDIUMBLOB")
    byte[] block;

    public Block(){

    }

    public Block(int id, byte[] block) {
        super();
        this.id=id;
        this.block=block;
    }


    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Lob
    @Column(name = "block",columnDefinition = "MEDIUMBLOB")
    public byte[] getBlock() {
        return block;
    }

    public void setBlock(byte[] block) {
        this.block = block;
    }
}

