package com.work.api.open.model.client;

import com.http.network.model.ClientModel;

/**
 * Created by Administrator on 2019/4/24
 * Description
 */

public class OpenImageConfig extends ClientModel {

    /**
     * expiration : 2021-05-11T07:31:27Z
     * accessKeyId : STS.NUNLRkUQT4KBme2qsQ5VGsc8J
     * accessKeySecret : Bu6TrBcpeZh3usiFKH8q8M7fFvDLYBr82xXia9nc8dm
     * securityToken : CAIS8AF1q6Ft5B2yfSjIr5b7B+jfuI51g4mpb0ODlXMEOdlrnKaTqDz2IHtKfHhoBewesfgznW1Z7vYblqFiW5ZdREXcdZOwfiXtDETzDbDasumZsJYm6vT8a0XxZjf/2MjNGZabKPrWZvaqbX3diyZ32sGUXD6+XlujQ/br4NwdGbZxZASjaidcD9p7PxZrrNRgVUHcLvGwKBXn8AGyZQhKwlMk0zwluf3kk5PBsECH0ALAp7VL99irEP+NdNJxOZpzadCx0dFte7DJuCwqsEMUrPYu0PAcpmub54zMWwkI+XScOu/T6cZ0MBRprXm3xi/lLZEagAEtCI1QR6Z+pbRPB3R6WC6FpzpKbI3+huRT5FX7adqXkidfScKMv0IHXvn8n2kmfm+GP6MWPXZ+/9sVW7CIpK4sNz3qQQAJvMZiN2R+VGRf9yls4sCzfqjVNvZioB8RMLr+LPuvmZ73RDbrAWWZLGzIDh7BclsUTTYJHnE6v8gWNg==
     * requestId : 1AFF03EA-AEDE-400E-804C-37B218186A07
     * bucket : cheoa
     * endPoint : https://oss.cheoa.cn/
     */

    private String expiration;
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String requestId;
    private String bucket;
    private String endPoint;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
