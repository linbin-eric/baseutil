package com.jfireframework.baseutil.encrypt;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.exception.UnSupportException;

/**
 * rsa加解密工具类，注意，该类是非线程安全的
 * 
 * @author 林斌（windfire@zailanghua.com）
 * 
 */
public class RSAUtil implements EnDecrpt
{
    
    private final String algorithms;
    
    private PublicKey    publicKey;
    private PrivateKey   privateKey;
    private Cipher       decryptCipher;
    private Cipher       encrptCipher;
    private Signature    sign;
    private Signature    check;
    
    public RSAUtil(String algorithms)
    {
        this.algorithms = algorithms;
    }
    
    /**
     * 设置rsa加密所需要的公钥
     */
    @Override
    public void setPublicKey(byte[] publicKeyBytes)
    {
        try
        {
            // 取得公钥
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("rsa");
            publicKey = keyFactory.generatePublic(x509KeySpec);
            encrptCipher = Cipher.getInstance("rsa");
            encrptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            check = Signature.getInstance(algorithms);
            check.initVerify(publicKey);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    /**
     * 设置rsa解密所需要的密钥
     */
    @Override
    public void setPrivateKey(byte[] privateKeyBytes)
    {
        try
        {
            // 取得私钥
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("rsa");
            privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            decryptCipher = Cipher.getInstance("rsa");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            sign = Signature.getInstance(algorithms);
            sign.initSign(privateKey);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    @Override
    public void setKey(byte[] key)
    {
        throw new UnSupportException("rsa加密方法，不能设置对称密钥");
    }
    
    @Override
    public byte[] encrypt(byte[] src)
    {
        try
        {
            return encrptCipher.doFinal(src);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    @Override
    public byte[] decrypt(byte[] src)
    {
        try
        {
            return decryptCipher.doFinal(src);
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
    @Override
    public byte[] sign(byte[] src)
    {
        try
        {
            sign.update(src);
            return sign.sign();
        }
        catch (SignatureException e)
        {
            throw new JustThrowException(e);
        }
    }
    
    @Override
    public boolean check(byte[] src, byte[] sign)
    {
        try
        {
            check.update(src);
            return check.verify(sign);
        }
        catch (SignatureException e)
        {
            throw new JustThrowException(e);
        }
    }
    
    public void buildKey() throws IOException
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithms);
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();
            System.out.println("公钥是：" + Base64Tool.encode(publicKey.getEncoded()));
            System.out.println("私钥是：" + Base64Tool.encode(privateKey.getEncoded()));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        new RSAUtil("SHA1WithRSA").buildKey();
    }
}
