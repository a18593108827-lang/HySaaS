package com.hysaas.storage;

import com.hysaas.common.exception.BizException;
import com.hysaas.framework.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_PDF_BYTES = 20 * 1024 * 1024;
    private static final Set<String> PDF_TYPES = Set.of("application/pdf", "application/x-pdf");

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String storePaperPdf(Long paperId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择 PDF 文件");
        }
        if (file.getSize() > MAX_PDF_BYTES) {
            throw new BizException("PDF 不能超过 20MB");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename();
        if (!isPdf(contentType, name)) {
            throw new BizException("仅支持 PDF 格式");
        }
        String key = "papers/" + paperId + "/" + UUID.randomUUID().toString().replace("-", "") + ".pdf";
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(key)
                    .stream(in, file.getSize(), -1)
                    .contentType("application/pdf")
                    .build());
        } catch (Exception e) {
            throw new BizException("文件上传失败，请确认 MinIO 已启动");
        }
        return key;
    }

    private boolean isPdf(String contentType, String filename) {
        if (StringUtils.hasText(contentType) && PDF_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        return StringUtils.hasText(filename) && filename.toLowerCase().endsWith(".pdf");
    }
}
