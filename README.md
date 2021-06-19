# CSD_PA1 Raiz
-instalar maven, spring,mysql, java 1.8 com bouncyCastle
-!!!! Nova release do java1.8-u291 pode ter conflito com acesso a keystore, solução utilizada foi por o bouncyCastle como o primeiro CriptoProvider
Utilizei base de dados relacional mysql:
  -criar utilizador csd2021
  -password csd2021
  criar as tabelas para as replicas o nome por default é csd2021, mas pode ser passado como argumento a tabela
  
  
# Docker 
	-docker run -it "image"
	
	Para cada servidor 
	
	-docker exec -it "containerId" bash 
	
	Iniciar o server mysql no docker 
	
	-service mysql start
	
	Mudar diretoria
	
	cd home/work/csd/OA3/RESTAPI
 
  
  Correr Spring na diretoria RESTAPI
  
    -mvn spring-boot:run -Dspring-boot.run.arguments="--id=0 --server.port=8443 --spring.datasource.url=jdbc:mysql://localhost:3306/csd2021"
    
    -mvn spring-boot:run -Dspring-boot.run.arguments="--id=1 --server.port=8444 --spring.datasource.url=jdbc:mysql://localhost:3306/csd2021_r1"
    
    -mvn spring-boot:run -Dspring-boot.run.arguments="--id=2 --server.port=8445 --spring.datasource.url=jdbc:mysql://localhost:3306/csd2021_r2"
    
    -mvn spring-boot:run -Dspring-boot.run.arguments="--id=3 --server.port=8446 --spring.datasource.url=jdbc:mysql://localhost:3306/csd2021_r3"
    
   
   Terminal Interativo na diretoria Terminal 
   
   -java Client localhost:8443
   
   Utilizadores de teste ja com chaves privadas com username e password igual,(teste1,teste2), pode também ser criado novo se o user for diferente de alguma keystore ja presente na diretoria  
    
   
