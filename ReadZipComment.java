package com.example.readzipcomment;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipFile;

public class ReadZipComment {


    public static String readZipComment(Context context) {
        String filePath = context.getPackageCodePath();
        String comment = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            comment = readZipCommentAboveKitKat(filePath);
        } else {
            comment = readZipCommentBelowKitKat(filePath);
        }

        return comment;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String readZipCommentAboveKitKat(String filePath) {
        String comment;
        ZipFile file = null;
        try {
            file = new ZipFile(filePath);
            comment = file.getComment();
        } catch (Exception e) {
            e.printStackTrace();
            comment = "";
        }
        return comment;
    }


    private static String readZipCommentBelowKitKat(String filePath) {
        String comment = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            int fileLen = (int) file.length();
            in = new FileInputStream(file);
            byte[] buffer = new byte[Math.min(fileLen, 8192)];
            int len;
            in.skip(fileLen - buffer.length);
            if ((len = in.read(buffer)) > 0) {
                comment = getZipCommentFromBuffer(buffer, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return comment;
    }

    private static String getZipCommentFromBuffer(byte[] buffer, int len) {
        byte[] endOfDirectoryFlag = {0x50, 0x4b, 0x05, 0x06};
        int buffLen = Math.min(buffer.length, len);

        for (int i = buffLen - endOfDirectoryFlag.length - 22; i >= 0; i--) {
            boolean isEndOfDirectoryFlag = true;
            for (int k = 0; k < endOfDirectoryFlag.length; k++) {
                if (buffer[i + k] != endOfDirectoryFlag[k]) {
                    isEndOfDirectoryFlag = false;
                    break;
                }
            }
            if (isEndOfDirectoryFlag) {
                int commentLen = buffer[i + 20] + buffer[i + 22] * 256;
                int realLen = buffLen - i - 22;
                String comment = new String(buffer, i + 22, Math.min(commentLen, realLen));
                return comment;
            }
        }
        return "";
    }
}