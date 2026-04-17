package dev.overgrown.thaumaturge.block.vessel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AspectReactionPrimitiveManager extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final List<AspectReactionPrimitive> PRIMITIVE_LIST = new ArrayList<>();

    public AspectReactionPrimitiveManager() {
        super(GSON, "aspect_reaction_primitive");
    }

    @Override
    public Identifier getFabricId() {
        return Thaumaturge.identifier("aspect_reaction_primitive");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        PRIMITIVE_LIST.clear();

        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement json = entry.getValue();

            AspectReactionPrimitive.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> Thaumaturge.LOGGER.error("Failed to parse {}: {}", id, error))
                    .ifPresent(PRIMITIVE_LIST::add);
        }

        Thaumaturge.LOGGER.info("Loaded {} Aspect Reaction Primitives", PRIMITIVE_LIST.size());
        Thaumaturge.LOGGER.info(PRIMITIVE_LIST.toString());
    }

    public static List<AspectReactionPrimitive> getPrimitiveList() {
        return Collections.unmodifiableList(PRIMITIVE_LIST);
    }
}
