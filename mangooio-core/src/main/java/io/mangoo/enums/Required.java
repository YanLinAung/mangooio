package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum Required {
    ACCOUNT_NAME("account name can not be null"),
    APPLICATION_SECRET("application secret can not be null"),
    AUTHENTICITY("authenticity can not be null"),
    BCC_RECIPIENT("bcc recipient can not be null"),
    BINDING("binding can not be null"),
    BODY("body can not be null"),
    CACHE_PROVIDER("cacheProvider can not be null"),
    CC_RECIPIENT("cc recipient can not be null"),
    CHANNEL("channel can not be null"),
    CHARSET("charset can not be null"),
    CLASS("class can not be null"),
    COMPONENT_TYPE("component type can not be null"),
    CONFIG("config can not be null"),
    CONFIG_FILE("config file can not be null"),
    CONNECTION("connection can not be null"),
    CONTENT("content can not be null"),
    CONTENT_TYPE("content type can not be null"),
    CONTROLLER_CLASS("controller class can not be null"),
    CONTROLLER_INSTANCE("controller instance can not be null"),
    CONTROLLER_METHOD("controller method can not be null"),
    CONTROLLER_NAME("controller name can not be null"),
    COOKIE("cookie can not be null"),
    CRON("cron can no be null"),
    CRYPTO("crypto can not be null"),
    DATA("data can not be null"),
    DATE("date can not be null"),
    EHCACHE("ehCache can not be null"),
    ENCRYPTED_TEXT("encrypted text can not be null"),
    EVENT_CALLBACK("event callback can not be null"),
    EXPIRES("expires can not be null"),
    FILE("file can not be null"),
    FROM("from can not be null"),
    GROUP_NAME("group name can not be null"),
    HASH("hash can not be null"),
    HEADERS("headers can not be null"),
    HTTP_HANDLER("httpHandler can not be null"),
    HTTP_SERVER_EXCHANGE("httpServerExchange can not be null"),
    IDENTITY("identity can not be null"),
    JOB_DETAIL("job detail can not be null"),
    JSON("json can not be null"),
    JSON_OBJECT("json object can not be null"),
    KEY("key can not be null"),
    LOCAL_DATE("localDate can not be null"),
    LOCAL_DATE_TIME("localDateTime can not be null"),
    LOCALE("locale can not be null"),
    MAP("map can not be null"),
    MESSAGES("messages can not be null"),
    METHOD("method can not be null"),
    METHOD_PARAMETERS("method parameters can not be null"),
    MODE("mode can not be null"),
    NAME("name can not be null"),
    OAUTH_PROVIDER("OAuthProvider can not be null"),
    OBJECT("object can not be null"),
    PACKAGE_NAME("package name"),
    PASSWORD("password can not be null"),
    PATH("path can not be null"),
    PLAIN_TEXT("plan text can not be null"),
    RECIPIENT("recipient can not be null"),
    REDIRECT_TO("redirect to can not be null"),
    REQUEST_HELPER("requesthelper can not be null"),
    REQUEST_PARAMETER("request parameter can not be null"),
    RESPONSE("response can not be null"),
    ROUTE("route can not be null"),
    ROUTE_TYPE("route type can not be null"),
    SALT("salt can not be null"),
    SCHEDULER("scheduler can not be null"),
    SECRET("secret can not be null"),
    SOURCE_PATH("source path can not be null"),
    STACK_TRACE_ELEMENT("stack trace element can not be null"),
    START("start can not be null"),
    SUBJECT("subject can not be null"),
    TEMPLATE("template can not be null"),
    TEMPLATE_ENGINE("tempalte engine can not be null"),
    TEMPLATE_NAME("template name can not be null"),
    TRIGGER("trigger can not be null"),
    TRIGGER_FIRE_BUNDLE("trigger fire bundle can not be null"),
    URI("uri can not be null"),
    URI_CONNECTIONS("uri connections can not be null"),
    URL("url can not be null"),
    USERNAME("username can not be null"),
    VALIDATOR("validator can not be null"),
    VALUE("value can not be null"),
    VALUES("values can not be null"),
    TWO_FACTOR_HELPER("twofactorhelper can not be null");

    private final String value;

    Required (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}