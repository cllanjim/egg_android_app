package com.lingyang.base.utils;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.lingyang.base.utils.Log;

public class AesUtil {
	private static final String TAG = "AesUtil";
	public static String encrypt(String source, String key) {
		byte[] buf = encryptE(source, key);
		if (buf == null) {
			return null;
		}
		return StringUtils.toHex(buf);
	}
	
	public static byte[] encryptE(String source, String key) {
		if (source == null) {
			return null;
		}
		return encrypt(source.getBytes(), key);
	}
	
	public static byte[] encrypt(byte[] source, String key) {
		if (source == null || key == null) {
			return null;
		}

		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] buf = cipher.doFinal(source);
			return buf;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "encrypt Exception: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String source, String key) {
		try {
			return decryptE(source, key);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "decrypt Exception: " + e.toString());
		}
		return null;
	}
	
	public static byte[] decrypt(byte[] source, String key) {
		try {
			return decryptE(source, key);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "decrypt Exception: " + e.toString());
		}
		return null;
	}
	
	public static String decryptE(String source, String key) 
			throws GeneralSecurityException  {
		if (source == null || key == null) {
			return null;
		}
		byte[] buf = decryptE(StringUtils.toByte(source), key);
		if (buf != null) {
			String result = new String(buf);
			return result;
		}
		return null;
	}
	
	public static byte[] decryptE(byte[] source, String key) 
			throws GeneralSecurityException  {
		if (source == null || key == null) {
			return null;
		}
		
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		Cipher cipher;
		cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] buf = cipher.doFinal(source);
		return buf;
	}
}
