package com.xxxx.rpc.protocal.codec;

import com.xxxx.rpc.protocal.MsgHeader;
import com.xxxx.rpc.protocal.RpcProtocol;
import com.xxxx.rpc.protocal.serialization.RpcSerialization;
import com.xxxx.rpc.protocal.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {
    
    
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        //获取消息头
        MsgHeader header = msg.getHeader();
        //将header信息写入byteBuf
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        byteBuf.writeInt(header.getSerializationLen());
        final byte[] bytes = header.getSerializations();
        byteBuf.writeBytes(bytes);
        String serializations = new String(bytes);
        RpcSerialization serialization = SerializationFactory.get(serializations);
        byte[] data = serialization.serialize(msg.getBody());
        //写入数据长度
        byteBuf.writeInt(data.length);
        //写入数据
        byteBuf.writeBytes(data);
    }
}
