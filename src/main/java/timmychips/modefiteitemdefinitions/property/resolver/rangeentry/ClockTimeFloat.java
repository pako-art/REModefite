package timmychips.modefiteitemdefinitions.property.resolver.rangeentry;

import com.mojang.serialization.Codec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import timmychips.modefiteitemdefinitions.property.handler.RangePropertyHandler;
import timmychips.modefiteitemdefinitions.property.type.codec.RangeDispatchDefinition;

public class ClockTimeFloat implements RangePropertyHandler {
    private final RandomSource random = RandomSource.create();
    private final ClockAngler.Angler wobbleAngler;
    private final ClockAngler.Angler instantAngler;

    // Constructor for anglers
    public ClockTimeFloat() {
        this.wobbleAngler = new ClockAngler().createAngler(0.9F, true);
        this.instantAngler = new ClockAngler().createAngler(0.9F, false);
    }

    @Override
    public float getValue(ItemStack stack, LivingEntity entity, RangeDispatchDefinition.Definition definition) {
        if (entity == null) return 0F;
        if (!(entity.level() instanceof ClientLevel clientWorld)) return 0f;

        // Specified source
        ClockSource source = definition.clockSource();

        boolean wobble = Boolean.TRUE.equals(definition.wobble());

        // Set angler to wobbled version or not
        ClockAngler.Angler angler;
        if (wobble) angler = wobbleAngler;
        else angler = instantAngler;

        if (source == null) return 0F;

        float targetAngle = source.getRandomAngle(clientWorld, stack, entity, this.random);
        long time = clientWorld.getGameTime();

        if (angler.shouldUpdate(time)) {
            angler.update(time, targetAngle);
        }
        return angler.getAngle();
    }

    public enum ClockSource implements StringRepresentable {
        DAYTIME("daytime") {
            public float getRandomAngle(ClientLevel clientWorld, ItemStack stack, Entity user, RandomSource random) {
                return clientWorld.getSunAngle(1.0F);
            }
        },
        MOON_PHASE("moon_phase") {
            public float getRandomAngle(ClientLevel clientWorld, ItemStack stack, Entity user, RandomSource random) {
                return (float) clientWorld.getMoonPhase() / 8F;
            }
        },
        RANDOM("random") {
            public float getRandomAngle(ClientLevel clientWorld, ItemStack stack, Entity user, RandomSource random) {

                return random.nextFloat();
            }
        };

        public static final Codec<ClockSource> CODEC = StringRepresentable.fromEnum(ClockSource::values);
        private final String name;

        ClockSource(String name) { this.name = name; }
        public String asString() { return name; }
        abstract float getRandomAngle(ClientLevel world, ItemStack stack, Entity user, RandomSource random);
    }
}


class ClockAngler {

    public interface Angler {
        float getAngle();
        boolean shouldUpdate(long time);
        void update(long time, float target);
    }

    protected Angler createAngler(float speedMultiplier, boolean should_wobble) {
        return should_wobble ? createWobblyAngler(speedMultiplier) : createInstantAngler();
    }

    public static Angler createWobblyAngler(final float speedMultiplier) {
        return new Angler() {
            private float angle;
            private float speed;
            private long lastUpdateTime;

            public float getAngle() {
                return this.angle;
            }

            public boolean shouldUpdate(long time) {
                return this.lastUpdateTime != time;
            }

            public void update(long time, float target) {
                this.lastUpdateTime = time;
                float f = Mth.positiveModulo(target - this.angle + 0.5F, 1.0F) - 0.5F;
                this.speed += f * 0.1F;
                this.speed *= speedMultiplier;
                this.angle = Mth.positiveModulo(this.angle + this.speed, 1.0F);
            }
        };
    }

    public static Angler createInstantAngler() {
        return new Angler() {
            private float angle;

            public float getAngle() {
                return this.angle;
            }

            public boolean shouldUpdate(long time) {
                return true;
            }

            public void update(long time, float target) {
                this.angle = target;
            }
        };
    }
}
