package org.joker.comfypilot.file;

import org.joker.comfypilot.common.util.FileContentUtil;
import org.junit.jupiter.api.Test;

public class TestFileContentUtil {

    @Test
    public void testFileContentUtil() {
        System.out.println(FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\mlk.flac"));
    }
}
