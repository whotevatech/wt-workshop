package net.wtworkshop.material;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.Nullable;

public final class ToolMaterial {
   private final String id;
   private final int color;
   private final boolean castable;
   @Nullable
   private final ToolMaterial.HeadStats head;
   @Nullable
   private final ToolMaterial.HandleStats handle;
   @Nullable
   private final ToolMaterial.BindingStats binding;
   private final Map<PartRole, Map<Trait, Integer>> traits;

   private ToolMaterial(ToolMaterial.Builder b) {
      this.id = b.id;
      this.color = b.color;
      this.castable = b.castable;
      this.head = b.head;
      this.handle = b.handle;
      this.binding = b.binding;
      this.traits = b.traits;
   }

   public String id() {
      return this.id;
   }

   public int color() {
      return this.color;
   }

   public boolean castable() {
      return this.castable;
   }

   @Nullable
   public ToolMaterial.HeadStats head() {
      return this.head;
   }

   @Nullable
   public ToolMaterial.HandleStats handle() {
      return this.handle;
   }

   @Nullable
   public ToolMaterial.BindingStats binding() {
      return this.binding;
   }

   public boolean supports(PartRole role) {
      return switch (role) {
         case HEAD -> this.head != null;
         case HANDLE -> this.handle != null;
         case BINDING -> this.binding != null;
      };
   }

   public Map<Trait, Integer> traits(PartRole role) {
      return this.traits.getOrDefault(role, Map.of());
   }

   public String translationKey() {
      int colon = this.id.indexOf(58);
      return colon < 0 ? "material.wt_workshop." + this.id : "material." + this.id.substring(0, colon) + "." + this.id.substring(colon + 1);
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public static ToolMaterial.Builder builder(String id, int rgb) {
      return new ToolMaterial.Builder(id, rgb);
   }

   public static record BindingStats(float durability, float speed) {
   }

   public static final class Builder {
      private final String id;
      private final int color;
      private boolean castable;
      private ToolMaterial.HeadStats head;
      private ToolMaterial.HandleStats handle;
      private ToolMaterial.BindingStats binding;
      private final Map<PartRole, Map<Trait, Integer>> traits = new EnumMap<>(PartRole.class);

      private Builder(String id, int rgb) {
         this.id = id;
         this.color = 0xFF000000 | rgb;
      }

      public ToolMaterial.Builder castable() {
         this.castable = true;
         return this;
      }

      public ToolMaterial.Builder head(int durability, float speed, float attack, Tiers tier) {
         this.head = new ToolMaterial.HeadStats(durability, speed, attack, tier);
         return this;
      }

      public ToolMaterial.Builder handle(float durability, float speed, float attack) {
         this.handle = new ToolMaterial.HandleStats(durability, speed, attack);
         return this;
      }

      public ToolMaterial.Builder binding(float durability, float speed) {
         this.binding = new ToolMaterial.BindingStats(durability, speed);
         return this;
      }

      public ToolMaterial.Builder trait(PartRole role, Trait trait, int level) {
         this.traits.computeIfAbsent(role, r -> new LinkedHashMap<>()).merge(trait, level, Integer::sum);
         return this;
      }

      public ToolMaterial build() {
         return new ToolMaterial(this);
      }
   }

   public static record HandleStats(float durability, float speed, float attack) {
   }

   public static record HeadStats(int durability, float speed, float attack, Tier tier) {
   }
}
