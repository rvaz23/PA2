package bftsmart.demo.csdcoin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import auxiliary.utils;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import com.example.csd.Account.Account;
import com.example.csd.Account.AccountRepository;
import com.example.csd.BlockChain.Block;
import com.example.csd.BlockChain.BlockFunctions;
import com.example.csd.BlockChain.ChainRepository;
import com.example.csd.CoinBase.CoinBase;
import com.example.csd.CoinBase.CoinBaseRepository;
import com.example.csd.Transfer.TokenTransfer;
import com.example.csd.Transfer.TransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@Scope("singleton")
@Service("server")
public class CsdServer extends DefaultSingleRecoverable {

	private static CsdServer instance;

	//@Autowired
	TransferRepository transferRepository;

	//@Autowired
	AccountRepository repositorydb;

	CoinBaseRepository coinRepository;

	ChainRepository chainRepository;
	private Logger logger;

	@Value("${id}")
	private int id;

	public CsdServer() {
		logger = Logger.getLogger(CsdServer.class.getName());
		new ServiceReplica(id, this, this);
		System.out.println("acabou o construtor");
	}

	public CsdServer(int id, AccountRepository acc_repo, TransferRepository tr_repo, CoinBaseRepository coin_repo,ChainRepository chainRepository) {
		repositorydb=acc_repo;
		transferRepository=tr_repo;
		coinRepository=coin_repo;
		this.chainRepository=chainRepository;
		String pub = "30 81 89 02 81 81 00 AE 7D 7D 83 1A 24 AC AD F0 F5 00 14 FE 6F 12 01 64 70 14 83 BC 1E 17 24 A8 91 87 29 75 DB CB 9E C1 1A 3A 75 ED 0D 3B F7 BF ED 08 D7 7E 53 83 A7 C1 C2 9E 0F E5 83 FA 8E 5A DD 43 A8 A0 27 BE 44 FD D6 EA 9E 26 BF D9 E6 79 DF B6 1E 05 EE B4 0E 81 D1 F0 5C 80 E1 69 48 43 F1 34 04 45 1B EE 94 4D D9 97 F4 57 29 49 F7 AB D4 3D 18 0D AB 0D 6B 30 65 86 01 A2 89 6E C2 F7 5D 8E E4 F3 C7 71 6D 02 03 01 00 01";
		for(int i=0;i<20;i++) {
			CoinBase initialAmount = new CoinBase(i, "Ruben", 10000);
			coinRepository.save(initialAmount);
		}
		logger = Logger.getLogger(CsdServer.class.getName());
		new ServiceReplica(id, this, this);

	}

	public byte[] sendPending(int nTransactions) throws IOException {
		List<CoinBase> ltest = (List<CoinBase>)coinRepository.findAll();
		System.out.println(ltest.size());
		byte[] transactions= BlockFunctions.buildPendingTransactions(nTransactions,(List<TokenTransfer>) transferRepository.findAll(),(List<CoinBase>) coinRepository.findAll());
		System.out.println(transactions.length);
		return transactions;
	}
/*
	public static CsdServer getInstance(int id) {
		if (instance==null){
			instance = new CsdServer(id);
		}
		return instance;
	}
/*
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: demo.map.MapServer <server id>");
			System.exit(-1);
		}
		new CsdServer(Integer.parseInt(args[0]));
	}
*/
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

