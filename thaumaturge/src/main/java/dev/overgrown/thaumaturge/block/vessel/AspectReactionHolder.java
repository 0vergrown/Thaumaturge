package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.util.AspectMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AspectReactionHolder {

    private static final int MIN_TEMP = -2;
    private static final int MAX_TEMP = 4;

    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thaumaturge: Could not Access Required SHA-256 Digest");
        }
    }

    private Map<Integer,Map<Identifier, List<AspectReaction>>> reactionMap;

    public AspectReactionHolder(long seed) {
        List<AspectReactionPrimitive> primitiveList = AspectReactionPrimitiveManager.getPrimitiveList();
        reactionMap = new HashMap<>();

        for (AspectReactionPrimitive primitive: primitiveList) {

            // Get a Random Object which Seeds depends upon world seed and primary aspects in a reaction
            byte[] inputA = primitive.inputA().toString().getBytes();
            byte[] inputB = primitive.inputB().toString().getBytes();
            byte[] result = primitive.result().toString().getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + inputA.length + inputB.length + result.length);
            buffer.putLong(seed);
            buffer.put(inputA);
            buffer.put(inputB);
            buffer.put(result);
            digest.reset();
            byte[] hashResult = digest.digest(buffer.array());
            long randomSeed = ((long) hashResult[0]) << 56 |
                    ((long) hashResult[1]) << 48 |
                    ((long) hashResult[2]) << 40 |
                    ((long) hashResult[3]) << 32 |
                    ((long) hashResult[4]) << 24 |
                    ((long) hashResult[5]) << 16 |
                    ((long) hashResult[6]) << 8 |
                    ((long) hashResult[7]);
            Random random = Random.create(randomSeed);

            int reactionTemp;
            boolean forwardReactionTrendsTowardsColder = primitive.frTowardsColder();

            if (primitive.FrBegin() == primitive.FrEnd()) {
                reactionTemp = primitive.FrEnd();
            } else {
                reactionTemp = random.nextBetweenExclusive(primitive.FrBegin(), primitive.FrEnd());
            }

            if (primitive.reactionDirectionReversible()) {
                forwardReactionTrendsTowardsColder = random.nextBoolean();
            }

            Identifier catalyst = null;

            if (!primitive.catalysts().isEmpty()) {
                if (random.nextFloat() <= primitive.catalystChance()) {
                    catalyst = primitive.catalysts().get(random.nextInt(primitive.catalysts().size()));
                }
            }

            AspectReaction forwardImplementation = new AspectReaction(
                    Set.of(primitive.inputA(), primitive.inputB()), Set.of(primitive.result()));
            AspectReaction backwardImplementation = new AspectReaction(
                    Set.of(primitive.result()), Set.of(primitive.inputA(), primitive.inputB()));

            // Iterate over all temps form low to the mid
            for (int i = MIN_TEMP; i <= reactionTemp; i++) {
                // Access or create a Map of Reactions that can occur at a temperature
                Map<Identifier, List<AspectReaction>> reactionMapAtTemp;
                if (reactionMap.containsKey(i)) {
                    reactionMapAtTemp = reactionMap.get(i);
                } else {
                    reactionMapAtTemp = new HashMap<>();
                    reactionMap.put(i, reactionMapAtTemp);
                }

                Identifier key = catalyst == null ?
                        (forwardReactionTrendsTowardsColder ? primitive.inputA() : primitive.result()) : catalyst;

                // Access or Create a List of Reactions for a Aspect at a specific Temperature
                List<AspectReaction> aspectReactionsForAspectAtTemp;
                if (reactionMapAtTemp.containsKey(key)) {
                    aspectReactionsForAspectAtTemp = reactionMapAtTemp.get(key);
                } else {
                    aspectReactionsForAspectAtTemp = new ArrayList<>();
                    reactionMapAtTemp.put(key, aspectReactionsForAspectAtTemp);
                }
                aspectReactionsForAspectAtTemp.add(forwardReactionTrendsTowardsColder ? forwardImplementation : backwardImplementation);
            }

            // Iterate over all temps from high to end
            for (int i = reactionTemp + 1; i <= MAX_TEMP; i++) {
                // Access or create a Map of Reactions that can occur at a temperature
                Map<Identifier, List<AspectReaction>> reactionMapAtTemp;
                if (reactionMap.containsKey(i)) {
                    reactionMapAtTemp = reactionMap.get(i);
                } else {
                    reactionMapAtTemp = new HashMap<>();
                    reactionMap.put(i, reactionMapAtTemp);
                }

                Identifier key = catalyst == null ?
                        (forwardReactionTrendsTowardsColder ? primitive.result() : primitive.inputA()) : catalyst;

                // Access or Create a List of Reactions for a Aspect at a specific Temperature
                List<AspectReaction> aspectReactionsForAspectAtTemp;
                if (reactionMapAtTemp.containsKey(key)) {
                    aspectReactionsForAspectAtTemp = reactionMapAtTemp.get(key);
                } else {
                    aspectReactionsForAspectAtTemp = new ArrayList<>();
                    reactionMapAtTemp.put(key, aspectReactionsForAspectAtTemp);
                }
                aspectReactionsForAspectAtTemp.add(forwardReactionTrendsTowardsColder ? backwardImplementation : forwardImplementation);
            }


        }
    }

    public List<AspectReaction> getPossibleReactions(int temp, AspectMap map) {
        List<AspectReaction> list = new ArrayList<>();
        if (reactionMap.containsKey(temp)) {
            var mapAtTemp = reactionMap.get(temp);
            // Query valid Reaction Implementation for all Aspects
            for (Identifier aspect : map.getAspects()) {
                if (map.getAspectLevel(aspect) > 0 && mapAtTemp.containsKey(aspect)) {
                    List<AspectReaction> listPerAspectAtTemp = mapAtTemp.get(aspect);
                    for (AspectReaction implementation : listPerAspectAtTemp) {
                        boolean containsAllInputs = true;
                        for (Identifier inputAspect : implementation.input) {
                            if (map.getAspectLevel(inputAspect) <= 0) {
                                containsAllInputs = false;
                                break;
                            }
                        }
                        if (containsAllInputs) {
                            list.add(implementation);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (var temperatures : reactionMap.keySet()) {
            builder.append("\tTemp: ").append(temperatures).append(" {\n");
            var mapAtTemperature = reactionMap.get(temperatures);
            for (var aspect : mapAtTemperature.keySet()) {
                builder.append("\t\tAspect: ").append(aspect).append(" [\n");
                var reactionList = mapAtTemperature.get(aspect);
                for (var reaction : reactionList) {
                    builder.append("\t\t\t{\n")
                            .append("\t\t\t\t Input:").append(reaction.input).append("\n")
                            .append("\t\t\t\t Output:").append(reaction.output).append("\n")
                            .append("\t\t\t}\n");
                }
                builder.append("\t\t]");
            }
            builder.append("\t}\n");
        }

        builder.append("}");
        return builder.toString();
    }

    public interface Provider {
        AspectReactionHolder thaumaturge$getAspectReactionHolder();
    }
}
