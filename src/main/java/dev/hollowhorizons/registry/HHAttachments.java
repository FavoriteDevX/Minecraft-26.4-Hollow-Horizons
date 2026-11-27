package dev.hollowhorizons.registry;

import dev.hollowhorizons.HollowHorizons;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.GlobalPos;

/** Dados guardados no próprio jogador (sobrevivem a relogs e reinícios do servidor). */
public final class HHAttachments {
	/** Último bloco sólido em que o jogador pisou — o ponto de retorno da Âncora do Vazio. */
	public static final AttachmentType<GlobalPos> LAST_SAFE_GROUND = AttachmentRegistry.create(
		HollowHorizons.id("last_safe_ground"),
		builder -> builder.persistent(GlobalPos.CODEC).copyOnDeath());

	private HHAttachments() {}

	public static void init() {}
}
