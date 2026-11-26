package dev.hollowhorizons.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Esporo de Seda: flutua devagar, oscila de lado e pulsa em brilho próprio
 * (ignora a escuridão do End, como os vaga-lumes).
 */
public class SilkSporeParticle extends SingleQuadParticle {
	private final float phase;

	protected SilkSporeParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites) {
		super(level, x, y, z, xa, ya, za, sprites.get(level.getRandom()));
		this.phase = this.random.nextFloat() * Mth.TWO_PI;
		this.lifetime = 120 + this.random.nextInt(120);
		this.gravity = -0.0015F;
		this.friction = 0.97F;
		this.hasPhysics = false;
		this.quadSize *= 0.55F + this.random.nextFloat() * 0.35F;
		this.xd = xa + (this.random.nextFloat() - 0.5F) * 0.01F;
		this.yd = ya + this.random.nextFloat() * 0.006F;
		this.zd = za + (this.random.nextFloat() - 0.5F) * 0.01F;
		float tint = this.random.nextFloat();
		this.setColor(0.86F + tint * 0.14F, 0.88F + tint * 0.1F, 1.0F);
		this.alpha = 0.0F;
	}

	@Override
	public void tick() {
		super.tick();
		this.xd += Mth.sin(this.age * 0.05F + this.phase) * 0.0007F;
		this.zd += Mth.cos(this.age * 0.04F + this.phase) * 0.0007F;
		float life = (float) this.age / this.lifetime;
		float fade = life < 0.15F ? life / 0.15F : life > 0.8F ? (1.0F - life) / 0.2F : 1.0F;
		this.alpha = Mth.clamp(fade, 0.0F, 1.0F) * (0.75F + 0.25F * Mth.sin(this.age * 0.15F + this.phase));
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public int getLightCoords(float partialTick) {
		return LightCoordsUtil.pack(15, 15);
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
			double xAux, double yAux, double zAux, RandomSource random) {
			return new SilkSporeParticle(level, x, y, z, xAux, yAux, zAux, this.sprites);
		}
	}
}
