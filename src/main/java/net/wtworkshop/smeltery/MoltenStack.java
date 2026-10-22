package net.wtworkshop.smeltery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MoltenStack(String molten, int amount) {
   public static final Codec<MoltenStack> CODEC = RecordCodecBuilder.create(
      i -> i.group(Codec.STRING.fieldOf("molten").forGetter(MoltenStack::molten), Codec.intRange(1, 1000000).fieldOf("amount").forGetter(MoltenStack::amount))
            .apply(i, MoltenStack::new)
   );
   public static final StreamCodec<ByteBuf, MoltenStack> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, MoltenStack::molten, ByteBufCodecs.VAR_INT, MoltenStack::amount, MoltenStack::new
   );
}
