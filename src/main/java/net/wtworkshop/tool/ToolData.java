package net.wtworkshop.tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.wtworkshop.material.Materials;
import net.wtworkshop.material.PartRole;
import net.wtworkshop.material.ToolMaterial;
import org.jetbrains.annotations.Nullable;

public record ToolData(List<String> materials, List<String> modifiers, Map<String, Integer> progress, int level, long xp, List<String> expansions, Map<String,String> addonSlots) {
   public static final Codec<ToolData> CODEC = RecordCodecBuilder.create(
      i -> i.group(
               Codec.STRING.listOf().fieldOf("materials").forGetter(ToolData::materials),
               Codec.STRING.listOf().optionalFieldOf("modifiers", List.of()).forGetter(ToolData::modifiers),
               Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("progress", Map.of()).forGetter(ToolData::progress),
               Codec.INT.optionalFieldOf("level", 0).forGetter(ToolData::level),
               Codec.LONG.optionalFieldOf("xp", 0L).forGetter(ToolData::xp),
               Codec.STRING.listOf().optionalFieldOf("expansions", List.of()).forGetter(ToolData::expansions),
               Codec.unboundedMap(Codec.STRING,Codec.STRING).optionalFieldOf("addon_slots",Map.of()).forGetter(ToolData::addonSlots)
            )
            .apply(i, ToolData::new)
   );
   private static final StreamCodec<ByteBuf, ToolData> BASE_STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      ToolData::materials,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      ToolData::modifiers,
      ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
      ToolData::progress,
      ByteBufCodecs.VAR_INT,
      ToolData::level,
      ByteBufCodecs.VAR_LONG,
      ToolData::xp,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      ToolData::expansions,
      ToolData::new
   );
   private static final StreamCodec<ByteBuf,Map<String,String>> SLOT_CODEC=ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.STRING_UTF8);
   public static final StreamCodec<ByteBuf,ToolData> STREAM_CODEC=new StreamCodec<>(){
    public void encode(ByteBuf b,ToolData d){BASE_STREAM_CODEC.encode(b,d);SLOT_CODEC.encode(b,d.addonSlots());}
    public ToolData decode(ByteBuf b){var d=BASE_STREAM_CODEC.decode(b);return new ToolData(d.materials(),d.modifiers(),d.progress(),d.level(),d.xp(),d.expansions(),SLOT_CODEC.decode(b));}
   };
   public ToolData(List<String> materials,List<String> modifiers,Map<String,Integer> progress,int level,long xp,List<String> expansions){this(materials,modifiers,progress,level,xp,expansions,Map.of());}


   public ToolData(List<String> materials, List<String> modifiers, Map<String, Integer> progress, int level, long xp, List<String> expansions, Map<String,String> addonSlots) {
      materials = List.copyOf(materials);
      modifiers = List.copyOf(modifiers);
      progress = Map.copyOf(progress);
      this.materials = materials;
      this.modifiers = modifiers;
      this.progress = progress;
      this.level = level;
      this.xp = xp;
      this.expansions = List.copyOf(expansions);
      this.addonSlots=Map.copyOf(addonSlots);
   }

   public ToolData(List<String> materials,List<String> modifiers,Map<String,Integer> progress,int level,long xp){this(materials,modifiers,progress,level,xp,List.of());}
   public ToolData withExpansion(String id){var list=new ArrayList<>(expansions);list.add(id);return new ToolData(materials,modifiers,progress,level,xp,list,addonSlots);}
   public int expansionTier(){int tier=0;for(var expansion:SlotExpansion.values()){if(!expansions.contains(expansion.id()))break;tier++;}return tier;}

   public static ToolData of(String head, String handle, String binding) {
      return new ToolData(List.of(head, handle, binding), List.of(), Map.of(), 0, 0L);
   }

   @Nullable
   public ToolMaterial material(PartRole role) {
      return role.ordinal() < this.materials.size() ? Materials.get(this.materials.get(role.ordinal())) : null;
   }

   public String materialId(PartRole role) {
      return role.ordinal() < this.materials.size() ? this.materials.get(role.ordinal()) : "";
   }

   public boolean valid() {
      for (PartRole role : PartRole.values()) {
         ToolMaterial m = this.material(role);
         if (m == null || !m.supports(role)) {
            return false;
         }
      }

      return true;
   }

   public int modifierLevel(String id){return (int)modifiers.stream().filter(id::equals).count();}
   public int progressOf(String id){return progress.getOrDefault(id,0);}
   public ToolData withAddonSlot(String id,Modifier.Slot slot){var slots=new HashMap<>(addonSlots);slots.put(id,slot.name());return new ToolData(materials,modifiers,progress,level,xp,expansions,slots);}
   public ToolData withModifierAdded(String id){var list=new ArrayList<>(modifiers);list.add(id);var p=new HashMap<>(progress);p.remove(id);return new ToolData(materials,list,p,level,xp,expansions,addonSlots);}
   public ToolData withProgress(String id,int points){var p=new HashMap<>(progress);if(points<=0)p.remove(id);else p.put(id,points);return new ToolData(materials,modifiers,p,level,xp,expansions,addonSlots);}

   public int modifierLevel(Modifier m) {
      int n = 0;

      for (String id : this.modifiers) {
         if (id.equals(m.id())) {
            n++;
         }
      }

      return n;
   }

   public int progressOf(Modifier m) {
      return this.progress.getOrDefault(m.id(), 0);
   }

   public ToolData withMaterial(PartRole role, String material) {
      List<String> list = new ArrayList<>(this.materials);
      list.set(role.ordinal(), material);
      return new ToolData(list, this.modifiers, this.progress, this.level, this.xp, this.expansions, this.addonSlots);
   }

   public ToolData withModifierAdded(Modifier m) {
      List<String> list = new ArrayList<>(this.modifiers);
      list.add(m.id());
      Map<String, Integer> p = new HashMap<>(this.progress);
      p.remove(m.id());
      return new ToolData(this.materials, list, p, this.level, this.xp, this.expansions, this.addonSlots);
   }

   public ToolData withProgress(Modifier m, int points) {
      Map<String, Integer> p = new HashMap<>(this.progress);
      if (points <= 0) {
         p.remove(m.id());
      } else {
         p.put(m.id(), points);
      }

      return new ToolData(this.materials, this.modifiers, p, this.level, this.xp, this.expansions, this.addonSlots);
   }

   public ToolData withLastModifierRemoved() {
      if (!this.modifiers.isEmpty()) {
         List<String> list = new ArrayList<>(this.modifiers);
         list.remove(list.size() - 1);
         return new ToolData(this.materials, list, this.progress, this.level, this.xp, this.expansions, this.addonSlots);
      } else {
         return new ToolData(this.materials, this.modifiers, Map.of(), this.level, this.xp, this.expansions, this.addonSlots);
      }
   }

   public ToolData withXp(int newLevel, long newXp) {
      return new ToolData(this.materials, this.modifiers, this.progress, newLevel, newXp, this.expansions, this.addonSlots);
   }
}
