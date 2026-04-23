package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.model.dto.MdHeadingVecDto;
import com.kano.main_data.model.dto.MdParagraphVecDto;
import com.kano.main_data.service.MarkDownService;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MarkDownServiceImpl implements MarkDownService {

    @Override
    public void parseMd(String fileName, String mdContent) {
        Parser parser = Parser.builder(new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                TocExtension.create(),
                FootnoteExtension.create(),
                YamlFrontMatterExtension.create(),
                EmojiExtension.create()
        ))).build();

        Node document = parser.parse(mdContent);

        List<MdHeadingVecDto> headings = new ArrayList<>();
        List<MdParagraphVecDto> paragraphs = new ArrayList<>();
        List<String> path = new ArrayList<>();
        path.add(fileName);

        addHeading(headings, path);
        traverse(document, headings, paragraphs, path);

        printResultTree(headings, paragraphs);
    }

    private void traverse(Node node,
                          List<MdHeadingVecDto> headings,
                          List<MdParagraphVecDto> paragraphs,
                          List<String> path) {
        int pushed = 0;

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Heading heading) {
                path.add(clean(heading.getText().toString()));
                addHeading(headings, path);
                pushed++;
                continue;
            }

            String headingId = headings.get(path.size() - 1).getHeadingId();

            if (child instanceof Paragraph paragraph) {
                addParagraph(paragraphs, clean(paragraph.getChars().toString()), headingId);
            } else if (child instanceof ListBlock listBlock) {
                for (String item : extractListItems(listBlock)) {
                    addParagraph(paragraphs, item, headingId);
                }
            } else if (child instanceof TableBlock tableBlock) {
                for (String row : extractTableRows(tableBlock)) {
                    addParagraph(paragraphs, row, headingId);
                }
            } else if (child instanceof BlockQuote quote) {
                traverse(quote, headings, paragraphs, path);
            } else if (child instanceof Block block) {
                addParagraph(paragraphs, clean(block.getChars().toString()), headingId);
            }
        }

        while (pushed-- > 0) {
            path.remove(path.size() - 1);
        }
    }

    private List<String> extractListItems(ListBlock listBlock) {
        List<String> result = new ArrayList<>();
        for (Node child = listBlock.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof ListItem item) {
                String text = extractNodeText(item);
                if (!text.isBlank()) result.add(text);

                for (Node sub = item.getFirstChild(); sub != null; sub = sub.getNext()) {
                    if (sub instanceof ListBlock nested) {
                        result.addAll(extractListItems(nested));
                    }
                }
            } else if (child instanceof ListBlock nested) {
                result.addAll(extractListItems(nested));
            }
        }
        return result;
    }

    private List<String> extractTableRows(TableBlock tableBlock) {
        List<List<String>> rows = new ArrayList<>();
        collectTableRows(tableBlock, rows);

        if (rows.isEmpty()) {
            String raw = clean(tableBlock.getChars().toString());
            return raw.isBlank() ? Collections.emptyList() : List.of(raw);
        }

        if (rows.size() == 1) {
            return List.of(String.join(" ", rows.get(0)));
        }

        List<String> headers = rows.get(0);
        List<String> result = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            List<String> kv = new ArrayList<>();

            for (int j = 0; j < Math.min(headers.size(), row.size()); j++) {
                String h = clean(headers.get(j));
                String v = clean(row.get(j));
                if (isSeparatorCell(h) || isSeparatorCell(v)) continue;
                if (h.isBlank() && v.isBlank()) continue;
                kv.add(h.isBlank() ? v : (v.isBlank() ? h + ":" : h + ": " + v));
            }

            if (!kv.isEmpty()) result.add(String.join(", ", kv));
        }

        if (result.isEmpty()) {
            String raw = clean(tableBlock.getChars().toString());
            if (!raw.isBlank()) result.add(raw);
        }

        return result;
    }

    private void collectTableRows(Node node, List<List<String>> rows) {
        if (node instanceof TableRow row) {
            List<String> cells = new ArrayList<>();
            for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
                String text = clean(cell.getChars() == null ? "" : cell.getChars().toString());
                if (cell instanceof TableCell || !text.isBlank()) {
                    cells.add(text);
                }
            }
            if (!cells.isEmpty() && !isSeparatorRow(cells)) rows.add(cells);
            return;
        }

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            collectTableRows(child, rows);
        }
    }

    private boolean isSeparatorRow(List<String> cells) {
        for (String cell : cells) {
            if (!isSeparatorCell(cell)) return false;
        }
        return true;
    }

    private boolean isSeparatorCell(String text) {
        return text == null || text.trim().replace(":", "").replace("-", "").trim().isEmpty();
    }

    private String extractNodeText(Node node) {
        List<String> parts = new ArrayList<>();
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof ListBlock) continue;
            String text = clean(child.getChars() == null ? "" : child.getChars().toString());
            if (!text.isBlank()) parts.add(text);
        }
        return String.join(" ", parts);
    }

    private void addHeading(List<MdHeadingVecDto> headings, List<String> path) {
        headings.add(MdHeadingVecDto.builder()
                .headingId(UUID.randomUUID().toString().replace("-", ""))
                .content(String.join(" > ", path))
                .build());
    }

    private void addParagraph(List<MdParagraphVecDto> paragraphs, String content, String headingId) {
        if (content.isBlank()) return;
        paragraphs.add(MdParagraphVecDto.builder()
                .headingId(headingId)
                .paragraphId(UUID.randomUUID().toString().replace("-", ""))
                .content(content.trim())
                .build());
    }

    private String clean(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private void printResultTree(List<MdHeadingVecDto> headings, List<MdParagraphVecDto> paragraphs) {
        log.info("====== Result Tree ======");
        if (headings == null || headings.isEmpty()) return;

        Map<String, MdHeadingVecDto> byId = new LinkedHashMap<>();
        Map<String, String> idByPath = new HashMap<>();
        Map<String, List<String>> children = new LinkedHashMap<>();
        Map<String, List<MdParagraphVecDto>> paras = new LinkedHashMap<>();

        for (MdHeadingVecDto h : headings) {
            byId.put(h.getHeadingId(), h);
            idByPath.put(h.getContent(), h.getHeadingId());
            children.putIfAbsent(h.getHeadingId(), new ArrayList<>());
        }

        String rootId = headings.get(0).getHeadingId();
        for (MdHeadingVecDto h : headings) {
            if (h.getHeadingId().equals(rootId)) continue;
            String parent = parentPathOf(h.getContent());
            children.get(idByPath.getOrDefault(parent, rootId)).add(h.getHeadingId());
        }

        for (MdParagraphVecDto p : paragraphs) {
            paras.computeIfAbsent(p.getHeadingId(), k -> new ArrayList<>()).add(p);
        }

        printSubTree(rootId, byId, children, paras, 0);
    }

    private void printSubTree(String id,
                              Map<String, MdHeadingVecDto> byId,
                              Map<String, List<String>> children,
                              Map<String, List<MdParagraphVecDto>> paras,
                              int depth) {
        String indent = "  ".repeat(depth);
        MdHeadingVecDto h = byId.get(id);

        log.info(indent + "- H: " + (h == null ? "(missing)" : clean(h.getContent())));
        for (MdParagraphVecDto p : paras.getOrDefault(id, Collections.emptyList())) {
            log.info(indent + "  * P: " + clean(p.getContent()));
        }
        for (String child : children.getOrDefault(id, Collections.emptyList())) {
            printSubTree(child, byId, children, paras, depth + 1);
        }
    }

    private String parentPathOf(String path) {
        if (path == null) return null;
        int idx = path.lastIndexOf(" > ");
        return idx < 0 ? null : path.substring(0, idx);
    }

}