package nl.weeaboo.vn.impl.script.lvn;

import nl.weeaboo.filesystem.FilePath;

public interface ICompiledLvnFile {

    /**
     * @return The filename of the LVN source file.
     */
    public FilePath getFilename();

    /**
     * @return The compiled version of the LVN file.
     */
    public String getCompiledContents();

    /**
     * Counts the number of text-mode lines in the compiled LVN file.
     * @param countEmptyLines If {@code true}, count lines that are empty or consist entirely of whitespace.
     */
    public int countTextLines(boolean countEmptyLines);

    /**
     * Counts the number of words in text-mode lines in the compiled LVN file.
     */
    public int countTextWords();

}
