package dev.overgrown.thaumaturge.spell.impl.potentia.entity;

import dev.overgrown.thaumaturge.spell.impl.potentia.render.SpellBoltRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SpellBoltEntity extends Entity {
    private UUID casterUuid;
    private List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private static final TrackedData<Integer> TIER = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Long> SEED = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.LONG);
    private int life;

    public SpellBoltEntity(EntityType<?> type, World world) {
        super(type, world);
        this.life = 2;
    }

    public static final double LENGTH = 16;

    public void setCaster(PlayerEntity caster) {
        this.casterUuid = caster.getUuid();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TIER, 0);
        builder.add(SEED, getWorld().getRandom().nextLong());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Read custom data from NBT (if needed)
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Write custom data to NBT (if needed)
    }

    public long getSeed() {
        return dataTracker.get(SEED);
    }

    public int getTier() {
        return dataTracker.get(TIER);
    }

    public void setTier(int tier) {
        dataTracker.set(TIER, tier);
    }

    public void setOnHitEffects(List<Consumer<Entity>> effects) {
        this.onHitEffects = effects;
    }

    @Override
    public void tick() {

        super.tick();

        if (getWorld() instanceof ServerWorld serverWorld) {

            if (life-- <= 0) {
                discard();
                return;
            }

            Vector3f unit_facing = getRotationVector().toVector3f().normalize();
            Vector3f beam_start_pos = getPos().toVector3f().add(unit_facing.negate().mul((float) (SpellBoltEntity.LENGTH / 2)));

            final double step_multiplier = 3;

            Vec3d unit = new Vec3d(unit_facing).multiply(1.0 / step_multiplier);
            Vec3d beam_start = new Vec3d(beam_start_pos);

            ArrayList<Entity> entities = new ArrayList<>();

            // Simple raytracing
            for (int i = 0; i < (int) SpellBoltEntity.LENGTH * step_multiplier; i++) {

                Vec3d currentCenterPos = beam_start.add(unit.multiply(i).negate());

                // Visualize raytracing check
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, currentCenterPos.x, currentCenterPos.y, currentCenterPos.z, 1, 0, 0, 0, 100);

                List<Entity> hit = serverWorld.getOtherEntities(this, Box.of(currentCenterPos, 2, 2, 2),
                        entity -> {
                            if (entity.isSpectator() || !entity.isAlive()) {
                                return false;
                            }
                            // Exclude the caster from collision
                            return !entity.getUuid().equals(casterUuid);
                        });
                entities.addAll(hit);
            }

            for (final Entity entity : entities) {
                entity.damage(serverWorld, serverWorld.getDamageSources().lightningBolt(), 10);
            }

            /*
            EntityHitResult hitResult = ProjectileUtil.raycast(
                    this,
                    startPos,
                    endPos,
                    getBoundingBox().stretch(getVelocity()),
                    entity -> {
                        if (entity.isSpectator() || !entity.isAlive()) {
                            return false;
                        }
                        // Exclude the caster from collision
                        return !entity.getUuid().equals(casterUuid);
                    },
                    getVelocity().lengthSquared()
            );

            if (hitResult != null) {
                Entity target = hitResult.getEntity();
                target.damage(serverWorld, serverWorld.getDamageSources().lightningBolt(), 10);
                // onHitEffects.forEach(effect -> effect.accept(target));
                discard();
            } else {
                setVelocity(getVelocity().multiply(0.95));
                move(MovementType.SELF, getVelocity());
                ProjectileUtil.setRotationFromVelocity(this, 1.0f);
            }
            */
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false; // Not damageable
    }
}