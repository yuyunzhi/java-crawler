package com.github.hcsp.io;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileAccessorTest {
    @Test
    public void test1() throws Exception {
        File tmp = File.createTempFile("tmp", "");
        List<String> list = Arrays.asList("a", "b", " ", "   ", "c");
        FileAccessor.writeLinesToFile1(list, tmp);
        Assertions.assertEquals(list, FileAccessor.readFile1(tmp));
    }

    @Test
    public void test2() throws Exception {
        File tmp = File.createTempFile("tmp", "");
        List<String> list = Arrays.asList("a", "b", " ", "   ", "c");
        FileAccessor.writeLinesToFile2(list, tmp);
        Assertions.assertEquals(list, FileAccessor.readFile2(tmp));
    }

    @Test
    public void test3() throws Exception {
        File tmp = File.createTempFile("tmp", "");
        List<String> list = Arrays.asList("a", "b", " ", "   ", "c");
        FileAccessor.writeLinesToFile3(list, tmp);
        Assertions.assertEquals(list, FileAccessor.readFile3(tmp));
    }

    @Test
    public void testUnexisting() throws Exception {
        Assertions.assertThrows(
                Exception.class, () -> FileAccessor.readFile1(new File("unexisting")));
        Assertions.assertThrows(
                Exception.class, () -> FileAccessor.readFile2(new File("unexisting")));
        Assertions.assertThrows(
                Exception.class, () -> FileAccessor.readFile3(new File("unexisting")));
    }
}
