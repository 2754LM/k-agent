package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.model.dto.FileInfoDto;
import com.kano.main_data.model.dto.MdHeadingVecDto;
import com.kano.main_data.model.dto.MdParagraphVecDto;
import com.kano.main_data.service.MarkDownService;
import com.kano.main_data.service.RagService;
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
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    RagService ragService;

    private static class PathEntry {
        String text;
        int level;

        PathEntry(String text, int level) {
            this.text = text;
            this.level = level;
        }
    }

    @Override
    public void parseMd(FileInfoDto fileInfoDto, String mdContent) {
        String fileName = fileInfoDto.getName();
        String fileId = fileInfoDto.getId();

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
        // 完整路径 -> headingId 的快速映射
        Map<String, String> pathToHeadingId = new HashMap<>();

        // 根路径（文件名，级别 0）
        List<PathEntry> rootPath = new ArrayList<>();
        rootPath.add(new PathEntry(fileName, 0));
        String rootPathStr = fileName;
        String rootHeadingId = UUID.randomUUID().toString().replace("-", "");
        pathToHeadingId.put(rootPathStr, rootHeadingId);
        headings.add(MdHeadingVecDto.builder()
                .fileId(fileId)
                .headingId(rootHeadingId)
                .content(rootPathStr)
                .build());

        StringBuilder buffer = new StringBuilder();
        traverse(document, headings, paragraphs, rootPath, fileId, buffer, pathToHeadingId);
        // 文档末尾可能还有未 flush 的内容
        flushBuffer(buffer, paragraphs, rootPath, pathToHeadingId);

        printResultTree(headings, paragraphs);
        ragService.saveMd(headings, paragraphs);
    }

    private void traverse(Node node,
                          List<MdHeadingVecDto> headings,
                          List<MdParagraphVecDto> paragraphs,
                          List<PathEntry> parentPath, // 上级调用者的路径（只读）
                          String fileId,
                          StringBuilder buffer,
                          Map<String, String> pathToHeadingId) {
        // 使用本地的路径副本，避免污染父级
        List<PathEntry> currentPath = new ArrayList<>(parentPath);

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Heading heading) {
                // 遇到新标题，先 flush 之前累积的内容（属于当前标题）
                flushBuffer(buffer, paragraphs, currentPath, pathToHeadingId);

                int newLevel = heading.getLevel(); // 1~6
                // 弹出所有级别 >= newLevel 的路径条目（包括旧的同级标题）
                while (!currentPath.isEmpty() && currentPath.get(currentPath.size() - 1).level >= newLevel) {
                    currentPath.remove(currentPath.size() - 1);
                }
                // 添加新标题条目
                currentPath.add(new PathEntry(clean(heading.getText().toString()), newLevel));
                // 注册标题对象，并建立路径映射
                String currentPathStr = pathToString(currentPath);
                String headingId = UUID.randomUUID().toString().replace("-", "");
                pathToHeadingId.put(currentPathStr, headingId);
                headings.add(MdHeadingVecDto.builder()
                        .fileId(fileId)
                        .headingId(headingId)
                        .content(currentPathStr)
                        .build());
                continue;
            }

            // 以下将所有非标题节点的文本追加到 buffer
            if (child instanceof Paragraph paragraph) {
                buffer.append(clean(paragraph.getChars().toString())).append(" ");
            } else if (child instanceof ListBlock listBlock) {
                List<String> items = extractListItems(listBlock);
                String joined = String.join("; ", items);
                if (!joined.isBlank()) {
                    buffer.append(joined).append(" ");
                }
            } else if (child instanceof TableBlock tableBlock) {
                List<String> rows = extractTableRows(tableBlock);
                String joined = String.join("; ", rows);
                if (!joined.isBlank()) {
                    buffer.append(joined).append(" ");
                }
            } else if (child instanceof BlockQuote quote) {
                // 引用块递归，传入当前路径副本，其内部标题不会影响外部
                traverse(quote, headings, paragraphs, currentPath, fileId, buffer, pathToHeadingId);
            } else if (child instanceof Block block) {
                buffer.append(clean(block.getChars().toString())).append(" ");
            }
        }

        // 本层遍历结束，flush 剩余内容（属于当前标题）
        flushBuffer(buffer, paragraphs, currentPath, pathToHeadingId);
    }

    private void flushBuffer(StringBuilder buffer,
                             List<MdParagraphVecDto> paragraphs,
                             List<PathEntry> currentPath,
                             Map<String, String> pathToHeadingId) {
        if (buffer.length() == 0) return;
        String text = buffer.toString().trim();
        buffer.setLength(0);
        if (text.isEmpty()) return;

        String pathStr = pathToString(currentPath);
        String headingId = pathToHeadingId.get(pathStr);
        // 正常情况下 headingId 一定存在，若不存在则用空，避免 NPE
        if (headingId == null) headingId = "";

        String prefix = pathStr;
        addParagraph(paragraphs, prefix + ": " + text, headingId);
    }

    // 将路径条目列表转为 " > " 分隔的字符串
    private String pathToString(List<PathEntry> path) {
        List<String> parts = new ArrayList<>();
        for (PathEntry entry : path) {
            parts.add(entry.text);
        }
        return String.join(" > ", parts);
    }

    // ------------- 列表和表格提取逻辑（保持不变） -------------
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
    // -----------------------------------------------------------

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

    // ------------- 调试打印，已适配新的 headings 结构 -------------
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
