package io.mangoo.controllers;

import static io.mangoo.test.hamcrest.RegexMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ApplicationControllerTest {
    private static final String JSON = "{\"foo\":\"bar\"}";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String TEXT_HTML = "text/html; charset=UTF-8";

    @Test
    public void testIndex() {
        //given
        MangooResponse response = MangooRequest.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    public void testRequest() {
        //given
        MangooResponse response = MangooRequest.get("/request").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/request"));
    }

    @Test
    public void testRedirectWithDisableRedirects() {
        //given
        MangooResponse response = MangooRequest.get("/redirect").withDisableRedirects(true).execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    public void testRedirectWithoutDisableRedirects() {
        //given
        MangooResponse response = MangooRequest.get("/redirect").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    public void testPlainText() {
        //given
        MangooResponse response = MangooRequest.get("/text").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    public void testNotFound() {
        //given
        MangooResponse response = MangooRequest.get("/foo").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }

    @Test
    public void testForbidden() {
        //given
        MangooResponse response = MangooRequest.get("/forbidden").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }

    @Test
    public void testBadRequest() {
        //given
        MangooResponse response = MangooRequest.get("/badrequest").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }

    @Test
    public void testUnauthorized() {
        //given
        MangooResponse response = MangooRequest.get("/unauthorized").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    public void testAdditionalHeaders() {
        //given
        MangooResponse response = MangooRequest.get("/header").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), equalTo("https://mangoo.io"));
    }

    @Test
    public void testBinaryDownload() throws IOException {
        //given
        Config config = Application.getInjector().getInstance(Config.class);
        String host = config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
        int port = config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());
        File file = new File(UUID.randomUUID().toString());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        
        //when
        CloseableHttpClient httpclient = HttpClients.custom().build();
        HttpGet httpget = new HttpGet("http://" + host + ":" + port + "/binary");
        CloseableHttpResponse response = httpclient.execute(httpget);
        fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
        fileOutputStream.close();
        response.close();

        //then
        assertThat(response.getStatusLine().getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(FileUtils.readFileToString(file), equalTo("This is an attachment"));
        assertThat(file.delete(), equalTo(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEtag() {
        //given
        MangooResponse response = MangooRequest.get("/etag").execute();
        String etag = response.getHeader(Headers.ETAG_STRING);

        //then
        assertThat(etag, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(etag, matches("^[a-f0-9]{32}$"));

        //given
        response = MangooRequest.get("/etag").withHeader(Headers.IF_NONE_MATCH_STRING, etag).execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_MODIFIED));
        assertThat(response.getContent(), equalTo(""));
    }
    
    @Test 
    public void testPost() {
        //given
        MangooResponse response = MangooRequest.post("/post")
                .withRequestBody("Winter is coming!")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("Winter is coming!"));
    }
    
    @Test 
    public void testPut() {
        //given
        MangooResponse response = MangooRequest.put("/put")
                .withRequestBody("The king of the north!")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("The king of the north!"));
    }
    
    @Test 
    public void testJsonPathWithPost() {
        //given
        MangooResponse response = MangooRequest.post("/jsonpathpost")
                .withRequestBody(JSON)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }
    
    @Test 
    public void testJsonPathWithPut() {
        //given
        MangooResponse response = MangooRequest.put("/jsonpathput")
                .withRequestBody(JSON)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }
    
    @Test 
    public void testJsonBoonWithPost() {
        //given
        MangooResponse response = MangooRequest.post("/jsonboonpost")
                .withRequestBody(JSON)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }
    
    @Test 
    public void testJsonBoonWithPut() {
        //given
        MangooResponse response = MangooRequest.put("/jsonboonput")
                .withRequestBody(JSON)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON));
    }
}