package dev.hollowhorizons.network;

import dev.hollowhorizons.HollowHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente → servidor: "fiz o salto duplo com as Botas de Gravidade Zero". */
public record DoubleJumpPayload() implements CustomPacketPayload {
	public static final DoubleJumpPayload INSTANCE = new DoubleJumpPayload();
	public static final CustomPacketPayload.Type<DoubleJumpPayload> TYPE = new CustomPacketPayload.Type<>(HollowHorizons.id("double_jump"));
	public static final StreamCodec<ByteBuf, DoubleJumpPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<DoubleJumpPayload> type() {
		return TYPE;
	}
}
