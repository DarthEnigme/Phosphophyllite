package net.roguelogix.phosphophyllite.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

@NonnullDefault
public final class InteractionResults {
    private InteractionResults() {
    }
    
    /**
     * Maps a pre-1.20.5 style block use result onto the item-use result used by Block#useItemOn
     * PASS falls through to the default (item-less) block interaction, just like it used to fall through to the item
     */
    public static ItemInteractionResult toItemResult(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
}
