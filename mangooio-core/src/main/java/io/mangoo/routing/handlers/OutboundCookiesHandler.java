package io.mangoo.routing.handlers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

/**
 *
 * @author svenkubiak
 *
 */
public class OutboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(OutboundCookiesHandler.class);
    private static final String SAME_SITE_MODE = "Strict";
    private Attachment attachment;
    private Config config;
    
    @Inject
    public OutboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestHelper.ATTACHMENT_KEY);

        setSessionCookie(exchange);
        setFlashCookie(exchange);
        setAuthenticationCookie(exchange);

        nextHandler(exchange);
    }

    /**
     * Sets the session cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setSessionCookie(HttpServerExchange exchange) {
        Session session = this.attachment.getSession();
        
        if (session != null && session.hasChanges()) {
            Instant instant = session.getExpires().atZone(ZoneId.systemDefault()).toInstant();
            
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setClaim(ClaimKey.AUTHENTICITY.toString(), session.getAuthenticity());
            jwtClaims.setClaim(ClaimKey.VERSION.toString(), this.config.getSessionCookieVersion());
            jwtClaims.setClaim(ClaimKey.DATA.toString(), session.getValues());
            jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(instant.toEpochMilli()));
            
            JsonWebSignature jsonWebSignature = new JsonWebSignature();
            jsonWebSignature.setKey(new HmacKey(this.config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)));
            jsonWebSignature.setPayload(jwtClaims.toJson());
            jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);

            //FIX ME: Need additonal code for encryption
//          if (this.config.isSessionCookieEncrypt()) {
//              jwt = this.attachment.getCrypto().encrypt(jwt, this.config.getSessionCookieEncryptionKey());
//          }
            
            try {
                final Cookie cookie = new CookieImpl(this.config.getSessionCookieName())
                        .setValue(jsonWebSignature.getCompactSerialization())
                        .setSameSite(true)
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setHttpOnly(true)
                        .setExpires(Date.from(instant))
                        .setSecure(this.config.isSessionCookieSecure());

                exchange.setResponseCookie(cookie);
            } catch (JoseException e) {
                LOG.error("Failed to generate session cookie", e);
            }
        }
    }

    /**
     * Sets the authentication cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setAuthenticationCookie(HttpServerExchange exchange) {
        Authentication authentication = this.attachment.getAuthentication();
        
        if (authentication != null && authentication.hasAuthenticatedUser()) {
            final String cookieName = this.config.getAuthenticationCookieName();
            if (authentication.isLogout()) {
                Cookie cookie = exchange.getRequestCookies().get(cookieName)
                    .setSecure(this.config.isAuthenticationCookieSecure())
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true);
                
                exchange.setResponseCookie(cookie);
            } else {
                Instant instant;
                if (authentication.isRememberMe()) {
                    instant = LocalDateTime.now().plusHours(this.config.getAuthenticationCookieRememberExpires()).atZone(ZoneId.systemDefault()).toInstant();
                } else {
                    instant = authentication.getExpires().atZone(ZoneId.systemDefault()).toInstant();
                }
                
                JwtClaims jwtClaims = new JwtClaims();
                jwtClaims.setSubject(authentication.getAuthenticatedUser());
                jwtClaims.setClaim(ClaimKey.VERSION.toString(), this.config.getAuthenticationCookieVersion());
                jwtClaims.setClaim(ClaimKey.TWO_FACTOR.toString(), authentication.isTwoFactor());
                jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(instant.toEpochMilli()));
                
                JsonWebSignature jsonWebSignature = new JsonWebSignature();
                jsonWebSignature.setKey(new HmacKey(this.config.getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8)));
                jsonWebSignature.setPayload(jwtClaims.toJson());
                jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
                
                //FIX ME Need additional code for encryption
//                if (this.config.isAuthenticationCookieEncrypt()) {
//                    jwt = this.attachment.getCrypto().encrypt(jwt, this.config.getAuthenticationCookieEncryptionKey());
//                }

                try {
                    Cookie cookie = new CookieImpl(cookieName)
                            .setValue(jsonWebSignature.getCompactSerialization())
                            .setSecure(this.config.isAuthenticationCookieSecure())
                            .setHttpOnly(true)
                            .setSameSite(true)
                            .setSameSiteMode(SAME_SITE_MODE)
                            .setExpires(Date.from(instant));

                    exchange.setResponseCookie(cookie);
                } catch (JoseException e) {
                    LOG.error("Failed to generate authentication cookie", e);
                }
            }
        }
    }

    /**
     * Sets the flash cookie to current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     * @throws MangooCookieException 
     */
    protected void setFlashCookie(HttpServerExchange exchange) {
        Flash flash = this.attachment.getFlash();
        Form form = this.attachment.getForm();
        
        if (flash != null && !flash.isDiscard() && (flash.hasContent() || form.flashify())) {
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setClaim(ClaimKey.DATA.toString(), flash.getValues());
            
            if (form.flashify()) {
                jwtClaims.setClaim(ClaimKey.FORM.toString(), CodecUtils.serializeToBase64(form));
            }
            
            Instant instant = LocalDateTime.now().plusSeconds(60).atZone(ZoneId.systemDefault()).toInstant();
            jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(instant.toEpochMilli()));
            
            JsonWebSignature jsonWebSignature = new JsonWebSignature();
            jsonWebSignature.setKey(new HmacKey(this.config.getApplicationSecret().getBytes(Charsets.UTF_8)));
            jsonWebSignature.setPayload(jwtClaims.toJson());
            jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
            
            try {
                final Cookie cookie = new CookieImpl(this.config.getFlashCookieName())
                        .setValue(jsonWebSignature.getCompactSerialization())
                        .setSecure(this.config.isFlashCookieSecure())
                        .setHttpOnly(true)
                        .setSameSite(true)
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setExpires(Date.from(instant));
                
                exchange.setResponseCookie(cookie);
            } catch (JoseException e) {
                LOG.error("Failed to generate flash cookie", e);
            }
        } else {
            final Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                    .setSecure(this.config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setMaxAge(0);

                exchange.setResponseCookie(cookie);
            }
        }
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(ResponseHandler.class).handleRequest(exchange);
    }
}