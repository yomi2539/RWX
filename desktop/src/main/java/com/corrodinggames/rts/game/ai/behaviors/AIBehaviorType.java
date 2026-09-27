package com.corrodinggames.rts.game.ai.behaviors;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.a.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/a/b.class */
public enum AIBehaviorType {
    unknown { // from class: com.corrodinggames.rts.game.a.a.b.1
        @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehaviorType
        /* JADX INFO: renamed from: a */
        public AIBehavior createBehavior() {
            return null;
        }
    },
    nuking { // from class: com.corrodinggames.rts.game.a.a.b.2
        @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehaviorType
        /* JADX INFO: renamed from: a */
        public AIBehavior createBehavior() {
            return new NukeBehavior();
        }
    };

    /* JADX INFO: renamed from: a */
    public abstract AIBehavior createBehavior();
}
