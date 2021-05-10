package com.work.api.open.model.client;

import com.http.network.model.ClientModel;

/**
 * Created by Administrator on 2019/4/24
 * Description
 */

public class OpenImageConfig extends ClientModel {

    /**
     * bucket : cheoa
     * endPoint : https://oss.cheoa.cn/
     * keyId : STS.NKL3Qxm4R6UH9tsdAP8nkp4cG
     * expiration : 2019-04-24T05:38:36Z
     * keySecret : 8AUb8h9c174a9qSCKY9B4pBmEgHPkW4s7cbPnfLR6DSV
     * region : cn-hangzhou
     * token : CAISkwJ1q6Ft5B2yfSjIr4j5eOvMgOtzgZejO1LCgEEFNOFHn/HIpTz2IHpFeXJgBukXsvQylGhR6PoclqB4S5JCTAmcNZIoPU7Wd8/kMeT7oMWQweEuuv/MQBquaXPS2MvVfJ+OLrf0ceusbFbpjzJ6xaCAGxypQ12iN+/m6/Ngdc9FHHP7D1x8CcxROxFppeIDKHLVLozNCBPxhXfKB0ca3WgZgGhku6Ok2Z/euFiMzn+Ck7BN/dSseMb4P5Y9ZscuDe3YhrImKvDztwdL8AVP+atMi6hJxCzKpNn1ASMKuUTbaLqLq4wzfFIpPvFkR/Be3/H4lOxlvOvIjJjwyBtLMuxTXj7WWIe62szAFfMBuE/o2UkQUBqAAZekl9iC9EHe3FVktYOAjCAlR0D5mEWMitWW6fUCmgyoq1bw5xlUzvgGXtgVZ6EVkuI3MDx8dmCUz94nLlEbmbXDGijLY7seypf/LYqL5mG9xsHG1QVNhn1TsCUQnBEn3B53xWP6B49K7K5+A8DBcIlOCymyF1i2yObcJCgILReA
     */

    private String bucket;
    private String endPoint;
    private String keyId;
    private String expiration;
    private String keySecret;
    private String region;
    private String token;

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

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public void setKeySecret(String keySecret) {
        this.keySecret = keySecret;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
