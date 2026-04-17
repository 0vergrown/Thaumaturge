package dev.overgrown.thaumaturge.block.vessel;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;

/** AspectReaction
 *  Object containing the input and output sets of Aspect for a Reaction
 */
public class AspectReaction {
    public Set<Identifier> input;
    public Set<Identifier> output;

    public AspectReaction(Set<Identifier> input, Set<Identifier> output) {
        this.input = Collections.unmodifiableSet(input);
        this.output = Collections.unmodifiableSet(output);
    }
}
