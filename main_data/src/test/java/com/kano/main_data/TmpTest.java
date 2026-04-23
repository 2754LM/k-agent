package com.kano.main_data;

import com.kano.main_data.registry.EmbeddingModelRegistry;
import com.kano.main_data.service.MarkDownService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TmpTest {
    @Autowired
    private List<ToolCallbackProvider> toolCallbackProviders;
    @Autowired
    EmbeddingModelRegistry embeddingModelRegistry;
    @Autowired
    MarkDownService markDownService;

    @Test
    public void testToolCallbackProvider() {
        markDownService.parseMd("test.md", """
                Threadlocal本身不存储数据，而是通过Thread对象内部的Threadlocals的map来存储数据，这个map的key是Threadlocal本身弱引用，value是要存储的值强引用。
                
                | 工具类             | 核心用途                                     | 是否可重用         | 关键角色           |
                | :----------------- | :------------------------------------------- | :----------------- | :----------------- |
                | **CountDownLatch** | 一个或多个线程等待**一组事件**发生           | **否** (一次性)    | 主线程等待工作线程 |
                | **CyclicBarrier**  | **一组线程**相互等待，到达屏障后**一起继续** | **是**             | 所有线程相互对等   |
                | **Semaphore**      | 控制**访问特定资源的线程数量**               | 是（许可证可释放） | 资源管理者         |
                
                JVM
                
                Java类的加载过程分为加载、连接、初始化三个大阶段。其中连接又细分为验证、准备、解析。（有在第一次主动使用时才会初始化）
                
                1. **加载**：查找并加载类的二进制数据，在内存中生成一个Class对象。
                
                2. 连接
                
                   ：
                
                   - **验证**：确保被加载的类是正确且安全的。
                   - **准备**：为类的静态变量分配内存并设置默认初始值（0, false, null等）。
                   - **解析**：将符号引用转换为直接引用。
                
                3. **初始化**：执行类构造器 `<clinit>()` 方法，为静态变量赋程序设定的值，并执行静态代码块。
                """);
    }

}
