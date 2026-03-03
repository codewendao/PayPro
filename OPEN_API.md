# 外部订单接口文档

## 接口概述

该接口用于外部系统向支付系统添加订单，支持MD5签名验证、时间戳校验、事务处理等功能。

## 接口信息

- **接口路径**: `POST /api/openapi/orders`
- **请求方法**: POST
- **Content-Type**: application/json

## 请求参数

| 参数名 | 类型 | 必填 | 说明                                       |
|--------|------|------|------------------------------------------|
| orderNo | String | 是 | 订单号，唯一标识                                 |
| amount | BigDecimal | 是 | 订单金额，必须大于0且小于等于100000                    |
| payType | String | 是 | 支付方式（alipay/wechat/alipay_dmf/wechat_zs） |
| nickName | String | 否 | 用户昵称                                     |
| description | String | 否 | 订单描述                                     |
| email | String | 否 | 用户邮箱                                     |
| notifyUrl | String | 否 | 异步通知地址                                   |
| userId | String | 否 | 用户ID                                     |
| productId | Long | 否 | 产品ID                                     |
| timestamp | Long | 是 | 请求时间戳（毫秒），有效期5分钟                         |
| sign | String | 是 | MD5签名                                    |

## 签名算法

1. 将所有参数（除sign外）按字母顺序排序
2. 拼接成 `key1=value1&key2=value2&...&key=secretKey` 格式
3. 对拼接后的字符串进行MD5加密，并转为大写

### 签名示例

假设配置的密钥为：`your_openapi_secret_key_here`

参数：
```json
{
  "orderNo": "EXT20250303001",
  "amount": 10.00,
  "payType": "alipay",
  "timestamp": 1733232000000
}
```

排序后的参数：
```
amount=10.00&orderNo=EXT20250303001&payType=alipay&timestamp=1733232000000&key=your_openapi_secret_key_here
```

MD5签名：`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

## 响应格式

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderId": "uuid-generated-id",
    "orderNo": "EXT20250303001",
    "amount": 10.00,
    "payType": "alipay",
    "payNum": "随机支付标识",
    "state": 0,
    "message": "订单创建成功",
    "timestamp": 1733232000000
  }
}
```

### 失败响应

```json
{
  "code": 400,
  "msg": "错误信息描述",
  "data": null
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 签名验证失败 |
| 402 | 时间戳无效或已过期 |
| 403 | 订单号已存在 |
| 404 | 金额错误 |
| 500 | 系统内部异常 |

## 请求示例

### cURL 示例

```bash
curl -X POST http://localhost:8889/api/openapi/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "EXT20250303001",
    "amount": 10.00,
    "payType": "alipay",
    "nickName": "测试用户",
    "description": "测试订单",
    "email": "test@example.com",
    "userId": "USER001",
    "productId": 1,
    "notifyUrl": "http://example.com/notify",
    "timestamp": 1733232000000,
    "sign": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  }'
```

### Java 示例

```java
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.crypto.SecureUtil;

public class ExternalOrderClient {
    
    private static final String API_URL = "http://localhost:8889/api/openapi/orders";
    private static final String SECRET_KEY = "your_openapi_secret_key_here";
    
    public static void main(String[] args) {
        JSONObject params = new JSONObject();
        params.put("orderNo", "EXT20250303001");
        params.put("amount", 10.00);
        params.put("payType", "alipay");
        params.put("nickName", "测试用户");
        params.put("description", "测试订单");
        params.put("email", "test@example.com");
        params.put("userId", "USER001");
        params.put("productId", 1);
        params.put("notifyUrl", "http://example.com/notify");
        params.put("timestamp", System.currentTimeMillis());
        
        String sign = generateSign(params);
        params.put("sign", sign);
        
        String response = HttpRequest.post(API_URL)
            .header("Content-Type", "application/json")
            .body(params.toString())
            .execute()
            .body();
        
        System.out.println(response);
    }
    
    private static String generateSign(JSONObject params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            Object value = params.get(key);
            if (value != null && !"".equals(value) && !"sign".equals(key)) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        sb.append("key=").append(SECRET_KEY);
        
        return SecureUtil.md5(sb.toString()).toUpperCase();
    }
}
```

## 配置说明

在 `application.yml` 中配置外部接口密钥：

```yaml
paypro:
  openapi:
    secret: your_openapi_secret_key_here
```

## 安全特性

1. **MD5签名验证**：确保请求参数未被篡改
2. **时间戳校验**：防止重放攻击，时间戳有效期5分钟
3. **订单号唯一性**：防止重复订单
4. **金额限制**：单笔订单金额不超过100000元
5. **事务处理**：确保订单创建的原子性

## 注意事项

1. 请妥善保管密钥，不要泄露给第三方
2. 时间戳必须使用毫秒级时间戳
3. 订单号必须唯一，重复的订单号会返回错误
4. 建议在生产环境中使用HTTPS协议
5. 建议实现IP白名单等额外的安全措施

## 测试页面

访问 `http://localhost:8889/open-api-test.html` 可以使用测试页面进行接口测试。
