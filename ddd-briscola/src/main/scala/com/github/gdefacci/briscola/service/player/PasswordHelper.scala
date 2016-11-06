package com.github.gdefacci.briscola.service.player

import java.security.MessageDigest
import java.util.Formatter
import java.security.SecureRandom

object PasswordHelper {

  def encryptPassword(password: String): String = {
    val crypt = MessageDigest.getInstance("SHA-1");
    crypt.reset();
    crypt.update(password.getBytes("UTF-8"));
    byteToHex(crypt.digest().map(p => java.lang.Byte.valueOf(p)));
  }

  def byteToHex(hash: Array[java.lang.Byte]): String = {
    val formatter = new Formatter();
    hash.foreach { b =>
      formatter.format("%02x", b)
    }
    val result = formatter.toString();
    formatter.close();
    return result;
  }
  
  def getSalt() = {
		val sr = SecureRandom.getInstance("SHA1PRNG");
		val salt = Array.ofDim[Byte](16)
		sr.nextBytes(salt)
		salt.toString()
	}
}