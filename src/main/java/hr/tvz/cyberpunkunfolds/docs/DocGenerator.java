package hr.tvz.cyberpunkunfolds.docs;

import hr.tvz.cyberpunkunfolds.config.Config;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class DocGenerator {
    private final DocAssets assets = new DocAssets();
    private final DocRenderer renderer = new DocRenderer();
    private final Config cfg = Config.load();

    public Path generateHtml(String basePackage) {
        try {
            Files.createDirectories(cfg.docsDir());
            Path out = cfg.docsDir().resolve("docs.html");

            List<String> classNames = scanClassNames(basePackage);
            String generatedAt = ZonedDateTime.now().toString();

            String sidebar = renderer.renderSidebar(classNames);
            String content = renderer.renderContent(classNames);

            String html = assets.renderFromTemplate(DocTemplateModel.builder().title("Docs • " + basePackage)
                                                                    .headerTitle("Generated docs")
                                                                    .basePackage(basePackage)
                                                                    .classCount(String.valueOf(classNames.size()))
                                                                    .generatedAt(generatedAt)
                                                                    .toolbar(renderer.renderToolbar())
                                                                    .sidebar(sidebar)
                                                                    .content(content)
                                                                    .build());
            Files.writeString(out, html);
            log.info("Generated docs at {}", out);

            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            if (!latch.await(3000, TimeUnit.MILLISECONDS)) {
                log.debug("UI update still pending after 100ms");
            }

            return out;

        } catch (IOException e) {
            throw new IllegalStateException("Failed generating docs", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while generating docs", e);
        }
    }

    private static List<String> scanClassNames(String basePackage) {
        try (ScanResult scan = new ClassGraph().enableClassInfo()
                                               .acceptPackages(basePackage)
                                               .scan()) {
            return scan.getAllClasses().getNames().stream().sorted().toList();
        }
    }
}
