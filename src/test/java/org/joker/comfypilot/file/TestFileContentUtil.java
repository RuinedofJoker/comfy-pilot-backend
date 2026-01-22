package org.joker.comfypilot.file;

import org.joker.comfypilot.common.util.FileContentUtil;
import org.junit.jupiter.api.Test;

public class TestFileContentUtil {

    @Test
    public void testFileContentUtil() {
        System.out.println(FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\mlk.flac"));
    }

    @Test
    public void testFileContentUtil2() {
        System.out.println(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\35507798632-1-192.mp4").length());
        System.out.println(FileContentUtil.getFileSize("C:\\Users\\61640\\Desktop\\35507798632-1-192.mp4"));
    }
}
