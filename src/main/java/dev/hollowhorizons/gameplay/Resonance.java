package dev.hollowhorizons.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Sons harmônicos dos Campos de Cristal Ressonante.
 *
 * <p>Em vez de tocar o som em afinação aleatória, cada posição é mapeada para um grau da
 * escala pentatônica maior. Qualquer combinação de notas pentatônicas soa consonante,
 * então várias torres tocando juntas formam acordes.
 */
public final class Resonance {
	/** Semitons da pentatônica maior (dó, ré, mi, sol, lá) em duas oitavas. */
	private static final int[] PENTATONIC = {0, 2, 4, 7, 9, 12, 14, 16, 19, 21};

	private Resonance() {}

	/** Afinação (pitch) determinística da posição, entre 0.5 e 2.0 como o bloco musical. */
	public static float pitchFor(BlockPos pos) {
		int hash = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 7, PENTATONIC.length);
		return (float) Math.pow(2.0, (PENTATONIC[hash] - 12) / 12.0);
	}

	/** Nota tocada no servidor (todos ouvem) — usada quando alguém pisa no cristal. */
	public static void chime(Level level, BlockPos pos, float volume) {
		float pitch = pitchFor(pos);
		level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, volume, pitch);
		level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, volume * 0.35F, pitch);
	}

	/** "Vento" passando pelas torres: nota local, só no cliente, com a quinta junto. */
	public static void windChime(Level level, BlockPos pos, RandomSource random) {
		float pitch = pitchFor(pos);
		level.playLocalSound(pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.AMBIENT, 0.6F, pitch, false);
		level.playLocalSound(pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.AMBIENT, 0.18F, Math.min(2.0F, pitch * 1.5F), false);
	}
}
