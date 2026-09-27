package com.corrodinggames.rts.gameFramework.ui;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.q */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/q.class */
class MinimapPing {

    /* JADX INFO: renamed from: a */
    public boolean isEnemy;

    /* JADX INFO: renamed from: b */
    public int x;

    /* JADX INFO: renamed from: c */
    public int y;

    /* JADX INFO: renamed from: d */
    public float radius;
    /* JADX INFO: renamed from: e */
    public float fadeOutTimer;

    /* JADX INFO: renamed from: f */
    final /* synthetic */ Minimap minimap;

    MinimapPing(Minimap minimap, float f, int i, int i2, boolean z) {
        this.minimap = minimap;
        this.radius = f;
        this.x = i;
        this.y = i2;
        this.isEnemy = z;
    }
}
