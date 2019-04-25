/*
 *BSD 2-Clause License
 *
 *Copyright (c) 2019, itinordic All rights reserved.
 *
 *Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 *conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 *IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 *THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package ca.uhn.fhir.jpa.starter.util2;

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
