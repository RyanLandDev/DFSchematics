package net.ryanland.dfschematics.schematic;

import net.ryanland.dfschematics.df.code.*;
import net.ryanland.dfschematics.df.value.*;
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

    public List<CodeLine> generate(boolean split) {
        CodeLine codeLine1 = new CodeLine();
        codeLine1.add(new Function(schematic.getName()));
        codeLine1.add(getMetadata());
        codeLine1.addAll(getPalette());

        // block entities
        if (!schematic.getHeads().isEmpty() || !schematic.getSigns().isEmpty()) {
            codeLine1.addAll(getBlockEntities());
        }

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

        if (!schematic.getTrackedBlocks().getBlocks().isEmpty()) {
            CodeLine line = new CodeLine();
            line.addAll(getTrackedBlocks());
            lines.add(line);
        }

        return lines;
    }

    private SetVariableCreateList getMetadata() {
        List<Value> values = new ArrayList<>();

        values.add(new Str(schematic.getName()));
        values.add(new Str(schematic.getAuthor()));
        values.add(new Vector(file.width(), file.height(), file.length()));

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

        return textsListToCodeBlocks(texts, "Palette");
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

        return textsListToCodeBlocks(texts, "Blocks");
    }

    private List<CodeBlock> getBlockEntities() {
        List<Value> values = new ArrayList<>();

        // heads format: H6,1,8,RyanLand;3,4,5,oiaejroaiejroaapPEOFIJapeoifja
        List<Head> heads = schematic.getHeads();
        if (heads.size() > 0) {
            String str = "H";
            for (Head head : heads) {
                str += head.pos().x + "," + head.pos().y + "," + head.pos().z + "," + head.texture() + ";";
                if (str.length() > 2400) {
                    values.add(new Str(str.substring(0, str.length() - 1)));
                    str = "H";
                }
            }
            values.add(new Str(str.substring(0, str.length() - 1)));
        }

        // signs use a sign item with lores
        values.addAll(schematic.getSigns());

        return listToCodeBlocks(values, "BlockEntities");
    }

    private List<CodeBlock> getTrackedBlocks() {
        List<CodeBlock> blocks = new ArrayList<>();
        Location offset = new Location((float) schematic.getTrackedBlocks().getOffset()[0], (float) schematic.getTrackedBlocks().getOffset()[1], (float) schematic.getTrackedBlocks().getOffset()[2]);
        for (TrackedBlock block : schematic.getTrackedBlocks().getBlocks()) {
            blocks.addAll(listToCodeBlocks(block.getLocations().stream().map(loc -> loc.add(offset)).toList(), block.getVariable()));
        }
        return blocks;
    }

    private static List<CodeBlock> textsListToCodeBlocks(List<String> texts, String listName) {
        return listToCodeBlocks(texts.stream().map(Str::new).toList(), listName);
    }

    private static <V extends Value> List<CodeBlock> listToCodeBlocks(List<V> list, String listName) {
        return listToCodeBlocks(list, new Variable(listName));
    }

    private static <V extends Value> List<CodeBlock> listToCodeBlocks(List<V> list, Variable var) {
        List<List<V>> valuesLists = partition(list, 26);// split values into batches of 26, to fit in codeblock

        List<CodeBlock> line = new ArrayList<>();
        int i = 0;
        for (List<V> l : valuesLists) {
            if (i == 0) line.add(new SetVariableCreateList(var, l));
            else line.add(new SetVariableAppendValue(var, l));
            i++;
        }
        return line;
    }

    private static <T> List<List<T>> partition(List<T> collection, int batchSize) {
        int i = 0;
        List<List<T>> batches = new ArrayList<>();
        while (i < collection.size()) {
            int nextInc = Math.min(collection.size()-i, batchSize);
            List<T> batch = collection.subList(i, i+nextInc);
            batches.add(batch);
            i = i + nextInc;
        }

        return batches;
    }
}
