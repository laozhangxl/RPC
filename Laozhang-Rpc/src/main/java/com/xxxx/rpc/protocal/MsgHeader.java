package com.xxxx.rpc.protocal;

import java.io.Serializable;

/**
 * 消息头
 */
public class MsgHeader implements Serializable {

    private short magic; //魔数:(安全校验，可以参考java中的CAFEBABE)

    private byte version; //协议版本号

    private byte msgType; //协议类型

    private byte status; //状态

    private long requestId; //请求ID

    private int serializationLen; //序列化数据长度

    private byte[] serializations; //存储序列化数据

    private int msgLen; //数据长度，指消息体的长度

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getMsgType() {
        return msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getSerializationLen() {
        return serializationLen;
    }

    public void setSerializationLen(int serializationLen) {
        this.serializationLen = serializationLen;
    }

    public byte[] getSerializations() {
        return serializations;
    }

    public void setSerializations(byte[] serializations) {
        this.serializations = serializations;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public void setMsgLen(int msgLen) {
        this.msgLen = msgLen;
    }
}
