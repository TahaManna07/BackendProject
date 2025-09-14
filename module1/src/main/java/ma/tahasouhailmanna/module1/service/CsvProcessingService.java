package ma.tahasouhailmanna.module1.service;


import ma.tahasouhailmanna.module1.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CsvProcessingService {
    @Value("${csv.processing.base-dir:${user.dir}}")
    private String baseDir;
    @Value("${csv.processing.input-dir}")
    private String inputDir;

    private Path resolvePath(String p) {
        Path path = Paths.get(p);
        if (path.isAbsolute()) return path.normalize();
        return Paths.get(baseDir).resolve(p).normalize().toAbsolutePath();
    }

    // Only store the uploaded file into dataInput
    public FileUploadResponse storeCsv(MultipartFile file) throws Exception {
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "products.csv");
        String base = original.toLowerCase().endsWith(".csv") ? original.substring(0, original.length() - 4) : original;
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());

        Path inDir = resolvePath(inputDir);          // ensure absolute
        Files.createDirectories(inDir);

        Path inputPath = inDir.resolve(base + "_" + ts + ".csv");
        Files.createDirectories(inputPath.getParent()); // ensure parent exists

        file.transferTo(inputPath.toFile());          // absolute target -> no temp resolution
        return new FileUploadResponse(inputPath.toString(), inputPath.getFileName().toString(), Files.size(inputPath));
    }
}