package com.corrodinggames.rts.gameFramework.ui;

/* JADX INFO: Access modifiers changed from: package-private */
/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/b.class */
public abstract class BackgroundTask {

    /* JADX INFO: renamed from: a */
    String taskName;

    /* JADX INFO: renamed from: b */
    abstract void run();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: <init> */
    protected BackgroundTask(String name) {
        this.taskName = name;
    }

    /* JADX INFO: renamed from: a */
    String getTaskName() {
        return this.taskName;
    }
}
