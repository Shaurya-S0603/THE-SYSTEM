package app.thesystem.mobile;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

/** No plaintext file, payload logging, backup, export or network endpoint. */
final class Vault {
    private static final String ALIAS="system-vault-v1";
    private final AtomicFile file;
    Vault(Context context){ file=new AtomicFile(new File(context.getNoBackupFilesDir(),"vault.bin")); }
    private SecretKey key(boolean create) throws Exception {
        KeyStore store=KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        if(store.containsAlias(ALIAS))return (SecretKey)store.getKey(ALIAS,null);
        if(!create)throw new IOException("Key unavailable");
        KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return generator.generateKey();
    }
    synchronized String read() throws Exception {
        if(!file.getBaseFile().exists())return null;
        byte[] encoded=file.readFully();
        if(encoded.length<29||encoded.length>300000||encoded[0]!=1)throw new IOException("Invalid vault");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,key(false),new GCMParameterSpec(128,encoded,1,12));
        cipher.updateAAD("THE-SYSTEM-v1".getBytes(StandardCharsets.UTF_8));
        return new String(cipher.doFinal(encoded,13,encoded.length-13),StandardCharsets.UTF_8);
    }
    synchronized void write(String value) throws Exception {
        if(value.getBytes(StandardCharsets.UTF_8).length>262144)throw new IOException("Capacity reached");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE,key(true));
        cipher.updateAAD("THE-SYSTEM-v1".getBytes(StandardCharsets.UTF_8));
        byte[] encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();bytes.write(1);bytes.write(cipher.getIV());bytes.write(encrypted);
        FileOutputStream out=null;
        try {out=file.startWrite();out.write(bytes.toByteArray());file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}
    }
    synchronized void erase() throws Exception {
        file.delete();if(file.getBaseFile().exists())throw new IOException("Erase failed");
        KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);store.deleteEntry(ALIAS);
    }
}
