package net.wtworkshop.client;

import net.minecraft.client.gui.screens.Screen;

public final class ClientHooks {
   private ClientHooks() {
   }

   public static boolean shiftDown() {
      return Screen.hasShiftDown();
   }
}
