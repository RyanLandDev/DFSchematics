package net.ryanland.dfschematics.schematic;

import net.ryanland.dfschematics.df.code.*;
import net.ryanland.dfschematics.df.value.Text;
import net.ryanland.dfschematics.df.value.Value;
import net.ryanland.dfschematics.df.value.Variable;
import net.ryanland.dfschematics.df.value.Vector;
import net.sandrohc.schematic4j.schematic.Schematic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TemplateFactory {

    private final DFSchematic schematic;
    private final Schematic file;

    public TemplateFactory(DFSchematic schematic) {
        this.schematic = schematic;
        this.file = schematic.getSchematic();
    }

    public List<CodeLine> generate(String fallbackName, boolean split) {
        CodeLine codeLine1 = new CodeLine();
        codeLine1.add(new Function(Objects.requireNonNullElse(file.getName(), fallbackName)));
        codeLine1.add(getMetadata(fallbackName));
        codeLine1.addAll(getPalette());
        List<CodeBlock> blocks = getBlocks();

        List<CodeLine> lines = new ArrayList<>();
        if (split) {
            //max 2 codeblocks per template for block data, for big schematics with large block data, otherwise template nbt max is reached
            List<List<CodeBlock>> splitBlocks = partition(blocks, 2);
            int i = 0;
            for (List<CodeBlock> segment : splitBlocks) {
                if (i == 0) {//for first template with metadata+palette
                    codeLine1.addAll(segment);
                    lines.add(codeLine1);
                    i++;
                    continue;
                }
                CodeLine line = new CodeLine();
                line.addAll(segment);
                lines.add(line);
                i++;
            }
        } else {
            codeLine1.addAll(blocks);
            lines.add(codeLine1);
        }

        return lines;
    }

    private SetVariableCreateList getMetadata(String fallbackName) {
        List<Value> values = new ArrayList<>();

        values.add(new Text(Objects.requireNonNullElse(file.getName(), fallbackName)));
        values.add(new Text(Objects.requireNonNullElse(file.getAuthor(), "Unknown Author")));
        values.add(new Vector(file.getWidth(), file.getHeight(), file.getLength()));

        return new SetVariableCreateList(new Variable("Metadata"), values);
    }

    private List<CodeBlock> getPalette() {
        // convert palette to texts
        List<String> palette = schematic.getStructure().getPalette();
        List<String> texts = new ArrayList<>();
        String text = "";
        for (String material : palette) {
            text += material + ".";
            if (text.length() > 2400) {//don't go over df text limit 2500
                texts.add(text.substring(0, text.length()-1));//-1 removes final comma
                text = "";
            }
        }
        texts.add(text.substring(0, text.length()-1));

        return listToCodeBlocks(texts, "Palette");
    }

    private List<CodeBlock> getBlocks() {
        // convert structure to texts
        StructureContainer structure = schematic.getStructure();
        List<String> texts = new ArrayList<>();
        String text = "";
        for (StructurePart part : structure.getParts()) {
            text += part.getText() + ".";
            if (text.length() > 2400) {//don't go over df text limit 2500
                texts.add(text.substring(0, text.length()-1));//-1 removes final comma
                text = "";
            }
        }
        texts.add(text.substring(0, text.length()-1));

        return listToCodeBlocks(texts, "Blocks");
    }

    private static List<CodeBlock> listToCodeBlocks(List<String> texts, String listName) {
        List<Text> list = texts.stream().map(Text::new).toList();
        List<List<Text>> textsLists = partition(list, 26);// split texts into batches of 26, to fit in codeblock

        List<CodeBlock> line = new ArrayList<>();
        Variable var = new Variable(listName);
        int i = 0;
        for (List<Text> l : textsLists) {
            if (i == 0) line.add(new SetVariableCreateList(var, l));
            else line.add(new SetVariableAppendValue(var, l));
            i++;
        }
        return line;
    }

    private static <T> List<List<T>> partition(List<T> collection, int batchSize) {
        int i = 0;
        List<List<T>> batches = new ArrayList<>();
        while(i < collection.size()) {
            int nextInc = Math.min(collection.size()-i, batchSize);
            List<T> batch = collection.subList(i, i+nextInc);
            batches.add(batch);
            i = i + nextInc;
        }

        return batches;
    }
}
