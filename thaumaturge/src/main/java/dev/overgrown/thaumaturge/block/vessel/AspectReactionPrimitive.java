package dev.overgrown.thaumaturge.block.vessel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/***
 * AspectReactionPrimitive
 * Holds Information from which to generate per Seed unique Reactions between Aspects
 * @param inputA First Source Aspect
 * @param inputB Second Source Aspect
 * @param result Resulting Aspect
 * @param FrBegin Start of the Range at which the forward reaction starting point can be in
 * @param FrEnd End of the Range at which the forward reaction starting temperature point can be in
 * @param reactionDirectionReversible If the reaction direction implied between FrBegin and FrEnd can randomly be reversed
 * @param catalystChance Chance of the reaction needing to have a catalyst
 * @param catalysts List of possible catalyst for this reaction
 */
public record AspectReactionPrimitive(
        @NotNull
        Identifier inputA,
        @NotNull
        Identifier inputB,
        @NotNull
        Identifier result,
        int FrBegin,
        int FrEnd,
        boolean frTowardsColder,
        boolean reactionDirectionReversible,
        float catalystChance,
        @NotNull
        List<Identifier> catalysts) {

        public AspectReactionPrimitive(
                @NotNull
                Identifier inputA,
                @NotNull
                Identifier inputB,
                @NotNull
                Identifier result,
                int FrBegin,
                int FrEnd,
                boolean frTowardsColder,
                boolean reactionDirectionReversible,
                float catalystChance,
                @NotNull
                List<Identifier> catalysts)
        {
                this.inputA = inputA;
                this.inputB = inputB;
                this.result = result;
                this.FrBegin = FrBegin;
                this.FrEnd = FrEnd;
                this.frTowardsColder = frTowardsColder;
                this.reactionDirectionReversible = reactionDirectionReversible;
                this.catalystChance = catalystChance;
                this.catalysts = catalysts;

                if (this.inputA.equals(inputB)) {
                        throw new RuntimeException("Inputs might not be of the same aspect");
                }

                if (this.FrBegin > this.FrEnd) {
                        throw new RuntimeException("Forward Direction Min can not be greater than Forward Direction Max");
                }
        }

    public static final Codec<AspectReactionPrimitive> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("input_a").forGetter(AspectReactionPrimitive::inputA),
                    Identifier.CODEC.fieldOf("input_b").forGetter(AspectReactionPrimitive::inputB),
                    Identifier.CODEC.fieldOf("result").forGetter(AspectReactionPrimitive::result),
                    Codec.INT.fieldOf("fr_min").forGetter(AspectReactionPrimitive::FrBegin),
                    Codec.INT.fieldOf("fr_max").forGetter(AspectReactionPrimitive::FrEnd),
                    Codec.BOOL.fieldOf("fr_towards_colder").forGetter(AspectReactionPrimitive::frTowardsColder),
                    Codec.BOOL.optionalFieldOf("fr_direction_reversible", false).forGetter(AspectReactionPrimitive::reactionDirectionReversible),
                    Codec.FLOAT.optionalFieldOf("catalyst_chance", 0f).forGetter(AspectReactionPrimitive::catalystChance),
                    Identifier.CODEC.listOf().optionalFieldOf("catalysts", List.of()).forGetter(AspectReactionPrimitive::catalysts)
            ).apply(instance, AspectReactionPrimitive::new));
}
