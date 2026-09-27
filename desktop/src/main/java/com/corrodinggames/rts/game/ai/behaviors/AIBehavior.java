package com.corrodinggames.rts.game.ai.behaviors;

import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;

import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.a.a */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/a/a.class */
public abstract class AIBehavior {
    /* JADX INFO: renamed from: a */
    public abstract AIBehaviorType getBehaviorType();

    /* JADX INFO: renamed from: a */
    public void updateAI(float f, AIController aIController) {
    }

    /* JADX INFO: renamed from: b */
    public void updateCore(float f, AIController aIController) {
    }

    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
    }

    public void a(GameOutputStream gameOutputStream) throws IOException {
    }

    /* JADX INFO: renamed from: a */
    public void onUnitAdded(AIController aIController, OrderableUnit orderableUnit) {
    }

    /* JADX INFO: renamed from: b */
    public void onUnitRemoved(AIController aIController, OrderableUnit orderableUnit) {
    }
}
