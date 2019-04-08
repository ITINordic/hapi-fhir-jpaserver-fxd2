package ca.uhn.fhir.jpa.starter.alternative;

import javax.annotation.Nonnull;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 *
 * @author oslo
 */
@Configuration
public class DhisConfiguration {

    @Nonnull
    @Bean(destroyMethod = "close")
    public CloseableHttpClient dhisHttpClient() {
        // do neither cache cookies nor authentication when connection
        // ise used for different users
        return HttpClientBuilder.create()
                .useSystemProperties()
                .disableCookieManagement()
                .disableAuthCaching()
                .build();
    }

    @Bean
    @Nonnull
    public ClientHttpRequestFactory dhisClientHttpRequestFactory(@Nonnull @Qualifier("dhisHttpClient") HttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    @Nonnull
    public RestTemplate dhisRestTemplate(@Nonnull @Qualifier("dhisClientHttpRequestFactory") ClientHttpRequestFactory clientHttpRequestFactory) {
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        final DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory("http://localhost:8080/api/30");
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        return restTemplate;
    }

}
