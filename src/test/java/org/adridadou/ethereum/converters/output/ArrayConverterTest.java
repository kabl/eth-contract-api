package org.adridadou.ethereum.converters.output;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ArrayConverterTest {

    private ArrayConverter arrayConverter;

    @Before
    public void before(){
        OutputTypeHandler oth = new OutputTypeHandler();
        arrayConverter = new ArrayConverter(oth);
    }

    @Test
    public void tesArrayConvertPrimitiveArray(){
        byte[] expected = "ABCD".getBytes();

        Object converted = arrayConverter.convert(expected, byte.class);
        byte[] actual = (byte[])converted;

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void tesArrayConvertObjectArray(){
        Byte[] expected = toByteArray("ABCD".getBytes());

        Object converted = arrayConverter.convert(expected, Byte.class);
        Byte[] actual = (Byte[])converted;

        assertThat(actual, equalTo(expected));
    }

    private Byte[] toByteArray(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }
}