package net.ryanland.dfschematics.schematic;

import net.ryanland.dfschematics.df.code.*;
import net.ryanland.dfschematics.df.value.*;
import net.ryanland.dfschematics.schematic.special.Head;
import net.ryanland.dfschematics.schematic.special.TrackedBlock;
import net.sandrohc.schematic4j.schematic.Schematic;

import java.util.ArrayList;
import java.util.List;

public class TemplateFactory {

    private final DFSchematic schematic;
    private final Schematic file;

    public TemplateFactory(DFSchematic schematic) {
        this.schematic = schematic;
        this.file = schematic.getSchematic();
    }

    public List<CodeLine> generate() {
        return splitCodeBlocks(generateCodeBlocks());
    }

    public List<CodeBlock> generateCodeBlocks() {
        // put codeblocks together
        List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(new Function(schematic.getName()));
        codeBlocks.add(getMetadata());
        codeBlocks.addAll(getPalette());
        if (!schematic.getHeads().isEmpty() || !schematic.getSigns().isEmpty()) {
            codeBlocks.addAll(getBlockEntities());
        }
        codeBlocks.addAll(getBlocks());
        if (!schematic.getTrackedBlocks().getBlocks().isEmpty()) {
            codeBlocks.addAll(getTrackedBlocks());
        }
        return codeBlocks;
    }

    public List<CodeLine> splitCodeBlocks(List<CodeBlock> codeBlocks) {
        // splitter
        List<CodeLine> lines = new ArrayList<>();
        int weight = 0;
        CodeLine line = new CodeLine();
        for (CodeBlock block : codeBlocks) {
            weight += block.getWeight();
            line.add(block);
            if (weight >= 52) {
                lines.add(line);
                line = new CodeLine();
                weight = 0;
            }
        }
        if (!line.isEmpty()) lines.add(line);

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
            if (text.length() > 2600) {//create the next string, don't go over data limit
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
            if (text.length() > 2600) {//create the next string, don't go over data limit
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
                if (str.length() > 2600) {
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
