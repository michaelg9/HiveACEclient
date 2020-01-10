package com.ace.mqtt.crypto;

import com.nimbusds.jose.JOSEException;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Base64;

import static org.junit.Assert.*;
public class MACCalculatorTest {
    private MACCalculator macCalculator;

    @Test
    public void testHMAC256() throws JOSEException {
        final byte[] key = Base64.getDecoder().decode("Af9+xth5wA4xh8dYCZcN+fj92TiDH5Qgfalvf/60evkVh54Az9Q/VQ9Jcut8hyYLnekr+EvHDHY/pcSw/joAqKFbRL9yySMMXuf8JSgmvO/7TUwf6cBnsXQJsQ9UmpEnzFKJCO7KBBqEeMuQmHQLLhJhhpeYVEbbyrI2js4Dc1I=");
        macCalculator =  new MACCalculator(key,"HS256");
        final byte[] nonce = "m1r8zr454b17ndra22u9grgtw9a4de4wa6tn2knupf74rc9vezf0mue9k8r3rky63ej4m7tgntw4xmavkx9v5urv68zf2cm3nnrhyb3kq3153gdwn45gcazpxtzzmn2162m1xuab09kdrzjguq7qz4eeu7pjy3g0hxnekqgy2p561v6yvnjvc2q1hh8640zcddx0bagrxkqjt1r7rzjwpb3yac4u3d3zr60u3dkcgqdfrkb1hz8quujr11tw3efu08ezah075ru54j64bqz7zv3f7uk4ghez92gnq2rwr82tx7zeaqezdxmczrdq6j4k37vqgjgzqck9ffa10xecgj09xe3m8wrx56d9mzpwqcf1wft8dwzu57bdby1z2ddtthgyb8c4y2aavhx2fjauh5t7nw".getBytes();
        final ByteBuffer nonceB = ByteBuffer.allocate(nonce.length + 2);
        nonceB.putShort((short) nonce.length);
        nonceB.put(nonce);
        nonceB.rewind();
        final byte[] output2 = macCalculator.signNonce(nonce);
        final byte[] expected = Base64.getDecoder().decode("H5cKlBob6MrFeQkR3iwUULProxdjMgOxO/HXcp/5o04=");
        assertArrayEquals(expected, output2);
    }

    @Test
    public void testHMAC5512() throws JOSEException {
        final byte[] key = Base64.getDecoder().decode("Af9+xth5wA4xh8dYCZcN+fj92TiDH5Qgfalvf/60evkVh54Az9Q/VQ9Jcut8hyYLnekr+EvHDHY/pcSw/joAqKFbRL9yySMMXuf8JSgmvO/7TUwf6cBnsXQJsQ9UmpEnzFKJCO7KBBqEeMuQmHQLLhJhhpeYVEbbyrI2js4Dc1I=");
        macCalculator =  new MACCalculator(key,"HS512");
        final byte[] nonce = "m1r8zr454b17ndra22u9grgtw9a4de4wa6tn2knupf74rc9vezf0mue9k8r3rky63ej4m7tgntw4xmavkx9v5urv68zf2cm3nnrhyb3kq3153gdwn45gcazpxtzzmn2162m1xuab09kdrzjguq7qz4eeu7pjy3g0hxnekqgy2p561v6yvnjvc2q1hh8640zcddx0bagrxkqjt1r7rzjwpb3yac4u3d3zr60u3dkcgqdfrkb1hz8quujr11tw3efu08ezah075ru54j64bqz7zv3f7uk4ghez92gnq2rwr82tx7zeaqezdxmczrdq6j4k37vqgjgzqck9ffa10xecgj09xe3m8wrx56d9mzpwqcf1wft8dwzu57bdby1z2ddtthgyb8c4y2aavhx2fjauh5t7nw".getBytes();
        final ByteBuffer nonceB = ByteBuffer.allocate(nonce.length + 2);
        nonceB.putShort((short) nonce.length);
        nonceB.put(nonce);
        nonceB.rewind();
        final byte[] output2 = macCalculator.signNonce(nonce);
        final byte[] expected = Base64.getDecoder().decode("SqtIPEWTb/mOmcupzRGzoNCBG88idJ00pIYzBHSNLqIeb3x5oJbclDHYtdNVYLsxucfaarZ4iaLTF6G/ATQ77A==");
        assertArrayEquals(expected, output2);
    }

}