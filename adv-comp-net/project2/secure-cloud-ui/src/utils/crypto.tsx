import * as crypto from "crypto";

export async function generateUserKeys() {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: "RSA-OAEP",
      modulusLength: 2048,
      publicExponent: new Uint8Array([1, 0, 1]),
      hash: "SHA-256",
    },
    true,
    ["encrypt", "decrypt"]
  );

  const privateKey = await window.crypto.subtle.exportKey(
    "jwk",
    keyPair.privateKey
  );
  const publicKey = await window.crypto.subtle.exportKey(
    "jwk",
    keyPair.publicKey
  );
  return { publicKey, privateKey };
}

/**
 * Asymmetric Encrypt (public key)
 * @param data the data in buffer form
 * @param publicKey the public key to encrypt with
 */
export async function encryptUserData(data: Buffer, publicKey: JsonWebKey) {
  const key = await window.crypto.subtle.importKey(
    "jwk",
    publicKey,
    { name: "RSA-OAEP", hash: "SHA-256" },
    true,
    ["encrypt"]
  );
  const buf = await window.crypto.subtle.encrypt("RSA-OAEP", key, data);
  return new TextDecoder().decode(buf);
}

/**
 * Asymmetric Decrypt (private key)
 * @param data the data in buffer form
 * @param privateKey the private key to decrypt with
 */
export async function decryptUserData(data: Buffer, privateKey: JsonWebKey) {
  const key = await window.crypto.subtle.importKey(
    "jwk",
    privateKey,
    { name: "RSA-OAEP", hash: "SHA-256" },
    true,
    ["decrypt"]
  );
  const res = await window.crypto.subtle.decrypt("RSA-OAEP", key, data);
  return new TextDecoder().decode(res);
}

/**
 *
 * @param data the private data to encrypt (only dealing with text atm)
 * @param key a randomly generated key to be stored in an encrypted format with the encrypted file (or password is key in case of user private keys) (32 bytes, [iv,key])
 * @returns the encrypted data
 */
export function encryptData(data: string, key: string) {
  console.log(`got key string ${key}`);
  const key_in_bytes = Buffer.from(key, "base64");
  console.log(
    `converted to ${key_in_bytes.length} bytes, ${key_in_bytes.toString(
      "base64"
    )}`
  );
  const cipher = crypto.createCipheriv(
    "aes-128-cbc",
    key_in_bytes.subarray(16, 32),
    key_in_bytes.subarray(0, 16)
  );
  let encrypted = cipher.update(data, "utf-8", "base64");
  encrypted += cipher.final("base64");
  return encrypted;
}

export function generateRandomKey(bytes: number) {
  const rand = crypto.randomBytes(bytes);
  return rand.toString("base64");
}
/**
 *
 * @param encData the encrypted data in base64 format
 * @param key the randomly generated key, stored alongside data in an encrypted format (or password is key in case of user private keys)
 * @returns the plaintext data
 */
export async function decryptData(encData: string, key: string) {
  try {
    console.log(`got key string ${key}`);
    const key_in_bytes = Buffer.from(key, "base64");
    console.log(
      `converted to ${key_in_bytes.length} bytes, ${key_in_bytes.toString(
        "base64"
      )}`
    );
    const cipher = crypto.createDecipheriv(
      "aes-128-cbc",
      key_in_bytes.subarray(16, 32),
      key_in_bytes.subarray(0, 15)
    );
    let res = cipher.update(encData, "base64", "utf-8");
    res += cipher.final("utf-8");
    return res;
  } catch (_) {
    throw new Error("decryption failed");
  }
}
