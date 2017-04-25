package org.adridadou.ethereum.converters.output;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ArrayConverterTest {

    @Test
    public void tesArrayConvertPrimitiveArray(){
        OutputTypeHandler oth = new OutputTypeHandler();
        ArrayConverter ac = new ArrayConverter(oth);

        byte[] expected = "hello".getBytes();

        Object converted = ac.convert(expected, byte.class);

        byte[] actual = (byte[])converted;

        assertThat(actual.length, is(expected.length));
        assertThat(actual, equalTo(expected));
        for(int i=0;i<actual.length;i++){
            assertThat(actual[i], is(expected[i]));
        }
    }

   // @Test
    public void tesArrayConvertObjectArray(){
        OutputTypeHandler oth = new OutputTypeHandler();
        ArrayConverter ac = new ArrayConverter(oth);

        Byte[] expected = toObjects("hello".getBytes());

        Object converted = ac.convert(expected, Byte.class);

        Byte[] actual = (Byte[])converted;

        assertThat(actual.length, is(expected.length));
        for(int i=0;i<actual.length;i++){
            assertThat(actual[i], is(expected[i]));
        }
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        Arrays.setAll(bytes, n -> bytesPrim[n]);
        return bytes;
    }
}