	private boolean veryfyMined(byte[] block) throws NoSuchAlgorithmException, IOException {
		//MessageDigest md = MessageDigest.getInstance("SHA-256");
		int ExpectingId;
		byte[] previousBlock;
		if(chainRepository.findLast()!=null){
			ExpectingId=chainRepository.findLast()+1;
			previousBlock=chainRepository.findDistinctById(ExpectingId-1).getBlock();
			byte[] realPrevious=getBlockHash(previousBlock);
			byte[] newBlockPreviousHash = getPreviousHash(block);
			if(!MessageDigest.isEqual(realPrevious,newBlockPreviousHash)){
				System.out.println("Falha na verificação dos hashes das anteriores");
				return false;
			}
		}else{
			ExpectingId=0;
			byte[] newBlockPreviousHash = getPreviousHash(block);
			for(int i=0;i<32;i++){
				if(newBlockPreviousHash[i]!=0){
					System.out.println("Falha na verificação dos hashes das anteriores");
					return false;
				}
			}
		}
		byte[] blockId = new byte[4];
		System.arraycopy(block,0,blockId,0,4);
		if(ExpectingId != utils.byteArrayToint(blockId)){
			System.out.println("Falha na verificação dos ids");
			return false;
		}

		if(!verifyTransactions_and_finalHash(block)){
			return false;
		}
		return true;
	}

