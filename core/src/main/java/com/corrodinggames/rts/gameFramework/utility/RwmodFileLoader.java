package com.corrodinggames.rts.gameFramework.utility;

import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.file.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.utility.ag */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/utility/ag.class */
public class RwmodFileLoader extends IFileLoader {
    static HashMap zipFileCache = new HashMap();

    public static void logZipMessage(String str) {
        GameEngine.log("Zip: " + str);
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: a */
    public boolean exists(String str) {
        if (str.endsWith(".rwmod") || str.endsWith(".rwmod/") || str.endsWith(".rwmod\\")) {
            return true;
        }
        ZipHelper zipHelperD = getOrOpenZipHelper(str, true);
        if (zipHelperD == null) {
            return false;
        }
        return zipHelperD.c(getPathInZip(str));
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: f */
    public String convertAbstractPathForDebug(String str) {
        return str;
    }

    public static boolean isRwmodPath(String str) {
        if (str.contains(".rwmod/") || str.contains(".rwmod\\") || str.endsWith(".rwmod")) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: d */
    public boolean isDirectory(String str) {
        if (str.endsWith(".rwmod") || str.endsWith(".rwmod/") || str.endsWith(".rwmod\\")) {
            return true;
        }
        ZipHelper zipHelperD = getOrOpenZipHelper(str, true);
        if (zipHelperD == null) {
            return false;
        }
        return zipHelperD.d(getPathInZip(str));
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: e */
    public boolean createDirectory(String str) {
        logZipMessage("createDirectory not supported in zip files: " + str);
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: b */
    public String[] listDir(String str) {
        ZipHelper zipHelperD = getOrOpenZipHelper(str, true);
        if (zipHelperD == null) {
            return null;
        }
        return zipHelperD.e(getPathInZip(str));
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: a */
    public long getSize(String str, boolean z) {
        ZipHelper zipHelperD = getOrOpenZipHelper(str, z);
        if (zipHelperD == null) {
            return -1L;
        }
        return zipHelperD.h(getPathInZip(str));
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: b */
    public AssetInputStream openAssetInputStream(String str, boolean z) {
        ZipHelper zipHelperD = getOrOpenZipHelper(str, z);
        if (zipHelperD == null) {
            return null;
        }
        return zipHelperD.i(getPathInZip(str));
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: g */
    public long getLastModified(String str) {
        String strJ = getRwmodArchivePath(str);
        IFileLoader zipFileLoaderForPath = FileLoaderFactory.getZipFileLoaderForPath(strJ);
        if (zipFileLoaderForPath != null) {
            return zipFileLoaderForPath.getLastModified(strJ);
        }
        return new File(strJ).lastModified();
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: c */
    public OutputStream openOutputStream(String str, boolean z) {
        logZipMessage("writableOutputSteam not supported in zip files: " + str);
        return null;
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: a */
    public boolean rename(String str, String str2) {
        logZipMessage("Rename not supported in zip files: " + str + " to " + str2);
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: c */
    public boolean delete(String str) {
        logZipMessage("Delete not supported in zip files: " + str);
        return false;
    }

    public static String getRwmodArchivePath(String str) {
        int iIndexOf = str.indexOf(".rwmod/");
        int iIndexOf2 = str.indexOf(".rwmod\\");
        if (iIndexOf2 != -1 && (iIndexOf2 < iIndexOf || iIndexOf == -1)) {
            iIndexOf = iIndexOf2;
        }
        if (iIndexOf == -1 && str.endsWith(".rwmod")) {
            iIndexOf = str.length() - ".rwmod".length();
        }
        if (iIndexOf == -1) {
            throw new RuntimeException("Could not find .rwmod in path: " + str);
        }
        return str.substring(0, iIndexOf + ".rwmod".length());
    }

    public static ZipHelper getOrOpenZipHelper(String str, boolean z) {
        ZipHelper zipHelper;
        String strConvertAbstractPath;
        String strJ = getRwmodArchivePath(str);
        synchronized (zipFileCache) {
            ZipHelper zipHelper2 = (ZipHelper) zipFileCache.get(strJ);
            if (zipHelper2 == null) {
                if (z) {
                    strConvertAbstractPath = strJ;
                } else {
                    strConvertAbstractPath = FileHelper.convertAbstractPath(strJ);
                }
                try {
                    try {
                        zipHelper2 = new ZipHelper(strJ, strConvertAbstractPath);
                        zipFileCache.put(strJ, zipHelper2);
                    } catch (IOException openZipException) {
                        logZipMessage("Failed to open source zip: '" + strConvertAbstractPath + "'");
                        openZipException.printStackTrace();
                        String str2 = "Failed to open zip, " + openZipException.getMessage();
                        if (FileHelper.isDirectory(strJ)) {
                            logZipMessage("isDirectory: " + strJ);
                            str2 = "Failed to open .rwmod file (Appears to be a directory!). Please remove .rwmod from any folder names.";
                        }
                        FileHelper.setLastError(str2 + VariableScope.nullOrMissingString);
                        return null;
                    }
                } catch (IllegalArgumentException e2) {
                    logZipMessage("Failed to open source zip: '" + strConvertAbstractPath + "'");
                    e2.printStackTrace();
                    FileHelper.setLastError("Failed to open zip, " + e2.getMessage());
                    return null;
                }
            }
            zipHelper = zipHelper2;
        }
        return zipHelper;
    }

    public static void closeTransferArchives(java.util.Collection<String> paths) {
        java.util.Set<java.nio.file.Path> targets = new java.util.HashSet<>();
        for (String path : paths) {
            targets.add(java.nio.file.Paths.get(FileHelper.convertAbstractPath(path)).toAbsolutePath().normalize());
        }
        synchronized (zipFileCache) {
            for (String key : new java.util.ArrayList<String>(zipFileCache.keySet())) {
                java.nio.file.Path path = java.nio.file.Paths.get(FileHelper.convertAbstractPath(key)).toAbsolutePath().normalize();
                if (targets.contains(path)) {
                    ZipHelper helper = (ZipHelper) zipFileCache.remove(key);
                    if (helper != null) helper.a();
                }
            }
        }
    }

    public static void closeZipFile(String str, boolean z) {
        final String strJ = getRwmodArchivePath(str);
        synchronized (zipFileCache) {
            final ZipHelper zipHelper = (ZipHelper) zipFileCache.remove(strJ);
            if (zipHelper != null) {
                GameEngine.log("Closing zip file: " + strJ);
                new Thread(new Runnable() { // from class: com.corrodinggames.rts.gameFramework.utility.ag.1
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            Thread.sleep(1500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        GameEngine.log("Running delayed close of zip: " + strJ);
                        zipHelper.a();
                    }
                }).start();
            }
        }
    }

    public void closeModFile(String str) {
        closeZipFile(str, false);
    }

    @Override // com.corrodinggames.rts.gameFramework.utility.IFileLoader
    /* JADX INFO: renamed from: a */
    public void closeAll() {
    }

    public static String getPathInZip(String str) {
        String strSubstring = str.substring(getRwmodArchivePath(str).length());
        if (strSubstring.startsWith("/") || strSubstring.startsWith("\\")) {
            strSubstring = strSubstring.substring(1);
        }
        if (strSubstring.startsWith("/") || strSubstring.startsWith("\\")) {
            strSubstring = strSubstring.substring(1);
        }
        if (strSubstring.contains("\\")) {
            strSubstring = strSubstring.replace("\\", "/");
        }
        if (strSubstring.contains("..")) {
            String[] strArrSplitByChar = Utility.splitByChar(strSubstring, '/');
            ArrayList arrayList = new ArrayList(strArrSplitByChar.length);
            int i = 0;
            for (int length = strArrSplitByChar.length - 1; length >= 0; length--) {
                if (strArrSplitByChar[length].equals("..")) {
                    i++;
                } else if (i > 0) {
                    i--;
                } else {
                    arrayList.add(0, strArrSplitByChar[length]);
                }
            }
            if (i != 0) {
                logZipMessage("getPathInZip: Backtracking attempt out of zip: " + strSubstring);
            }
            strSubstring = Utility.joinStrings("/", arrayList);
        }
        return strSubstring;
    }
}
