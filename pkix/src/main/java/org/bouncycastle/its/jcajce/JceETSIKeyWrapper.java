package org.bouncycastle.its.jcajce;

import java.security.Provider;
import java.security.interfaces.ECPublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.its.ETSIKeyWrapper;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.jce.spec.IESKEMParameterSpec;
import org.bouncycastle.oer.its.ieee1609dot2.EncryptedDataEncryptionKey;
import org.bouncycastle.oer.its.ieee1609dot2.basetypes.EccP256CurvePoint;
import org.bouncycastle.oer.its.ieee1609dot2.basetypes.EciesP256EncryptedKey;
import org.bouncycastle.util.Arrays;

public class JceETSIKeyWrapper
    implements ETSIKeyWrapper
{
    private final ECPublicKey recipientKey;
    private final byte[] recipientHash;
    private final JcaJceHelper helper;

    public JceETSIKeyWrapper(ECPublicKey key, byte[] recipientHash, JcaJceHelper helper)
    {
        this.recipientKey = key;
        this.recipientHash = recipientHash;
        this.helper = helper;
    }

    public EncryptedDataEncryptionKey wrap(byte[] secretKey)
    {
        try
        {
            Cipher etsiKem = helper.createCipher("ETSIKEMwithSHA256");
            etsiKem.init(Cipher.WRAP_MODE, recipientKey, new IESKEMParameterSpec(recipientHash, true));
            byte[] wrappedKey = etsiKem.wrap(new SecretKeySpec(secretKey, "AES"));

            int size = (recipientKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;

            if (wrappedKey[0] == 0x04)
            {
                size = 2 * size + 1;
            }
            else
            {
                size = size + 1;
            }


            // TODO add brainpool.
            EciesP256EncryptedKey key = EciesP256EncryptedKey.builder()
                .setV(EccP256CurvePoint.builder().createEncodedPoint(Arrays.copyOfRange(wrappedKey, 0, size)))
                .setC(Arrays.copyOfRange(wrappedKey, size, size + secretKey.length))
                .setT(Arrays.copyOfRange(wrappedKey, size + secretKey.length, wrappedKey.length)).build();

            return EncryptedDataEncryptionKey.eciesNistP256(key);

        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    public static class Builder
    {
        private final ECPublicKey recipientKey;
        private final byte[] recipientHash;
        private JcaJceHelper helper = new DefaultJcaJceHelper();

        public Builder(ECPublicKey recipientKey, byte[] recipientHash)
        {
            this.recipientKey = recipientKey;
            this.recipientHash = recipientHash;
        }

        public Builder setProvider(Provider provider)
        {
            this.helper = new ProviderJcaJceHelper(provider);
            return this;
        }

        public Builder setProvider(String name)
        {
            this.helper = new NamedJcaJceHelper(name);
            return this;
        }

        public JceETSIKeyWrapper build()
        {
            return new JceETSIKeyWrapper(recipientKey, recipientHash, helper);
        }
    }

}