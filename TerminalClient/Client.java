


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.security.cert.X509Certificate;
import java.util.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import java.sql.Timestamp;
import java.nio.ByteBuffer;


public class Client {
    public static class entry{
        public String username;
        public String password;
        public entry(String username,String password){
            this.username=username;
            this.password=password;
        }
    }
    
    public static byte[] lastBlock=null;
    public static byte[] transactions=null;
    public static byte[] lastBlockHash=null;
    public static int lastBlockId=-1;

    public static X509Certificate generateCertificate(KeyPair keyPair,String username) throws CertificateEncodingException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
        Random rand = new Random(); //instance of random class
        cert.setSerialNumber(BigInteger.valueOf(rand.nextInt(Integer.MAX_VALUE)));   //or generate a random number
        cert.setSubjectDN(new X509Principal("CN="+username));  //see examples to add O,OU etc
        cert.setIssuerDN(new X509Principal("CN="+username)); //same since it is self-signed
        cert.setPublicKey(keyPair.getPublic());
        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.YEAR, 2022);
        myCal.set(Calendar.MONTH, 1);
        myCal.set(Calendar.DAY_OF_MONTH, 1);
        Date endDate = myCal.getTime();
        cert.setNotBefore(new Date());
        cert.setNotAfter(endDate);
        cert.setSignatureAlgorithm("SHA1WithRSAEncryption");
        PrivateKey signingKey = keyPair.getPrivate();
        return cert.generate(signingKey, "BC");
    }


    public static PublicKey PublickKey(String username, String pwd) throws Exception{
        KeyStore ks = KeyStore.getInstance("PKCS12");
        char[] pwdArray = pwd.toCharArray();
        PublicKey pub;
        File file = new File(username+".p12");
        if (file.createNewFile()){
            System.out.println("Cria chave nova");
            ks.load(null, pwdArray);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            kpg.initialize(1024, random);

            KeyPair pair = kpg.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            pub = pair.getPublic();

            X509Certificate certificate = generateCertificate(pair,username);
            X509Certificate[] certChain = new X509Certificate[1];
            certChain[0] =  certificate;
            ks.setKeyEntry("CSDCOIN", (Key)pair.getPrivate(), pwd.toCharArray(), certChain);


            try (FileOutputStream fos = new FileOutputStream(username+".p12")) {
                ks.store(fos, pwdArray);
                fos.close();
            }

        }else{
            InputStream is = new FileInputStream(file);
            ks.load(is, pwd.toCharArray());
            String alias = "CSDCOIN";
            Key key = ks.getKey(alias, pwd.toCharArray());
            // if (key instanceof PrivateKey) {
            // Get certificate of public key
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            byte[] encCertInfo = cert.getEncoded();
            //MessageDigest sha = MessageDigest.getInstance("SHA1");
            //byte[] digest = sha.digest(encCertInfo);


            // Get public key
            pub = cert.getPublicKey();
            System.out.println("Chave ja existente");
            // Return a key pair
            //}
        }
        return pub;
    }

    public static entry login(Console console,SSLContext sslContext) throws Exception {
        String username=null;
        String password = null;
        while(username==null && password==null ){
            System.out.println();
            System.out.println("Select an option:");
            System.out.println("0 - Register");
            System.out.println("1 - Login");
            System.out.println();
            int cmd = Integer.parseInt(console.readLine("Option:"));
            username = console.readLine("Enter the username: ").trim();
            password = console.readLine("Enter the password: ").trim();
            PublicKey pk = PublickKey(username,password);

            URL url1 = new URL("https://localhost:8443/users/new?username="+username+"&pwd="+password);//+"&publickey="+pk.getEncoded());
            HttpsURLConnection con = (HttpsURLConnection) url1.openConnection();
            con.setSSLSocketFactory(sslContext.getSocketFactory());
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "text/plain");
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            try(OutputStream os = con.getOutputStream()) {
                os.write(pk.getEncoded(), 0, pk.getEncoded().length);
            }

            InputStream responseStream = con.getInputStream();
            String text = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

        }
        return new entry(username,password);
    }
    
    public static byte[] intToByteArray(int value){
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }


    public static byte[] floatToByteArray(float value){
        return ByteBuffer.allocate(8).putFloat(value).array();
    }

    public static float byteArrayTofloat(byte[] bytes){
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static int byteArrayToint(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }

    
    public static void main(String[] args) throws Exception, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        String keyPassphrase = "changeit";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("keystore.p12"), keyPassphrase.toCharArray());

        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyPassphrase.toCharArray());

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom());

        Console console = System.console();
        //String ip = args[0];
        boolean exit = false;
        String username=null;
        String password=null;
        String text;
        InputStream responseStream;

        entry aux =login(console,sslContext);
        username= aux.username;
        password= aux.password;

        while (!exit) {
            System.out.println();
            System.out.println("Select an option:");
            //System.out.println("0 - Insert user");
            System.out.println("1 - Get User Balance");
            System.out.println("2 - Deposit");
            System.out.println("3 - Transfer");
            System.out.println("4 - User ledger");
            System.out.println("5 - ledger");
            System.out.println("6 - Operation Hash");
            System.out.println();

            int cmd = Integer.parseInt(console.readLine("Option:"));
            switch (cmd) {
                case 1:
                    System.out.println("Getting Balance");
                    //String username = console.readLine("Enter the username: ").trim();
                    URL url = new URL("https://localhost:8443/users/balance?username="+username);
                    HttpsURLConnection con1 = (HttpsURLConnection) url.openConnection();
                    con1.setSSLSocketFactory(sslContext.getSocketFactory());
                    con1.setRequestProperty("Accept", "application/json");
                    con1.setRequestMethod("GET");

                    responseStream = con1.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;
                /*case 0:
                    System.out.println("New user");
                    String username1 = console.readLine("Enter the username: ").trim();
                    String pwd = console.readLine("Enter the password: ").trim();

                    //ver cerificado com chave publica e privada
                    PublicKey pk = PublickKey(username1,pwd);

                    URL url1 = new URL("https://localhost:8443/users/new?username="+username1+"&pwd="+pwd);//+"&publickey="+pk.getEncoded());
                    HttpsURLConnection con = (HttpsURLConnection) url1.openConnection();
                    con.setSSLSocketFactory(sslContext.getSocketFactory());
                    con.setRequestProperty("Accept", "application/json");
                    con.setRequestProperty("Content-Type", "text/plain");
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);

                    try(OutputStream os = con.getOutputStream()) {
                        os.write(pk.getEncoded(), 0, pk.getEncoded().length);
                    }

                    responseStream = con.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;*/
                case 2:
                    System.out.println("Deposit ");
                    //String username2 = console.readLine("Enter username: ").trim();
                    //String pwd2 = console.readLine("Enter the password: ").trim();
                    int amount = Integer.parseInt(console.readLine("Enter amount: ").trim());
                    URL url2 = new URL("https://localhost:8443/users/deposit?username="+username+"&amount="+amount);
                    HttpsURLConnection con2 = (HttpsURLConnection) url2.openConnection();
                    con2.setSSLSocketFactory(sslContext.getSocketFactory());
                    con2.setRequestProperty("Content-Type", "text/plain");
                    con2.setRequestMethod("POST");

                    con2.setDoOutput(true);


                    List<String> parameters = new ArrayList<String>();
                    parameters.add("DEPOSIT");
                    parameters.add(Integer.toString(amount));
                    byte[] singnated =SignRequest(username,password,parameters);
                    try(OutputStream os = con2.getOutputStream()) {
                        os.write(singnated, 0, singnated.length);
                    }

                    responseStream = con2.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    System.out.println();
                    break;
                case 3:
                    System.out.println("Transfer");
                    //String from = console.readLine("From: ").trim();
                    String to = console.readLine("To: ").trim();
                    float amount2 = Float.parseFloat(console.readLine("Enter amount: ").trim());
                    //String pwd3 = console.readLine("pwd: ").trim();
                    URL url3 = new URL("https://localhost:8443/transfer/csdcoin?from="+username+"&to="+to+"&amount="+amount2);
                    HttpsURLConnection con3 = (HttpsURLConnection) url3.openConnection();
                    con3.setSSLSocketFactory(sslContext.getSocketFactory());
                    con3.setRequestProperty("Content-Type", "text/plain");
                    con3.setRequestMethod("POST");

                    con3.setDoOutput(true);


                    List<String> parameters1= new ArrayList<String>();
                    parameters1.add("TRANSFER");
                    parameters1.add(to);
                    parameters1.add(Float.toString(amount2));
                    byte[] singnated1 =SignRequest(username,password,parameters1);
                    try(OutputStream os = con3.getOutputStream()) {
                        os.write(singnated1, 0, singnated1.length);
                    }

                    responseStream = con3.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;
                case 4:
                    System.out.println("Getting user transactions: ");
                    String username3 = console.readLine("Enter Username: ").trim();
                    String start = console.readLine("StartTime WITH yyyy-mm-dd HH:MM:SS : ").trim();
                    String end = console.readLine("StartTime WITH yyyy-mm-dd HH:MM:SS : ").trim();
                    long stt=0;
                    long ett=System.currentTimeMillis();
                    if(!start.equals("") && !end.equals("")){
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        stt=df.parse(start).getTime();
                        ett =df.parse(end).getTime();
                    }

                    URL url4 = new URL("https://localhost:8443/transfer/ledger?username="+username3+"&start="+stt+"&end="+ett);
                    HttpsURLConnection con4 = (HttpsURLConnection) url4.openConnection();
                    con4.setSSLSocketFactory(sslContext.getSocketFactory());
                    con4.setRequestMethod("GET");
                    responseStream = con4.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;
                case 5:
                    System.out.println("Getting all transactions");
                    System.out.println("If want all results press enter twice");
                    String start1 = console.readLine("StartTime WITH format yyyy-mm-dd HH:MM:SS : ").trim();
                    String end1 = console.readLine("EndTime WITH format yyyy-mm-dd HH:MM:SS : ").trim();
                    long stt1=0;
                    long ett1=System.currentTimeMillis();
                    if(!start1.equals("") && !end1.equals("")){
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        stt1=df.parse(start1).getTime();
                        ett1 =df.parse(end1).getTime();
                    }
                    URL url5 = new URL("https://localhost:8443/transfer/ledger?start="+stt1+"&end="+ett1);
                    HttpsURLConnection con5 = (HttpsURLConnection) url5.openConnection();
                    con5.setSSLSocketFactory(sslContext.getSocketFactory());
                    con5.setRequestMethod("GET");
                    responseStream = con5.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;
                case 6:
                    System.out.println("Getting operation Hash : ");
                    int opId = Integer.parseInt(console.readLine("Enter operation ID: ").trim());
                    URL url6 = new URL("https://localhost:8443/users/hash?id="+opId);
                    HttpsURLConnection con6 = (HttpsURLConnection) url6.openConnection();
                    con6.setSSLSocketFactory(sslContext.getSocketFactory());
                    con6.setRequestMethod("GET");
                    responseStream = con6.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    break;
                case 7:
                   int number = Integer.parseInt(console.readLine("Number of Blocks: ").trim());
                    URL url7 = new URL("https://localhost:8443/transfer/pending?nTransactions="+number);
                    HttpsURLConnection con7 = (HttpsURLConnection) url7.openConnection();
                    con7.setSSLSocketFactory(sslContext.getSocketFactory());
                    con7.setRequestMethod("GET");
                    responseStream = con7.getInputStream();
                    
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
        		int nRead;
       		 byte[] data = new byte[1024];
        		while((nRead = responseStream.read(data,0,data.length)) !=-1){
           			 os.write(data,0,nRead);
        		}
        		//os.flush();
       		pending(os.toByteArray());
                    break;
               case 8:
                    URL url8 = new URL("https://localhost:8443/transfer/LastBlock");
                    HttpsURLConnection con8 = (HttpsURLConnection) url8.openConnection();
                    con8.setSSLSocketFactory(sslContext.getSocketFactory());
                    con8.setRequestMethod("GET");
                    responseStream = con8.getInputStream();
                    
                    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        		int nRead1;
       		 byte[] data1 = new byte[1024];
        		while((nRead1 = responseStream.read(data1,0,data1.length)) !=-1){
           			 os1.write(data1,0,nRead1);
        		}
        		//os.flush();
       		lastBlock(os1.toByteArray());
                    break;
                    case 9:
                    byte[] minedBlock;
                    	byte id[] = new byte[4];
                    	if(lastBlock!=null){
    			System.arraycopy(lastBlock,0,id,0,4);
    			minedBlock=mine(lastBlockId);
    			}else{
    			minedBlock=mine(-1);
    			}
                    	
                    	
                    	 URL url9 = new URL("https://localhost:8443/transfer/mine?username="+username);
                    HttpsURLConnection con9 = (HttpsURLConnection) url9.openConnection();
                    con9.setSSLSocketFactory(sslContext.getSocketFactory());
                    con9.setRequestProperty("Content-Type", "text/plain");
                    con9.setRequestMethod("POST");

                    con9.setDoOutput(true);
                    
                    try(OutputStream os9 = con9.getOutputStream()) {
                        os9.write(minedBlock, 0, minedBlock.length);
                    }

                    responseStream = con9.getInputStream();
                    text = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    System.out.println(text);
                    
                    //ByteArrayOutputStream os2 = new ByteArrayOutputStream();
                    	
                    break;
                    
                default:
                    break;
            }

        }
    }
    
    private static void lastBlock(byte[] block){
    	if(block==null || block.length ==0){
    	System.out.println("Genesis");
    	} else{
    	byte id[] = new byte[4];
    	System.arraycopy(block,0,id,0,4);
    	System.out.println(byteArrayToint(id));
    	lastBlock=block;
    	lastBlockId=byteArrayToint(id);
    	byte[] hash = new byte[32];
    	System.arraycopy(block,72,hash,0,32);
    	lastBlockHash=hash;
    	}
    }
    
    private static void pending(byte[] pending){
    	byte[] nCoinbase = new byte[4];
    	System.out.println(pending.length);
    	System.arraycopy(pending,0,nCoinbase,0,4);
    	int nCoinBases = byteArrayToint(nCoinbase);

	int bytesConsumed=4;
	for(int i=0;i<nCoinBases;i++){
		byte[] aux = new byte[4];
		System.arraycopy(pending,bytesConsumed,aux,0,4);
		int id = byteArrayToint(aux);
		System.arraycopy(pending,bytesConsumed+4,aux,0,4);
		int nCarachters= byteArrayToint(aux);
		byte[] username= new byte[nCarachters];
		System.arraycopy(pending,bytesConsumed+8,username,0,nCarachters);
		String name = new String(username,StandardCharsets.ISO_8859_1);
		System.arraycopy(pending,bytesConsumed+8+nCarachters,aux,0,4);
		int amount = byteArrayToint(aux);
		System.out.println(id+name+amount);
	    	bytesConsumed+=(12+nCarachters);
	}
	
    	transactions=pending;
    	System.out.println(transactions);
    	
    }
     public static byte[] mine(int lastBlockId )throws Exception{
    	byte[] header=new byte[0];
    	byte[] id= intToByteArray(lastBlockId+1);
    	byte[] finalHash=new byte[0];
    	int nonce = 0;
    	boolean valid=false;
    	MessageDigest md = MessageDigest.getInstance("SHA-256");
    	if(lastBlockHash==null){
    	lastBlockHash = new byte[32];
    		for(int i =0;i<32;i++){
    			lastBlockHash[i]=0;
    		}
    	}
    	while(!valid){
    		ByteArrayOutputStream os = new ByteArrayOutputStream();
    		os.write(id);
    		os.write(intToByteArray(nonce));
    		os.write(md.digest(transactions));
    		os.write(lastBlockHash);
    		header= os.toByteArray();
        	finalHash = md.digest(header);
        	//System.out.println(Arrays.toString(finalHash));
        	valid=true;
        	for(int j=0;j<3;j++){
        		if(finalHash[j] !=0){
        		valid=false;
        		}
        	}
        	nonce++;
    	}
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	os.write(header);
    	os.write(finalHash);
    	os.write(transactions);
    	
    	return os.toByteArray();
    }

    
    

    private static byte[] SignRequest(String username, String pwd, List<String> parameters) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        InputStream is = new FileInputStream(username+".p12");
        ks.load(is, pwd.toCharArray());
        String alias = "CSDCOIN";
        Key key = ks.getKey(alias, pwd.toCharArray());
        String message = new String(username);
        for (String str : parameters){
            message=message.concat(str);
        }

        System.out.println(message);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(messageBytes);


        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] digitalSignature = cipher.doFinal(messageHash);
        return digitalSignature;
    }
}