	private List<CoinBase> getCoinBases(byte[] block){
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
			String name = new String(username,StandardCharsets.ISO_8859_1);
			System.arraycopy(block,bytesConsumed+8+nCarachters,aux,0,4);
			int amount = utils.byteArrayToint(aux);
			System.out.println(id+name+amount);
			bytesConsumed+=(12+nCarachters);
			CoinBase cb = new CoinBase(id,name,amount);
			lCb.add(cb);
		}
		return lCb;
	}
	private boolean verifyCoinTransferHashes(List<CoinBase> lCb,List<TokenTransfer> lTt,byte[] block) throws IOException, NoSuchAlgorithmException {
		byte[] bTransactionHashes = getTransactionHash(block);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(utils.intToByteArray(lCb.size()));
		for(CoinBase cb : lCb){
			os.write(cb.CoinBaseBlock());
		}
		os.write(utils.intToByteArray(0));
		byte[] hashesToCalculate= os.toByteArray();
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashed = md.digest(hashesToCalculate);
		if(MessageDigest.isEqual(hashed,bTransactionHashes)){
			return true;
		}else{
			System.out.println("Falha na verificação dos hashes das transferencias");
			return false;
		}
	}

	private boolean verifyHeaderHash(byte[] block) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    byte[] header = new byte[72];
	    byte[] blockHash = new byte[32];
        System.arraycopy(block,0,header,0,72);
	    System.arraycopy(block,72,blockHash,0,32);
	    byte[] headerHashed = md.digest(header);
	    if(MessageDigest.isEqual(blockHash,headerHashed)){
	        return true;
        }else{
	        return false;
        }
    }

    private boolean confirmCoinBase(List<CoinBase> lBc,int blockId){
	    List<CoinBase> listToUpdate = new ArrayList<CoinBase>();
        for(CoinBase cb :lBc ){
            Optional<CoinBase> optional =coinRepository.findById(cb.getId());
            if(optional.get()==null){
                return false;
            }
            CoinBase dCb = optional.get();
            if(cb.getId()==dCb.getId() && cb.getAmount()==dCb.getAmount() && cb.getUsername().equals(dCb.getUsername()) ){
                dCb.setBlockId(blockId);
                listToUpdate.add(dCb);
            }else{
                return false;
            }
        }
        for(CoinBase toUpdate: listToUpdate){
            coinRepository.addConfirmation(toUpdate.getId(), toUpdate.getBlockId());
        }
        //coinRepository.saveAll(listToUpdate);
        return true;
    }

	private boolean verifyTransactions_and_finalHash(byte[] block) throws IOException, NoSuchAlgorithmException {
		List<CoinBase> lCb = getCoinBases(block);
		//VerificarHash de transaçoes
		//fazer transações
		List<TokenTransfer> lTt = null;
		if(!verifyCoinTransferHashes(lCb,lTt,block)){
			return false;
		}
		if(!verifyHeaderHash(block)){
		    return false;
        }
		byte[] blockId = new byte[4];
		System.arraycopy(block,0,blockId,0,4);
        if(!confirmCoinBase(lCb,utils.byteArrayToint(blockId))){
            return false;
        }
		return true;
	}


	@SuppressWarnings("unchecked")
	@Override
	public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx)  {
		byte[] reply = null;
		boolean hasReply = false;
		List<byte[]> results = null;
		Message response = null;
		String pwd = null;
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
			 ObjectInput objIn = new ObjectInputStream(byteIn);
			 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
			Message message = (Message) objIn.readObject();
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			switch (message.getKey()) {
				case "MINE":
					byte[] MinedBlock=message.getValues().get("BLOCK");
					boolean done =veryfyMined(MinedBlock);
					results = new ArrayList<byte[]>();
					if(done){
						byte[]bId = new byte[4];
						System.arraycopy(MinedBlock,0,bId,0,4);
						Block blck = new Block(utils.byteArrayToint(bId),MinedBlock);
						chainRepository.save(blck);
						results.add("CONGRATS YOU DID IT".getBytes(StandardCharsets.UTF_8));
					}else{
						results.add("Block is wrong".getBytes(StandardCharsets.UTF_8));
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "PENDING":
					int nTran = utils.byteArrayToint(message.getValues().get("NUMBER"));
					System.out.println("Fez pedido Server 1");
					byte[] pending = sendPending(nTran);
					results = new ArrayList<byte[]>();
					System.out.println("Fez pedido Server 2");
					results.add(pending);
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "LASTB":
					results = new ArrayList<byte[]>();
					System.out.println("Fez pedido Server 2");
					int lastid =chainRepository.findLast();
					Block lastBlock = chainRepository.findDistinctById(lastid);
					results.add(lastBlock.getBlock());
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "NEW_USER":
					String username = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					pwd = new String(message.getValues().get("PASSWORD"), StandardCharsets.UTF_8);
					byte[] pubkey = message.getValues().get("PUBKEY");
					//repository.addAccount(username);
					results = new ArrayList<byte[]>();
					Account n=repositorydb.findDistinctByUsername(username);
					if ( n == null) {
						//System.out.println("Entrou para escrever na base de dados");
						String messageforHash = "REGISTER"+username + pwd+ new String(pubkey, StandardCharsets.UTF_8);
						byte[] MessToBy = messageforHash.getBytes();
						byte[] messageHash = md.digest(MessToBy);
						n = new Account(username, pwd, pubkey,messageHash);
						repositorydb.save(n);
						results.add(n.getId().toString().getBytes(StandardCharsets.UTF_8));
						results.add(messageHash);
						results.add("User Created".getBytes(StandardCharsets.UTF_8));
					} else {
						results.add(n.getId().toString().getBytes(StandardCharsets.UTF_8));
						results.add(new byte[2]);
						if(n.getPwd().equals(pwd)){
							results.add("Logged In".getBytes(StandardCharsets.UTF_8));
						}
						results.add("User already exists".getBytes(StandardCharsets.UTF_8));
					}

					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "DEPOSIT":
					String username1 = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					int amount = (int)Float.parseFloat(new String(message.getValues().get("AMOUNT"), StandardCharsets.UTF_8));
					byte[] signature = message.getValues().get("SIGNATURE");
					results = new ArrayList<byte[]>();

					Account acc = repositorydb.findDistinctByUsername(username1);
					if (acc != null) {
						PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(acc.getPublickey()));
						String message1 = username1 + "DEPOSIT" + amount;
						byte[] messageBytes = message1.getBytes(StandardCharsets.UTF_8);
						md = MessageDigest.getInstance("SHA-256");
						byte[] messageHash1 = md.digest(messageBytes);
						System.out.println(message1);
						Cipher cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.DECRYPT_MODE, publicKey);
						byte[] receivedHash = cipher.doFinal(signature);
						if (MessageDigest.isEqual(receivedHash, messageHash1)) {
							acc.deposit(amount);
							repositorydb.save(acc);
							if (coinRepository.findLast() != null) {
								CoinBase cb = new CoinBase(coinRepository.findLast()+1,username1,amount);
								coinRepository.save(cb);
							}else{
								CoinBase cb = new CoinBase(0,username1,amount);
								coinRepository.save(cb);
							}

							//TokenTransfer transfer = new TokenTransfer(amount,acc,acc,new java.sql.Timestamp(System.currentTimeMillis()),signature,messageHash1);
							//transferRepository.save(transfer);
							//results.add(transfer.getId().toString().getBytes(StandardCharsets.UTF_8));
							//results.add(messageHash1);
							results.add("User Deposit has been made".getBytes(StandardCharsets.UTF_8));
							results.add(("Current balance: " + acc.getAmount().toString()).getBytes(StandardCharsets.UTF_8));
						} else {
							results.add("Failed to deposit".getBytes(StandardCharsets.UTF_8));
						}
					} else {
						results.add("Failed to deposit".getBytes(StandardCharsets.UTF_8));
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "TRANSFER":
					String from = new String(message.getValues().get("FROM"), StandardCharsets.UTF_8);
					String to = new String(message.getValues().get("TO"), StandardCharsets.UTF_8);
					int amount1 = Integer.parseInt(new String(message.getValues().get("AMOUNT"), StandardCharsets.UTF_8));
					byte[] signature1 = message.getValues().get("SIGNATURE");

					Account fromAc = repositorydb.findDistinctByUsername(from);
					Account toAc = repositorydb.findDistinctByUsername(to);

					results = new ArrayList<byte[]>();
					byte[] hashed_transfer=verifyTransfer(fromAc,toAc,amount1,signature1);
					if (hashed_transfer!=null){
						fromAc.makeTransfer(amount1);
						repositorydb.save(fromAc);
						toAc.receiveTransfer(amount1);
						repositorydb.save(toAc);
						TokenTransfer transfer = new TokenTransfer(0,false, utils.intToByteArray(amount1),fromAc,toAc,new java.sql.Timestamp(System.currentTimeMillis()),signature1,hashed_transfer);
						transferRepository.save(transfer);
						results.add(transfer.getId().toString().getBytes(StandardCharsets.UTF_8));
						results.add(hashed_transfer);
						results.add("Transfer has been made".getBytes(StandardCharsets.UTF_8));
					}else{
						results.add("Transfer Failed".getBytes(StandardCharsets.UTF_8));
					}

					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "BALANCE":
					String username3 = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					Account acc1 = repositorydb.findDistinctByUsername(username3);
					results = new ArrayList<byte[]>();
					if (repositorydb.findDistinctByUsername(username3)!=null){
						results.add((acc1.getUsername()+" has "+Float.toString(acc1.getAmount())).getBytes(StandardCharsets.UTF_8));
					}else{
						results.add("Failed to connect to account".getBytes(StandardCharsets.UTF_8));
					}

					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "LEDGER":
					System.out.println("Entrou para o ledger server");
					String username2 = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					System.out.println(username2);
					results = new ArrayList<byte[]>();
					List<TokenTransfer> auxlist = (List<TokenTransfer>) transferRepository.findByUsername(username2);
					for(TokenTransfer tr : auxlist){
						System.out.println(tr.toString());
						if(tr.getFrom().getUsername().equals(tr.getTo().getUsername())){
							results.add(("Deposit on: "+tr.getFrom().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}else{
							results.add(("Transfer from: "+tr.getFrom().getUsername()+" to: "+tr.getTo().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}
					}
					System.out.println(results);
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "GLOBAL_LEDGER":
					results = new ArrayList<byte[]>();
					List<TokenTransfer> auxlist1 = (List<TokenTransfer>) transferRepository.findAll();
					for(TokenTransfer tr : auxlist1){
						if(tr.getFrom().getUsername().equals(tr.getTo().getUsername())){
							results.add(("Deposit on: "+tr.getFrom().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}else{
							results.add(("Transfer from: "+tr.getFrom().getUsername()+" to: "+tr.getTo().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "HASH":
					int id = (int)Integer.parseInt(new String(message.getValues().get("ID"), StandardCharsets.UTF_8));
					results = new ArrayList<byte[]>();
					Optional<Account> acc_exists = repositorydb.findById(id);
					if (acc_exists.isPresent()) {
						byte[] acc_hash=acc_exists.get().getOperationHash();
						if(MessageDigest.isEqual(acc_hash,message.getValues().get("HASH"))){
							System.out.println("Sao iguais");
						}
						results.add(acc_hash);
						System.out.println("Obteve ACCOUNT "+acc_exists.get().getUsername());
					} else{
						Optional<TokenTransfer> transfer_exists = transferRepository.findById(id);
						if(transfer_exists.isPresent()){
							results.add(transfer_exists.get().getOperationHash());
							System.out.println("Obteve tRANSFER ");
						}else{
							System.out.println("Falhou os IDs");
							results.add("ID DOES NOT EXIST".getBytes(StandardCharsets.UTF_8));
						}
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "LIST_USERS":
					List<Account> auxlist2 = (List<Account>) repositorydb.findAll();
					for(Account accounts : auxlist2){
						results.add((accounts.getUsername() +" "+accounts.getAmount()).getBytes(StandardCharsets.UTF_8));
					}
					System.out.println(results.size() + " numero de utilizadores no sistema");
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
			}
			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
			} else {
				reply = new byte[0];
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Ocurred during map operation execution", e);
		}
		return reply;
	}

	private byte[] verifyTransfer(Account acc,Account toAc,float amount,byte[] signature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (acc!=null && toAc!=null){
			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(acc.getPublickey()));
			String message = acc.getUsername()+"TRANSFER"+toAc.getUsername()+amount;
			byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageHash = md.digest(messageBytes);
			System.out.println(message);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			byte[] receivedHash = cipher.doFinal(signature);
			if (MessageDigest.isEqual(receivedHash,messageHash)){
				return messageHash;
			}else{
				return null;
			}

		}else {
			System.out.println("Account null");
			return null;
		}
	}

		@SuppressWarnings("unchecked")
	@Override
	public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
		byte[] reply = null;
		boolean hasReply = false;
		List<byte[]> results = null;
		Message response = null;
		String pwd=null;

		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
				ObjectInput objIn = new ObjectInputStream(byteIn);
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
			Message message = (Message)objIn.readObject();
			results = new ArrayList<byte[]>();
			switch (message.getKey()) {
				case "LASTB":
					results = new ArrayList<byte[]>();
					System.out.println("Fez pedido Server 2");
					if(chainRepository.findLast()!=null){
                        int lastid =chainRepository.findLast();
                        Block lastBlock = chainRepository.findDistinctById(lastid);
                        results.add(lastBlock.getBlock());
                    }


					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "PENDING":
					int nTran = utils.byteArrayToint(message.getValues().get("NUMBER"));
					System.out.println("Fez pedido Server 1");
					byte[] pending = sendPending(nTran);
					results = new ArrayList<byte[]>();
					System.out.println("Fez pedido Server 2");
					results.add(pending);
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "HASH":
					int id = (int)Integer.parseInt(new String(message.getValues().get("ID"), StandardCharsets.UTF_8));
					results = new ArrayList<byte[]>();
					Optional<Account> acc_exists = repositorydb.findById(id);
					if (acc_exists.isPresent()) {
						byte[] acc_hash=acc_exists.get().getOperationHash();
						results.add(acc_hash);
						//System.out.println("Obteve ACCOUNT "+acc_exists.get().getUsername());
					}else{
						Optional<TokenTransfer> transfer_exists = transferRepository.findById(id);
						if(transfer_exists.isPresent()){
							results.add(transfer_exists.get().getOperationHash());
							//System.out.println("Obteve tRANSFER ");
						}else{
							//System.out.println("Falhou os IDs");
							results.add("ID DOES NOT EXIST".getBytes(StandardCharsets.UTF_8));
						}
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "BALANCE":
					String username3 = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					Account acc1 = repositorydb.findDistinctByUsername(username3);
					if (repositorydb.findDistinctByUsername(username3)!=null){
						results.add((acc1.getUsername()+" has "+Float.toString(acc1.getAmount())).getBytes(StandardCharsets.UTF_8));
					}else{
						results.add(("Failed to connect to account").getBytes(StandardCharsets.UTF_8));
					}

					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "LEDGER":
					String username2 = new String(message.getValues().get("USERNAME"), StandardCharsets.UTF_8);
					List<TokenTransfer> auxlist;
					if(message.getValues().containsKey("START")&&message.getValues().containsKey("END")){
						long start = Message.bytesToLong(message.getValues().get("START"));
						long end = Message.bytesToLong(message.getValues().get("END"));
						Timestamp st = new Timestamp(start);
						Timestamp et = new Timestamp(end);
						transferRepository.findAllByUserandTimestamp(username2,st,et);
						auxlist = (List<TokenTransfer>) transferRepository.findAllByUserandTimestamp(username2,st,et);
					}else{
						auxlist = (List<TokenTransfer>) transferRepository.findByUsername(username2);
					}
					for(TokenTransfer tr : auxlist){
						if(tr.getFrom().getUsername().equals(tr.getTo().getUsername())){
							results.add(("Deposit on: "+tr.getFrom().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}else{
							results.add(("Transfer from: "+tr.getFrom().getUsername()+" to: "+tr.getTo().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				case "GLOBAL_LEDGER":
					List<TokenTransfer> auxlist1;
					if(message.getValues().containsKey("START")&&message.getValues().containsKey("END")){
						long start = Message.bytesToLong(message.getValues().get("START"));
						long end = Message.bytesToLong(message.getValues().get("END"));
						Timestamp st = new Timestamp(start);
						Timestamp et = new Timestamp(end);
						auxlist1 = (List<TokenTransfer>) transferRepository.findAllByTimestamp(st,et);
					}else{
						auxlist1 = (List<TokenTransfer>) transferRepository.findAll();
					}
					for(TokenTransfer tr : auxlist1){
						if(tr.getFrom().getUsername().equals(tr.getTo().getUsername())){
							results.add(("Deposit on: "+tr.getFrom().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}else{
							results.add(("Transfer from: "+tr.getFrom().getUsername()+" to: "+tr.getTo().getUsername()+" with a amount of: "+tr.getAmount()).getBytes(StandardCharsets.UTF_8));
						}
					}
					response = Message.results(results);
					objOut.writeObject(response);
					hasReply = true;
					break;
				default:
					logger.log(Level.WARNING, "in appExecuteUnordered only read operations are supported");
			}
			if (hasReply) {
				objOut.flush();
				byteOut.flush();
				reply = byteOut.toByteArray();
			} else {
				reply = new byte[0];
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Ocurred during map operation execution", e);
		}

		return reply;
	}

	/*private void keySet(ObjectOutput out) throws IOException, ClassNotFoundException {
		Set<K> keySet = replicaMap.keySet();
		int size = replicaMap.size();
		out.writeInt(size);
		for (K key : keySet)
			out.writeObject(key);
	}
*/
	@Override
	public byte[] getSnapshot()  {

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutput objOut = new ObjectOutputStream(byteOut)) {
			try{
				List<Account> accounts= (List<Account>) repositorydb.findAll();
				List<TokenTransfer> transfers = (List<TokenTransfer>) transferRepository.findAll();
				//System.out.println(accounts.get(0).getUsername());
				Repository backup = new Repository(accounts,transfers);
				objOut.writeObject(backup);
				return byteOut.toByteArray();
			}catch (NullPointerException e){}
			System.out.println("falhou respositorio");
			Repository backup = new Repository(new ArrayList<Account>(),new ArrayList<TokenTransfer>());
			objOut.writeObject(backup);
			return byteOut.toByteArray();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while taking snapshot", e);
		}
		return new byte[0];
	}


	@SuppressWarnings("unchecked")
	@Override
	public void installSnapshot(byte[] state) {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {
			Repository repository = (Repository) objIn.readObject();
			repositorydb.saveAll(repository.getAccounts());
			transferRepository.saveAll(repository.getTransfers());
		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Error while installing snapshot", e);
		}
	}

}
