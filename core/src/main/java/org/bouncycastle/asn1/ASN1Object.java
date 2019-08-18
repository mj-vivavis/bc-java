package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.util.Encodable;

/**
 * Base class for defining an ASN.1 object.
 */
public abstract class ASN1Object
    implements ASN1Encodable, Encodable
{
    public void encodeTo(OutputStream output) throws IOException
    {
        toASN1Primitive().encode(ASN1OutputStream.create(output), true);
    }

    public void encodeTo(OutputStream output, String encoding) throws IOException
    {
        ASN1Primitive p = toASN1Primitive();
        if (encoding.equals(ASN1Encoding.DER))
        {
            p.toDERObject().encode(ASN1OutputStream.create(output, ASN1Encoding.DER), true);
        }
        else if (encoding.equals(ASN1Encoding.DL))
        {
            p.toDLObject().encode(ASN1OutputStream.create(output, ASN1Encoding.DL), true);
        }
        else
        {
            p.encode(ASN1OutputStream.create(output), true);
        }
    }

    /**
     * Return the default BER or DER encoding for this object.
     *
     * @return BER/DER byte encoded object.
     * @throws java.io.IOException on encoding error.
     */
    public byte[] getEncoded() throws IOException
    {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        encodeTo(bOut);
        return bOut.toByteArray();
    }

    /**
     * Return either the default for "BER" or a DER encoding if "DER" is specified.
     *
     * @param encoding name of encoding to use.
     * @return byte encoded object.
     * @throws IOException on encoding error.
     */
    public byte[] getEncoded(String encoding) throws IOException
    {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        encodeTo(bOut, encoding);
        return bOut.toByteArray();
    }

    public int hashCode()
    {
        return this.toASN1Primitive().hashCode();
    }

    public boolean equals(
        Object  o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof ASN1Encodable))
        {
            return false;
        }

        ASN1Encodable other = (ASN1Encodable)o;

        return this.toASN1Primitive().equals(other.toASN1Primitive());
    }

    /**
     * @deprecated use toASN1Primitive()
     * @return the underlying primitive type.
     */
    public ASN1Primitive toASN1Object()
    {
        return this.toASN1Primitive();
    }

    /**
     * Return true if obj is a byte array and represents an object with the given tag value.
     *
     * @param obj object of interest.
     * @param tagValue tag value to check for.
     * @return  true if obj is a byte encoding starting with the given tag value, false otherwise.
     */
    protected static boolean hasEncodedTagValue(Object obj, int tagValue)
    {
        return (obj instanceof byte[]) && ((byte[])obj)[0] == tagValue;
    }

    /**
     * Method providing a primitive representation of this object suitable for encoding.
     * @return a primitive representation of this object.
     */
    public abstract ASN1Primitive toASN1Primitive();
}
