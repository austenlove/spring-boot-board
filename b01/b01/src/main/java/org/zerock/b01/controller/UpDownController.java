package org.zerock.b01.controller;

//import io.github.classgraph.Resource;
import io.swagger.annotations.ApiOperation;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.b01.dto.upload.UploadFileDTO;
import org.zerock.b01.dto.upload.UploadResultDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Log4j2
public class UpDownController {

    // 저장 경로 지정, import 시에 springframework로 시작하는 value
    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @ApiOperation(value = "Upload POST", notes = "POST 방식으로 파일 등록")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> upload(UploadFileDTO uploadFileDTO) {

        log.info(uploadFileDTO);

        if(uploadFileDTO.getFiles() != null) {
            
            // 업로드할 파일이 여러개일 경우 여러 개의 정보를 반환할 UploadResultDTO를 list로 구성
            final List<UploadResultDTO> list = new ArrayList<>();

            uploadFileDTO.getFiles().forEach(multipartFile -> {

                String originalName = multipartFile.getOriginalFilename();
                log.info(originalName);

                String uuid = UUID.randomUUID().toString();

                Path savePath = Paths.get(uploadPath, uuid+"_"+originalName);

                // image 여부 초기화(default false)
                boolean image = false;

                try {
                    // 실제파일 저장
                    multipartFile.transferTo(savePath);

                    // 이미지 파일 종류일 때
                    if(Files.probeContentType(savePath).startsWith("image")) {

                        image = true;

                        File thumbFile = new File(uploadPath, "s_" + uuid + "_" + originalName);

                        Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }   // try end

                list.add(UploadResultDTO.builder()
                        .uuid(uuid)
                        .fileName(originalName)
                        .img(image)
                        .build());

            });   // end each

            return list;
        }   // end if

        return null;
    }

    // 첨부파일 조회
    @ApiOperation(value = "view 파일", notes = "GET 방식으로 파일 등록")
    @GetMapping(value = "/view/{fileName}")
    public ResponseEntity<Resource> viewFileGet(@PathVariable String fileName) {

        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();

        try {
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }


    @ApiOperation(value = "remove 파일", notes = "DELETE 방식으로 파일 삭제")
    @DeleteMapping(value = "/remove/{fileName}")
    public Map<String, Boolean> removeFile(@PathVariable String fileName) {

        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

        String resourceName = resource.getFilename();

        Map<String, Boolean> resultMap = new HashMap<>();
        boolean removed = false;

        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            removed = resource.getFile().delete();

            // 썸네일 존재 시
            if(contentType.startsWith("image")) {
                File thumbnailFile = new File(uploadPath + File.separator + "s_" + fileName);
                thumbnailFile.delete();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        resultMap.put("result", removed);
        return resultMap;
    }
}
