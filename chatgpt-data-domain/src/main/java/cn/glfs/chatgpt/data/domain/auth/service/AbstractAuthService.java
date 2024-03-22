package cn.glfs.chatgpt.data.domain.auth.service;


import cn.glfs.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import cn.glfs.chatgpt.data.domain.auth.model.valobj.AuthTypeVo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码校验并生成token
 */
@Slf4j
//抽象类可以选择性的去实现接口的方法，而抽象类的子类必须实现实现未实现的方法
public abstract class AbstractAuthService implements IAuthService{

    /** SecretKey 后期通过配置的方式使用 */
    //在 JWT（JSON Web Token）中，密钥（key）用于对 JWT 进行数字签名（digital signature）和/或加密（encryption）
    // 以确保 JWT 的完整性、真实性并且保护其内容不被篡改


    private static final String defaultBase64EncodedSecretKey = "B*B^D%fe";
    /** 将默认的 Base64 编码后的密钥再次进行 Base64 编码*/
    private final String base64EncodedSecretKey = org.apache.commons.codec.binary.Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes());
    private final Algorithm algorithm = Algorithm.HMAC256(org.apache.commons.codec.binary.Base64.decodeBase64(Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes())));

    protected abstract AuthStateEntity checkCode(String code);

    @Override
    public AuthStateEntity doLogin(String code) {
        //1.如果不是四位有效数字，返回验证码无效
        if(!code.matches("\\d{4}")){
            log.info("鉴权，用户输入验证码无效{}",code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVo.A0002.getCode())
                    .info(AuthTypeVo.A0002.getInfo())
                    .build();
        }

        //2.校验判断,非成功则直接返回
        AuthStateEntity authStateEntity = this.checkCode(code);
        if(!authStateEntity.getCode().equals(AuthTypeVo.A0000.getCode())){
            return authStateEntity;
        }

        //3.获取Token并返回
        Map<String,Object> chaim = new HashMap<>();
        chaim.put("openId",authStateEntity.getOpenId());
        String token = encode(authStateEntity.getOpenId(), 7 * 24 * 60 * 60 * 1000, chaim);
        authStateEntity.setToken(token);

        return authStateEntity;
    }

    /**
     * 生成jwt
     *
     * jwt由三部分组成
     1.Header（头部）:
     Header 包含了关于生成和验证 JWT 的元数据信息，通常包括以下内容：
     声明的类型（typ）：一般为 “JWT”，表示该令牌的类型。
     使用的签名算法（alg）：指定用于生成签名的算法，如 HS256、RS256等。

     2.Payload（荷载）:
     存储用户信息
     Payload 包含了 JWT 的声明信息（Claims），用于传输需要在应用程序之间共享的信息。Payload 可以包含三种类型的声明：
     Registered Claims（注册声明）：一些预定义的声明，如发行人（iss）、主题（sub）、过期时间（exp）等。
     Public Claims（公共声明）：自定义的声明，可以根据需要自行定义。
     Private Claims（私有声明）：用来在双方之间共享信息，不会被注册的声明名冲突。
     在生成 JWT 时，荷载（Payload）部分通常会包含一些声明（Claims）信息，如用户身份、权限等。这些声明信息会被编码成 Base64 字符串并放入 JWT 的 Payload 中。也正因为如此，JWT 在传输过程中只是一串字符串，而不是被拆分为三个部分存储

     3.Signature（签名）:
     Signature 是由 Header 和 Payload 基于指定的算法计算生成的签名，用于验证 JWT 的完整性和真实性。Signature 由 Header、Payload 和秘钥一起经过签名算法计算得到。

     */

    protected String encode(String issuer, long ttlMillis, Map<String, Object> claims){
        if (claims == null) {
            claims = new HashMap<>();
        }
        //当前系统时间的毫秒数
        long nowMillis = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                // 荷载部分
                .setClaims(claims)
                // .setId(UUID.randomUUID().toString()) 则是用来生成一个唯一标识符（UUID），并将其作为 JWT 的 ID（即 Payload 部分的一个字段）。
                .setId(UUID.randomUUID().toString())//2
                // 签发时间
                .setIssuedAt(new Date(nowMillis))
                // 签发人，也就是JWT是给谁的（逻辑上一般都是username或者userId）
                .setSubject(issuer)
                .signWith(SignatureAlgorithm.HS256, base64EncodedSecretKey);//这个地方是生成jwt使用的算法和秘钥
        //设置过期时间
        if(ttlMillis >= 0){
            long expMillis = nowMillis+ttlMillis;
            Date exp = new Date(expMillis);// 4. 过期时间，这个也是使用毫秒生成的，使用当前时间+前面传入的持续时间生成
            builder.setExpiration(exp);
        }
        //将 JwtBuilder 对象转换为 JWT 字符串的方法
        return builder.compact();
    }

    // 相当于encode的方向，传入jwtToken生成对应的username和password等字段。Claim就是一个map
    // 拿到荷载部分所有的键值对
    protected Claims decode(String jwtToken){
        //得到DefaultJwtParser
        return Jwts.parser()
                //设置签名的密钥
                .setSigningKey(base64EncodedSecretKey)
                .parseClaimsJws(jwtToken)
                .getBody();
    }


    //判断jwtToken的合法性
    protected boolean isVerify(String jwtToken){
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(jwtToken);
            // 校验不通过会抛出异常
            // 判断合法的标准：1. 头部和荷载部分没有篡改过。2. 没有过期
            return true;
        }catch (Exception e){
            log.error("jwt isVerify Err", e);
            return false;
        }
    }

}
