package nl.weeaboo.vn.impl.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import nl.weeaboo.filesystem.FilePath;
import nl.weeaboo.filesystem.FileSystemUtil;
import nl.weeaboo.filesystem.IWritableFileSystem;
import nl.weeaboo.vn.core.MediaType;
import nl.weeaboo.vn.core.ResourceId;
import nl.weeaboo.vn.impl.core.FileResourceLoader;

public class FileResourceLoaderTest {

    private static final FilePath BASE_FOLDER = FilePath.of("base/");

    private FileResourceLoader resourceLoader;
    private ResourceId lastPreload;

    @Before
    public void before() throws IOException {
        TestEnvironment env = TestEnvironment.newInstance();

        IWritableFileSystem outputFileSystem = env.getOutputFileSystem();
        writeTestFiles(outputFileSystem);

        resourceLoader = new FileResourceLoader(env, MediaType.OTHER, BASE_FOLDER) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void preloadNormalized(ResourceId resourceId) {
                super.preloadNormalized(resourceId);

                lastPreload = resourceId;
            }
        };
    }

    private void writeTestFiles(IWritableFileSystem fs) throws IOException {
        writeString(fs, "base/valid.png");
        writeString(fs, "base/valid.jpg");
        writeString(fs, "base/subfolder/a.txt");
        writeString(fs, "base/subfolder/b.txt");
        writeString(fs, "invalid.jpg");
    }

    @Test
    public void isValidFilename() {
        // Resource loader paths are relative to the base folder
        assertValidFilename(true, "valid.jpg");
        assertValidFilename(true, "valid.png");
        assertValidFilename(true, "subfolder/a.txt");
        assertValidFilename(true, "subfolder/b.txt");
        assertValidFilename(false, "invalid.jpg");

        // Null filenames just return false and don't throw an exception
        assertValidFilename(false, null);
    }

    @Test
    public void normalizeFilename() {
        resourceLoader.setAutoFileExts("png", "jpg");
        assertNormalizedFilename("valid.png", "valid");
        assertNormalizedFilename("valid.jpg", "valid.jpg");
        assertNormalizedFilename(null, "subfolder/a");
        resourceLoader.setAutoFileExts("txt");
        assertNormalizedFilename("subfolder/a.txt", "subfolder/a");

        // Null filenames just return null and don't throw an exception
        assertNormalizedFilename(null, null);
    }

    /** Check behavior when changing the base resource folder at runtime */
    @Test
    public void changeResourceFolder() {
        Assert.assertEquals(BASE_FOLDER, resourceLoader.getResourceFolder());
        assertValidFilename(true, "valid.jpg");
        assertValidFilename(false, "a.txt");

        // Change resource folder
        FilePath subFolder = BASE_FOLDER.resolve("subfolder/");
        resourceLoader.setResourceFolder(subFolder);
        Assert.assertEquals(subFolder, resourceLoader.getResourceFolder());
        Assert.assertEquals(subFolder.resolve("test"), resourceLoader.getAbsolutePath(FilePath.of("test")));
        assertValidFilename(false, "valid.jpg");
        assertValidFilename(true, "a.txt");
        assertFiles(Arrays.asList("a.txt", "b.txt"));
    }

    /** Check that the preload method calls preloadNormalized for any valid filename */
    @Test
    public void testPreload() {
        resourceLoader.setAutoFileExts("png");

        assertPreload("valid.jpg", "valid.jpg");
        assertPreload("valid.png", "valid.png");
        // Incorrect file ext is automatically fixed
        assertPreload("valid.png", "valid.txt");
        assertPreload("valid.png", "valid");
        assertPreload(null, "invalid");
    }

    private void assertPreload(String expectedNormalized, String inputFilename) {
        lastPreload = null;
        resourceLoader.preload(FilePath.of(inputFilename));
        String actual = (lastPreload != null ? lastPreload.getFilePath().toString() : null);
        Assert.assertEquals(expectedNormalized, actual);
    }

    private void assertFiles(Collection<String> expected) {
        ImmutableSet.Builder<FilePath> expectedSet = ImmutableSet.builder();
        for (String exp : expected) {
            expectedSet.add(FilePath.of(exp));
        }

        Assert.assertEquals(expectedSet.build(),
                ImmutableSet.copyOf(resourceLoader.getMediaFiles(FilePath.empty())));
    }

    private void assertNormalizedFilename(String expectedNormalized, String inputFilename) {
        ResourceId resourceId = resourceLoader.resolveResource(
                inputFilename != null ? FilePath.of(inputFilename) : null);
        String actual = (resourceId != null ? resourceId.getFilePath().toString() : null);
        Assert.assertEquals(expectedNormalized, actual);
    }

    private void assertValidFilename(boolean expectedValid, String path) {
        Assert.assertEquals(expectedValid, resourceLoader.isValidFilename(
                path != null ? FilePath.of(path) : null));
    }

    private static void writeString(IWritableFileSystem fs, String path) throws IOException {
        FileSystemUtil.writeString(fs, FilePath.of(path), path);
    }

}
