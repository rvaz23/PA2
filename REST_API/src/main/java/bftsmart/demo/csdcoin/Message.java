package bftsmart.demo.csdcoin;

import auxiliary.utils;
import org.bouncycastle.util.Times;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {


    private static final long serialVersionUID = -6917626236238045064L;

    public enum Type {
        NEW, GET,  UPDATE, DELETE, RESULT, ERROR,TRANSFER
    };


    private Type type;
    private String key;
    private HashMap<String, byte[]> values;
    private int result = -1;
    private HashMap<String, byte[]> results;
    private String errorMsg;

    private Message() {
        super();
        result = -1;
    }

    public static Message addUser(String username,String pwd,byte[] publicKey){
    	Message message = new Message();
    	message.type = Type.NEW;
    	message.key = "NEW_USER";
    	HashMap<String,byte[]> newuser = new HashMap<String,byte[]>();
        newuser.put("USERNAME",username.getBytes());
        newuser.put("PASSWORD",pwd.getBytes());
        newuser.put("PUBKEY",publicKey);
    	message.values = newuser;
        return message;
    }

    public static Message getOperationHash(int id){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "HASH";
        HashMap<String,byte[]> userdeposit = new HashMap<String,byte[]>();
        userdeposit.put("ID",Integer.toString(id).getBytes());
        message.values = userdeposit;
        return message;
    }

    public static Message deposit(String username,float amount,byte[] signature){
        Message message = new Message();
        message.type = Type.UPDATE;
        message.key = "DEPOSIT";
        HashMap<String,byte[]> userdeposit = new HashMap<String,byte[]>();
        userdeposit.put("USERNAME",username.getBytes());
        userdeposit.put("AMOUNT",Float.toString(amount).getBytes());
        userdeposit.put("SIGNATURE",signature);
        message.values = userdeposit;
        return message;
    }

    public static Message balance(String username){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "BALANCE";
        HashMap<String,byte[]> userbalance = new HashMap<String,byte[]>();
        userbalance.put("USERNAME",username.getBytes());
        message.values = userbalance;
        return message;
    }

    public static Message pending(int number){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "PENDING";
        HashMap<String,byte[]> n = new HashMap<String,byte[]>();
        n.put("NUMBER", utils.intToByteArray(number));
        message.values = n;
        return message;
    }

    public static Message mine(byte[] block){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "MINE";
        HashMap<String,byte[]> n = new HashMap<String,byte[]>();
        n.put("BLOCK", block);
        message.values = n;
        return message;
    }

    public static Message LastBlock(){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "LASTB";
        return message;
    }




    public static Message transfer(String from,String to,float amount,byte[] signature){
        Message message = new Message();
        message.type = Type.UPDATE;
        message.key = "TRANSFER";
        HashMap<String,byte[]> userdeposit = new HashMap<String,byte[]>();
        userdeposit.put("FROM",from.getBytes());
        userdeposit.put("TO",to.getBytes());
        userdeposit.put("AMOUNT",Float.toString(amount).getBytes());
        userdeposit.put("SIGNATURE",signature);
        message.values = userdeposit;
        return message;
    }
    public static Message userLedger(String username,Long start,Long end){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "LEDGER";
        if(start!=null && end !=null){
            HashMap<String,byte[]> times = new HashMap<String,byte[]>();
            times.put("USERNAME",username.getBytes());
            times.put("START", longToBytes(start));
            times.put("END", longToBytes(end));
            message.values= times;
        }
        return message;
    }

    public static byte[] longToBytes(long x){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    public static Message globalLedger(Long start, Long end){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "GLOBAL_LEDGER";
        if(start!=null && end !=null){
            HashMap<String,byte[]> times = new HashMap<String,byte[]>();
            times.put("START", longToBytes(start));
            times.put("END", longToBytes(end));
            message.values= times;
        }

        return message;
    }

    public static Message results(List<byte[]> list){
        Message message = new Message();
        message.type = Type.RESULT;
        message.key = "RESULT";
        HashMap<String,byte[]> results = new HashMap<String,byte[]>();
        int i=0;
        for (byte[] result : list){
            results.put(Integer.toString(i),result);
            i++;
        }
        message.values= results;
        return message;
    }

    public static Message listUsers(){
        Message message = new Message();
        message.type = Type.GET;
        message.key = "LIST_USERS";
        return message;
    }


    public static Message newInsertRequest(String key, HashMap<String, byte[]> values) {
        Message message = new Message();
        message.type = Type.NEW;
        message.key = key;
        message.values = values;
        return message;
    }




    public String getKey() { return key; }

    public Type getType() {
        return type;
    }
    
    public HashMap<String, byte[]> getValues() {
        return values;
    }


}
