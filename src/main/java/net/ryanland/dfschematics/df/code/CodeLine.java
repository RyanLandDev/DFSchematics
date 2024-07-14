package net.ryanland.dfschematics.df.code;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Represents a DiamondFire code line.
 */
public class CodeLine {

    private final List<CodeBlock> codeBlocks = new ArrayList<>();

    public List<CodeBlock> getCodeBlocks() {
        return codeBlocks;
    }

    public boolean isEmpty() {
        return getCodeBlocks().isEmpty();
    }

    /**
     * Adds a {@link CodeBlock} to this line at the end.
     * @param codeBlock The code block to add
     */
    public void add(CodeBlock codeBlock) {
        codeBlocks.add(codeBlock);
    }

    /**
     * Adds {@link CodeBlock CodeBlocks} to this line at the end.
     * @param codeBlocks The code blocks to add
     */
    public void add(CodeBlock... codeBlocks) {
        this.codeBlocks.addAll(List.of(codeBlocks));
    }

    /**
     * Adds {@link CodeBlock CodeBlocks} to this line at the end.
     * @param codeBlocks The code blocks to add
     */
    public <C extends Collection<B>, B extends CodeBlock> void addAll(C codeBlocks) {
        this.codeBlocks.addAll(codeBlocks);
    }

    /**
     * Adds a {@link CodeBlock} to this line at the end minus a provided index.
     * @param codeBlock The code block to add
     * @param insertBefore Offset index for the end, e.g. 1 would add this code block to the second-last position.
     */
    public void add(CodeBlock codeBlock, int insertBefore) {
        codeBlocks.add(codeBlocks.size() - 1 - insertBefore, codeBlock);
    }

    /**
     * Inserts a {@link CodeBlock} in this line at the specified index.
     * @param index The index to add the code block at
     * @param codeBlock The code block to insert
     */
    public void insert(int index, CodeBlock codeBlock) {
        codeBlocks.add(index, codeBlock);
    }

    public JsonObject toJson() {
        JsonArray blocks = new JsonArray();
        codeBlocks.forEach(block -> blocks.add(block.toJson()));

        JsonObject json = new JsonObject();
        json.add("blocks", blocks);
        return json;
    }

    public String toCompressedJson() throws IOException {
        return compressGzipBase64(toJson().toString());
    }

    private static String compressGzipBase64(String uncompressed) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressed.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);

        // Convert to gzip
        gzip.write(uncompressed.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();

        // Convert to base64 and returns it
        //return new String(compressed, StandardCharsets.UTF_8);
        return new String(Base64.getEncoder().encode(compressed), StandardCharsets.UTF_8);
    }
}
