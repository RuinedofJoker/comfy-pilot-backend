package org.joker.comfypilot.ssh;

public class LineBreakDemo {

    public static void main(String[] args) throws Exception {

        /*System.out.println("=== \\r 示例（回车，不换行） ===");
        System.out.print("Hello World");
        Thread.sleep(1000);
        System.out.print("\rABC");   // 回到行首，覆盖前面内容
        Thread.sleep(1000);
        System.out.println();        // 手动换行

        System.out.println("\n=== \\n 示例（换行） ===");
        System.out.print("Hello\nWorld\n");

        System.out.println("\n=== \\r\\n 示例（Windows 标准换行） ===");
        System.out.print("Hello\r\nWorld\r\n");

        System.out.println("\n=== \\n\\r 示例（几乎不用） ===");
        System.out.print("Hello\n\rWorld\n");*/

        String s = "\r\n  123123 \r \n 231\n";
        String[] split = s.split("\n");
        System.out.println();
    }

}
