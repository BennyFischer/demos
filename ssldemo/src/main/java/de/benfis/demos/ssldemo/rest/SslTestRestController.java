package de.benfis.demos.ssldemo.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller to test HTTPS calls
 * 
 * @author benja
 *
 */
@RestController(value = "rest/ssl/")
public class SslTestRestController {

	private Logger LOGGER = LoggerFactory.getLogger(SslTestRestController.class);

	@Autowired
	private ResourceLoader resourceLoader;

	@Value("${positive.url:https://localhost:443}")
	private String positiveUrl;

	@Value("${negative.url:https://localhost:444}")
	private String negativeUrl;

	@Value("${positive.certpath:classpath:nginx.crt}")
	private String positiveCertPath;

	@Value("${negative.certpath:classpath:nginx_noHost.crt}")
	private String negativeCertPath;

	@Autowired
	Environment environment;

	/**
	 * Runs positive test for certificate
	 * 
	 * @return
	 */
	@GetMapping(path = "positive", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> positiveSslTest() {
		LOGGER.info("Positive testing");
		String result;
		try {
			InputStream inputStream = resourceLoader.getResource(positiveCertPath).getInputStream();
			createAndSetSSLContext(inputStream);
			result = callUrlAndReturnResultAsString(positiveUrl);
			inputStream.close();
		} catch (Exception e) {
			result = e.getMessage();
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Runs negative test for certificate
	 * 
	 * @return
	 */
	@GetMapping(path = "negative", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> negativeSslTest() {
		LOGGER.info("Negative testing");
		String result;
		try {
			InputStream inputStream = resourceLoader.getResource(negativeCertPath).getInputStream();
			createAndSetSSLContext(inputStream);
			result = callUrlAndReturnResultAsString(negativeUrl);
			inputStream.close();
		} catch (Exception e) {
			result = e.getMessage();
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * Calls given URL and returns the target page/url as string. In case of an
	 * exception the message of the exception is returned as string.
	 * 
	 * @param urlToVisit
	 * @return String representation of target URL or message of exception
	 */
	private String callUrlAndReturnResultAsString(String urlToVisit) {
		URL url;
		try {
			url = new URL(urlToVisit);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			StringBuilder textBuilder = new StringBuilder();
			try (Reader reader = new BufferedReader(
					new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {
				int c = 0;
				while ((c = reader.read()) != -1) {
					textBuilder.append((char) c);
				}
				return textBuilder.toString();
			}
		} catch (Exception e) {
			LOGGER.error("Error accessing URL {}", negativeUrl, e);
			return e.getMessage();
		}
	}

	/**
	 * Creates a key store and sets it to SSL context of current runtime
	 * 
	 * @param certificateToTrust
	 * @throws Exception
	 */
	private void createAndSetSSLContext(InputStream certificateToTrust) throws Exception {
		try {
			KeyStore keyStoreToTrust = getKeyStore(certificateToTrust);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStoreToTrust);
			sslContext.init(null, tmf.getTrustManagers(), null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Creates key store with given {@link InputStream}
	 * 
	 * @param certificateToTrust
	 * @return KeyStore containing given certificate (and only that certificate)
	 *         with name SSLCert.
	 * @throws Exception in case of error
	 */
	private KeyStore getKeyStore(InputStream certificateToTrust) throws Exception {
		try {
			KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
			clientKeyStore.load(null, null);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(clientKeyStore, null);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(certificateToTrust);
			clientKeyStore.setCertificateEntry("SSLCert", cert);
			return clientKeyStore;
		} catch (Exception e) {
			LOGGER.error("Error generating keystore {}", e.getMessage());
			throw e;
		}
	}

	/**
	 * Prints usage informations
	 */
	@PostConstruct
	private void printUsageInfo() {
		LOGGER.info("------------------ Usage ------------------");
		LOGGER.info("Positive testing for configured cert: {} and target {} call http://localhost:{}/positive",
				positiveCertPath, positiveUrl, environment.getProperty("server.port"));
		LOGGER.info("Negative testing for configured cert: {} and target {} call http://localhost:{}/negative",
				negativeCertPath, negativeUrl, environment.getProperty("server.port"));
		LOGGER.info("-------------------------------------------");
	}
}