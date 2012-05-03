package com.ttProject.websocket.library;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	/**
	 * 文字列からエンコードする。
	 * @param string 変換する文字列
	 * @return 変換してできたMD5バイトデータ
	 * @throws NoSuchAlgorithmException 例外
	 */
	public static byte[] crypt(String string) throws NoSuchAlgorithmException {
		if(string == null || string.length() == 0) {
			throw new IllegalArgumentException("String for encrypt must have body.");
		}
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(string.getBytes());
		return md.digest();
	}
	/**
	 * byteデータからエンコードする。
	 * @param bytes 変換するバイトデータ
	 * @return 変換してできたMD5バイトデータ
	 * @throws NoSuchAlgorithmException 例外
	 */
	public static byte[] crypt(byte[] bytes) throws NoSuchAlgorithmException {
		if(bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("bytes for encrypt must have body");
		}
		MessageDigest md = MessageDigest.getInstance("MD5");
		return md.digest(bytes);
	}
}
