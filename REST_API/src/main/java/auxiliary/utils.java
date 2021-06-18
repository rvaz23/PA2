package auxiliary;

import java.nio.ByteBuffer;

public  class utils {

    public static byte[] intToByteArray(int value){
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }


    public static byte[] floatToByteArray(float value){
        return ByteBuffer.allocate(8).putFloat(value).array();
    }

    public static float byteArrayTofloat(byte[] bytes){
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static int byteArrayToint(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }

}
