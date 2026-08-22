package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class CompassFloat implements RangePropertyHandler {

    private final AngleInterpolator aimedInterpolator = new AngleInterpolator();
    private final AngleInterpolator aimlessInterpolator = new AngleInterpolator();

    public enum CompassTarget implements StringRepresentable {
        NONE("none"),
        LODESTONE("lodestone"),
        SPAWN("spawn"),
        RECOVERY("recovery");

        private final String name;

        CompassTarget(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        public static final Codec<CompassTarget> CODEC = StringRepresentable.fromEnum(CompassTarget::values);
    }

    // Get the angle of the compass and return as float
    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition def) {
        if (entity == null) return 0f;
        if (!(entity.level() instanceof ClientLevel clientWorld)) return 0f;

        if (def.target() == null) return 0f; // target is not defined or is none

        GlobalPos pos = getTargetPosition(clientWorld, stack, entity, def.target());
        long time = clientWorld.getGameTime();
        boolean should_wobble = Boolean.TRUE.equals(def.wobble());

        if (!canPointTo(entity, pos)) return this.getAimlessAngle(0, time, should_wobble); // compass randomly rotates if it cant point at block

        // Angle float calculation
        double angle = this.getAngleTo(entity, pos.pos());
        double yaw = this.getBodyYaw(entity);
        double adjusted;

        // Performs interpolated wobble if true
        if (should_wobble) {
            if (this.aimedInterpolator.shouldUpdate(time)) {
                this.aimedInterpolator.update(time, 0.5 - (yaw - 0.25));
            }

            adjusted = angle + this.aimedInterpolator.value;
        }
        else adjusted = 0.5 - (yaw - 0.25 - angle); // immediately points in direction; no interpolation

        return Mth.positiveModulo((float) adjusted, 1.0F);
    }

    private float getAimlessAngle(int seed, long time, boolean should_wobble) {
        // Interpolated random rotation
        if (should_wobble) {
            if (this.aimlessInterpolator.shouldUpdate(time)) {
                this.aimlessInterpolator.update(time, Math.random());
            }

            double d = this.aimlessInterpolator.value + (double) ((float) this.scatter(seed) / 2.14748365E9F);
            return Mth.positiveModulo((float)d, 1.0F);
        }
        // Non-interpolated random rotation
        return Mth.positiveModulo((float) this.scatter(seed) / 2.14748365E9F, 1.0F);
    }

    private GlobalPos getTargetPosition(ClientLevel world, ItemStack stack, LivingEntity holder, CompassTarget target) {
        return switch (target) {
            case LODESTONE -> {
                LodestoneTracker comp = stack.get(DataComponents.LODESTONE_TRACKER);
                yield comp != null ? comp.target().orElse(null) : null;
            }
            case SPAWN -> GlobalPos.of(world.dimension(), world.getSharedSpawnPos());
            case RECOVERY -> {
                if (holder instanceof Player player)
                    yield player.getLastDeathLocation().orElse(null);
                yield null;
            }
            case NONE -> null;
        };
    }

    private float getAngleTo(LivingEntity entity, BlockPos pos) {
        Vec3 target = Vec3.atCenterOf(pos);
        return (float) (Math.atan2(target.getZ() - entity.getZ(), target.getX() - entity.getX()) / (2 * Math.PI));
    }

    private boolean canPointTo(Entity entity, @Nullable GlobalPos pos) {
        return pos != null && pos.dimension() == entity.level().dimension() && !(pos.pos().getSquaredDistance(entity.getPos()) < 9.999999747378752E-6);
    }

    private float getBodyYaw(LivingEntity entity) {
        return Mth.positiveModulo(entity.yBodyRot / 360.0F, 1.0F);
    }

    private double scatter(int seed) {
        return seed * 1327217883F;
    }

    static class AngleInterpolator {
        double value;
        private double speed;
        private long lastUpdateTime;

        AngleInterpolator() {
        }

        boolean shouldUpdate(long time) {
            return this.lastUpdateTime != time;
        }

        void update(long time, double target) {
            this.lastUpdateTime = time;
            double d = target - this.value;
            d = Mth.positiveModulo(d + 0.5, 1.0) - 0.5;
            this.speed += d * 0.1;
            this.speed *= 0.8;
            this.value = Mth.positiveModulo(this.value + this.speed, 1.0);
        }
    }
}
