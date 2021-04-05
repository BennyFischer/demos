# SSL Test Demo
This Demo helps to test SSL connections using a simple scenario by calling an URL with a provided certificate. One scenario for positive testing. One for negative testing.

## What you need
1. Java 11 or newer (please use google for setup)
2. Maven (please use google for setup)
3. Nginx server (http://nginx.org/en/download.html)
4. OpenSSL (https://www.openssl.org/source/, or use Linux or WSL)

### Setup Nginx with HTTPS and self signed certificate (positive scenario)
#### Create self signed certificate (with localhost Common Name)
1. Install OpenSSL (or use Linux of your choice or WSL1/2)
2. Go to folder of your choice and run
```bash
openssl req -x509 -nodes -days 365 -subj "/CN=localhost" -newkey rsa:2048 -keyout ./nginx.key -out ./nginx.crt
```
This will create certificates for your local Nginx server. 

#### Nginx config
1. Unzip Nginx package
2. Go to nginx-root/conf and create a copy of nginx.conf file and name it nginx_pos.conf
3. Make the following adjustments in nginx_pos.conf
```
...
server {
        listen  443 ssl;
        ssl_certificate     nginx.crt;
        ssl_certificate_key nginx.key;
        ssl_protocols       TLSv1 TLSv1.1 TLSv1.2;
        server_name  localhost;
        ...
}
...
```
4. Copy the certificates nginx.key and nginx.crt to nginx-root/conf
5. start Nginx using command in nginx-root:
```cmd
PS C:\nginx-1.18.0> .\nginx.exe -c .\conf\nginx_pos.conf
```
6. Call https://localhost:443 and verify if Nginx is running

To stop Nginx call: 
```cmd
PS C:\dev\nginx-1.18.0> .\nginx.exe -s stop
```

### Setup Nginx with HTTPS and self signed certificate (negative scenario)
#### Create self signed certificate
1. Install OpenSSL (or use Linux of your choice or WSL1/2)
2. Go to folder of your choice and run
```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./nginx_noHost.key -out ./nginx_noHost.crt
```
Do not answer the questions. Just accept all of them by hitting "Enter". <br>
This will create certificates for your local Nginx server. Using no hostname.

#### Nginx config
1. Unzip Nginx package
2. Go to nginx-root/conf and create a copy of nginx.conf file and name it nginx_negative.conf
3. Make the following adjustments in nginx_negative.conf
```
...
server {
        listen  444 ssl;
        ssl_certificate     nginx_noHost.crt;
        ssl_certificate_key nginx_noHost.key;
        ssl_protocols       TLSv1 TLSv1.1 TLSv1.2;
        server_name  localhost;
        ...
}
...
```
4. Copy the certificates nginx_noHost.key and nginx_noHost.crt to nginx-root/conf
5. start Nginx using command in nginx-root:
```cmd
PS C:\nginx-1.18.0> .\nginx.exe -c .\conf\nginx_negative.conf
```
6. Call https://localhost:444 and verify if Nginx is running

To stop Nginx call: 
```cmd
PS C:\dev\nginx-1.18.0> .\nginx.exe -s stop
```

### Prepare and run Testapplication
After Nginx setup you can prepare and run the Testapplication.

1. Copy both of mentioned .cer files from above to ssldemo\src\main\resources
2. Run the application by running the SslDemonstrationApplication class in e.g. Eclipse or run maven build and start with your local JRE: 
```cmd 
cd ssldemo
mvn clean install
java -jar .\target\ssldemo-1.0.0.jar
```
3. After application startup you can access http://localhost:8080/positive for positive testing. You should see the Nginx welcome screen.
4. After application startup you can access http://localhost:8080/negative for negative testing. You should see a message stating the error and the stacktrace in your console/IDE. 

#### Advanced configuration
You can set the following properties if you want to change the setup above.
```properties
# Spring boot server port
server.port=8080
# URL for positive test
positive.url=https://localhost:443
# URL for negative test
negative.url=https://localhost:444
# Certificate to use for positive URL test
positive.certpath=classpath:nginx.crt
# Certificate to use for negative URL test
negative.certpath=classpath:nginx_noHost.crt
```